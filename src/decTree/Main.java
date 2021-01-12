package decTree;
import java.io.*;

public class Main {
	public static void main(String[] args) {
		File file = new File("src\\decTree\\breast-cancer.data");
		try {
			@SuppressWarnings("unused")
			Table table = new Table(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
