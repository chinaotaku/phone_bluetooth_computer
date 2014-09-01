package com.example.newbluetooth;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognizerIntent;
import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;

public class MainActivity extends Activity {
	private ArrayAdapter<String> mArrayAdapter;
	public static Bt blueTooth;
	private GestureDetector mGestureDetector;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private PowerManager.WakeLock wakeLock; // 设置屏幕锁用的

	public MainActivity mainActivity = this;

	public boolean isFull = false;
	public boolean isShow = false;

	public static int width;

	private ImageButton fullScreanBtn;
	private ImageButton paintBtn;
	private ImageButton changePcbBtn;
	private ImageButton selectBtn;
	private ImageButton addBtn;
	// private Button btn1;

	public Bitmap addBitmap;
	public Bitmap cutBitmap;

	public static enum Click {
		FULL, CHANGEPCB, SELECT, PAINT, ESC, ADD
	};

	/**
	 * 监听手势
	 * 
	 * @author KumaHime
	 * 
	 */
	private class MygestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			final int MIN_DISTANCE = 100;
			// Toast.makeText(this, "onfiling", Toast.LENGTH_SHORT);
			if (e1.getX() - e2.getX() > MIN_DISTANCE) {
				blueTooth.Sendmessage("LEFT");
			}
			if (e2.getX() - e1.getX() > MIN_DISTANCE) {
				blueTooth.Sendmessage("RIGHT");
			}
			if (e1.getY() - e2.getY() > MIN_DISTANCE) {
				blueTooth.Sendmessage("UP");
			}
			if (e2.getY() - e1.getY() > MIN_DISTANCE) {
				blueTooth.Sendmessage("DOWN");
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}

	public ScaleAnimation scaleAnimation() {
		ScaleAnimation animationScale = new ScaleAnimation(1f, 2, 1f, 2,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.45f);
		animationScale.setInterpolator(mainActivity,
				anim.accelerate_decelerate_interpolator);
		animationScale.setDuration(500);
		animationScale.setFillAfter(false);
		return animationScale;
	}

	private class onClick implements OnClickListener {
		public Click click;

