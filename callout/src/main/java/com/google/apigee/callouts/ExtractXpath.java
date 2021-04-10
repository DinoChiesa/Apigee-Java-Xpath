// Copyright 2018-2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.google.apigee.callouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.XPathEvaluator;
import java.util.Map;
import java.util.HashMap;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtractXpath extends XpathCalloutBase implements Execution {

    public ExtractXpath(Map properties) {
        super(properties);
    }

    private XPathEvaluator getXpe(MessageContext msgCtxt) throws Exception {
        XPathEvaluator xpe = new XPathEvaluator();
        // register namespaces
        for (Object prop : properties.keySet()) {
            String key = (String) prop;
            if (key.startsWith("xmlns:")) {
                String value =
                    resolvePropertyValue((String) properties.get(key), msgCtxt);
                String[] parts = key.split(":");
                xpe.registerNamespace(parts[1], value);
            }
        }
        return xpe;
    }

    private Map<String,String> getVarXpathPairs(MessageContext msgCtxt) throws Exception {
        Map<String,String> xpaths = new HashMap<String,String>();
        for (Object prop : properties.keySet()) {
            String key = (String) prop;
            if (key.startsWith("xpath:")) {
                String value =
                    resolvePropertyValue((String) properties.get(key), msgCtxt);

                String[] parts = key.split(":");
                xpaths.put(parts[1], value);
            }
        }
        return xpaths;
    }


    private void validate(NodeList nodes) throws IllegalStateException {
        int length = nodes.getLength();
        if (length != 1) {
            throw new IllegalStateException("xpath does not resolve to one node. (length=" + length + ")");
        }
    }

    private String extract(NodeList nodes) {
        Node currentNode = nodes.item(0);
        short nodeType = currentNode.getNodeType();
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE:
                return ((Attr)currentNode).getNodeValue();
            case Node.ELEMENT_NODE:
                return ((Element)currentNode).getTextContent();
            case Node.TEXT_NODE:
                return currentNode.getNodeValue();
        }
        return "";
    }

    public ExecutionResult execute (final MessageContext msgCtxt,
                                    final ExecutionContext execContext) {
        try {
            // 0. get the source document
            Document document = getDocument(msgCtxt);

            // 1. get XPath evaluator
            XPathEvaluator xpe = getXpe(msgCtxt);

            // 2. get all XPaths
            Map<String,String> varXpathPairs = getVarXpathPairs(msgCtxt);

            if (varXpathPairs.entrySet().size() < 1) {
                throw new IllegalStateException("no xpaths provided");
            }

            // 3. iterate through each xpath, evaluating and setting var
            for (Map.Entry<String, String> entry : varXpathPairs.entrySet()) {
                String variableName = entry.getKey();
                String xpath = entry.getValue();
                NodeList nodes = (NodeList)xpe.evaluate(xpath, document, XPathConstants.NODESET);
                try {
                    validate(nodes);
                    msgCtxt.setVariable(variableName, extract(nodes));
                }
                catch (IllegalStateException exc1) {
                    setExceptionVariables(exc1,msgCtxt);
                }
            }

            return ExecutionResult.SUCCESS;
        }
        catch (javax.xml.xpath.XPathExpressionException texc1){
            setExceptionVariables(texc1,msgCtxt);
            return ExecutionResult.ABORT;
        }
        catch (IllegalStateException exc1) {
            setExceptionVariables(exc1,msgCtxt);
            return ExecutionResult.ABORT;
        }
        catch (Exception e) {
            if (getDebug()) {
                System.out.println(getStackTrace(e));
            }
            setExceptionVariables(e,msgCtxt);
            msgCtxt.setVariable(varName("stacktrace"), getStackTrace(e));
            return ExecutionResult.ABORT;
        }
    }

}
