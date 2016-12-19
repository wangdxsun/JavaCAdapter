package hust.wdx;

import redis.clients.jedis.Jedis;
/**
 * JavaCAdapter Connection
 * @author wangdxsun
 * Jedis is Non thread safe,
 * but I use synchronized block jedis object to guarantee
 * listen for result thread safe.
 * line 69
 */
public final class JedisUtil{
	
//	JedisPoolUtil jedisPool = new JedisPoolUtil("192.168.206.130", 6379);
	
	public JedisUtil(){
		jedis = new Jedis("192.168.206.130", 6379);
        jedis.auth("yang");//权限认证
	}
	
	private Jedis jedis; 
	
	/** 
	 * Java侧接口函数:发送任务请求
	 * @param path 源文件路径
	 * @param key 加密秘钥
	 * @param operType 操作类型
	 * @return 任务序列号
	 */
	public synchronized long sendTaskRequest(String path,String key,int operType){
		Message message = new Message();
		message.setPath(path);
		message.setKey(key);
		message.setDpath(generateDpath(path,operType));
		message.setOperType(operType);
		message.setStatus(0);
		lpush("task",message);
		//TODO 根据operType放入不同Redis List
		return message.getSync();
	}
	
	/** 
	 * Java侧接口函数:发送高优先级任务请求
	 * @param path 源文件路径
	 * @param key 加密秘钥
	 * @param operType 操作类型
	 * @return 任务序列号
	 */
	public long sendTaskPriority(String path,String key,int operType){
		Message message = new Message();
		message.setPath(path);
		message.setKey(key);
		message.setDpath(generateDpath(path,operType));
		message.setOperType(operType);
		message.setStatus(0);
		rpush("task",message);
		//TODO 根据operType放入不同Redis List
		return message.getSync();
	}
	
	/** 
	 * Java侧接口函数：hash查找获取任务结果
	 * 若执行成功，返回目的路径
	 * @param sync 任务序列号
	 * @return 下载路径
	 */
	public String getTaskResultByIndex(Long sync){
		//结果队列采用Hash形式，查找更快
		synchronized (jedis) {
			if(jedis.hexists("result", String.valueOf(sync))){
				String tempStr = jedis.hget("result", String.valueOf(sync));
				//从缓冲区移除
				jedis.hdel("result", String.valueOf(sync));
				if(tempStr != null && !"".equals(tempStr)){
					Message message = new Message(tempStr);
					return message.getDpath();
				}
			}
		}
		return "";
	}
	
	/** 
	 * Java侧接口函数：获取任务结果
	 * 若执行成功，返回目的路径
	 * @param sync 任务序列号
	 * @return 下载路径
	 */
	public String getTaskResultByPop(Long sync){
		if(jedis.llen("result") > 0){
			String tempStr = jedis.lpop("result");
			//和自己的入队列序号比较，如果匹配，且状态为已完成，弹出list并返回对出，否则继续加入c队列
			String[] retMsg = tempStr.split("\\<\\?\\>");
			if(Long.valueOf(retMsg[0])== sync){
				//&& "2".equals(retMsg[retMsg.length-1])
				//testcase:rpush result 10<?>G:\wdx\hello9.txt<?>G:\wdx\9-result-hello9.txt<?>234<?>9<?>0
				//检测到加密完成从缓冲区移除
				String dpath = retMsg[2];//结果文件路径
				return dpath;
			} else {
				jedis.rpush("result", tempStr);
			}
		}
		return "";
	}
	
	/** 
	 * 将message写入名为producer消息队列头
	 * @param message 任务的消息载体
	 */
	private void lpush(String key,Message message){
		if(message != null)
			jedis.lpush(key,message.toString());
//		System.out.println(key + ":" + jedis.lindex(key,0));//返回名称为key的list中最后一个元素);
	}
	
	/** 
	 * 将message写入名为producer消息队列尾
	 * @param message 任务的消息载体
	 */
	private void rpush(String key,Message message){
		if(message != null)
			jedis.lpush(key,message.toString());
		System.out.println(key + ":" + jedis.lindex(key,0));//返回名称为key的list中最后一个元素);
	}
	
	private String generateDpath(String path,int operType){
		String[] items = path.split("\\\\");
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<items.length-1;i++){
			sb.append(items[i]);
			sb.append("\\");
		}
		sb.append(operType + "-result-");
		sb.append(items[items.length-1]);
		return sb.substring(0,sb.length());
	}
	
	public void test(Message message){
		jedis.exists("key");//是否存在某个key
		jedis.rpush("result", message.toString());
	}
	
	//only for test
	public void sendResult(String path,String key,int operType){
		Message message = new Message();
		message.setPath(path);
		message.setKey(key);
		message.setDpath(generateDpath(path,operType));
		message.setOperType(operType);
		message.setStatus(0);
		hset("result",String.valueOf(message.getSync()),message);
	}
	
	//only for test
	private void hset(String key,String field,Message message){
		if(message != null)
			jedis.hset(key,field,message.toString());
//		System.out.println(key + ":" + jedis.hget(key, field));//返回名称为key的list中最后一个元素);
	}
	
}