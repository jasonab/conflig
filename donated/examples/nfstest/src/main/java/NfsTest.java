import java.io.File;

public class NfsTest {
	public static void main(String args[]) throws Exception {
		if (args.length != 1) {
			testFileExists();
			testWriteFile();
		} else if (args[0].equals("1")) {
			testFileExists();
		} else if (args[1].equals("2")) {
			testWriteFile();
		}
	}
	
	private static void testFileExists() throws Exception {
		System.out.println(System.nanoTime() + ": testFileExists ENTER");
		String fileName = "/data/uMessage/config/nfstest/DO_NOT_DELETE.txt";
		System.out.println(System.nanoTime() + ": Checking " + fileName);
		File file = new File(fileName);
		if (file.exists()) {
			System.out.println(System.nanoTime() + ": PASS : File exists " + fileName);
		} else {
			System.out.println(System.nanoTime() + ": FAIL : File does not exist " + fileName);
			throw new RuntimeException();
		}
		System.out.println(System.nanoTime() + ": testFileExists EXIT");
	}
	
	private static void testWriteFile() throws Exception {
		System.out.println(System.nanoTime() + ": testWriteFile ENTER");
		String dirName = "/data/uMessage/config/nfstest";
		System.out.println(System.nanoTime() + ": Checking " + dirName);
		File dir = new File(dirName);
		if (dir.exists()) {
			System.out.println(System.nanoTime() + ": PASS : Directory exists " + dirName);
		} else {
			System.out.println(System.nanoTime() + ": FAIL : Directory does not exist " + dirName);
			throw new RuntimeException();
		}
		
		System.out.println(System.nanoTime() + ": Creating Temp File " + dirName);
		File file = File.createTempFile("nfs", "foo", dir);
		
		System.out.println(System.nanoTime() + ": File created " + file.getName());
		
		if (file.exists()) {
			System.out.println(System.nanoTime() + ": PASS : File exists " + file.getName());
		} else {
			System.out.println(System.nanoTime() + ": FAIL : File does not exist " + file.getName());
			throw new RuntimeException();
		}
		
		System.out.println(System.nanoTime() + ": Deleting the file " + file.getName());
		file.delete();
		
		System.out.println(System.nanoTime() + ": testWriteFile EXIT");
	}
}