package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class LLLAParserException extends Exception{
	private int pos = -1;
	public LLLAParserException(String m){
		super(m);
	}
	
	public LLLAParserException(CharSequence cs, int position,TokenPattern tk){
		super(buildMessage(cs,position,tk));
		pos = position;
	}
	
	public LLLAParserException(LLLAParserException[] multiCause,int position){
		super(buildMessage(multiCause));
		pos = position;
	}
	
	public LLLAParserException(CharSequence cs,int position,String expectString){
		//int start = position - 25;
		//start = start<0?0:start;
		//CharSequence nearString = cs.subSequence(start,position);
		super(buildMessage(cs,position,expectString));
		pos = position;
	}
	
	public int getPosition(){
		return pos;
	}
	
	static String buildMessage(LLLAParserException[] multiCause){
		StringBuilder buf = new StringBuilder();
		for(int i=0;i<multiCause.length;i++){
			if(i!=0)
				buf.append("\n\r ----- or --------\n\r");
			buf.append(multiCause[i].getMessage());
		}
		return buf.toString();
	}
	
	static String buildMessage(CharSequence cs,int position,TokenPattern tk){
		return buildMessage(cs,position,tk.pattern.toString());
	}
	
	static String buildMessage(CharSequence cs,int position,String expectString){
		int start = position - 25;
		start = start<0?0:start;
		int end = position + 25;
		end = end>cs.length()?cs.length():end;
		
		
		return "at position "+position+",\r\n near: "+ cs.subSequence(start,position)
		+"\r\nfind: "+cs.subSequence(position,end)+"\r\nexpect: "+
		(expectString==null?"<EOF>":expectString);
	}
}
