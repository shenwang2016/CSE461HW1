/**
 * 
 */
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
/**
 * @author Yilun Hua (1428927), Shen Wang (1571169)
 *
 */
public class ClientSW {
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		// This part is a sample code that sends out a UDP packet
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		clientSocket.connect(IPAddress, 12235);
		// array to store secrets
		int[] secrets = new int[4];
		// in stage a, buffer should contain hello world
		String sentence = "hello world\0";
		byte[] sentence_byte = sentence.getBytes();
		int payload_length = sentence_byte.length;
		// stage a1
		byte[] sendData;
		// if payload size can be divided by 4, we don't need padding
        if(payload_length % 4 == 0) {
			sendData = new byte[payload_length + 12];
		} else { // we need padding payload to be divisible by 4
		    sendData = new byte[payload_length + 12 + 4 - payload_length % 4];
		}
		int psecret = 0;  // 4 bytes
		short step_num = 1;  // 2 bytes
		short student_id = 927;  // 2 bytes
		ByteBuffer header = ByteBuffer.allocate(12);
		// save fields into header
		header.putInt(payload_length).putInt(psecret).putShort(step_num).putShort(student_id);
		// save header info into sendData[]
		byte[] header_array = header.array();
		for (int i = 0; i < 12; i++) {
			sendData[i] = header_array[i];
		}
		// save payload into sendData[]
		for (int i = 0; i < payload_length; i++) {
			// save sentence info
			if(i < sentence_byte.length) {
			   sendData[i + 12] = sentence_byte[i];
			} else { // add padding
			    byte temp = 0;
			    sendData[i + 12] = temp;
			}
		}
		// send packet from client to server
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		// stage a2
		// send packet from server to client
		byte[] receiveData = new byte[28];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		byte[] fromServer = receivePacket.getData();
		// extract data from server packet
		// defending programming 
		// check actual payload_len info and payload_len stored in header
		byte[] return_header_payload_len = new byte[4];
		for (int i = 0; i < 4; i++) {
			return_header_payload_len[i] = fromServer[i];
		}
		int[] array_int = decryptSecret(return_header_payload_len);
		assert(array_int.length == 1);
		assert(array_int[0] == 16);
		// get packet payload
		byte[] payload_server = new byte[array_int[0]];
		for (int i = 12; i < 28; i++) {
			payload_server[i - 12] = fromServer[i];
		}
		int[] payload_data = decryptSecret(payload_server);
		assert(payload_data.length == 4);
		int count_max = payload_data[0]; // num of packets client will need to send
		int payload_len = payload_data[1]; // get the payload size of packets that will be send
		int udp_port = payload_data[2]; // port num for next few packets
		int secretA = payload_data[3]; // get secret A
		secrets[0] = secretA; // save secretA
		// stage b1
		// build header
		int actual_payload_len = payload_len + 4;
		header = ByteBuffer.allocate(12);
		header.putInt(actual_payload_len).putInt(secretA).putShort(step_num).putShort(student_id);
        // reinitialize sendData array
		// if payload size can be divided by 4, we don't need padding
        if(actual_payload_len % 4 == 0) {
			sendData = new byte[actual_payload_len + 12];
		} else { // we need padding payload to be divisible by 4
		    sendData = new byte[actual_payload_len + 12 + 4 - actual_payload_len % 4];
		}
        // save header info into sendData
        header_array = header.array();
        for (int i = 0; i < 12; i++) {
			sendData[i] = header_array[i];
		}
        // keep sending packets to server, until count reach to count_max
        int count_num = 0;
		while (true) {
			System.out.println("start of while loop");
			// adding payload starts from here
			// add packet id
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
			DatagramPacket sendPacket_b = new DatagramPacket(sendData, sendData.length, IPAddress, udp_port);
			clientSocket.connect(IPAddress, udp_port);
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
			byte[] ack_array = new byte[4];
			for(int i = 12; i < 16; i++) {
				ack_array[i-12] = receive_ack[i];
			}
			int[] ack_id = decryptSecret(ack_array);
			if (ack_id[0] == count_num) {
				count_num++;
			} else {
				continue;
			}
			if (count_num == count_max) {
				break;
			}
		}
		System.out.println("while loop finish, server should send secretB");
		byte[] receiveData_b = new byte[20];
		DatagramPacket receivePacket_b = new DatagramPacket(receiveData_b, receiveData_b.length);
		clientSocket.receive(receivePacket_b);
		System.out.println("check 2");
		// get receivePacket_b payload
		byte[] data_b = new byte[8];
		for (int i = 0; i < 8; i++) {
			data_b[i] = receiveData_b[i + 12];
		}
		int[] payload_b = decryptSecret(data_b);
		assert(payload_b.length == 2);
		int tcp_port = payload_b[0];
		int secretB = payload_b[1];
		secrets[1] = secretB;
		System.out.println(secrets[0] + "  " + secrets[1]);
		// step b done
		clientSocket.close();
	}

	
	public static byte[] padding(byte[] ba, int content_length) {
		int padding_num = 4 - content_length % 4;
		// if content length is not divisible by 4, we need add padding
		if (padding_num < 4) {
			while (padding_num > 0) {
				byte temp = 0;
				ba[content_length + padding_num - 1] = temp; 
				padding_num--;
			}
		}
		return ba;
	}
	
	public static int[] decryptSecret(byte[] byteArray) {
		IntBuffer intBuf =
				   ByteBuffer.wrap(byteArray)
				     .order(ByteOrder.BIG_ENDIAN)
				     .asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		return array;
	}

}
