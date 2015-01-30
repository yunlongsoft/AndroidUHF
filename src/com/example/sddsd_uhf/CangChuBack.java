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

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class CangChuBack extends Activity implements OnClickListener, OnItemSelectedListener {

	private Spinner warhouseid;
	private Spinner projectid;
	private Spinner companyid;
	private EditText returnquantitys;//归还数量
	private EditText wastequantitys;//损耗数量
	private Button scanbutton;
	private EditText barcode;
	private Button addbutton;
	private Button savebutton;//保存按钮
	private Button clearbutton;//清空按钮
	
	private String warhouseidkey;
	private String projectidkey;
	private String companyidkey;
	
	private int quantity;
	private int onquantity;
	
	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
	List<String> listdetail = new ArrayList<String>(); // 在库明细列表
	private final String activity = "com.example.sddsd_uhf.CangChuBack";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cangchu_back);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(CangChuBack.this,
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
		warhouseid=(Spinner) findViewById(R.id.warhouseid);
		projectid=(Spinner) findViewById(R.id.projectid);
		companyid=(Spinner) findViewById(R.id.companyid);
		returnquantitys=(EditText) findViewById(R.id.returnquantity);
		wastequantitys=(EditText) findViewById(R.id.wastequantity);
		returnquantitys.setText("0");
		wastequantitys.setText("1");
		barcode=(EditText) findViewById(R.id.barcode);
		scanbutton=(Button) findViewById(R.id.scanbutton);
		addbutton=(Button) findViewById(R.id.addbutton);
		savebutton=(Button) findViewById(R.id.savebutton);
		clearbutton=(Button) findViewById(R.id.clearbutton);
		
		companyidDropdown();
		projectidDropdown();
		warhouseidDropdown();
		scanbutton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cangchu_back, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent toService=new Intent(CangChuBack.this,UhfService.class);
		Intent ac=new Intent();
		ac.setAction("com.example.UHFService.UhfService");
		ac.putExtra("activity", activity);
		sendBroadcast(ac);
		cmdCode = 0;
		db.open();
		switch(v.getId()){
		case R.id.scanbutton:
			cmdCode = Constants.CMD_ISO18000_6C_READ;
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.addbutton:
			String barcodetext = barcode.getText().toString().trim();
			String number = returnquantitys.getText().toString().trim();
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
					onquantity= (int)cursor.getDouble(0);
						ContentValues contentvalue = new ContentValues();
						contentvalue.put("Key", CreateGuid.GenerateGUID());
						contentvalue.put("BarCode", recvString);
						contentvalue.put("ReturnQuantity", returnquantitys.getText().toString().trim());
						contentvalue.put("WasteQuantity", wastequantitys.getText().toString().trim());
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
						
						String sqlstr="Update StockDetail set OnQuantity="+(onquantity+quantity)+" where BarCode='"+recvString+"'";
						listdetail.add(sqlstr);
						barcode.setText("");
					
				} else {

					Toast.makeText(CangChuBack.this, "该标签不在库",
							Toast.LENGTH_SHORT).show();
				}
				cursor.close();// 关闭游标，释放资源
			}
			break;
		case R.id.clearbutton:
			listIndetail.removeAll(listIndetail);
			listdetail.removeAll(listdetail);
			listViewData.setAdapter(null);
			break;
		case R.id.savebutton:
				if (!listIndetail.isEmpty()) {
					InsertLocalSQL();// 保存到本地数据库
				}
				break;
		}
		db.close();
		}
private void InsertLocalSQL() {
	// TODO Auto-generated method stub
	db.open();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// 生成入库单号
	SimpleDateFormat formatNumber = new SimpleDateFormat("yyyyMMddHHmmss");
	String dateBatchnum = formatNumber.format(new Date());// 获取当前时间并格式化
	String BuildBatchnum = "CC-GHD" + dateBatchnum;

	String date = format.format(new Date());// 获取当前时间并格式化
	String stockAllocateInfokey = CreateGuid.GenerateGUID();
	ContentValues content = new ContentValues();
	content.put("Key", stockAllocateInfokey);
	content.put("BatchNumber", BuildBatchnum);// 归还单
	content.put("StockReturnDateTime", date);// 归还日期
	content.put("WarhouseID", warhouseidkey);//归还仓库
	content.put("ProjectID", projectidkey);// 使用项目
	content.put("CompanyID", companyidkey);// 使用公司
	
	content.put("CreateOperater", CurrentUser.CurrentUserGuid);
	content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
	// db.insert(content, "");
	long resultInsert = 0;
	
	// 遍历插入入库明细表
	for (int i = 0; i < this.listIndetail.size(); i++) {
		ContentValues contentvalue = listIndetail.get(i);
		contentvalue.put("StockReturnInfoID", stockAllocateInfokey);
		// db.insert(contentvalue, "AssetInDetail");// 将入库信息插入数据库
	}
	try {
		// 主表从表保存，只要有一个不成功就事务回滚
		resultInsert = db.insertList(content, listIndetail,
				"StockReturnInfo", "StockReturnDetail");
		if (resultInsert ==listIndetail.size()+1) {
			Toast.makeText(this, "退库成功", Toast.LENGTH_LONG).show();
			listIndetail.removeAll(listIndetail);
			listViewData.setAdapter(null);
		}else{
			Toast.makeText(this, "退库失败，请重试", Toast.LENGTH_LONG).show();
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		System.out.println("仓储退库异常");
	}
	
	resultInsert = 0;// 重新归零
	/*// 遍历更新在库明细
	for (int i = 0; i < this.listMap.size(); i++) {
		String tid = listMap.get(i).get("TID").toString();
		String sqlstr="Update StockDetail set OnQuantity="+(onquantity+quantity)+" where BarCode='"+tid+"'";
		listdetail.add(sqlstr);
	}*/
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
	case R.id.companyid://使用公司
		companyidkey=((BranchCompanyInfo)companyid.getSelectedItem()).getKey();
		break;
	case R.id.warhouseid://仓库
		warhouseidkey=((Warehouse)warhouseid.getSelectedItem()).getKey();
		break;
	case R.id.projectid://项目
		projectidkey=((ProjectInfo)projectid.getSelectedItem()).getKey();
		break;
	}
}

@Override
public void onNothingSelected(AdapterView<?> arg0) {
	// TODO Auto-generated method stub
	
}

	// 使用，公司
		public void companyidDropdown() {
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
			companyid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			companyid.setOnItemSelectedListener(this);
			// 设置默认值
			companyid.setVisibility(View.VISIBLE);
			db.close();
		}

	// 调拨源，项目
		public void projectidDropdown() {
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
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			projectid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			projectid.setOnItemSelectedListener(this);
			// 设置默认值
			projectid.setVisibility(View.VISIBLE);
			db.close();
		}
		
		// 仓库调拨源
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
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			warhouseid.setAdapter(adapter);
			// 添加事件Spinner事件监听
			warhouseid.setOnItemSelectedListener(this);
			// 设置默认值
			warhouseid.setVisibility(View.VISIBLE);
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
			map.put("COUNT", tiddata.get("ReturnQuantity"));
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(CangChuBack.this,
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
				 intent.setClass(CangChuBack.this,CangChuManagerActivity.class);
		            startActivity(intent);
			}
			return super.onKeyDown(keyCode, event);
		}

}
