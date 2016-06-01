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
	  private String key;
	  private String value;
	 public Item(String v) {
	    this.value = v;
	    this.key = v.toLowerCase(); 
	  }
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Item other = (Item) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return value;
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
		System.out.println("¬ведите путь к файлу (res/text.txt):");
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
