import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class DirectMessageNewApi {
	private final String url = "https://api.twitter.com/1.1/direct_messages/events/new.json";
	private final String USER_AGENT = "OAuth gem v0.4.4";
	
	private String consumer_key = "";
	private String consumer_secret = "";
	private String access_token = "";
	private String access_token_secret = "";
	
	private String oauth_version = "";
	private String oauth_signature_method="";
	
	private String body_data = "";
	private String timestamp_string = "";
	private String oauth_nonce = "";

	
	public DirectMessageNewApi(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
		
		this.consumer_key = consumerKey;
		this.consumer_secret = consumerSecret;
		this.access_token = accessToken;
		this.access_token_secret = accessTokenSecret;
		this.oauth_version = "1.0";
		this.oauth_signature_method="HMAC-SHA1";
	}

	private String getSignature() {
		
		String parameterString = getParameterString();
		String finalBaseString = "POST&" + getPercentEncoding(url) + "&" + getPercentEncoding(parameterString);
		String signingKey = getPercentEncoding(consumer_secret) + "&" + getPercentEncoding(access_token_secret);
		return computeSignature(finalBaseString, signingKey);
	}

	private String computeSignature(String finalBaseString, String signingKey) {
		byte[] byteHMAC = null;  
	     try {  
	       Mac mac = Mac.getInstance("HmacSHA1");  
	       SecretKeySpec spec;       
	       spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");  
	       mac.init(spec);  
	       byteHMAC = mac.doFinal(finalBaseString.getBytes());  
	     } catch (Exception e) {  
	       e.printStackTrace();  
	     }
	     return Base64.getEncoder().encodeToString(byteHMAC);
	}

	private String getParameterString() {
		
		consumer_key = getPercentEncoding(consumer_key);
		String consumer_key_param_name =  getPercentEncoding("oauth_consumer_key");
		
		oauth_nonce = getPercentEncoding(oauth_nonce);
		String oauth_nonce_param_name = getPercentEncoding("oauth_nonce");
		
		oauth_signature_method = getPercentEncoding(oauth_signature_method);
		String oauth_signature_method_param_name = getPercentEncoding("oauth_signature_method");
		
		timestamp_string = getPercentEncoding(timestamp_string);
		String timestamp_string_param_name = getPercentEncoding("oauth_timestamp");
		
		access_token = getPercentEncoding(access_token);
		String access_token_param_name = getPercentEncoding("oauth_token");
		
		oauth_version = getPercentEncoding(oauth_version);
		String oauth_version_param_name = getPercentEncoding("oauth_version");
		
		String finalParamString = 
				consumer_key_param_name + "=" + consumer_key + "&" +
				oauth_nonce_param_name + "=" + oauth_nonce + "&" +
				oauth_signature_method_param_name + "=" + oauth_signature_method + "&" +
				timestamp_string_param_name + "=" + timestamp_string + "&" +
				access_token_param_name + "=" + access_token + "&" +
				oauth_version_param_name + "=" + oauth_version;
		return finalParamString;
	}

	private String getPercentEncoding(String value) {  
	     String encoded = "";  
	     try {  
	       encoded = URLEncoder.encode(value, "UTF-8");  
	     } catch (Exception e) {  
	       e.printStackTrace();  
	     }  
	      String sb = "";  
	     char focus;  
	     for (int i = 0; i < encoded.length(); i++) {  
	       focus = encoded.charAt(i);  
	       if (focus == '*') {  
	         sb += "%2A"; 
	       } else if (focus == '+') {  
	         sb += "%20";
	       } else if (focus == '%' && i + 1 < encoded.length()  
	           && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {  
	         sb += '~';
	         i += 2;  
	       } else {  
	         sb += focus;
	       }  
	     }  
	     return sb.toString();  
	   }  
	
	private void constructBodyData (String message, String recipient_id) {
		body_data = "{\"event\": {\"type\": \"message_create\", \"message_create\": {\"target\": {\"recipient_id\": \""+recipient_id +"\"}, \"message_data\": {\"text\": \""+ message +"\"}}}}";
	}

	public void sendPost(String messageToSend, String recipient_id) throws Exception {

		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		timestamp_string = "" + System.currentTimeMillis() / 1000;
		byte[] b = new byte[32];
		new Random().nextBytes(b);
		
		constructBodyData(messageToSend, recipient_id);
		
		
		String encodedString = Base64.getEncoder().encodeToString(b);
		oauth_nonce = encodedString.replaceAll("[^\\p{L}\\p{Nd}]+", "");
		
		
		String auth_signature = getSignature();
		auth_signature = getPercentEncoding(auth_signature);
		
		String DST = "OAuth " + getPercentEncoding("oauth_consumer_key") + "=\"" + consumer_key + "\"" + "," +
		getPercentEncoding("oauth_nonce")+ "=\"" + oauth_nonce + "\"" + "," +
		getPercentEncoding("oauth_signature") + "=\"" + auth_signature + "\"" + "," +	
		getPercentEncoding("oauth_signature_method") + "=\"" + oauth_signature_method + "\"" + "," + 
		getPercentEncoding("oauth_timestamp")+ "=\"" + timestamp_string + "\"" + "," +
		getPercentEncoding("oauth_token") +"=\"" + access_token + "\"" + "," +
		getPercentEncoding("oauth_version") + "=\"" + oauth_version + "\"";

		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Authorization", DST);

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(body_data);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		String message = con.getResponseMessage();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post body : " + body_data);
		System.out.println("Response Code : " + responseCode);
		System.out.println("Response Message : " + message);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println(response.toString());
	}	

}
