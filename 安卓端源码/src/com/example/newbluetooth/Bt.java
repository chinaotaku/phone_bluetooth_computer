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
	private OutputStream btOut = null; // ���
	private InputStream btIn = null; // ����
	private final String TAG = getClass().getSimpleName();
	private String msg = "";
	private boolean CONNECT = false;
	private static final int SIZE = 1027; // �����С
	public boolean BEGIN_OF_X = false;
	public boolean END_OF_X = false;
	public boolean BEGIN_OF_Y = false;
	public boolean END_OF_Y = false;
	private String[] blueAddress = new String[50]; // �����Ѿ����յ���������ַ�ж�
	private int index = 0; // Ŀ¼���������

	public static int SCREENWIDTH;
	public static int SCREENHEIGHT;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				Log.d(TAG, "ACTION_FOUND");
				// ���ҵ�device��ʱ�򡢴�Intentȡ��BluetoothDevice
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (!isExist(device.getAddress())) {
					// �������ƺ͵�ַ�����
					mArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}

				insertAddress(device.getAddress(), index);
				index++;
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				Log.d(TAG, "ACTION_DISCOVERY_FINISHED");
				// ��device�������ʱ�����BroadcastReceiver
				context.unregisterReceiver(mReceiver);
			}
		}
	};

	/**
	 * ���ڸ���ַ�ж�����ӵ�ַ
	 * 
	 * @param add
	 *            ������ַ
	 * @param index
	 *            Ŀ¼
	 */
	private void insertAddress(String add, int index) {
		blueAddress[index] = add;
	}

	/**
	 * �ж������Ƿ�����ƶ������ĵ�ַ
	 * 
	 * @param add
	 *            ������ַ
	 * @return ������������ҵ��˵�ַ�ͷ���true������Ϊfalse
	 */
	private boolean isExist(String add) {
		for (int i = 0; i < blueAddress.length; i++) {
			Log.e(String.valueOf(i) + "   ", String.valueOf(blueAddress[i]));
			Log.e("add", add);
			if (blueAddress[i] == null) {
				Log.e("���ڷ�ķ���", "������");
				return false;
			} else if (blueAddress[i].equals(add)) {
				Log.e("���ڷ�ķ���", "����");
				return true;
			}
		}
		Log.e("���ڷ�ķ���", "������");
		return false;
	}

	public void clearBlueAddress() {
		blueAddress = new String[50];
		Log.e("���", "�Ѿ����");
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
		// ���ɿͻ���
		mConnectThread = new ConnectThread(address);
		mConnectThread.start();
	}

	public boolean IsHaveBt() {
		if (mBluetoothAdapter == null) {
			// ��֧����������
			return false;
		} else {
			return true;
		}
	}

	public void TurnOnBt() {
		if (!mBluetoothAdapter.isEnabled()) {
			// ����û�д�,���ظ����������
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
	 * ������Ϣ���Է�
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
		private MediaPlayer soundHinter; // ý�岥�������ڲ�����ʾ��
		private AssetManager am; // ����assets
		private Vibrator vibrator; // �����ֻ���

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
					// ���ر����������֮�����Ӿ��Զ�����Ϊfalse��Ϊ��ֹ��������
					CONNECT = false;
				}

				btOut = mmSocket.getOutputStream();
				btIn = mmSocket.getInputStream();
				CONNECT = true;

				// ������ʾ��
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
					long[] pattern = { 100, 400 }; // ֹͣ ���� ֹͣ ����
					vibrator.vibrate(pattern, -1); // �ظ����������pattern
													// ���ֻ����һ�Σ�index��Ϊ-1
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("������ʾ��ʱ����", "");
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

			// �����ֻ��˵���Ļ�ߴ�
			DisplayMetrics dism = new DisplayMetrics();
			dism = mainActivity.getResources().getDisplayMetrics();
			Sendmessage("SCREENSIZE" + "::" + dism.widthPixels + ","
					+ dism.heightPixels);

			// ��������������Ϣ
			Sendmessage("INFORMATION" + "::" + "Product Model: "
					+ android.os.Build.MODEL + ","
					+ android.os.Build.VERSION.SDK + ","
					+ android.os.Build.VERSION.RELEASE);

			// ���Զ˵ļ���
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
		 * ����Ϣ����������
		 * 
		 */
		private void outDataMethod(OutputStream btOut) {

			// ͨ�������ַ����������Ӷ˵ĵ���
			try {
				btOut.write(msg.getBytes());
				btOut.write("\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * �ر�socket
		 * 
		 */
		public void cancel() {
			try {
				mmSocket.close();
				// ��socket�رպ���������Ϊfalse��Ϊ��ֹ��������
				CONNECT = false;
			} catch (IOException e) {
			}
		}
	}
}
