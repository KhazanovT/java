import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;


public class StringReader {

public static String key, keyLow, str, strLow;	
//public static HashSet<String> keySet;
static Set<String> keySet = new HashSet();

public static void keyWord(){
	System.out.println("Введите ключевое слово:");
	Scanner in = new Scanner(System.in);
	key = in.nextLine();
	keyLow = key.toLowerCase(); //Сравнение с ключом вне зависимости от регистра.
//	in.close();
	System.out.println("Ключевое слово:" + key);
	//На случай, если несколько слов в ключе.
	String[] keyMass = keyLow.split(" ");
	for (int i = 0; i < keyMass.length; i++){
		keySet.add(keyMass[i]);
		//System.out.println(keySet);
	}
}

public static void filterByKey(){
	while(true){
		System.out.println("Введите строку:");
		Scanner in = new Scanner(System.in);
		str = in.nextLine();
		strLow = str.toLowerCase();
		in.close();
		if(strLow.contains(keyLow)){
			System.out.println(str);
		} else if (str.equals("Exit")) {
			System.exit(1);
		}
	}
}
	public static void main(String[] args) {
		
		keyWord();
		System.out.println(keySet);
		filterByKey();
	}

}
