package org.liujing.util;


/**
You can use this Thread either in a big thread pool or use it alone.<br>
you have to implement a method of this class with your own code.
to call thread start work, use setRunning(true)
@author liujing 2007
*/
public abstract class WorkingThread extends Thread
{
	private boolean haveJobTodo=true;
	private boolean over=false;
	public WorkingThread(){}
	/**
	 pls overwite this method,<br>
	 fill with your own code what is the job you want the thread to do every time.<br>
	 maybe you can release it to the thread pool in this method after everyting done, if you have used a thread pool.
	*/
	protected abstract void doJob();

	/**
	 call this to notify the thread to work. after done thread will be waiting automaticlly.
	*/
	public synchronized void setRunning(boolean running)
	{
		this.haveJobTodo=running;
		if(haveJobTodo){
			notify();
		}
	}
	/**
	call thread wake up and destory self
	*/
	public void terminate()
	{
		over=true;
		setRunning(true);
	}
	
	public boolean doseHaveJobTodo()
	{
		return haveJobTodo;
	}
	/**
	 the thread use this method to check whether have job todo
	 notice this method with setHaveJobTodo()
	*/
	private synchronized boolean _checkJob()
	{
		boolean ret=haveJobTodo;
		haveJobTodo=false;
		return ret;
	}
	public void run()
	{
		while(!over){
			if(_checkJob()){
				//System.out.println(this.getClass().getName()+" has job");
				// TODO:
				doJob();
			}else{
				synchronized(this){
					try{this.wait();}catch(Exception e){e.printStackTrace();}
				}
			}
			//System.out.println(this.getClass().getName()+" to check over="+over);
		}
		//System.out.println(this.getClass().getName()+" is over");
	}

}
