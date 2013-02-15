package liujing.persist;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.liujing.util.*;

public class FileAccessTest{
    private static Logger log = Logger.getLogger(FileAccessTest.class.getName());

    @Ignore @Test
    public void test1()throws Exception{
        FileRecordController fa = new FileRecordController(new File("testFileAccess.txt"));
        //fa.truncate();
        long p = fa.add("liu".getBytes());
        log.info("p = " + p);
        p = fa.add(" jing".getBytes());
        log.info("p = " + p);

    }

    @Ignore @Test
    public void test2()throws Exception{
        FileRecordController fa = new FileRecordController(new File("testFileAccess.txt"));
        //fa.truncate();
        int r1 = fa.add("liu".getBytes());
        log.info("r1 = " + r1);
        int r2 = fa.add("jing.break".getBytes());
        log.info("r2 = " + r2);
        int r3 = fa.add("@gmail".getBytes());

        String s1 = new String(fa.getBytes(r1));
        log.info("s1 = " + s1);

        String s2 = new String(fa.getBytes(r2));
        log.info("s2 = " + s2);

        String s3 = new String(fa.getBytes(r3));
        log.info("s3 = " + s3);

        fa.delete(r2);
        int r4 = fa.add(".com".getBytes());
        log.info("s4 = " + new String(fa.getBytes(r4)));

        log.info(r1 + ", " + r2 + ", " + r3 + ", " + r4);
    }

    //@Test
    public void testFileObjectRef()throws Exception{
        try{
            FileRecordController fa = new FileRecordController(new File("testDemo.txt"));
            Demo demo = new Demo("_abc_", "_efg_", fa);
            FileObjectRef<Demo> fr = new FileObjectRef<Demo>(demo);

            log.info(fr.getValue(fa).toString(fa));
        }catch(Exception e){
            log.log(Level.SEVERE, "", e);
        }
    }

    private static class Demo implements Serializable{
        FileObjectRef<String> child;
        String content;

        public Demo(String s, String fs, FileRecordController fa)throws Exception{
            content = s;
            child = new FileObjectRef();
            child.setValue(fa, fs);
        }

        public String toString(FileRecordController fa)throws Exception{
            return content + ", " + child.getValue(fa);
        }
    }

    @Test
    public void testDynamic1()throws Exception{
        log.info("----start-----------");
        FileRecordController fa = new FileRecordController(new File("dynDemo.txt"));
        fa.truncate();
        int idx = fa.allocate(4);
        fa.update(idx, "abcd".getBytes());
        log.info("just write: " + new String(fa.getBytes(idx)));
        fa.update(idx, "1234567".getBytes());
        log.info("just write: " + new String(fa.getBytes(idx)));
        log.info(fa.toString());

        int idx2 = fa.allocate(10, "xyz".getBytes());
        log.info("just write: " + new String(fa.getBytes(idx2)));
        log.info(fa.toString());
        fa.update(idx, "01234567".getBytes());
        log.info("just update: " + new String(fa.getBytes(idx)));
        log.info("last written: " + new String(fa.getBytes(idx2)));
        log.info(fa.toString());
        fa.update(idx, "8901234567".getBytes());
        log.info("just update: " + new String(fa.getBytes(idx)));
        log.info(fa.toString());
    }


}
