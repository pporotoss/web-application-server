package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	String line = br.readLine();
        	log.debug("request line :{}", line);
        	
        	if(line == null) {
        		return;
        	}
        	
        	String[] tokens = line.split(" ");
        	boolean logined = false;
        	int contentLength = 0;
        	while(!line.equals("")) {
        		line = br.readLine();
        		log.debug("Header : {}", line);
        		if(line.contains("Content-Length")) {
        			contentLength = getContentLength(line);
        			log.debug("Content-Length : {}", contentLength);
        		} else if (line.contains("Cookie")) {
        			log.debug("Cookie : {}", line);
        			logined = isLogin(line);
        		}
        	}
        	
        	String url = tokens[1];
        	log.debug("url : {}",url);
        	if(url.equals("/user/create")) {
        		
        		String body = IOUtils.readData(br, contentLength);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		log.debug("User : {}", user);
        		DataBase.addUser(user);
        		DataOutputStream dos = new DataOutputStream(out);
        		response302Header(dos, "/index.html");
        		
        	} else if(url.equals("/user/login")) {
        		
        		String body = IOUtils.readData(br, contentLength);
        		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
        		User user = DataBase.findUserById(params.get("userId"));
        		log.debug("User : {}",user);
        		if(user == null) {
        			log.debug("LoginFailed!!");
        			responseResource(out, "/user/login_failed.html");
        			return;
        		}
        		
        		if (user.getPassword().equals(params.get("password"))) {
        			log.debug("LoginSuccess!!");
        			DataOutputStream dos = new DataOutputStream(out);
        			reponse302LoginSuccesHeader(dos);
        		} else {
        			log.debug("LoginFailed!!");
        			responseResource(out, "/user/login_failed.html");
        		}
        	
        	} else if (url.equals("/user/list")) {
        		if (!logined) {	// 로그인 안했으면,
        			responseResource(out, "/user/login.html");	// 로그인 페이지로 이동.
        			return;
        		}
        		Collection<User> users = DataBase.findAll();
        		StringBuilder sb = new StringBuilder();
        		sb.append("<table boarder='1'>");
	        		sb.append("<tr>");
						sb.append("<td>userID</td>");
						sb.append("<td>name</td>");
						sb.append("<td>email</td>");
					sb.append("</tr>");
        		for (User user : users) {
        			sb.append("<tr>");
						sb.append("<td>"+user.getUserId()+"</td>");
						sb.append("<td>"+user.getName()+"</td>");
						sb.append("<td>"+user.getEmail()+"</td>");
					sb.append("</tr>");
				}
        		sb.append("</table>");
        		DataOutputStream dos = new DataOutputStream(out);
        		byte[] body = sb.toString().getBytes();
        		response200Header(dos, body.length);	// 200 응답코드 전송.
        		responseBody(dos, body);	// 바디 전송.
        		
        	} else if (url.endsWith(".css")) {	// 요청 url 마지막이 .css 이면,
        		
        		DataOutputStream dos = new DataOutputStream(out);
        		byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
        		response200CssHeader(dos, body.length);
        		responseBody(dos, body);
        		
        	} else {
        		responseResource(out, url);
        	}
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String line) {	// 로그인 검사
    	String[] headerTokens = line.split(":");	// Cookie: 로 시작하는 라인에서 : 기준으로 나눔.
    	log.debug("headerTokens : {}", headerTokens);
    	Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());	// logined=true만 추출. 
    																																// Cookie: 다음에 공백있어서 trim() 해줘야 함.
    	log.debug("cookies : {}", cookies);
    	String value = cookies.get("logined");	// 쿠키들중에서 키값이 logined인 쿠키를 반환.
    	if (value == null) {
    		return false;
    	}
    	return Boolean.parseBoolean(value);
    }
    
    private String getAccept(String line) {
    	String[] headerTokens = line.split(":");
    	return headerTokens[1].trim();
    }
    
    private int getContentLength(String line) {
    	String [] headerTokens = line.split(":");
    	return Integer.parseInt(headerTokens[1].trim());
    }
    
    private void responseResource(OutputStream out, String url) throws IOException {	// 200코드 페이지 이동.
    	DataOutputStream dos = new DataOutputStream(out);
    	byte[] body = Files.readAllBytes(new File("./webapp"+url).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
    
    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response200CssHeader(DataOutputStream dos, int lengthOfBodyContent) {
    	try {
    		dos.writeBytes("HTTP/1.1 200 OK \r\n");
    		dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
    		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
    		dos.writeBytes("\r\n");
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }
    
    private void response302Header(DataOutputStream dos, String url) {	// 리다이렉트 헤더
    	try {
    		dos.writeBytes("HTTP/1.1 302 Found \r\n");
    		dos.writeBytes("Location: "+url);
    		dos.writeBytes("\r\n");
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }
    
    private void reponse302LoginSuccesHeader(DataOutputStream dos) {	// 로그인 성공시 페이지 이동 헤더.
    	try {
    		dos.writeBytes("HTTP/1.1 302 Found \r\n");
    		dos.writeBytes("Set-Cookie: logined=true \r\n");	// 쿠키 설정.
    		dos.writeBytes("Location: /index.html");
    		dos.writeBytes("\r\n");
    	} catch (IOException e) {
    		log.error(e.getMessage());
    	}
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
