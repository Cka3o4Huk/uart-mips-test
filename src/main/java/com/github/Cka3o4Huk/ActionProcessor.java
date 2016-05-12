package com.github.Cka3o4Huk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ActionProcessor {

	public static Boolean testStatus = null;
	public static RuntimeException error = null;
	
	public static ArrayList<Action> actions = new ArrayList<>();
	
	public static void register(Action e){
		actions.add(e);
	}
	
	public static boolean process(String line, BufferedWriter writer) throws InterruptedException, IOException{
		for(Action action : actions)
			if(action.perform(line, writer))
				return true;
		return false;
	}
	
	public static void result(Action action){
		ActionProcessor.testStatus = action.isSuccess();
		if(!testStatus){
			error = new RuntimeException(action.out());
		}
	}
}
