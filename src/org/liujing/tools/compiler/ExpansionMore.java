package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class ExpansionMore extends LLLAProduction{
	private static Logger log = Logger.getLogger(ExpansionMore.class.getName());
	/**
	( expansion_unit )?  or [ expansion_unit ]
	*/
	public final static short TYPE_DEFAULT = 1; 
	/**
	( expansion_unit )+
	*/
	public final static short TYPE_PLUS = 2; 
	/**
	( expansion_unit )*
	*/
	public final static short TYPE_STAR = 3;
	
	protected int minRepeat = 0;
	protected int maxRepeat = 1;
	protected short type;
	protected LLLAProduction unit;
	
	private TreeMap<Integer,Boolean> choiceCache;
	
	private ExpansionMore(){
		choiceCache = new TreeMap(new Comparator<Integer>(){
				public int compare(Integer o1, Integer o2){
					return o1.intValue()-o2.intValue();
				}
				public boolean equals(Object o){
					return this == o;
				}				
		});
	}
	
	public ExpansionMore(LLLAProduction unit){
		this();
		this.unit = unit;
	}
	
	public ExpansionMore(String unit){
		this();
		this.unit = new Keyword(unit);
	}
	
	public ExpansionMore(short type,String unit){
		this();
		this.unit = new Keyword(unit);
		this.type = type;
		switch(type){
		case TYPE_DEFAULT:
			break;
		case TYPE_PLUS:
			minRepeat = 1;
			maxRepeat = Integer.MAX_VALUE;
			break;
		case TYPE_STAR:
			maxRepeat = Integer.MAX_VALUE;
			break;
		}
	}
	
	public ExpansionMore(short type,LLLAProduction unit){
		this();
		this.type = type;
		this.unit = unit;
		switch(type){
		case TYPE_DEFAULT:
			break;
		case TYPE_PLUS:
			minRepeat = 1;
			maxRepeat = Integer.MAX_VALUE;
			break;
		case TYPE_STAR:
			maxRepeat = Integer.MAX_VALUE;
			break;
		}
	}
	
	public ExpansionMore(int min, int max,LLLAProduction unit){
		this();
		this.unit = unit;
		minRepeat = min;
		maxRepeat = max;
	}
	public Object consume(LLLACompiler parser,int[] limit)throws LLLAParserException{

		List results = null;
		if((!parser.evaluating) && !parser.skipping){
			parser.createLocalVariableStack();
			before(parser);
		}
		int loopCnt = 0;
		int[] lookedNum = new int[1];
		while(loopCnt < maxRepeat){
			int startPos = parser.token.getNextStartPos();
			Integer iStartPos = Integer.valueOf(startPos);
			Boolean laSuccess = choiceCache.get(iStartPos);
			boolean isLaSuccess = false;
			if(laSuccess!=null){
				parser.cacheCount++;
				//if(!parser.isSlient() && log.isLoggable(Level.FINE))
				//	log.fine("find loop cache @"+startPos+" ="+laSuccess.booleanValue());
				if(!parser.evaluating){
					SortedMap uselessCache = choiceCache.headMap(iStartPos);
					uselessCache.clear();
					choiceCache.remove(iStartPos);
				}
				isLaSuccess = laSuccess.booleanValue();
			}
			if((laSuccess!=null && isLaSuccess) || unit.lookAhead(parser,lookedNum)){
				loopCnt++;
				if(parser.evaluating){
					if(laSuccess==null){
						//if((!parser.isSlient()) && log.isLoggable(Level.FINE))
						//	log.fine(iStartPos+" save loop Count="+loopCnt+" minRepeat="+minRepeat);
						choiceCache.put(iStartPos,Boolean.TRUE);
					}
					if((!parser.isSlient()) && log.isLoggable(Level.FINE))
						log.fine(startPos+" Ex.more"+hashCode()+" lookedNum = "+lookedNum[0]+" limit[0]="+limit[0]);
					if(limit[0]>lookedNum[0])
						unit.consume(parser,limit);
					else{
						limit[0]-=lookedNum[0];
						break;
					}
					if(limit[0]<=0)
						break;
				}else{
					unit.consume(parser,limit);
				}
			}else{
				if(loopCnt < minRepeat){
					throw unit.getLAFailReason();
				}
				else{
					if(!parser.skipping)
						parser.lastExpansionMoreFail = unit.getLAFailReason();
					break;
				}
			}
			
		}
		
		
		if(parser.evaluating  || parser.skipping){
			return null;
		}else{
			Object ret = after(parser);
			parser.clearLocalVariableStack();
			return ret;
		}
	}
	
	public Object after(LLLACompiler parser)throws LLLAParserException{
		
		return null;
	}
	
	//public String toString(){
	//	return "More "+unit
	//}
	
	public void writeTo(Writer w)throws IOException{
		w.write(" (");
		unit.writeTo(w);
		w.write("){");
		w.write(minRepeat);
		w.write(",");
		w.write(maxRepeat);
		w.write("} ");
	}
}
