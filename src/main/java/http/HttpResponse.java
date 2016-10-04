package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
	
	Map<String, String> header = new HashMap<String, String>();
	
	public HttpResponse (OutputStream out) {
		DataOutputStream dos = new DataOutputStream(out);
	}
	
	
	private void addHeader() {
		
	}
	
	public void forward(String url) {
		
	}
	
	private void forwardBody() {
		
	}
	
	
	private void response200Header() {
		
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body);
			dos.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}	
			
	public void sendRedirect(String url) {
		
	}
	
	private void processHeaders() {
		
	}
}
