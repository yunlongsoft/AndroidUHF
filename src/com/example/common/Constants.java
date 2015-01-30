package com.example.common;

public class Constants {
	/*这些指令code是用户自己约定的*/
	public static final int CMD_SET_BAUD = 1001;//设置波特率
	public static final int CMD_ISO18000_6C_INVENTORY = 1002;//6c盘存
	public static final int CMD_ISO18000_6C_READ = 1003;//6c读标签
	public static final int CMD_ISO18000_6C_WRITE = 1004;//6C写标签
	public static final int CMD_ISO18000_6C_LOCK = 1005;//锁定标签
	public static final int CMD_ISO18000_6C_KILL = 1006;//销毁标签
	public static String ip="" ;//销毁标签
	public static String port="" ;//销毁标签
}
