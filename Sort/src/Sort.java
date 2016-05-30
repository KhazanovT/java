import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Sort {
public static BufferedReader in;
public static String textString;
public static List<Item> listItem;

static class Item {
	  String key;
	  String value;
	 public Item(String v) {
	    value = v;
	    key = v.toLowerCase(); 
	  }
	}

	public static void readText(String way){
		listItem = new ArrayList<Item>();
		String[] textArr;
		 try {
			 in = new BufferedReader(new FileReader(way));
			 while((textString = in.readLine()) != null){
				 textArr = textString.split(" ");
				 for(int i = 0; i < textArr.length; i++){
					 listItem.add(new Item(textArr[i]));
					}
			 }
		 } catch (FileNotFoundException ex){
			 System.err.println("File not found.");
		 } catch (IOException e){
			 System.err.print("Can't read the file.");
		 } finally {
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
		readText(way.nextLine());
		Collections.sort(listItem, new Comparator<Item>() {
			  public int compare(Item o1, Item o2) {
			    return o1.key.compareTo(o2.key);
			  }
		});
/*		textArr = textString.split(" ");
		Collections.sort(textList);
		System.out.println(textString);
		System.out.println(textList);*/
		 System.out.println("Sort text: " + listItem);
	}

}
