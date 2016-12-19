package hust.wdx;

import redis.clients.jedis.Jedis;
/**
 * JavaCAdapter Connection
 * @author lenovo
 */
public final class CopyOfJedisUtil{
	private static Jedis jedis; 
	static {
		//连接redis服务器，ip:6379
        jedis = new Jedis("192.168.206.130", 6379);
        jedis.auth("yang");//权限认证
	}
	
	/** 
	 * Java侧接口函数:发送任务请求
	 * @param path 源文件路径
	 * @param key 加密秘钥
	 * @param operType 操作类型
	 * @return 任务序列号
	 */
	public long sendTaskRequest(String path,String key,int operType){
		Message message = new Message();
		message.setPath(path);
		message.setKey(key);
		message.setDpath(generateDpath(path,operType));
		message.setOperType(operType);
		message.setStatus(0);
		lpush("task",message);
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
		if(jedis.hexists("result", String.valueOf(sync))){
			String tempStr = jedis.hget("result", String.valueOf(sync));
			//从缓冲区移除
			jedis.hdel("result", String.valueOf(sync));
			if(tempStr != null && !"".equals(tempStr)){
				Message message = new Message(tempStr);
				return message.getDpath();
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
		System.out.println(key + ":" + jedis.lindex(key,0));//返回名称为key的list中最后一个元素);
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
	
}