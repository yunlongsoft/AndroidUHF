package com.example.bean;

import java.util.Date;

public class StockDetail {

	public String Key;
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
	public double getOnQuantity() {
		return OnQuantity;
	}
	public void setOnQuantity(double onQuantity) {
		OnQuantity = onQuantity;
	}
	public int getStockType() {
		return StockType;
	}
	public void setStockType(int stockType) {
		StockType = stockType;
	}

	public String getWarehouseID() {
		return WarehouseID;
	}
	public void setWarehouseID(String warehouseID) {
		WarehouseID = warehouseID;
	}
	
	public String getBarCode() {
		return BarCode;
	}
	public void setBarCode(String barCode) {
		BarCode = barCode;
	}
   
	public String getUpdateOperater() {
		return UpdateOperater;
	}
	public void setUpdateOperater(String updateOperater) {
		UpdateOperater = updateOperater;
	}
	public String getCreateOperater() {
		return CreateOperater;
	}
	public void setCreateOperater(String createOperater) {
		CreateOperater = createOperater;
	}
	public String getSpecificationsID() {
		return SpecificationsID;
	}
	public void setSpecificationsID(String specificationsID) {
		SpecificationsID = specificationsID;
	}
	public String UpdateDateTime;
	
	public String getUpdateDateTime() {
		return UpdateDateTime;
	}
	public void setUpdateDateTime(String updateDateTime) {
		UpdateDateTime = updateDateTime;
	}
	public String UpdateOperater;
	public String CreateOperater;
	public String BarCode;
	public String MaterialID;
	public double OnQuantity;
	public int StockType;
	public String SpecificationsID;
	public String WarehouseID;
}
