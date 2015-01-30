package com.example.common;

public class IsEmpty {
	// 判断从EditText获取的值是否为空
	public static String getIsEmpty(String gettext) {
		if (gettext != null) {
			return gettext;
		}
		return "";
	}

}
