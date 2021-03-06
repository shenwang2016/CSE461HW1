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
	

	public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = new DatagramSocket(12235); 
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
		public int student_id = 0;
		public int[] secrets = new int[4];
		
		public Client_handler(DatagramPacket receivePacket, DatagramSocket serverSocket) {
			this.receivePacket = receivePacket;
			this.serverSocket = serverSocket;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//generate_secret();
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
			System.out.println("num of packets: " + num2);
			System.out.println("length d: " + len2);
			System.out.println("char origin: " + c);
			// get input from client
			
			System.out.println("apple" + clientSocket.isClosed());
			InputStream in;
			DataInputStream dis = null;
			int counter = 0;
			while (counter != num2) {
				/*
				InputStream in;
				DataInputStream dis = null;
				*/
				try {
					in = clientSocket.getInputStream();
					dis = new DataInputStream(in);
					System.out.println("first try");
				} catch (IOException e) {
					System.out.println("in or out failed");
					System.exit(-1);
				}
				System.out.println("enter while loop");
				byte[] data = new byte[12 + len2 + padding_bytes(len2)];
				dis.read(data);
				ByteBuffer in_data = ByteBuffer.wrap(data);
				System.out.println("stage d len: " + in_data.getInt(0));
				System.out.println("stage d psecret: " + in_data.getInt(4));
				System.out.println("stage d step num: " + in_data.getShort(8));
				System.out.println("stage d sid: " + in_data.getShort(10));
				//System.out.println("stage d tcp_port a to b: " + port_num);
				
				// verify whether the secret is 0
				if (!verify_header(secrets[2], in_data)) {
					System.out.println(counter);
					System.out.println("header format problem");
					System.exit(-1);
				}
				System.out.println("actual c: " + c);
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
			ByteBuffer sendData = ByteBuffer.allocate(16);
			generate_header(secrets[2], 4, sendData);
			sendData.putInt(12, secrets[3]);
			dos.write(sendData.array(), 0, 16);
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
			ByteBuffer sendData = ByteBuffer.allocate(12 + actual_payload + padding_byte);
			generate_header(secrets[1], actual_payload, sendData);
			Random rand = new Random();
			int num2 = rand.nextInt(99) + 1;
			int len2 = rand.nextInt(499) + 1;
			// create a random c
			byte[] c = new byte[1];
			rand.nextBytes(c);
			sendData.putInt(12, num2).putInt(16, len2).putInt(20, secrets[2]).put(24, c[0]);
			// stuffing with padding
			for (int i = 0; i < padding_byte; i++) {
				byte temp = 0;
				sendData.put(i + 25, temp);
			}
			byte[] send = sendData.array();
			dos.write(send, 0, 12 + actual_payload + padding_byte);
			ByteBuffer from_stage_c = ByteBuffer.allocate(9);
			from_stage_c.putInt(num2).putInt(len2).put(c[0]);
			System.out.println("char in stage c: " + c[0]);
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
				
				System.out.println("ack receive header len: " + ack_pack.getInt(0));
				System.out.println("ack receive header psecret b: " + ack_pack.getInt(4));
				System.out.println("ack receive header step num b: " + ack_pack.getShort(8));
				System.out.println("ack receive header sid b: " + ack_pack.getShort(10));
				
				int packet_id = ack_pack.getInt(12);
				System.out.println("check pack_id: " + packet_id);
				for (int i = 0; i < len; i++) {
					byte temp = ack_pack.get(i + 16);
					if (temp != (byte) 0) {
						System.out.println("payload content is not correct!");
						continue;
					}
				}
				
				// send data
				ByteBuffer sendData = ByteBuffer.allocate(16);
				generate_header(secrets[0], 4, sendData);
				
				
				// prepare packet
				System.out.println("len a to b: " + len);
				System.out.println("len b: " + sendData.getInt(0));
				System.out.println("psecret b: " + sendData.getInt(4));
				System.out.println("step num b: " + sendData.getShort(8));
				System.out.println("sid b: " + sendData.getShort(10));
				sendData.putInt(12, packet_id);
				System.out.println("packid b: " + sendData.getInt(12));
				byte[] send = sendData.array();
				DatagramPacket sendPacket_b = new DatagramPacket(send, send.length, IPAddress, port);
				clientSocket.send(sendPacket_b);
				counter++;
			}
			ByteBuffer sendData = ByteBuffer.allocate(20);
			generate_header(secrets[0], 8, sendData);
			// ByteBuffer content = ByteBuffer.allocate(8);
			Random rand = new Random();
			int tcp_port = rand.nextInt(49000) + 1024;
			while (port_num - 12235 == 0) {
				tcp_port = rand.nextInt(49000) + 1024;
			}
			System.out.println(secrets[1]);
			sendData.putInt(12, tcp_port).putInt(16, secrets[1]);
			
			System.out.println("len: " + sendData.getInt(0));
			System.out.println("psecret: " + sendData.getInt(4));
			System.out.println("step num: " + sendData.getShort(8));
			System.out.println("sid: " + sendData.getShort(10));
			System.out.println("tcp_port a to b: " + port_num);
			
			System.out.println("tcp port: " + sendData.getInt(12));
			System.out.println("secretB: " + sendData.getInt(16));
			byte[] send = sendData.array();
			DatagramPacket sendPacket = new DatagramPacket(send, send.length, IPAddress, port);
			clientSocket.send(sendPacket);
			return tcp_port;

		}

		public int[] stageA() throws Exception {
			// get input from client
			// byte[] sendData = new byte[28];
			ByteBuffer sendData = ByteBuffer.allocate(28);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
			// extract data from server packet
			byte[] fromClient = receivePacket.getData();
			ByteBuffer bf = ByteBuffer.wrap(fromClient);
			
			//student_id = 927;
			student_id = bf.getShort(10);
			generate_secret();
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

			generate_header(0, 16, sendData);
			System.out.println("duck generate header");
			System.out.println(sendData.getShort(8));
			System.out.println(sendData.getShort(10));
			
			Random rand = new Random();
			int port_num = rand.nextInt(49000) + 1024;
			System.out.println("port num: " + port_num);
			while (port_num - 12235 == 0) {
				port_num = rand.nextInt(49000) + 1024;
			}
			int num_send = rand.nextInt(99) + 1;
			System.out.println("num send: " + num_send);
			int len = rand.nextInt(499) + 1;
			System.out.println("len: " + len);
			System.out.println("appleAA: " + secrets[0]);
			sendData.putInt(num_send).putInt(len).putInt(port_num).putInt(secrets[0]);
			
			System.out.println("len A: " + sendData.getInt(0));
			System.out.println("psecret A: " + sendData.getInt(4));
			System.out.println("step num A: " + sendData.getShort(8));
			System.out.println("sid A: " + sendData.getShort(10));
			
			System.out.println("NUM A: " + sendData.getInt(12));
			System.out.println("LEN A: " + sendData.getInt(16));
			System.out.println("UDP PORT A: " + sendData.getInt(20));
			System.out.println("SECRET A: " + sendData.getInt(24));
            byte[] send = sendData.array();
			DatagramPacket sendPacket = new DatagramPacket(send, send.length, IPAddress, port);
			serverSocket.send(sendPacket);
			
			/*System.out.println("len A: " + bfA.getInt(0));
			System.out.println("psecret A: " + bfA.getInt(4));
			System.out.println("step num A: " + bfA.getInt(8));
			System.out.println("sid A: " + bfA.getInt(10));
			
			System.out.println("NUM A: " + bfA.getInt(12));
			System.out.println("LEN A: " + bf.getInt(16));
			System.out.println("UDP PORT A: " + bf.getInt(20));
			System.out.println("SECRET A: " + bf.getInt(24));*/
			int[] from_stage_a = {num_send, len, port_num };
			return from_stage_a;
         }

		// we only need to verify psecret, step num, and student ID last 3
		// digits
		public boolean verify_header(int psecret, ByteBuffer head_buf) {
			int psecret_get = head_buf.getInt(4);
			System.out.println("secret: " + psecret_get);
			int step_num_get = head_buf.getShort(8);
			System.out.println("step_num: " + step_num_get);
			int student_id_get = head_buf.getShort(10);
			System.out.println("student_id: " + student_id_get);
			System.out.println("enter verify header");
			if (psecret != psecret_get) {
				System.out.println("psecret wrong");
				return false;
			}
			if (step_num_get != (short) 1) {
				System.out.println("step num wrong");
				return false;
			}
			if (student_id_get != (short) student_id) {
				System.out.println("sid wrong");
				return false;
			}
			return true;
		}

		public void generate_header(int secret, int content_len, ByteBuffer header) {
			//ByteBuffer header = ByteBuffer.allocate(12);
			short step_num = 2;
			//short student = 927;
			header.putInt(content_len).putInt(secret).putShort(step_num).putShort((short)student_id);
			System.out.println("header step header: " + header.getInt(0));
			System.out.println("header step header: " + header.getInt(4));
			System.out.println("duck");
			System.out.println(header.getShort(8));
			System.out.println(header.getShort(10));
			//return header.array();
		}

		public void generate_secret() {
			Random rand = new Random();
			secrets[0] = rand.nextInt(student_id);
			secrets[1] = rand.nextInt(student_id);
			secrets[2] = rand.nextInt(student_id);
			secrets[3] = rand.nextInt(student_id);
			/*secrets[0] = 11;
			secrets[1] = 12;
			secrets[2] = 13;
			secrets[3] = 14;*/
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
