package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpRequest;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private HttpRequest request;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	request = new HttpRequest(in);
        	
        	String path = getDefaultPath(request.getPath());
        	log.debug("path : {}",path);
        	if(path.equals("/user/create")) {
        		User user = new User(
        				request.getParameter("userId"), 
        				request.getParameter("password"), 
        				request.getParameter("name"), 
        				request.getParameter("email")
        			);
        		log.debug("User : {}", user);
        		DataBase.addUser(user);
        		DataOutputStream dos = new DataOutputStream(out);
        		response302Header(dos, "/index.html");
        		
        	} else if(path.equals("/user/login")) {
        		
        		User user = DataBase.findUserById(request.getParameter("userId"));
        		log.debug("User : {}",user);
        		if(user == null) {
        			log.debug("LoginFailed!!");
        			responseResource(out, "/user/login_failed.html");
        			return;
        		}
        		
        		if (user.getPassword().equals(request.getParameter("password"))) {
        			log.debug("LoginSuccess!!");
        			DataOutputStream dos = new DataOutputStream(out);
        			reponse302LoginSuccesHeader(dos);
        		} else {
        			log.debug("LoginFailed!!");
        			responseResource(out, "/user/login_failed.html");
        		}
        	
        	} else if (path.equals("/user/list")) {
        		if (!request.isLogin()) {	// 로그인 안했으면,
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
        		
        	} else if (path.endsWith(".css")) {	// 요청 path 마지막이 .css 이면,
        		
        		DataOutputStream dos = new DataOutputStream(out);
        		byte[] body = Files.readAllBytes(new File("./webapp"+path).toPath());
        		response200CssHeader(dos, body.length);
        		responseBody(dos, body);
        		
        	} else {
        		responseResource(out, path);
        	}
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getDefaultPath(String path) {
    	if (path.equals("/")) {
    		return "/index.html";
    	}
    	return path;
    }
    
    private void responseResource(OutputStream out, String path) throws IOException {	// 200코드 페이지 이동.
    	DataOutputStream dos = new DataOutputStream(out);
    	byte[] body = Files.readAllBytes(new File("./webapp"+path).toPath());
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
    
    private void response302Header(DataOutputStream dos, String path) {	// 리다이렉트 헤더
    	try {
    		dos.writeBytes("HTTP/1.1 302 Found \r\n");
    		dos.writeBytes("Location: "+path);
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
