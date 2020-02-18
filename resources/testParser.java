import java.io.File;

class testParser {
	static int even;
	static int size;
	static int ret;
	
	public static int func(int posX, int posY) {
		int tempA = posX - 2;
		int tempB = tempA - posX;
		even = tempA;
		size = tempB;
		ret = tempA + tempB + 2;
		return ret;
	}
	
	public static void main(String args[]) {
		System.out.println(func(1, 4));
	}
}