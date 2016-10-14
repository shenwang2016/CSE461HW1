import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

// import org.omg.CORBA.portable.OutputStream;

/**
 * 
 */

/**
 * @author Shen Wang(1571169), Yilun Hua (1428927)
 *
 */
public class Client_Update {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// array to store secrets
		int[] secrets = new int[4];
		int[] result_from_a = part1_stageA();
		assert(result_from_a.length == 4);
		secrets[0] = result_from_a[3];
		System.out.println("Stage A done");
		int[] result_from_b = part1_stageB(result_from_a);
		assert(result_from_b.length == 2);
		secrets[1] = result_from_a[1];
		System.out.println("Stage B done");
		// deleted this part to fix the refuse to connect problem, to go back, uncomment
		/*
		byte[] result_from_c_byte = part1_stageC(result_from_b);
		ByteBuffer from_c = ByteBuffer.wrap(result_from_c_byte);
		int[] result_from_c = {from_c.getInt(12), from_c.getInt(16), from_c.getInt(20)}; 
		char character_from_c = from_c.getChar(24);
		System.out.println(result_from_c[0]);
		System.out.println(result_from_c[1]);
		System.out.println(result_from_c[2]);
		secrets[2] = result_from_c[2];
		System.out.println(character_from_c);
		*/
		
		// this part is new
		int[] next_two_secrets = part1_stageC(result_from_b);
		// this phase, up to here
		
		System.out.println("Stage C done");
		// int secretD = part1_stageD(character_from_c, result_from_c, result_from_b[0]);
		System.out.println("Stage D done");
		
		// next phase starts here
		for (int i = 0; i < 2; i++) {
			secrets[i + 2] = next_two_secrets[i];
		}
		// up to here
		
