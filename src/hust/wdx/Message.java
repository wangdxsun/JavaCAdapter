package hust.wdx;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务消息载体，依据message.sync
 * 消息驱动接口
 * @author lenovo
 * 
 */
public class Message {
	
	public Message(){
		this.sync = count.incrementAndGet();
	}
	
	public Message(String str){
		if(!"".equals(str)){
			String[] retMsg = str.split("\\<\\?\\>");
			this.sync = Long.valueOf(retMsg[0]);
			this.path = retMsg[1];
			this.dpath = retMsg[2];
			this.key = retMsg[3];
			this.operType = Integer.valueOf(retMsg[4]);
			this.status = Integer.valueOf(retMsg[5]);
		} else {
			this.sync = 0l;
		}
		
	}
	
	//AtomicLong
	public static AtomicLong count = new AtomicLong(0);
	
	/*
	 * 序列号 任务唯一标识 从1开始自增
	 */
	private final long sync;
	
	/*
	 * 源文件路径
	 */
	private String path;
	
	/*
	 * 目的文件路径
	 */
	private String dpath;
	
	/*
	 * 加密秘钥 注意和k-v中k区分
	 */
	private String key;
	
	/*
	 * 操作类型 如DES AES 用常量标识
	 */
	private int operType;
	
	/*
	 * 任务状态  2 加密完成 ；1 已被处理；0 新建，未被处理
	 * 1 作为容错机制使用
	 */
	private int status;

	public long getSync() {
		return sync;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getOperType() {
		return operType;
	}

	public void setOperType(int operType) {
		this.operType = operType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDpath() {
		return dpath;
	}

	public void setDpath(String dpath) {
		this.dpath = dpath;
	}
	
	public String toString(){
		return this.sync +"<?>" + this.path +
				"<?>" + this.dpath + "<?>" + this.key +
				"<?>" + this.operType + "<?>" + this.status;
	}
	
	public String toStringWithTips(){
		return "sync:" + this.sync +"<?>path:" + this.path +
				"<?>dpath:" +	this.dpath + "<?>key:" + this.key +
				"<?>operType:" + this.operType + "<?>status:" + this.status;
	}

}
