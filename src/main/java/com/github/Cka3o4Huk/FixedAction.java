package com.github.Cka3o4Huk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class FixedAction implements Action {

	private String prefix = "notexists";
	private boolean isTestFinished = false;
	private boolean isTestFailed = false;
	private int outputDelay = 0;
	private String out = "";
	private boolean isNewLineRequired = false;
	private boolean interactive;
	
	public FixedAction(){
		
	}
	
	public FixedAction(boolean interactive){
		this.interactive = interactive;
	}
	
	public FixedAction withNewLine() {
		this.isNewLineRequired = true;
		return this;
	}
	
	public FixedAction out(String out) {
		this.out = out;
		return this;
	}
	
	public FixedAction delay(int outputDelay) {
		this.outputDelay = outputDelay;
		return this;
	}
	
	public FixedAction ifGet(String prefix) {
		this.prefix = prefix;
		return this;
	}
	
	public FixedAction finishTest() {
		this.isTestFinished = true;
		return this;
	}
	
	public FixedAction failTest() {
		this.isTestFailed = true;
		return finishTest();
	}
	
	public void register(){
		ActionProcessor.register(this);
	}
	
	@Override
	public boolean taste(String line) {
		return line.startsWith(prefix);
	}

	@Override
	public boolean isTestFinished() {
		return isTestFinished;
	}

	@Override
	public int outputDelay() {
		return outputDelay;
	}

	@Override
	public String out() {
		return out;
	}

	@Override
	public boolean isNewLineRequired() {
		return isNewLineRequired;
	}
	
	public boolean isSuccess(){
		if(isTestFailed && interactive){
			try {
				pollTerminal(CallUnixWrapper.br,CallUnixWrapper.bw);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return !isTestFailed;
	}
	
	public void performOut(BufferedReader reader, BufferedWriter writer) throws IOException {
		writer.write(out());
		if(isNewLineRequired())
			writer.newLine();
		
		writer.flush();
	}
	
	
	public void performInternal(BufferedReader reader, BufferedWriter writer) throws IOException{
		if(interactive)
			pollTerminal(reader,writer);
		else
			performOut(reader,writer);
	}
	
	public void pollTerminal(BufferedReader reader, BufferedWriter writer) 
			throws IOException {
		
		System.out.println("[CUW] pollTerminal");
		
		while(true){
			while(reader.ready()){
				System.out.print((char)reader.read());
			}
			
			while (System.in.available() > 0){
				int b = System.in.read();
				if (b >=0 )
					writer.append((char)b);
				else 
					return;
			}
			writer.flush();
						
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
	}

}
