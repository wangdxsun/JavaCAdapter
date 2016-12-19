package hust.wdx.sample;

import hust.wdx.JedisUtil;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * 请求任务
 * @author lenovo
 *
 */
public class RequestTask implements Callable<Long>{

	protected final Logger logger = Logger.getLogger("sample.RequeTask");
	JedisUtil util = new JedisUtil();
	Integer count;
	
	/**
	 * 初始化多线程任务
	 * @param util 接口工具类
	 * @param count 备用计数
	 */
	public void init(JedisUtil util,Integer count){
		this.util = util;
		this.count = count;
	}
	
	@Override
	public Long call() throws Exception {
		Long sync = util.sendTaskRequest("G:\\wdx\\hello" + count + ".txt", "234", count);
		System.out.println("put task " + sync + " success!");
		logger.info("put task " + sync + " success!");
		return sync;
	}

}
