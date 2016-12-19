package hust.wdx.sample;
import hust.wdx.JedisUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
public class TestJedis {
	static int count;
	JedisUtil util = new JedisUtil();
	private static final ExecutorService exec = Executors.newCachedThreadPool();
	private RequestTask task ;
	@Test
	public void testRequest() {
		for(int i=0;i<3;i++){
			task = new RequestTask();
			//初始化任务
			task.init(util,count++);
			//分配线程执行有返回值的task
			Future<Long> future = exec.submit(task);
			Long sync;
			if(future.isDone()){
				try {
					sync = future.get();//get message sync
					String downloadPath = util.getTaskResultByPop(sync);
					System.out.println("sync:" + sync + " operate over success download path is " + downloadPath + "!!!");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	public void testGetResult() {
//		for(int i=0;i<10;i++){
//			task = new RequestTask();
//			task.init(util,count++);
//			exec.submit(task);
//		}
//	}
}
