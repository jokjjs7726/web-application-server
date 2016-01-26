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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.omg.CORBA.ParDescriptionSeqHelper;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		util.HttpRequestUtils httpUtil = new HttpRequestUtils();
		
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			DataOutputStream dos = new DataOutputStream(out);
			
			BufferedReader bfReader = new BufferedReader(new InputStreamReader(in));
			
			byte[] body =null;
			int length;
			
			String line = bfReader.readLine();
			if (line == null) 
				{ return;}
			
			//while (!"".equals(line)) {
			String[] tokens = line.split(" "); 
			
			
			
			if(tokens[1].startsWith("/create")){//회원가입
				while (!"".equals(line)) {
					if(line.startsWith("Content-Length:")){
						String[] lengths = line.split(" ");
						length = Integer.parseInt(lengths[1]);
					}
						line = bfReader.readLine();
				}
				
				String url = tokens[1];
				String params = url.substring(index+1);
				Map <String, String> newUser = httpUtil.parseQueryString(params);
				User user = new User(newUser.get("userId"), newUser.get("password"),
						newUser.get("name"),newUser.get("email"));
				DataBase.addUser(user);
				response302Header(dos);
			}
			else{//index
				body = Files.readAllBytes(new File("./webapp" + tokens[1]).toPath()); 
			}
			
			//body = url.getBytes();	
			//line = bfReader.readLine();
			// } 
			 
			 response200Header(dos, body.length);
			 responseBody(dos, body);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
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
	
	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
