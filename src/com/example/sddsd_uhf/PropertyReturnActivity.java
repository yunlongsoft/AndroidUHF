package com.example.sddsd_uhf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PropertyReturnActivity extends Activity implements OnClickListener {

	private Button scanbutton;// 扫描按钮
	private EditText barCode;// 获取的标签号
	private Button addbutton;// 添加到列表按钮
	private Button saveButton;// 保存按钮
	private Button clearbutton;// 保存按钮
	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	private final String activity = "com.example.sddsd_uhf.PropertyReturnActivity";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_property_return);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(PropertyReturnActivity.this,
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
		/*
		 * IOcost=(EditText) findViewById(R.id.IOCost);IOcost.setText("0.0");
		 * quantity=(EditText) findViewById(R.id.Quantity);
		 * quantity.setText("1");
		 */
		scanbutton = (Button) findViewById(R.id.scanButton);
		barCode = (EditText) findViewById(R.id.barcode);
		addbutton = (Button) findViewById(R.id.addButton);
		saveButton = (Button) findViewById(R.id.button_save);
		clearbutton = (Button) findViewById(R.id.clear_button);
		listViewData = (ListView) findViewById(R.id.data_list);
		listTID = new ArrayList<TID>();
		listMap = new ArrayList<Map<String, Object>>();

		scanbutton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.property_return, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
		Intent toService = new Intent(PropertyReturnActivity.this,
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
			String barcodetext = barCode.getText().toString().trim();
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
					Toast.makeText(this, "该标签不在库", Toast.LENGTH_SHORT).show();
				} else {
					if (cursor.getInt(0) == 2 || cursor.getInt(0) == 3) {// 资产状态(1-在库;2-领用;3-报停;4-报废)
						addListView(listTID, barcodetext);
						barCode.setText("");
					} else {
						Toast.makeText(PropertyReturnActivity.this,
								"该物品已经归还或者已报废！", Toast.LENGTH_SHORT).show();
					}
				}
				cursor.close();// 关闭游标，释放资源
				db.close();
			}
			break;
		case R.id.clear_button:
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
		String BuildBatchnum = "ZC-GHD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String AssetReturnInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", AssetReturnInfokey);
		content.put("BatchNumber", BuildBatchnum);// 归还单
		content.put("ReturnDate", date);// 归还日期
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
			contentvalue.put("AssetReturnInfoID", AssetReturnInfokey);
			contentvalue.put("BarCode", tid);
			contentvalue.put("Quantity", 1);
			contentvalue.put("IOCost", 0);// 折旧金额
			contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
			contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
			// db.insert(contentvalue, "AssetInDetail");// 将入库信息插入数据库
			listIndetail.add(contentvalue);
		}

		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail,
					"AssetReturnInfo", "AssetReturnDetail");
			if (resultInsert == listIndetail.size() + 1) {
				Toast.makeText(this, "归还成功", Toast.LENGTH_LONG).show();
				listTID.removeAll(listTID);
				listViewData.setAdapter(null);
			} else {
				Toast.makeText(this, "归还失败，请重试", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("资产归还异常");
		}
		List<String> listdetail = new ArrayList<String>(); // 在库明细列表
		resultInsert = 0;// 重新归零
		// 遍历插入在库明细
		for (int i = 0; i < this.listMap.size(); i++) {
			String tid = listMap.get(i).get("TID").toString();
			String sqlstr = "Update AssetDetail set AssetState=1,BelongID='00000000-0000-0000-0000-000000000000' where BarCode='"
					+ tid + "'";
			listdetail.add(sqlstr);
		}
		try {
			resultInsert = db.updateList(listdetail);
			if (resultInsert == this.listMap.size()) {
				lifecycle(listMap);
				Toast.makeText(this, "更新在库成功", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "更新到在库失败，请重试", Toast.LENGTH_LONG).show();
		}
	}

	// 记录设备的生命周期
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
			content.put("OperatingType", "归还");
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
		listViewData.setAdapter(new SimpleAdapter(PropertyReturnActivity.this,
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
						barCode.setText(recvString.toString());
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
			intent.setClass(PropertyReturnActivity.this,
					ZiChanManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
