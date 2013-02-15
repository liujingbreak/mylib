package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class Add extends LLLAProduction{
	protected LLLAProduction production;
	private String assignVb;
	private static Logger log = Logger.getLogger(Add.class.getName());
	public Add(String vbName,LLLAProduction production){
		this.production = production;
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
		if(!parser.evaluating){
			List vb = (List)parser.getVariable(assignVb);
			if(vb == null){
				vb = new ArrayList();
				parser.setVariable(assignVb,vb);
			}
			vb.add(result);
		}
		return result;
	}
	public void writeTo(Writer w)throws IOException{
		w.write(assignVb);
		w.write(" ADD ");
		production.writeTo(w);
	}
}
