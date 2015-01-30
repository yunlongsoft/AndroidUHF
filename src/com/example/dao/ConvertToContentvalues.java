package com.example.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.bean.AccountCompanyInfo;
import com.example.bean.AssetDetail;
import com.example.bean.AssetLifecycleInfo;
import com.example.bean.BranchCompanyInfo;
import com.example.bean.CleverInfo;
import com.example.bean.DepartmentInfo;
import com.example.bean.MaterialInfo;
import com.example.bean.MaterialModelInfo;
import com.example.bean.ProjectInfo;
import com.example.bean.ProviderInfo;
import com.example.bean.SpecificationsInfo;
import com.example.bean.StockDetail;
import com.example.bean.UserInfo;
import com.example.bean.Warehouse;
import com.example.common.TableNameStrings;
import com.google.gson.Gson;

import android.content.ContentValues;

public class ConvertToContentvalues {

	public static ContentValues convertToClass(JSONObject json,String table) throws JSONException{
		ContentValues contentvalues=new ContentValues();
		Gson gon=new Gson();
		  SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < TableNameStrings.tablenames.length; i++) {
				if (TableNameStrings.tablenames[i]==table) {
					switch(i){
					case 0:
						if(TableNameStrings.tablenames[i]=="MaterialInfo"){
							MaterialInfo materialInfo=new MaterialInfo();
						    materialInfo=gon.fromJson(json.toString(), MaterialInfo.class);
						    contentvalues.put("Key", materialInfo.Key);
						    contentvalues.put("Name", materialInfo.Name);
							long time=Long.parseLong(materialInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("MaterialModelID", materialInfo.MaterialModelID);
						    contentvalues.put("Price", materialInfo.Price);
						    contentvalues.put("ProviderID", materialInfo.ProviderID);
						    contentvalues.put("CreateOperater", materialInfo.CreateOperater);
						}
						break;
					case 1:
						if(TableNameStrings.tablenames[i]=="StockDetail"){
							StockDetail stockDetail=new StockDetail();
							stockDetail=gon.fromJson(json.toString(), StockDetail.class);
						    contentvalues.put("Key", stockDetail.Key);
						    contentvalues.put("BarCode", stockDetail.BarCode);
						    contentvalues.put("MaterialID", stockDetail.MaterialID);
						    contentvalues.put("WarehouseID", stockDetail.WarehouseID);
						    contentvalues.put("OnQuantity", stockDetail.OnQuantity);
						    contentvalues.put("SpecificationsID", stockDetail.SpecificationsID);
						    contentvalues.put("CreateOperater", stockDetail.CreateOperater);
						    contentvalues.put("UpdateOperater", stockDetail.UpdateOperater);
						    long time=Long.parseLong(stockDetail.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("StockType", stockDetail.StockType);
						}
						break;
					case 2:
						if(TableNameStrings.tablenames[i]=="ProjectInfo"){
							ProjectInfo projectInfo=new ProjectInfo();
							projectInfo=gon.fromJson(json.toString(), ProjectInfo.class);
						    contentvalues.put("Key", projectInfo.Key);
						    contentvalues.put("Name", projectInfo.Name);
						    contentvalues.put("District", projectInfo.District);
						    long time=Long.parseLong(projectInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", projectInfo.CreateOperater);
						}
						break;
					case 3:
						if(TableNameStrings.tablenames[i]=="ProviderInfo"){
							ProviderInfo providerInfo=new ProviderInfo();
							providerInfo=gon.fromJson(json.toString(), ProviderInfo.class);
						    contentvalues.put("Key", providerInfo.Key);
						    contentvalues.put("Name", providerInfo.Name);
						    contentvalues.put("Type", providerInfo.Type);
						    long time=Long.parseLong(providerInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", providerInfo.CreateOperater);
						}
						break;
					case 4:
						if(TableNameStrings.tablenames[i]=="BranchCompanyInfo"){
							BranchCompanyInfo branchCompanyInfo=new BranchCompanyInfo();
							branchCompanyInfo=gon.fromJson(json.toString(), BranchCompanyInfo.class);
						    contentvalues.put("Key", branchCompanyInfo.Key);
						    contentvalues.put("Name", branchCompanyInfo.Name);
						    long time=Long.parseLong(branchCompanyInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", branchCompanyInfo.CreateOperater);
						}
						break;
					case 5:
						if(TableNameStrings.tablenames[i]=="DepartmentInfo"){
							DepartmentInfo departmentInfo=new DepartmentInfo();
							departmentInfo=gon.fromJson(json.toString(), DepartmentInfo.class);
						    contentvalues.put("Key", departmentInfo.Key);
						    contentvalues.put("Name", departmentInfo.Name);
						    contentvalues.put("CompanyID", departmentInfo.CompanyID);
						    long time=Long.parseLong(departmentInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", departmentInfo.CreateOperater);
						}
						break;
					case 6:
						if(TableNameStrings.tablenames[i]=="Warehouse"){
							Warehouse warehouse=new Warehouse();
							warehouse=gon.fromJson(json.toString(), Warehouse.class);
						    contentvalues.put("Key", warehouse.Key);
						    contentvalues.put("Name", warehouse.Name);
						    long time=Long.parseLong(warehouse.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", warehouse.CreateOperater);
						}
						break;
					case 7:
						if(TableNameStrings.tablenames[i]=="MaterialModelInfo"){
							MaterialModelInfo materialModelInfo=new MaterialModelInfo();
							materialModelInfo=gon.fromJson(json.toString(), MaterialModelInfo.class);
						    contentvalues.put("Key", materialModelInfo.Key);
						    contentvalues.put("Name", materialModelInfo.Name);
						    contentvalues.put("ParentID", materialModelInfo.ParentID);
						    long time=Long.parseLong(materialModelInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", materialModelInfo.CreateOperater);
						}
						break;
					case 8:
						if(TableNameStrings.tablenames[i]=="CleverInfo"){
							CleverInfo cleverInfo=new CleverInfo();
							cleverInfo=gon.fromJson(json.toString(), CleverInfo.class);
						    contentvalues.put("Key", cleverInfo.Key);
						    contentvalues.put("Name", cleverInfo.Name);
						    contentvalues.put("DepatmentID", cleverInfo.DepatmentID);
						    long time=Long.parseLong(cleverInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", cleverInfo.CreateOperater);
						}
						break;
					case 9:
						if(TableNameStrings.tablenames[i]=="AssetDetail"){
							AssetDetail assetDetail=new AssetDetail();
							assetDetail=gon.fromJson(json.toString(), AssetDetail.class);
						    contentvalues.put("Key", assetDetail.Key);
						    contentvalues.put("BarCode", assetDetail.BarCode);
						    contentvalues.put("BelongID", assetDetail.BelongID);
						    contentvalues.put("CreateOperater", assetDetail.CreateOperater);
						    contentvalues.put("MaterialID", assetDetail.MaterialID);
						    contentvalues.put("UpdateOperater", assetDetail.UpdateOperater);
						    contentvalues.put("AsseetType", assetDetail.AsseetType);
						    contentvalues.put("BelongType", assetDetail.BelongType);
						    contentvalues.put("Quantity", assetDetail.Quantity);
						    contentvalues.put("AssetState", assetDetail.AssetState);
						    contentvalues.put("SpecificationsID", assetDetail.SpecificationsID);
						    long time=Long.parseLong(assetDetail.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						}
						break;
					case 10:
						if(TableNameStrings.tablenames[i]=="UserInfo"){
							UserInfo userInfo=new UserInfo();
							userInfo=gon.fromJson(json.toString(), UserInfo.class);
						    contentvalues.put("Key", userInfo.Key);
						    contentvalues.put("LoginID", userInfo.LoginID);
						    contentvalues.put("LoginPwd", userInfo.LoginPwd);
						    contentvalues.put("UserType", userInfo.UserType);
						    long time=Long.parseLong(userInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						}
						break;
					case 11:
						if(TableNameStrings.tablenames[i]=="SpecificationsInfo"){
							SpecificationsInfo specificationInfo=new SpecificationsInfo();
							specificationInfo=gon.fromJson(json.toString(), SpecificationsInfo.class);
						    contentvalues.put("Key", specificationInfo.Key);
						    contentvalues.put("MaterialID", specificationInfo.MaterialID);
						    contentvalues.put("Name", specificationInfo.Name);
						    contentvalues.put("Unit", specificationInfo.Unit);
						    long time=Long.parseLong(specificationInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", specificationInfo.CreateOperater);
						}
						break;
					case 12:
						if(TableNameStrings.tablenames[i]=="AccountCompanyInfo"){
							AccountCompanyInfo accountCompanyInfo=new AccountCompanyInfo();
							accountCompanyInfo=gon.fromJson(json.toString(), AccountCompanyInfo.class);
						    contentvalues.put("Key", accountCompanyInfo.Key);
						    contentvalues.put("Name", accountCompanyInfo.Name);
						    long time=Long.parseLong(accountCompanyInfo.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(time));
						    contentvalues.put("CreateOperater", accountCompanyInfo.CreateOperater);
						}
						break;
					/*case 13:
						if(TableNameStrings.tablenames[i]=="AssetLifecycleInfo"){
							AssetLifecycleInfo assetlife=new AssetLifecycleInfo();
							assetlife=gon.fromJson(json.toString(), AssetLifecycleInfo.class);
						    contentvalues.put("Key", assetlife.Key);
						    contentvalues.put("AssetOperatingID", assetlife.AssetOperatingID);
						    contentvalues.put("BarCode", assetlife.BarCode);
						    contentvalues.put("OperatingType", assetlife.OperatingType);
						    contentvalues.put("MaterialID", assetlife.MaterialID);
						    contentvalues.put("SpecificationsID", assetlife.SpecificationsID);
						    contentvalues.put("OperatingDate", assetlife.OperatingDate);
						    contentvalues.put("Number", assetlife.Number);
						    long updatetime=Long.parseLong(assetlife.UpdateDateTime.substring(6, 19));
						    contentvalues.put("UpdateDateTime",format.format(updatetime));
						    contentvalues.put("CreateOperater", assetlife.CreateOperater);
						    contentvalues.put("UpdateOperater", assetlife.UpdateOperater);
						    long createdatetime=Long.parseLong(assetlife.CreateDateTime.substring(6, 19));
						    contentvalues.put("CreateDateTime", format.format(createdatetime));
						}
						break;*/
					}
				}
		}
		
		return contentvalues;
	}
	
}
