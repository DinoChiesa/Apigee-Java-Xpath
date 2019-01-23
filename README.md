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

See [the example API proxy included here](./bundle) for the implementation.


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
