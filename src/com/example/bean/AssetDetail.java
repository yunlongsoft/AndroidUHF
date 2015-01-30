package com.example.bean;

public class AssetDetail {
	public String Key;
	public String MaterialID; 
	public String BarCode; 
	public int AsseetType;
	public double Quantity;
	public int AssetState;
	public String SpecificationsID;
	public int BelongType;
	public String BelongID; 
	public String CreateOperater; 
	public String UpdateOperater;
	public String UpdateDateTime;
	public String getUpdateDateTime() {
		return UpdateDateTime;
	}
	public void setUpdateDateTime(String updateDateTime) {
		UpdateDateTime = updateDateTime;
	}
	public String getKey() {
		return Key;
	}
	public void setKey(String key) {
		Key = key;
	}
	public String getMaterialID() {
		return MaterialID;
	}
	public void setMaterialID(String materialID) {
		MaterialID = materialID;
	}
	public String getBarCode() {
		return BarCode;
	}
	public void setBarCode(String barCode) {
		BarCode = barCode;
	}
	public int getAsseetType() {
		return AsseetType;
	}
	public void setAsseetType(int asseetType) {
		AsseetType = asseetType;
	}
	public double getQuantity() {
		return Quantity;
	}
	public void setQuantity(double quantity) {
		Quantity = quantity;
	}
	public int getAssetState() {
		return AssetState;
	}
	public void setAssetState(int state) {
		AssetState = state;
	}
	public int getBelongType() {
		return BelongType;
	}
	public void setBelongType(int belongType) {
		BelongType = belongType;
	}
	public String getBelongID() {
		return BelongID;
	}
	public void setBelongID(String belongID) {
		BelongID = belongID;
	}
	public String getCreateOperater() {
		return CreateOperater;
	}
	public void setCreateOperater(String createOperater) {
		CreateOperater = createOperater;
	}
	public String getUpdateOperater() {
		return UpdateOperater;
	}
	public void setUpdateOperater(String updateOperater) {
		UpdateOperater = updateOperater;
	}
	public String getSpecificationsID() {
		return SpecificationsID;
	}
	public void setSpecificationsID(String specificationsID) {
		SpecificationsID = specificationsID;
	}
}
