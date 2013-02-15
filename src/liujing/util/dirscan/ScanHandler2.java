package liujing.util.dirscan;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public interface ScanHandler2{
	public void processFile(File f, String relativePath);
}
