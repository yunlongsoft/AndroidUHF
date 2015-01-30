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

public class PropertyDiaoBoActivity extends Activity implements
		OnClickListener, OnItemSelectedListener {

	private Spinner AssetFromid;// 调拨源
	private Spinner AssetToid;// 调拨目标
	private Spinner allcotetype;// 调拨类型
	private Button addButton;// 添加按钮
	private Button scanButton;// 扫描
	private EditText barcode;// 条形码
	private Button savebutton;// 保存
	private Button clearbutton;// 清空
	private String assetfrom;
	private String assetTo;
	private int allotype;

	private int belongtype;// 在库类型
	private String belongkey;// 在库所属

	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	private final String activity = "com.example.sddsd_uhf.PropertyDiaoBoActivity";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_property_diao_bo);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(PropertyDiaoBoActivity.this,
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
		AssetFromid = (Spinner) findViewById(R.id.AssetFromID);
		AssetToid = (Spinner) findViewById(R.id.AssetToID);
		allcotetype = (Spinner) findViewById(R.id.allcotetype);
		addButton = (Button) findViewById(R.id.addButton);
		scanButton = (Button) findViewById(R.id.scanButton);
		barcode = (EditText) findViewById(R.id.barcode);
		savebutton = (Button) findViewById(R.id.button_save);
		clearbutton = (Button) findViewById(R.id.button_clear);
		listViewData = (ListView) findViewById(R.id.data_list);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();
		assetAllocattypeDropdown();
		addButton.setOnClickListener(this);
		scanButton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
		Intent toService = new Intent(PropertyDiaoBoActivity.this,
				UhfService.class);
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
				if (!cursor.moveToFirst()) {// 判断是否为在库
					Toast.makeText(PropertyDiaoBoActivity.this,
							"该物品不在库，不能进行此操作", Toast.LENGTH_SHORT).show();
				} else {
					switch (cursor.getInt(0)) {
					case 1:
						addListView(listTID, barcodetext);
						barcode.setText("");
						break;
					case 2:
						addListView(listTID, barcodetext);
						barcode.setText("");
						break;
					case 3:
						Toast.makeText(PropertyDiaoBoActivity.this, "该物品已经报停",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(PropertyDiaoBoActivity.this, "该物品已经报废",
								Toast.LENGTH_SHORT).show();
						break;
					}
				}
				cursor.close();// 关闭游标，释放资源
				// db.close();
			}
			break;
		case R.id.button_clear:
			listTID.removeAll(listTID);
			listViewData.setAdapter(null);
			break;
		case R.id.button_save:
			if (listMap != null) {
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
		String BuildBatchnum = "ZC-DBD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String AssetAllocateInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", AssetAllocateInfokey);
		content.put("BatchNumber", BuildBatchnum);// 归还单
		content.put("AssetToID", assetTo);// 调拨目标
		content.put("AssetFromID", assetfrom);// 调拨源
		content.put("AssetType", allotype);// 调拨类型
		content.put("Date", date);// 调拨日期
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "");
		long resultInsert = 0;
		List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
		// 遍历插入入库明细表
		for (int i = 0; i < this.listMap.size(); i++) {
			// View view = listViewData.getChildAt(i);
			String tid = listMap.get(i).get("TID").toString();
			ContentValues contentvalue = new ContentValues();
			contentvalue.put("Key", CreateGuid.GenerateGUID());
			contentvalue.put("AssetAllocateInfoID", AssetAllocateInfokey);
			contentvalue.put("BarCode", tid);
			contentvalue.put("Quantity", 1.0);
			contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
			contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
			listIndetail.add(contentvalue);
		}

		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail,
					"AssetAllocateInfo", "AssetAllocateDetail");
			if (resultInsert == listIndetail.size() + 1) {
				Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
				listTID.removeAll(listTID);
				listViewData.setAdapter(null);
			} else {
				Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("资产调拨异常");
		}
		List<String> listdetail = new ArrayList<String>(); // 在库明细列表
		resultInsert = 0;// 重新归零
		// 遍历插入在库明细
		for (int i = 0; i < this.listMap.size(); i++) {
			String tid = listMap.get(i).get("TID").toString();
			if (allotype == 1) {
				String sqlstr = "Update AssetDetail set BelongType=" + allotype
						+ " , BelongID='" + assetTo + "' where BarCode='" + tid
						+ "'";
				listdetail.add(sqlstr);
			}
		}
		try {
			resultInsert = db.updateList(listdetail);
			if (resultInsert == this.listMap.size()) {
				lifecycle(listMap);
				Toast.makeText(this, "更新在库成功", Toast.LENGTH_LONG).show();
				// listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("资产调拨异常");
		}
		db.close();
	}

	public void lifecycle(List<Map<String, Object>> listdetail) {
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
			content.put("OperatingType", "调拨");
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

	@Override
	public void onItemSelected(AdapterView<?> selection, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (selection.getId()) {
		case R.id.AssetFromID:// 1，公司，2库房，3项目
			if (allotype == 1) {
				assetfrom = ((BranchCompanyInfo) AssetFromid.getSelectedItem())
						.getKey();

			} else if (allotype == 2) {
				assetfrom = ((Warehouse) AssetFromid.getSelectedItem())
						.getKey();

			} else if (allotype == 3) {
				assetfrom = ((ProjectInfo) AssetFromid.getSelectedItem())
						.getKey();

			}

			break;
		case R.id.AssetToID:// 1，公司，2库房，3项目
			if (allotype == 1) {
				assetTo = ((BranchCompanyInfo) AssetToid.getSelectedItem())
						.getKey();
			} else if (allotype == 2) {
				assetTo = ((Warehouse) AssetToid.getSelectedItem()).getKey();
			} else if (allotype == 3) {
				assetTo = ((ProjectInfo) AssetToid.getSelectedItem()).getKey();
			}

			break;
		case R.id.allcotetype:
			allotype = arg2 + 1;// 1，公司，2库房，3项目
			if (belongtype == 1 && allotype == belongtype) {
				fromBranchCompanyDropdown(belongkey);
				toBranchCompanyDropdown();
			} else if (belongtype == 2 && allotype == belongtype) {
				fromwarshusDropdown(belongkey);
				towarshusDropdown();
			} else if (allotype == 3 && allotype == belongtype) {
				fromprojectDropdown(belongkey);
				toprojectDropdown();
			} else {
				if (allotype == 1) {
					fromBranchCompanyDropdown(belongkey);
					toBranchCompanyDropdown();
				} else if (allotype == 2) {
					fromwarshusDropdown(belongkey);
					towarshusDropdown();
				} else if (allotype == 3) {
					fromprojectDropdown(belongkey);
					toprojectDropdown();
				}
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	// 调拨源，公司
	public void fromBranchCompanyDropdown(String key) {
		ArrayAdapter<BranchCompanyInfo> adapter;
		List<BranchCompanyInfo> listName = new ArrayList<BranchCompanyInfo>();
		db.open();
		Cursor cursor = db
				.getAllTitles("BranchCompanyInfo", "Key", key, "Name");
		if (cursor.moveToFirst()) {
			do {
				BranchCompanyInfo item = new BranchCompanyInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		if (listName.isEmpty()) {
			/*
			 * BranchCompanyInfo itembase = new BranchCompanyInfo();
			 * itembase.Key = "00000000-0000-0000-0000-000000000000";
			 * itembase.Name ="未知公司"; listName.add(itembase);
			 */
			Cursor cursors = db.getAllTitles("BranchCompanyInfo", "Name");
			if (cursors.moveToFirst()) {
				do {
					BranchCompanyInfo item = new BranchCompanyInfo();
					item.Key = cursors.getString(cursors.getColumnIndex("Key"));
					item.Name = cursors.getString(cursors
							.getColumnIndex("Name"));
					listName.add(item);
				} while (cursors.moveToNext());
			}
		}
		adapter = new ArrayAdapter<BranchCompanyInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		AssetFromid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetFromid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetFromid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 调拨目标，公司
	public void toBranchCompanyDropdown() {
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
		AssetToid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetToid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetToid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 调拨源，项目
	public void fromprojectDropdown(String key) {
		ArrayAdapter<ProjectInfo> adapter;
		List<ProjectInfo> listName = new ArrayList<ProjectInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("ProjectInfo", "Key", key, "Name");
		if (cursor.moveToFirst()) {
			do {
				ProjectInfo item = new ProjectInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		if (listName.isEmpty()) {
			/*
			 * ProjectInfo itembase = new ProjectInfo(); itembase.Key =
			 * "00000000-0000-0000-0000-000000000000"; itembase.Name ="未知项目";
			 * listName.add(itembase);
			 */

			Cursor cursors = db.getAllTitles("ProjectInfo", "Name");
			if (cursors.moveToFirst()) {
				do {
					ProjectInfo item = new ProjectInfo();
					item.Key = cursors.getString(cursors.getColumnIndex("Key"));
					item.Name = cursors.getString(cursors
							.getColumnIndex("Name"));
					listName.add(item);
				} while (cursors.moveToNext());
			}
		}
		adapter = new ArrayAdapter<ProjectInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		AssetFromid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetFromid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetFromid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 调拨目标，项目
	public void toprojectDropdown() {
		ArrayAdapter<ProjectInfo> adapter;
		List<ProjectInfo> listName = new ArrayList<ProjectInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("ProjectInfo", "Name");
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
		AssetToid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetToid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetToid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 仓库调拨源
	public void fromwarshusDropdown(String key) {
		ArrayAdapter<Warehouse> adapter;
		List<Warehouse> listName = new ArrayList<Warehouse>();
		db.open();
		Cursor cursor = db.getAllTitles("Warehouse", "Key", key, "Name");
		if (cursor.moveToFirst()) {
			do {
				Warehouse item = new Warehouse();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		if (listName.isEmpty()) {
			/*
			 * Warehouse itembase = new Warehouse(); itembase.Key =
			 * "00000000-0000-0000-0000-000000000000"; itembase.Name ="未知仓库";
			 * listName.add(itembase);
			 */
			Cursor cursors = db.getAllTitles("Warehouse", "Name");
			if (cursors.moveToFirst()) {
				do {
					Warehouse item = new Warehouse();
					item.Key = cursors.getString(cursors.getColumnIndex("Key"));
					item.Name = cursors.getString(cursors
							.getColumnIndex("Name"));
					listName.add(item);
				} while (cursors.moveToNext());
			}
		}
		adapter = new ArrayAdapter<Warehouse>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		AssetFromid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetFromid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetFromid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 仓库调拨目标
	public void towarshusDropdown() {
		ArrayAdapter<Warehouse> adapter;
		List<Warehouse> listName = new ArrayList<Warehouse>();
		db.open();
		Cursor cursor = db.getAllTitles("Warehouse", "Name");
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
		AssetToid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		AssetToid.setOnItemSelectedListener(this);
		// 设置默认值
		AssetToid.setVisibility(View.VISIBLE);
		db.close();
	}

	// 调拨类型
	public void assetAllocattypeDropdown() {
		ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
				.createFromResource(this, R.array.Asseetdiaobotype,
						android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		allcotetype.setAdapter(adaptertype);
		// 添加事件Spinner事件监听
		allcotetype.setOnItemSelectedListener(this);
		// 设置默认值
		allcotetype.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.property_diao_bo, menu);
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
			map.put("COUNT", tiddata.getCount());
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(PropertyDiaoBoActivity.this,
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
						Cursor curosr = db.getTitles("AssetDetail",
								new String[] { "BelongType", "BelongID" },
								"BarCode='" + recvString.toString() + "'");
						if (curosr.moveToFirst()) {
							belongtype = curosr.getInt(curosr
									.getColumnIndex("BelongType"));
							belongkey = curosr.getString(curosr
									.getColumnIndex("BelongID"));
							if (belongtype == 1 && allotype == belongtype) {
								fromBranchCompanyDropdown(belongkey);
								toBranchCompanyDropdown();
							} else if (belongtype == 2
									&& allotype == belongtype) {
								fromwarshusDropdown(belongkey);
								towarshusDropdown();
							} else if (allotype == 3 && allotype == belongtype) {
								fromprojectDropdown(belongkey);
								toprojectDropdown();
							} else {
								if (allotype == 1) {
									fromBranchCompanyDropdown(belongkey);
									toBranchCompanyDropdown();
								} else if (allotype == 2) {
									fromwarshusDropdown(belongkey);
									towarshusDropdown();
								} else if (allotype == 3) {
									fromprojectDropdown(belongkey);
									toprojectDropdown();
								}
							}
						}
						System.out.println("allotype:" + allotype
								+ ",belongtype:" + belongtype);
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

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(PropertyDiaoBoActivity.this,
					ZiChanManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
