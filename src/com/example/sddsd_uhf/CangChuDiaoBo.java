package com.example.sddsd_uhf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
import com.example.bean.BranchCompanyInfo;
import com.example.bean.CleverInfo;
import com.example.bean.ProjectInfo;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class CangChuDiaoBo extends Activity implements OnClickListener, OnItemSelectedListener {
	private Spinner allcotetype;//调拨类型
	private AutoCompleteTextView allcotefromid;//调拨源
	private AutoCompleteTextView allcotetoid;//调拨目标
	private AutoCompleteTextView callinpeopleid;//调入负责人
	private AutoCompleteTextView recallpeopleid;//调出负责人
	private AutoCompleteTextView handledpeopleid;//调拨经手人
	//private Spinner allcoteoperater;//调拨人
	private EditText amount;//金额
	private EditText allcotequantity;//调拨数量
	private Button scanbutton;//扫描按钮
	private EditText barcode;//标签号
	private Button addbutton;//添加按钮
	private Button savebutton;//保存按钮
	private Button clearbutton;//清空按钮
	
	private int quantity;
	private int onquantity;
	private String belongkey;//在库所属
	
	private int allcotetypekey;//调拨类型
	private String allcotefromidkey;//调拨源主键
	private String allcotetoidkey;//调拨目标主键
	private String callinpeopleidkey;//调入负责人主键
	private String recallpeopleidkey;//调出负责人主键
	private String handledpeopleidkey;//调拨经手人主键
	private String allcoteoperaterkey;//调拨源主键
	
	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
	private final String activity = "com.example.sddsd_uhf.CangChuDiaoBo";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cang_chu_diao_bo);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(CangChuDiaoBo.this,
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
		listViewData = (ListView) findViewById(R.id.data_list);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();
		
		allcotetype=(Spinner) findViewById(R.id.allcotetype);
		allcotefromid=(AutoCompleteTextView) findViewById(R.id.allcotefromid);
		allcotetoid=(AutoCompleteTextView) findViewById(R.id.allcotetoid);
		callinpeopleid=(AutoCompleteTextView) findViewById(R.id.callinpeopleid);
		recallpeopleid=(AutoCompleteTextView) findViewById(R.id.recallpeopleid);
		handledpeopleid=(AutoCompleteTextView) findViewById(R.id.handledpeopleid);
		//allcoteoperater=(Spinner) findViewById(R.id.allcoteoperater);
		
		amount=(EditText) findViewById(R.id.amount);
		amount.setText("0.0");
		allcotequantity=(EditText) findViewById(R.id.allcotequantity);
		allcotequantity.setText("1");
		barcode=(EditText) findViewById(R.id.barcode);
		scanbutton=(Button)findViewById(R.id.scanbutton);
		addbutton=(Button)findViewById(R.id.addbutton);
		savebutton=(Button)findViewById(R.id.savebutton);
		clearbutton=(Button)findViewById(R.id.clearbutton);
		
		assetAllocattypeDropdown();
		callinpeopleidDropdown();
		recallpeopleidDropdown();
		handledpeopleidDropdown();
		scanbutton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
		ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this,
				R.array.AsseetwarehouseType, android.R.layout.simple_spinner_dropdown_item);
		allcotetype.setAdapter(adapter);
		// 添加事件Spinner事件监听
		allcotetype.setOnItemSelectedListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
		Intent toService = new Intent(CangChuDiaoBo.this, UhfService.class);
		Intent ac = new Intent();
		ac.setAction("com.example.UHFService.UhfService");
		ac.putExtra("activity", activity);
		sendBroadcast(ac);
		cmdCode = 0;
		db.open();
		switch (v.getId()) {
		case R.id.scanbutton:
			cmdCode = Constants.CMD_ISO18000_6C_READ;
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.addbutton:
			String barcodetext = barcode.getText().toString().trim();
			String number = allcotequantity.getText().toString().trim();
			quantity = Integer.parseInt(number);
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
				if (cursor.moveToFirst()) {// 判断
					onquantity=(int)cursor.getDouble(0);
					if (onquantity >= quantity) {
						ContentValues contentvalue = new ContentValues();
						contentvalue.put("Key", CreateGuid.GenerateGUID());
						contentvalue.put("BarCode", recvString);
						contentvalue.put("Amount", amount.getText().toString().trim());
						contentvalue.put("AllocateQuantity", allcotequantity.getText().toString().trim());
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
						//addListView(listTID, barcodetext);
						addListView(listIndetail);
						barcode.setText("");
					} else {
						Toast.makeText(CangChuDiaoBo.this,
								"该产品当前库存量只有" + cursor.getDouble(0), Toast.LENGTH_SHORT)
								.show();
					}
				} else {

					Toast.makeText(CangChuDiaoBo.this, "该标签不在库",
							Toast.LENGTH_SHORT).show();
				}
				cursor.close();// 关闭游标，释放资源
			}
			break;
		case R.id.clearbutton:
			listIndetail.removeAll(listIndetail);
			listViewData.setAdapter(null);
			break;
		case R.id.savebutton:
				if (!listIndetail.isEmpty()&&checkEdit()) {
					InsertLocalSQL();// 保存到本地数据库
				}
				break;
		}
		db.close();
	}
	private boolean checkEdit() {
		// TODO Auto-generated method stub
		if (allcotefromid.getText().toString().trim().equals("")||allcotetoid.getText().toString().trim().equals("")) {
			Toast.makeText(CangChuDiaoBo.this, "调拨源或调拨目标不能为空", Toast.LENGTH_SHORT)
					.show();
		}else{
			if (callinpeopleid.getText().toString().trim().equals("")||recallpeopleid.getText().toString().trim().equals("")||handledpeopleid.getText().toString().trim().equals("")) {
				Toast.makeText(CangChuDiaoBo.this, "经手人和调拨人都不能为空", Toast.LENGTH_SHORT)
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
		String BuildBatchnum = "CC-DBD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String stockAllocateInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", stockAllocateInfokey);
		content.put("BatchNumber", BuildBatchnum);// 归还单
		content.put("AllcoteDateTime", date);// 归还日期
		content.put("AllcoteType", allcotetypekey);// 调拨类型
		content.put("AllcoteToID", allcotetoidkey);// 调拨目标
		content.put("AllcoteFromID", allcotefromidkey);// 调拨源
		content.put("CallInPeopleID", callinpeopleidkey);// 调入负责人
		content.put("RecallPeopleID", recallpeopleidkey);// 调出负责人
		content.put("HandledPeopleID", handledpeopleidkey);// 经手人
		content.put("AllcoteOperater", "00000000-0000-0000-0000-000000000000");// 调拨人allcoteoperaterkey
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "");
		long resultInsert = 0;
		
		// 遍历插入入库明细表
		for (int i = 0; i < this.listIndetail.size(); i++) {
			ContentValues contentvalue = listIndetail.get(i);
			contentvalue.put("StockAllocateInfoID", stockAllocateInfokey);
			// db.insert(contentvalue, "AssetInDetail");// 将入库信息插入数据库
		}
		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail,
					"StockAllocateInfo", "StockAllocateDetail");
			if (resultInsert ==listIndetail.size()+1) {
				Toast.makeText(this, "调拨成功", Toast.LENGTH_SHORT).show();
				listIndetail.removeAll(listIndetail);
				listViewData.setAdapter(null);
			}else{
				Toast.makeText(this, "调拨失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("仓储调拨异常");
		}
		List<String> listdetail = new ArrayList<String>(); // 在库明细列表
		resultInsert = 0;// 重新归零
		// 遍历更新在库明细
		for (int i = 0; i < this.listMap.size(); i++) {
			String tid = listMap.get(i).get("TID").toString();
			String sqlstr="Update StockDetail set WarehouseID='"+allcotetoidkey+"' where BarCode='"+tid+"'";
			listdetail.add(sqlstr);
		}
		try {
			resultInsert = db.updateList(listdetail);
			if (resultInsert==this.listMap.size()) {
				Toast.makeText(this, "保存到在库成功", Toast.LENGTH_LONG).show();
				listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "保存到在库失败，请重试", Toast.LENGTH_LONG).show();
		}
		db.close();
	}
	@Override
	public void onItemSelected(AdapterView<?> selection, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch(selection.getId()){
		/*case R.id.callinpeopleid://调入负责人
			callinpeopleidkey=((CleverInfo)callinpeopleid.getSelectedItem()).getKey();
			break;
		case R.id.recallpeopleid://调出负责人
			recallpeopleidkey=((CleverInfo)recallpeopleid.getSelectedItem()).getKey();
			break;
		case R.id.handledpeopleid://经手人
			handledpeopleidkey=((CleverInfo)handledpeopleid.getSelectedItem()).getKey();
			break;
			*/
		/*case R.id.allcotetoid:
			if (allcotetypekey==1) {
				allcotetoidkey=((BranchCompanyInfo)allcotetoid.getSelectedItem()).getKey();
			}else if(allcotetypekey==2){
				allcotetoidkey=((Warehouse)allcotetoid.getSelectedItem()).getKey();
			}else if(allcotetypekey==3){
				allcotetoidkey=((ProjectInfo)allcotetoid.getSelectedItem()).getKey();
				
			}
			break;
		case R.id.allcotefromid://1，公司，2库房，3项目
			if (allcotetypekey==1) {
				allcotefromidkey=((BranchCompanyInfo)allcotefromid.getSelectedItem()).getKey();
			}else if(allcotetypekey==2){
				allcotefromidkey=((Warehouse)allcotefromid.getSelectedItem()).getKey();
			}else if(allcotetypekey==3){
				allcotefromidkey=((ProjectInfo)allcotefromid.getSelectedItem()).getKey();
			}

			break;*/
		case R.id.allcotetype:
			allcotetypekey=arg2+1;//1，公司，2库房，3项目
			if(allcotetypekey==1){
				fromBranchCompanyDropdown();
				toBranchCompanyDropdown();
			}
			else if(allcotetypekey==2){
				fromwarshusDropdown(belongkey);
				towarshusDropdown();
				}
			else if(allcotetypekey==3){
				fromprojectDropdown();
				toprojectDropdown();
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	// 调拨源，公司
		public void fromBranchCompanyDropdown() {
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
			/*if (listName.isEmpty()) {
				BranchCompanyInfo itembase = new BranchCompanyInfo();
				itembase.Key = "00000000-0000-0000-0000-000000000000";
				itembase.Name ="未知公司";
				listName.add(itembase);
			}*/
			adapter = new ArrayAdapter<BranchCompanyInfo>(this,
					android.R.layout.simple_dropdown_item_1line, listName);
			//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			allcotefromid.setAdapter(adapter);
			//设置输入一个字符时，就开始检索
			allcotefromid.setThreshold(1);
			// 添加事件Spinner事件监听
			allcotefromid.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					allcotefromidkey=((BranchCompanyInfo)allcotefromid.getAdapter().getItem(arg2)).getKey();
				}
				
			});
			allcotefromid.setOnFocusChangeListener(new OnFocusChangeListener() {
				
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
			allcotefromid.setVisibility(View.VISIBLE);
			db.close();
		}
		// 调拨目标，公司
			public void toBranchCompanyDropdown() {
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
						android.R.layout.simple_dropdown_item_1line, listName);
				//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 将adapter 添加到spinner中
				allcotetoid.setAdapter(adapter);
				// 添加事件Spinner事件监听
				allcotetoid.setThreshold(1);
				// 添加事件Spinner事件监听
				allcotetoid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						allcotetoidkey=((BranchCompanyInfo)allcotetoid.getAdapter().getItem(arg2)).getKey();
					}
					
				});
				allcotetoid.setOnFocusChangeListener(new OnFocusChangeListener() {
					
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
				allcotetoid.setVisibility(View.VISIBLE);
				db.close();
			}

		// 调拨源，项目
			public void fromprojectDropdown() {
				ArrayAdapter<ProjectInfo> adapter;
				List<ProjectInfo> listName = new ArrayList<ProjectInfo>();
				
				db.open();
				Cursor cursor = db.getAllTitles("ProjectInfo","Name");
				if (cursor.moveToFirst()) {
					do {
						ProjectInfo item = new ProjectInfo();
						item.Key = cursor.getString(cursor.getColumnIndex("Key"));
						item.Name = cursor.getString(cursor.getColumnIndex("Name"));
						listName.add(item);
					} while (cursor.moveToNext());
				}
				/*if (listName.isEmpty()) {
					ProjectInfo itembase = new ProjectInfo();
					itembase.Key = "00000000-0000-0000-0000-000000000000";
					itembase.Name ="未知项目";
					listName.add(itembase);
				}*/
				adapter = new ArrayAdapter<ProjectInfo>(this,
						android.R.layout.simple_dropdown_item_1line, listName);
				//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 将adapter 添加到spinner中
				allcotefromid.setAdapter(adapter);
				//设置开始检索的字符数
				allcotefromid.setThreshold(1);
				// 添加事件Spinner事件监听
				allcotefromid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						allcotefromidkey=((ProjectInfo)allcotefromid.getAdapter().getItem(arg2)).getKey();
					}
				
				});
				allcotefromid.setOnFocusChangeListener(new OnFocusChangeListener() {
					
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
				allcotefromid.setVisibility(View.VISIBLE);
				db.close();
			}
			// 调拨目标，项目
					public void toprojectDropdown() {
						ArrayAdapter<ProjectInfo> adapter;
						List<ProjectInfo> listName = new ArrayList<ProjectInfo>();
						db.open();
						Cursor cursor = db.getAllTitles("ProjectInfo","Name");
						if (cursor.moveToFirst()) {
							do {
								ProjectInfo item = new ProjectInfo();
								item.Key = cursor.getString(cursor.getColumnIndex("Key"));
								item.Name = cursor.getString(cursor.getColumnIndex("Name"));
								listName.add(item);
							} while (cursor.moveToNext());
						}
						adapter = new ArrayAdapter<ProjectInfo>(this,
								android.R.layout.simple_dropdown_item_1line, listName);
						//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						// 将adapter 添加到spinner中
						allcotetoid.setAdapter(adapter);
						//设置开始检索的字符数
						allcotetoid.setThreshold(1);
						// 添加事件Spinner事件监听
						allcotetoid.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1,
									int arg2, long arg3) {
								// TODO Auto-generated method stub
								allcotetoidkey=((ProjectInfo)allcotetoid.getAdapter().getItem(arg2)).getKey();
							}
						
						});
						allcotetoid.setOnFocusChangeListener(new OnFocusChangeListener() {
							
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
						allcotetoid.setVisibility(View.VISIBLE);
						db.close();
					}
			// 仓库调拨源
			public void fromwarshusDropdown(String key) {
				ArrayAdapter<Warehouse> adapter;
				List<Warehouse> listName = new ArrayList<Warehouse>();
				db.open();
				Cursor cursor = db.getAllTitles("Warehouse","Key",key,"Name");
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
				allcotefromid.setAdapter(adapter);
				//设置开始检索的字符数
				allcotefromid.setThreshold(1);
				// 添加事件Spinner事件监听
				allcotefromid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						allcotefromidkey=((Warehouse)allcotefromid.getAdapter().getItem(arg2)).getKey();
					}
				
				});
				allcotefromid.setOnFocusChangeListener(new OnFocusChangeListener() {
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
				allcotefromid.setVisibility(View.VISIBLE);
				db.close();
			}
			//仓库调拨目标
			public void towarshusDropdown() {
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
				allcotetoid.setAdapter(adapter);
				//设置开始检索的字符数
				allcotetoid.setThreshold(1);
				// 添加事件Spinner事件监听
				allcotetoid.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						allcotetoidkey=((Warehouse)allcotetoid.getAdapter().getItem(arg2)).getKey();
					}
				
				});
				allcotetoid.setOnFocusChangeListener(new OnFocusChangeListener() {
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
				allcotetoid.setVisibility(View.VISIBLE);
				cursor.close();
				db.close();
			}
			//调拨类型
		   public void assetAllocattypeDropdown() {
			ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
					.createFromResource(this, R.array.AsseetwarehouseType,
							android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			allcotetype.setAdapter(adaptertype);
			// 添加事件Spinner事件监听
			allcotetype.setOnItemSelectedListener(this);
			// 设置默认值
			allcotetype.setVisibility(View.VISIBLE);
		}

		   
	// 调入负责人
			public void callinpeopleidDropdown() {
				ArrayAdapter<CleverInfo> adapter;
				List<CleverInfo> listName = new ArrayList<CleverInfo>();
				db.open();
				Cursor cursor = db.getAllTitles("CleverInfo","Name");
				if (cursor.moveToFirst()) {
					do {
						CleverInfo item = new CleverInfo();
						item.Key = cursor.getString(cursor.getColumnIndex("Key"));
						item.Name = cursor.getString(cursor.getColumnIndex("Name"));
						item.DepatmentID=cursor.getString(cursor.getColumnIndex("DepatmentID"));
						listName.add(item);
					} while (cursor.moveToNext());
				}
				adapter = new ArrayAdapter<CleverInfo>(this,
						android.R.layout.simple_dropdown_item_1line, listName);
				//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 将adapter 添加到spinner中
				callinpeopleid.setAdapter(adapter);
				//
				callinpeopleid.setThreshold(1);
				// 添加事件Spinner事件监听
				callinpeopleid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						callinpeopleidkey=((CleverInfo)callinpeopleid.getAdapter().getItem(arg2)).getKey();
					}
				});
				callinpeopleid.setOnFocusChangeListener(new OnFocusChangeListener() {
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
				callinpeopleid.setVisibility(View.VISIBLE);
				db.close();
			}
			// 经手人，职员
			public void handledpeopleidDropdown() {
				ArrayAdapter<CleverInfo> adapter;
				List<CleverInfo> listName = new ArrayList<CleverInfo>();
				db.open();
				Cursor cursor = db.getAllTitles("CleverInfo","Name");
				if (cursor.moveToFirst()) {
					do {
						CleverInfo item = new CleverInfo();
						item.Key = cursor.getString(cursor.getColumnIndex("Key"));
						item.Name = cursor.getString(cursor.getColumnIndex("Name"));
						item.DepatmentID = cursor.getString(cursor.getColumnIndex("DepatmentID"));
						listName.add(item);
					} while (cursor.moveToNext());
				}
				adapter = new ArrayAdapter<CleverInfo>(this,
						android.R.layout.simple_dropdown_item_1line, listName);
				//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				// 将adapter 添加到spinner中
				handledpeopleid.setAdapter(adapter);
				handledpeopleid.setThreshold(1);
				// 添加事件Spinner事件监听
				handledpeopleid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						handledpeopleidkey=((CleverInfo)handledpeopleid.getAdapter().getItem(arg2)).getKey();
					}
				});
				handledpeopleid.setOnFocusChangeListener(new OnFocusChangeListener() {
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
				handledpeopleid.setVisibility(View.VISIBLE);
				db.close();
			}
			// 调出负责人，职员
					public void recallpeopleidDropdown() {
						ArrayAdapter<CleverInfo> adapter;
						List<CleverInfo> listName = new ArrayList<CleverInfo>();
						db.open();
						Cursor cursor = db.getAllTitles("CleverInfo","Name");
						if (cursor.moveToFirst()) {
							do {
								CleverInfo item = new CleverInfo();
								item.Key = cursor.getString(cursor.getColumnIndex("Key"));
								item.Name = cursor.getString(cursor.getColumnIndex("Name"));
								item.DepatmentID = cursor.getString(cursor.getColumnIndex("DepatmentID"));
								listName.add(item);
							} while (cursor.moveToNext());
						}
						adapter = new ArrayAdapter<CleverInfo>(this,
								android.R.layout.simple_dropdown_item_1line, listName);
						//adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						// 将adapter 添加到spinner中
						recallpeopleid.setAdapter(adapter);
						//
						recallpeopleid.setThreshold(1);
						
						// 添加事件Spinner事件监听
						recallpeopleid.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
								// TODO Auto-generated method stub
								recallpeopleidkey=((CleverInfo)recallpeopleid.getAdapter().getItem(arg2)).getKey();
							}
							
						});
						recallpeopleid.setOnFocusChangeListener(new OnFocusChangeListener() {
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
						recallpeopleid.setVisibility(View.VISIBLE);
						cursor.close();
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
				map.put("COUNT", tiddata.get("AllocateQuantity"));
				idcount++;
				listMap.add(map);
			}
			// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
			// Toast.LENGTH_SHORT).show();
			listViewData.setAdapter(new SimpleAdapter(CangChuDiaoBo.this,
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
							db.open();
							Cursor curosr=db.getTitles("StockDetail", new String[]{"WarehouseID"},"BarCode='"+recvString.toString()+"'");
							if(curosr.moveToFirst()){
								//belongtype=curosr.getInt(curosr.getColumnIndex("BelongType"));
								belongkey=curosr.getString(curosr.getColumnIndex("WarehouseID"));
								if(allcotetypekey==1){
									fromBranchCompanyDropdown();
									toBranchCompanyDropdown();
								}
								else if(allcotetypekey==2){
									fromwarshusDropdown(belongkey);
									towarshusDropdown();
									}
								else if(allcotetypekey==3){
									fromprojectDropdown();
									toprojectDropdown();
								}
							}
							db.close();
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
						 intent.setClass(CangChuDiaoBo.this,CangChuManagerActivity.class);
				            startActivity(intent);
					}
					return super.onKeyDown(keyCode, event);
				}
}
