package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;

public class HttpRequest {
	private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
		
	private String method;
	private String path;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> params = new HashMap<>();
	
	public HttpRequest (InputStream in) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			String line = br.readLine();
			if(line == null) {
				return;
			}
			
			processRequestLine(line);
			
			line = br.readLine();
			while(!line.equals("")) {
				logger.debug("Header : {}", line);
				
				String[] tokens = line.split(":");
				if(tokens.length == 2) {
					headers.put(tokens[0], tokens[1].trim());
				}
				line = br.readLine();
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}// Constructor
	
	private void processRequestLine(String requestLine) {
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
			params = HttpRequestUtils.parseQueryString(requestLine);
		}
	}
	
	public String getMethod() {
		
		return method;
	}
	
	public String getPath() {
		
		return path;
	}
	
	public String getHeader(String headerName) {
		
		return headers.get(headerName);
	}
	
	public String getParameter(String paramName) {
		
		return params.get(paramName);
	}
}
