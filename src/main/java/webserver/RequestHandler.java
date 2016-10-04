package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private HttpRequest request;
    private HttpResponse response;
    private Controller controller;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	request = new HttpRequest(in);
        	response = new HttpResponse(out);
        	
        	String path = getDefaultPath(request.getPath());	// 요청경로
        	log.debug("path : {}",path);
        	controller = RequestMapping.getController(path);
        	if (controller == null) {
        		response.forward(path);
        	} else {
        		controller.service(request, response);
        	}
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private String getDefaultPath(String path) {
    	if (path.equals("/")) {	// 요청이 '/' 로 들어오면 index.html을 반환. 나머지 경우에는 요청받은 경로를 반환.
    		return "/index.html";
    	}
    	return path;
    }
}
