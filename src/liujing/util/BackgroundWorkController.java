package liujing.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import java.util.*;
import javax.swing.SwingUtilities;
/**
It provides a thread pool functionality for running those heavy tasks
*/
public class BackgroundWorkController{
	private static Logger log = Logger.getLogger(BackgroundWorkController.class.getName());

	protected BlockingQueue<Task> taskQ1 = new LinkedBlockingQueue();

	protected StatusListener statusListener;

	protected List<Thread> threads;
	/**
	number of current idle threads
	*/
	protected int idle = 0;

	protected int maxThreads = 1;

	protected int idleThreads = 1;

	private boolean invokedBySwing = false;

	public BackgroundWorkController(){
		this(false);
	}

	public BackgroundWorkController(boolean invokedBySwing){
		this(1, 1, invokedBySwing);
	}

	public BackgroundWorkController(int maxNumOfThread, int maxIdleNumOfThread){
		this(maxNumOfThread, maxIdleNumOfThread, false);
	}

	/**  construct BackgroundWorkController
     @param maxNumOfThread maxNumOfThread
     @param maxIdleNumOfThread maxIdleNumOfThread
     @param invokedBySwing set to true if you want the callback function to be
     invoked in SwingUtilities.invokeLater()
    */
	public BackgroundWorkController(int maxNumOfThread, int maxIdleNumOfThread, boolean invokedBySwing){
		maxThreads = maxNumOfThread;
		idleThreads = maxIdleNumOfThread;
		threads = new LinkedList();
		//threads = Collections.synchronizedList(new LinkedList());
		setInvokedBySwing(invokedBySwing);
	}

	/**
	 set the Task.onTaskDone() and Task.onTaskFail() always be invoked by SwingUtilities.invokeLater()
	*/
	public void setInvokedBySwing(boolean b){
	    invokedBySwing = b;
	}

	public void joinAll()throws InterruptedException{

            for(Thread t: threads)
                t.join();
	}

	public void interruptAll(){
	    synchronized(threads){
            for(Iterator<Thread> threadsIt = threads.iterator(); threadsIt.hasNext();){
                Thread t = threadsIt.next();
                t.interrupt();
            }
		}
		idle = 0;
	}

	public void clearTasks(){
	    taskQ1.clear();
	}

	public void addTask(Task t){
		//synchronized(this){
		log.fine(readStatus() + "[add Task]");
		synchronized(threads){
		    synchronized(this){
                if(idle<=0 && threads.size() < maxThreads){
                    log.fine("add Thread");
                    Thread th = new Runner();
                    threads.add(th);
                    th.start();
                }
            }
        }
		//}
		try{
			taskQ1.put(t);
		}catch( InterruptedException ie){
			log.severe("TaskQ1 is full?");
		}
	}

	private class Runner extends Thread{

		public Runner(){

		}

		public void run(){
			try{
				while(true){
					synchronized(BackgroundWorkController.this){
						idle++;
					}
					fireStatusChange(readStatus() + "[ready]");
					Task t = taskQ1.take();
					synchronized(BackgroundWorkController.this){
						idle--;
					}
					fireStatusChange(readStatus() + "[in progress]");
					Object o = null;
					try{
						o = t.execute();
						if(invokedBySwing){
                                SwingUtilities.invokeLater(new DoneSwingTask(t, o));
                        }else{
                            try{
                                t.onTaskDone(o);
                            }catch(Exception ex){
                                log.log(Level.SEVERE, "", ex);
                            }
                        }
					}catch(Exception ex){
					    if(invokedBySwing){
					        SwingUtilities.invokeLater(new FailSwingTask(t, ex));
					    }else{
					        try{
					            t.onTaskFail(ex);
					        }catch(Exception failex){
                                log.log(Level.SEVERE, "", failex);
                            }
					    }
					}catch(Throwable thr){
					    log.log(Level.SEVERE, "", thr);
					    throw thr;
					}

					synchronized(BackgroundWorkController.this){
						if(idle + 1 > idleThreads){
							return;
						}
					}
				}
			}catch(InterruptedException ie){
				log.fine(Thread.currentThread().getName() + "[terminated]");
				//idle--;
				fireStatusChange(readStatus() + "[to be terminated]");
			}catch(Exception ex){
			    log.log(Level.SEVERE, ex.getMessage(), ex);
			    fireStatusChange(readStatus() + "[to be removed due to exception]");
			    //idle--;
			}catch(Error rt){
			    log.log(Level.SEVERE, rt.getMessage(), rt);
			    fireStatusChange(readStatus() + "[to be removed due to error]");
			    throw rt;
			}catch(Throwable t){
			    log.log(Level.SEVERE, t.getMessage(), t);
			    fireStatusChange(readStatus() + "[to be removed due to throwable]");
			}finally{
			    synchronized(threads){
			        threads.remove(this);
			    }
			    fireStatusChange(readStatus() + "[removed]");
			}
		}
	}

	private class DoneSwingTask implements Runnable{
	    private Task task;
	    private Object result;
	    public DoneSwingTask(Task t, Object o){
	        task = t;
	        result = o;
	    }

	    public void run(){
	        task.onTaskDone(result);
	    }
	}

	private class FailSwingTask implements Runnable{
	    private Task task;
	    private Exception exception;
	    public FailSwingTask(Task t, Exception ex){
	        task = t;
	        exception = ex;
	    }

	    public void run(){
	        task.onTaskFail(exception);
	    }
	}

	public synchronized String readStatus(){
		return Thread.currentThread().getName()+ ": "+ taskQ1.size()
		+ " tasks are waiting, total threads: "+ threads.size()
		+ " idle:" + idle;
	}

	public void setStatusListener(StatusListener listener){
		statusListener = listener;
	}

	protected void fireStatusChange(String msg){
	    if(statusListener != null)
	        statusListener.onTextMessage(msg);
	    log.fine(msg);
	}

	public interface StatusListener{
		public void onTextMessage(String msg);
	}

	public interface Task<T>{
		public T execute() throws Exception;
		public void onTaskDone(T result);
		public void onTaskFail(Exception thr);
	}


}
