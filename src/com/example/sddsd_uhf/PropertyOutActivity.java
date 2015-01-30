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
import com.example.bean.EPC;
import com.example.bean.ProjectInfo;
import com.example.bean.ProviderInfo;
import com.example.bean.SpecificationsInfo;
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

public class PropertyOutActivity extends Activity implements
		OnItemSelectedListener, OnClickListener {

	private Button scanButton;// 扫描按钮
	private EditText barcode;// 显示扫描到的标签号
	// private EditText Depreciation;// 折旧金额
	// private EditText Depreciationyear;// 折旧年限
	private Button addbutton;// 将扫描到的标签号添加到list列表
	private Button savebutton;// 保存按钮
	private Button clearbutton;// 清空列表按钮
	private Spinner Recipients;// 领用者下拉列表
	private Spinner Departmentid;// 所属部门
	private Spinner theuser;// 使用者列表
	private Spinner theusertype;// 使用者类型列表
	private Spinner usercompany;// 使用者公司
	private String Recipientskey;// 领用者。即职员
	private String Departmentkey;// 所属分公司
	private String theuserkey;// 使用者，即项目或职员
	private String userstyle;// 使用者类型
	private int userstype;// 使用者类型

	private String usercompanykey;// 使用公司id
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
	private final String activity = "com.example.sddsd_uhf.PropertyOutActivity";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_property_out);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(PropertyOutActivity.this,
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
		scanButton = (Button) findViewById(R.id.scanbutton);
		barcode = (EditText) findViewById(R.id.barcodes);
		// Depreciation = (EditText) findViewById(R.id.depreciation);
		// Depreciation.setText("0.0");
		addbutton = (Button) findViewById(R.id.button_add);
		savebutton = (Button) findViewById(R.id.button_save);
		clearbutton = (Button) findViewById(R.id.button_clear);
		listViewData = (ListView) findViewById(R.id.data_list);
		usercompany = (Spinner) findViewById(R.id.usercompany);
		Recipients = (Spinner) findViewById(R.id.recipients);
		Departmentid = (Spinner) findViewById(R.id.departmentid);
		theuser = (Spinner) findViewById(R.id.theusers);
		theusertype = (Spinner) findViewById(R.id.theuserstype);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();
		// RecipientsDropdown();
		theusertypeDropdown();
		userCompanyDropdown();

		scanButton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.property_out, menu);
		return true;
	}

	/*
	 * 按钮单击事件
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
		Intent toService = new Intent(PropertyOutActivity.this,
				UhfService.class);
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
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.button_add:
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
				/*
				 * if (!cursor.moveToFirst()) { addListView(listTID,
				 * barcodetext); barcode.setText(""); } else {
				 * Toast.makeText(PropertyOutActivity.this, "该物品已经出库",
				 * Toast.LENGTH_SHORT).show(); }
				 */
				if (cursor.moveToFirst()) {
					switch (cursor.getInt(0)) {// 资产状态(1-在库;2-领用;3-报停;4-报废)
					case 1:
						ContentValues contentvalue = new ContentValues();
						contentvalue.put("Key", CreateGuid.GenerateGUID());
						contentvalue.put("BarCode", recvString);
						contentvalue.put("Quantity", 1);
						contentvalue.put("CreateOperater",
								CurrentUser.CurrentUserGuid);
						contentvalue.put("UpdateOperater",
								CurrentUser.CurrentUserGuid);
						if (listIndetail.isEmpty()) {
							listIndetail.add(contentvalue);
							Log.e("read tid", listIndetail.get(0)
									.get("BarCode").toString());
						} else {
							for (int i = 0; i < listIndetail.size(); i++) {
								ContentValues mTID = listIndetail.get(i);
								// list中有此EPC
								if (recvString.equals(mTID.get("BarCode")
										.toString())) {
									break;
								} else if (i == (listIndetail.size() - 1)) {
									// list中没有此epc
									listIndetail.add(contentvalue);
								}
							}
						}
						// addListView(listTID, barcodetext);
						addListView(listIndetail);

						String sqlstr = "Update AssetDetail set AssetState=2 where BarCode='"
								+ recvString + "'";
						listdetail.add(sqlstr);
						barcode.setText("");
						break;
					case 2:
						Toast.makeText(PropertyOutActivity.this, "该物品已经被领用",
								Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(PropertyOutActivity.this, "该物品已经报停",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						Toast.makeText(PropertyOutActivity.this, "该物品已经报废",
								Toast.LENGTH_SHORT).show();
						break;
					}
				} else {
					Toast.makeText(PropertyOutActivity.this, "该标签不在库",
							Toast.LENGTH_SHORT).show();
				}
				cursor.close();// 关闭游标，释放资源
				db.close();
			}
			break;
		case R.id.button_clear:
			listIndetail.removeAll(listIndetail);
			listdetail.removeAll(listdetail);
			listViewData.setAdapter(null);
			break;
		case R.id.button_save:
			if (!listIndetail.isEmpty()) {// 保存到本地数据库
				InsertLocalSQL();
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
		String BuildBatchnum = "ZC-CKD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String AssetOutInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", AssetOutInfokey);
		content.put("BatchNumber", BuildBatchnum);// 出库单
		content.put("DepartmentID", Departmentkey);// 所属公司部门
		content.put("OutDate", date);// 出库日期
		content.put("UseModel", userstype);// 使用方式(1-内部使用;2-项目使用)
		content.put("ConsumingUserID", Recipientskey);// 领用者
		content.put("UserID", theuserkey);// 使用者
		content.put("UserCompanyID", usercompanykey);// 使用公司
		// content.put("Depreciation", 0);// 折旧金额
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "");
		long resultInsert = 0;

		// 遍历插入入库明细表
		for (int i = 0; i < this.listIndetail.size(); i++) {
			// View view = listViewData.getChildAt(i);
			ContentValues contentvalue = listIndetail.get(i);
			contentvalue.put("AssetOutInfoID", AssetOutInfokey);
		}

		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail, "AssetOutInfo",
					"AssetOutDetail");
			if (resultInsert == listIndetail.size() + 1) {
				Toast.makeText(this, "出库成功", Toast.LENGTH_LONG).show();
				listIndetail.removeAll(listIndetail);
				listViewData.setAdapter(null);
			} else {
				Toast.makeText(this, "出库失败，请重试", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("资产出库异常");
		}

		resultInsert = 0;// 重新归零
		/*
		 * // 遍历插入在库明细 for (int i = 0; i < this.listMap.size(); i++) { String
		 * tid = listMap.get(i).get("TID").toString(); String
		 * sqlstr="Update AssetDetail set AssetState=2 where BarCode='"+tid+"'";
		 * listdetail.add(sqlstr); }
		 */
		try {
			resultInsert = db.updateList(listdetail);
			if (resultInsert == this.listMap.size()) {
				lifecycle(listMap);
				Toast.makeText(this, "更新在库成功", Toast.LENGTH_LONG).show();
				listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "更新到在库失败，请重试", Toast.LENGTH_LONG).show();
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
			content.put("OperatingType", "领用");
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

	// 添加数据到ListView
	private void addListView(List<ContentValues> list) {
		// 将数据添加到ListView
		listMap = new ArrayList<Map<String, Object>>();
		int idcount = 1;
		for (ContentValues tiddata : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ID", idcount);
			map.put("TID", tiddata.get("BarCode"));
			map.put("COUNT", tiddata.get("Quantity"));
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(PropertyOutActivity.this,
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

	// 领用人
	public void RecipientsDropdown(String key) {
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
			item.Name = "未知规格";
			listName.add(item);
		}
		adapter = new ArrayAdapter<CleverInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		Recipients.setAdapter(adapter);
		// 添加事件Spinner事件监听
		Recipients.setOnItemSelectedListener(this);
		// 设置默认值
		Recipients.setVisibility(View.VISIBLE);
		db.close();
	}

	// 领用人所属部门
	public void RecipientstypeDropdown(String key) {
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
		Departmentid.setAdapter(adapter);
		// 添加事件Spinner事件监听
		Departmentid.setOnItemSelectedListener(this);
		// 设置默认值
		Departmentid.setVisibility(View.VISIBLE);
		db.close();
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
			item.Name = "未知规格";
			listName.add(item);
		}
		adapter = new ArrayAdapter<CleverInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		theuser.setAdapter(adapter);
		// 添加事件Spinner事件监听
		theuser.setOnItemSelectedListener(this);
		// 设置默认值
		theuser.setVisibility(View.VISIBLE);
		db.close();
	}

	// 使用人，项目
	public void theuserProjectDropdown() {
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
		theuser.setAdapter(adapter);
		// 添加事件Spinner事件监听
		theuser.setOnItemSelectedListener(this);
		// 设置默认值
		theuser.setVisibility(View.VISIBLE);
		db.close();
	}

	// 使用公司
	public void userCompanyDropdown() {
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
		usercompany.setAdapter(adapter);
		// 添加事件Spinner事件监听
		usercompany.setOnItemSelectedListener(this);
		// 设置默认值
		usercompany.setVisibility(View.VISIBLE);
		db.close();
	}

	public void theusertypeDropdown() {
		ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
				.createFromResource(this, R.array.AsseetUserTypes,
						android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		theusertype.setAdapter(adaptertype);
		// 添加事件Spinner事件监听
		theusertype.setOnItemSelectedListener(this);
		// 设置默认值
		theusertype.setVisibility(View.VISIBLE);
	}

	@Override
	public void onItemSelected(AdapterView<?> selectitem, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (selectitem.getId()) {
		case R.id.recipients:
			Recipientskey = ((CleverInfo) Recipients.getSelectedItem())
					.getKey();
			/*
			 * RecipientstypeDropdown(((CleverInfo)
			 * Recipients.getSelectedItem()) .getDepatmentID());
			 */
			break;
		case R.id.usercompany:
			usercompanykey = ((BranchCompanyInfo) usercompany.getSelectedItem())
					.getKey();
			RecipientstypeDropdown(usercompanykey);
			break;
		case R.id.departmentid:
			Departmentkey = ((DepartmentInfo) Departmentid.getSelectedItem())
					.getKey();

			RecipientsDropdown(Departmentkey);
			if (userstype == 1) {
				// 选择职员
				theuserCleverDropdown(Departmentkey);
			}
			break;
		case R.id.theusers:
			// 判断为职员还是项目

			if (userstyle == "CleverInfo") {
				theuserkey = ((CleverInfo) theuser.getSelectedItem()).getKey();
			}
			if (userstyle == "ProjectInfo") {
				theuserkey = ((ProjectInfo) theuser.getSelectedItem()).getKey();
			}
			break;
		case R.id.theuserstype:
			userstype = arg2 + 1;
			if (arg2 == 0) {
				userstyle = "CleverInfo";
				// 选择职员
				theuserCleverDropdown(Departmentkey);
			}
			if (arg2 == 1) {
				userstyle = "ProjectInfo";
				// 选择项目
				theuserProjectDropdown();
			}
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

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
			/*
			 * case Constants.CMD_ISO18000_6C_INVENTORY: if (recvString != null)
			 * { // 播放提示音 // playMedia(ISO18000_6C_Inventory.this); play(1, 0);
			 * addListView(listTID, recvString); } break;
			 */
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
			intent.setClass(PropertyOutActivity.this,
					ZiChanManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
