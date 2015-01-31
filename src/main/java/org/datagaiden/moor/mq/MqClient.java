package org.datagaiden.moor.mq;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.ZMQ;

@Component
public class MqClient {

	private ZMQ.Context context;
	private ZMQ.Socket requester;
	private String ip = "tcp://localhost:5555";
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	private int imageCounter = 0;
	
	public String getImageCounter() {
		return (imageCounter-1)+"";
	}

	public void setImageCounter(int imageCounter) {
		this.imageCounter = (imageCounter);
	}

	@Autowired
	ServletContext servletContext;
	
	public MqClient()
	{
		this.context = ZMQ.context(2);

        //  Socket to talk to server
        System.out.println("Connecting to zeromq server ...");

        requester = context.socket(ZMQ.REQ);
        requester.connect(ip);


    }
	
	public void reconnect(String ip)
	{
		requester.close();
        System.out.println("Connecting to new zeromq server ...");
        requester = context.socket(ZMQ.REQ);
        this.ip = ip;
        requester.connect(ip);
        
	}
	
	public String sendMessages(String request)
	{
		requester.send(request.getBytes(),0);

		//Check if png
		byte[] receive = requester.recv(0);
		byte[] mimetype = Arrays.copyOfRange(receive, 0, 4);
		byte[] mimearray = new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};
		if (Arrays.equals(mimearray, mimetype)) {
			
			try {			    
				BufferedOutputStream stream;
				stream = new BufferedOutputStream(new FileOutputStream(new File("plot"+imageCounter+".png")));
				imageCounter++;
				stream.write(receive);
		        stream.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
          
			return "plotgenerated"; 
			
		} // png file spotted;
		
		return new String(receive);
	}
	
	@PreDestroy
	public void destroy()
	{
		requester.close();
        context.term();
	}
}
