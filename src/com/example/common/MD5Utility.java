package com.example.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class MD5Utility {
	/*
	 * 加密操作
	 */
	public static String getMD5(String val) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(val.getBytes());
		byte[] m = md5.digest();// 加密
		return getString(m);
	}

	/*
	 * 转成字符串
	 */
	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	public static byte[] unGZip(byte[] bContent) {
		byte[] data = new byte[65536];
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bContent);
			GZIPInputStream pIn = new GZIPInputStream(in);
			DataInputStream objIn = new DataInputStream(pIn);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			int count = 0;
			while ((count = pIn.read(data)) != -1) {
				baos.write(data, 0, count);
			}
			byte[] trueData=baos.toByteArray();
			objIn.close();
			pIn.close();
			in.close();
			return trueData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] unZip(byte[] bContent) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bContent);
			ZipInputStream zip = new ZipInputStream(bis);
			while (zip.getNextEntry() != null) {
				byte[] buf = new byte[1024];
				int num = -1;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ((num = zip.read(buf, 0, buf.length)) != -1) {
					baos.write(buf, 0, num);
				}
				b = baos.toByteArray();
				baos.flush();
				baos.close();
			}
			zip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
}
