package hust.wdx.thread;

public class TestTry {

	public static void main(String[] args) {
		System.out.println(print());

	}
	public static int print(){
		int i = 0;
		try {
			i++;
			return i;
		} catch (Exception e) {
		} finally{
			i++;
			System.out.println("iiii");
		}
		return i;
	}

}
