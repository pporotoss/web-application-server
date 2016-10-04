package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class CreateUserController implements Controller{
	private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		User user = new User(
				request.getParameter("userId"), 
				request.getParameter("password"), 
				request.getParameter("name"), 
				request.getParameter("email")
			);
		logger.debug("User : {}", user);
		DataBase.addUser(user); // 맵에 유저 저장.
		response.sendRedirect("/index.html");
	}

}
