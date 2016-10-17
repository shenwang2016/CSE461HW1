import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Shen Wang(1571169), Yilun Hua (1428927)
 *
 */
public class Server {
	/**
	 * @param args
	 * @throws IOException
	 */
	public int student_id = 0;
	public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(12345); 
		while(true){
			byte[] receiveData = new byte[24];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			serverSocket.receive(receivePacket);
			new Thread(
					new Client_handler(
							receivePacket, serverSocket)
						).start(); 
		  	}
	}

	static class Client_handler implements Runnable {

		public DatagramPacket receivePacket;
		public DatagramSocket serverSocket;
		public int student_id;
		public int[] secrets = new int[4];

		public Client_handler(DatagramPacket receivePacket, DatagramSocket serverSocket) {
			this.receivePacket = receivePacket;
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int[] data = null;
			try {
				data = stageA();
			} catch (Exception e) {
				e.printStackTrace();
			}
			int next_port = 0;
			try {
				next_port = stageB(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ServerSocket new_server = null;
			try {
				new_server = new ServerSocket(next_port);
			} catch (IOException e) {
				System.out.println("Could not listen on port " + next_port);
				System.exit(-1);
			}
			Socket client = null;
			try {
				client = new_server.accept();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ByteBuffer data_from_c = null;
			try {
				data_from_c = stageC(client);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				stageD(data_from_c, client);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void stageD(ByteBuffer from_stage_c, Socket clientSocket) throws IOException {
			int num2 = from_stage_c.getInt(0);
			int len2 = from_stage_c.getInt(4);
			byte c = from_stage_c.get(8);
			System.out.println("char origin: " + c);
			// get input from client
			
			System.out.println("apple" + clientSocket.isClosed());
			
			int counter = 0;
			while (counter != num2) {
				InputStream in;
				DataInputStream dis = null;
				try {
					in = clientSocket.getInputStream();
					dis = new DataInputStream(in);
					System.out.println("first try");
				} catch (IOException e) {
					System.out.println("in or out failed");
					System.exit(-1);
				}
				System.out.println("enter while loop");
				byte[] data = new byte[12 + len2];
				dis.read(data);
				ByteBuffer in_data = ByteBuffer.wrap(data);
				// verify whether the secret is 0
				if (!verify_header(secrets[2], in_data)) {
					System.out.println(counter);
					System.out.println("header format problem");
					System.exit(-1);
				}
				for (int i = 0; i < len2; i++) {
					byte temp = in_data.get(i + 12);
					System.out.println("char get: " + temp);
					if (temp != c) {
						System.out.println(i);
						System.out.println("wrong message");
						System.exit(-1);
					}
				}
				counter++;
			}
			// send data to client
			OutputStream out;
			DataOutputStream dos = null;
			try {
				out = clientSocket.getOutputStream();
				dos = new DataOutputStream(out);
			} catch (IOException e) {
				System.out.println("Read failed");
				System.exit(-1);
			}
			byte[] sendData = new byte[16];
			byte[] head = generate_header(secrets[3], 4);
			for (int i = 0; i < 12; i++) {
				sendData[i] = head[i];
			}
			ByteBuffer content = ByteBuffer.allocate(4);
			content.putInt(secrets[1]);
			byte[] content_byte = content.array();
			for (int i = 0; i < content_byte.length; i++) {
				sendData[i + 12] = content_byte[i];
			}
			dos.write(sendData, 0, 16);
		}

		public ByteBuffer stageC(Socket clientSocket) throws IOException {
			// send data to client
			OutputStream out;
			DataOutputStream dos = null;
			try {
				out = clientSocket.getOutputStream();
				dos = new DataOutputStream(out);
			} catch (IOException e) {
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
			for (int i = 0; i < padding_byte; i++) {
				byte temp = 0;
				content.put(temp);
			}
			byte[] content_byte = content.array();
			for (int i = 0; i < content_byte.length; i++) {
				sendData[i + 12] = content_byte[i];
			}
			dos.write(sendData, 0, 12 + actual_payload + padding_byte);
			ByteBuffer from_stage_c = ByteBuffer.allocate(9);
			from_stage_c.putInt(num2).putInt(len2).put(c);
			return from_stage_c;
		}

		@SuppressWarnings("resource")
		public int stageB(int[] from_stage_a) throws Exception {
			int send_num = from_stage_a[0];
			int len = from_stage_a[1];
			int port_num = from_stage_a[2];
			DatagramSocket clientSocket = new DatagramSocket(port_num);
			int counter = 0;
			int in_data_size = 12 + len + 4 + padding_bytes(len + 4);
			InetAddress IPAddress = null;
			int port = 0;
			while (counter != send_num) {
				DatagramPacket receivePacket;
				try{ 
					System.out.println("entering try catch");
					// set retransmit interval
					clientSocket.setSoTimeout(500);
					System.out.println("sent");
					byte[] receiveData = new byte[in_data_size];
					receivePacket = new DatagramPacket(receiveData, receiveData.length);
					System.out.println("socket connect status: " + clientSocket.isConnected());
					System.out.println("start receive");
					clientSocket.receive(receivePacket);
					IPAddress = receivePacket.getAddress();
					port = receivePacket.getPort();
					System.out.println("receive end");
				} catch (SocketTimeoutException e) {
					System.out.println("continue");
					continue;
				}
				byte[] receive_ack = receivePacket.getData();
				ByteBuffer ack_pack = ByteBuffer.wrap(receive_ack);
				if (ack_pack.getInt(0) - 4 != len || !verify_header(secrets[0], ack_pack)) {
					System.out.println("header format problem");
					System.exit(-1);
				}
				int packet_id = ack_pack.getInt(12);
				for (int i = 0; i < len; i++) {
					byte temp = ack_pack.get(i + 16);
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
				byte[] ack = ByteBuffer.allocate(4).putInt(packet_id).array();
				for (int i = 0; i < 4; i++) {
					sendData[12 + i] = ack[i];
				}
				// prepare packet
				DatagramPacket sendPacket_b = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				clientSocket.send(sendPacket_b);
				counter++;
			}
			byte[] sendData = new byte[20];
			byte[] head = generate_header(secrets[1], 8);
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
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			clientSocket.send(sendPacket);
			return tcp_port;

		}

		public int[] stageA() throws Exception {
			// get input from client
			byte[] sendData = new byte[28];
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			// extract data from server packet
			byte[] fromClient = receivePacket.getData();
			ByteBuffer bf = ByteBuffer.wrap(fromClient);
			student_id = bf.getShort(10);
			// verify whether the secret is 0
			if (!verify_header(0, bf)) {
				System.out.println("header format problem");
				System.exit(-1);
			}
			String secret_phase = "hello world\0";
			int phase_byte_length = secret_phase.getBytes().length;
			assert (phase_byte_length == 12);
			String incoming_phase = "";
			for (int i = 0; i < phase_byte_length; i++) {
				byte c = 0;
				try {
					c = bf.get(i + 12);
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("wrong message");
					System.exit(-1);
				}
				incoming_phase += (char) c;
			}
			if (!incoming_phase.equals(secret_phase)) {
				System.out.println("wrong message");
				System.exit(-1);
			}

			// now send packet back to client

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

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);
			int[] from_stage_a = { num_send, len, port_num };
			return from_stage_a;
         }

		// we only need to verify psecret, step num, and student ID last 3
		// digits
		public boolean verify_header(int psecret, ByteBuffer head_buf) {
			System.out.println("enter verify header");
			if (psecret != head_buf.getInt(4)) {
				System.out.println("psecret wrong");
				return false;
			}
			if (head_buf.getShort(8) != (short) 1) {
				System.out.println("step num wrong");
				return false;
			}
			if (head_buf.getShort(10) != (short) student_id) {
				System.out.println("sid wrong");
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
			/*Random rand = new Random();
			secrets[0] = rand.nextInt(student_id);
			secrets[1] = rand.nextInt(student_id);
			secrets[2] = rand.nextInt(student_id);
			secrets[3] = rand.nextInt(student_id);*/
			secrets[0] = 11;
			secrets[1] = 12;
			secrets[2] = 13;
			secrets[3] = 14;
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
