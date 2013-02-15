package org.liujing.tools.compiler;

import java.util.*;

public class EvaluationStatus{
	public int lookAheadNum = 0;
	private LinkedList<LLLAProduction> productions = null;
	
	public EvaluationStatus(int lookAheadNum){
		this.lookAheadNum = lookAheadNum;
	}
	
	public EvaluationStatus(LLLAProduction prod){
		productions = new LinkedList();
		if(prod instanceof Expansion){
			Expansion expan = (Expansion)prod;
			List<LLLAProduction> units = expan.getUnits();
			productions.addAll(units);
		}else{
			productions.add(prod);
		}
		lookAheadNum = Integer.MAX_VALUE;
	}
	
	public boolean isEvaluating(){
		return lookAheadNum>0;
	}
	
	public void tokenRead(){
		if(lookAheadNum>0){
			lookAheadNum--;
		}
	}
	
	
}
