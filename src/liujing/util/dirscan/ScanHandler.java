package liujing.util.dirscan;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public interface ScanHandler{
	public void processFile(File f);
}
