package com.example.sddsd_uhf;

import java.io.ByteArrayOutputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PanDianActivity extends Activity implements OnClickListener {
	private Button scanbutton;// 扫描按钮
	private EditText barcode;// 标签号
	private Button querybutton;// 查询按钮
	private EditText remark;// 备注
	private TextView materialname;// 设备名称
	private TextView materialmodel;// 设备规格型号
	private TextView oncount;// 在库数量
	private EditText realcount;// 实际数量
	private EditText description;//盘点描述
	private Button addbutton;// 查询按钮
	private Button savebutton;// 保存按钮
	private Button clearbutton;// 清空按钮
	private String materialnamekey;// 产品id
	private String materialmodelkey;// 产品规格型号id

	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
	private final String activity = "com.example.sddsd_uhf.PanDianActivity";
	DBAdapter db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pan_dian);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(PanDianActivity.this, UhfService.class);
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
		listViewData = (ListView) findViewById(R.id.data_list);// 获取listview
	/*	listViewData.setOnItemClickListener(new OnItemClickListener(){

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> selection, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				ListView selected=(ListView)selection;
				HashMap<String,String> map=(HashMap<String,String>) listViewData.getItemAtPosition(position);
				
			}
			
		});*/
		listTID = new ArrayList<TID>();// 实例化一个list
		listMap = new ArrayList<Map<String, Object>>();// 创建一个泛型集合
		scanbutton = (Button) findViewById(R.id.scanbutton);
		querybutton = (Button) findViewById(R.id.querybutton);
		addbutton = (Button) findViewById(R.id.addbutton);
		savebutton = (Button) findViewById(R.id.button_save);
		clearbutton = (Button) findViewById(R.id.button_clear);
		barcode = (EditText) findViewById(R.id.barcode);
		realcount = (EditText) findViewById(R.id.realcount);
		realcount.setText("1");
		remark = (EditText) findViewById(R.id.remark);
		materialname = (TextView) findViewById(R.id.materialname);
		materialmodel = (TextView) findViewById(R.id.materialmodel);
		oncount = (TextView) findViewById(R.id.oncount);
		description=(EditText) findViewById(R.id.description);
		
		scanbutton.setOnClickListener(this);
		querybutton.setOnClickListener(this);
		addbutton.setOnClickListener(this);
		savebutton.setOnClickListener(this);
		clearbutton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent toService = new Intent(PanDianActivity.this, UhfService.class);
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
		case R.id.querybutton:
			String barcodetext = barcode.getText().toString().trim();
			if (barcodetext.length() != 0) {
				Cursor cursor = null;
				db.open();
				try {
					// 查询这个标签是否存在
					cursor = db.getTitle("StockDetail", new String[] {
							"MaterialID", "OnQuantity", "SpecificationsID" },
							"BarCode", recvString);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					Log.e("addTidToList", "查询这个标签是否存在" + e.toString());
				}
				int count = 0;
				if (cursor.moveToFirst()) {
					materialnamekey = cursor.getString(cursor
							.getColumnIndex("MaterialID"));
					materialmodelkey = cursor.getString(cursor
							.getColumnIndex("SpecificationsID"));
					/*
					Cursor cursors = db.getTitles("StockDetail",
							new String[] { "OnQuantity" }, "MaterialID='"
									+ materialnamekey + "'"
									+ " and SpecificationsID='"
									+ materialmodelkey + "'");
					if (cursors.moveToFirst()) {
						do {
							count += cursor.getDouble(cursor
									.getColumnIndex("OnQuantity"));

						} while (cursor.moveToNext());
					}*/
					count=(int) cursor.getDouble(cursor.getColumnIndex("OnQuantity"));
					materialname.setText(db.getNameBykey("MaterialInfo",
							materialnamekey));
					materialmodel.setText(db.getNameBykey("SpecificationsInfo",
							materialmodelkey));
					String countstr=String.valueOf(count);
					oncount.setText(countstr);// 显示在库数量
				} else {
					Toast.makeText(PanDianActivity.this, "该标签不在库",
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.addbutton:
			String barcodetext1 = barcode.getText().toString().trim();
			String number = realcount.getText().toString().trim();
			if (barcodetext1.length() != 0) {
				/*
				 * Cursor cursor = null; db.open(); try { // 查询这个标签是否存在 cursor =
				 * db.getTitle("StockDetail", new String[] { "OnQuantity" },
				 * "BarCode", recvString); } catch (SQLException e) { // TODO
				 * Auto-generated catch block Log.e("addTidToList", "查询这个标签是否存在"
				 * + e.toString()); }
				 */
				// if (cursor.moveToFirst()) {// 判断
				// 新建一个ContentValues对象，用来记录添加的数据
				ContentValues contentvalue = new ContentValues();
				contentvalue.put("Key", CreateGuid.GenerateGUID());
				contentvalue.put("BarCode", recvString);
				contentvalue.put("StockQuantity", oncount.getText().toString()
						.trim());
				contentvalue.put("StockRealQuantity", realcount.getText().toString()
						.trim());
				contentvalue.put("Remark", remark.getText().toString().trim());
				contentvalue.put("CreateOperater", CurrentUser.CurrentUserGuid);
				contentvalue.put("UpdateOperater", CurrentUser.CurrentUserGuid);
				if (listIndetail.isEmpty()) {
					listIndetail.add(contentvalue);
					Log.e("read tid", listIndetail.get(0).get("BarCode")
							.toString());
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
				addListView(listIndetail);
				barcode.setText("");

				/*
				 * } else {
				 * 
				 * Toast.makeText(PanDianActivity.this, "该标签不在库",
				 * Toast.LENGTH_SHORT).show(); } cursor.close();// 关闭游标，释放资源
				 */}
			break;
		case R.id.button_save:
			if (!listIndetail.isEmpty()) {
				InsertLocalSQL();// 保存到本地数据库
			}
			break;
		case R.id.button_clear:
			listIndetail.removeAll(listIndetail);
			listViewData.setAdapter(null);
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
		String BuildBatchnum = "CC-PDD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String stockCheckInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", stockCheckInfokey);
		content.put("BatchNumber", BuildBatchnum);//盘点日期Description
		content.put("CheckDateTime", date);//盘点日期
		content.put("Description", description.getText().toString());//盘点日期
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "");
		long resultInsert = 0;
		
		// 遍历插入入库明细表
		for (int i = 0; i < this.listIndetail.size(); i++) {
			ContentValues contentvalue = listIndetail.get(i);
			contentvalue.put("StockCheckInfoID", stockCheckInfokey);
			// db.insert(contentvalue, "AssetInDetail");// 将入库信息插入数据库
		}
		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail,
					"StockCheckInfo", "StockCheckDetail");
			if (resultInsert ==listIndetail.size()+1) {
				Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
				listIndetail.removeAll(listIndetail);
				listViewData.setAdapter(null);
			}else{
				Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		   System.out.println("保存盘点信息异常");
		}
		db.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pan_dian, menu);
		return true;
	}

	/*// 获取全部产品
	public void ShowList() {

		Cursor cursor = db.getAllTitles("MaterialInfo", new String[] { "Key",
				"Model", "Price" });
		if (cursor.moveToFirst()) {
			do {
				Toast.makeText(
						this,
						"ID:" + cursor.getString(0) + "\n" + "Model:"
								+ cursor.getString(1) + "\n",
						Toast.LENGTH_SHORT).show();

			} while (cursor.moveToNext());
		}
		Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.diaobo);
		rawBitmap.compress(CompressFormat.PNG, 100, os);
		ContentValues contents = new ContentValues();
		contents.put("Key", 2);
		contents.put("Model", "sdf");
		contents.put("Name", "name");
		contents.put("Price", 12.9);
		contents.put("MaterialModelID", "qweqwe");
		contents.put("image", os.toByteArray());
		contents.put("ProviderID", "sfsdfsdf");
		contents.put("DelFlg", false);
		// db.insert(contents, "MaterialInfo");
	}
*/
	// 添加数据到ListView
	private void addListView(List<ContentValues> list) {
		// 将数据添加到ListView
		listMap = new ArrayList<Map<String, Object>>();
		int idcount = 1;
		for (ContentValues tiddata : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ID", idcount);
			map.put("TID", tiddata.get("BarCode"));
			map.put("COUNT", tiddata.get("StockRealQuantity"));
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(PanDianActivity.this,
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
			intent.setClass(PanDianActivity.this, CangChuManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

}
