package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class Assign extends LLLAProduction{
	private LLLAProduction production;
	private String assignVb;
	private static Logger log = Logger.getLogger(Assign.class.getName());
	public Assign(String vbName,LLLAProduction production){
		this.production = production;
		assignVb = vbName;
	}
	
	public Assign(String vbName,String keyword){
		this.production = new Keyword(keyword);
		assignVb = vbName;
	}
	
	public LLLAProduction getProduction(){
		return production;
	}
	
	public boolean lookAhead(LLLACompiler parser,int[] feedNum){
		return production.lookAhead(parser,feedNum);
	}
	
	public LLLAParserException getLAFailReason(){
		return production.laFailReason;
	}
	
	public LLLAProduction setLookAhead(int lookAhead){
		production.setLookAhead(lookAhead);
		return this;
	}
	
	public LLLAProduction setLookAhead(Expansion lookAhead){
		production.setLookAhead(lookAhead);
		return this;
	}
	
	public Object consume(LLLACompiler parser, int[] limit)throws LLLAParserException{
		Object result = production.consume(parser,limit);
		if(!parser.evaluating)
			parser.setVariable(assignVb,result);
		return result;
	}
	
	public void writeTo(Writer w)throws IOException{
		w.write(assignVb);
		w.write(" = ");
		production.writeTo(w);
	}
}
