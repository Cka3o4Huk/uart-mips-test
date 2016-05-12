package com.github.Cka3o4Huk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class CallUnixWrapper {
	static BufferedReader br;
	static BufferedWriter bw;
	static BufferedWriter log;

	public static void configure(InputStream is, OutputStream os, boolean stdout) throws IOException {
		br = new BufferedReader(new InputStreamReader(is));
		bw = new BufferedWriter(new OutputStreamWriter(os));
		log = new BufferedWriter((stdout) ? new PrintWriter(System.out) : new FileWriter("log.lst"));
	}

	public static void prepareShutdown(Process p) {
		Thread closeChildThread = new Thread() {
			public void run() {
				if(p.isAlive())
					p.destroy();
			}
		};

		Runtime.getRuntime().addShutdownHook(closeChildThread);
	}

	public static void process() throws IOException, InterruptedException {
		String line;
		while ((line = br.readLine()) != null) {
			log.write(line);
			log.newLine();
			log.flush();

			if (ActionProcessor.process(line, bw))
				break;
		}
		
		Thread.sleep(200);
		
		while(br.ready()){
			log.write(br.read());
		}
		
		log.flush();
		bw.close();
		br.close();
		log.close();
		Thread.sleep(300);
	}
	
	public static String tftpBootCustomKernel(String ip){
		return "boot -tftp -raw -addr=0x80800000 -max=0x770000 " + ip + ":kernel.BCM.tramp.bin";
	}
	
	public static void initActions(boolean customKernel, String ip) {
		new FixedAction().ifGet("FreeBSD/mips (freebsd-wifi)").out("root").withNewLine().register();
		new FixedAction().ifGet("login: root").out("uname -a").delay(100).withNewLine().register();
		new FixedAction().ifGet("# uname -a").out("devinfo -r").delay(100).withNewLine().register();
		new FixedAction().ifGet("# devinfo -r").out("hostname").delay(500).withNewLine().register();
		new FixedAction().ifGet("# hostname").finishTest().register();
		new FixedAction().ifGet("all ports busy").failTest().out("Another CU is running").register();
		
		if (customKernel){
			new FixedAction().ifGet("Init Arena").out("" + ((char) 3)).register();
			new FixedAction().ifGet("Startup canceled").out(tftpBootCustomKernel(ip)).withNewLine().register();
			new FixedAction().ifGet("*** command status = -21").failTest().out("No ethernet connectivity between router and PC").register();
		}
	}

	public static void main(String[] args) {
		boolean customKernel = false;
		boolean stdout = false;
		String ip = "";
		
		for(String arg : args){
			if(arg.equals("-c"))
				customKernel = true;
			
			if(arg.equals("-s"))
				stdout = true;
			
			if(arg.startsWith("-i"))
				ip = arg.substring(2);
		}
		ProcessBuilder pb = new ProcessBuilder("cu", "-115200", "-l", "cuaU0");
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStream is = p.getInputStream();
			OutputStream os = p.getOutputStream();
			initActions(customKernel, ip);
			prepareShutdown(p);
			configure(is, os, stdout);
			process();
			p.destroy();
			if(!ActionProcessor.testStatus)
				throw ActionProcessor.error;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return;
	}

}
