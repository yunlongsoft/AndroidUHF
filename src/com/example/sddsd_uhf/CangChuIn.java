package com.example.sddsd_uhf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
import com.example.bean.AccountCompanyInfo;
import com.example.bean.BranchCompanyInfo;
import com.example.bean.CleverInfo;
import com.example.bean.MaterialInfo;
import com.example.bean.MaterialModelInfo;
import com.example.bean.ProviderInfo;
import com.example.bean.SpecificationsInfo;
import com.example.bean.TID;
import com.example.bean.Warehouse;
import com.example.common.Constants;
import com.example.common.CreateGuid;
import com.example.common.CurrentUser;
import com.example.dao.DBAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class CangChuIn extends Activity implements OnClickListener, OnItemSelectedListener {

	private Spinner materialname;// 产品名称
	private Spinner specificationsid;// 规格型号
	private Spinner materialcategory;// 产品类别
	private AutoCompleteTextView providerid;// 供应商
	private AutoCompleteTextView warhouseid;// 仓库
	private Spinner belongid;// 所属方
	private Spinner purchaseid;// 采购方
	private Spinner stocktype;// 库存类型
	private CheckBox ispayment;
	private String materialnamekey;
	private String specificationsidkey;
	private String materialcategorykey;
	private String provideridkey;
	private String warhouseidkey;
	private String belongidkey;
	private String purchaseidkey;
	private int stocktypekey;
	
	
	private EditText price;// 单价
	private EditText inquantity;// 数量
	//private EditText amount;// 总价
	private EditText invoicenum;// 发票信息
	private EditText remark;// 备注
	private Button scanButton;// 扫描按钮
	private EditText barcode;// 标签号
	private Button addbutton;// 添加到列表按钮
	private Button savebutton;// 保存按钮
	private Button cancelbutton;// 取消

	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	List<ContentValues> listdetail = new ArrayList<ContentValues>(); // 在库明细列表
	List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
	List<String> listdetailstr = new ArrayList<String>(); // 在库明细列表
	private final String activity = "com.example.sddsd_uhf.CangChuIn";
	DBAdapter db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cang_chu_in);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(CangChuIn.this,
				UhfService.class);
		startService(startServer);
		initView();// 初始化控件
		initSoundPool();

		// 注册广播接收者
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(activity);
		registerReceiver(myReceiver, filter);
	}

	private void initView() {
		// TODO Auto-generated method stub
		materialname = (Spinner) findViewById(R.id.materialname);
		specificationsid = (Spinner) findViewById(R.id.specificationsid);
		materialcategory = (Spinner) findViewById(R.id.materialcategory);
		providerid = (AutoCompleteTextView) findViewById(R.id.providerid);
		warhouseid = (AutoCompleteTextView) findViewById(R.id.warhouseid);
		belongid = (Spinner) findViewById(R.id.belongid);
		purchaseid = (Spinner) findViewById(R.id.purchaseid);
		stocktype = (Spinner) findViewById(R.id.stocktype);
		ispayment=(CheckBox)findViewById(R.id.ispayment);
		price = (EditText) findViewById(R.id.price);
		price.setText("0.0");
		inquantity = (EditText) findViewById(R.id.inquantity);
		inquantity.setText("1");
		/*amount = (EditText) findViewById(R.id.amount);
		amount.setText("0.0");*/
		invoicenum = (EditText) findViewById(R.id.invoicenum);
		remark = (EditText) findViewById(R.id.remark);
		barcode = (EditText) findViewById(R.id.barcode);
		
		listViewData = (ListView) findViewById(R.id.data_list);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();
		
		scanButton = (Button) findViewById(R.id.scanButton);
		addbutton = (Button) findViewById(R.id.addbutton);
		savebutton = (Button) findViewById(R.id.savebutton);
		cancelbutton = (Button) findViewById(R.id.cancelbutton);

		materialcategoryDropdown();
		provideridDropdown();
		warhouseidDropdown();
		purchaseidDropdown();
		belongidDropdown();
		
		scanButton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		cancelbutton.setOnClickListener(this);
		
		ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
				.createFromResource(this, R.array.AsseetTypes,
						android.R.layout.simple_spinner_dropdown_item);
		stocktype.setAdapter(adaptertype);
		// 添加事件Spinner事件监听
		// Statespinner.setOnItemSelectedListener(this);
		stocktype.setOnItemSelectedListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
				Intent toService = new Intent(CangChuIn.this, UhfService.class);
				Intent ac = new Intent();
				ac.setAction("com.example.UHFService.UhfService");
				ac.putExtra("activity", activity);
				sendBroadcast(ac);
				cmdCode = 0;
				db.open();
		switch (v.getId()) {
		case R.id.scanButton:
			cmdCode = Constants.CMD_ISO18000_6C_READ;
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.addbutton:
			String barcodetext = barcode.getText().toString().trim();
			if (barcodetext.length() != 0) {
				Cursor cursor = null;
				db.open();
				try {
					// 查询这个标签是否存在
					cursor = db.getTitle("StockDetail",
							new String[] { "OnQuantity" }, "BarCode",
							recvString);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					Log.e("addTidToList", "查询这个标签是否存在" + e.toString());
				}
				
				System.out.println(provideridkey+"你好");
				if (!cursor.moveToFirst()) {// 判断是否为入库
					ContentValues contentvalue = new ContentValues();
					contentvalue.put("Key", CreateGuid.GenerateGUID());
					contentvalue.put("MaterialID", materialnamekey);
					contentvalue.put("BarCode", recvString);
					contentvalue.put("ProviderID", provideridkey);
					double inquantitys=0;
					double prices=0;
					
					//总金额计算出来
					if(inquantity.getText().toString().trim().equals("")){
						
					}else{
						inquantitys=Double.parseDouble(inquantity.getText().toString().trim());
					}
					if(price.getText().toString().trim().equals("")){
						
					}else{
						   prices=Double.parseDouble(price.getText().toString().trim());
					}
					contentvalue.put("Amount",inquantitys*prices);
					contentvalue.put("MaterialModelID", materialcategorykey);
					contentvalue.put("StockType", stocktypekey);
					contentvalue.put("INQuantity", inquantity.getText().toString().trim());
					contentvalue.put("UnitPrice", price.getText().toString().trim());// 创建下拉框
					contentvalue.put("SpecificationsID", specificationsidkey);// 创建规格型号，获取id
					contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
					contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
					if (listIndetail.isEmpty()) {
						listIndetail.add(contentvalue);
						Log.e("read tid", listIndetail.get(0).get("BarCode").toString());
					} else {
					for (int i = 0; i < listIndetail.size(); i++) {
						ContentValues mTID = listIndetail.get(i);
						// list中有此EPC
						if (recvString.equals(mTID.get("BarCode").toString())) {
							break;
						} else if (i == (listIndetail.size() - 1)) {
							// list中没有此epc
							listIndetail.add(contentvalue);
						}
					}
					}
					addListView(listIndetail);//将入库明细添加到列表中
					
					ContentValues contentvaluedetail = new ContentValues();
					contentvaluedetail.put("Key", CreateGuid.GenerateGUID());
					contentvaluedetail.put("BarCode", recvString);
					contentvaluedetail.put("StockType", stocktypekey);
					contentvaluedetail.put("OnQuantity", inquantity.getText().toString().trim());
					contentvaluedetail.put("MaterialID", materialnamekey);
					//contentvaluedetail.put("WarehouseID", warhouseidkey);
					contentvaluedetail.put("SpecificationsID", specificationsidkey);// 创建规格型号，获取id
					contentvaluedetail.put("CreateOperater", CurrentUser.CurrentUserGuid);
					contentvaluedetail.put("UpdateOperater", CurrentUser.CurrentUserGuid);
					// resultInsert= db.insert(contentvalue, "AssetDetail");
					if (listdetail.isEmpty()) {
						listdetail.add(contentvaluedetail);
						Log.e("read tid", listdetail.get(0).get("BarCode").toString());
					} else {
					for (int i = 0; i < listdetail.size(); i++) {
						ContentValues mTID = listdetail.get(i);
						// list中有此EPC
						if (recvString.equals(mTID.get("BarCode").toString())) {
							break;
						} else if (i == (listdetail.size() - 1)) {
							// list中没有此epc
							listdetail.add(contentvaluedetail);
						}
					}
					}
					barcode.setText("");
				}else{
					double onquantity=cursor.getDouble(0);
					ContentValues contentvalue = new ContentValues();
					contentvalue.put("Key", CreateGuid.GenerateGUID());
					contentvalue.put("MaterialID", materialnamekey);
					contentvalue.put("BarCode", recvString);
					contentvalue.put("ProviderID", provideridkey);
					double inquantitys=0;
					double prices=0;
					
					//总金额计算出来
					if(inquantity.getText().toString().trim().equals("")){
						
					}else{
						inquantitys=Double.parseDouble(inquantity.getText().toString().trim());
					}
					if(price.getText().toString().trim().equals("")){
						
					}else{
						   prices=Double.parseDouble(price.getText().toString().trim());
					}
					contentvalue.put("Amount",inquantitys*prices);
					contentvalue.put("MaterialModelID", materialcategorykey);
					contentvalue.put("StockType", stocktypekey);
					contentvalue.put("INQuantity", inquantity.getText().toString().trim());
					contentvalue.put("UnitPrice", price.getText().toString().trim());// 创建下拉框
					contentvalue.put("SpecificationsID", specificationsidkey);// 创建规格型号，获取id
					contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
					contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
					if (listIndetail.isEmpty()) {
						listIndetail.add(contentvalue);
						Log.e("read tid", listIndetail.get(0).get("BarCode").toString());
					} else {
					for (int i = 0; i < listIndetail.size(); i++) {
						ContentValues mTID = listIndetail.get(i);
						// list中有此EPC
						if (recvString.equals(mTID.get("BarCode").toString())) {
							break;
						} else if (i == (listIndetail.size() - 1)) {
							// list中没有此epc
							listIndetail.add(contentvalue);
						}
					}
					}
					addListView(listIndetail);//将入库明细添加到列表中
					String sqlstr="Update StockDetail set OnQuantity="+(onquantity+inquantitys)+" where BarCode='"+recvString+"'";
					listdetailstr.add(sqlstr);
					barcode.setText("");
				}
				cursor.close();// 关闭游标，释放资源
				
			}
			break;
		case R.id.savebutton:
			if (!listIndetail.isEmpty()&&checkEdit()) {
				InsertLocalSQL();// 保存到本地数据库
			}
			break;
		case R.id.cancelbutton:
			listIndetail.removeAll(listIndetail);
			listdetail.removeAll(listdetail);
			listViewData.setAdapter(null);
			break;
		}
		db.close();
	}
	private boolean checkEdit() {
		// TODO Auto-generated method stub
		if (providerid.getText().toString().trim().equals("")) {
			Toast.makeText(CangChuIn.this, "供应商不能为空", Toast.LENGTH_SHORT)
					.show();
		}else{
			if (warhouseid.getText().toString().trim().equals("")) {
				Toast.makeText(CangChuIn.this, "库房不能为空", Toast.LENGTH_SHORT)
				.show();
			}
			return true;
		}
		return false;
	}
	private void InsertLocalSQL() {
		// TODO Auto-generated method stub
		db.open();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 生成入库单号
		SimpleDateFormat formatNumber = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateBatchnum = formatNumber.format(new Date());// 获取当前时间并格式化
		String BuildBatchnum = "CC-RKD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String StockInInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", StockInInfokey);
		content.put("BatchNumber", BuildBatchnum);
		content.put("BelongID", belongidkey);
		content.put("StockInDateTime", date);
		content.put("InvoiceNumber", invoicenum.getText().toString().trim());
		if (warhouseidkey==null) {
			warhouseidkey=((Warehouse) warhouseid.getAdapter().getItem(0)).getKey();
			//Toast.makeText(this, warhouseidkey, Toast.LENGTH_SHORT).show();
		}
		content.put("WarhouseID", warhouseidkey);// 总金额
		content.put("PurchaseID", purchaseidkey);// 采购者
		content.put("IsPayment", ispayment.isChecked()==true?"true":"false");// 是否付款
		content.put("Remark", remark.getText().toString().trim());//备注
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "AssetInInfo");// 插入入库信息数据库
		long resultInsert = 0;
		
		// 遍历插入入库明细表
		for (int i = 0; i < this.listIndetail.size(); i++) {
			// View view = listViewData.getChildAt(i);
			ContentValues contentvalue = listIndetail.get(i);
			contentvalue.put("StockInInfoID", StockInInfokey);
		}

		try {
			
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail, "StockInInfo",
					"StockInDetail");
			if (resultInsert ==listIndetail.size()+1) {
				Toast.makeText(this, "入库成功", Toast.LENGTH_SHORT).show();
				listIndetail.removeAll(listIndetail);
				listViewData.setAdapter(null);
			}else{
				Toast.makeText(this, "入库失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("仓储入库异常");
		}
		
		resultInsert = 0;// 重新归零
		// 遍历插入入库明细表
				for (int i = 0; i < this.listdetail.size(); i++) {
					// View view = listViewData.getChildAt(i);
					ContentValues contentvalue = listdetail.get(i);
					contentvalue.put("WarehouseID", warhouseidkey);
				}
		// 遍历插入在库明细
		try {
			resultInsert = db.insertList(listdetail, "StockDetail");
			if (resultInsert > 0) {
				Toast.makeText(this, "保存到在库成功", Toast.LENGTH_SHORT).show();
				listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "保存到在库失败，请重试", Toast.LENGTH_SHORT).show();
		}
		resultInsert = 0;// 重新归零
		// 遍历插入在库明细
		try {
			resultInsert = db.updateList(listdetailstr);
			if (resultInsert > 0) {
				Toast.makeText(this, "更新在库成功", Toast.LENGTH_SHORT).show();
				listdetailstr.remove(listdetailstr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "更新在库失败，请重试", Toast.LENGTH_SHORT).show();
		}
		db.close();

	}
	@Override
	public void onItemSelected(AdapterView<?> selection, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch(selection.getId()){
		case R.id.stocktype:
			if (arg2 == 0) {
				stocktypekey = 1;
			}
			if (arg2 == 1) {
				stocktypekey = 2;
			}
			break;
		//case R.id.providerid:
			//provideridkey = ((ProviderInfo) providerid.getSelectedItem())
			//		.getKey();
			//break;
		case R.id.materialname:
			materialnamekey = ((MaterialInfo) materialname.getSelectedItem())
					.getKey();
			specificationsidDropdown(materialnamekey);
			break;
		case R.id.belongid:
			belongidkey = ((BranchCompanyInfo) belongid.getSelectedItem())
					.getKey();
			break;
		case R.id.purchaseid:
			purchaseidkey = ((AccountCompanyInfo) purchaseid
					.getSelectedItem()).getKey();
			break;
		case R.id.materialcategory:
			materialcategorykey = ((MaterialModelInfo) materialcategory
					.getSelectedItem()).getKey();
			materialnameDropdown(materialcategorykey);
			break;
		case R.id.specificationsid:
			specificationsidkey = ((SpecificationsInfo) specificationsid
					.getSelectedItem()).getKey();
			break;
		//case R.id.warhouseid:
			//warhouseidkey = ((Warehouse) warhouseid.getAdapter().getItem(0)).getKey();
			//break;
		/*case R.id.newproviderid:
			newproviderkey = ((ProviderInfo) newprovider.getSelectedItem()).getKey();
			break;*/
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	 //先选择产品类别
	public void materialcategoryDropdown() {
		ArrayAdapter<MaterialModelInfo> adapter;
		List<MaterialModelInfo> listName = new ArrayList<MaterialModelInfo>();
		db.open();
		
		//78af83a7-c9bb-4382-bfc4-23b0ac736514
		Cursor cursor = db.getAllTitles("MaterialModelInfo","ParentID","78af83a7-c9bb-4382-bfc4-23b0ac736514","Name");
		if (cursor.moveToFirst()) {
			do {
				MaterialModelInfo item1 = new MaterialModelInfo();
				Cursor cursorsecond = db.getAllTitles("MaterialModelInfo","ParentID",cursor.getString(cursor.getColumnIndex("Key")),"Name");
				if (cursorsecond.moveToFirst()) {
					do{
						MaterialModelInfo item2 = new MaterialModelInfo();
						Cursor cursorlast = db.getAllTitles("MaterialModelInfo","ParentID",cursorsecond.getString(cursorsecond.getColumnIndex("Key")),"Name");
						if(cursorlast.moveToFirst()){
							
							do{
								MaterialModelInfo item3 = new MaterialModelInfo();
								Cursor cursorlast2 = db.getAllTitles("MaterialModelInfo","ParentID",cursorlast.getString(cursorlast.getColumnIndex("Key")),"Name");
								if(cursorlast2.moveToFirst()){
									//Toast.makeText(this, "超出界限", Toast.LENGTH_SHORT).show();
									System.out.println("你好吗，边界超出了");
								}else{
									item3.Key = cursorlast.getString(cursorlast.getColumnIndex("Key"));
									item3.Name = cursorlast.getString(cursorlast.getColumnIndex("Name"));
									listName.add(item3);
								}
								cursorlast2.close();
							}while(cursorlast.moveToNext());
							
						}else{
							item2.Key = cursorsecond.getString(cursorsecond.getColumnIndex("Key"));
							item2.Name = cursorsecond.getString(cursorsecond.getColumnIndex("Name"));
							listName.add(item2);
						}
						cursorlast.close();
					}while(cursorsecond.moveToNext());
				}else{
					item1.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item1.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item1);
				}
				cursorsecond.close();
			} while (cursor.moveToNext());
		}
		cursor.close();
		adapter = new ArrayAdapter<MaterialModelInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		materialcategory.setAdapter(adapter);
		// 添加事件Spinner事件监听
		materialcategory.setOnItemSelectedListener(this);
		// 设置默认值
		materialcategory.setVisibility(View.VISIBLE);
		db.close();
	}
	   //产品
		public void materialnameDropdown(String key) {
			ArrayAdapter<MaterialInfo> adapter;
			List<MaterialInfo> listName = new ArrayList<MaterialInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("MaterialInfo","MaterialModelID",key,"Name");
			if (cursor.moveToFirst()) {
				do {
					MaterialInfo item = new MaterialInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			adapter = new ArrayAdapter<MaterialInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			materialname.setAdapter(adapter);
			// 添加事件Spinner事件监听
			materialname.setOnItemSelectedListener(this);
			// 设置默认值
			materialname.setVisibility(View.VISIBLE);
			db.close();
		}
		 //产品规格型号
		public void provideridDropdown() {
			ArrayAdapter<ProviderInfo> adapter;
			List<ProviderInfo> listName = new ArrayList<ProviderInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("ProviderInfo","Type",1,"Name");
			if (cursor.moveToFirst()) {
				do {
					ProviderInfo item = new ProviderInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			adapter = new ArrayAdapter<ProviderInfo>(this,
					android.R.layout.simple_dropdown_item_1line, listName);
			//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			providerid.setAdapter(adapter);
			//设置当输入一个字符之后就开始检索
			providerid.setThreshold(1);
			// 添加事件Spinner事件监听
			providerid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					provideridkey = ((ProviderInfo) providerid.getAdapter().getItem(arg2))
									.getKey();
					//Toast.makeText(CangChuIn.this,((ProviderInfo)providerid.getAdapter().getItem(arg2)).getKey(),Toast.LENGTH_LONG).show();
				}
			});
			providerid.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasfoucs) {
					// TODO Auto-generated method stub
					AutoCompleteTextView view=(AutoCompleteTextView)v;
					if (hasfoucs) {
						view.showDropDown();
					}
				}
			});
			// 设置默认值
			providerid.setVisibility(View.VISIBLE);
			cursor.close();
			db.close();
		}
		 //产品规格型号warhouseid
		public void specificationsidDropdown(String key) {
			ArrayAdapter<SpecificationsInfo> adapter;
			List<SpecificationsInfo> listName = new ArrayList<SpecificationsInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("SpecificationsInfo","MaterialID",key,"Name");
			if (cursor.moveToFirst()) {
				do {
					SpecificationsInfo item = new SpecificationsInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					//item.MaterialID=cursor.getString(cursor.getColumnIndex("MaterialID"));
					//item.Unit=cursor.getString(cursor.getColumnIndex("Unit"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			if (listName.isEmpty()) {
				SpecificationsInfo item = new SpecificationsInfo();
				item.Key ="00000000-0000-0000-0000-000000000000";
				item.Name = "未知规格";
				listName.add(item);
			}
			adapter = new ArrayAdapter<SpecificationsInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			specificationsid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			specificationsid.setOnItemSelectedListener(this);
			// 设置默认值
			specificationsid.setVisibility(View.VISIBLE);
			db.close();
		}
		 //所属公司
		public void belongidDropdown() {
			ArrayAdapter<BranchCompanyInfo> adapter;
			List<BranchCompanyInfo> listName = new ArrayList<BranchCompanyInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("BranchCompanyInfo","Name");
			if (cursor.moveToFirst()) {
				do {
					BranchCompanyInfo item = new BranchCompanyInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			adapter = new ArrayAdapter<BranchCompanyInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			belongid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			belongid.setOnItemSelectedListener(this);
			// 设置默认值
			belongid.setVisibility(View.VISIBLE);
			db.close();
		}
		 //产品规格型号purchaseid
		public void warhouseidDropdown() {
			ArrayAdapter<Warehouse> adapter;
			List<Warehouse> listName = new ArrayList<Warehouse>();
			db.open();
			Cursor cursor = db.getAllTitles("Warehouse","Name");
			if (cursor.moveToFirst()) {
				do {
					Warehouse item = new Warehouse();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			adapter = new ArrayAdapter<Warehouse>(this,
					android.R.layout.simple_dropdown_item_1line, listName);
			//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			warhouseid.setAdapter(adapter);
			warhouseid.setThreshold(1);
			// 添加事件Spinner事件监听
			//warhouseid.setOnItemSelectedListener(this);
			warhouseid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					//监听选择下拉列表事件
					warhouseidkey = ((Warehouse) warhouseid.getAdapter().getItem(arg2)).getKey();
					
				}
				
			});
			// 设置默认值
			warhouseid.setVisibility(View.VISIBLE);
			db.close();
		}
		 //采购公司
		public void purchaseidDropdown() {
			ArrayAdapter<AccountCompanyInfo> adapter;
			List<AccountCompanyInfo> listName = new ArrayList<AccountCompanyInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("AccountCompanyInfo","Name");
			if (cursor.moveToFirst()) {
				do {
					AccountCompanyInfo item = new AccountCompanyInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			adapter = new ArrayAdapter<AccountCompanyInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			purchaseid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			purchaseid.setOnItemSelectedListener(this);
			// 设置默认值
			purchaseid.setVisibility(View.VISIBLE);
			db.close();
		}
	// 添加数据到ListView
	private void addListView(List<ContentValues> list) {
		// 将数据添加到ListView
		listMap = new ArrayList<Map<String, Object>>();
		int idcount = 1;
		for (ContentValues tiddata : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ID", idcount);
			map.put("TID", tiddata.get("BarCode"));
			map.put("COUNT", tiddata.get("INQuantity"));
			idcount++;
			listMap.add(map);
		}
		listViewData.setAdapter(new SimpleAdapter(CangChuIn.this,
				listMap, R.layout.listview_item, new String[] { "ID", "TID",
						"COUNT" }, new int[] { R.id.textView_id,
						R.id.textView_TID, R.id.textView_count }));

	}

	private SoundPool sp;
	private Map<Integer, Integer> suondMap;

	// 初始化声音池
	private void initSoundPool() {
		sp = new SoundPool(1, AudioManager.STREAM_ALARM, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(1, sp.load(this, R.raw.msg, 1));
	}

	// 播放声音池声音
	private void play(int sound, int number) {
		AudioManager am = (AudioManager) this
				.getSystemService(this.AUDIO_SERVICE);
		// 返回当前AlarmManager最大音量
		float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);

		// 返回当前AudioManager对象的音量值
		float audioCurrentVolume = am
				.getStreamVolume(AudioManager.STREAM_ALARM);
		float volumnRatio = audioCurrentVolume / audioMaxVolume;
		sp.play(suondMap.get(sound), // 播放的音乐Id
				audioCurrentVolume, // 左声道音量
				audioCurrentVolume, // 右声道音量
				1, // 优先级，0为最低
				number, // 循环次数，0无不循环，-1无永远循环
				1);// 回放速度，值在0.5-2.0之间，1为正常速度
	}

	// 播放提示音
	private void playMedia(Context context) {
		player = MediaPlayer.create(context, R.raw.msg);
		if (player.isPlaying()) {
			// player.reset();
			player.stop();
			return;
		}

		player.start();
		// player.release();
	}

	protected void onDestroy() {
		// 卸载广播接受者
		unregisterReceiver(myReceiver);
		super.onDestroy();
	}

	// 广播接收者
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 服务返回的数据
			recvString = intent.getStringExtra("result");
			switch (cmdCode) {
			case Constants.CMD_ISO18000_6C_READ:
				if (recvString != null) {
					// 播放提示音
					// playMedia(ISO18000_6C_Inventory.this);
					play(1, 0);
					if (recvString != null) {
						// batchNum.setText(recvString);
						barcode.setText(recvString.toString());
					} else {
						Toast.makeText(getApplicationContext(),
								"Failure to read data", 0).show();
					}
				}
				break;

			default:
				break;
			}
		}
	}
	//监听返回键
			public boolean onKeyDown(int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
					Intent intent=new Intent();
					 intent.setClass(CangChuIn.this,CangChuManagerActivity.class);
			            startActivity(intent);
				}
				return super.onKeyDown(keyCode, event);
			}
	
}
