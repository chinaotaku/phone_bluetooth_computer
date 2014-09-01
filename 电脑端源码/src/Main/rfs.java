package Main;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.imageio.ImageIO;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu.Separator;
import javax.tools.Tool;

import org.w3c.dom.css.Rect;

public class rfs {
	private static boolean end = false; // 用于判断是否结束程序
	private static TrayIcon trayicon; // 用于后台icon
	private static SystemTray tray; // 获得systemTray的defualt
	private static String channelMessage; // 得到蓝牙频道的信息

	private final static String soundFileName = "sound\\msg.wav";

	static final String serverUUID = "11111111111111111111111111111123";

	private static StreamConnectionNotifier server = null;

	private final static String spetor = "::";

	private final static int ScreenWidth = Toolkit.getDefaultToolkit()
			.getScreenSize().width;
	private final static int ScreenHeight = Toolkit.getDefaultToolkit()
			.getScreenSize().height;

	private static int RadioDistance = 1; // PC端的屏幕高度与安卓端屏幕宽度比例

	private static int MemoryX = 0; // PC端结束点鼠标的x
	private static int MemoryY = 0; // PC端结束点鼠标的y

	public rfs() throws IOException {
		channelMessage = "当前没有连接";

		server = (StreamConnectionNotifier) Connector.open("btspp://localhost:"
				+ serverUUID, Connector.READ_WRITE, true);

		ServiceRecord record = LocalDevice.getLocalDevice().getRecord(server);
		LocalDevice.getLocalDevice().updateRecord(record);

		// 默认设置鼠标的位置
		MemoryX = (int) MouseInfo.getPointerInfo().getLocation().getX();
		MemoryY = (int) MouseInfo.getPointerInfo().getLocation().getY();

	}

