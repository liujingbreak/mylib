package org.liujing.tools.compiler;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.text.*;
import java.util.regex.*;

public class Keyword extends LLLAProduction{
	private static Logger log = Logger.getLogger(Keyword.class.getName());
	protected String word;
	protected Keyword(String p){
		word = p;
		setName(word);
	}
	
	@Override
	public int lookAheadNum(LLLACompiler parser){
		return 1;
	}
	
	public Object consume(LLLACompiler parser,int[] limit)throws LLLAParserException{
		//try{
		parser.skip();
		if(!parser.evaluating)
			before(parser);
		else
			--limit[0];
		//if(!parser.skipping && log.isLoggable(Level.FINE))
		//	log.fine((parser.evaluating?"evaluate":"consume")+" keyword: \""+word+"\" begin");
		parser.nextToken(word);
		//if(!parser.skipping && log.isLoggable(Level.FINE))
		//	log.fine((parser.evaluating?"evaluate":"consume")+" keyword: \""+word+"\" end");
		if(parser.evaluating){
			return null;
		}else{
			return after(parser, parser.token);
		}
	}
	
	public Object after(LLLACompiler parser,Object result){
		//if("+".equals(parser.token.image))
		//	log.info("match +");
		return parser.token;
	}
	
	public void writeTo(Writer w)throws IOException{
		w.write("\""+word+"\" ");
	}
}
