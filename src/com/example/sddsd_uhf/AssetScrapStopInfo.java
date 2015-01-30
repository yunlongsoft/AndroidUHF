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
import com.example.bean.DepartmentInfo;
import com.example.bean.ProjectInfo;
import com.example.bean.TID;
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

public class AssetScrapStopInfo extends Activity implements OnClickListener,
		OnItemSelectedListener {

	private Spinner Cleverid;
	private Spinner BranchCompanyid;
	private Spinner mode;
	private Spinner departmentid;
	private String cleverkey;
	private String branchCompanykey;
	private String departmentidkey;
	private int modeselect;
	private Button scanButton;
	private EditText barcode;
	private Button addbutton;
	private Button savebutton;
	private Button clearbutton;
	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	private final String activity = "com.example.sddsd_uhf.AssetScrapStopInfo";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_asset_scrap_stop_info);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(AssetScrapStopInfo.this,
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
		Cleverid = (Spinner) findViewById(R.id.CleverID);
		BranchCompanyid = (Spinner) findViewById(R.id.BranchCompanyID);
		mode = (Spinner) findViewById(R.id.Mode);
		departmentid = (Spinner) findViewById(R.id.departmentid);
		scanButton = (Button) findViewById(R.id.scanButton);
		barcode = (EditText) findViewById(R.id.barcode);
		addbutton = (Button) findViewById(R.id.addButton);
		savebutton = (Button) findViewById(R.id.button_save);
		clearbutton = (Button) findViewById(R.id.button_clear);
		listViewData = (ListView) findViewById(R.id.data_list);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();

		materialModeDropdown();// 初始化状态列表
		// theuserCleverDropdown();//初始化职员列表
		theuserCompanyDropdown();// 使用单位列表
		scanButton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
		Intent toService = new Intent(AssetScrapStopInfo.this, UhfService.class);
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
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.addButton:
			String barcodetext = barcode.getText().toString().trim();
			if (barcodetext.length() != 0) {
				Cursor cursor = null;
				db.open();
				try {
					// 查询这个标签是否存在
					cursor = db.getTitle("AssetDetail",
							new String[] { "AssetState" }, "BarCode",
							recvString);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					Log.e("addTidToList", "查询这个标签是否存在" + e.toString());
				}
				if (!cursor.moveToFirst()) {// 判断是否为首次归还
					Toast.makeText(AssetScrapStopInfo.this, "该物品不在库，不能进行此操作",
							Toast.LENGTH_SHORT).show();
				} else {
					switch (cursor.getInt(0)) {// 资产状态(1-在库;2-领用;3-报停;4-报废)
					case 1:
						addListView(listTID, barcodetext);
						barcode.setText("");
						break;
					case 2:
						Toast.makeText(AssetScrapStopInfo.this, "该物品已被领用,请先退库",
								Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(AssetScrapStopInfo.this, "该物品已经报停",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(AssetScrapStopInfo.this, "该物品已经报废",
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
				cursor.close();// 关闭游标，释放资源
				db.close();
			}
			break;
		case R.id.button_clear:
			listTID.removeAll(listTID);
			listViewData.setAdapter(null);
			break;
		case R.id.button_save:
			if (!listTID.isEmpty()) {
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
		String BuildBatchnum = "ZC-BTBFD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String AssetScrapStopInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", AssetScrapStopInfokey);
		content.put("BatchNumber", BuildBatchnum);// 归还单
		content.put("Date", date);// 归还日期
		//content.put("CleverID", cleverkey);// 使用人
		content.put("AssetScrapStopMode", modeselect);// 设备状态
		//content.put("BranchCompanyID", branchCompanykey);// 使用单位
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "");
		long resultInsert = 0;
		List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
		// 遍历插入入库明细表
		for (int i = 0; i < this.listMap.size(); i++) {
			String tid = listMap.get(i).get("TID").toString();
			ContentValues contentvalue = new ContentValues();
			contentvalue.put("Key", CreateGuid.GenerateGUID());
			contentvalue.put("AssetScrapStopInfoID", AssetScrapStopInfokey);
			contentvalue.put("BarCode", tid);
			contentvalue.put("Quantity", 1);
			contentvalue.put("CleverID", cleverkey);// 使用人
			contentvalue.put("BranchCompanyID", branchCompanykey);// 使用单位
			contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
			contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
			// db.insert(contentvalue, "AssetInDetail");// 将入库信息插入数据库
			listIndetail.add(contentvalue);
		}

		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail,
					"AssetScrapStopInfo", "AssetScrapStopDetail");
			if (resultInsert == listIndetail.size() + 1) {
				Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
				listTID.removeAll(listTID);
				listViewData.setAdapter(null);
			} else {
				Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("报废报停保存异常");
		}
		List<String> listdetail = new ArrayList<String>(); // 在库明细列表
		resultInsert = 0;// 重新归零
		// 遍历更新在库明细
		for (int i = 0; i < this.listMap.size(); i++) {
			String tid = listMap.get(i).get("TID").toString();
			String sqlstr = "Update AssetDetail set AssetState="
					+ (modeselect + 2) + " where BarCode='" + tid + "'";
			listdetail.add(sqlstr);
		}
		try {
			resultInsert = db.updateList(listdetail);
			if (resultInsert == this.listMap.size()) {
				lifecycle(listMap);
				Toast.makeText(this, "保存到在库成功", Toast.LENGTH_LONG).show();
				listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("保存到在库失败异常");
		}
		db.close();
	}

	public void lifecycle(List<Map<String, Object>> listdetail) {
		// List<String> listdetail = new ArrayList<String>(); // 在库明细列表
		String tid = null;
		for (int i = 0; i < listdetail.size(); i++) {
			tid = listdetail.get(i).get("TID").toString();

			SimpleDateFormat formatdate = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String operdate = formatdate.format(new Date());// 获取当前时间并格式化
			ContentValues content = new ContentValues();
			content.put("Key", CreateGuid.GenerateGUID());
			content.put("AssetOperatingID", CurrentUser.CurrentUserGuid);
			content.put("BarCode", tid);
			if (modeselect == 1) {
				content.put("OperatingType","报停" );
			} else {
				content.put("OperatingType", "报废");
			}
			Cursor cursor = db
					.getTitles("AssetDetail", new String[] { "MaterialID",
							"SpecificationsID" }, "BarCode='" + tid + "'");
			if (cursor.moveToFirst()) {
				content.put("MaterialID", cursor.getString(0));
				content.put("SpecificationsID", cursor.getString(1));
			}

			content.put("OperatingDate", operdate);
			content.put("Number", 1);
			content.put("CreateOperater", CurrentUser.CurrentUserGuid);
			content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
			content.put("UpdateDateTime", operdate);
			content.put("CreateDateTime", operdate);
			db.open();
			db.insert(content, "AssetLifecycleInfo");
			db.close();
		}
	}

	// 使用人，职员
	public void theuserCleverDropdown(String key) {
		ArrayAdapter<CleverInfo> adapter;
		List<CleverInfo> listName = new ArrayList<CleverInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("CleverInfo", "DepatmentID", key,
				"Name");
		if (cursor.moveToFirst()) {
			do {
				CleverInfo item = new CleverInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				item.DepatmentID = cursor.getString(cursor
						.getColumnIndex("DepatmentID"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		if (listName.isEmpty()) {
			CleverInfo item = new CleverInfo();
			item.Key = "00000000-0000-0000-0000-000000000000";
			item.Name = "未知职员";
			listName.add(item);
		}
		adapter = new ArrayAdapter<CleverInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		Cleverid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		Cleverid.setOnItemSelectedListener(this);
		// 设置默认值
		Cleverid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 使用人，项目
	public void theuserCompanyDropdown() {
		ArrayAdapter<BranchCompanyInfo> adapter;
		List<BranchCompanyInfo> listName = new ArrayList<BranchCompanyInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("BranchCompanyInfo", "Name");
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
		BranchCompanyid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		BranchCompanyid.setOnItemSelectedListener(this);
		// 设置默认值
		BranchCompanyid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 使用部门
	public void theuserDepartmentDropdown(String key) {
		ArrayAdapter<DepartmentInfo> adapter;
		List<DepartmentInfo> listName = new ArrayList<DepartmentInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("DepartmentInfo", "CompanyID", key,
				"Name");
		if (cursor.moveToFirst()) {
			do {
				DepartmentInfo item = new DepartmentInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				item.CompanyID = cursor.getString(cursor
						.getColumnIndex("CompanyID"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<DepartmentInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		departmentid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		departmentid.setOnItemSelectedListener(this);
		// 设置默认值
		departmentid.setVisibility(View.VISIBLE);
		db.close();
	}

	public void materialModeDropdown() {
		ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
				.createFromResource(this, R.array.AsseetModel,
						android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		mode.setAdapter(adaptertype);
		// 添加事件Spinner事件监听
		mode.setOnItemSelectedListener(this);
		// 设置默认值
		mode.setVisibility(View.VISIBLE);
	}

	@Override
	public void onItemSelected(AdapterView<?> selection, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (selection.getId()) {
		case R.id.CleverID:
			cleverkey = ((CleverInfo) Cleverid.getSelectedItem()).getKey();
			break;
		case R.id.BranchCompanyID:
			branchCompanykey = ((BranchCompanyInfo) BranchCompanyid
					.getSelectedItem()).getKey();
			theuserDepartmentDropdown(branchCompanykey);
			break;
		case R.id.departmentid:
			departmentidkey = ((DepartmentInfo) departmentid.getSelectedItem())
					.getKey();
			theuserCleverDropdown(departmentidkey);
			break;
		case R.id.Mode:
			modeselect = arg2 + 1;
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.asset_scrap_stop_info, menu);
		return true;
	}

	// 添加数据到ListView
	private void addListView(List<TID> list, String tid) {
		// 第一次读入数据
		if (list.isEmpty()) {
			TID epcTag = new TID();
			epcTag.setTID(tid);
			epcTag.setCount(1);
			list.add(epcTag);
			Log.e("read tid", tid);
		} else {
			for (int i = 0; i < list.size(); i++) {
				TID mTID = list.get(i);
				// list中有此EPC
				if (tid.equals(mTID.getTID())) {
					mTID.setCount(mTID.getCount() + 1);
					list.set(i, mTID);
					break;
				} else if (i == (list.size() - 1)) {
					// list中没有此epc
					TID newtid = new TID();
					newtid.setTID(tid);
					newtid.setCount(1);
					list.add(newtid);
				}
			}

		}
		// 将数据添加到ListView
		listMap = new ArrayList<Map<String, Object>>();
		int idcount = 1;
		for (TID tiddata : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ID", idcount);
			map.put("TID", tiddata.getTID());
			map.put("COUNT", 1);
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(AssetScrapStopInfo.this,
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

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(AssetScrapStopInfo.this,
					ZiChanManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