		// secrets[3] = result_from_c[3];
		for (int i = 0; i < 4; i++) {
			System.out.println(i + " secret: " + secrets[i]);
		}
		System.out.println("Step 1 done");
	}
	
	public static int part1_stageD(char c, int[] data_from_prev, /*int port, */Socket socket) throws Exception {
		// InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		// client open socket
		// Socket socket = new Socket(IPAddress, port);
		ByteBuffer header = ByteBuffer.allocate(12);
		header.putInt(data_from_prev[1]).putInt(data_from_prev[2]).putShort((short) 1).putShort((short) 927);
		java.io.OutputStream out = socket.getOutputStream(); 
	    DataOutputStream dos = new DataOutputStream(out);
	    byte[] head = header.array();
	    int padding = padding_bytes(data_from_prev[2]);
	    byte[] sendData = new byte[12 + data_from_prev[2] + padding];
	    // store header
	    for (int i = 0; i < 12; i++) {
	    	sendData[i] = head[i];
	    }
	    boolean error = false;
	    int count_num = 0;
	    int count_fail = 0;
	    int max_fail = 100;
	    while (true) {
	    	if (socket.isOutputShutdown()) {
	    		count_fail++;
	    		if (count_fail == max_fail) {
	    			error = true;
	    			break;
	    		}
	    		continue;
	    	}
	    	
	    	// will cause buffer overflow
	    	/*
	    	ByteBuffer payload_d = ByteBuffer.allocate(data_from_prev[2]);
	    	for (int i = 0; i < data_from_prev[2]; i++) {
	    		payload_d.putChar(c);
	    	}
	    	byte[] payload_content = payload_d.array();
	    	// add the content to sendData
	    	for (int i = 0; i < data_from_prev[2]; i++) {
	    		sendData[i + 12] = payload_content[i];
	    	}
	    	*/
	    	// do not use
	    	
	    	// changed codes starts here
	    	int count = 0;
	    	while (count < data_from_prev[2]) {
	    		ByteBuffer payload_d = ByteBuffer.allocate(4);
	    		byte[] cha = payload_d.putChar(c).array();
	    		for (int i = 0; i < cha.length; i++) {
	    			if (count >= data_from_prev[2]) {
	    				break;
	    			}
	    			sendData[count] = cha[i];
	    			count++;
	    		}
	    	}
	    	// ends here
	    	
	    	// else connection is still open, send the data
	    	dos.write(sendData, 0, 12 + data_from_prev[2] + padding);
	    	count_num++;
	    	if (count_num == data_from_prev[0]) {
	    		break;
	    	}
	    }
	    // get the data the server sends back
	    int error_count = 0;
		int max_error = 100;
		int secretD = 0;
		while(true) {
			if(socket.isInputShutdown()) {
				error_count++;
	    		if (error_count == max_error) {
	    			error = true;
	    			break;
	    		}
	    		continue;
			}
			// server send packet
			InputStream in = socket.getInputStream();
		    DataInputStream dis = new DataInputStream(in);
		    byte[] data = new byte[4];
		    dis.readFully(data);  // socket execption here, reasons unknown, yet causes vary
		    					  // stackoverflow says it maybe caused by network connection
		    					  // thus, i will try again later. But otherwise HURRAY!!!
		    secretD = ByteBuffer.wrap(data).getInt();
		}
		socket.close();
		assert(!error);
		return secretD;
	}
	
	public static int[]/*byte[] */part1_stageC(int[] data_from_prev) throws Exception {
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		// client open socket
		Socket socket = new Socket(IPAddress, data_from_prev[0]);
		int error_count = 0;
		int max_error = 100;
		while(true) {
			if(socket.isConnected()) {
				break;
			} else {
				error_count++;
				if(error_count == max_error) {
					socket.close();
					System.out.println("Failed");
					return null;
				}
			}
		}
		// server send packet
		InputStream in = socket.getInputStream();
	    DataInputStream dis = new DataInputStream(in);
        byte[] data = new byte[28];
	    dis.readFully(data);
	    
	    // starts from here, is to fix the refuse to connect exception happened in part d otherwise
		ByteBuffer from_c = ByteBuffer.wrap(data);
		int[] result_from_c = {from_c.getInt(12), from_c.getInt(16), from_c.getInt(20)}; 
		char character_from_c = from_c.getChar(24);
	    int next_secret = part1_stageD(character_from_c, result_from_c, socket);
	    // delete it to go back
	    int[] secrets = {from_c.getInt(20), next_secret};
	    // socket.close();
		// return data;
	    return secrets;
	}
	
	public static int[] part1_stageB(int[] data_from_prev) throws Exception {
		// stage b1
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.connect(IPAddress, data_from_prev[2]);
		// build header
		int actual_payload_len = data_from_prev[1] + 4;
		ByteBuffer header = ByteBuffer.allocate(12);
		header.putInt(actual_payload_len).putInt(data_from_prev[3]).putShort((short) 1).putShort((short) 927);
		// reinitialize sendData array
		// if payload size can be divided by 4, we don't need padding
		int padding = padding_bytes(actual_payload_len);
		// keep sending packets to server, until count reach to count_max
		int count_num = 0;
		byte[] sendData = new byte[actual_payload_len + 12 + padding];
		// save header info into sendData
        byte[] header_array = header.array();
        for (int i = 0; i < 12; i++) {
			sendData[i] = header_array[i];
		}
		while (true) {
			System.out.println("start of while loop");
			// adding payload starts from here
		    // put the buffers togther
		    byte[] packet_id = ByteBuffer.allocate(4).putInt(count_num).array();
			for (int i = 0; i < 4; i++) {
				sendData[i + 12] = packet_id[i];
			}
			// add payload content: 0s
			for (int i = 16; i < sendData.length; i++) {
				byte temp = 0;
				sendData[i] = temp;
			}
			// prepare packet
			DatagramPacket sendPacket_b = new DatagramPacket(sendData, sendData.length, IPAddress, data_from_prev[2]);
			clientSocket.connect(IPAddress, data_from_prev[2]);
			clientSocket.send(sendPacket_b);
			System.out.println("sent");
			byte[] receiveData_b = new byte[16];
			DatagramPacket receivePacket_b = new DatagramPacket(receiveData_b, receiveData_b.length);
			System.out.println("entering try catch");
			System.out.println("socket connect status: " + clientSocket.isConnected());
			try{ 
				System.out.println("start receive");
				// set retransmit interval
				clientSocket.setSoTimeout(500);
				clientSocket.receive(receivePacket_b);
				System.out.println("receive end");
			} catch (SocketTimeoutException e) {
				System.out.println("continue");
				continue;
			}
			byte[] receive_ack = receivePacket_b.getData();
			ByteBuffer ack_pack = ByteBuffer.wrap(receive_ack);
			int ack_id = ack_pack.getInt(12);
			if (ack_id == count_num) {
				count_num++;
			} else {
				continue;
			}
			if (count_num == data_from_prev[0]) {
				break;
			}
		}
		System.out.println("while loop finish, server should send secretB");
		byte[] receiveData_b = new byte[20];
		DatagramPacket receivePacket_b = new DatagramPacket(receiveData_b, receiveData_b.length);
		System.out.println("Receive final packet");
		int count_fail = 0;
		int max_fail_before_drop = 100;
		while (true) {
			try{
				clientSocket.receive(receivePacket_b);
				break;
			} catch (SocketTimeoutException e) {
				count_fail++;
				if (count_fail == max_fail_before_drop) {
					clientSocket.close();
					return null;
				}
				System.out.println("continue");
				continue;
			}
		}
		System.out.println("check 2");
		// get receivePacket_b payload
		ByteBuffer data_b = ByteBuffer.wrap(receiveData_b);
		int tcp_port = data_b.getInt(12);
		int secretB = data_b.getInt(16);
		int[] payload_b = {tcp_port, secretB};
		assert(payload_b.length == 2);
		// step b done
		clientSocket.close();
		return payload_b;
	}
	
	public static int[] part1_stageA() throws UnknownHostException, IOException {
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.connect(IPAddress, 12235);
		if (clientSocket.isConnected()) {
			System.out.println("Connected");
		} else {
			System.out.println("Disconnected");
			clientSocket.close();
			return null;
		}
		String message = "hello world\0";
		byte[] message_byte = message.getBytes();
		int stepA1 = message_byte.length;
		int padding = padding_bytes(stepA1);
		ByteBuffer sendData = ByteBuffer.allocate(stepA1 + 12 + padding);
		sendData.putInt(stepA1).putInt(0).putShort((short) 1).putShort((short) 927);
		sendData.put(message_byte);
		byte[] send_data_a = sendData.array();
		// send packet from client to server
		DatagramPacket sendPacket = new DatagramPacket(send_data_a, send_data_a.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		// stage a2
		// send packet from server to client
		byte[] receiveData = new byte[28];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		// extract data from server packet
		byte[] fromServer = receivePacket.getData();
		ByteBuffer bf = ByteBuffer.wrap(fromServer);
		int num = bf.getInt(12);
		int len = bf.getInt(16);
		int udp_port = bf.getInt(20);
		int secretA = bf.getInt(24);
		int[] result = {num, len, udp_port, secretA};
		clientSocket.close();
		return result;
	}
	
	public static int padding_bytes(int length) {
		if (length % 4 == 0) {
			return 0;
		} else {
			return 4 - length % 4;
		}
	}

}
