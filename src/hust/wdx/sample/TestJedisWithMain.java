package hust.wdx.sample;
import hust.wdx.JedisUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class TestJedisWithMain {
	static int count;
	static JedisUtil util = new JedisUtil();
	private static final ExecutorService exec = Executors.newCachedThreadPool();
	private static RequestTask requestTask;
	private static ResultTask resultTask;
	
	public static void main(String[] args) {
		for(int i=0;i<10;i++){
//		    Runnable business = new Runnable(){
//		      public void run(){
		    	  requestTask = new RequestTask();
					//初始化任务
					requestTask.init(util,count++);
					//分配线程执行有返回值的task
					Future<Long> future = exec.submit(requestTask);
					Long sync;
					resultTask = new ResultTask();
					Future<String> strFuture = new FutureTask<String>(resultTask);
					try {
						sync = future.get();//get message sync
						
						resultTask.init(util, sync);
						exec.submit(resultTask).get();
//						strFuture = exec.submit(resultTask);
//						String downloadPath = strFuture.get();
//						System.out.println("Main Thread say task[" + sync + "] success,download path : " + downloadPath + "!!!");
					} catch (InterruptedException e) {
						strFuture.cancel(true);
					} catch (ExecutionException e) {
						strFuture.cancel(true);
					}
		      }
//		    };
//		    exec.execute(business);
//		  }
		
	}
	
//	public void testGetResult() {
//		for(int i=0;i<10;i++){
//			task = new RequestTask();
//			task.init(util,count++);
//			exec.submit(task);
//		}
//	}
}


