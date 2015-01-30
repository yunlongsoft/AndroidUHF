package com.example.sddsd_uhf;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.UHFService.SysApplication;
import com.example.bean.IListTable;
import com.example.bean.StockDetail;
import com.example.common.Constants;
import com.example.common.ImageButtonTemplate;
import com.example.common.TableNameStrings;
import com.example.dao.DBAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class CangChuManagerActivity extends Activity implements
		OnClickListener, OnTouchListener {
	ImageButtonTemplate chukuButton;
	ImageButtonTemplate tuikuButton;
	ImageButtonTemplate pandianButton;
	ImageButtonTemplate diaoboButton;
	ImageButtonTemplate uploadButton;
	ImageButtonTemplate rukuButton;
	ImageButtonTemplate cangchuquery;
	ProgressDialog dialogs;
	private boolean Confirm;
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cang_chu_manager);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		initActivity();

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
	/**
	 * 用Handler来更新UI
	 */
	private Handler handlerToast = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (Confirm) {
				Toast.makeText(CangChuManagerActivity.this, "数据全部上传成功！",
						Toast.LENGTH_SHORT).show();
			}

		}
	};

	private void initActivity() {
		// TODO Auto-generated method stub
		chukuButton = (ImageButtonTemplate) findViewById(R.id.cangChu_chuku);
		tuikuButton = (ImageButtonTemplate) findViewById(R.id.cangChu_tuiku);
		pandianButton = (ImageButtonTemplate) findViewById(R.id.cangChu_pandian);
		diaoboButton = (ImageButtonTemplate) findViewById(R.id.cangChu_diaobo);
		uploadButton = (ImageButtonTemplate) findViewById(R.id.cangChu_upload);
		rukuButton = (ImageButtonTemplate) findViewById(R.id.cangchuin);
		cangchuquery=(ImageButtonTemplate) findViewById(R.id.cangchuquery);
		// 点击事件
		chukuButton.setOnClickListener(this);
		tuikuButton.setOnClickListener(this);
		pandianButton.setOnClickListener(this);
		diaoboButton.setOnClickListener(this);
		uploadButton.setOnClickListener(this);
		rukuButton.setOnClickListener(this);
		cangchuquery.setOnClickListener(this);

		// 触摸事件
		chukuButton.setOnTouchListener(this);
		tuikuButton.setOnTouchListener(this);
		pandianButton.setOnTouchListener(this);
		diaoboButton.setOnTouchListener(this);
		uploadButton.setOnTouchListener(this);
		rukuButton.setOnTouchListener(this);
		cangchuquery.setOnTouchListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cang_chu_manager, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.cangChu_pandian:
			intent.setClass(CangChuManagerActivity.this, PanDianActivity.class);
			startActivity(intent);
			break;
		case R.id.cangChu_diaobo:
			intent.setClass(CangChuManagerActivity.this, CangChuDiaoBo.class);
			startActivity(intent);
			break;
		case R.id.cangChu_upload:
			showDialog(this);
			break;
		case R.id.cangChu_tuiku:
			intent.setClass(CangChuManagerActivity.this, CangChuBack.class);
			startActivity(intent);
			break;
		case R.id.cangChu_chuku:
			intent.setClass(CangChuManagerActivity.this,
					CangChuOutManager.class);
			startActivity(intent);
			break;
		case R.id.cangchuin:
			intent.setClass(CangChuManagerActivity.this, CangChuIn.class);
			startActivity(intent);
			break;
		case R.id.cangchuquery:
			intent.setClass(CangChuManagerActivity.this, CangChuQuery.class);
			startActivity(intent);
			break;
		}
	}

	private void showDialog(Context context) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle("确认");
		builder.setMessage("确定要上传到服务器吗？");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialogs = new ProgressDialog(getApplicationContext());
				dialogs = ProgressDialog.show(CangChuManagerActivity.this,
						"Loading...", "Please wait...", true, false);
				// 新建线程
				new Thread() {
					@Override
					public void run() {
						// 需要花时间计算的方法
						Looper.prepare();
						Confirm = upLoad();
						handler.sendEmptyMessage(0);
						Looper.loop();
					}
				}.start();
			}
		});
		builder.setNegativeButton("否", null);
		builder.show();
	}

	/*
	 * 当触摸按钮时所激发的动作
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		boolean result = true;
		switch (v.getId()) {
		case R.id.cangChu_pandian:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				pandianButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				pandianButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangChu_diaobo:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				diaoboButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				diaoboButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangChu_upload:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				uploadButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				uploadButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangChu_tuiku:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				tuikuButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				tuikuButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangChu_chuku:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				chukuButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				chukuButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangchuin:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				rukuButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				rukuButton.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		case R.id.cangchuquery:
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				cangchuquery.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				cangchuquery.setBackgroundColor(Color.TRANSPARENT);
			}
			result = false;
			break;
		}
		return false;
	}

	/*
	 * 将仓储管理中的数据上传至服务器，在这里写逻辑
	 */
	public boolean upLoad() {
		db.open();
		String main = new String();
		int successCount = 0;
		for (int i = 0; i < TableNameStrings.getStockTables().size(); i++) {
			Cursor maincursor = null;
			IListTable maintable = TableNameStrings.getStockTables().get(i);
			try {
				maincursor = db.getAllTitles(maintable.main);// 根据表名，获取该表的所有记录
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (maincursor.moveToFirst()) {
				do {
					JSONObject jsonObj = new JSONObject(); // 接收主表的字符串
					JSONArray arrya = new JSONArray();// 用来接收明细表列表
					for (int j = 0; j < maincursor.getColumnNames().length; j++) {
						String colum = maincursor.getColumnNames()[j];
						String value = maincursor.getString(j);
						try {
							jsonObj.put(colum, value);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					main = jsonObj.toString();// 将主表Json对象转换为json字符串
					int resultInt=0;
					Cursor detailcursor = null;// 声明一个明细表的接收集合
					String mainkey = maincursor.getString((maincursor
							.getColumnIndex("Key")));
					if (!maintable.detail.equals("")) { // 判断detail是否为空
						detailcursor = db.getAllTitles(maintable.detail,
								maintable.main + "ID='"+ mainkey+"'",0);
						if (detailcursor.moveToFirst()) {

							do {
								JSONObject jsonObjDetial = new JSONObject();// 创建Json对象，用来接收明细表单表
								for (int j = 0; j < detailcursor
										.getColumnNames().length; j++) {
									String colum = detailcursor
											.getColumnNames()[j];
									String value = detailcursor.getString(j);
									try {
										jsonObjDetial.put(colum, value);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										Log.e("jsonObjDetial.put(colum, value)",
												e.toString());
									}
								}
								arrya.put(jsonObjDetial);

							} while (detailcursor.moveToNext());
						}
						detailcursor.close();// 关闭游标，释放资源
						resultInt = singleUpload(main, arrya.toString(),//调用单步上传
								maintable.main);
						if (resultInt == 1) {
							successCount++;
						}
					}else{
						resultInt = singleUpload(main, "",//调用单步上传
								maintable.main);
						if (resultInt == 1) {
							successCount++;
						}
					}
				} while (maincursor.moveToNext());
			}
			maincursor.close();// 关闭游标，释放资源
		}
		db.close();// 关闭数据库
		if (successCount > 1) {
			Toast.makeText(CangChuManagerActivity.this, "数据上传成功！",
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	// 单步上传
	private int singleUpload(String mainInfo, String detaialList, String table) {

		//先配置服务器请求参数
		HttpPost requestget = new HttpPost("http://" + Constants.ip + ":"
				+ Constants.port + "/SetService.asmx/insert" + table);
		requestget.setHeader("Accept", "application/json");
		requestget.addHeader("Content-Type", "application/json; charset=utf-8");

		JSONObject jsonParams = new JSONObject();//声明一个json对象，用来装载参数
		try {
			jsonParams.put("mainInfo", mainInfo);//参数名要和服务器接口一样，主表
			jsonParams.put("detaialList", detaialList);// 从表，详单
			HttpEntity bodyEntity = new StringEntity(jsonParams.toString(),
					"utf8");
			requestget.setEntity(bodyEntity);
			DefaultHttpClient client = new DefaultHttpClient();//声明
			HttpResponse responsepost = client.execute(requestget);
			if (responsepost.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(responsepost.getEntity());
				int resultobj = new JSONObject(result).getInt("d");
				// String pwd= resultobj.getString("LoginPwd");
				// 判断返回值
				if (resultobj > 0) {
					return 1;
				}
			} else {
				// 测试用
				Toast.makeText(this, "访问服务器失败！", Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("上传仓储失败！");
		}
		return 0;
	}

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(CangChuManagerActivity.this, MainActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
