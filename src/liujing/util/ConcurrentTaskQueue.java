package liujing.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.*;
import java.util.*;
/**
It provides a thread pool functionality for running those heavy tasks
Same as BackgroundWorkController but removed Swing dependency
*/
public class ConcurrentTaskQueue{
	private static Logger log = Logger.getLogger(ConcurrentTaskQueue.class.getName());

	protected BlockingQueue<Task> taskQ1 = new LinkedBlockingQueue();

	protected StatusListener statusListener;

	protected List<Thread> threads;
	/**
	number of current idle threads
	*/
	protected int idle = 0;

	protected int maxThreads = 1;

	protected int idleThreads = 1;

	public ConcurrentTaskQueue(){
	    this(1, 1);
	}


	public ConcurrentTaskQueue(int maxNumOfThread, int maxIdleNumOfThread){
	    maxThreads = maxNumOfThread;
		idleThreads = maxIdleNumOfThread;
		threads = new LinkedList();
	}

	public void joinAll()throws InterruptedException{
	    //synchronized(threads){
            for(Thread t: threads)
                t.join();
		//}
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
					synchronized(ConcurrentTaskQueue.this){
						idle++;
					}
					fireStatusChange(readStatus() + "[ready]");
					Task t = taskQ1.take();
					synchronized(ConcurrentTaskQueue.this){
						idle--;
					}
					fireStatusChange(readStatus() + "[in progress]");
					Object o = null;
					try{
						o = t.execute();

                        try{
                            t.onTaskDone(o);
                        }catch(Exception ex){
                            log.log(Level.SEVERE, "", ex);
                        }

					}catch(Exception ex){

					    try{
					        t.onTaskFail(ex);
					    }catch(Exception failex){
                            log.log(Level.SEVERE, "", failex);
                        }

					}catch(Throwable thr){
					    log.log(Level.SEVERE, "", thr);
					    throw thr;
					}

					synchronized(ConcurrentTaskQueue.this){
					    fireStatusChange(readStatus() + "[done]");
						if(idle + 1 > idleThreads){
						    log.fine("terminate thread");
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

	//private class DoneSwingTask implements Runnable{
	//    private Task task;
	//    private Object result;
	//    public DoneSwingTask(Task t, Object o){
	//        task = t;
	//        result = o;
	//    }
    //
	//    public void run(){
	//        task.onTaskDone(result);
	//    }
	//}
    //
	//private class FailSwingTask implements Runnable{
	//    private Task task;
	//    private Exception exception;
	//    public FailSwingTask(Task t, Exception ex){
	//        task = t;
	//        exception = ex;
	//    }
    //
	//    public void run(){
	//        task.onTaskFail(exception);
	//    }
	//}

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
