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
	
	/**
	 * 헤더 추가용 메서드 
	 * 
	 * @param key : 헤더 키
	 * @param value : 헤더 값
	 */
	public void addHeader(String key, String value) {
		if (headers.containsKey(key)) {	// 키값이 존재하고 있으면,
			logger.debug("같은 값 있음!!");
			headers.replace(key, value);
			return;
		}
		headers.put(key, value);
	}
	
	/**
	 * 페이지 전환 없이 요청한 페이지로 응답하기.
	 * 
	 * @param url : 표시할 페이지
	 */
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
			responseBody(body);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * 직접 작성한 내용을 응답하기.
	 */
	public void forwardBody(String body) {
		byte[] contents = body.getBytes();
		addHeader("Content-Type", "text/html;charset=utf-8");
		addHeader("Content-Length", contents.length+"");
		
		response200Header();
		responseBody(contents);
	}
	
	
	private void response200Header() {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			processHeaders();	// 상태코드 이외의 헤드는 순서 중요치 않다.
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private void responseBody(byte[] body) {
		try {
			dos.write(body, 0, body.length);	// 스트림을 통해 읽어들인 파일의 길이만큼의 내용을 전송한다.
			dos.flush();	// 전송을 마친후 비워줌으로 마무리한다.
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}	
			
	public void sendRedirect(String url) {
		try {
			dos.writeBytes("HTTP/1.1 302 Found \r\n");
			processHeaders();	// 쿠키가 있을경우 대비.
			dos.writeBytes("Location: "+url+" \r\n");	// 리다이렉트는 다시 요청을 하여 페이지 이동하기 때문에 요청 페이지에 대한 정보가 필요없다. 따라서 헤더 마지막에 위치.
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	private void processHeaders() {
		try {
			Set<String> keys = headers.keySet();	// Map에 저장된 키를 set형태로 반환.
			for (String key : keys) {
				logger.debug("key : {}", key);
				dos.writeBytes(key+": "+headers.get(key)+" \r\n");	// Map에 저장된 Header들을 출력.
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
}
