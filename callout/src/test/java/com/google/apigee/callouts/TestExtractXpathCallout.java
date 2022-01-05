// Copyright 2019-2022 Google LLC.
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
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestExtractXpathCallout {

  MessageContext msgCtxt;
  InputStream messageContentStream;
  Message message;
  ExecutionContext exeCtxt;

  @BeforeMethod()
  public void beforeMethod() {

    msgCtxt =
        new MockUp<MessageContext>() {
          private Map<String,Object> variables;

          public void $init() {
            variables = new HashMap<String,Object>();
          }

          @Mock()
          public Object getVariable(final String name) {
            if (variables == null) {
              variables = new HashMap<String,Object>();
            }
            return variables.get(name);
          }

          @Mock()
          public boolean setVariable(final String name, final Object value) {
            if (variables == null) {
              variables = new HashMap<String,Object>();
            }
            variables.put(name, value);
            return true;
          }

          @Mock()
          public boolean removeVariable(final String name) {
            if (variables == null) {
              variables = new HashMap<String,Object>();
            }
            if (variables.containsKey(name)) {
              variables.remove(name);
            }
            return true;
          }

          @Mock()
          public Message getMessage() {
            return message;
          }
        }.getMockInstance();

    exeCtxt = new MockUp<ExecutionContext>() {}.getMockInstance();

    message =
        new MockUp<Message>() {
          @Mock()
          public InputStream getContentAsStream() {
            // new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
            return messageContentStream;
          }
        }.getMockInstance();
  }

  private static final String simpleXml2 =
      "<?xml version=\"1.1\"?>"
          + "<Task> \n"
          + "  <Triggers>\n"
          + "    <EventTrigger>\n"
          + "      <ExecutionTimeLimit>123</ExecutionTimeLimit>\n"
          + "    </EventTrigger>\n"
          + "  </Triggers>"
          + "</Task>";

  private static final String simpleXml1 =
      "<?xml version='1.0' encoding='UTF-8'?>\n"
          + "<tx:order xmlns:tx='https://example.com/20190122/tx' xmlns='https://example.com/20190122/entities'>"
          + "        <customer customerNumber='0815A4711'>\n"
          + "                <name>Michael Sonntag</name>\n"
          + "                <address>\n"
          + "                        <street>Altenbergerstr. 69</street>\n"
          + "                        <ZIP>4040</ZIP>\n"
          + "                        <city>Linz</city>\n"
          + "                        <province>Upper Austria</province>\n"
          + "                        <country>Austria</country>\n"
          + "                        <email>sonntag@fim.uni-linz.ac.at</email>\n"
          + "                        <phone>+43(732)2468-9330</phone>\n"
          + "                        <fax>+43(732)2468-8599</fax>\n"
          + "                </address>\n"
          + "        </customer>\n"
          + "        <articles>\n"
          + "                <line>\n"
          + "                        <quantity unit='piece'>30</quantity>\n"
          + "                        <product productNumber='9907'>XML editing widget</product>\n"
          + "                        <price currency='EUR'>0.10</price>\n"
          + "                </line>\n"
          + "                <line>\n"
          + "                        <quantity unit='piece'>1</quantity>\n"
          + "                        <product productNumber='666'>Course supervisor handbook</product>\n"
          + "                        <price currency='EUR'>999.89</price>\n"
          + "                </line>\n"
          + "                <line>\n"
          + "                        <quantity unit='litre'>5</quantity>\n"
          + "                        <product productNumber='007'>Super juice</product>\n"
          + "                        <price currency='HUF'>500</price>\n"
          + "                </line>\n"
          + "        </articles>\n"
          + "        <delivery>\n"
          + "                <deliveryaddress>\n"
          + "                        <name>Michael Sonntag</name>\n"
          + "                        <address>\n"
          + "                                <street>Auf der Wies 18</street>\n"
          + "                                <ZIP>4040</ZIP>\n"
          + "                                <city>Linz</city>\n"
          + "                                <province>Upper Austria</province>\n"
          + "                                <country>Austria</country>\n"
          + "                                <phone>+43(676)3965166</phone>\n"
          + "                        </address>\n"
          + "                </deliveryaddress>\n"
          + "                <options>\n"
          + "                        <insurance>none</insurance>\n"
          + "                        <collection>1</collection>\n"
          + "                        <service>post</service>\n"
          + "                </options>\n"
          + "        </delivery>\n"
          + "        <payment type='CC'>\n"
          + "                <creditcard issuer='Mastercard'>\n"
          + "                        <nameOnCard>Mag. Dipl.-Ing. Dr. Michael Sonntag</nameOnCard>\n"
          + "                        <number>5201 2345 6789 0123</number>\n"
          + "                        <expiryDate>2006-04-30</expiryDate>\n"
          + "                </creditcard>\n"
          + "        </payment>\n"
          + "</tx:order>\n";

  @Test
  public void test_EmptySource() throws Exception {
    String expectedError = "source variable resolves to null";
    msgCtxt.setVariable("message-content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "not-message.content");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    // System.out.printf("expected error: %s\n", errorOutput);
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "EmptySource() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_MissingXpath() throws Exception {
    String expectedError = "no xpaths provided";

    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNotNull(exception, "exception");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "MissingXpath() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_RubbishXpath() throws Exception {
    String expectedError = "Unknown error in XPath.";
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xpath:var1", "$%rubbish-here");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNotNull(exception, "exception");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "RubbishXpath() stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_MissingNamespace() throws Exception {
    String expectedError = "Prefix must resolve to a namespace: tx";
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xpath:var1", "/tx:order/payment");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.ABORT, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    Assert.assertEquals(errorOutput, expectedError, "error not as expected");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNotNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    System.out.println("=========================================================");
  }

  @Test
  public void test_ValidResult_Attribute() throws Exception {
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "https://example.com/20190122/tx");
    props.put("xmlns:e", "https://example.com/20190122/entities");
    props.put("xpath:var1", "/tx:order/e:payment/@type");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertEquals(value, "CC", "result not as expected");
    System.out.println("=========================================================");
  }

  @Test
  public void test_ValidResult_Element() throws Exception {
    String expectedExtractedValue = "5201 2345 6789 0123";
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "https://example.com/20190122/tx");
    props.put("xmlns:e", "https://example.com/20190122/entities");
    props.put("xpath:var1", "/tx:order/e:payment/e:creditcard/e:number");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertEquals(value, expectedExtractedValue, "result not as expected");
    System.out.println("=========================================================");
  }

  @Test
  public void test_ValidResult_Text() throws Exception {
    String expectedExtractedValue = "5201 2345 6789 0123";
    msgCtxt.setVariable("message.content", simpleXml1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "https://example.com/20190122/tx");
    props.put("xmlns:e", "https://example.com/20190122/entities");
    props.put("xpath:var1", "/tx:order/e:payment/e:creditcard/e:number/text()");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertEquals(value, expectedExtractedValue, "result not as expected");
    System.out.println("=========================================================");
  }

  @Test
  public void test_ValidResult_Text_XpathFromVariable() throws Exception {
    String expectedExtractedValue = "5201 2345 6789 0123";
    msgCtxt.setVariable("message.content", simpleXml1);
    msgCtxt.setVariable("xmlns1", "https://example.com/20190122/tx");
    msgCtxt.setVariable("xmlns2", "https://example.com/20190122/entities");
    msgCtxt.setVariable("xpath1", "/tx:order/e:payment/e:creditcard/e:number/text()");

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "{xmlns1}");
    props.put("xmlns:e", "{xmlns2}");
    props.put("xpath:var1", "{xpath1}");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertEquals(value, expectedExtractedValue, "result not as expected");
    System.out.println("=========================================================");
  }

  @Test
  public void test_XpathReturnsNothing() throws Exception {
    String expectedError = "xpath does not resolve to one node. (length=0)";
    msgCtxt.setVariable("message.content", simpleXml1);
    msgCtxt.setVariable("xmlns1", "https://example.com/20190122/tx");
    msgCtxt.setVariable("xmlns2", "https://example.com/20190122/entities");
    msgCtxt.setVariable("xpath1", "/tx:order/e:payment/e:foo/e:number/text()");

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "{xmlns1}");
    props.put("xmlns:e", "{xmlns2}");
    props.put("xpath:var1", "{xpath1}");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    Assert.assertEquals(errorOutput, expectedError, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNotNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertNull(value, "variable");
    System.out.println("=========================================================");
  }

  @Test
  public void test_XpathReturnsTooManyThings() throws Exception {
    String expectedError = "xpath does not resolve to one node. (length=12)";
    msgCtxt.setVariable("message.content", simpleXml1);
    msgCtxt.setVariable("xmlns1", "https://example.com/20190122/tx");
    msgCtxt.setVariable("xmlns2", "https://example.com/20190122/entities");
    msgCtxt.setVariable("xpath1", "/tx:order/*/text()");

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xmlns:tx", "{xmlns1}");
    props.put("xmlns:e", "{xmlns2}");
    props.put("xpath:var1", "{xpath1}");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNotNull(errorOutput, "errorOutput");
    Assert.assertEquals(errorOutput, expectedError, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNotNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertNull(value, "variable");
    System.out.println("=========================================================");
  }

  @Test
  public void test_Xml11_Text() throws Exception {
    String expectedExtractedValue = "123";
    msgCtxt.setVariable("message.content", simpleXml2);

    Map<String, String> props = new HashMap<String, String>();
    props.put("source", "message.content");
    props.put("xpath:var1", "/Task/Triggers/EventTrigger/ExecutionTimeLimit/text()");

    ExtractXpath callout = new ExtractXpath(props);

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    Assert.assertEquals(actualResult, ExecutionResult.SUCCESS, "result not as expected");
    Object errorOutput = msgCtxt.getVariable("xpath_error");
    Assert.assertNull(errorOutput, "errorOutput");
    Object exception = msgCtxt.getVariable("xpath_exception");
    Assert.assertNull(exception, "exception");
    Object stacktrace = msgCtxt.getVariable("xpath_stacktrace");
    Assert.assertNull(stacktrace, "stacktrace");
    String value = msgCtxt.getVariable("var1");
    Assert.assertEquals(value, expectedExtractedValue, "result not as expected");
    System.out.println("=========================================================");
  }
}
