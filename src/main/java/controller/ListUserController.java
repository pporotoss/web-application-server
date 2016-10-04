package controller;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class ListUserController implements Controller{
	private static final Logger logger = LoggerFactory.getLogger(ListUserController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {
		if (!request.isLogin()) {	// 로그인 안했으면,
			response.forward("/user/login.html");	// 로그인 페이지로 이동.
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

		response.forwardBody(sb.toString());
	}

}
