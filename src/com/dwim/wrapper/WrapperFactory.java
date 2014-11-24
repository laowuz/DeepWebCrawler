package com.dwim.wrapper;

import java.util.HashMap;

import org.apache.log4j.*;

/**
 * This factory manage all wrapper
 * You can get a DAO by declaring its nick name which you can see in config file
 * By using this factory, you haven't to consider which DAO is about to using.
 * All of this is managed by the factory itself
 * @author JL
 *
 */
public class WrapperFactory {
	private Logger logger = Logger.getLogger(getClass()); 
	
	private static HashMap <String, String> map = null;
	/**
	 * By invoking this method, obtain a DAO Object
	 * @param name the nick name of the DAO object
	 * @return a DAO suit for your nick name
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws DAOException thrown if any error occurs from dao layer. You can check the cause of this exception
	 */
	public static IWrapper getWrapper(String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(map == null) initializeConfig();
		String className = map.get(name.toLowerCase());
		if(className == null)	return null;
		IWrapper wrapper = (IWrapper) Class.forName(className).newInstance();
		
		return wrapper;
	}


	private static void initializeConfig()  {
		map = new HashMap<String, String>(12);			//load info from config file
		//assume here configration is loaded
		map.put("lazy", "com.dwim.wrapper.LazyWrapper");
		map.put("vod", "com.dwim.wrapper.RegulaExperssionWrapper");
		String nickName,className;
		IWrapper obj;
		/*BufferedReader reader =new BufferedReader(new FileReader("config.txt"));
			nickName = reader.readLine();
			while(nickName != null) {
				className = reader.readLine();
				obj = (IWrapper)Class.forName(className).newInstance();
				map.put(nickName, obj);
				nickName = reader.readLine();
		 */

	}

	void test() {
		//logger.er
	}
	
	public static void main(String args[]) {
		try {
			Class.forName("com.dwim.wrapper.RegulaExperssionWrapper").newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
