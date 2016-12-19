package hust.wdx.thread;

import hust.wdx.Message;

import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
 
/**
 * JavaCAdapter Connection JedisPool implementation
 * singleton
 * @author wangdxsun
 * JedisPool is Thread safe.
 * use jedisPool create strong jedis instance
 * get jedis intance before use
 * return back instance after use
 */
public class JedisThreadSafeUtil {
	
    private static JedisThreadSafeUtil instance = null;  
    private JedisThreadSafeUtil(){
    	//if jedisPool null init it else do nothing.
    	if (jedisPool == null) {
            poolInit();
        }
    	jedisPool.getNumActive();
    }  
    public static JedisThreadSafeUtil getInstance(){  
        if(null == instance){  
            instance = new JedisThreadSafeUtil();  
        }  
        return instance;
    }
    
    private static JedisPool jedisPool = null;
     
    protected static Logger logger = Logger.getLogger("thread.RedisUtil");
     
    //Redis服务器IP
    private static String HOST = SettingUtil.getPropertyValue("host");
     
    //Redis的端口号
    private static int PORT = SettingUtil.getPropertyInt("port");
     
    //访问密码
    private static String AUTH = SettingUtil.getPropertyValue("auth");
     
    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static int MAX_ACTIVE = SettingUtil.getPropertyInt("max_active");;
     
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = SettingUtil.getPropertyInt("max_idle");;
     
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static int MAX_WAIT = SettingUtil.getPropertyInt("max_wait");;
 
    //超时时间
    private static int TIMEOUT = SettingUtil.getPropertyInt("timeout");;
     
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = SettingUtil.getPropertyBoolean("test_on_borrow");;
     
    /**
     * redis过期时间,以秒为单位
     */
    public final static int EXRP_HOUR = 60*60;          //一小时
    public final static int EXRP_DAY = 60*60*24;        //一天
    public final static int EXRP_MONTH = 60*60*24*30;   //一个月
     
    /**
     * 初始化Redis连接池
     */
    private static void initialPool(){
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config, HOST.split(",")[0], PORT, TIMEOUT, AUTH);
        } catch (Exception e) {
            logger.warning("First create JedisPool error : "+e);
            try{
                //如果第一个IP异常，则访问第二个IP
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(MAX_ACTIVE);
                config.setMaxIdle(MAX_IDLE);
                config.setMaxWaitMillis(MAX_WAIT);
                config.setTestOnBorrow(TEST_ON_BORROW);
                jedisPool = new JedisPool(config, HOST.split(",")[1],PORT,TIMEOUT,AUTH);
            }catch(Exception e2){
                logger.warning("Second create JedisPool error : "+e2);
            }
        }
    }
     
    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (jedisPool == null) {  
            initialPool();
        }
    }
    
    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null && jedisPool !=null) {
            jedisPool.returnResource(jedis);
        }
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
	//重构hexists hget hdel
	public String getTaskResultByIndex(Long sync){
		System.out.println("loop and listen again!");
		//结果队列采用Hash形式，查找更快
		if(hexists("result", String.valueOf(sync))){
			System.out.println("hi,i am coming~");
			String tempStr = hget("result", String.valueOf(sync));
			//从缓冲区移除
			hdel("result", String.valueOf(sync));
			if(tempStr != null && !"".equals(tempStr)){
				Message message = new Message(tempStr);
				return message.getDpath();
			}
		}
		return "";
	}
	
	private void hdel(String key,String field){
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.hdel(key,field);
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
	}
	
	private String hget(String key,String field){
		Jedis jedis = null;
		String str = "";
		try {
			jedis = jedisPool.getResource();
			str = jedis.hget(key,field);
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return str;
	}
	
	private boolean hexists(String key,String field){
		Jedis jedis = null;
		boolean flag = false;
		try {
			jedis = jedisPool.getResource();
			flag = jedis.hexists(key,field);
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
		return flag;
	}
	
	/** 
	 * 将message写入名为key消息队列头
	 * @param key 消息队列标识
	 * @param message 任务的消息载体
	 */
	private void lpush(String key,Message message){
		if(message == null)
			return;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.lpush(key,message.toString());
//			System.out.println(key + ":" + jedis.lindex(key,0));//返回名称为key的list中最后一个元素);
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
	}
	
	/** 
	 * 将message写入名为key消息队列尾
	 * @param key 消息队列标识
	 * @param message 任务的消息载体
	 */
	private void rpush(String key,Message message){
		if(message == null)
			return;
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.rpush(key,message.toString());
//			System.out.println(key + ":" + jedis.lindex(key,0));//返回名称为key的list中最后一个元素);
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			e.printStackTrace();
		} finally {
			returnResource(jedis);
		}
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