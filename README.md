# boomi-ns-oauth1
Generate your NetSuite Token Based Authorization Header with SHA256

## Introduction
This script serve as an example on how to generate the OAuth1 header inside Dell Boomi with groovy 2.4

## Requirements

- Groovy 2.4

## Usage

1. Create a Data Process Shape
2. Add Custom Scripting
3. Use Language = Groovy 2.4
4. Paste the genHeader.groovy script
5. Change vars according to your needs
6. Header will be stored on a dynamic property called 'header' that you can later use to inject into your http call as the authorizathion header


## Example Output Result

``
OAuth realm="5728780_SB2", oauth_consumer_key="093cc3f4ff37952ae4c406c38429b1ab76a94d919383c80fbda7563201300153", oauth_nonce="GYzCJagRjW6tNbTZifKmzmOK36slP3iW", oauth_signature="IQvKhQKIJyFpsLUmyEHTKx4LXmZJev2tLgg8bwNQbbk%3D", oauth_signature_method="HMAC-SHA256", oauth_timestamp="1625681203", oauth_token="f5a4769de1005ed33de241c029d91bedda5eadced9b1534c647a6a3f52d2fc93", oauth_version="1.0"
``



