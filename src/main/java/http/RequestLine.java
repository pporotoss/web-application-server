package http;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class RequestLine {
	private static final Logger logger = LoggerFactory.getLogger(RequestLine.class);
	
	private String method;
	private String path;
	private Map<String, String> params = new HashMap<>();
		
	public RequestLine(String requestLine) {
		logger.debug("requestLine : {}", requestLine);
		String[] headerLine = requestLine.split(" ");
		method = headerLine[0];
		logger.debug("method : {}", method);
		
		int index = headerLine[1].indexOf("?");
		
		logger.debug("index : {}", index);
		
		if(index == -1) {
			path = headerLine[1];
			logger.debug("-1 path : {}", path);
		} else {
			path = headerLine[1].substring(0, index);
			logger.debug("path : {}", path);
			params = HttpRequestUtils.parseQueryString(headerLine[1].substring(index+1));
			logger.debug("params : {}",params);
		}		
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getPath() {
		return path;
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
}
