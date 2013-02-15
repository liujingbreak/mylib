package org.liujing.util;

import java.util.LinkedList;
import org.liujing.util.WorkingThread;

/**
 * use this class to do log will not cause current thread block when logging,
 * it will use a Thread always running to log message
 * usage:<br>
 *	LogThread lg=new LogThread(MyClass.class);
 *	lg.debug("....");
 * @author liujing
 */
public class LogThread
{
	protected static LinkedList messageToLog=new LinkedList(); 
	protected static ServiceThread server;
	static{
		server=new ServiceThread();
		server.start();
	}

	protected static LogThread instance=null;

	protected String _className;
	public synchronized static LogThread getInstance()
	{
		if(instance==null){
			instance=new LogThread();
		}
		return instance;
	}
	protected LogThread()
	{
		_className="[ROOT]";
	}
	public LogThread(Class clz)
	{
		_className="["+clz.getName()+"]";
	}
	public void debug(String text)
	{
		StringBuilder buf=new StringBuilder();
		buf.append('[');
		buf.append(Thread.currentThread().getName());
		buf.append(']');
		buf.append(_className);
		buf.append(text);
		text=buf.toString();
		synchronized(messageToLog){
			messageToLog.add(text);
		}
		server.setRunning(true);
	}
	public void debug(String text,Throwable thr)
	{
		StringBuilder buf=new StringBuilder();
		buf.append('[');
		buf.append(Thread.currentThread().getName());
		buf.append(']');
		buf.append(_className);
		buf.append(text);
		text=buf.toString();
		synchronized(messageToLog){
			messageToLog.add(new Object[]{text,thr});
		}
		server.setRunning(true);
	}

	static class ServiceThread extends WorkingThread
	{
		protected void doJob()
		{
			while(messageToLog.size()>0)
			{
				Object msg=null;
				synchronized(messageToLog){
					msg=messageToLog.removeFirst();
				}
				if(msg instanceof String){
					LogWindow.getInstance().debug((String)msg);
				}
				else if(msg instanceof Object[]){
					Object []msgs=(Object[])msg;
					LogWindow.getInstance().debug((String)msgs[0], (Throwable)msgs[1]);
				}
			}
		}
	}
}
