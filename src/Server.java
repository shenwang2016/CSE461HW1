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
		private int[] secrets = new int[4];
		
		
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
				e.printStackTrace();
			}
			
		}
		
		public void stageD() {
			
		}
		
		public int[] stageC(int port_from_satge_b) throws IOException {
			ServerSocket new_server;
			try{
				 new_server = new ServerSocket(port_from_satge_b);
			 } catch (IOException e) {
				 System.out.println("Could not listen on port " + port_from_satge_b);
				 System.exit(-1);
			 }
			// send data to client
			 OutputStream out;
			 DataOutputStream dos = null;
			 try{
		    	out = clientSocket.getOutputStream();
		    	dos = new DataOutputStream(out);
		     }catch (IOException e) {
		    	System.out.println("Read failed");
		    	System.exit(-1);
		     }
			 
			 int actual_payload = 13;
			 int padding_byte = padding_bytes(13);
			 byte[] sendData = new byte[12 + actual_payload + padding_byte];
			 byte[] head = generate_header(secrets[1], actual_payload);
			 for (int i = 0; i < 12; i++) {
		    		sendData[i] = head[i];
		     }
			 ByteBuffer content = ByteBuffer.allocate(actual_payload + padding_byte);
			 Random rand = new Random();
			 int num2 = rand.nextInt(99) + 1;
		     int len2 = rand.nextInt(499) + 1;
		     // create a random c
		     byte[] c = new byte[1];
		     rand.nextBytes(c);
		     content.putInt(num2).putInt(len2).putInt(secrets[2]).put(c[0]);
		     // stuffing with padding
		     for(int i = 0; i < padding_byte; i++) {
		    	 byte temp = 0;
		    	 content.put(temp);
		     }
		     byte[] content_byte = content.array();
		     for (int i = 0; i < content_byte.length; i++) {
	    		sendData[i + 12] = content_byte[i];
	    	 }
	    	dos.write(sendData, 0, 12 + actual_payload + padding_byte);
	    	int [] from_stage_c = {num2, len2, (int) c[0]};
	    	return from_stage_c;
			
		}
		
		
		public int stageB(int[] from_stage_a) throws Exception {
			ServerSocket new_server;
			int send_num = from_stage_a[0];
			int len = from_stage_a[1];
			int port_num = from_stage_a[2];
			 try{
				 new_server = new ServerSocket(port_num);
			 } catch (IOException e) {
				 System.out.println("Could not listen on port " + port_num);
				 System.exit(-1);
			 }
			 int counter = 0;
			 // get data from client
			 InputStream in;
			 DataInputStream dis = null;
			 try{
			    in = clientSocket.getInputStream();
			    dis = new DataInputStream(in);
			 } catch (IOException e) {
			    System.out.println("in or out failed");
			    System.exit(-1);
			 }
			 
			 // send data to client
			 OutputStream out;
			 DataOutputStream dos = null;
			 try{
		    	out = clientSocket.getOutputStream();
		    	dos = new DataOutputStream(out);
		     }catch (IOException e) {
		    	System.out.println("Read failed");
		    	System.exit(-1);
		     }
			 
			 int in_data_size = 12 + len + 4 + padding_bytes(len + 4);
			 while (counter != send_num) {
			    byte[] data = new byte[in_data_size];
			    dis.read(data);
			    ByteBuffer in_data = ByteBuffer.wrap(data);
			    // verify header info
			    if ((in_data.getInt(0) - 4 != len) || verify_header(secrets[0], in_data)) {
			    	System.out.println("header format problem");
			    	System.exit(-1);
			    }
			    // grab payload data
			    // first check packet_id;
			    int packet_id = in_data.getInt(12);
			    for(int  i = 0; i < len; i++) {
			    	byte temp = in_data.get(i + 16);
			    	if (temp != (byte) 0) {
			    	   System.out.println("payload content is not correct!");
			    	   continue;
			    	}
			     }
				 byte[] sendData = new byte[16];
				 byte[] head = generate_header(secrets[0], 4);
				 for (int i = 0; i < 12; i++) {
			    		sendData[i] = head[i];
			     }
				 ByteBuffer content = ByteBuffer.allocate(4);
                 content.putInt(packet_id);
                 byte[] content_byte = content.array();
 		    	 for (int i = 0; i < content_byte.length; i++) {
 		    		sendData[i + 12] = content_byte[i];
 		    	 }
 		    	 dos.write(sendData, 0, 16);
 		     }
			 byte[] sendData = new byte[20];
			 byte[] head = generate_header(secrets[0], 8);
			 for (int i = 0; i < 12; i++) {
		    		sendData[i] = head[i];
		     }
			 ByteBuffer content = ByteBuffer.allocate(8);
			 Random rand = new Random();
		    	int tcp_port = rand.nextInt(49000) + 1024;
		    while (port_num - 12235 == 0) {
		    	 tcp_port = rand.nextInt(49000) + 1024;
		    }
		    content.putInt(tcp_port).putInt(secrets[1]);
		    byte[] content_byte = content.array();
		    for (int i = 0; i < content_byte.length; i++) {
	    		sendData[i + 12] = content_byte[i];
	    	}
	    	dos.write(sendData, 0, 20);
	    	return tcp_port;
			 
			
		}
		
		public int[] stageA() throws Exception {
			// get input from client
			InputStream in;
		    DataInputStream dis = null;
		    try{
		    	in = clientSocket.getInputStream();
		    	dis = new DataInputStream(in);
		    } catch (IOException e) {
		    	System.out.println("in or out failed");
		    	System.exit(-1);
		    }
		    byte[] data = new byte[24];
		    dis.read(data);
		    ByteBuffer in_data = ByteBuffer.wrap(data);
		    student_id = in_data.getShort(10);
		    //verify whether the secret is 0
		    if (verify_header(0, in_data)) {
		    	System.out.println("header format problem");
		    	System.exit(-1);
		    }
		    String secret_phase = "hello world\0"; 
		    int phase_byte_length = secret_phase.getBytes().length;
		    assert(phase_byte_length == 12);
		    String incoming_phase = "";
		    for (int i = 0; i < phase_byte_length; i++) {
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
		    byte[] sendData = new byte[28];
		    while(true){
		    	try{
		    		out = clientSocket.getOutputStream();
		    		dos = new DataOutputStream(out);
		    	}catch (IOException e) {
		    		System.out.println("Read failed");
		    		System.exit(-1);
		    	}
		    	byte[] head = generate_header(0, 16);
		    	for (int i = 0; i < 12; i++) {
		    		sendData[i] = head[i];
		    	}
		    	ByteBuffer content = ByteBuffer.allocate(16);
		    	Random rand = new Random();
		    	int port_num = rand.nextInt(49000) + 1024;
		    	while (port_num - 12235 == 0) {
		    	    port_num = rand.nextInt(49000) + 1024;
		    	}
		    	int num_send = rand.nextInt(99) + 1;
		    	int len = rand.nextInt(499) + 1;
		    	content.putInt(num_send).putInt(len).putInt(port_num).putInt(secrets[0]);
		    	byte[] content_byte = content.array();
		    	for (int i = 0; i < content_byte.length; i++) {
		    		sendData[i + 12] = content_byte[i];
		    	}
		    	dos.write(sendData, 0, 28);
		    	int[] from_stage_a = {num_send, len, port_num};
		    	return from_stage_a;
		    }
		}
		
		// we only need to verify psecret, step num, and student ID last 3 digits
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
			secrets[3] = rand.nextInt(student_id);
		}
		
		public int padding_bytes(int length) {
			if (length % 4 == 0) {
				return 0;
			} else {
				return 4 - length % 4;
			}
		}
		
	}
}