import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/map")
public class Server extends HttpServlet{
	
	public static DBManager dbmn;
		
	@OnMessage
    public void echoTextMessage(Session session, int interval) {
        
		dbmn = new DBManager();
		dbmn.connect();
		ArrayList<String> data = new ArrayList<String>();
		// update every one minute
		while(session.isOpen()) {
			data = dbmn.getData(interval);		
    		if(data == null) {
    			System.out.println("data is null");
    			try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			data = dbmn.getData(interval);	
    		}
			try {
            	int size = data.size();
            	System.out.println(size);
            	for (int i=0; i < size; i++) {
            		String msg = data.get(i);
            		session.getBasicRemote().sendText(msg);
					System.out.print(true);
            	}               
	        } catch (IOException e) {
	        	e.printStackTrace();
	        	System.out.println("IO is wrong");
	        	/**
	        	try { 
	                session.close();
	            } catch (IOException e1) {
	                // Ignore
	            
	            }finally{
	            	dbmn.close();
	            	System.out.println("db is close");
	            }
	            */
	        }
			interval = interval - 5;
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
    }

}