package com.example.sddsd_uhf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.UHFService.SysApplication;
import com.example.UHFService.UhfService;
import com.example.bean.AccountCompanyInfo;
import com.example.bean.EPC;
import com.example.bean.MaterialInfo;
import com.example.bean.MaterialModelInfo;
import com.example.bean.ProviderInfo;
import com.example.bean.SpecificationsInfo;
import com.example.common.Constants;
import com.example.common.CreateGuid;
import com.example.common.CurrentUser;
import com.example.dao.DBAdapter;

public class AssetGatherActivity extends Activity implements
		OnItemSelectedListener {
	private String providerkey;// 供应商主键值
	private String belongkey = "00000000-0000-0000-0000-000000000000";// 所属者主键值初始化
	private String materialkey;// 产品主键值
	private String materialTypekey;// 产品类别主键值
	private String procurementkey;// 采购者主键值
	private String specificationskey = "";// 规格型号主键值
	private String newproviderkey;// 生产厂家主键值
	private String recvString;// 调用读卡服务返回的结果
	private String departmentidkey = "00000000-0000-0000-0000-000000000000";// 部门主键
	private String providerspinnerdialogkey;// 供应商主键
	private Button saveSetButton;//保存按钮
	private Button setBaseInfo;// 设置基本信息
	private Button buttonSave;// 保存采集信息
	private Button buttonClear; // 清空按钮
	private ImageButton imagebuttonadd;// 给下拉列表添加新的选择项
	private ImageButton provideradd;//给供应商下拉列表添加新的选项
	private ImageButton materialnameadd;//给设备名称添加新的选择项
	private Button addButton;//添加按钮
	private ListView listViewData;// listview显示数据列表
	private Button dateSelectButton;//选择
	private TextView dateselect;
	// private Spinner Statespinner;//产品状态选择框
	private Spinner typespinner;// 库存类型选择框
	private Spinner specifications;// 规格型号
	// private Spinner Belongspinner;// 下拉列表所属者
	private Spinner materialspinner;// 产品下来列表
	private Spinner materialTypespinner;// 产品类别下拉列表
	private Spinner procurementspinner;// 采购者列表
	private AutoCompleteTextView providerspinner;// 供应商下拉列表
	// private Spinner clevers;
	private AutoCompleteTextView newprovider;// 生产厂家
	// private Spinner departmentid;// 使用部门
	private Spinner providerspinnerdialog;// 弹出框选择

	private String states;// 接收选择的库存类型
	private int assettypes;// 接收选择的库存类型
	// private int types;//接收选择的产品状态
	private EditText invoiceNumber;// 发票信息
	private EditText factoryInformation;// 出厂信息
	private EditText remark;// 备注
	private EditText barcode;// 获取标签号
	private List<Map<String, Object>> listMap;// listview数据源
	private List<EPC> listEPC;// EPC集合
	private boolean startFlag = false;
	private MyReceiver myReceiver = null;// 广播接收者
	private int cmdCode;
	// private EditText markPriceText;
	private EditText depreCiation;// 使用年限
	private EditText price;// 价格
	private LinearLayout layoutinvoic;
	private MediaPlayer player;
	List<ContentValues> listIndetail = new ArrayList<ContentValues>(); // 入库明细列表
	List<ContentValues> listdetail = new ArrayList<ContentValues>(); // 在库明细列表
	private Calendar c = null;
	private final String activity = "com.example.sddsd_uhf.AssetGatherActivity";
	DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_asset_gather);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		// 启动服务
		Intent startServer = new Intent(this, UhfService.class);
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

		setBaseInfo = (Button) findViewById(R.id.setbaseInfo);// 设置资产基本信息
		buttonClear = (Button) findViewById(R.id.button_clear);
		// markscan = (Button) findViewById(R.id.markscan);
		listViewData = (ListView) findViewById(R.id.data_list);
		buttonSave = (Button) findViewById(R.id.button_save);
		// markPriceText = (EditText) findViewById(R.id.markprice);
		dateSelectButton = (Button) findViewById(R.id.dateBtn);
		// markPriceText.setText("0.0");
		dateselect = (TextView) findViewById(R.id.dateSelect);
		imagebuttonadd = (ImageButton) findViewById(R.id.imagebuttonadd);// 实现列表添加新项
		materialnameadd = (ImageButton) findViewById(R.id.materialnameadd);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());// 获取当前时间并格式化
		dateselect.setText(date);
		listEPC = new ArrayList<EPC>();
		listMap = new ArrayList<Map<String, Object>>();
		// Statespinner = (Spinner) findViewById(R.id.spinner_State);
		typespinner = (Spinner) findViewById(R.id.spinner_types);
		// clevers = (Spinner) findViewById(R.id.cleverid);
		providerspinner = (AutoCompleteTextView) findViewById(R.id.spinner_providerid);
		specifications = (Spinner) findViewById(R.id.specificationsid);
		invoiceNumber = (EditText) findViewById(R.id.invoicenumber);
		newprovider = (AutoCompleteTextView) findViewById(R.id.newproviderid);// 生产厂家
		factoryInformation = (EditText) findViewById(R.id.factorynum);
		depreCiation = (EditText) findViewById(R.id.depreciation);
		price = (EditText) findViewById(R.id.price);
		price.setText("0.0");
		remark = (EditText) findViewById(R.id.remark);
		addButton = (Button) findViewById(R.id.addbutton);
		barcode = (EditText) findViewById(R.id.batchNum);
		// departmentid = (Spinner) findViewById(R.id.spinner_departmentid);
		// 加载下拉列表的选项
		providerDropDown();
		newproviderDropDown();
		// belongDropDown();
		materialTypeDropDown();
		procurementDropDown();
		// cleversDropDown();
		// 设置按钮监听
		buttonClear.setOnClickListener(new MyOnClickable());
		// markscan.setOnClickListener(new MyOnClickable());
		buttonSave.setOnClickListener(new MyOnClickable());
		setBaseInfo.setOnClickListener(new MyOnClickable());
		addButton.setOnClickListener(new MyOnClickable());
		dateSelectButton.setOnClickListener(new MyOnClickable());
		imagebuttonadd.setOnClickListener(new MyOnClickable());
		materialnameadd.setOnClickListener(new MyOnClickable());

		ArrayAdapter<CharSequence> adaptertype = ArrayAdapter
				.createFromResource(this, R.array.AsseetTypes,
						android.R.layout.simple_spinner_dropdown_item);
		typespinner.setAdapter(adaptertype);
		// 添加事件Spinner事件监听
		// Statespinner.setOnItemSelectedListener(this);
		typespinner.setOnItemSelectedListener(this);
	}

	// 按钮点击事件
	private class MyOnClickable implements OnClickListener {
		private Intent toService = new Intent(AssetGatherActivity.this,
				UhfService.class);

		@Override
		public void onClick(View v) {
			// 给服务发送广播，内容为当前activity
			Intent ac = new Intent();
			ac.setAction("com.example.UHFService.UhfService");
			ac.putExtra("activity", activity);
			sendBroadcast(ac);
			cmdCode = 0;
			switch (v.getId()) {
			// 清空按钮
			case R.id.button_clear:
				listIndetail.removeAll(listIndetail);
				listdetail.remove(listdetail);
				listViewData.setAdapter(null);
				break;
			case R.id.button_save:
				if (!listIndetail.isEmpty() && checkEdit()) {
					InsertLocalSQL();
				}
				break;
			case R.id.setbaseInfo:
				cmdCode = Constants.CMD_ISO18000_6C_READ;
				toService.putExtra("cmd", cmdCode);
				toService.putExtra("cmd", cmdCode);
				toService.putExtra("startFlag", startFlag);
				startService(toService);
				break;
			case R.id.dateBtn:
				showDialog(0);// 选择时间
				break;
			case R.id.addbutton:
				String barcodetext = barcode.getText().toString().trim();
				if (barcode.getText().toString().trim().length() != 0) {
					Cursor cursor = null;
					db.open();
					try {
						// 查询这个标签是否存在
						cursor = db.getTitle("AssetInDetail",
								new String[] { "BarCode" }, "BarCode",
								recvString);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						Log.d("查询标签", e.toString());
					}
					if (!cursor.moveToFirst()) {
						ContentValues contentvalue = new ContentValues();
						contentvalue.put("Key", CreateGuid.GenerateGUID());
						contentvalue.put("MaterialID", materialkey);
						contentvalue.put("FactoryInformation",
								factoryInformation.getText().toString());
						contentvalue.put("BarCode", recvString);
						contentvalue.put("ProviderID", providerkey);
						contentvalue.put("Price", price.getText().toString()
								.trim());
						contentvalue.put("MaterialModelID", materialTypekey);
						contentvalue.put("Number", 1);
						contentvalue.put("CleverID",
								"00000000-0000-0000-0000-000000000000");// 创建下拉框cleverskey
						contentvalue.put("Remark", remark.getText().toString());// 备注
						contentvalue.put("SpecificationsID", specificationskey);// 创建规格型号，获取id
						contentvalue.put("Depreciation", depreCiation.getText()
								.toString());// 使用年限
						contentvalue.put("WareStates", states);// 库房状态
						contentvalue.put("ManufacturerID", newproviderkey);// 出厂日期
						contentvalue.put("FactoryDateTime", dateselect
								.getText().toString().trim());// 生产厂家

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
						addListView(listIndetail);
						ContentValues contentvaluelist = new ContentValues();
						contentvaluelist.put("Key", CreateGuid.GenerateGUID());
						contentvaluelist.put("BarCode", recvString);
						contentvaluelist.put("AsseetType", assettypes);
						contentvaluelist.put("Quantity", 1);
						contentvaluelist.put("AssetState", 1);//
						contentvaluelist.put("BelongID", belongkey);
						contentvaluelist.put("MaterialID", materialkey);
						contentvaluelist.put("BelongType", 1);
						contentvaluelist.put("SpecificationsID",
								specificationskey);// 创建规格型号，获取id
						contentvaluelist.put("CreateOperater",
								CurrentUser.CurrentUserGuid);
						contentvaluelist.put("UpdateOperater",
								CurrentUser.CurrentUserGuid);
						if (listdetail.isEmpty()) {
							listdetail.add(contentvaluelist);
							Log.e("read tid", listdetail.get(0).get("BarCode")
									.toString());
						} else {
							for (int i = 0; i < listdetail.size(); i++) {
								ContentValues mTID = listdetail.get(i);
								// list中有此EPC
								if (recvString.equals(mTID.get("BarCode")
										.toString())) {
									break;
								} else if (i == (listdetail.size() - 1)) {
									// list中没有此epc
									listdetail.add(contentvaluelist);
								}
							}
						}
						barcode.setText("");
					} else {
						Toast.makeText(AssetGatherActivity.this, "该标签已经存在",
								Toast.LENGTH_SHORT).show();
					}
					cursor.close();
					db.close();
				}
				break;
			case R.id.imagebuttonadd:
				LayoutInflater inflater = LayoutInflater
						.from(AssetGatherActivity.this); // 间接显示
				final View textEntryView = inflater.inflate(R.layout.zichanadd,
						null);
				final EditText specificationsname = (EditText) textEntryView
						.findViewById(R.id.name);
				final EditText specificationsunit = (EditText) textEntryView
						.findViewById(R.id.unit);

				final AlertDialog.Builder builder = new AlertDialog.Builder(
						AssetGatherActivity.this);
				builder.setCancelable(false);
				builder.setIcon(R.drawable.dialogicon);
				builder.setTitle("添加规格型号");
				builder.setView(textEntryView);
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// new Thread(){
								// public void run(){
								// Looper.prepare();
								ContentValues contentvalues = new ContentValues();
								contentvalues.put("Key",
										CreateGuid.GenerateGUID());
								contentvalues.put("MaterialID", materialkey);
								contentvalues.put("Name", specificationsname
										.getText().toString().trim());
								contentvalues.put("Unit", specificationsunit
										.getText().toString().trim());
								contentvalues.put("CreateOperater",
										CurrentUser.CurrentUserGuid);
								db.open();
								if (specificationsname.getText().toString()
										.trim().equals("")
										|| specificationsunit.getText()
												.toString().trim().equals("")) {
									Toast.makeText(AssetGatherActivity.this,
											"名称或单位不能为空", Toast.LENGTH_SHORT)
											.show();
								} else {
									long result = db.insert(contentvalues,
											"SpecificationsInfo");
									if (result > 0) {
										specificationsDropDown(materialkey);
									}
								}
								db.close();
								// Looper.loop();
								// }
								// }.start();

							}
						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								setTitle("");
							}
						});
				builder.show();

				break;
			case R.id.materialnameadd:
				db.open();
				LayoutInflater inflater1 = LayoutInflater
						.from(AssetGatherActivity.this); // 间接显示
				final View textEntryView1 = inflater1.inflate(
						R.layout.addmaterialname, null);
				final EditText materialname = (EditText) textEntryView1
						.findViewById(R.id.materialname);
				final EditText materialprice = (EditText) textEntryView1
						.findViewById(R.id.materialprice);
				/*
				 * providerspinnerdialog = (Spinner) textEntryView1
				 * .findViewById(R.id.providerid); provideradd = (ImageButton)
				 * textEntryView1 .findViewById(R.id.provideradd);
				 */
				// providerDropDown(providerspinnerdialog);
				/*
				 * provideradd.setOnClickListener(new OnClickListener() {
				 * 
				 * @Override public void onClick(View arg0) { // TODO
				 * Auto-generated method stub final EditText inputServer = new
				 * EditText( AssetGatherActivity.this); AlertDialog.Builder
				 * builder = new AlertDialog.Builder( AssetGatherActivity.this);
				 * builder.setTitle("添加供应商") .setIcon(R.drawable.dialogicon)
				 * .setView(inputServer) .setNegativeButton("取消", null);
				 * builder.setPositiveButton("确定", new
				 * DialogInterface.OnClickListener() {
				 * 
				 * public void onClick(DialogInterface dialog, int which) {
				 * ContentValues contentvalues1 = new ContentValues();
				 * contentvalues1.put("Key", CreateGuid.GenerateGUID());
				 * contentvalues1.put("Name", inputServer
				 * .getText().toString().trim());
				 * 
				 * if (inputServer.getText().toString() .trim().equals("")) {
				 * Toast.makeText( AssetGatherActivity.this, "名称能为空",
				 * Toast.LENGTH_SHORT) .show(); } else { long result =
				 * db.insert( contentvalues1, "ProviderInfo"); if (result > 0) {
				 * providerDropDown(providerspinnerdialog); providerDropDown();
				 * } }
				 * 
				 * } }); builder.show(); } });
				 */
				final AlertDialog.Builder builder1 = new AlertDialog.Builder(
						AssetGatherActivity.this);
				builder1.setCancelable(false);
				builder1.setIcon(R.drawable.dialogicon);
				builder1.setTitle("添加设备名称");
				builder1.setView(textEntryView1);
				builder1.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								ContentValues contentvalues = new ContentValues();
								contentvalues.put("Key",
										CreateGuid.GenerateGUID());
								contentvalues.put("MaterialModelID",
										materialTypekey);
								contentvalues.put("Name", materialname
										.getText().toString().trim());
								contentvalues.put("Price", materialprice
										.getText().toString().trim());
								contentvalues.put("ProviderID",
										"00000000-0000-0000-0000-000000000000");
								contentvalues.put("CreateOperater",
										CurrentUser.CurrentUserGuid);
								if (materialname.getText().toString().trim()
										.equals("")
										|| materialprice.getText().toString()
												.trim().equals("")) {
									Toast.makeText(AssetGatherActivity.this,
											"名称或价格不能为空", Toast.LENGTH_SHORT)
											.show();
								} else {
									long result = db.insert(contentvalues,
											"MaterialInfo");
									if (result > 0) {
										materialDropDown(materialTypekey);
										// specificationsDropDown(materialkey);
									}
								}
								db.close();
							}
						});
				builder1.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								setTitle("");
							}
						});
				builder1.show();
				break;
			default:
				break;
			}
		}
	}

	private boolean checkEdit() {
		// TODO Auto-generated method stub
		if (providerspinner.getText().toString().trim().equals("")) {
			Toast.makeText(AssetGatherActivity.this, "供应商不能为空",
					Toast.LENGTH_SHORT).show();
		} else {
			if (newprovider.getText().toString().trim().equals("")) {
				Toast.makeText(AssetGatherActivity.this, "生产厂家不能为空",
						Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}

	/*
	 * 插入本地数据库
	 */
	private void InsertLocalSQL() {
		// TODO Auto-generated method stub
		// StringBuffer sb = new StringBuffer();
		db.open();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// 生成入库单号
		SimpleDateFormat formatNumber = new SimpleDateFormat("yyyyMMddHHmmss");
		String dateBatchnum = formatNumber.format(new Date());// 获取当前时间并格式化
		String BuildBatchnum = "ZC-RKD" + dateBatchnum;

		String date = format.format(new Date());// 获取当前时间并格式化
		String AssetInInfokey = CreateGuid.GenerateGUID();
		ContentValues content = new ContentValues();
		content.put("Key", AssetInInfokey);
		content.put("BatchNumber", BuildBatchnum);
		content.put("BelongID", belongkey);// 新添加项
		content.put("DepartmentID", departmentidkey);
		content.put("InDateTime", date);
		content.put("InvoiceNumber", invoiceNumber.getText().toString());
		content.put("Price", 0);// 总金额
		content.put("PurchaseID", procurementkey);// 采购者
		content.put("CreateOperater", CurrentUser.CurrentUserGuid);
		content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
		// db.insert(content, "AssetInInfo");// 插入入库信息数据库
		long resultInsert = 0;

		// 遍历插入入库明细表
		for (int i = 0; i < this.listIndetail.size(); i++) {
			ContentValues contentvalue = listIndetail.get(i);
			contentvalue.put("AssetInInfoID", AssetInInfokey);
		}
		try {
			// 主表从表保存，只要有一个不成功就事务回滚
			resultInsert = db.insertList(content, listIndetail, "AssetInInfo",
					"AssetInDetail");
			if (resultInsert == listIndetail.size() + 1) {
				Toast.makeText(this, "入库成功", Toast.LENGTH_LONG).show();
				listIndetail.removeAll(listIndetail);
				listViewData.setAdapter(null);

			} else {
				Toast.makeText(this, "入库失败，请重试", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "入库失败，请重试", Toast.LENGTH_LONG).show();
		}
		resultInsert = 0;// 重新归零
		try {
			resultInsert = db.insertList(listdetail, "AssetDetail");
			if (resultInsert == listdetail.size()) {
				lifecycle(listdetail);
				Toast.makeText(this, "保存到在库成功", Toast.LENGTH_LONG).show();
				listdetail.remove(listdetail);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, "保存到在库失败，请重试", Toast.LENGTH_LONG).show();
		}
		db.close();

	}

	public void lifecycle(List<ContentValues> listdetail) {
		String tid = null;
		for (int i = 0; i < listdetail.size(); i++) {
			ContentValues contentvalue = listdetail.get(i);
			tid = contentvalue.getAsString("BarCode");

			SimpleDateFormat formatdate = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String operdate = formatdate.format(new Date());// 获取当前时间并格式化
			ContentValues content = new ContentValues();
			content.put("Key", CreateGuid.GenerateGUID());
			content.put("AssetOperatingID", CurrentUser.CurrentUserGuid);
			content.put("BarCode", tid);
			content.put("OperatingType", "入库");
			content.put("MaterialID", materialkey);
			content.put("SpecificationsID", specificationskey);
			content.put("OperatingDate", operdate);
			content.put("Number", 1);
			content.put("CreateOperater", CurrentUser.CurrentUserGuid);
			content.put("UpdateOperater", CurrentUser.CurrentUserGuid);
			content.put("UpdateDateTime", operdate);
			content.put("CreateDateTime", operdate);
			db.insert(content, "AssetLifecycleInfo");
		}
	}

	// 设置按钮是否可用
	private void setButtonClickable(Button button, boolean flag) {
		button.setClickable(flag);
		if (flag) {
			button.setTextColor(Color.BLACK);
		} else {
			button.setTextColor(Color.GRAY);
		}
	}

	// 添加数据到ListView
	private void addListView(List<ContentValues> list) {
		// 将数据添加到ListView
		listMap = new ArrayList<Map<String, Object>>();
		int idcount = 1;
		for (ContentValues epcdata : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ID", idcount);
			map.put("TID", epcdata.get("BarCode"));
			map.put("COUNT", 1);
			idcount++;
			listMap.add(map);
		}
		// Toast.makeText(AssetGatherActivity.this, listMap.get(1).toString(),
		// Toast.LENGTH_SHORT).show();
		listViewData.setAdapter(new SimpleAdapter(AssetGatherActivity.this,
				listMap, R.layout.listview_item, new String[] { "ID", "TID",
						"COUNT" }, new int[] { R.id.textView_id,
						R.id.textView_TID, R.id.textView_count }));

	}

	@Override
	protected void onPause() {
		// 页面切换的时候应中止盘询
		Intent toStopInventory = new Intent(AssetGatherActivity.this,
				UhfService.class);
		toStopInventory.putExtra("cmd", 0);
		startService(toStopInventory);
		super.onPause();
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

	@Override
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
			 * addListView(listEPC, recvString); } break;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.asset_gather, menu);
		return true;
	}

	// 选择状态和库存类型
	@Override
	public void onItemSelected(AdapterView<?> selectitem, View v, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch (selectitem.getId()) {
		/*
		 * case R.id.spinner_State: //states=arg2+1; types=arg2+1;
		 * //Toast.makeText(AssetGatherActivity.this, "states:"+states,
		 * Toast.LENGTH_LONG).show(); break;
		 */
		case R.id.spinner_types:

			if (arg2 == 0) {
				states = "false";
				assettypes = 1;
			}
			if (arg2 == 1) {
				states = "true";
				assettypes = 2;
			}
			break;
		// case R.id.spinner_providerid:
		// providerkey = ((ProviderInfo) providerspinner.getSelectedItem())
		// .getKey();
		// break;
		case R.id.materialID:
			materialkey = ((MaterialInfo) materialspinner.getSelectedItem())
					.getKey();
			specificationsDropDown(materialkey);
			break;
		/*
		 * case R.id.spinner_BelongID: belongkey = ((BranchCompanyInfo)
		 * Belongspinner.getSelectedItem()) .getKey();
		 * departmentDropDown(belongkey); break;
		 */
		case R.id.procurement:
			procurementkey = ((AccountCompanyInfo) procurementspinner
					.getSelectedItem()).getKey();
			break;
		case R.id.materialIDType:
			materialTypekey = ((MaterialModelInfo) materialTypespinner
					.getSelectedItem()).getKey();
			materialDropDown(materialTypekey);
			break;
		case R.id.specificationsid:
			specificationskey = ((SpecificationsInfo) specifications
					.getSelectedItem()).getKey();
			break;
		/*
		 * case R.id.cleverid: cleverskey = ((CleverInfo)
		 * clevers.getSelectedItem()).getKey(); break;
		 */
		// case R.id.newproviderid:
		// newproviderkey = ((ProviderInfo) newprovider.getSelectedItem())
		// .getKey();
		// break;
		case R.id.providerid:
			providerspinnerdialogkey = ((ProviderInfo) providerspinnerdialog
					.getSelectedItem()).getKey();
			break;
		/*
		 * case R.id.spinner_departmentid: departmentidkey = ((DepartmentInfo)
		 * departmentid.getSelectedItem()) .getKey(); break;
		 */
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private void providerDropDown() {
		// TODO Auto-generated method stub

		ArrayAdapter<ProviderInfo> adapter;
		List<ProviderInfo> listName = new ArrayList<ProviderInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("ProviderInfo", "Type", 1, "Name");
		if (cursor.moveToFirst()) {
			do {
				ProviderInfo item = new ProviderInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<ProviderInfo>(this,
				android.R.layout.simple_dropdown_item_1line, listName);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		providerspinner.setAdapter(adapter);
		// 设置当输入一个字符时，就开始索引
		providerspinner.setThreshold(1);
		// 添加事件Spinner事件监听
		providerspinner.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				providerkey = ((ProviderInfo) providerspinner.getAdapter()
						.getItem(arg2)).getKey();
			}
		});

		providerspinner.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasfoucs) {
				// TODO Auto-generated method stub
				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasfoucs) {
					view.showDropDown();
				}
			}
		});
		// 设置默认值
		providerspinner.setVisibility(View.VISIBLE);
		cursor.close();
		db.close();
	}

	private void providerDropDown(Spinner spinner) {
		// TODO Auto-generated method stub
		ArrayAdapter<ProviderInfo> adapter;
		List<ProviderInfo> listName = new ArrayList<ProviderInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("ProviderInfo", "Name");
		if (cursor.moveToFirst()) {
			do {
				ProviderInfo item = new ProviderInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<ProviderInfo>(this,
				android.R.layout.simple_dropdown_item_1line, listName);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		spinner.setAdapter(adapter);

		// 添加事件Spinner事件监听
		spinner.setOnItemSelectedListener(this);
		// 设置默认值
		spinner.setVisibility(View.VISIBLE);

	}

	// 生产厂家
	private void newproviderDropDown() {
		// TODO Auto-generated method stub
		ArrayAdapter<ProviderInfo> adapter;
		List<ProviderInfo> listName = new ArrayList<ProviderInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("ProviderInfo", "Type", 1, "Name");
		if (cursor.moveToFirst()) {
			do {
				ProviderInfo item = new ProviderInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<ProviderInfo>(this,
				android.R.layout.simple_dropdown_item_1line, listName);
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		newprovider.setAdapter(adapter);
		// 设置输入多少字符时开始检索
		newprovider.setThreshold(1);
		// 添加事件Spinner事件监听
		newprovider.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				newproviderkey = ((ProviderInfo) newprovider.getAdapter()
						.getItem(arg2)).getKey();
			}

		});
		newprovider.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasfoucs) {
				// TODO Auto-generated method stub
				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasfoucs) {
					view.showDropDown();
				}
			}
		});
		// 设置默认值
		newprovider.setVisibility(View.VISIBLE);
		cursor.close();
		db.close();
	}

	/*
	 * // 使用部门 private void departmentDropDown(String key) { // TODO
	 * Auto-generated method stub ArrayAdapter<DepartmentInfo> adapter;
	 * List<DepartmentInfo> listName = new ArrayList<DepartmentInfo>();
	 * db.open(); Cursor cursor = db.getAllTitles("DepartmentInfo", "CompanyID",
	 * key); if (cursor.moveToFirst()) { do { DepartmentInfo item = new
	 * DepartmentInfo(); item.Key =
	 * cursor.getString(cursor.getColumnIndex("Key")); item.Name =
	 * cursor.getString(cursor.getColumnIndex("Name")); listName.add(item); }
	 * while (cursor.moveToNext()); } adapter = new
	 * ArrayAdapter<DepartmentInfo>(this, android.R.layout.simple_spinner_item,
	 * listName);
	 * adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item
	 * ); // 将adapter 添加到spinner中 departmentid.setAdapter(adapter); //
	 * 添加事件Spinner事件监听 departmentid.setOnItemSelectedListener(this); // 设置默认值
	 * departmentid.setVisibility(View.VISIBLE);
	 * 
	 * }
	 */

	/*
	 * // 使用单位 private void belongDropDown() { // TODO Auto-generated method
	 * stub Belongspinner = (Spinner) findViewById(R.id.spinner_BelongID);
	 * ArrayAdapter<BranchCompanyInfo> adapter; List<BranchCompanyInfo> listName
	 * = new ArrayList<BranchCompanyInfo>(); db.open(); Cursor cursor =
	 * db.getAllTitles("BranchCompanyInfo", "Name"); if (cursor.moveToFirst()) {
	 * do { BranchCompanyInfo item = new BranchCompanyInfo(); item.Key =
	 * cursor.getString(cursor.getColumnIndex("Key")); item.Name =
	 * cursor.getString(cursor.getColumnIndex("Name")); listName.add(item); }
	 * while (cursor.moveToNext()); } adapter = new
	 * ArrayAdapter<BranchCompanyInfo>(this,
	 * android.R.layout.simple_spinner_item, listName);
	 * adapter.setDropDownViewResource
	 * (android.R.layout.simple_spinner_dropdown_item); // 将adapter 添加到spinner中
	 * Belongspinner.setAdapter(adapter); // 添加事件Spinner事件监听
	 * Belongspinner.setOnItemSelectedListener(this); // 设置默认值
	 * Belongspinner.setVisibility(View.VISIBLE);
	 * 
	 * }
	 */

	private void materialDropDown(String key) {
		// TODO Auto-generated method stub
		materialspinner = (Spinner) findViewById(R.id.materialID);
		ArrayAdapter<MaterialInfo> adapter;
		List<MaterialInfo> listName = new ArrayList<MaterialInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("MaterialInfo", "MaterialModelID", key,
				"Name");
		if (cursor.moveToFirst()) {
			do {
				MaterialInfo item = new MaterialInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);

			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<MaterialInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		materialspinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		materialspinner.setOnItemSelectedListener(this);
		// 设置默认值
		materialspinner.setVisibility(View.VISIBLE);
	}

	// 选择类别
	private void materialTypeDropDown() {
		// TODO Auto-generated method stub
		materialTypespinner = (Spinner) findViewById(R.id.materialIDType);
		ArrayAdapter<MaterialModelInfo> adapter;
		List<MaterialModelInfo> listName = new ArrayList<MaterialModelInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("MaterialModelInfo", "ParentID",
				"3d40c585-591f-444b-a78b-45bd4b61c3f8", "Name");
		if (cursor.moveToFirst()) {
			do {
				MaterialModelInfo item = new MaterialModelInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		adapter = new ArrayAdapter<MaterialModelInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		materialTypespinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		materialTypespinner.setOnItemSelectedListener(this);
		// 设置默认值
		materialTypespinner.setVisibility(View.VISIBLE);
	}

	private void procurementDropDown() {
		// TODO Auto-generated method stub
		procurementspinner = (Spinner) findViewById(R.id.procurement);
		ArrayAdapter<AccountCompanyInfo> adapter;
		List<AccountCompanyInfo> listName = new ArrayList<AccountCompanyInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("AccountCompanyInfo", "Name");
		if (cursor.moveToFirst()) {
			do {
				AccountCompanyInfo item = new AccountCompanyInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				listName.add(item);
			} while (cursor.moveToNext());
		}
		cursor.close();// 关闭游标，释放资源
		adapter = new ArrayAdapter<AccountCompanyInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		procurementspinner.setAdapter(adapter);
		// 添加事件Spinner事件监听
		procurementspinner.setOnItemSelectedListener(this);
		// 设置默认值
		procurementspinner.setVisibility(View.VISIBLE);
	}

	private void specificationsDropDown(String key) {
		// TODO Auto-generated method stub
		specifications = (Spinner) findViewById(R.id.specificationsid);
		ArrayAdapter<SpecificationsInfo> adapter;
		List<SpecificationsInfo> listName = new ArrayList<SpecificationsInfo>();
		db.open();
		Cursor cursor = db.getAllTitles("SpecificationsInfo", "MaterialID",
				key, "Name");
		if (cursor.moveToFirst()) {
			do {
				SpecificationsInfo item = new SpecificationsInfo();
				item.Key = cursor.getString(cursor.getColumnIndex("Key"));
				item.Name = cursor.getString(cursor.getColumnIndex("Name"));
				item.Unit = cursor.getString(2);
				listName.add(item);
			} while (cursor.moveToNext());
		}
		if (listName.isEmpty()) {
			SpecificationsInfo item = new SpecificationsInfo();
			item.Key = "00000000-0000-0000-0000-000000000000";
			item.Name = "未知规格";
			listName.add(item);
		}
		cursor.close();// 关闭游标，释放资源
		adapter = new ArrayAdapter<SpecificationsInfo>(this,
				android.R.layout.simple_spinner_item, listName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// 将adapter 添加到spinner中
		specifications.setAdapter(adapter);
		// 添加事件Spinner事件监听
		specifications.setOnItemSelectedListener(this);
		// 设置默认值
		specifications.setVisibility(View.VISIBLE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case 0:
			c = Calendar.getInstance();
			dialog = new DatePickerDialog(this,
					new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker arg0, int year,
								int month, int dayOfMonth) {
							// TODO Auto-generated method stub
							dateselect.setText(year + "-" + (month + 1) + "-"
									+ dayOfMonth);
						}
					}, c.get(Calendar.YEAR), // 传入年份
					c.get(Calendar.MONTH), // 传入月份
					c.get(Calendar.DAY_OF_MONTH) // 传入天数
			);
			break;
		}
		return dialog;
	}

	// 监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(AssetGatherActivity.this,
					ZiChanManagerActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

}
