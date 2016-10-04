package http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
	private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
	
	private Map<String, String> headers = new HashMap<String, String>();
	private DataOutputStream dos;
	
	
	public HttpResponse (OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	
	public void forward(String url) {
		try {
			byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());	// 요청 들어온 경로에 있는 파일을 읽어들인다.
			
			if (url.endsWith(".css")) {
				addHeader("Content-Type", "text/css;charset=utf-8");
			} else if (url.endsWith(".js")) {
				addHeader("Content-Type", "application/javascript");
			} else {
				addHeader("Content-Type", "text/html;charset=utf-8");
			}
			
			addHeader("Content-Length", body.length+"");
			logger.debug("headers : {}", headers);
			response200Header();
			responseBody(dos, body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private void forwardBody() {
		
	}
	
	
	private void response200Header() {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body);	// 스트림을 통해 읽어들인 파일의 내용을 전송한다.
			dos.flush();	// 전송을 마친후 비워줌으로 마무리한다.
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}	
			
	public void sendRedirect(String url) {
		
	}
	
	private void processHeaders() {
		try {
			Set<String> keys = headers.keySet();	// Map에 저장된 키를 set형태로 반환.
			for (String key : keys) {
				logger.debug("key : {}", key);
				dos.writeBytes(key+": "+headers.get(key)+" \r\n");
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}
