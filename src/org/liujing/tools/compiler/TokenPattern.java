package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class TokenPattern extends LLLAProduction{
	private static Logger log = Logger.getLogger(TokenPattern.class.getName());
	public Pattern pattern;
	public Matcher matcher;
	public String name;
	
	
	protected TokenPattern(Pattern p){
		pattern = p;
		setName(p.toString());
	}
	
	@Override
	public int lookAheadNum(LLLACompiler parser){
		return 1;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	protected void buildMatcher(CharSequence cs){
		matcher = pattern.matcher(cs);
	}
	
	public Object consume(LLLACompiler compiler,int[] limit)throws LLLAParserException{
		compiler.skip();
		if(!compiler.evaluating)
			before(compiler);
		else
			--limit[0];
		//if(!compiler.skipping && log.isLoggable(Level.FINE))
		//	log.fine((compiler.evaluating?"evaluate":"consume")+" token pattern: "+(name==null?pattern:name));
		compiler.nextToken(this);
		if(!compiler.evaluating)
			return after(compiler, compiler.token);
		else{
			
			return null;
		}
	}
	
	public Object after(LLLACompiler parser,Object results){
		return parser.token;
	}
	
	public void writeTo(Writer w)throws IOException{
		w.write("<"+pattern+"> ");
	}
}
