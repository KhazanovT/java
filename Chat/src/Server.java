import java.io.*;
import java.net.*;

public class Server extends Thread{

	public static void main(String[] args) {
		int port = 1234;
		try{
			ServerSocket ss = new ServerSocket(port); //Сокет сервера на указанном порту
			System.out.println("Ожидание подключения.");
			Socket socket = ss.accept(); //Сервер ждет подключения.
			System.out.println("Подключение установлено.");
			
			InputStream inS = socket.getInputStream(); //Входной и выходной потоки сокета.
			OutputStream outS = socket.getOutputStream();
			
	        DataInputStream in = new DataInputStream(inS); //Конвертация для упрощенной обработки текста.
	        DataOutputStream out = new DataOutputStream(outS);
			
			String line = new String();
			
			while(true){
				line = in.readUTF();
				System.out.println("Сообщение клиента:" + line);
				out.writeUTF(line); //Отсылаем обратно клиенту.
		        out.flush(); // заставляем поток закончить передачу данных.
		        System.out.println("Ожидание подключения.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
