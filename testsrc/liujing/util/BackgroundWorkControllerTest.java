package liujing.util;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 BackgroundWorkControllerTest
 @author Break(Jing) Liu
*/
public class BackgroundWorkControllerTest{
    /** log */
    private static Logger log = Logger.getLogger(BackgroundWorkControllerTest.class.getName());
    BackgroundWorkController ctl;
    @Test
    public void test1() throws Exception{
        ctl = new BackgroundWorkController(3, 1);

        for(int i = 0; i< 4; i++)
            ctl.addTask(new TestTask(i));
        ctl.joinAll();
    }

    class TestTask implements BackgroundWorkController.Task{
        int id;
        public TestTask(int i){
            id = i;
        }

        public Object execute() throws Exception{
            Thread.sleep(3000);
            if(id == 3)
                throw new Exception("dummy exception");
            return null;
        }
		public void onTaskDone(Object result){
		    System.out.println(ctl.readStatus());
		}
		public void onTaskFail(Exception thr){
		    thr.printStackTrace();
		}
	}

	public static void main(String[] args)throws Exception{
	    new BackgroundWorkControllerTest().test1();
	}

}

