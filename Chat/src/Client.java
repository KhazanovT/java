import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Client {

	public static void main(String[] args) {
		int serverPort = 1234;
		String address = "127.0.0.1";
		try {
			InetAddress ipAddress = InetAddress.getByName(address);
			Socket socket = new Socket (ipAddress, serverPort);
			
			InputStream inS = socket.getInputStream();
			OutputStream outS = socket.getOutputStream();
			
			DataInputStream in = new DataInputStream(inS); //Конвертация для упрощенной обработки текста.
	        DataOutputStream out = new DataOutputStream(outS);
			
			String line = new String();
			Scanner mail = new Scanner(System.in);
			while(true){
				line = mail.nextLine();
				out.writeUTF(line); 
		        out.flush();
		        line = in.readUTF();
		        System.out.println("Сообщение от сервера:" + line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
