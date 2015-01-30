package com.example.bean;

public class AssetLifecycleInfo {
	
	public String Key;
	public String AssetOperatingID;
	public String OperatingType;
	public String MaterialID;
	public String SpecificationsID;
	public String OperatingDate;
	public int Number;
	public String BarCode;
	public String CreateOperater;
	public String UpdateOperater;
	public String UpdateDateTime;
	public String CreateDateTime;
	public String getAssetOperatingID() {
		return AssetOperatingID;
	}
	public void setAssetOperatingID(String assetOperatingID) {
		AssetOperatingID = assetOperatingID;
	}
	public String getOperatingType() {
		return OperatingType;
	}
	public void setOperatingType(String operatingType) {
		OperatingType = operatingType;
	}
	public String getMaterialID() {
		return MaterialID;
	}
	public void setMaterialID(String materialID) {
		MaterialID = materialID;
	}
	public String getSpecificationsID() {
		return SpecificationsID;
	}
	public void setSpecificationsID(String specificationsID) {
		SpecificationsID = specificationsID;
	}
	public String getOperatingDate() {
		return OperatingDate;
	}
	public void setOperatingDate(String operatingDate) {
		OperatingDate = operatingDate;
	}
	public int getNumber() {
		return Number;
	}
	public void setNumber(int number) {
		Number = number;
	}
	public String getUpdateOperater() {
		return UpdateOperater;
	}
	public void setUpdateOperater(String updateOperater) {
		UpdateOperater = updateOperater;
	}
	public String getCreateDateTime() {
		return CreateDateTime;
	}
	public void setCreateDateTime(String createDateTime) {
		CreateDateTime = createDateTime;
	}
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getCreateOperater() {
		return CreateOperater;
	}
	public void setCreateOperater(String createOperater) {
		CreateOperater = createOperater;
	}
	public String getBarCode() {
		return BarCode;
	}
	public void setBarCode(String barCode) {
		BarCode = barCode;
	}
	
	public String getUpdateDateTime() {
		return UpdateDateTime;
	}
	public void setUpdateDateTime(String updateDateTime) {
		UpdateDateTime = updateDateTime;
	}
	@Override
	public String toString() {
		// 为什么要重写toString()呢？因为适配器在显示数据的时候，如果传入适配器的对象不是字符串的情况下，直接就使用对象.toString()
		// TODO Auto-generated method stub
		return BarCode;
	}
}
