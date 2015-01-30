package com.example.dao;

import java.util.List;

import com.example.common.TableNameStrings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

public class DBAdapter {

	private static final String TAG = "DBAdapter";
	private static final String DATABASE_NAME = "SDDKSD_UHF";
	private static final int DATABASE_VERSION = 1;
	public static final String KEY = "KEY";

	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);// 只在第一次加载时使用
		// DBHelper.onCreate(db);
	}

	/*
	 * 扩展类DatabaseHelper
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)// 创建一个数据库表，取名为bookstitles
		{
			for (int i = 0; i < CreateTableStrings.tables.length; i++) {
				db.execSQL(CreateTableStrings.tables[i]);
			}
		}

		/*
		 * 数据库升级 (non-Javadoc)
		 * 
		 * @see
		 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database
		 * .sqlite.SQLiteDatabase, int, int)
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			for (int i = 0; i < TableNameStrings.tablenames.length; i++) {
				db.execSQL("DROP TABLE IF EXISTS "
						+ TableNameStrings.tablenames[i]);
			}
			// db.execSQL("DROP TABLE IF EXISTS MaterialInfo");
			// db.execSQL("DROP TABLE IF EXISTS StockDetail");
			// db.execSQL("DROP TABLE IF EXISTS ProjectInfo");
			// db.execSQL("DROP TABLE IF EXISTS ProviderInfo");
			onCreate(db);
		}
	}

	// ---打开数据库---

	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// ---关闭数据库---
	public void close() {
		DBHelper.close();
	}

	// ---插入一条数据---
	public long insert(ContentValues initialvalues, String table) {
		return db.insert(table, null, initialvalues);
	}

	// ---插入n条数据---
	public long insertList(List<ContentValues> initialvalues, String table) {
		String DATABASE_TABLES = table;
		long result = 0;
		db.beginTransaction(); // 提交事务
		try {
			for (int i = 0; i < initialvalues.size(); i++) {// 循环将数据保存到数据库
				if (db.insert(DATABASE_TABLES, null, initialvalues.get(i)) > 0) {
					result++;
				}
			}
			// 设置事务标志为成功，当结束事务时就会提交事务
			if (result == initialvalues.size()) {
				db.setTransactionSuccessful();
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("操作失败");
			return result = -1;
		} finally {
			// 结束事务
			db.endTransaction();
		}
		return result;
	}

	// ---主从表保存---
	public long insertList(ContentValues content,
			List<ContentValues> initialvalues, String maintable,
			String detailtable) {
		long result = 0;
		db.beginTransaction(); // 提交事务
		try {
			if (db.insert(maintable, null, content) > 0) {
				result++;
			}
			for (int i = 0; i < initialvalues.size(); i++) {// 循环将数据保存到数据库
				if (db.insert(detailtable, null, initialvalues.get(i)) > 0) {
					result++;
				}
			}
			if (result == initialvalues.size() + 1) {
				// 设置事务标志为成功，当结束事务时就会提交事务
				db.setTransactionSuccessful();
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("操作失败");
			return result = -1;
		} finally {
			// 结束事务
			db.endTransaction();
		}
		return result;
	}

	// ---删除一个指定对象---

	public boolean deleteTitle(String table, long rowId) {
		return db.delete(table, KEY + "=" + rowId, null) > 0;
	}

	// ---检索指定表所有值---
	public Cursor getAllTitles(String table, String[] columns) {
		return db.query(table, columns, null, null, null, null, null);
	}

	// ---根据条件检索指定表所有值---
	public Cursor getAllTitles(String table, String key, String value,String orderbyname) {
		Cursor mCursor = null;
		try {
			mCursor = db.query(table, null, key + "='" + value + "'", null,
					null, null, orderbyname
					+ " COLLATE LOCALIZED ASC");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("getAllTitles", e.toString());
		}
		return mCursor;
	}
	// ---根据条件检索指定表所有值---
		public Cursor getAllTitles(String table, String sqlstr, int value) {
			Cursor mCursor = null;
			try {
				mCursor = db.query(table, null, sqlstr , null,
						null, null, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("getAllTitles", e.toString());
			}
			return mCursor;
		}
		// ---根据条件检索指定表所有值---
				public Cursor getAllTitles(String table,String[] colums,String sqlstr,String[] selectionArgs,String groupBy,String having, String orderBy) {
					Cursor mCursor = null;
					try {
						mCursor = db.query(table, colums, sqlstr, selectionArgs,
								groupBy, having, orderBy);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Log.e("getAllTitles", e.toString());
					}
					return mCursor;
				}
	// ---检索指定表所有值---
	public Cursor getAllTitles(String table, String orderbyname) {
		return db.query(table, null, null, null, null, null, orderbyname
				+ " COLLATE LOCALIZED ASC");
	}
	// ---根据条件检索指定表所有值---
		public Cursor getAllTitles(String table, String key, int value,String orderbyname) {
			Cursor mCursor = null;
			try {
				mCursor = db.query(table, null, key + "=" + value, null,
						null, null, orderbyname
						+ " COLLATE LOCALIZED ASC");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("getAllTitles", e.toString());
			}
			return mCursor;
		}
	// ---检索指定表所有值---
	public Cursor getAllTitles(String table) {
		return db.query(table, null, null, null, null, null, null);
	}

	// ---检索指定表所有值---
	public Cursor getAllByDate(String table, String time) {
		return db.query(table, null, null, null, null, null, time + " DESC");
	}

	// ---检索一个指定对象---
	public Cursor getTitle(String table, String[] columns, long rowId)
			throws SQLException {
		Cursor mCursor = db.query(true, table, columns, KEY + "=" + rowId,
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor getTitle(String table, String[] columns, String key,
			String value) throws SQLException {
		Cursor mCursor = null;
		try {
			mCursor = db.query(true, table, columns, key + " like'%" + value
					+ "%'", null, null, null, key + " desc", null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("getTitle", e.toString());
		}
		return mCursor;
	}

	// 根据获取的barcode查询所有该产品的数量
	public Cursor getTitles(String table, String[] columns, String sqlstr)
			throws SQLException {
		Cursor mCursor = null;
		try {
			mCursor = db.query(true, table, columns, sqlstr, null, null, null,
					null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("getTitle", e.toString());
		}
		return mCursor;
	}

	public String getNameBykey(String table, String value) throws SQLException {
		Cursor mCursor = null;
		String name = null;
		try {
			mCursor = db.query(true, table, new String[] { "Name" }, "Key='"
					+ value + "'", null, null, null, null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("getTitle", e.toString());
		}
		if (mCursor.moveToFirst()) {
			name = mCursor.getString(0);
		}
		return name;
	}

	// ---更新多个对象---

	public long updateList(List<String> initialvalues) {
		long result = 0;
		db.beginTransaction(); // 提交事务

		try {
			for (int i = 0; i < initialvalues.size(); i++) {// 循环将数据保存到数据库
				db.execSQL(initialvalues.get(i));
				result++;
			}
			// 设置事务标志为成功，当结束事务时就会提交事务
			db.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("操作失败");
			return result = -1;
		} finally {
			// 结束事务
			db.endTransaction();
		}
		return result;
	}

	// 通过name 获取主键
	public String getKeyByName(String table, String key, String value)
			throws SQLException {

		Cursor mCursor = db.query(true, table, new String[] { "Key" }, key
				+ "=" + value, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor.getString(0);
	}

	// 更新实体类
	public int updateData(String table, ContentValues content, String key,
			String[] value) {
		return db.update(table, content, key + "=?", value);
	}

	// 更新下载时间
	public void updateTime(String sqlstr) {
		db.execSQL(sqlstr);
	}

	/*
	 * 通过指定条件获取对象 key：指定的条件字段 value:查询的条件值
	 */
	public Cursor getUserInfoByCondition(String table, String key, String value)
			throws SQLException {

		Cursor mCursor = db.query(true, table, null, key + "='" + value + "'",
				null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// 清空指定表数据
	public boolean delete(String table) {
		return db.delete(table, null, null) > 0;
	}
}
