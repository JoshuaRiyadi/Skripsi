
import com.virtenio.preon32.examples.common.RadioInit;
import com.virtenio.preon32.examples.common.USARTConstants;
import com.virtenio.radio.ieee_802_15_4.Frame;
import com.virtenio.radio.ieee_802_15_4.FrameIO;
import com.virtenio.radio.ieee_802_15_4.RadioDriver;
import com.virtenio.radio.ieee_802_15_4.RadioDriverFrameIO;
import com.virtenio.radio.ieee_802_15_4.ThreadedFrameIO;
import com.virtenio.vm.Time;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import com.virtenio.io.TimeoutException;

import com.virtenio.driver.device.at86rf231.*;
import com.virtenio.driver.usart.NativeUSART;
import com.virtenio.driver.usart.USART;
import com.virtenio.driver.usart.USARTParams;
import com.virtenio.io.Console;
import com.virtenio.misc.PropertyHelper;

public class BaseStation {
	Time time = new Time();
	private volatile static boolean exit = false;

	static USART usart;
	private static OutputStream out;

	// Alamat sensor
	private int srcAddr = PropertyHelper.getInt("local.addr", 0xAFFE);

	// Alamat sensor yang terhubung dengan sensor ini
	private int neighbour[] = { 
			PropertyHelper.getInt("remote.addr", 0xBABE),
			PropertyHelper.getInt("remote.addr", 0xEABE),
			PropertyHelper.getInt("remote.addr", 0xDABE),
//			PropertyHelper.getInt("remote.addr", 0xFABE),
			PropertyHelper.getInt("remote.addr", 0xCABE) 
	};

	// Alamat broadcast
	private int broadcast = PropertyHelper.getInt("remote.addr", 0xFFFF);

	// Pan id yang digunakan
	private int cmnPanId = PropertyHelper.getInt("radio.panid", 0xCAFE);

	public void run() {
		try {
			final AT86RF231 radio = RadioInit.initRadio();
			radio.open();
			radio.setAddressFilter(cmnPanId, srcAddr, srcAddr, false);

			final RadioDriver rd = new AT86RF231RadioDriver(radio);
			final FrameIO fio = new RadioDriverFrameIO(rd);

			int fitur = usart.read();

			if (fitur == 1) {
				sendSynchronization(fio, fitur);
			} else if (fitur == 2) {
				sendReqTime(fio, fitur);
			} else if (fitur == 3) {
				exit = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendSynchronization(final FrameIO fio, int idFitur) throws Exception {
		if (idFitur == 1) {
			int ctn = 0;
			while (ctn < 10) {
				createMsgSync(fio, idFitur);
				ctn++;
				Thread.sleep(400);
			}
			System.out.print("Sinkronisasi Selesai");
		}
	}

	public void createMsgSync(final FrameIO fio, int idFitur) throws IOException {
		Frame frame = new Frame(Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
		frame.setSrcAddr(srcAddr);
		frame.setDestAddr(broadcast);
		frame.setDestPanId(cmnPanId);
		String message = "1," + time.currentTimeMillis();
		frame.setPayload(message.getBytes()); // Memasukan paket pesan ke payload
		fio.transmit(frame);
		String hex_addr = Integer.toHexString((int) frame.getSrcAddr());
		System.out.println(hex_addr + " send : " + message);
	}

	public void sendReqTime(final FrameIO fio, int idFitur) {
		String base = "Base," + time.currentTimeMillis() + ",";
		System.out.println(base);
		try {
			for (int j = 0; j < neighbour.length; j++) {
				String message = "2";
				Frame frame = new Frame(Frame.TYPE_DATA | Frame.DST_ADDR_16 | Frame.INTRA_PAN | Frame.SRC_ADDR_16);
				frame.setSrcAddr(srcAddr);
				frame.setDestAddr(neighbour[j]);
				frame.setDestPanId(cmnPanId);
				frame.setPayload(message.getBytes()); // Memasukan paket pesan ke payload
				fio.transmit(frame);
				String hex_addr = Integer.toHexString((int) frame.getSrcAddr());
				if (neighbour[j] == 0xABE) { // menerima waktu ke sensor node multihop
					Frame f = null;
					try {
						f = new Frame();
						int i = 0;
						while (i < 2) { // sebanyak node yang dijembatani + dirinya sendiri
							try {
								fio.setReceiveTimeout(2000);
								fio.receive(f);
								if (f != null) {
									byte[] dg = f.getPayload();
									String str = new String(dg, 0, dg.length);
									hex_addr = Integer.toHexString((int) f.getSrcAddr());
									char id = str.charAt(0);
									String idString = id + "";
									idFitur = Integer.parseInt(idString);
									if (idFitur == 3) {
										String timeRep = str.substring(2) + ",";
										System.out.println(timeRep);
									}
								}
								i++;
							} catch (TimeoutException e) {
								System.out.println("Timeout");
							}
						}
					} catch (Exception e) {

					}
				} else { // menerima waktu yang singlehop
					Frame f = null;
					try {
						f = new Frame();
						fio.setReceiveTimeout(2000);
						fio.receive(f);
					} catch (TimeoutException e) {
						System.out.println("Timeout");
					}
					if (f != null) {
						byte[] dg = f.getPayload();
						String str = new String(dg, 0, dg.length);
						hex_addr = Integer.toHexString((int) f.getSrcAddr());
						char id = str.charAt(0);
						String idString = id + "";
						idFitur = Integer.parseInt(idString);
						if (idFitur == 3) {
							String timeRep = str.substring(2) + ",";
							System.out.println(timeRep);
						}
					}
				}
			}
			System.out.println("Kirim Waktu Selesai");
		} catch (Exception e) {

		}
	}

	public static void useUSART() throws Exception {
		usart = configUSART();
	}

	private static USART configUSART() throws Exception {

		int instanceID = 0;
		USARTParams params = USARTConstants.PARAMS_115200;
		NativeUSART usart = NativeUSART.getInstance(instanceID);
		try {
			usart.close();
			usart.open(params);
			return usart;
		} catch (Exception e) {
			return null;
		}
	}

	public static void main(String[] args) throws Exception {

		try {
			useUSART();
			out = usart.getOutputStream();
		} catch (Exception e) {
			
		}
		BaseStation bs = new BaseStation();
		while (!exit) {
			bs.run();
		}
	}

}
