package controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController implements Controller{
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Override
	public void service(HttpRequest request, HttpResponse response) {
		User user = DataBase.findUserById(request.getParameter("userId"));
		logger.debug("User : {}",user);
		if(user == null) {
			logger.debug("LoginIdFailed!!");
			response.addHeader("Set-Cookie", "logined=false");
			response.forward("/user/login_failed.html");
			return;
		}
		
		if (user.login(request.getParameter("password"))) {	// 유저가 로그인 했으면,
			logger.debug("LoginSuccess!!");
			response.addHeader("Set-Cookie", "logined=true");
			response.sendRedirect("/index.html");
		} else {
			logger.debug("LoginPasswordFailed!!");
			response.addHeader("Set-Cookie", "logined=false");
			response.forward("/user/login_failed.html");
		}
	}

}
