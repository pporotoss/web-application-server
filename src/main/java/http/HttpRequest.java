package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.RequestingUserName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
		
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	private RequestLine requestLine;
	private HttpMethod method;
	
	public HttpRequest (InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			String line = br.readLine();
			if(line == null) {
				return;
			}
			
			requestLine = new RequestLine(line);
			
			line = br.readLine();
			while(!line.equals("")) {
				logger.debug("Header : {}", line);
				
				String[] tokens = line.split(":");
				if(tokens.length == 2) {
					headers.put(tokens[0], tokens[1].trim());
				}
				line = br.readLine();
			}
			
			logger.debug("headers : {}", headers);
			
			method = HttpMethod.valueOf(requestLine.getMethod());	// 문자열을 Enum 객체화.
			if(method.isPost()) {
				int contentLength = Integer.parseInt(getHeader("Content-Length"));
				logger.debug("contentLength : {}",contentLength);
				String queryString = IOUtils.readData(br, contentLength);
				logger.debug("queryString : {}", queryString);
				params = HttpRequestUtils.parseQueryString(queryString);
				logger.debug("params : {}", params);
			} else {
				params = requestLine.getParams();
				logger.debug("params : {}", params);
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}// Constructor
	
	public String getMethod() {
		
		return requestLine.getMethod();
	}
	
	public String getPath() {
		
		return requestLine.getPath();
	}
	
	public String getHeader(String headerName) {
		
		return headers.get(headerName);
	}
	
	public String getParameter(String paramName) {
		
		return params.get(paramName);
	}
	
	public boolean isLogin() {	// 로그인 검사
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(getHeader("Cookie"));	// logined=true형식을 추출하여 키, 값으로 저장. 
    	logger.debug("cookies : {}", cookies);
    	String value = cookies.get("logined");	// 쿠키들중에서 키값이 logined인 쿠키를 반환.
    	if (value == null) {
    		return false;
    	}
    	return Boolean.parseBoolean(value);
    }
}
