import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.shuttle.Shuttle;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;
import com.virtenio.vm.Time;

import java.io.IOException;
import java.util.ArrayList;

import com.virtenio.driver.device.at86rf231.*;
import com.virtenio.driver.led.LED;
import com.virtenio.io.Console;
import com.virtenio.misc.PropertyHelper;

public class Node {
	Time time = new Time();
	FrameIO fio = null;
	private long[] timeRef = new long[3]; // tempat penyimpanan waktu referensi
	private long[] timeLok = new long[3]; // tempat penyimpanan waktu lokal
	private ArrayList<Frame> frames;

	// The local address
	private int srcAddr = PropertyHelper.getInt("local.addr", 0xEABE);

	// the remote address, address of the receiver
	private int neighbour[] = { 
//			PropertyHelper.getInt("remote.addr", 0xABE),
			PropertyHelper.getInt("remote.addr", 0xAFFE),
//			PropertyHelper.getInt("remote.addr", 0xFABE),
//			PropertyHelper.getInt("remote.addr", 0xCABE),
//			PropertyHelper.getInt("remote.addr", 0xBABE),
//			PropertyHelper.getInt("remote.addr", 0xEABE),
//			PropertyHelper.getInt("remote.addr", 0xDABE) 
	};

	private int broadcast = PropertyHelper.getInt("remote.addr", 0xFFFF);

	// the panID to use
	private int cmnPanId = PropertyHelper.getInt("radio.panid", 0xCAFE);

	public Node() throws Exception {
		frames = new ArrayList<Frame>();
	}

	public void run() {
		System.out.println(Integer.toHexString(srcAddr) + " dengan nilai waktu : " + time.currentTimeMillis());
		System.out.println("----------------------------------------------------------");
		try {
			final AT86RF231 radio = RadioInit.initRadio();
			radio.open();
			radio.setAddressFilter(cmnPanId, srcAddr, srcAddr, false);

			final RadioDriver rd = new AT86RF231RadioDriver(radio);
			final FrameIO fio = new RadioDriverFrameIO(rd);

			receive(fio);
			insert(fio);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insert(final FrameIO fio) {
		Thread insert = new Thread() {
			@Override
			public void run() {
				while (true) {
					Frame f = null;
					try {
						f = new Frame();
//						System.out.println("Menunggu Pesan Masuk");
						fio.receive(f);
						frames.add(f);
					} catch (Exception e) {

					}
				}
			}
		};
		insert.start();
	}

	public void sendMsgBroadcast(final FrameIO fio, String message) throws IOException {
		Frame frame = new Frame(Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		frame.setSrcAddr(srcAddr);
		frame.setDestAddr(broadcast);
		frame.setDestPanId(cmnPanId);
		frame.setPayload(message.getBytes());
		fio.transmit(frame);
	}

	public void sendMsgMulticast(final FrameIO fio, String message, int i) throws IOException {
		Frame frame = new Frame(Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		frame.setSrcAddr(srcAddr);
		frame.setDestAddr(neighbour[i]);
		frame.setDestPanId(cmnPanId);
		frame.setPayload(message.getBytes());
		fio.transmit(frame);
	}

	public void receive(final FrameIO fio) throws Exception {
		Thread reader = new Thread() {
			@Override
			public void run() {
				String source = Integer.toHexString((int) srcAddr);
				int countRef = 0; // jumlah waktu referensi
				while (true) { 
					while (!frames.isEmpty()) {
						Frame f = frames.remove(0);
						String rallyMsg = "";
						byte[] dg = f.getPayload();
						String str = new String(dg, 0, dg.length);
						char id = str.charAt(0);
						String idString = id + "";
						int idFitur = Integer.parseInt(idString);
						String desAddr = Integer.toHexString((int) f.getDestAddr()); // alamat penerima
						String srcAddr = Integer.toHexString((int) f.getSrcAddr()); // alamat pengirim

						if (idFitur == 3 || idFitur == 1 || idFitur == 5) {
							rallyMsg = str.substring(2); // untuk mendapatkan pesan operan
							long timeRefVal = 0;
							if (rallyMsg.charAt(0) >= '0' && rallyMsg.charAt(0) <= '9') {
								timeRefVal = Long.parseLong(rallyMsg);
							}
							System.out.println(source + " receive : " + str);

							if (idFitur == 1 || idFitur == 5) {
								if (timeRef[countRef] == 0) {
									timeRef[countRef] = timeRefVal;
									timeLok[countRef] = time.currentTimeMillis();
									System.out.println("Waktu Global " + (countRef + 1) + " : " + timeRef[countRef]);
									System.out.println("Waktu Lokal       : " + timeLok[countRef]);
									System.out.println("Selisih Waktu     : " + Math.abs(timeRef[countRef] - timeLok[countRef]));
									countRef++;
								}
								if (countRef == timeRef.length) {
									synchronization(countRef);
									countRef = 0;
								}
							}
						}
						if (idFitur == 1) {
							String tempExchange = str.substring(2);
							long exchange = Long.parseLong(tempExchange);
							try {
								String message = "5," + exchange;
								sendMsgBroadcast(fio,message);
								System.out.println(source + " send : " + message);
							} catch (Exception e) {

							}
						} else if (idFitur == 2) {
							try {
								String message = "2";  //untuk melanjutkan permintaan waktu sensor
								int idx = 1; 
								sendMsgMulticast(fio, message, idx);
								System.out.println("----------------------------------------------------------");
								System.out.println(source + " send : " + message);
							} catch (Exception e) {
								System.out.println("ERROR: no receiver 2");
							}
							try {
								String message = "3," + source + "," + time.currentTimeMillis(); //untuk kirim waktu sensor
								int idx = 0;
								sendMsgMulticast(fio, message, idx);
								System.out.println(source + " send : " + message);
								System.out.println("----------------------------------------------------------");
							} catch (Exception e) {
								System.out.println("ERROR: no receiver 3a");
							}
						} else if (idFitur == 3) {
							try {
								String message = "3," + rallyMsg; // untuk mengoper waktu sensor
								int idx = 0;
								sendMsgMulticast(fio, message, idx);
								System.out.println(source + " send : " + message);
								System.out.println("Pesan Operan");
								System.out.println("----------------------------------------------------------");
							} catch (Exception e) {
								System.out.println("ERROR: no receiver 3b");
							}
						}
					}
				}
			}
		};
		reader.start();
	}

	public void synchronization(int countRef) {
		long temp = 0;
		for (int i = 0; i < timeRef.length; i++) {
			temp += timeRef[i] - timeLok[i];
		}
		long offsetTime = temp / countRef;
		time.setCurrentTimeMillis(time.currentTimeMillis() + offsetTime);
		System.out.println("Sinkronisasi Berhasil, waktu lokal saat ini : " + time.currentTimeMillis());
		System.out.println("----------------------------------------------------------");
		timeRef = new long[timeRef.length];	
	}

	public static void main(String[] args) throws Exception {
		Node node = new Node();
		node.run();
	}

}
