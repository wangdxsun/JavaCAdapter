package hust.wdx.sample;

import hust.wdx.JedisUtil;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * 识别结果
 * @author lenovo
 *
 */
public class ResultTask implements Callable<String>{

	protected final Logger logger = Logger.getLogger("sample.ResultTask");
	JedisUtil util = new JedisUtil();
	Long sync;
	
	/**
	 * 初始化多线程任务
	 * @param util 接口工具类
	 * @param count 备用计数
	 */
	public void init(JedisUtil util,Long sync){
		this.util = util;
		this.sync = sync;
	}
	
	@Override
	public String call() throws Exception {
		String downloadPath = util.getTaskResultByPop(sync);
		System.out.println("task[" + sync + "] success,download path : " + downloadPath + "!!!");
		logger.info("task[" + sync + "] success,download path : " + downloadPath + "!!!");
		return downloadPath;
	}

}
