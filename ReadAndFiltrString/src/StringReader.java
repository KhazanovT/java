import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class StringReader {

public static String key, keyLow, str, strLow;	
static Set<String> keySet;
static ArrayList<String> keyList = new ArrayList();
static String[] keyMassGeneral;

public static void keyWord(){
	System.out.println("������� �������� �����:");
	keySet = new HashSet();
	Scanner in = new Scanner(System.in);
	key = in.nextLine();
	keyLow = key.toLowerCase(); //��������� � ������ ��� ����������� �� ��������.
//	in.close();
	System.out.println("�������� �����:" + key);
	//�� ������, ���� ��������� ���� � �����.
	String[] keyMass = keyLow.split(" ");
	for (int i = 0; i < keyMass.length; i++){
		keySet.add(keyMass[i]);
		//System.out.println(keySet);
	}
	keyList.addAll(keySet);
}

public static void filterByKey(){
	boolean test = false;	
	System.out.println("������� ������:");
	Scanner in = new Scanner(System.in);
	str = in.nextLine();
	strLow = str.toLowerCase();
	//in.close();
	for(int i = 0; i < keyList.size(); i++){
		if(strLow.contains(keyList.get(i))){
			test = true;
		}
	}
	if(test == true){
		System.out.println(str);
	}
}
	public static void main(String[] args) {
		while(true){
			keyWord();
			filterByKey();
		}
	}

}
