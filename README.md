# Java XPath Callout

This directory contains the Java source code and pom.xml file required
to compile a simple Java callout for Apigee Edge, that performs an
XPath extraction. This works similarly to ExtractVariables, except that the xpath itself can
be specified in a variable.

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.

## License

This material is copyright 2018-2019, Google LLC.
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file.

This code is open source but you don't need to compile it in order to use it.

## Details

There is one callout class: `com.google.apigee.edgecallouts.xpath.ExtractXpath`.  It encrypts the element specified by an xpath.


## Usage

Configure the policy like this:

```xml
<JavaCallout name='Java-Xpath-Extract'>
  <Properties>
    <Property name='source'>message.content</Property>
    <Property name='xmlns:ns1'>https://xml.example.com/20190122/ns</Property>
    <Property name='xmlns:ns2'>{xmlns_ns2}</Property>
    <Property name='xpath:var1'>/ns1:rootElement/childelement/text</Property>
    <Property name='xpath:var2'>{xpath2}</Property>
  </Properties>
  <ClassName>com.google.apigee.edgecallouts.ExtractXpath</ClassName>
  <ResourceURL>java://edge-xpath-1.0.1.jar</ResourceURL>
</JavaCallout>
```

Specify the important configuration that determines the operation of the policy through the Property elements.

* Specify the source as the VARIABLE NAME which contains XML to use as the source. By default the source is `message.content`. This is required.
* Specify each xpath to extract with a name attribute equal to the string `xpath:` followed by the name of a _context variable_ to set with the extracted value. At least one xpath Property is required.
* Optionally, specify each XML namespace and its prefix with a name attribute equal to the string `xmlns:` followed by the prefix string to use. Then you can use that prefix string in any xpath.

The values for the namespaces and the xpaths can be specified directly, or via context variables, which should be surrounded by curly braces.


See [the example API proxy included here](./bundle) for a working sample implementation.


### Example

```
curl -i "https://${ORG}-${ENV}.apigee.net/xpath/extract?xpath=/tx:order/e:payment/e:creditcard/e:number/text()" -H content-type:application/xml --data-binary @./sample-data/order.xml
```

If you send that to the sample bundle, then, as a response, you should see:

```
HTTP/1.1 200 OK
Date: Wed, 23 Jan 2019 01:14:21 GMT
Content-Type: application/json
Content-Length: 78
Connection: keep-alive

{
    "var1" : "5201 2345 6789 0123",
    "error" : "",
    "exception" : ""
}
```


## Bugs

None reported.
