package com.example.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageButtonTemplate extends LinearLayout {
	private  ImageView   imageViewbutton;
	private  TextView   textView;

	public ImageButtonTemplate(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		
		imageViewbutton = new ImageView(context, attrs);
		
		imageViewbutton.setPadding(0, 0, 0, 0);
		
		textView =new TextView(context, attrs);
		//Ë®Æ½¾ÓÖÐ
		textView.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
		
		textView.setPadding(0, 0, 0, 0);
		
		setClickable(true);
		
		setFocusable(true);
		
		//setBackgroundResource(android.R.drawable.btn_default);
		
		setOrientation(LinearLayout.VERTICAL);
		
		addView(imageViewbutton);
		
		addView(textView);
		
	}

}