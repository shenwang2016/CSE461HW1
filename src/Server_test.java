import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class Server_test {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ServerSocket serverSocket = null;
		// InetAddress IPAddress = InetAddress.getByName("attu4.cs.washington.edu");
        try {
            serverSocket = new ServerSocket(12235);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 12235.");
            System.exit(1);
        }
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            System.err.println("Accept succeeded.");
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        int student_id = 0;
        int[] secrets = new int[4];
        stageA(clientSocket, student_id, secrets);

	}
	
	public static int[] stageA(Socket clientSocket, int student_id, int[] secrets) throws Exception {
		// get input from client
		System.out.println("in");
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
	    if (verify_header(0, in_data, student_id)) {
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
	    try{
    		out = clientSocket.getOutputStream();
    		dos = new DataOutputStream(out);
    	}catch (IOException e) {
    		System.out.println("Read failed");
    		System.exit(-1);
    	}
	    System.out.println("connected");
	    while(true){	
	    	byte[] head = generate_header(secrets[0], 16, student_id);
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
	public static boolean verify_header(int psecret, ByteBuffer head_buf, int student_id) {
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
	
	public static byte[] generate_header(int secret, int content_len, int student_id) {
		ByteBuffer header = ByteBuffer.allocate(12);
		header.putInt(content_len).putInt(secret).putShort((short) 2).putShort((short) student_id);
		return header.array();
	}
	
	public static int[] generate_secret(int student_id) {
		Random rand = new Random();
		int[] secrets = new int[4];
		secrets[0] = rand.nextInt(student_id);
		secrets[1] = rand.nextInt(student_id);
		secrets[2] = rand.nextInt(student_id);
		secrets[3] = rand.nextInt(student_id);
		return secrets;
	}

}
