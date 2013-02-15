package org.liujing.util;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory; 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/**
@author liujing
*/
public class MySaxXMLTokenizer
{
	protected Reader reader;
	public static int TT_CHARACTERS=1;
	public static int TT_END_DOC=2;
	public static int TT_END_ELEMENT=3;
	public static int TT_ERROR=4;
	public static int TT_FATAL_ERROR=5;
	public static int TT_IGNO_WS=6;//ignorableWhitespace
	public static int TT_START_DOC=7;
	public static int TT_START_ELEMENT=8;
	public int ttype=0;	
	public Token token=new Token();
	private static ThreadLocal pthreads = new ThreadLocal();

	protected DefaultHandler _handler =null;
	protected SAXParser saxParser=null;
	private int loglevel=1;
	private static LogThread log=new LogThread(MySaxXMLTokenizer.class);
	private static LogThread log1=new LogThread(MySaxXMLTokenizer.SaxHandle.class);
	protected ParseThread pthread;

	//protected Go test=null;
	public MySaxXMLTokenizer(Reader r)
		throws ParserConfigurationException,SAXException,IOException
	{
		reader=r;
		_handler = new SaxHandle(); 
		 // Use the default (non-validating) parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		saxParser = factory.newSAXParser();

		pthread=(ParseThread)pthreads.get();
		if(pthread==null){
			pthread=new ParseThread(this);
			pthread.setRunning(false);
			pthreads.set(pthread);
			pthread.start();
		}else{
			pthread.setOwner(this);
		}

		//test=new Go();
	}
	
	public int nextToken()throws SAXParseException,SAXException,IOException
	{
		
		synchronized(_handler){
			if(ttype==0){
				pthread.setRunning(true);
			}else{
				_handler.notify();
			}
			//log.debug("before wait");
			try{_handler.wait();}catch(InterruptedException ie){ie.printStackTrace();}
			//log.debug("after wait");
		}
		
		if(ttype==TT_ERROR||ttype==TT_FATAL_ERROR){
			throw token.e;
		}
		/*else if(ttype==TT_END_DOC){
			pthread.terminate();
			
		}*/
		return ttype;
	}
	protected  void finalize() 
	{
		System.out.println(this.getClass().getName()+" finalize()");
		//pthread.terminate();
	}
	public static void main(String[] arg)
	{
		try{
			
			MySaxXMLTokenizer tk=new MySaxXMLTokenizer(new FileReader("test.xml"));
			tk.nextToken();
			while(tk.ttype!=tk.TT_END_DOC)
			{
				log.debug("ttype="+tk.ttype);
				tk.nextToken();
			}
			tk=null;	
			
			System.gc();
			log.debug("end");
			//tk.pthread.terminate();
			//log.debug("terminated");
			//System.exit(0);
			
		}
		catch(Exception e){
			e.printStackTrace();
			log.debug("",e);
		}
	}
	protected void _parse()
	{
		try{
		saxParser.parse( new InputSource(reader), _handler ); 
		if(loglevel>=1) log.debug("parse over");
		}catch(Exception se)
		{
			log.debug("_parse() error",se);
		}
	}
	public static class Token
	{
		public char[] ch;
		public int start;
		public int length;
		public String uri;
		public String localName;
		public String qName;
		public SAXParseException e;
		public Attributes attributes;
	}

	public static class ParseThread extends WorkingThread
	{
		protected MySaxXMLTokenizer tokenizer=null;
		public ParseThread(MySaxXMLTokenizer tk)
		{
			tokenizer=tk;
		}
		public void setOwner(MySaxXMLTokenizer tk)
		{
			tokenizer=tk;
		}
		protected void doJob()
		{
			tokenizer._parse();
			tokenizer=null;
		}
	}
	class SaxHandle extends DefaultHandler
	{
		
		
		public void skipError()
		{
			if(ttype==TT_ERROR||ttype==TT_FATAL_ERROR){
				synchronized(this){
					notify();
				}
			}
		}
		//------over write method
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			token.ch=ch;
			token.start=start;
			token.length=length;
			ttype=TT_CHARACTERS;
			if(loglevel==1) log1.debug("characters() ch="+new String(ch,start,length));
			_onEven();
		}
		public void endDocument()throws SAXException
		{
			ttype=TT_END_DOC;
			if(loglevel==1) log1.debug("endDocument()");
			synchronized(this){
				notify();
			}
		}
		public void endElement(String uri, String localName, String qName)throws SAXException
		{
			token.uri=uri;
			token.localName=localName;
			token.qName=qName;
			ttype=TT_END_ELEMENT;
			if(loglevel==1) log1.debug("endElement() uri="+uri+", localName="+localName+", qName="+qName);
			_onEven();
		}
		public void error(SAXParseException e) throws SAXException
		{
			token.e=e;
			ttype=TT_ERROR;
			if(loglevel==1) log1.debug("error()");
			_onEven();
		}
		public void fatalError(SAXParseException e) throws SAXException
		{
			token.e=e;
			ttype=TT_FATAL_ERROR;
			if(loglevel==1) log1.debug("fatalError()");
			_onEven();
		}
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
		{
			token.ch=ch;
			token.start=start;
			token.length=length;
			ttype=TT_IGNO_WS;
			if(loglevel==1) log1.debug("ignorableWhitespace()");
			_onEven();
		}
		public void startDocument() throws SAXException
		{
			ttype=TT_START_DOC;
			if(loglevel==1) log1.debug("startDocument()");
			_onEven();
			
		}
		public void startElement(String uri, String localName, String qName, Attributes attributes)throws SAXException
		{
			token.uri=uri;
			token.localName=localName;
			token.qName=qName;
			token.attributes=attributes;
			ttype=TT_START_ELEMENT;
			if(loglevel==1) log1.debug("startElement() uri="+uri+", localName="+localName+", qName="+qName);
			_onEven();
		}
		protected void _onEven()
		{
			synchronized(this){
				notify();
				//log1.debug("even end, wait");
				try{wait();}catch(InterruptedException ie){ie.printStackTrace();}
				//log1.debug("start to catch even");
			}
		}
	}

	public class Go 
	{
		String test="test";
		
		public Go()
		{
			
		}
		protected  void finalize() 
		{
			System.out.println("go finalize()");
		}
	}
}
