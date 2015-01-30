package com.example.sddsd_uhf;

import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.example.UHFService.SysApplication;
import com.example.bean.UserInfo;
import com.example.common.Constants;
import com.example.common.CurrentUser;
import com.example.common.ImageButtonTemplate;
import com.example.dao.DBAdapter;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener,
		OnTouchListener {

	private Button connectnet;
	private Button logginButton;
	private EditText loginName;
	private EditText loginPwd;
	private ImageButtonTemplate setip;
	private CheckBox remember;
	private Properties properties;// 保存登录配置信息
	private ProgressDialog pd;// 访问接口等待进度条
	private EditText edtInputip, edtInputport;

	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		setContentView(R.layout.activity_login);
		SysApplication.getInstance().addActivity(this);
		// DBAdapter db = new DBAdapter(this);
		// db.open();
		// db.close();
		db = new DBAdapter(this);
		initWigit();
	}

	class myOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, MainActivity.class);
			startActivity(intent);
		}
	}

	private void initWigit() {
		loginName = (EditText) findViewById(R.id.login_username);
		loginPwd = (EditText) findViewById(R.id.login_password);
		logginButton = (Button) findViewById(R.id.login_button);
		remember = (CheckBox) findViewById(R.id.remember);

		logginButton.setOnClickListener(this);
		logginButton.setOnTouchListener(this);
		setip = (ImageButtonTemplate) findViewById(R.id.setIp);
		setip.setOnClickListener(this);
		SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
		String name_str = remdname.getString("name", "");
		String pwd_str = remdname.getString("pwd", "");
		Constants.ip = remdname.getString("ip", "");
		Constants.port = remdname.getString("port", "");
		loginName.setText(name_str);
		loginPwd.setText(pwd_str);

		if (!pwd_str.equals("")) {
			remember.setChecked(true);
		}
		// logginButton.setOnClickListener(new myOnclickListener());
		loginName.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (!hasFocus) {
					String login = loginName.getText().toString().trim();
					if (login.length() < 4) {
						Toast.makeText(LoginActivity.this, "用户名长度不能小于4",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		remember.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
				SharedPreferences.Editor edit = remdname.edit();
				if (isChecked) {
					edit.putString("pwd", loginPwd.getText().toString().trim());
					edit.commit();
				} else {
					edit.putString("pwd", "");
					edit.commit();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.login_button:
			// 记住用户名
			SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
			SharedPreferences.Editor edit = remdname.edit();
			edit.putString("name", loginName.getText().toString().trim());
			edit.commit();
			if (Constants.ip.equals("") || Constants.port.equals("")
					|| Constants.port == null || Constants.ip == null) {
				Toast.makeText(this, "请先设置IP或端口号！", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!isNetworkConnected(this)) {
				// setNetworkMethod(this);
				login();
			} else if (checkEdit()) {
				db.open();
				Cursor usersor = db.getAllTitles("UserInfo");

				if (!usersor.moveToFirst()) {
					Toast.makeText(this, "当前还没有数据，请登录后下载数据！",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(LoginActivity.this,
							MainActivity.class);
					Bundle bundleSimple = new Bundle();
					bundleSimple.putBoolean("IsHaveData", false);
					intent.putExtras(bundleSimple);
					db.close();
					startActivity(intent);
				} else {
					loginASP();
				}
			}
			break;
		case R.id.setIp:
			// showAlertDialog();
			// setipport.setVisibility(setipport.VISIBLE);
			showDialog_Layout(this);
			break;
		}
	}

	// 显示基于Layout的AlertDialog
	private void showDialog_Layout(Context context) {
		LayoutInflater inflater = LayoutInflater.from(this); // 间接显示
		final View textEntryView = inflater.inflate(R.layout.menu_item, null);
		final EditText edtInputip = (EditText) textEntryView
				.findViewById(R.id.edtInputip);
		final EditText edtInputport = (EditText) textEntryView
				.findViewById(R.id.edtInputport);
		SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
		String ip_str = remdname.getString("ip", "");
		String port_str = remdname.getString("port", "");
		edtInputip.setText(ip_str);
		edtInputport.setText(port_str);
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setIcon(R.drawable.dialogicon);
		builder.setTitle("设置IP和端口号");
		builder.setView(textEntryView);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// setTitle(edtInputip.getText());
				// setTitle(edtInputport.getText());
				SharedPreferences remdname = getPreferences(Activity.MODE_PRIVATE);
				SharedPreferences.Editor edit = remdname.edit();
				edit.putString("ip", edtInputip.getText().toString().trim());
				edit.putString("port", edtInputport.getText().toString().trim());
				edit.commit();
				Constants.ip = remdname.getString("ip", "");
				Constants.port = remdname.getString("port", "");
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setTitle("");
			}
		});
		builder.show();
	}

	private void loginASP() {
		HttpPost requestget = new HttpPost("http://" + Constants.ip + ":"
				+ Constants.port + "/GetService.asmx/Login");
		requestget.setHeader("Accept", "application/json");
		requestget.addHeader("Content-Type", "application/json; charset=utf-8");

		JSONObject jsonParams = new JSONObject();
		try {
			jsonParams.put("loginName", loginName.getText().toString().trim());// 登录名
			jsonParams.put("loginPwd", loginPwd.getText().toString().trim());// 密码
			HttpEntity bodyEntity = new StringEntity(jsonParams.toString(),
					"utf8");

			requestget.setEntity(bodyEntity);
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse responsepost = client.execute(requestget);
			if (responsepost.getStatusLine().getStatusCode() == 200) {
				String result = EntityUtils.toString(responsepost.getEntity());
				Boolean resultobj = (Boolean) new JSONObject(result).get("d");
				if (resultobj == true) {
					DBAdapter db = new DBAdapter(this);
					db.open();
					Cursor cursor = db.getUserInfoByCondition("UserInfo",
							"LoginID", loginName.getText().toString().trim());
					UserInfo user = new UserInfo();
					user.Key = cursor.getString(cursor.getColumnIndex("Key"));
					user.UserType = cursor.getInt(cursor
							.getColumnIndex("UserType"));
					db.close();
					Toast.makeText(LoginActivity.this, "登录成功",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(LoginActivity.this,
							MainActivity.class);
					CurrentUser.CurrentUserGuid = user.Key;
					CurrentUser.userType = user.UserType;
					startActivity(intent);
				} else {
					Toast.makeText(LoginActivity.this, "登录名或密码错误！",
							Toast.LENGTH_SHORT).show();
				}

			} else {
				// 测试用
				Toast.makeText(LoginActivity.this, "连接服务器失败，请检查网络！",
						Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(LoginActivity.this, "登录名或密码错误！", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void login() {
		db.open();
		Cursor usersor = db.getAllTitles("UserInfo");
		if (!usersor.moveToFirst()) {
			Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
			/*
			 * Intent intent = new Intent(LoginActivity.this,
			 * MainActivity.class); startActivity(intent);
			 */
		} else {
			Cursor cursor = db.getUserInfoByCondition("UserInfo", "LoginID",
					loginName.getText().toString().trim());
			String pwd = loginPwd.getText().toString().trim();
			UserInfo user = new UserInfo();
			user.Key = cursor.getString(cursor.getColumnIndex("Key"));
			user.LoginPwd = cursor.getString(cursor.getColumnIndex("LoginPwd"));
			user.UserType = cursor.getInt(cursor.getColumnIndex("UserType"));
			db.close();
			if (user.LoginPwd.toString().trim().equals(pwd)) {
				Toast.makeText(this, "当前网络不可用,数据无法交互", Toast.LENGTH_LONG)
						.show();
				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);
				CurrentUser.CurrentUserGuid = user.Key;
				CurrentUser.userType = user.UserType;
				startActivity(intent);
			}
		}
	}

	private boolean checkEdit() {
		// TODO Auto-generated method stub
		if (loginName.getText().toString().trim().equals("")) {
			Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT)
					.show();
		}
		if (loginPwd.getText().toString().trim().equals("")) {
			Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT)
					.show();
		} else {
			return true;
		}
		return false;
	}

	// 判断当前网络是否可用
	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
			/*
			 * if (mConnectivityManager == null) { } else { NetworkInfo[] info =
			 * mConnectivityManager.getAllNetworkInfo(); if (info != null) { for
			 * (int i = 0; i < info.length; i++) { if (info[i].getState() ==
			 * NetworkInfo.State.CONNECTED) { return true; } } } } return false;
			 */
		}
		return false;
	}

	/*
	 * 打开设置网络界面
	 */
	public static void setNetworkMethod(final Context context) {
		// 提示对话框
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("网络设置提示")
				.setMessage("网络连接不可用,是否进行设置?")
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = null;
						// 判断手机系统的版本 即API大于10 就是3.0或以上版本
						if (android.os.Build.VERSION.SDK_INT > 10) {
							intent = new Intent(
									android.provider.Settings.ACTION_WIFI_SETTINGS);
						} else {
							intent = new Intent();
							ComponentName component = new ComponentName(
									"com.android.settings",
									"com.android.settings.WifiSettings");
							intent.setComponent(component);
							intent.setAction("android.intent.action.VIEW");
						}
						context.startActivity(intent);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).show();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		boolean result = true;
		switch (v.getId()) {
		case R.id.login_button:// 仓储管理
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				logginButton.setBackgroundColor(Color.rgb(111, 166, 234));
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				logginButton.setBackgroundColor(Color.parseColor("#CD6839"));
			}
			result = false;
			break;
		}
		return result;
	}

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			System.exit(0);
		}
		return super.onKeyDown(keyCode, event);
	}

}
