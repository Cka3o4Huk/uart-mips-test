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
	public static BufferedReader br;
	public static BufferedWriter bw;
	public static BufferedWriter log;
	public static BufferedReader console;

	public static void configure(InputStream is, OutputStream os, boolean stdout) throws IOException {
		br = new BufferedReader(new InputStreamReader(is));
		bw = new BufferedWriter(new OutputStreamWriter(os));
		log = new BufferedWriter((stdout) ? new PrintWriter(System.out) : new FileWriter("log.lst"));
		console = new BufferedReader(new InputStreamReader(System.in));
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

			if (ActionProcessor.process(line, br, bw))
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
		return "boot -tftp -raw -addr=0x80900000 -max=0x770000 " + ip + ":kernel.BCM.tramp.bin";
	}
	
	public static void initActions(String mode, String ip, boolean interactive) {
		System.out.println("Using mode: " + mode);
		new FixedAction().ifGet("FreeBSD/mips (freebsd-wifi)").out("root").withNewLine().register();
		new FixedAction().ifGet("login: root").out("uname -a").delay(100).withNewLine().register();
		new FixedAction().ifGet("# uname -a").out("devinfo -r").delay(100).withNewLine().register();
		new FixedAction().ifGet("# devinfo -r").out("kenv").delay(500).withNewLine().register();
		new FixedAction().ifGet("# kenv").out("ifconfig").delay(500).withNewLine().register();
		new FixedAction().ifGet("# ifconfig").out("ls /dev").delay(500).withNewLine().register();
		new FixedAction().ifGet("# ls /dev").out("hostname").delay(500).withNewLine().register();
		new FixedAction(interactive).ifGet("# hostname").failTest().register();
		new FixedAction().ifGet("all ports busy").failTest().out("Another CU is running").register();
		
		switch(mode){
		case "CUSTOM":
			new FixedAction().ifGet("Init Arena").out("" + ((char) 3)).register();
			new FixedAction().ifGet("Startup canceled").out(tftpBootCustomKernel(ip)).withNewLine().register();
			new FixedAction().ifGet("*** command status = -21").failTest().out("No ethernet connectivity between router and PC").register();
			new FixedAction().ifGet("*** command status = -24").failTest().out("No ethernet connectivity between router and PC (different domains)").register();
			break;
		case "FWUPLOAD":
			new FixedAction().ifGet("FreeBSD is a registered trademark of The FreeBSD Foundation.")
				.failTest().out("Standard boot, no FW update").register();
			new FixedAction().ifGet("done. ").finishTest().register();
			break;
		}
		
		new FixedAction(interactive).ifGet("Manual root filesystem specification").failTest().out("NO MOUNT").register();
		new FixedAction(interactive).ifGet("Stopped at ").failTest().out("KERNEL PANIC!").register();
	}

	public static void main(String[] args) {
		String mode = "";
		boolean stdout = false;
		boolean interactive = false;
		String ip = "";
		
		for(String arg : args){
			if(arg.startsWith("-c="))
				mode = arg.substring(3);
			
			if(arg.equals("-s"))
				stdout = true;
			
			if(arg.equals("-p"))
				interactive = true;
			
			if(arg.startsWith("-i="))
				ip = arg.substring(3);
		}
		ProcessBuilder pb = new ProcessBuilder("cu", "-115200", "-l", "cuaU0");
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			InputStream is = p.getInputStream();
			OutputStream os = p.getOutputStream();
			initActions(mode, ip, interactive);
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
