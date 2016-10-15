import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class Server {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	
	class Client_handler implements Runnable {
		
		private Socket clientSocket;
		private int student_id;
		private int[] secrets = new int[3];
		
		
		public Client_handler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int new_udp_port = 0;
			try {
				new_udp_port = stageA();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		public void stageD() {
			
		}
		
		public void stageC() {
			
		}
		
		public void stageB(int port_num) throws Exception {
			ServerSocket new_server;
			 try{
				 new_server = new ServerSocket(port_num);
			 } catch (IOException e) {
				 System.out.println("Could not listen on port " + port_num);
				 System.exit(-1);
			 }
			
		}
		
		public int stageA() throws Exception {
			InputStream in;
		    DataInputStream dis = null;
		    try{
		    	in = clientSocket.getInputStream();
		    	dis = new DataInputStream(in);
		    } catch (IOException e) {
		    	System.out.println("in or out failed");
		    	System.exit(-1);
		    }
		    byte[] data = new byte[30];
		    dis.read(data);
		    ByteBuffer in_data = ByteBuffer.wrap(data);
		    student_id = in_data.getShort(10);
		    if (verify_header(0, in_data)) {
		    	System.out.println("header format problem");
		    	System.exit(-1);
		    }
		    String secret_phase = "hello world\0";
		    String incoming_phase = "";
		    for (int i = 0; i < secret_phase.getBytes().length; i++) {
		    	byte c = 0;
		    	try{
		    		c = in_data.get(i + 12);
		    	}catch (ArrayIndexOutOfBoundsException e) {
		    		System.out.println("wrong message");
			    	System.exit(-1);
		    	}
		    	incoming_phase += (char) c;
		    }
		    if (!incoming_phase.equals(secret_phase)) {
		    	System.out.println("wrong message");
		    	System.exit(-1);
		    }
		    OutputStream out;
		    DataOutputStream dos = null;
		    byte[] sendData = new byte[48];
		    while(true){
		    	try{
		    		out = clientSocket.getOutputStream();
		    		dos = new DataOutputStream(out);
		    	}catch (IOException e) {
		    		System.out.println("Read failed");
		    		System.exit(-1);
		    	}
		    	byte[] head = generate_header(secrets[0], 32);
		    	for (int i = 0; i < 12; i++) {
		    		sendData[i] = head[i];
		    	}
		    	ByteBuffer content = ByteBuffer.allocate(32);
		    	Random rand = new Random();
		    	int port_num = rand.nextInt(99000) + 1000;
		    	content.putInt(rand.nextInt(99) + 1).putInt(rand.nextInt(499) + 1).putInt(port_num).putInt(secrets[0]);
		    	byte[] content_byte = content.array();
		    	for (int i = 0; i < content_byte.length; i++) {
		    		sendData[i + 12] = content_byte[i];
		    	}
		    	dos.write(sendData, 0, 48);
		    	return port_num;
		    }
		}
		
		public boolean verify_header(int psecret, ByteBuffer head_buf) {
			if (psecret != head_buf.getInt(4)) {
				return false;
			}
			if (head_buf.getShort(8) != (short) 1) {
				return false;
			}
			if (head_buf.getShort(10) != (short) student_id) {
				return false;
			}
			return true;
		}
		
		public byte[] generate_header(int secret, int content_len) {
			ByteBuffer header = ByteBuffer.allocate(12);
			header.putInt(content_len).putInt(secret).putShort((short) 2).putShort((short) student_id);
			return header.array();
		}
		
		public void generate_secret() {
			Random rand = new Random();
			secrets[0] = rand.nextInt(student_id);
			secrets[1] = rand.nextInt(student_id);
			secrets[2] = rand.nextInt(student_id);
		}
		
	}
}
