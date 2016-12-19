package hust.wdx;

public enum OperType {
	COPY(0,"COPY"),
	AES(1,"AES"),
	DES(2,"DES"),
	ABC(3,"ABC");
	
	Integer code;
	String msg;
	OperType(Integer code,String msg){
		this.code = code;
		this.msg = msg;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

}
