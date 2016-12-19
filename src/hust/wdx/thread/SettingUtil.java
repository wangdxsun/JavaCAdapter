package hust.wdx.thread;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SettingUtil {
	
	private static Properties initProperties(){
		InputStream inputStream;
		Properties prop = new Properties();
		try {
			//获取项目当前运行目录如D:/.../projectName/javacadpter/
			//切割目录拼接为D:/.../projectName/lib/redisPool.properties
			//当前写死，后期考虑要不要采用Pool方案，取舍配置文件
			inputStream = new FileInputStream("E:/workspace/JBridgeSecuritySys/redisPool.properties");
			prop.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	
	public static String getPropertyValue(String param){
		Properties prop = initProperties();
		return prop.getProperty(param);
	}
	
	public static int getPropertyInt(String param){
		Properties prop = initProperties();
		return Integer.valueOf(prop.getProperty(param));
	}
	
	public static boolean getPropertyBoolean(String param){
		Properties prop = initProperties();
		return Boolean.valueOf(prop.getProperty(param));
	}

}
