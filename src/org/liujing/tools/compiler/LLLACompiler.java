package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class LLLACompiler{
	private static Logger log = Logger.getLogger(LLLACompiler.class.getName());
	protected CharSequence input;
	protected int position = 0;
	protected ExpansionMore skipProd;
	protected boolean skipping = false;
	protected boolean evaluating = false;
	protected int evaLevel = 0;
	protected List<TokenPattern> tokens = new ArrayList<TokenPattern>();
	public static final EOFPattern EOF = new EOFPattern();
	public Token token;
	protected Token nextToken;
	protected Token currLAToken;
	protected int lookAheadIdx = 0;
	//protected LookAheadHelper lookAheadHelper;
	protected int globalLookAhead = 1;
	private List<Map> localVariableStacks = new ArrayList();
	protected LLLAParserException lastExpansionMoreFail;
	protected boolean slient = true;
	
	protected int cacheCount = 0;//only for statistics
	
	public LLLACompiler(){
	}
	
	public LLLACompiler(CharSequence cs){
		this();
		reInit(cs);
	}
	
	public boolean isSlient(){
		return slient||skipping;
	}
	
	public void setSlient(boolean s){
		slient = s;
	}
	
	public void reInit(CharSequence cs){
		this.input = cs;
		token = new Token((String)null,0,0);
		
		for(int i=0,l=tokens.size();i<l;i++){
			tokens.get(i).buildMatcher(cs);
		}
	}
	
	protected void createLocalVariableStack(){
		localVariableStacks.add(null);
		//log.info("<create LocalVariableStack"+ new Throwable().getStackTrace()[1]);
	}
	protected void clearLocalVariableStack(){
		localVariableStacks.remove(localVariableStacks.size()-1);
		//log.info("clear LocalVariableStack>" + new Throwable().getStackTrace()[1]);
	}
	
	public void declareVariable(String name){
		declareVariable(name,null);
		
	}
	
	public void declareVariable(String name,Object value){
		int last = localVariableStacks.size()-1;
		Map stack = localVariableStacks.get(last);
		if(stack == null){
			stack = new HashMap();
			localVariableStacks.set(last,stack);
		}
		stack.put(name,value);
		//log.info("declareVariable "+name+ "@"+localVariableStacks.size());
	}
	
	public Object getVariable(String name)throws LLLAParserException{
		for(int i=localVariableStacks.size()-1;i>=0;i--){
			Map map = localVariableStacks.get(i);
			if(map!=null && map.containsKey(name)){
				return map.get(name);
			}
		}
		//Iterator<Map> it = localVariableStacks.iterator();
		//while(it.hasNext()){
		//	Map map = it.next();
		//	if(map!=null && map.containsKey(name)){
		//		return map.get(name);
		//	}
		//}
		throw new LLLAParserException("Parser Variable '"+name+"' is not defined.");
	}
	
	public void setVariable(String name,Object value)throws LLLAParserException{
		for(int i=localVariableStacks.size()-1;i>=0;i--){
			Map map = localVariableStacks.get(i);
			if(map!=null && map.containsKey(name)){
				map.put(name,value);
				return;
			}
		}
		//Iterator<Map> it = localVariableStacks.iterator();
		//while(it.hasNext()){
		//	Map map = it.next();
		//	if(map!=null && map.containsKey(name)){
		//		map.put(name,value);
		//		return;
		//	}
		//}
		throw new LLLAParserException("Parser Variable '"+name+"' is not defined.");
	}
	/**
	set global lookAhead option
	*/
	public void setLookAhead(int num){
		globalLookAhead = num;
	}
	
	public int getPosition(){
		return position;
	}
	
	public void setPosition(int p){
		position = p;
	}
	
	public TokenPattern defineToken(Pattern p){
		TokenPattern tk = new TokenPattern(p);
		//log.info("input="+input);
		if(input!=null)
			tk.buildMatcher(input);
		tokens.add(tk);
		return tk;
	}
	
	public TokenPattern defineToken(String pattern,int flag){
		Pattern p = Pattern.compile(pattern,flag);
		return defineToken(p);
	}
	
	public TokenPattern defineToken(String pattern){
		Pattern p = Pattern.compile(pattern);
		return defineToken(p);
	}
	
	public Token nextToken(TokenPattern tp)throws LLLAParserException{
		token = lookNextOf(token,tp);
		return token;
	}
	
	public Token nextToken(String keyword)throws LLLAParserException{
		token = lookNextOf(token,keyword);
		return token;
	}
	
	//public boolean isNextToken(TokenPattern tp){
	//	
	//	token = lookNextOf(token,keyword);
	//	return token;
	//}
	//public boolean isNextToken(String keyword){
	//	token = lookNextOf(token,keyword);
	//	return token;
	//}
	
	private Token lookNextOf(Token token,TokenPattern tp)throws LLLAParserException{
		//if(!isSlient()){
		//	System.out.print(token.endPos);
		//	System.out.print(" ");
		//}
		Token nextToken = token.next;
		if((!isSlient()) && log.isLoggable(Level.FINEST))
				log.log(Level.FINEST,"@"+token.endPos+" lookNextOf :"+tp.pattern,new Throwable());
		if(nextToken!=null){
			if(nextToken.tokenPattern!=tp){
				//if(evaluating){
				//	position = token.endPos;
				//	nextToken = fetchToken(tp);
				//	token.next = nextToken;
				//}else{
					throw new LLLAParserException(input,nextToken.startPos,tp);
				//}
			}
		}else{
			position = token.getNextStartPos();
			nextToken = fetchToken(tp);
			nextToken.skip = skipping;
			token.next = nextToken;
		}
		return nextToken;
	}
	
	private Token lookNextOf(Token token,String str)throws LLLAParserException{
		//if(!isSlient()){
		//	System.out.print(token.endPos);
		//	System.out.print(" ");
		//}
		Token nextToken = token.next;
		if((!isSlient()) && log.isLoggable(Level.FINEST))
				log.finest("@"+token.endPos+" lookNextOf :"+str);
		if(nextToken!=null){
			if(nextToken.image!=str && (
				(nextToken.image==null || str==null) || (!nextToken.image.equals(str)) )){
				//if(evaluating){
				//	position = token.endPos;
				//	nextToken = fetchToken(str);
				//	token.next = nextToken;
				//}else{
					
					throw new LLLAParserException(input,nextToken.startPos,str);
				//}
			}
		}else{
			position = token.getNextStartPos();
			nextToken = fetchToken(str);
			nextToken.skip = skipping;
			token.next = nextToken;
		}
		return nextToken;
	}
	
	
	/**
	try to consume the token from "position" by specific token pattern.
	*/
	private Token fetchToken(TokenPattern tp)throws LLLAParserException{
		//skip();
		int start = position;
		tp.matcher.region(position,input.length());
		if((!skipping) && log.isLoggable(Level.FINE))
				log.fine("@"+position+" fetch token :"+tp.pattern);
		if(tp.matcher.lookingAt()){
			position = tp.matcher.end();			
		}else{
			throw new LLLAParserException(input,position,tp);
		}
		return new Token(tp,start,position);
	}
	
	/**
	try to consume the string from "position".
	*/
	private Token fetchToken(String str)throws LLLAParserException{
		//skip();
		int start = position;
		if(str==null){
			return fetchEOF();
		}
		if( (!skipping) && log.isLoggable(Level.FINE))
			log.fine("@"+position+" to fetch token :"+str);
		int end = position+str.length();
		
		if(end<= input.length() && input.subSequence(position,end).equals(str)){
			position = end;
			//if( (!skipping) && log.isLoggable(Level.FINE))
			//	log.fine("@"+position+" fetch token :"+str);
		}else{
			throw new LLLAParserException(input,position,str);
		}
		return new Token(str,start,position);
	}
	
	
	private EOFToken fetchEOF()throws LLLAParserException{

		int start = position;
		if(position == input.length()){
			
		}else{
			throw new LLLAParserException(input,position,"<EOF>");
		}
		return new EOFToken(start,position);
	}
	
	public static class EOFToken extends Token{
		public EOFToken(int startPos, int endPos){
			super((String)null,startPos,endPos);
		}
	}
	
	public static class EOFPattern extends Keyword{
		EOFPattern(){
			super(null);
			setName("<EOF>");
		}
	}
	
	public void setSkip(LLLAProduction production){
		skipProd = new ExpansionMore(ExpansionMore.TYPE_STAR, production);
	}
	
	protected void skip()throws LLLAParserException{
		if(skipping || skipProd==null){
			return;
		}
		if(token.next!=null && !token.next.skip)
			return;
		skipping = true;
		Token previousTk = token;
		skipProd.consume(this,new int[]{Integer.MAX_VALUE});
		if(previousTk!=null && token!=previousTk){
			previousTk.nextStartPos = token.endPos;
			previousTk.next = token.next;
		}
		skipping = false;
		
	}
	
	public Object parse(LLLAProduction prd)throws LLLAParserException{
		cacheCount = 0;
		Object o = prd.consume(this,new int[]{0});
		log.fine(">>> choice cache used times total is "+cacheCount);
		return o;
	}
}