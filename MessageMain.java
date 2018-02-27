
public class MessageMain {
	
	public static void main(String[] args) {
		String consumer_key = "";
		String consumer_secret = "";
		String access_token = "";
		String access_token_secret = "";
		DirectMessageNewApi dm = new DirectMessageNewApi(consumer_key, consumer_secret, access_token, access_token_secret);
		try {
			dm.sendPost("", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
