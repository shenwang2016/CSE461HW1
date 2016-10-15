import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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
		private int[] secrets;
		
		
		public Client_handler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
		public void stageD() {
			
		}
		
		public void stageC() {
			
		}
		
		public void stageB() {
			
		}
		
		public void stageA() throws Exception {
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
		    for (int i = 0; i < secret_phase.length(); i++) {
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
		    OutputStream out = clientSocket.getOutputStream(); 
		    DataOutputStream dos = new DataOutputStream(out);
		    while(true){
		    	try{
		    		
		    	}catch (IOException e) {
		    		System.out.println("Read failed");
		    		System.exit(-1);
		    	}
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
			return false;
		}
		
		public byte[] generate_header(int secret, int content_len) {
			ByteBuffer header = ByteBuffer.allocate(12);
			header.putInt(content_len).putInt(secret).putShort((short) 2).putShort((short) student_id);
			return header.array();
		}
		
		public void generate_secret() {
			int[] secrets = new int[3];
			Random rand = new Random();
			secrets[0] = rand.nextInt(student_id);
			secrets[1] = rand.nextInt(student_id);
			secrets[2] = rand.nextInt(student_id);
			this.secrets = secrets;
		}
		
	}
}