	public Session accept() {
		log("Accept");
		StreamConnection channel;
		try {
			log("Connectready");
			channel = server.acceptAndOpen();

			log("Connect");
			return new Session(channel);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void dispose() {
		log("Dispose");
		if (server != null)
			try {
				server.close();
			} catch (Exception e) {/* ignore */
			}
	}

	static class Session implements Runnable {
		private final Dimension screanSize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		private StreamConnection channel = null;
		private InputStream btIn = null;
		private OutputStream btOut = null;
		private boolean isPressAlt = false;

		public Session(StreamConnection channel) throws IOException {
			this.channel = channel;
			this.btIn = channel.openInputStream();
			this.btOut = channel.openOutputStream();
		}

		private SourceDataLine getLine(AudioFormat audioFormat) {
			SourceDataLine res = null;
			DataLine.Info info = new DataLine.Info(SourceDataLine.class,
					audioFormat);
			try {
				res = (SourceDataLine) AudioSystem.getLine(info);
				res.open(audioFormat);
			} catch (Exception e) {
			}
			return res;
		}

		public void PlaySound() throws UnsupportedAudioFileException,
				IOException {
			AudioInputStream adinput = AudioSystem
					.getAudioInputStream(new File(soundFileName));

			AudioFormat baseFormat = adinput.getFormat();
			SourceDataLine line = getLine(baseFormat);
			int BUFFER_SIZE = 4000 * 4;

			line.start();
			int inBytes = 0;
			byte[] audioData = new byte[BUFFER_SIZE];

			while (inBytes != -1) {
				inBytes = adinput.read(audioData, 0, BUFFER_SIZE);
				if (inBytes >= 0) {
					int outBytes = line.write(audioData, 0, inBytes);
				}
			}
		}

		public void run() {

			BufferedReader br = new BufferedReader(new InputStreamReader(btIn));
			try {
				int PointX = 0, PointY = 0; // x,y值用来记录前一次的鼠标位置

				Robot rob = new Robot();
				String point = "";
				int index = 0;
				while (true) {
					String Read = br.readLine();
					System.out.println(Read);
					try {
						if ((index = Read.indexOf(spetor)) != -1) {
							point = Read.substring(index + 2);
							Read = Read.substring(0, index);
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
						System.err.println("in NullPointerException");
						close();
						return;
					}

					log(Read);
					switch (Read) {
					case "INFORMATION": {
						channelMessage = point;

						try {
							PlaySound();
						} catch (UnsupportedAudioFileException e) {
							e.printStackTrace();
						}// 播放提示音
						new messageThread().start(); // 创建线程弹出提示框
						break;
					}
					case "UP": {

						rob.keyPress(KeyEvent.VK_UP);
						break;
					}
					case "DOWN": {
						rob.keyPress(KeyEvent.VK_DOWN);
						break;
					}
					case "LEFT": {
						rob.keyPress(KeyEvent.VK_LEFT);
						break;
					}
					case "RIGHT": {
						rob.keyPress(KeyEvent.VK_RIGHT);
						break;
					}
					case "ESC": {
						rob.keyPress(KeyEvent.VK_ESCAPE);
						break;
					}
					case "FULLSCREAN": {
						rob.keyPress(KeyEvent.VK_ALT);
						rob.keyRelease(KeyEvent.VK_ALT);
						rob.keyPress(KeyEvent.VK_S);
						rob.keyPress(KeyEvent.VK_C);
						break;
					}
					case "OFFLINE": {
						channelMessage = "当前没有连接";
						System.out.println("下线");
						try {
							PlaySound();
						} catch (UnsupportedAudioFileException e) {
							e.printStackTrace();
						}// 播放提示音
						new messageThread().start(); //创建线程弹出提示框
						close();
						return;
					}
					case "MOUSEMOVE": {
						index = point.indexOf(",");
						String x = point.substring(0, index);
						String y = point.substring(index + 1);

						System.out.println(x + " " + y);

						PointX = (Integer.parseInt(x)) + MemoryX;
						PointY = (Integer.parseInt(y)) + MemoryY;
						MemoryX = PointX;
						MemoryY = PointY;

						if (PointX <= 0) {
							MemoryX = 0;
							sendMessage("BEGIN_OF_X");
						}
						if (PointX >= ScreenWidth) {
							MemoryX = ScreenWidth;
							sendMessage("END_OF_X");
						}
						if (PointY <= 0) {
							MemoryY = 0;
							sendMessage("BEGIN_OF_Y");
						}
						if (PointY >= ScreenHeight) {
							MemoryY = ScreenHeight;
							sendMessage("END_OF_Y");
						}

						rob.mouseMove(PointX, PointY);
						break;
					}
					case "SCREENSIZE": {
						index = point.indexOf(",");
						String x = point.substring(0, index);
						String y = point.substring(index + 1);
						System.out.println(x + "  " + y);
						RadioDistance = ScreenHeight / Integer.parseInt(x);
						break;
					}
					case "MOUSEDOWN": {
						rob.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						break;
					}
					case "MOUSERELEASE": {
						rob.mouseRelease(InputEvent.BUTTON1_MASK);
						break;
					}
					case "CHANGEPCB": {
						if (!isPressAlt) {
							rob.keyPress(KeyEvent.VK_ALT);
							isPressAlt = true;
						}
						rob.keyPress(KeyEvent.VK_TAB);
						break;
					}
					case "SELECT": {
						if (isPressAlt) {
							rob.keyRelease(KeyEvent.VK_ALT);
							isPressAlt = false;
						}
						break;
					}
					case "RETURNMOUSE": {
						rob.keyPress(KeyEvent.VK_R);
						break;
					}
					case "DRAW": {
						rob.keyPress(KeyEvent.VK_C);
						break;
					}
					case "CLEAR": {
						rob.keyPress(KeyEvent.VK_E);
						break;
					}
					case "MARK": {
						rob.keyPress(KeyEvent.VK_F);
						break;
					}
					case "ERASER": {
						rob.keyPress(KeyEvent.VK_G);
						break;
					}
					default:
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("in IOException");
				close();
				return;
			} catch (AWTException e) {
				e.printStackTrace();
				System.err.println("in AWTException");
				close();
				return;
			}
		}

		class messageThread extends Thread {
			@Override
			public void run() {
				showMessage();
				super.run();
			}
		}

		/**
		 * 向外发送信息
		 * 
		 * @param 文件发送
		 */
		public void sendMessage(String msg) {
			// 通过输入字符来控制连接端的电脑
			try {
				btOut.write(msg.getBytes());
				btOut.write("\n".getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void close() {
			try {
				log("Session Close");
				if (btIn != null)
					channel.close();
				btIn.close();
			} catch (Exception e) {/* ignore */
			}
			if (channel != null)
				try {
				} catch (Exception e) {/* ignore */
				}
		}
	}

	public static void removeTrayicon() {
		// try {
		// server.close();
		// } catch (IOException e) {
		// // TODO 自动生成的 catch 块
		// e.printStackTrace();
		// }
		tray.remove(trayicon);
	}

	public static void showMessage() {
		JOptionPane.showMessageDialog(null, channelMessage, "连接信息",
				JOptionPane.INFORMATION_MESSAGE);
	}

	// ------------------------------------------------------
	public static void main(String[] args) throws Exception {

		if (SystemTray.isSupported()) {
			tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().createImage(
					"icon\\bluetooth.png");

			if (image == null)
				System.out.println("null image");

			String title = "蓝牙";
			PopupMenu popup = new PopupMenu();
			MenuItem showItem = new MenuItem("连接信息");
			showItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					showMessage();
				}
			}); // 显示当前信息

			MenuItem exitItem = new MenuItem("退出");
			exitItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					removeTrayicon(); // 调用静态函数去控制后台图标的删除同时结束程序
					System.exit(0);
				}
			}); // 结束程序

			popup.add(showItem);
			popup.add(exitItem);

			trayicon = new TrayIcon(image, title, popup);
			trayicon.setImageAutoSize(true);

			tray.add(trayicon);

			int count = 0;
			rfs server = new rfs();
			while (true) {
				Session session = server.accept();
				new Thread(session).start();
			}
		} else {
			System.out.println("Exit Thread!");
		}
	}

	private static void log(String msg) {
		System.out.println("[" + (new Date()) + "] " + msg);
	}
}
