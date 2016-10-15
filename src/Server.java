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
	
	public static boolean verify_header(int psecret, byte[] head) {
		if (head.length != 12) {
			return false;
		}
		ByteBuffer head_buf = ByteBuffer.wrap(head);
		if (psecret != head_buf.getInt(4)) {
			return false;
		}
		if (head_buf.getShort(8) != 1) {
			return false;
		}
		return false;
	}
	
	public static byte[] generate_header(int secret, int student_id, int content_len) {
		ByteBuffer header = ByteBuffer.allocate(12);
		header.putInt(content_len).putInt(secret).putShort((short) 2).putShort((short) student_id);
		return header.array();
	}
	
	public static int[] generate_secret(int student_id) {
		int[] secrets = new int[3];
		Random rand = new Random();
		secrets[0] = rand.nextInt(student_id);
		secrets[1] = rand.nextInt(student_id);
		secrets[2] = rand.nextInt(student_id);
		return secrets;
	}

}
