package com.example.sddsd_uhf;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract.Columns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.hdhe.uhf.util.Tools;
import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
import com.example.bean.TID;
import com.example.common.Constants;
import com.example.common.CurrentUser;
import com.example.common.ImageButtonTemplate;
import com.example.common.MD5Utility;
import com.example.common.TableNameStrings;
import com.example.dao.ConvertToContentvalues;
import com.example.dao.DBAdapter;

public class MainActivity extends Activity implements OnClickListener,
		OnTouchListener {
	ImageButtonTemplate cangchuManager;
	ImageButtonTemplate ziChanManager;
	ImageButtonTemplate shuJuDownload;
	ImageButtonTemplate cleardata;
	ImageButtonTemplate readwritetag;
	ProgressDialog dialogs;
	private long exitTime = 0;
	private boolean isHaveData = true;
	private boolean Confirm;
	private boolean clearcangchu = false;
	private boolean clearzichan = false;
	public String barcode = null;
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
	private final String activity = "com.example.sddsd_uhf.MainActivity";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		SysApplication.getInstance().addActivity(this);
		setProgressBarVisibility(true);
		db = new DBAdapter(this);// 调用数据库创建
		// 启动服务
		Intent startServer = new Intent(MainActivity.this, UhfService.class);
		startService(startServer);
		initSoundPool();

		// 注册广播接收者
		myReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(activity);
		registerReceiver(myReceiver, filter);
		Bundle receivedata = this.getIntent().getExtras();
		try {
			isHaveData = receivedata.getBoolean("IsHaveData");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("isHaveData 为空");
		}
		InitMain();
	}

	/**
	 * 用Handler来更新UI
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			// 关闭ProgressDialog
			dialogs.dismiss();

		}
	};

	private void InitMain() {
		// TODO Auto-generated method stub
		// 仓储管理
		cangchuManager = (ImageButtonTemplate) findViewById(R.id.cangChuManager);
		// cangChuManager.setClickable(true);
		// 仓储盘点扫描
		ziChanManager = (ImageButtonTemplate) findViewById(R.id.storageScan);
		// ziChanManager.setClickable(true);
		// 资产出库扫描
		shuJuDownload = (ImageButtonTemplate) findViewById(R.id.shuJuDownload);
		// shuJuDownload.setClickable(true);
		cleardata = (ImageButtonTemplate) findViewById(R.id.cleardata);
		// shuJuDownload.setClickable(true);
		readwritetag = (ImageButtonTemplate) findViewById(R.id.readwritetag);
		// shuJuDownload.setClickable(true);
		cangchuManager.setOnClickListener(this);
		ziChanManager.setOnClickListener(this);
		shuJuDownload.setOnClickListener(this);
		cleardata.setOnClickListener(this);
		readwritetag.setOnClickListener(this);

		cangchuManager.setOnTouchListener(this);
		ziChanManager.setOnTouchListener(this);
		shuJuDownload.setOnTouchListener(this);
		cleardata.setOnTouchListener(this);
		readwritetag.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.cangChuManager:// 仓储管理
			if (isHaveData) {
				if (CurrentUser.userType == 4 || CurrentUser.userType == 1
						|| CurrentUser.userType == 2) {
					intent.setClass(MainActivity.this,
							CangChuManagerActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(this, "您当前没有权限", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "当前没有数据，请先下载数据！", Toast.LENGTH_SHORT)
						.show();
			}

			break;
		case R.id.storageScan:// 资产管理
			if (isHaveData) {
				if (CurrentUser.userType == 3 || CurrentUser.userType == 1
						|| CurrentUser.userType == 2) {
					intent.setClass(MainActivity.this,
							ZiChanManagerActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(this, "您当前没有权限", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "当前没有数据，请先下载数据！", Toast.LENGTH_SHORT)
						.show();
			}

			break;

		case R.id.shuJuDownload:// 初始化数据库
			new AlertDialog.Builder(this)
					.setTitle("确认")
					.setMessage("确定要更新本地数据库吗？")
					.setPositiveButton("是",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									dialogs = new ProgressDialog(
											getApplicationContext());
									dialogs = ProgressDialog.show(
											MainActivity.this, "Loading...",
											"Please wait...", true, false);
									// 新建线程
									new Thread() {
										@Override
										public void run() {
											// 需要花时间计算的方法
											Looper.prepare();
											Confirm = initDatabase();
											// 向handler发消息
											handler.sendEmptyMessage(0);
											Toast.makeText(MainActivity.this,
													"数据下载成功！",
													Toast.LENGTH_SHORT).show();
											if (Confirm) {
												Intent intentlogin = new Intent(
														MainActivity.this,
														LoginActivity.class);
												startActivity(intentlogin);
											}
											Looper.loop();
										}
									}.start();
								}
							}).setNegativeButton("否", null).show();
			break;
		case R.id.cleardata:
			if (isHaveData) {
				showDialog_Layout(this);
			}
			break;
		case R.id.readwritetag:
			final EditText shownum = new EditText(MainActivity.this);
			shownum.setClickable(false);
			shownum.setFocusable(false);
			db.open();
			Cursor cursor = null;

			try {
				cursor = db.getAllTitles("RecodeTag", null, null, null, null,
						null, "BarCode DESC");// new
				// String[]{"BarCode"}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("获取标签号异常");
			}
			if (cursor.moveToFirst()) {
				barcode = cursor.getString(cursor.getColumnIndex("BarCode"));
				if (barcode != null) {
					int barcodeint = Integer.parseInt(barcode);
					barcodeint++;// 编号加1
					DecimalFormat df = new DecimalFormat("00000000");// 要转换的编号格式
					barcode = df.format(barcodeint);// 格式化得到的int编号
				}
			} else {
				barcode = "00000001";
			}

			shownum.setText(barcode);
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					MainActivity.this);
			dialog.setTitle("您要写入的标签号是").setIcon(R.drawable.dialogicon)
					.setView(shownum).setNegativeButton("取消", null);
			dialog.setPositiveButton("写卡",
					new DialogInterface.OnClickListener() {
						private Intent toService = new Intent(
								MainActivity.this, UhfService.class);

						@Override
						public void onClick(DialogInterface dialogin, int whitch) {
							// TODO Auto-generated method stub
							Intent ac = new Intent();
							ac.setAction("com.example.UHFService.UhfService");
							ac.putExtra("activity", activity);
							sendBroadcast(ac);

							byte[] accessPassword = null;
							byte[] dataBytes = null;
							accessPassword = Tools.HexString2Bytes("00000000");

							int writeDataLen = barcode.length() / 4;
							if (writeDataLen % 2 != 0) {
								barcode = barcode + "0";
							}
							dataBytes = Tools.HexString2Bytes(barcode);
							cmdCode = Constants.CMD_ISO18000_6C_WRITE;
							toService.putExtra("cmd", cmdCode);
							// 写标签参数：访问密码、数据区、起始地址、写入数据的长度、数据
							toService
									.putExtra("accessPassword", accessPassword);
							// toService.putExtra("dataLen", writeDataLen);
							toService.putExtra("dataBytes", dataBytes);
							startService(toService);

						}
					});
			dialog.show();
			db.close();
		}
	}

	private void showDialog_Layout(MainActivity context) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(this); // 间接显示
		final View textEntryView = inflater.inflate(R.layout.clearpage, null);
		final CheckBox selectcangchu = (CheckBox) textEntryView
				.findViewById(R.id.selectcangchu);
		selectcangchu.setClickable(false);
		final CheckBox selectzichan = (CheckBox) textEntryView
				.findViewById(R.id.selectzichan);
		selectzichan.setClickable(false);
		if (CurrentUser.userType == 3 || CurrentUser.userType == 1
				|| CurrentUser.userType == 2) {
			selectzichan.setClickable(true);
		}
		// 判断是否有数据
		if (CurrentUser.userType == 4 || CurrentUser.userType == 1
				|| CurrentUser.userType == 2) {
			selectcangchu.setClickable(true);

		}
		// 资产多选框选中状态改变事件
		selectcangchu.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					clearcangchu = true;
				}
			}
		});
		// 仓储多选框选中状态改变事件
		selectzichan.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					clearzichan = true;
				}
			}
		});
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.dialogicon);
		builder.setTitle("清空数据");
		builder.setView(textEntryView);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialogs = new ProgressDialog(getApplicationContext());
				dialogs = ProgressDialog.show(MainActivity.this, "Clearing...",
						"Please wait...", true, false);
				new Thread() {
					public void run() {
						// 需要花时间计算的方法
						Looper.prepare();
						deletedata(clearcangchu, clearzichan);
						/*
						 * try { Thread.sleep(500); } catch
						 * (InterruptedException e) { // TODO Auto-generated
						 * catch block System.out.println("延时500毫秒"); }
						 */
						// 向handler发消息
						handler.sendEmptyMessage(0);

						Looper.loop();
					}
				}.start();

			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setTitle("");
			}
		});
		builder.show();
		clearcangchu = false;
		clearzichan = false;
	}

	// 清空数据方法
	public void deletedata(boolean clearcangchu, boolean clearzichan) {
		db.open();
		// 循环删除，并判断是否为空
		if (clearcangchu) {
			boolean result = false;
			// 循环删除
			for (int i = 0; i < TableNameStrings.getStockTables().size(); i++) {

				if (!TableNameStrings.getStockTables().get(i).detail.equals("")) {
					db.delete(TableNameStrings.getStockTables().get(i).main);
					db.delete(TableNameStrings.getStockTables().get(i).detail);
				}
			}
			Cursor cursormain = null;
			Cursor cursordetail = null;
			for (int i = 0; i < TableNameStrings.getStockTables().size(); i++) {

				if (!TableNameStrings.getStockTables().get(i).detail.equals("")) {
					cursormain = db.getAllTitles(TableNameStrings
							.getStockTables().get(i).main);
					cursordetail = db.getAllTitles(TableNameStrings
							.getStockTables().get(i).detail);
				}

				if (cursormain.moveToFirst() != false
						|| cursordetail.moveToFirst() != false) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			if (result) {
				Toast.makeText(this, "删除仓储数据成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "删除仓储数据失败", Toast.LENGTH_SHORT).show();
			}
		}
		// 循环删除，并判断是否为空
		if (clearzichan) {
			boolean result = false;
			// 循环删除
			for (int i = 0; i < TableNameStrings.getAssetTables().size(); i++) {

				if (!TableNameStrings.getAssetTables().get(i).detail.equals("")) {
					db.delete(TableNameStrings.getAssetTables().get(i).main);
					db.delete(TableNameStrings.getAssetTables().get(i).detail);
				}
			}
			Cursor cursormain = null;
			Cursor cursordetail = null;
			for (int i = 0; i < TableNameStrings.getAssetTables().size(); i++) {
				if (!TableNameStrings.getAssetTables().get(i).detail.equals("")) {
					cursormain = db.getAllTitles(TableNameStrings
							.getAssetTables().get(i).main);
					cursordetail = db.getAllTitles(TableNameStrings
							.getAssetTables().get(i).detail);
				}
				if (cursormain.moveToFirst() != false
						|| cursordetail.moveToFirst() != false) {
					result = false;
					break;
				} else {
					result = true;
				}
			}
			if (result) {
				Toast.makeText(this, "删除资产数据成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "删除资产数据失败", Toast.LENGTH_SHORT).show();
			}

		}

		db.close();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		boolean result = true;
		switch (v.getId()) {
		case R.id.cangChuManager:// 仓储管理
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				cangchuManager.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				cangchuManager.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.storageScan:// 资产管理
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				ziChanManager.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				ziChanManager.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.shuJuDownload:// 数据下载
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				shuJuDownload.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				shuJuDownload.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cleardata:// 数据下载
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				cleardata.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				cleardata.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.readwritetag:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				readwritetag.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				readwritetag.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		}
		return result;
	}

	/*
	 * 初始化数据库
	 */
	public boolean initDatabase() {
		db.open();
		boolean result = false;
		// Cursor cursor = db.getAllTitles("RecodeUpdateTime");
		Cursor cursor = null;
		for (int i = 0; i < TableNameStrings.tablenames.length; i++) {
			String date = "2011-05-21 10:37:12";
			cursor = db.getAllByDate(TableNameStrings.tablenames[i],
					"UpdateDateTime");
			if (cursor.moveToFirst()) {
				date = cursor
						.getString(cursor.getColumnIndex("UpdateDateTime"));
			}
			if (date == null) {
				date = "2011-05-21 10:37:12";
			}
			try {
				result = getRequestData(TableNameStrings.tablenames[i], date);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("下载异常：" + e.toString());
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				System.out.println("下载格式异常：" + e.toString());
			}
			// downloadByTables(TableNameStrings.tablenames[i], requestget,
			// date);
		}
		cursor.close();
		db.close();
		return result;
	}

	/**
	 * 基于ksoap2调用ASP.netwebservice
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private boolean getRequestData(String example, String date)
			throws IOException, XmlPullParserException {
		boolean result = false;
		// 命名空间
		final String serviceNameSpace = "http://tempuri.org/";
		// final String example = "SpecificationsInfo";
		// 请求URL
		// final String serviceURL =
		// "http://192.168.1.150:8080/GetService.asmx";
		final String serviceURL = "http://" + Constants.ip + ":"
				+ Constants.port + "/GetService.asmx";
		// 实例化SoapObject对象,指定webService的命名空间以及调用方法的名称
		SoapObject request = new SoapObject(serviceNameSpace, example);
		// example方法中有一个String的参数，这里将“android client”传递到example中
		request.addProperty("entityType", example);
		request.addProperty("date", date);
		// 获得序列化的Envelope
		SoapSerializationEnvelope envelope;
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.bodyOut = request;
		envelope.dotNet = true;
		// Android传输对象
		HttpTransportSE transport = new HttpTransportSE(serviceURL);
		transport.debug = true;
		try {
			// 调用WebService
			transport.call(serviceNameSpace + example, envelope);
			if (envelope.getResponse() != null) {
				SoapObject object = (SoapObject) envelope.bodyIn;

				byte[] resultstream = Base64.decode(object.getProperty(0)
						.toString());
				resultstream = MD5Utility.unGZip(resultstream);
				// base64解码
				String strResult = new String(resultstream, "UTF-8");
				JSONArray jsonArray = null;
				// 判断获取数据是否为空
				if (strResult == null || strResult.equals("null")) {
					return false;
				} else {
					jsonArray = new JSONArray(strResult); // 数据直接为一个数组形式，所以可以直接
															// 用android提供的框架JSONArray读取JSON数据，转换成Array
				}

				if (jsonArray.isNull(0)) {
					return false;
				}
				// db.open();
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jobject = (JSONObject) jsonArray.get(i);
					ContentValues content = new ContentValues();
					// 此处调用类方法，返回ContentValues对象,参数为content和table
					content = ConvertToContentvalues.convertToClass(jobject,
							example);
					String key = jobject.getString("Key");
					Cursor cursors = null;
					try {

						cursors = db.getAllTitles(example, "Key='" + key + "'",
								0);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int resultInt = 0;
					if (cursors.moveToFirst()) {// 判断下载的数据是更新还是新增
						resultInt = db.updateData(example, content, "Key",
								new String[] { key });
					} else {
						db.insert(content, example);
					}
				}

			} else {
				// 测试用
				Toast.makeText(MainActivity.this, example + "获取失败！",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			result = true;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("数据下载异常");
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	/*
	 * 检验写入的数据是否是十六进制
	 */
	private boolean checkWriteData(String data) {
		boolean flag = false;
		String reg = "[a-f0-9A-F]+";
		flag = Pattern.matches(reg, data);
		return flag;
	}

	/*
	 * 检验密码是否输入正确
	 */
	private boolean checkPassword(String password) {
		boolean flag = false;
		String reg = "[a-f0-9A-F]{8}";
		flag = Pattern.matches(reg, password);
		return flag;
	}

	// 广播接收者
	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 服务返回的数据
			// String recvString = intent.getStringExtra("result");
			boolean writeFlag = intent.getBooleanExtra("writeFlag", false);
			switch (cmdCode) {
			case Constants.CMD_ISO18000_6C_WRITE:
				if (writeFlag) {
					play(1, 0);
					final ContentValues contentvalues = new ContentValues();
					contentvalues.put("BarCode", barcode);
					contentvalues.put("CreateOperater",
							CurrentUser.CurrentUserGuid);
					new Thread() {
						public void run() {
							db.open();
							int result = (int) db.insert(contentvalues,
									"RecodeTag");
							db.close();
							Looper.prepare();
							if (result > 0) {
								Toast.makeText(MainActivity.this, "写入数据成功",
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(MainActivity.this, "写入数据失败",
										Toast.LENGTH_SHORT).show();
							}
							Looper.loop();
						}
					}.start();

				} else {
					Toast.makeText(getApplicationContext(), "写入数据失败",
							Toast.LENGTH_SHORT).show();
				}

				break;
			default:
				break;
			}
		}
	}

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(MainActivity.this, "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				SysApplication.getInstance().exit();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
