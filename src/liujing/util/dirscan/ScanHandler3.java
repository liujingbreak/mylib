package liujing.util.dirscan;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public interface ScanHandler3 extends ScanHandler2{
	public void processZipEntry(ZipInputStream zin, ZipEntry entry, String relativePath);
}
