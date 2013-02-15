package org.liujing.tools.compiler;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class ExpansionChoices extends LLLAProduction{
	protected List<LLLAProduction> units = new ArrayList<LLLAProduction>();
	private static Logger log = Logger.getLogger(ExpansionChoices.class.getName());
	private TreeMap<Integer,LLLAProduction> choiceCache;
	
	public ExpansionChoices(){
		choiceCache = new TreeMap(new Comparator<Integer>(){
				public int compare(Integer o1, Integer o2){
					return o1.intValue()-o2.intValue();
				}
				public boolean equals(Object o){
					return this == o;
				}				
		});
	}
	
	public ExpansionChoices addChoice(LLLAProduction expansion){
		units.add(expansion);
		return this;
	}
	
	public ExpansionChoices addChoice(String str){
		units.add(new Keyword(str));
		return this;
	}
	
	public List<LLLAProduction> getChoices(){
		return units;
	}

	public Object consume(LLLACompiler parser, int[] limit)throws LLLAParserException{
		if( (!parser.evaluating) && !parser.skipping){
			parser.createLocalVariableStack();
			before(parser);
		}
		int i = 0;
		int startPos = parser.token.endPos;
		Integer iStartPos = Integer.valueOf(startPos);
		LLLAProduction choice = choiceCache.get(iStartPos);
		if(choice!=null){
			parser.cacheCount++;
			choice.consume(parser,limit);
			if(!parser.evaluating){
				SortedMap uselessCache = choiceCache.headMap(iStartPos);
				uselessCache.clear();
				choiceCache.remove(iStartPos);
			}
		}else{
			LLLAParserException[] failReasons = new LLLAParserException[units.size()];
			int[] lookedNum = new int[1];
			for(int l=units.size(),last=l-1;i<l;i++){
				LLLAProduction unit = units.get(i);
				if(unit.lookAhead(parser,lookedNum)){
					choiceCache.put(iStartPos,unit);
					if(parser.evaluating){
						if(limit[0]> lookedNum[0]){
							//if((!parser.isSlient()) && log.isLoggable(Level.FINE))
							//	log.fine("Choices<"+hashCode()+"> lookahead not enough limit[0] = "+limit[0]+" lookedNum[0]="+lookedNum[0]);
							unit.consume(parser,limit);
						}else{
							//if((!parser.isSlient()) && log.isLoggable(Level.FINE))
							//	log.fine("Choices<"+hashCode()+"> lookahead enough lookedNum = "+lookedNum[0]);
							limit[0]-=lookedNum[0];
							break;
						}
					}else{
						unit.consume(parser,limit);
					}
				}else{
					failReasons[i] = unit.getLAFailReason();
					if(i== last)
						throw new LLLAParserException(failReasons,startPos);
					else
						continue;
					
				}
				break;
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
			w.write("\n\r");
			it.next().writeTo(w);
			if(it.hasNext())
				w.write("|");
		}
		w.write(") ");
	}
}
