package co.aurasphere.revolver.utils;

import java.util.List;

public class StringUtils {
	
	public static String firstCharUppercase(String string) {
		return string.substring(0, 1).toUpperCase()
				+ string.substring(1, string.length());
	}
	
	public static String stringListToString(List<String> stringList) {
		if (stringList == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for(String s : stringList){
			builder.append(s).append(", ");
		}
		// Removes the last comma.
		int len = builder.length();
		if (len > 0) {
			builder.delete(len - 2, len);
		}
		return builder.toString();
	}

}
