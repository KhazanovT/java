import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Sort {
public static BufferedReader in;
public static String textString;
public static byte[] textByte;

	public static void readText(String way){
		 try{
			 in = new BufferedReader(new FileReader(way));
			 textString = in.readLine();
		 } catch (FileNotFoundException ex){
			 System.err.println("File not found.");
		 } catch (IOException e){
			 System.err.print("Can't read the file.");
		 } 
		 finally {
			 if(in != null){
				 try {
					in.close();
				} catch (IOException e) {
					System.err.print("Can't close the file.");
				}
			 }
		 }
	}
	public static void main(String[] args){
		System.out.println("¬ведите путь к файлу:");
		Scanner way = new Scanner(System.in);
		String textWay = way.nextLine();
		List<String> textList = new ArrayList<String>();
		String[] textArr;
		readText(textWay);
		textArr = textString.split(" ");
		for(int i = 0; i < textArr.length; i++){
			textList.add(textArr[i]);
		}
		Collections.sort(textList);
		textByte = textString.getBytes();
		System.out.println(textString);
		System.out.println(textList);
	}

}
