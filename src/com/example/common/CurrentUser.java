package com.example.common;

public class CurrentUser {

	//声明一个全局的静态变量，用来记录当前登录用户
	public static String CurrentUserGuid="00000000-0000-0000-0000-000000000000";
	//声明一个全局的静态变量，用来记录当前登录用户
		public static int userType=1;//1、管理员。2、普通管理员。3、资产。4、仓储
	/*public static String getCurrentUserGuid() {
		return CurrentUserGuid;
	}

	public static void setCurrentUserGuid(String currentUserGuid) {
		CurrentUserGuid = currentUserGuid;
	}*/
	
}
