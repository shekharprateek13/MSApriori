package minsup.msapriori.utils;

import com.google.gson.Gson;

public class GsonUtil {
	
	private static Gson INSTANCE = new Gson();
	
	public static Gson getGson(){
		if(GsonUtil.INSTANCE ==  null){
			INSTANCE = new Gson();
		}
		return INSTANCE;
	}
}










