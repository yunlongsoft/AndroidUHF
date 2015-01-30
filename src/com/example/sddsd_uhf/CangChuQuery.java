package com.example.sddsd_uhf;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.UHFService.SysApplication;
import com.example.bean.MaterialInfo;
import com.example.bean.MaterialModelInfo;
import com.example.bean.SpecificationsInfo;
import com.example.dao.DBAdapter;

public class CangChuQuery extends Activity implements OnItemSelectedListener {

	private Spinner materialnamequery;
	private Spinner specificationsquery;
	private Spinner materialcategory;
	private Button querybutton;
	
	private String materialnamequerykey;
	private String specificationsquerykey;
	private String materialcategorykey;
	
	private TextView materialtype;
	private TextView material;
	private TextView materialmodel;
	private TextView warehouseid;
	private TextView quantity;
	
	private String materialtypename;
	private String materialname;
	private String materialmodelname;
	private String warehousename;
	private String quantitystr;
	private String belongname;
	DBAdapter db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cang_chu_query);
		SysApplication.getInstance().addActivity(this);
		db = new DBAdapter(this);
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		materialnamequery=(Spinner) findViewById(R.id.materialnamequery);
		specificationsquery=(Spinner) findViewById(R.id.specificationsquery);
		materialcategory=(Spinner) findViewById(R.id.materialcategory);
		materialtype=(TextView) findViewById(R.id.materialtype);
		material=(TextView) findViewById(R.id.material);
		materialmodel=(TextView) findViewById(R.id.materialmodel);
		warehouseid=(TextView) findViewById(R.id.warehouseid);
		quantity=(TextView) findViewById(R.id.quantity);
		
		querybutton=(Button) findViewById(R.id.querybutton);
		materialcategoryDropdown();
		querybutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				materialtype.setText("");
				material.setText("");
				materialmodel.setText("");
				warehouseid.setText("");
				quantity.setText("");
				querymaterial();
			}
		});
	}
	private void querymaterial() {
		// TODO Auto-generated method stub
		db.open();
		Cursor cursor=db.getTitles("StockDetail", new String[]{"OnQuantity","WarehouseID"},"MaterialID='"+materialnamequerykey+"' "
				+ "and SpecificationsID='"+specificationsquerykey+"'");
		if (cursor.moveToFirst()) {
			do{
				materialtype.setText(materialtypename);
				material.setText(materialname);
				materialmodel.setText(materialmodelname);
				warehousename=db.getNameBykey("Warehouse", cursor.getString(cursor.getColumnIndex("WarehouseID")));
				warehouseid.setText(warehousename);
				quantity.setText(cursor.getString(cursor.getColumnIndex("OnQuantity")));
			}while(cursor.moveToNext());
		}else {
			Toast.makeText(this, "不存在该商品", Toast.LENGTH_SHORT).show();
		}
		db.close();
	}

	 //先选择产品类别
		public void materialcategoryDropdown() {
			ArrayAdapter<MaterialModelInfo> adapter;
			List<MaterialModelInfo> listName = new ArrayList<MaterialModelInfo>();
			db.open();
			
			//78af83a7-c9bb-4382-bfc4-23b0ac736514
			Cursor cursor = db.getAllTitles("MaterialModelInfo","ParentID","78af83a7-c9bb-4382-bfc4-23b0ac736514","Name");
			if (cursor.moveToFirst()) {
				do {
					MaterialModelInfo item1 = new MaterialModelInfo();
					Cursor cursorsecond = db.getAllTitles("MaterialModelInfo","ParentID",cursor.getString(cursor.getColumnIndex("Key")),"Name");
					if (cursorsecond.moveToFirst()) {
						do{
							MaterialModelInfo item2 = new MaterialModelInfo();
							Cursor cursorlast = db.getAllTitles("MaterialModelInfo","ParentID",cursorsecond.getString(cursorsecond.getColumnIndex("Key")),"Name");
							if(cursorlast.moveToFirst()){
								
								do{
									MaterialModelInfo item3 = new MaterialModelInfo();
									Cursor cursorlast2 = db.getAllTitles("MaterialModelInfo","ParentID",cursorlast.getString(cursorlast.getColumnIndex("Key")),"Name");
									if(cursorlast2.moveToFirst()){
										//Toast.makeText(this, "超出界限", Toast.LENGTH_SHORT).show();
										System.out.println("你好吗，边界超出了");
									}else{
										item3.Key = cursorlast.getString(cursorlast.getColumnIndex("Key"));
										item3.Name = cursorlast.getString(cursorlast.getColumnIndex("Name"));
										listName.add(item3);
									}
									cursorlast2.close();
								}while(cursorlast.moveToNext());
								
							}else{
								item2.Key = cursorsecond.getString(cursorsecond.getColumnIndex("Key"));
								item2.Name = cursorsecond.getString(cursorsecond.getColumnIndex("Name"));
								listName.add(item2);
							}
							cursorlast.close();
						}while(cursorsecond.moveToNext());
					}else{
						item1.Key = cursor.getString(cursor.getColumnIndex("Key"));
						item1.Name = cursor.getString(cursor.getColumnIndex("Name"));
						listName.add(item1);
					}
					cursorsecond.close();
				} while (cursor.moveToNext());
			}
			cursor.close();
			adapter = new ArrayAdapter<MaterialModelInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			materialcategory.setAdapter(adapter);
			// 添加事件Spinner事件监听
			materialcategory.setOnItemSelectedListener(this);
			// 设置默认值
			materialcategory.setVisibility(View.VISIBLE);
			db.close();
		}
		
		 //产品
		public void materialnameDropdown(String key) {
			ArrayAdapter<MaterialInfo> adapter;
			List<MaterialInfo> listName = new ArrayList<MaterialInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("MaterialInfo","MaterialModelID",key,"Name");
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
			materialnamequery.setAdapter(adapter);
			// 添加事件Spinner事件监听
			materialnamequery.setOnItemSelectedListener(this);
			// 设置默认值
			materialnamequery.setVisibility(View.VISIBLE);
			db.close();
		}
		
		 //产品规格型号
		public void specificationsidDropdown(String key) {
			ArrayAdapter<SpecificationsInfo> adapter;
			List<SpecificationsInfo> listName = new ArrayList<SpecificationsInfo>();
			db.open();
			Cursor cursor = db.getAllTitles("SpecificationsInfo","MaterialID",key,"Name");
			if (cursor.moveToFirst()) {
				do {
					SpecificationsInfo item = new SpecificationsInfo();
					item.Key = cursor.getString(cursor.getColumnIndex("Key"));
					item.Name = cursor.getString(cursor.getColumnIndex("Name"));
					//item.MaterialID=cursor.getString(cursor.getColumnIndex("MaterialID"));
					//item.Unit=cursor.getString(cursor.getColumnIndex("Unit"));
					listName.add(item);
				} while (cursor.moveToNext());
			}
			if (listName.isEmpty()) {
				SpecificationsInfo item = new SpecificationsInfo();
				item.Key ="00000000-0000-0000-0000-000000000000";
				item.Name = "未知规格";
				listName.add(item);
			}
			adapter = new ArrayAdapter<SpecificationsInfo>(this,
					android.R.layout.simple_spinner_item, listName);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// 将adapter 添加到spinner中
			specificationsquery.setAdapter(adapter);
			// 添加事件Spinner事件监听
			specificationsquery.setOnItemSelectedListener(this);
			// 设置默认值
			specificationsquery.setVisibility(View.VISIBLE);
			db.close();
		}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cang_chu_query, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> selection, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch(selection.getId()){
		case R.id.materialcategory:
			materialcategorykey=((MaterialModelInfo)materialcategory.getSelectedItem()).getKey();
			materialtypename=((MaterialModelInfo)materialcategory.getSelectedItem()).getName();
			materialnameDropdown(materialcategorykey);
			break;
		case R.id.materialnamequery:
			materialnamequerykey=((MaterialInfo)materialnamequery.getSelectedItem()).getKey();
			materialname=((MaterialInfo)materialnamequery.getSelectedItem()).getName();
			specificationsidDropdown(materialnamequerykey);
			break;
		case R.id.specificationsquery:
			specificationsquerykey=((SpecificationsInfo)specificationsquery.getSelectedItem()).getKey();
			materialmodelname=((SpecificationsInfo)specificationsquery.getSelectedItem()).getName();
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	//监听返回键
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent=new Intent();
			 intent.setClass(CangChuQuery.this,CangChuManagerActivity.class);
	            startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
