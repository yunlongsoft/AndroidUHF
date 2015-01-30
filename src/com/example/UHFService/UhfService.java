package com.example.UHFService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.android.hdhe.uhf.SerialPort;
import com.android.hdhe.uhf.reader.SendCommendManager;
import com.android.hdhe.uhf.util.Tools;
import com.example.common.Constants;

public class UhfService extends Service {
	
	private SerialPort mSerialPort;//串口操作句柄
	private InputStream mInputStream;//串口输入流
	private OutputStream mOutputStream;//串口输出流
	private String recvActivity = null;//发送请求的Activity
	private ServiceReceiver myReceiver = null;//广播接受者
	private int cmdCode = 0;
	private boolean runFlag = true; //盘询线程运行状态
	private final int port = 13;
	private final int baudrate = 115200;
	private SendCommendManager cmdManager = null;//指令管理者
	private InventoryThread inventoryThread = null;//盘询线程

	private int addr;//起始地址
	private int readDataLength;//读数据长度
	private byte[] accessPassword;//访问密码
	private byte[] dataBytes;//写入的数据
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {
		initSerialport();
		//注册广播接收者
		myReceiver = new ServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.UHFService.UhfService");
		registerReceiver(myReceiver, filter);
	    inventoryThread = new InventoryThread();
	    inventoryThread.start();
		super.onCreate();
	}
	
	private void initSerialport(){
		try {
			mSerialPort = new SerialPort(port, baudrate, 0);//打开串口
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mSerialPort == null){
			return;
		}
		mSerialPort.uhfPowerOn();
		mInputStream = mSerialPort.getInputStream();
		mOutputStream = mSerialPort.getOutputStream();
		//
		cmdManager = new SendCommendManager(mInputStream, mOutputStream);
		
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//卸载Receiver
		unregisterReceiver(myReceiver);
		//销毁盘询线程
		runFlag = false;
		//关闭串口
		if(mSerialPort != null){
			try {
				mInputStream.close();
				mOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//关闭读写器电源
			mSerialPort.uhfPowerOff();
		}
		mSerialPort.close(port);
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if(intent!=null){
			 cmdCode = intent.getIntExtra("cmd", 0);
			//执行指令
			 if (cmdCode!=0) {
					accessPassword = intent.getByteArrayExtra("accessPassword");
					dataBytes = intent.getByteArrayExtra("dataBytes");
				 exeCmd(cmdCode);
			}
		}else{
			return 0;
		}
		 //runFlag=intent.getBooleanExtra("startflag", false);
		return super.onStartCommand(intent, flags, startId);
	}
	
	//盘询线程
	private class InventoryThread extends Thread{
		
		@Override
		public void run() {
			while(runFlag){
				if(cmdCode == Constants.CMD_ISO18000_6C_INVENTORY){
					Inventory(cmdCode);
				}
			}
			super.run();
		}
	}
	private void Inventory(int cmdCode){
	    Intent toActivity = new Intent();
		toActivity.setAction(recvActivity);
		int count = 0;
		while(count < 1){
			//设置长度为32个字，读32次，无差错
			byte[] recvData = cmdManager.readFrom6C(2,3,2);
			if(recvData != null){
				String readData = Tools.Bytes2HexString(recvData,recvData.length);
				Log.e("read tag", readData);
				toActivity.putExtra("result", readData.substring(readData.length() - 4*2, readData.length()));
			}
			sendBroadcast(toActivity);
			count++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//执行指令
	private void exeCmd(int cmdCode){
		Intent toActivity = new Intent();
		toActivity.setAction(recvActivity);
		switch (cmdCode) {
		/*//读标签
		case Constants.CMD_ISO18000_6C_INVENTORY:
			int count = 0;
			while(count < 2){
				//设置长度为32个字，读32次，无差错
				byte[] recvData = cmdManager.readFrom6C(2,3,2);
				if(recvData != null){
					String readData = Tools.Bytes2HexString(recvData,recvData.length);
					Log.e("read tag", readData);
					toActivity.putExtra("result", readData.substring(readData.length() - 4*2, readData.length()));
				}
				sendBroadcast(toActivity);
//				addr += 32;
				count++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;*/
		case Constants.CMD_ISO18000_6C_READ:
			int read = 0;
			while(read < 1){
				//设置长度为32个字，读32次，无差错
				byte[] recvData = cmdManager.readFrom6C(1,2,2);//原先为2,3,2
				if(recvData != null){
					String readData = Tools.Bytes2HexString(recvData,recvData.length);
					Log.e("read tag", readData);
					System.out.println("yaha :"+readData);
					toActivity.putExtra("result", readData.substring(readData.length() - 4*2, readData.length()));
				}
				sendBroadcast(toActivity);
//				addr += 32;
				read++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case Constants.CMD_ISO18000_6C_WRITE:
			addr = 0;
			Log.i("dataBytes***", Tools.Bytes2HexString(dataBytes, dataBytes.length));
			while( addr < 8){
				boolean writeFlag = cmdManager.writeTo6C(accessPassword, 1, 2, 2, dataBytes);
				//第一次没写成功，再写一次
				if(!writeFlag){
					writeFlag = cmdManager.writeTo6C(accessPassword, 1, 2, 2, dataBytes);
				}
				toActivity.putExtra("writeFlag", writeFlag);
				sendBroadcast(toActivity);
				addr += 8;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i("write addr value", "/////////////// "+ addr+"  //////////////" + writeFlag);
			}
			break;
		default:
			break;
		}
	}
	
	//服务广播接收者
	private class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String ac = intent.getStringExtra("activity");
			if(ac!=null)
				Log.e("receive activity", ac);
			    recvActivity = ac; // 获取activity
			if (intent.getBooleanExtra("stopflag", false))
				stopSelf(); // 收到停止服务信号
			Log.e("stop service", intent.getBooleanExtra("stopflag", false)
					+ "");

		}

	}
}
