package http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.Test;

public class HttpResponseTest {
	private String testDirectory = "./src/test/resources/";
	
	@Test
	public void responseForward() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Forward.txt"));
		response.forward("/index.html");
	}
	
	@Test
	public void responseRedirect() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect.txt"));
		response.sendRedirect("/index.html");
	}
	
	@Test
	public void responseCookies() throws Exception {
		HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie.txt"));
		response.addHeader("Set-Cookie", "logined=true");
		response.sendRedirect("/index.html");
	}
	
	private OutputStream createOutputStream(String filename) throws FileNotFoundException {	// 클라이언트로 부터 얻어온 스트림이 아닌, 직접 스트림 생성.
		return new FileOutputStream(new File(testDirectory+filename));
	}
}
