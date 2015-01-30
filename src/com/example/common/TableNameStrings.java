package com.example.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.bean.IListTable;

public class TableNameStrings {

	public static String[] tablenames = new String[] { "MaterialInfo",
			"StockDetail", "ProjectInfo", "ProviderInfo", "BranchCompanyInfo",
			"DepartmentInfo", "Warehouse", "MaterialModelInfo", "CleverInfo",
			"AssetDetail", "UserInfo", "SpecificationsInfo",
			"AccountCompanyInfo" };

	public static List<IListTable> getAssetTables() {
		List<IListTable> ilist = new ArrayList<IListTable>();
		// 资产入库
		IListTable assetin = new IListTable(1);
		assetin.main = "AssetInInfo";
		assetin.detail = "AssetInDetail";
		ilist.add(assetin);
		// 资产出库
		IListTable assetout = new IListTable(2);
		assetout.main = "AssetOutInfo";
		assetout.detail = "AssetOutDetail";
		ilist.add(assetout);
		// 资产归还
		IListTable assetreturn = new IListTable(3);
		assetreturn.main = "AssetReturnInfo";
		assetreturn.detail = "AssetReturnDetail";
		ilist.add(assetreturn);

		// 资产调拨
		IListTable assetdiaobo = new IListTable(4);
		assetdiaobo.main = "AssetAllocateInfo";
		assetdiaobo.detail = "AssetAllocateDetail";
		ilist.add(assetdiaobo);

		// 资产保废报亭
		IListTable assetscrapstop = new IListTable(5);
		assetscrapstop.main = "AssetScrapStopInfo";
		assetscrapstop.detail = "AssetScrapStopDetail";
		ilist.add(assetscrapstop);

		// 资产在库
		IListTable assetdetail = new IListTable(6);
		assetdetail.main = "AssetDetail";
		assetdetail.detail = "";
		ilist.add(assetdetail);

		// 资产生命周期
		IListTable assetcycle = new IListTable(7);
		assetcycle.main = "AssetLifecycleInfo";
		assetcycle.detail = "";
		ilist.add(assetcycle);

		// 资产盘点
		IListTable assetinventory = new IListTable(8);
		assetinventory.main = "AssetCheckInfo";
		assetinventory.detail = "AssetCheckDetail";
		ilist.add(assetinventory);
		return ilist;
	}

	public static List<IListTable> getStockTables() {
		List<IListTable> ilist = new ArrayList<IListTable>();
		// 仓储入库
		IListTable stockin = new IListTable(1);
		stockin.main = "StockInInfo";
		stockin.detail = "StockInDetail";
		ilist.add(stockin);
		// 仓储出库
		IListTable stockout = new IListTable(2);
		stockout.main = "StockOutInfo";
		stockout.detail = "StockOutDetail";
		ilist.add(stockout);
		// 仓储退货
		IListTable assetback = new IListTable(3);
		assetback.main = "StockReturnInfo";
		assetback.detail = "StockReturnDetail";
		ilist.add(assetback);
		// 仓储调拨
		IListTable stockdiaobo = new IListTable(4);
		stockdiaobo.main = "StockAllocateInfo";
		stockdiaobo.detail = "StockAllocateDetail";
		ilist.add(stockdiaobo);
		// 仓储在库
		IListTable stockdetail = new IListTable(5);
		stockdetail.main = "StockDetail";
		stockdetail.detail = "";
		ilist.add(stockdetail);
		// 仓储盘点
		IListTable stockinventory = new IListTable(6);
		stockinventory.main = "StockCheckInfo";
		stockinventory.detail = "StockCheckDetail";
		ilist.add(stockinventory);

		return ilist;
	}
}
