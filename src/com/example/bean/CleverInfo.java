package com.example.bean;

public class CleverInfo {
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String Key;
	public String Name;
	public String DepatmentID;
	public String getDepatmentID() {
		return DepatmentID;
	}
	public void setDepatmentID(String depatmentID) {
		DepatmentID = depatmentID;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String UpdateDateTime;
	public String getUpdateDateTime() {
		return UpdateDateTime;
	}
	public void setUpdateDateTime(String updateDateTime) {
		UpdateDateTime = updateDateTime;
	}
	public String CreateOperater;
	public String getCreateOperater() {
		return CreateOperater;
	}
	public void setCreateOperater(String createOperater) {
		CreateOperater = createOperater;
	}
	@Override
	public String toString() {
		// 为什么要重写toString()呢？因为适配器在显示数据的时候，如果传入适配器的对象不是字符串的情况下，直接就使用对象.toString()
		// TODO Auto-generated method stub
		return Name;
	}
}
