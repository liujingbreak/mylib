package liujing.util.dirscan;

import java.util.*;
import java.io.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.liujing.util.*;

public class DirectoryScan2Test{
    @Test
    public void testScan()throws Exception{
        List<String> includes = new ArrayList();
        includes.add("liujing/persist/*.class");
        DirectoryScan2 scanner = new DirectoryScan2(includes, new ArrayList<String>(), false);
        scanner.scan(new File("."), "liujing", new ScanHandler2(){
                public void processFile(File f, String relativePath){
                    System.out.println(">>>>>>>>>>>>scan "+ relativePath);
                }
        });
    }
}
