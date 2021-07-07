/**
* The generateOAuth program allows to create an OAuth1.0 Authorization header that can be injected into
* the http call, algorithm is based on RFC 5849 https://tools.ietf.org/html/rfc5849
* This script is meant to be used on Dell Boomi with groovy 2.4 in conjuntion with RESTlets call to Netsuite,
* The example used on this script if based on a not real account
*
* @author  Alejandro Fierro <alejandro.fierro@bringitps.com>
* @version 1.0
* @since   07-07-2021 
*/

import java.util.Properties;
import java.io.InputStream;
import java.net.URLEncoder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.DatatypeConverter;
import com.boomi.execution.ExecutionUtil;


/* Change below vars according to your needs */
String method = 'GET'
String urlStringToEncode  = 'https://5728780-sb2.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=441&deploy=1&ss=customsearch691&page=2'
String ConsumerSecret = '7fbf164c7d66e5c983dcc20d97a6a41c6c1f2932d382f46315c3c3c82f556d79'
String TokenSecret = '09eabac5a1155fb4cc5124c6de30e62b83a3d85f1c7eb79c2243267dbc49b4ec'
String ConsumerKey = '093cc3f4ff37952ae4c406c38429b1ab76a94d919383c80fbda7563201300153'
String TokenKey = 'f5a4769de1005ed33de241c129d91bedda5eadced9b1534c647a6a3f52d2fc93'
String realm = '5728780_SB2'

// Main Process
String nonce = getNonce()
String timestamp = getTimestamp() 
String baseString = getBaseString(method, urlStringToEncode, ConsumerKey,TokenKey, nonce, timestamp )

String key = URLEncoder.encode(ConsumerSecret, "UTF-8") + '&' + URLEncoder.encode(TokenSecret, "UTF-8")

Mac mac = Mac.getInstance("HmacSHA256")
SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"HmacSHA256")
mac.init(secretKeySpec)
byte[] signatureBytes = mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8))
String encryptedSignature = new String(DatatypeConverter.printBase64Binary(signatureBytes));
encryptedSignature = URLEncoder.encode(encryptedSignature, "UTF-8");

String authHeader = 'OAuth realm="' + realm +'", ' +
        'oauth_consumer_key="' + ConsumerKey + '", ' +
        'oauth_nonce="' + nonce + '", ' +
        'oauth_signature="' + encryptedSignature + '", ' +
        'oauth_signature_method="HMAC-SHA256", ' +
        'oauth_timestamp="' + timestamp + '", ' +
        'oauth_token="' + TokenKey + '", ' +
        'oauth_version="1.0"' 

InputStream is = dataContext.getStream(0);
Properties props = dataContext.getProperties(0);

props.setProperty("document.dynamic.userdefined.header",authHeader)
/*props.setProperty("document.dynamic.userdefined.nonce",nonce)
props.setProperty("document.dynamic.userdefined.timestamp",timestamp)
props.setProperty("document.dynamic.userdefined.baseString",baseString)
props.setProperty("document.dynamic.userdefined.signature",encryptedSignature)*/

dataContext.storeStream(is, props);


/* Helping Functions */

/**
* This will generate a random string up to 32 chars
* @return String
*/

public static String getNonce() {
    String chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890';
    String result = '';
    for (int i = 0; i < 32; i++) {
        int d = Math.random() * chars.length();                
        result += chars[d];
    }
    return result;
    
}

/**
* This will get the epoch time (number of seconds since Jan 1st 1970) 
* @return String
*/
public static String getTimestamp() {
    Date now = new Date();
    int unixtime = now.getTime() / 1000;
    
    return unixtime
}

/**
* This will get the base string to be used later to create the signature, SHA256 must be used to be compatible with Netsuite 2021.2
* @param httpMethod  - The method to be used on the RESTlet call (GET, PUT, DELETE, POST)
* @param url         - The REST endpoint
* @param consumerKey - Consumer Key (From Netsuite)
* @param tokenKey    - Token Key (From Netsuite)
* @param nonce       - A Unique string up to 32 chars
* @param timestamp   - Seconds since Jan 1st 1970 (Usually current time)
* @return String
*/
public static String getBaseString(String httpMethod, String url, String consumerKey, String tokenKey, String nonce, String timestamp) {
    
    String[] baseUrlArr = url.split('\\?');
    String baseUrl = baseUrlArr[0];

    String querystring = baseUrlArr[1];
    String[] params = querystring.split('&');

    Map<String, String> data = new HashMap<String, String>();
    
    data.put('oauth_consumer_key', consumerKey)
    data.put('oauth_nonce', nonce)
    data.put('oauth_signature_method', 'HMAC-SHA256')
    data.put('oauth_timestamp', timestamp)
    data.put('oauth_token', tokenKey)
    data.put('oauth_version', '1.0')

    for(int i = 0; i < params.length; i++){
        String[] p = params[i].split('=')
        data.put(p[0], p[1])
    }

    TreeMap<String, Integer> sorted = new TreeMap<>();
    sorted.putAll(data);


    String baseString = httpMethod + '&' + URLEncoder.encode(baseUrl, "UTF-8") + '&'

    String str =''
    for (Map.Entry<String, Integer> entry : sorted.entrySet()){
        str += entry.getKey() + '=' + entry.getValue() + '&'
    }
    
    str = str.substring(0, str.length() - 1);

    baseString += URLEncoder.encode(str, "UTF-8")

    return baseString;
}
