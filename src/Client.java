/**
 * 
 */
import java.io.*;
import java.net.*;
/**
 * @author ylh96
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
		BufferedReader inFromUser =
		         new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		// stage a, buffer should contain hello world
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];
		// needs header
		String sentence = inFromUser.readLine();
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
        
		// This part is a sample code that uses socket
		String sentence1;
        String secret;
        BufferedReader message = new BufferedReader(new InputStreamReader(System.in));

        Socket socket = new Socket("attu2.cs.washington.edu", 12235);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        sentence1 = message.readLine();
        outToServer.writeBytes(sentence1 + '\n');
        secret = inFromServer.readLine();
        System.out.println(decryptSecret(secret));
        socket.close();
	}
	
	public static String decryptSecret(String fromServer) {
		return null;
	}

}
