package com.example.newbluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import org.apache.http.cookie.SM;

import android.R.bool;
import android.R.dimen;
import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings.System;
import android.util.Base64InputStream;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

public class Bt {
	private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private final Activity mainActivity;
	private static final int REQUEST_ENABLE_BT = 0x1;
	private final ArrayAdapter<String> mArrayAdapter;
	private ConnectThread mConnectThread;
	private OutputStream btOut = null; // 输出
	private InputStream btIn = null; // 输入
	private final String TAG = getClass().getSimpleName();
	private String msg = "";
	private boolean CONNECT = false;
	private static final int SIZE = 1027; // 缓冲大小
	public boolean BEGIN_OF_X = false;
	public boolean END_OF_X = false;
	public boolean BEGIN_OF_Y = false;
	public boolean END_OF_Y = false;
	private String[] blueAddress = new String[50]; // 用于已经接收到的蓝牙地址判断
	private int index = 0; // 目录，方便计算

	public static int SCREENWIDTH;
	public static int SCREENHEIGHT;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.d(TAG, "ACTION_FOUND");
				// 当找到device的时候、从Intent取出BluetoothDevice
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (!isExist(device.getAddress())) {
					// 根据名称和地址来存放
					mArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}

				insertAddress(device.getAddress(), index);
				index++;
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
				// 当device搜索完毕时、解除BroadcastReceiver
				context.unregisterReceiver(mReceiver);
			}
		}
	};

	/**
	 * 用于给地址判断来添加地址
	 * 
	 * @param add
	 *            蓝牙地址
	 * @param index
	 *            目录
	 */
	private void insertAddress(String add, int index) {
		blueAddress[index] = add;
	}

	/**
	 * 判断数组是否存在制定蓝牙的地址
	 * 
	 * @param add
	 *            蓝牙地址
	 * @return 如果在数组中找到了地址就返回true，否则为false
	 */
	private boolean isExist(String add) {
		for (int i = 0; i < blueAddress.length; i++) {
			Log.e(String.valueOf(i) + "   ", String.valueOf(blueAddress[i]));
			Log.e("add", add);
			if (blueAddress[i] == null) {
				Log.e("存在否的返回", "不存在");
				return false;
			} else if (blueAddress[i].equals(add)) {
				Log.e("存在否的返回", "存在");
				return true;
			}
		}
		Log.e("存在否的返回", "不存在");
		return false;
	}

	public void clearBlueAddress() {
		blueAddress = new String[50];
		Log.e("清除", "已经清除");
	}

	public boolean Isconnect() {
		return CONNECT;
	}

	public Bt(Activity acti, ArrayAdapter<String> aAD) {
		mainActivity = acti;
		mArrayAdapter = aAD;
	}

	public void connect(String address) {
		int index;
		if ((index = address.indexOf("\n")) != -1) {
			address = address.substring(index + 1);
		}
		// 生成客户端
		mConnectThread = new ConnectThread(address);
		mConnectThread.start();
	}

	public boolean IsHaveBt() {
		if (mBluetoothAdapter == null) {
			// 不支持蓝牙功能
			return false;
		} else {
			return true;
		}
	}

	public void TurnOnBt() {
		if (!mBluetoothAdapter.isEnabled()) {
			// 蓝牙没有打开,就重复请求打开蓝牙
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			mainActivity.startActivityForResult(enableBtIntent,
					REQUEST_ENABLE_BT);
		}
	}

	public void searchBlutooth() {
		mArrayAdapter.clear();
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mainActivity.registerReceiver(mReceiver, filter); // Don't forget to
															// unregister during
															// onDestroy
		mBluetoothAdapter.startDiscovery();
	}

	public ArrayAdapter<String> getDefualtAdapter() {
		mArrayAdapter.clear();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				mArrayAdapter
						.add(device.getName() + "\n" + device.getAddress());
			}
		}
		;
		return mArrayAdapter;
	}

	public void endSearchBluetooth() {
		Log.d("endSearchBluetooth", "endSearchBluetooth");
		index = 0;
		for (int i = 0; i < blueAddress.length; i++) {
			blueAddress[i] = null;
		}
		mBluetoothAdapter.cancelDiscovery();
	}

	/**
	 * 发送消息给对方
	 * 
	 * @param KumaHime
	 */
	public void Sendmessage(String m) {
		msg = m;
		mConnectThread.outDataMethod(btOut);
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private MediaPlayer soundHinter; // 媒体播放器用于播放提示音
		private AssetManager am; // 调控assets
		private Vibrator vibrator; // 用于手机震动

		public ConnectThread(String address) {
			soundHinter = new MediaPlayer();
			soundHinter.setAudioStreamType(AudioManager.STREAM_RING);

			am = mainActivity.getAssets();

			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;

			mmDevice = mBluetoothAdapter.getRemoteDevice(address);

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = mmDevice.createRfcommSocketToServiceRecord(UUID
						.fromString("11111111-1111-1111-1111-111111111123"));
			} catch (IOException e) {
			}
			mmSocket = tmp;
		}

		public void run() {
			// // Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();

				// Toast.makeText(mainActivity, "cccc",
				// Toast.LENGTH_SHORT).show();
				if (btIn != null) {
					btIn.close();
					CONNECT = false;
				}
				if (btOut != null) {
					btOut.close();
					// 当关闭输入输出流之后连接就自动设置为false，为防止出现意外
					CONNECT = false;
				}

				btOut = mmSocket.getOutputStream();
				btIn = mmSocket.getInputStream();
				CONNECT = true;

				// 播放提示音
				try {
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					AssetFileDescriptor fd = am.openFd("sound.wav");
					soundHinter.reset();
					soundHinter.setDataSource(fd.getFileDescriptor(),
							fd.getStartOffset(), fd.getLength());
					soundHinter.prepare();
					soundHinter.start();
					vibrator = (Vibrator) mainActivity
							.getSystemService(Context.VIBRATOR_SERVICE);
					long[] pattern = { 100, 400 }; // 停止 开启 停止 开启
					vibrator.vibrate(pattern, -1); // 重复两次上面的pattern
													// 如果只想震动一次，index设为-1
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("播放提示音时出错", "");
				}

			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
					CONNECT = false;
				} catch (IOException closeException) {
					return;
				}
				return;
			}

			// 发送手机端的屏幕尺寸
			DisplayMetrics dism = new DisplayMetrics();
			dism = mainActivity.getResources().getDisplayMetrics();
			Sendmessage("SCREENSIZE" + "::" + dism.widthPixels + ","
					+ dism.heightPixels);

			// 发送蓝牙连接信息
			Sendmessage("INFORMATION" + "::" + "Product Model: "
					+ android.os.Build.MODEL + ","
					+ android.os.Build.VERSION.SDK + ","
					+ android.os.Build.VERSION.RELEASE);

			// 电脑端的监听
			inDataMethodLoop(btIn);
		}

		private void inDataMethodLoop(InputStream in) {

			BufferedInputStream bIn = new BufferedInputStream(in);
			while (true) {
				String Read;
				BufferedReader bReader = new BufferedReader(
						new InputStreamReader(bIn));
				try {
					Read = bReader.readLine();
					if (Read == "BEGIN_OF_X") {
						Sendmessage("BEGIN_OF_X");
						BEGIN_OF_X = true;
						break;
					} else if (Read == "END_OF_X") {
						Sendmessage("END_OF_X");
						END_OF_X = true;
						break;
					} else if (Read == "BEGIN_OF_Y") {
						Sendmessage("BEGIN_OF_Y");
						BEGIN_OF_Y = true;
						break;
					} else if (Read == "END_OF_Y") {
						Sendmessage("END_OF_Y");
						END_OF_Y = true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 将消息传给服务器
		 * 
		 */
		private void outDataMethod(OutputStream btOut) {

			// 通过输入字符来控制连接端的电脑
			try {
				btOut.write(msg.getBytes());
				btOut.write("\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 关闭socket
		 * 
		 */
		public void cancel() {
			try {
				mmSocket.close();
				// 当socket关闭后连接设置为false，为防止发生意外
				CONNECT = false;
			} catch (IOException e) {
			}
		}
	}
}
