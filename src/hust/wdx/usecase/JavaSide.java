package hust.wdx.usecase;

import hust.wdx.JedisUtil;

public class JavaSide {

	static JedisUtil util = new JedisUtil();
	static Long start;

	/**
	 * 测试Java侧单线程发送100000条消息用时
	 * @param args
	 */
	public static void main(String[] args) {
		// 任务消息为：
		// [sync]<?>G:\wdx\test.txt<?>G:\wdx\ABC-result-test.txt<?>5<?>3<?>0
		Long start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			util.sendTaskRequest("G:\\wdx\\test.txt", "5", 3);
			System.out.println("1");
		}
		Long end = System.currentTimeMillis();
		System.out.println(end - start);
	}

	/**
	 * 测试Java侧多线程总共发送100000条消息用时
	 * @param args
	 */
	public static void main2(String[] args) {
		// 任务消息为：
		// [sync]<?>G:\wdx\test.txt<?>G:\wdx\ABC-result-test.txt<?>5<?>3<?>0

		Thread[] threads = new Thread[100];
		for (int i = 0; i < 100; i++) {
			threads[i] = new Thread(new Task());
		}
		start = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			threads[i].start();
		}
	}
	
	/**
	 * 测试Java侧接收100000条消息用时
	 * @param args
	 */
	public static void main8(String[] args) {
//		for (long i = 0; i < 100000; i++) {
//			util.sendResult("G:\\wdx\\test.txt", "5", 3);
//		}
		// 结果消息为：
		// [sync]<?>G:\wdx\test.txt<?>G:\wdx\ABC-result-test.txt<?>5<?>3<?>2
		Long start = System.currentTimeMillis();
		for (long i = 1; i <= 100000; i++) {
			util.getTaskResultByIndex(i);
		}
		Long end = System.currentTimeMillis();
		System.out.println(end - start);
	}

}

class Task implements Runnable {
	static JedisUtil util = new JedisUtil();

	@Override
	public void run() {
		for (int i = 0; i < 1000; i++) {
//			System.out.print(Thread.currentThread().getName() + " ");
			util.sendTaskRequest("G:\\wdx\\test.txt", "5", 3);
		}
		System.currentTimeMillis();
		Long end = System.currentTimeMillis();
		System.out.println(end - JavaSide.start);
	}
}