		public onClick(Click c) {
			click = c;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (click) {
			case FULL: {
				if (blueTooth.Isconnect()) {
					if (!isFull) {
						blueTooth.Sendmessage("FULLSCREAN");
						fullScreanBtn.startAnimation(scaleAnimation());
						isFull = true;
					} else {
						blueTooth.Sendmessage("ESC");
						fullScreanBtn.startAnimation(scaleAnimation());
						isFull = false;
					}
				}
				break;
			}
			case CHANGEPCB: {
				if (blueTooth.Isconnect()) {
					blueTooth.Sendmessage("CHANGEPCB");
					changePcbBtn.startAnimation(scaleAnimation());
				}
				break;
			}
			case SELECT: {
				if (blueTooth.Isconnect()) {
					blueTooth.Sendmessage("SELECT");
					selectBtn.startAnimation(scaleAnimation());
				}
				break;
			}
			case PAINT: {
				if (blueTooth.Isconnect()) {
					paintBtn.startAnimation(scaleAnimation());
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, PaintActivity.class);
					startActivity(intent);
					overridePendingTransition(android.R.anim.slide_in_left,
							android.R.anim.slide_out_right);
				}
				break;
			}
			case ADD: {
				int x = width / 4;
				if (!isShow) {
					addBtn.setImageBitmap(cutBitmap);

					TranslateAnimation animation1 = new TranslateAnimation(0,
							width - x, 0, 50);
					TranslateAnimation animation2 = new TranslateAnimation(0,
							width - (x * 2), 0, 50);
					TranslateAnimation animation3 = new TranslateAnimation(0,
							width - (x * 3), 0, 50);
					TranslateAnimation animation4 = new TranslateAnimation(0,
							width - (x * 4), 0, 50);

					LayoutParams params = new LayoutParams(0, 0);// 用来初始化按钮
					params.height = 0;
					params.width = 0;
					params.setMargins(0, 0, 0, 0);

					float alpha = 0;

					fullScreanBtn.setLayoutParams(params);
					fullScreanBtn.setAlpha(alpha);

					paintBtn.setLayoutParams(params);
					paintBtn.setAlpha(alpha);

					changePcbBtn.setLayoutParams(params);
					changePcbBtn.setAlpha(alpha);

					selectBtn.setLayoutParams(params);
					selectBtn.setAlpha(alpha);

					animation4.setAnimationListener(new animeStart(
							fullScreanBtn, null, null, width - (x * 4), 50,
							isShow));

					animation3.setAnimationListener(new animeStart(
							changePcbBtn, fullScreanBtn, animation4, width
									- (x * 3), 50, isShow));

					animation2.setAnimationListener(new animeStart(selectBtn,
							changePcbBtn, animation3, width - (x * 2), 50,
							isShow));

					animeStart anibegin = new animeStart(paintBtn, selectBtn,
							animation2, width - x, 50, isShow);
					animation1.setAnimationListener(anibegin);
					paintBtn.startAnimation(animation1);

					long durationMillis = 25;
					animation1.setDuration(durationMillis);
					animation2.setDuration(durationMillis);
					animation3.setDuration(durationMillis);
					animation4.setDuration(durationMillis);

					isShow = true;
				} else {
					addBtn.setImageBitmap(addBitmap);
					float alpha = 100;

					LayoutParams params = new LayoutParams(0, 0);// 用来初始化按钮
					params.height = width / 8;
					params.width = width / 8;
					params.setMargins(0, 50, 0, 0);
					fullScreanBtn.setLayoutParams(params);
					fullScreanBtn.setAlpha(alpha);

					LayoutParams params1 = new LayoutParams(0, 0);// 用来初始化按钮
					params1.height = width / 8;
					params1.width = width / 8;
					params1.setMargins(x, 50, 0, 0);
					changePcbBtn.setLayoutParams(params1);
					changePcbBtn.setAlpha(alpha);

					LayoutParams params2 = new LayoutParams(0, 0);// 用来初始化按钮
					params2.height = width / 8;
					params2.width = width / 8;
					params2.setMargins(2 * x, 50, 0, 0);
					selectBtn.setLayoutParams(params2);
					selectBtn.setAlpha(alpha);

					LayoutParams params3 = new LayoutParams(0, 0);// 用来初始化按钮
					params3.height = width / 8;
					params3.width = width / 8;
					params3.setMargins(3 * x, 50, 0, 0);
					paintBtn.setLayoutParams(params3);
					paintBtn.setAlpha(alpha);

					TranslateAnimation animation1 = new TranslateAnimation(0,
							-(x * 3), 0, -50);
					TranslateAnimation animation2 = new TranslateAnimation(0,
							-(x * 2), 0, -50);
					TranslateAnimation animation3 = new TranslateAnimation(0,
							-x, 0, -50);
					TranslateAnimation animation4 = new TranslateAnimation(0,
							0, 0, -50);

					animation4.setAnimationListener(new animeStart(
							fullScreanBtn, changePcbBtn, animation3, 0, 0,
							isShow));

					animation3.setAnimationListener(new animeStart(
							changePcbBtn, selectBtn, animation2, 0, 0, isShow));

					animation2.setAnimationListener(new animeStart(selectBtn,
							paintBtn, animation1, 0, 0, isShow));

					animation1.setAnimationListener(new animeStart(paintBtn,
							null, null, 0, 0, isShow));

					fullScreanBtn.startAnimation(animation4);

					long durationMillis = 25;
					animation4.setDuration(durationMillis);
					animation3.setDuration(durationMillis);
					animation2.setDuration(durationMillis);
					animation1.setDuration(durationMillis);
					isShow = false;
				}
				break;
			}
			default:
				break;
			}
		}
	}

	public class animeStart implements AnimationListener {
		ImageButton imageButton;
		ImageButton myImageButton;
		TranslateAnimation translateAnimation;
		int lastX;
		int lastY;
		boolean IsShow;

		public animeStart(ImageButton myimbtn, ImageButton imbtn,
				TranslateAnimation trans, int lX, int lY, boolean is) {
			imageButton = imbtn;
			myImageButton = myimbtn;
			translateAnimation = trans;
			lastX = lX;
			lastY = lY;
			IsShow = is;
		}

		@Override
		public void onAnimationEnd(Animation arg0) {
			LayoutParams params = new LayoutParams(0, 0);

			if (!(imageButton == null || translateAnimation == null)) {
				imageButton.setAnimation(translateAnimation);

			}
			if (IsShow) {
				float alpha = 0;
				params.height = 0;
				params.width = 0;
				myImageButton.setAlpha(alpha);
			} else {
				float alpha = 100;
				params.height = width / 8;
				params.width = width / 8;
				myImageButton.setAlpha(alpha);
			}

			params.setMargins(lastX, lastY, 0, 0);
			myImageButton.setLayoutParams(params);
			myImageButton.clearAnimation();

		}

		@Override
		public void onAnimationRepeat(Animation arg0) {
		}

		@Override
		public void onAnimationStart(Animation arg0) {
			if (!IsShow) {
				float alpha = 100;
				myImageButton.setAlpha(alpha);
				LayoutParams params = new LayoutParams(0, 0);
				params.height = width / 8;
				params.width = width / 8;
				params.setMargins(0, 0, 0, 0);
				myImageButton.setLayoutParams(params);
			}
		}
	}

	/**
	 * 设置屏幕常量
	 */
	private void acquireWakeLock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this
					.getClass().getCanonicalName());
			wakeLock.acquire();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		acquireWakeLock(); // 设置屏幕常量
		setContentView(R.layout.activity_main);

		// ///////////
		// 语音功能
		// ///////////
		// btn1 = (Button) findViewById(R.id.button1);
		// btn1.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// startVoiceRecognitionActivity();
		// }
		// });

		Resources r = getResources();
		InputStream is = r.openRawResource(R.drawable.add);
		BitmapDrawable bd = new BitmapDrawable(is);
		addBitmap = bd.getBitmap();

		is = r.openRawResource(R.drawable.cut);
		bd = new BitmapDrawable(is);
		cutBitmap = bd.getBitmap();

		LayoutParams params = new LayoutParams(0, 0);// 用来初始化按钮

		mGestureDetector = new GestureDetector(this, new MygestureListener()); // 手势监听

		width = getWindowManager().getDefaultDisplay().getWidth();

		float alpha = 0;

		addBtn = (ImageButton) findViewById(R.id.add);
		params.setMargins(0, 0, 0, 0);
		params.height = 35;
		params.width = 35;
		addBtn.setLayoutParams(params);
		addBtn.setBackgroundColor(Color.BLACK);
		addBtn.setOnClickListener(new onClick(Click.ADD));

		params = new LayoutParams(0, 0);
		params.height = 0;
		params.width = 0;

		fullScreanBtn = (ImageButton) findViewById(R.id.fullscreen); // 全屏按键
		fullScreanBtn.setAlpha(alpha);
		fullScreanBtn.setLayoutParams(params);
		fullScreanBtn.setBackgroundColor(Color.BLACK);

		fullScreanBtn.setOnClickListener(new onClick(Click.FULL));// 按键触碰响应

		paintBtn = (ImageButton) findViewById(R.id.pptdraw);
		paintBtn.setAlpha(alpha);
		paintBtn.setLayoutParams(params);
		paintBtn.setBackgroundColor(Color.BLACK);

		paintBtn.setOnClickListener(new onClick(Click.PAINT)); // 界面切换响应

		changePcbBtn = (ImageButton) findViewById(R.id.exchange);
		changePcbBtn.setAlpha(alpha);
		changePcbBtn.setLayoutParams(params);
		changePcbBtn.setBackgroundColor(Color.BLACK);

		changePcbBtn.setOnClickListener(new onClick(Click.CHANGEPCB)); // 切换

		selectBtn = (ImageButton) findViewById(R.id.select);
		selectBtn.setAlpha(alpha);
		params = new LayoutParams(0, 0);
		params.setMargins(0, 0, 0, 0);
		params.height = 0;
		params.width = 0;
		selectBtn.setLayoutParams(params);
		selectBtn.setBackgroundColor(Color.BLACK);

		selectBtn.setOnClickListener(new onClick(Click.SELECT)); // 确认

		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		blueTooth = new Bt(this, mArrayAdapter);

		if (!blueTooth.IsHaveBt()) {
			// 检查系统是否支持blueTooth
			this.finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		blueTooth.TurnOnBt(); // 检查蓝牙是否打开，如果没打开就不断要求打开蓝牙
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			blueTooth.endSearchBluetooth();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int Id = item.getItemId();
		if (Id == R.id.open_bluetooth_item) {
			ListView lv = new ListView(this);
			lv.setAdapter(blueTooth.getDefualtAdapter());
			lv.setScrollingCacheEnabled(false);
			final AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.title_dialog)
					.setPositiveButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									blueTooth.endSearchBluetooth();
								}
							}).setView(lv).create();
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> items, View view,
						int position, long id) {
					dialog.dismiss();
					String address = blueTooth.getDefualtAdapter().getItem(
							position);
					blueTooth.connect(address);
				}
			});
			dialog.show();
		}
		if (Id == R.id.Search_BlueTooth) {
			ListView lv = new ListView(this);
			lv.setAdapter(mArrayAdapter);
			lv.setScrollingCacheEnabled(false);
			final AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.title_dialog)
					.setPositiveButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									blueTooth.endSearchBluetooth();
								}
							}).setView(lv).create();
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> items, View view,
						int position, long id) {
					dialog.dismiss();
					String address = mArrayAdapter.getItem(position);
					blueTooth.connect(address);
				}
			});
			dialog.show();
			blueTooth.searchBlutooth();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (blueTooth.Isconnect()) {
			return mGestureDetector.onTouchEvent(event); // 触碰屏幕事件时调用手势
		} else {
			return super.onTouchEvent(event);
		}
	}

	// /////////////////////
	// 语音功能，目前没有必要。
	// /////////////////////
	/*
	 * private void startVoiceRecognitionActivity() { // 通过Intent传递语音识别的模式
	 * Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //
	 * 语言模式和自由形式的语音识别 intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	 * RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // 提示语音开始
	 * intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出需要打开的网站名字."); //
	 * 开始执行我们的Intent、语音识别 startActivityForResult(intent,
	 * VOICE_RECOGNITION_REQUEST_CODE); }
	 * 
	 * // 当语音结束时的回调函数onActivityResult
	 * 
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { if (requestCode ==
	 * VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) { // 取得语音的字符
	 * ArrayList<String> matches = data
	 * .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	 * 
	 * Iterator<String> it1 = matches.iterator(); String str; while
	 * (it1.hasNext()) { str = it1.next(); if (str.equals("切换") ||
	 * str.equals("変換")) { blueTooth.Sendmessage("CHANGEPCB"); break; } else if
	 * (str.equals("选择") || str.equals("選択する")) {
	 * blueTooth.Sendmessage("SELECT"); break; } else if (str.equals("关机") ||
	 * str.equals("シャットダウン")) { blueTooth.Sendmessage("SHUTDOWN"); break; } } }
	 * super.onActivityResult(requestCode, resultCode, data); }
	 */

	/**
	 * 退出后就命令电脑端退出进程
	 */
	@Override
	protected void onDestroy() {
		if (blueTooth.Isconnect())
			blueTooth.Sendmessage("OFFLINE");
		java.lang.System.exit(0);
		super.onDestroy();
	}

}
