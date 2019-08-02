import com.virtenio.commander.io.DataConnection;
import com.virtenio.commander.toolsets.preon32.Preon32Helper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.DefaultLogger;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.*;
import java.util.*;
import java.util.Calendar;
import java.io.File;
import java.text.SimpleDateFormat;

public class connectionBasestation {
	Calendar cal = Calendar.getInstance();

	private BufferedWriter writer;
	private Scanner choice;
	private volatile boolean exit = false;

	private void context_set(String target) throws Exception {
		DefaultLogger consoleLogger = getConsoleLogger();
		// Prepare ant project
		File buildFile = new File("E:\\Sandbox\\buildUser.xml");
		Project antProject = new Project();
		antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
		antProject.addBuildListener(consoleLogger);

		try {
			antProject.fireBuildStarted();
			antProject.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			antProject.addReference("ant.ProjectHelper", helper);
			helper.parse(antProject, buildFile);
			antProject.executeTarget(target);
			antProject.fireBuildFinished(null);
		} catch (BuildException e) {
			e.printStackTrace();
		}
	}

	private void time_synchronize() throws Exception {
		DefaultLogger consoleLogger = getConsoleLogger();
		File buildFile = new File("E:\\Sandbox\\build.xml");
		Project antProject = new Project();
		antProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
		antProject.addBuildListener(consoleLogger);

		try {
			antProject.fireBuildStarted();
			antProject.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			antProject.addReference("ant.ProjectHelper", helper);
			helper.parse(antProject, buildFile);
			String target = "cmd.time.synchronize";
			antProject.executeTarget(target);
			antProject.fireBuildFinished(null);
		} catch (BuildException e) {
			e.printStackTrace();
		}
	}

	public void init() throws Exception {
		boolean start = true;
		try {
			Preon32Helper nodeHelper = new Preon32Helper("COM7", 115200); //harus disesuaikan sama com yang digunakan
			DataConnection conn = nodeHelper.runModule("BaseStation");

			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			int choiceentry = 0;
			String rallyMsg;
			choice = new Scanner(System.in);
			/// START MENU
			conn.flush();
			do {
				try {
					System.out.println("MENU");
					System.out.println("1. Lakukan Sinkronisasi"); 
					System.out.println("2. Minta Waktu Setiap Sensor");
					System.out.println("3. Exit");
					System.out.println("Pilihan Menu : ");

					choiceentry = choice.nextInt();
					conn.write(choiceentry);
					
					switch (choiceentry) {
					case 3: {
						exit = true;
						System.out.print("Keluar Program");
						break;
					}
					case 1: {
						if (start == true) {
							start = false;
							Thread.sleep(3000);
							System.out.println("Menunggu Sinkronisasi Waktu");
							Thread.sleep(3000);
							byte[] buffer = new byte[1024];
							while (in.available() > 0) {
								in.read(buffer);
								rallyMsg = new String(buffer);
								conn.flush();
								System.out.println(rallyMsg);
							}
						}
						start = true;
						break;
					}
					case 2: {
						if (start == true) {
							start = false;
							Thread.sleep(2500);
							System.out.println("Menunggu Waktu Sensor");
							Thread.sleep(2500);
							byte[] buffer = new byte[1024];
							while (in.available() > 0) {
								in.read(buffer);
								conn.flush();
								rallyMsg = new String(buffer);
								String name = "";
								long tempT;
								String[] array = rallyMsg.split(",");
								for (int i = 0; i < array.length - 1; i++) {
									if (i % 2 == 0) {
										name = array[i];
									} else {
										tempT = Long.parseLong(array[i]);
										SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
										String time = df.format(tempT);
										System.out.print(name + " : " + time);
									}
								}
							}
							System.out.println();
						}
						start = true;
						break;
					}
					}
		        } catch (InputMismatchException e){
		        	String bad_input = choice.nextLine();
		        	System.out.println("Input Tidak Sesuai");
		            continue;
		        }
			} while (choiceentry != 3);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static DefaultLogger getConsoleLogger() { // untuk memunculkan tulisan pada console
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		return consoleLogger;
	}

	public static void main(String[] args) throws Exception {
		connectionBasestation aGet = new connectionBasestation();
		aGet.context_set("context.set.5");
		aGet.time_synchronize();
		aGet.init();
	}
}