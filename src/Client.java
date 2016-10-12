/**
 * 
 */
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
/**
 * @author Yilun Hua(1428927), Shen Wang()
 *
 */
public class Client {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		
		// This part is a sample code that sends out a UDP packet
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");

		// needs header
		String sentence = "hello world";
		int payload_length = sentence.getBytes().length;
		// stage a, buffer should contain hello world
		// added 4 bytes just to play safe
		// stage a 1
		// if payload size can be divided by 4, we don't need padding
		byte[] sendData;
		if(payload_length % 4 == 0) {
			sendData = new byte[payload_length + 12];
		} else { // we need padding payload to be divisible by 4
		    sendData = new byte[payload_length + 12 + 4 - payload_length % 4];
		}
		//int payload_len = payload_length;  // 4 bytes
		int psecret = 0;  // 4 bytes
		short step_num = 1;  // 2 bytes
		short student_id = 927;  // 2 bytes
		ByteBuffer header = ByteBuffer.allocate(12);
		// put fields into header
		header.putInt(payload_length).putInt(psecret).putShort(step_num).putShort(student_id);
		// save header info into sendData[]
		byte[] header_array = header.array();
		System.out.println(header_array.length);
		for (int i = 0; i < 12; i++) {
			sendData[i] = header_array[i];
		}
		// save payload into sendData[]
		byte[] sentence_byte = sentence.getBytes();
		//sentence_byte = padding(sentence_byte, payload_length);
		for (int i = 0; i < payload_length; i++) {
			// save sentence info
			if(i < sentence_byte.length) {
			   sendData[i + 12] = sentence_byte[i];
			} else { // add padding
			    byte temp = 0;
			    sendData[i + 12] = temp;
			}
		}
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		System.out.println("stage a1 done");
		// stage a 2
		byte[] receiveData = new byte[28];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		System.out.println("before receive");
		clientSocket.receive(receivePacket);
		System.out.println("after receive");
		byte[] fromServer = receivePacket.getData();
		assert(fromServer.length == 28);
		byte[] payload_server = new byte[16];
		for (int i = 12; i < 28; i++) {
			payload_server[i - 12] = fromServer[i];
		}
		int[] data = decryptSecret(payload_server);
		System.out.println("stage a2 done");
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
