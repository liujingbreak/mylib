package org.liujing.util.logging;

import org.liujing.util.LogWindow;
import java.util.logging.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
/**
SEVERE（最高值） 
WARNING 
INFO 
CONFIG 
FINE 
FINER 
FINEST（最低值） 

*/
public class MyLogHandler extends Handler
{
	protected static MyLogHandler handler=null;
	//protected SimpleAttributeSet attrs[];
	protected SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm:ss.SSS");
	protected Color[] backgroundCol=new Color[]{new Color(0xffffff),new Color(0xccffff),
		new Color(0xdbdedf),
		new Color(0x99ff99),
		new Color(0xffff66),
		new Color(0xff9933),
		new Color(0xff4300)};
	public MyLogHandler()
	{
		configure();
	}
	protected void configure() {
        	//LogManager manager = LogManager.getLogManager();
        	//String cname = getClass().getName();
		//setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
		////setFilter(manager.getFilterProperty(cname + ".filter", null));
		////setFormatter(manager.getFormatterProperty(cname + ".formatter",	new XMLFormatter()));
		
	}
	
	public static MyLogHandler getGlobalHandler()
	{
		return handler;
	}
	protected SimpleAttributeSet[] _setColor(LogRecord record)
	{
		SimpleAttributeSet[] attrs=new SimpleAttributeSet[6];
		for(int i=0;i<attrs.length;i++){
			attrs[i]=new SimpleAttributeSet();
		}
		StyleConstants.setBold(attrs[0],true);
		//StyleConstants.setForeground(attrs[0],new Color(0xcccccc));
		StyleConstants.setForeground(attrs[0],new Color(0x666666));
		StyleConstants.setForeground(attrs[1],new Color(0x003399));
		StyleConstants.setUnderline(attrs[2],true);
		StyleConstants.setBold(attrs[3],true);
		StyleConstants.setForeground(attrs[4],new Color(0x006600));
		
		int colIdx=0;
		if(record.getLevel()==Level.FINEST)
			colIdx=0;
		else if(record.getLevel()==Level.FINER)
			colIdx=1;
		else if(record.getLevel()==Level.FINE)
			colIdx=2;
		else if(record.getLevel()==Level.CONFIG)
			colIdx=3;
		else if(record.getLevel()==Level.INFO)
			colIdx=4;
		else if(record.getLevel()==Level.WARNING)
			colIdx=5;
		else if(record.getLevel()==Level.SEVERE)
			colIdx=6;
		for(int i=0;i<1;i++){
			StyleConstants.setBackground(attrs[i],backgroundCol[colIdx]);
		}
		return attrs;
	}
	@Override
	public void publish(LogRecord record)
	{
		if (!isLoggable(record)) {
			return;
		}
		Date time=new Date(record.getMillis());
		LogWindow logw=LogWindow.getInstance();
		//synchronized(attrs){
			SimpleAttributeSet[] attrs=_setColor(record);
			logw.append(record.getLevel().toString()+"\t",attrs[0]);
			logw.append(timeFormat.format(time),attrs[1]);
			logw.append(" T-",attrs[2]);
			logw.append(String.valueOf(record.getThreadID()),attrs[2]);
			logw.append(" ",attrs[2]);
			logw.append(record.getSourceClassName()+".",attrs[3]);
			logw.append(record.getSourceMethodName()+"() ",attrs[4]);
		
			Throwable thr=record.getThrown();
			logw.appendLine(record.getMessage(),attrs[5]);
			if(thr!=null){
				StringWriter sw=new StringWriter();
				thr.printStackTrace(new PrintWriter(sw));
				logw.appendLine(sw.toString());
			}
		//}
	}
	/**
		set logger level and add handle
	*/
	public static void setonLog(String logname,Level l)
	{
		if(handler==null){
			handler=new MyLogHandler();
			//Logger.getLogger(logname).removeHandler(handler);
			//Logger.getLogger(logname).
		}
		Logger logger=Logger.getLogger(logname);
		Handler[] hs=logger.getHandlers();
		//System.out.println("number of handlers"+hs.length);
		for(int i=0;i<hs.length;i++){
			//System.out.println("logger handler: "+hs[i].getClass().getName());
			if(hs[i].getClass().getName().equals(MyLogHandler.class.getName())){
				System.out.println("remove old logger handler: "+hs[i].getClass().getName());
				logger.removeHandler(hs[i]);
			}
		}
		logger.addHandler(handler);
		logger.setLevel(l);
	}
	/**
		add handle to specific level logger only
	*/
	public static void handleLog(String logname,Level l)
	{
		if(handler==null){
			handler=new MyLogHandler();
			//Logger.getLogger(logname).removeHandler(handler);
			//Logger.getLogger(logname).
		}
		Logger logger=Logger.getLogger(logname);
		Handler[] hs=logger.getHandlers();
		//System.out.println("number of handlers"+hs.length);
		for(int i=0;i<hs.length;i++){
			if(hs[i].getClass().getName().equals(MyLogHandler.class.getName())){
				System.out.println("remove old logger handler: "+hs[i].getClass().getName());
				logger.removeHandler(hs[i]);
			}
		}
		logger.addHandler(handler);
		handler.setLevel(l);
	}
	
	public  void close()
	{
		
	}
	
	public void flush()
	{
	}
}
