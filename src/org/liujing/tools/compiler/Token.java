package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class Token{
	protected TokenPattern tokenPattern;
	protected Token next;
	protected String image;
	protected int startPos = -1;
	protected int endPos = -1;
	protected int nextStartPos = -1;
	protected boolean skip = false;
	
	protected Token(TokenPattern tp,int startPos, int endPos){
		tokenPattern = tp;
		image = tp.matcher.group();
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	protected Token(String s,int startPos, int endPos){
		tokenPattern = null;
		image = s;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	
	public int getStartPos(){
		return startPos;
	}
	
	public int getEndPos(){
		return endPos;
	}
	
	public Token getNext(){
		return next;
	}
	
	public int getNextStartPos(){
		return nextStartPos==-1?endPos:nextStartPos;
	}
	
	public CharSequence getImage(){
		return image;
	}
	
	@Override
	public String toString(){
		return image;
	}
}
