import java.io.*;
import java.net.*;

public class Server extends Thread{

	public static void main(String[] args) {
		int port = 1234;
		try{
			ServerSocket ss = new ServerSocket(port); //����� ������� �� ��������� �����
			System.out.println("�������� �����������.");
			Socket socket = ss.accept(); //������ ���� �����������.
			System.out.println("����������� �����������.");
			
			InputStream inS = socket.getInputStream(); //������� � �������� ������ ������.
			OutputStream outS = socket.getOutputStream();
			
	        DataInputStream in = new DataInputStream(inS); //����������� ��� ���������� ��������� ������.
	        DataOutputStream out = new DataOutputStream(outS);
			
			String line = new String();
			
			while(true){
				line = in.readUTF();
				System.out.println("��������� �������:" + line);
				out.writeUTF(line); //�������� ������� �������.
		        out.flush(); // ���������� ����� ��������� �������� ������.
		        System.out.println("�������� �����������.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
