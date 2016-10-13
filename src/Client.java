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
public class Client {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		
		// This part is a sample code that sends out a UDP packet
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		// array to store secrets
		int[] secrets = new int[4];
		// in stage a, buffer should contain hello world
		String sentence = "hello world";
		sentence += "\0"; // add end mark
		int payload_length = sentence.getBytes().length;
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
		byte[] sentence_byte = sentence.getBytes();
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
		// extract data from server packet
		byte[] fromServer = receivePacket.getData();
		
		// assure returning packet_header payload_len is the same starts here
		byte[] return_packet_header_payload_len = new byte[4];
		for (int i = 0; i < 4; i++) {
			return_packet_header_payload_len[i] = fromServer[i];
		}
		// get the length of receiving payload
		int[] array_int = decryptSecret(return_packet_header_payload_len);
        // assure returning packet_header payload_len is the same ends here
		
		// get packet payload
		byte[] payload_server = new byte[array_int[0]];
		for (int i = 12; i < 28; i++) {
			payload_server[i - 12] = fromServer[i];
		}
		int[] data = decryptSecret(payload_server);
		secrets[0] = data[3];
		// part b starts here
		int count_num = 0;
		int count_max = data[0];
		int port_num = data[2];
		int len_payload = data[1];
		while (true) {
			// adding header starts from here
			byte[] sendData_b;
			if (len_payload % 4 == 0) {
				sendData_b = new byte[len_payload + 4 + 12];
			} else {
				sendData_b = new byte[len_payload + 4 + (4 - len_payload % 4) + 12];
			}
			byte[] len_byte = ByteBuffer.allocate(4).putInt(len_payload).array();
			assert(len_byte.length == 4);
			// add header
			for (int i = 0; i < 12; i++) {
				if( i < 4) {
				   sendData_b[i] = len_byte[i];
				} else {
					sendData_b[i] = header_array[i-4];
				}
			}
			// add packet id
			byte[] packet_id = ByteBuffer.allocate(4).putInt(count_num).array();
			for (int i = 0; i < 4; i++) {
				sendData_b[i + 12] = packet_id[i];
			}
			// add payload content: 0s
			for (int i = 0; i < len_payload; i++) {
				sendData_b[i + 16] = 0;
			}
			DatagramPacket sendPacket_b = new DatagramPacket(sendData_b, sendData_b.length, IPAddress, port_num);
			// set retransmit interval
			clientSocket.setSoTimeout(500);
			try{ 
				clientSocket.send(sendPacket_b);
			} catch (SocketTimeoutException e) {
				continue;
			}
			byte[] receiveData_b = new byte[16];
			DatagramPacket receivePacket_b = new DatagramPacket(receiveData_b, receiveData_b.length);
			clientSocket.receive(receivePacket_b);
			count_num++;
			if (count_num == count_max) {
				break;
			}
		}
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
