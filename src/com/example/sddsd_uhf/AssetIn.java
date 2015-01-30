package com.example.sddsd_uhf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
import com.example.bean.TID;
import com.example.common.Constants;
import com.example.dao.DBAdapter;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AssetIn extends Activity implements OnClickListener {

	private Button scanButton;
	private EditText barcode;
	private TextView materialname;//产品名称
	private TextView materialmodel;//产品规格型号
	private TextView materialtype;//产品类别
	private TextView provider;//产品供应商
	private TextView theuser;//产品使用人
	private TextView belongid;//产品所属
	private TextView assetstate;//产品状态
	private TextView outfactorynum;//出厂编号
	private TextView outfactorydate;//出厂日期
	private TextView factorCreator;//出厂日期
	private Button selectinfo;
	private String recvString;// 调用读卡服务返回的结果
	private ListView listViewData;// listview显示数据列表
	private MediaPlayer player;
	private boolean startFlag = false;
	private int cmdCode;
	private MyReceiver myReceiver = null;// 广播接收者
	private List<Map<String, Object>> listMap;// listview数据源
	private List<TID> listTID;// TID集合
	private final String activity = "com.example.sddsd_uhf.AssetIn";
	DBAdapter db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_asset_in);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(AssetIn.this,
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
		scanButton=(Button) findViewById(R.id.scanButton);
		selectinfo=(Button) findViewById(R.id.selectinfo);
		barcode=(EditText) findViewById(R.id.barcode);
		materialname=(TextView) findViewById(R.id.materialname);
		materialmodel=(TextView) findViewById(R.id.materialmodel);
		materialtype=(TextView) findViewById(R.id.materialtype);
		provider=(TextView) findViewById(R.id.provider);
		theuser=(TextView) findViewById(R.id.theuser);
		belongid=(TextView) findViewById(R.id.belongid);
		assetstate=(TextView) findViewById(R.id.assetstate);
		outfactorynum=(TextView) findViewById(R.id.outfactorynum);
		outfactorydate=(TextView) findViewById(R.id.outfactorydate);
		factorCreator=(TextView)findViewById(R.id.factorycreator);
		scanButton.setOnClickListener(this);
		//scanButton.setClickable(false);
		selectinfo.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.asset_in, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 给服务发送广播，内容为当前activity
				Intent toService = new Intent(AssetIn.this, UhfService.class);
				Intent ac = new Intent();
				ac.setAction("com.example.UHFService.UhfService");
				ac.putExtra("activity", activity);
				sendBroadcast(ac);
				cmdCode = 0;
				db.open();
		switch(v.getId()){
		case R.id.scanButton:
			clearInfo();
			cmdCode = Constants.CMD_ISO18000_6C_READ;
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("cmd", cmdCode);
			toService.putExtra("startFlag", startFlag);
			startService(toService);
			break;
		case R.id.selectinfo:
			selectFromdatabase();
			break;
		}
		db.close();
	}
	private void clearInfo() {
		// TODO Auto-generated method stub
		materialname.setText("");
		materialmodel.setText("");
		materialtype.setText("");
		belongid.setText("");
		assetstate.setText("");
		provider.setText("");
		theuser.setText("");
		factorCreator.setText("");
		outfactorynum.setText("");
		outfactorydate.setText("");
	}

	private void selectFromdatabase() {
		// TODO Auto-generated method stub
		db.open();
		String barcodeNum=barcode.getText().toString().trim();
		if (barcodeNum.equals("")) {
			return;
		}
		Cursor cursor1=db.getTitle("AssetDetail", new String[]{"MaterialID","SpecificationsID","AssetState","BelongID"}, "BarCode", barcodeNum);
		if(cursor1.moveToFirst()){
			String materialkey=cursor1.getString(cursor1.getColumnIndex("MaterialID"));
			materialname.setText(db.getNameBykey("MaterialInfo",materialkey));
			String specificationkey=cursor1.getString(cursor1.getColumnIndex("SpecificationsID"));
			materialmodel.setText(db.getNameBykey("SpecificationsInfo",specificationkey));
			//String materialTypekey=cursor1.getString(cursor1.getColumnIndex("SpecificationsID"));
			Cursor modelkey=db.getTitle("AssetInDetail", new String[]{"MaterialModelID"}, "MaterialID", materialkey);
			if (modelkey.moveToFirst()) {
				String s=db.getNameBykey("MaterialModelInfo",modelkey.getString(0));
				materialtype.setText(s);
				
			}
			String belongkey=cursor1.getString(cursor1.getColumnIndex("BelongID"));
			belongid.setText(db.getNameBykey("BranchCompanyInfo",belongkey));
			int assetState=cursor1.getInt(cursor1.getColumnIndex("AssetState"));
			if (assetState==1) {
				assetstate.setText("在库");
			}
			if (assetState==2) {
				assetstate.setText("领用");
			}
			if (assetState==3) {
				assetstate.setText("报停");
			}
			if (assetState==4) {
				assetstate.setText("报废");
			}
		
			
		}
		Cursor cursor2=db.getTitle("AssetInDetail", new String[]{"ProviderID","FactoryInformation","CleverID","ManufacturerID","FactoryDateTime"}, "BarCode", barcodeNum);
		if(cursor2.moveToFirst()){
			String providekey=cursor2.getString(cursor2.getColumnIndex("ProviderID"));
			provider.setText(db.getNameBykey("ProviderInfo",providekey));
			String theuserkey=cursor2.getString(cursor2.getColumnIndex("CleverID"));
			theuser.setText(db.getNameBykey("CleverInfo",theuserkey));
			String manufacturerkey=cursor2.getString(cursor2.getColumnIndex("ManufacturerID"));
			factorCreator.setText(db.getNameBykey("ProviderInfo",manufacturerkey));
			
			String factorCreatornum=cursor2.getString(cursor2.getColumnIndex("FactoryInformation"));
			outfactorynum.setText(factorCreatornum);
			String factorCreatordate=cursor2.getString(cursor2.getColumnIndex("FactoryDateTime"));
			outfactorydate.setText(factorCreatordate);
		}
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
			 intent.setClass(AssetIn.this,ZiChanManagerActivity.class);
	            startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
