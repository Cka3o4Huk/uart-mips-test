package com.github.Cka3o4Huk;

import java.io.BufferedWriter;
import java.io.IOException;

public interface Action {
	public boolean taste(String line);
	public boolean isSuccess();
	public boolean isTestFinished();
	public int outputDelay();
	public String out();
	public boolean isNewLineRequired();
	
	public default boolean perform(String line, BufferedWriter writer) throws InterruptedException, IOException{
		if(taste(line)){
			if(isTestFinished()){
				ActionProcessor.result(this);
				return true;
			}
			
			if(outputDelay() > 0)
				Thread.sleep(outputDelay());
			
			writer.write(out());
			if(isNewLineRequired())
				writer.newLine();
			
			writer.flush();
		}
		return false;
	}
}
