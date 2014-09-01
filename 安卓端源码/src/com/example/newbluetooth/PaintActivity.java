package com.example.newbluetooth;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

public class PaintActivity extends Activity {
	private boolean DRAW_UNDERLINE = false;
	private boolean WRITE = false;
	private boolean RETURN_MOUSE = false;
	private PowerManager.WakeLock wakeLock; //设置屏幕锁用的

	private int MoveCounter = 0;

	private Canvas canvas;
	private Paint paint;
	private Bitmap bitmap;

	private ImageView backgroundView;

	private int MemoryX = 0; // 安卓端触碰记录的x
	private int MemoryY = 0; // 安卓端触碰记录的y

	private int MoveOfX = 0; // 手机端x的移动
	private int MoveOfY = 0; // 手机端y的移动

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
		acquireWakeLock();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.bluepaint_main);

		paint = new Paint();
		paint.setStrokeWidth(5);
		paint.setColor(Color.RED);

		backgroundView = (ImageView) findViewById(R.id.background);
		backgroundView.setOnTouchListener(touch); // 触碰反应

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.paintmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setClass(PaintActivity.this, MainActivity.class);
			PaintActivity.this.finish();
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int Id = item.getItemId();
		switch (Id) {
		case R.id.Return: {
			Intent intent = new Intent();
			intent.setClass(PaintActivity.this, MainActivity.class);
			PaintActivity.this.finish();
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			break;
		}
		case R.id.underline: {
			backgroundView.setImageResource(R.drawable.background);
			bitmap = null;
			MainActivity.blueTooth.Sendmessage("DRAW");
			DRAW_UNDERLINE = true;
			WRITE = false;
			RETURN_MOUSE = false;
			break;
		}
		case R.id.Write: {
			MainActivity.blueTooth.Sendmessage("DRAW");
			WRITE = true;
			MoveCounter = 0;
			DRAW_UNDERLINE = false;
			RETURN_MOUSE = false;
			break;
		}
		case R.id.MoveMouse: {
			backgroundView.setImageResource(R.drawable.background);
			bitmap = null;
			MainActivity.blueTooth.Sendmessage("RETURNMOUSE");
			RETURN_MOUSE = true;
			DRAW_UNDERLINE = false;
			WRITE = false;
			break;
		}
		case R.id.Mark: {
			MainActivity.blueTooth.Sendmessage("MARK");
			break;
		}
		case R.id.Clear: {
			if (WRITE || DRAW_UNDERLINE) {
				if (WRITE) {
					bitmap = Bitmap
							.createBitmap(backgroundView.getWidth(),
									backgroundView.getHeight(),
									Bitmap.Config.ARGB_8888);
					canvas = new Canvas(bitmap);
					backgroundView.setImageBitmap(bitmap);
				}
				MainActivity.blueTooth.Sendmessage("CLEAR");
			}
			break;
		}
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private View.OnTouchListener touch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (MainActivity.blueTooth.Isconnect()) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN: {
					if (WRITE) {
						if (MoveCounter == 0) {
							MoveCounter++;

							if (bitmap == null) {
								bitmap = Bitmap.createBitmap(
										backgroundView.getWidth(),
										backgroundView.getHeight(),
										Bitmap.Config.ARGB_8888);

								canvas = new Canvas(bitmap);
							}
						} else {
							MainActivity.blueTooth.Sendmessage("MOUSEMOVE"
									+ "::" + ((int) event.getY() - MemoryY)
									+ "," + (MemoryX - (int) event.getX()));
						}MainActivity.blueTooth.Sendmessage("MOUSEDOWN");

					} else if (DRAW_UNDERLINE) {
						MainActivity.blueTooth.Sendmessage("MOUSEDOWN");
					} else if (RETURN_MOUSE) {
						MainActivity.blueTooth.Sendmessage("RETURNMOUSE");
						RETURN_MOUSE = false;
					}

					MemoryX = (int) event.getX();
					MemoryY = (int) event.getY();
					break;
				}
				case MotionEvent.ACTION_UP: {
					MainActivity.blueTooth.Sendmessage("MOUSERELEASE");
					if(WRITE || DRAW_UNDERLINE){
						MainActivity.blueTooth.Sendmessage("MOUSEDOWN");
						MainActivity.blueTooth.Sendmessage("MOUSERELEASE");
					}
				}
				case MotionEvent.ACTION_MOVE: {

					MoveOfX = ((int) event.getY() - MemoryY);
					MoveOfY = (MemoryX - (int) event.getX());

					// 绘制手机上的图像
					if (WRITE) {
						canvas.drawLine(MemoryX, MemoryY, event.getX(),
								event.getY(), paint);
						backgroundView.setImageBitmap(bitmap);
					}

					if (MainActivity.blueTooth.BEGIN_OF_X
							|| MainActivity.blueTooth.END_OF_X) {
						MoveOfX = 0;
						MainActivity.blueTooth.BEGIN_OF_X = true;
						MainActivity.blueTooth.END_OF_X = true;
					}
					if (MainActivity.blueTooth.BEGIN_OF_Y
							|| MainActivity.blueTooth.END_OF_Y) {
						MoveOfY = 0;
						MainActivity.blueTooth.BEGIN_OF_Y = true;
						MainActivity.blueTooth.END_OF_Y = true;
					}
					// 发送移动的信息
					MainActivity.blueTooth.Sendmessage("MOUSEMOVE" + "::"
							+ MoveOfX + "," + MoveOfY);

					MemoryX = (int) event.getX();
					MemoryY = (int) event.getY();
					break;
				}
				default:
					break;
				}
			}
			return true;
		}

	};
}
