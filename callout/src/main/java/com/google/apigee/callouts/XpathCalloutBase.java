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

import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.XmlUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Document;

public abstract class XpathCalloutBase {
    private final static String _varprefix= "xpath_";
    protected Map properties; // read-only
    private static final String variableReferencePatternString = "(.*?)\\{([^\\{\\} ]+?)\\}(.*?)";
    private static final Pattern variableReferencePattern = Pattern.compile(variableReferencePatternString);

    public XpathCalloutBase(Map properties) {
        this.properties = properties;
    }

    static String varName(String s) { return _varprefix + s; }

    protected Document getDocument(MessageContext msgCtxt) throws Exception {
        String source = getSimpleOptionalProperty("source", msgCtxt);
        if (source == null) {
            return XmlUtils.parseXml(msgCtxt.getMessage().getContentAsStream());
        }
        String text = (String) msgCtxt.getVariable(source);
        if (text == null) {
            throw new IllegalStateException("source variable resolves to null");
        }
        return XmlUtils.parseXml(text);
    }

    protected static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    // protected boolean getPretty(MessageContext msgCtxt) throws Exception {
    //     String pretty = getSimpleOptionalProperty("pretty", msgCtxt);
    //     if (pretty == null) return false;
    //     pretty = pretty.toLowerCase();
    //     return pretty.equals("true");
    // }

    protected boolean getDebug() {
        String value = (String) this.properties.get("debug");
        if (value == null) return false;
        if (value.trim().toLowerCase().equals("true")) return true;
        return false;
    }

    // protected String getOutputVar(MessageContext msgCtxt) throws Exception {
    //     String dest = getSimpleOptionalProperty("output-variable", msgCtxt);
    //     if (dest == null) {
    //         return "message.content";
    //     }
    //     return dest;
    // }

    protected String getSimpleOptionalProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) { return null; }
        value = value.trim();
        if (value.equals("")) { return null; }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) { return null; }
        return value;
    }

    protected String getSimpleRequiredProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) {
            throw new IllegalStateException(propName + " resolves to an empty string");
        }
        value = value.trim();
        if (value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string");
        }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string");
        }
        return value;
    }

    // If the value of a property contains any pairs of curlies,
    // eg, {apiproxy.name}, then "resolve" the value by de-referencing
    // the context variables whose names appear between curlies.
    protected String resolvePropertyValue(String spec, MessageContext msgCtxt) {
        Matcher matcher = variableReferencePattern.matcher(spec);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            sb.append(matcher.group(1));
            Object v = msgCtxt.getVariable(matcher.group(2));
            if (v != null){
                sb.append((String) v );
            }
            sb.append(matcher.group(3));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static final String qualifiedClassNameRegex =
        "(?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)+[a-zA-Z_$][a-zA-Z0-9_$]*";
    private static final String errorSuffixRegex =
        ": (.+)";
    private static final Pattern errorStringPattern =
        Pattern.compile("^.*"+ qualifiedClassNameRegex + errorSuffixRegex + "$");

    protected void setExceptionVariables(Exception exc1, MessageContext msgCtxt) {
        String error = exc1.toString();
        msgCtxt.setVariable(varName("exception"), error);
        Matcher matcher = errorStringPattern.matcher(error);
        if (matcher.find()) {
            msgCtxt.setVariable(varName("error"), matcher.group(1));
        }
        else {
            msgCtxt.setVariable(varName("error"), error);
        }
    }

}
