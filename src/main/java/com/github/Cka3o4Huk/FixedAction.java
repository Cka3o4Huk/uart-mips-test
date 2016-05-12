package com.github.Cka3o4Huk;

public class FixedAction implements Action {

	private String prefix = "notexists";
	private boolean isTestFinished = false;
	private boolean isTestFailed = false;
	private int outputDelay = 0;
	private String out = "";
	private boolean isNewLineRequired = false;
	
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
		return !isTestFailed;
	}

}
