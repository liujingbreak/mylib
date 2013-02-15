package liujing.persist;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import org.junit.*;

/**
 PersistentTreeSetTest
 @author Break(Jing) Liu
*/
public class PersistentTreeSetTest{
    /** log */
    private static Logger log = Logger.getLogger(PersistentTreeSetTest.class.getName());

    public PersistentTreeSetTest(){
    }

    @Test
    public void testWrite()throws Exception{
        PersistentTreeSet<String> set = new PersistentTreeSet();
        set.create(new File("./testt-PersistentTreeSet"), new TestComparator());
        set.add("xyz");
        set.add("abc");
        set.close();
    }

    static class TestComparator implements PersistentTreeSet.PersistentComparator<String>{
        public int compare(String s, String s2){
                    return s.hashCode() - s2.hashCode();
                }
    }
}

