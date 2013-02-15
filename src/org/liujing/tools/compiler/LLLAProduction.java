package org.liujing.tools.compiler;

import java.util.logging.*;
import java.io.*;

public abstract class LLLAProduction<T>{
	private static Logger log = Logger.getLogger(LLLAProduction.class.getName());
	protected int lookAhead = 0;
	protected boolean isNegativeLookahead = false;
	protected LLLAProduction lookAheadProd;
	protected LLLAParserException laFailReason;
	//protected String assignVb;
	private String name;
	
	public LLLAProduction(){
	}
	
	public void setName(String s){
		name = s;
	}
	
	public String getName(){
		return name;
	}
	//public void assignToVariable(String variableName){
	//	assignVb = variableName;
	//}
	
	/** positive lookahead*/
	public LLLAProduction<T> setLookAhead(int lookAhead){
		this.lookAhead = lookAhead;
		isNegativeLookahead = false;
		return this;
	}
	/** positive lookahead*/
	public LLLAProduction<T> setLookAhead(LLLAProduction lookAhead){
		//log.info("<"+this.hashCode()+"> name="+this.getName()+" set look ahead large value");
		lookAheadProd = lookAhead;
		isNegativeLookahead = false;
		return this;
	}
	/** negative lookahead*/
	public LLLAProduction<T> setNegativeLookAhead(LLLAProduction lookAhead){
		lookAheadProd = lookAhead;
		isNegativeLookahead = true;
		return this;
	}
	
	public int lookAheadNum(LLLACompiler parser){
		if(lookAheadProd!=null){
			lookAhead = 2000000000;
		}
		if(parser==null){
			return lookAhead>0?lookAhead:1;
		}else{
			int lookAheadNum = lookAhead>0?lookAhead:parser.globalLookAhead;
			return lookAheadNum;
		}
	}
	
	public boolean lookAhead(LLLACompiler parser,int[] feedNum){
		int lookAheadNum = lookAheadNum(parser);
		//if(lookAheadNum==2000000000){
		//	if(log.isLoggable(Level.INFO) && !parser.isSlient())
		//		log.info("<"+this.hashCode()+"> name="+this.getName()+"'s lookaheadnum is very big LAP=<"+lookAheadProd.hashCode()+">"+lookAheadProd.getName());
		//}
		//lookAheadProd = lookAheadProd==null?this:lookAheadProd;
		boolean origStatus = parser.evaluating;
		parser.evaluating = true;
		parser.evaLevel++;
		Token savePoint = parser.token;
		if(log.isLoggable(Level.FINE) && !parser.isSlient() && name!=null)
			log.fine(parser.token.endPos+"["+name+"] lookAhead start: "+parser.token.getImage());
		try{
			int[] newLookAheadlimit = new int[]{lookAheadNum};
			if(lookAheadProd==null)
				consume(parser,newLookAheadlimit);
			else
				lookAheadProd.consume(parser,newLookAheadlimit);
			int actualNum = lookAheadNum-newLookAheadlimit[0];
			if(actualNum==0){
				log.info("lookAheadNum:"+lookAheadNum+" newLookAheadlimit:"+newLookAheadlimit[0]+" lookAheadProd="+
					lookAheadProd.getName());
			}
			feedNum[0] = actualNum;
			if(isNegativeLookahead){
				parser.token.next = null; //clear cached tokens, because the fetched token may be not the one that parser will expect.
				return false;
			}
			return true;
		}catch(LLLAParserException pe){
			laFailReason = pe;
			return isNegativeLookahead?true:false;
		}finally{
			if(log.isLoggable(Level.FINE) && !parser.isSlient()&& name!=null)
			log.fine(parser.token.endPos+"["+name+"] lookAhead end");
			parser.token = savePoint;
			parser.evaluating = origStatus;
			parser.evaLevel--;
		}
	}
	
	public LLLAParserException getLAFailReason(){
		return laFailReason;
	}
	
	public void writeTo(Writer w)throws IOException{}
	
	public abstract T consume(LLLACompiler compiler, int[] limit)throws LLLAParserException;
	public void before(LLLACompiler parser)throws LLLAParserException
	{}
	
}
