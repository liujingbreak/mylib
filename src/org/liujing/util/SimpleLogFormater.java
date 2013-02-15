package org.liujing.util;

import java.util.logging.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

public class SimpleLogFormater extends java.util.logging.Formatter{
	
	protected SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss.SSS");
	
	public final static String LINE_SEP = System.getProperty("line.separator");
	
	public String format(LogRecord record) 
	{
	    StringBuilder sbuf = new StringBuilder();
	    //sbuf.append(record.getLevel().getName());
	    //sbuf.append('\t');
	    Date time=new Date(record.getMillis());
	    sbuf.append(timeFormat.format(time));
	    //sbuf.append(" [T-");
	    //sbuf.append(record.getThreadID());
	    //sbuf.append("] ");
	    //
	    //sbuf.append(record.getSourceClassName());
	    //sbuf.append('.');
	    sbuf.append(' ');
	    sbuf.append(record.getSourceMethodName());
	    sbuf.append("() ");
	    
	    sbuf.append(record.getMessage());
	    Throwable thr=record.getThrown();
	    if(thr!=null){
		    StringWriter sw=new StringWriter();
		    thr.printStackTrace(new PrintWriter(sw));
		    sbuf.append(sw.toString());
	    }
	    sbuf.append(LINE_SEP);
	    return sbuf.toString();
	}
    }
