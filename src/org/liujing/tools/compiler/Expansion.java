package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class Expansion extends LLLAProduction{
	private static Logger log = Logger.getLogger(Expansion.class.getName());
	protected List<LLLAProduction> units = new ArrayList<LLLAProduction>();
	protected Object[] results;

	
	protected LLLAParserException laFailReason;
	
	public Expansion(){
	}
	
	public Expansion addUnit(LLLAProduction expansion){
		units.add(expansion);
		return this;
	}
	
	public Expansion addUnit(String s){
		units.add(new Keyword(s));
		return this;
	}
	
	protected List<LLLAProduction> getUnits(){
		return units;
	}
	
	@Override
	public Object consume(LLLACompiler parser,int[] limit)throws LLLAParserException{
		Object[] results = null;
		if( (!parser.evaluating)&& !parser.skipping){
			parser.createLocalVariableStack();
			before(parser);
		}
		for(int i=0,l=units.size();i<l;i++){
			LLLAProduction unit = units.get(i);
			if(parser.evaluating){				
				unit.consume(parser,limit);
				//if(log.isLoggable(Level.FINE) && parser.evaluating && !parser.skipping)
				//		log.fine("limit = "+limit);
				if(limit[0]<=0)
					break;
			}else{
				unit.consume(parser,limit);
			}
		}
		if( (!parser.evaluating) && !parser.skipping){			
			Object ret = after(parser);
			parser.clearLocalVariableStack();
			return ret;
		}else{
			return null;
		}
	}
	
	public Object after(LLLACompiler parser)throws LLLAParserException{		
		return null;
	}
	
	public void writeTo(Writer w)throws IOException{
		Iterator<LLLAProduction> it = units.iterator();
		w.write(" (");
		while(it.hasNext()){
			it.next().writeTo(w);
			w.write(" ");
		}
		w.write(" )");
	}
}
