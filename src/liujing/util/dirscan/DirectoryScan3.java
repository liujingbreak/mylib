package liujing.util.dirscan;

import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
    support scan from zip/jar file
    */
public class DirectoryScan3 extends DirectoryScan2{
    private static Logger log = Logger.getLogger(DirectoryScan3.class.getName());
    
    public DirectoryScan3(){}
    
    public DirectoryScan3(Collection<String> includes, Collection<String> excludes,
	    boolean defaultIncludeAll){
	    super(includes, excludes, defaultIncludeAll);
	}
	
	public DirectoryScan3(Collection<String> includes, Collection<String> excludes){
		super(includes, excludes);
	}
	
	public void scan(File root, String fixedPath, ScanHandler3 handler)throws IOException{
	    super.scan(root, fixedPath, handler);
	}
	
	private boolean isZip(File f){
	    if(!f.isFile())
	        return false;
	    if(f.getName().length()>4 ){ 
            String suffix = 
                f.getName().substring(f.getName().length() -4);
            if(suffix.equalsIgnoreCase(".zip") || suffix.equalsIgnoreCase(".jar")){
                return true;
            }
            return false;
        }
        return false;
	}
	
	@Override
	protected void dirRecursion(File dir, int idx, List<CompiledExpress> paths, String fixedPath, ScanHandler2 handler)
	throws IOException{
	    if(isZip(dir)){
	        ScanHandler3 handler3 = (ScanHandler3) handler;
	        ZipInputStream zin = new ZipInputStream(new FileInputStream(dir));
	        String zipFilePath = relativePath(scanRoot, dir);
	        while(true){
	            ZipEntry ent = zin.getNextEntry();
	            if(ent == null)
	                break;
	            handleZipEntry(ent, zin, zipFilePath + "/" + ent.getName(), paths, handler3);
	            zin.closeEntry();
	        }
	    }else{
	        super.dirRecursion(dir, idx, paths, fixedPath, handler);
	    }
	}
	
	@Override
	protected void dirTraversal(File dir, int idx, List<CompiledExpress> paths,
	    String fixedPath, ScanHandler2 handler)throws IOException
	{
	    if(isZip(dir)){
	        ScanHandler3 handler3 = (ScanHandler3) handler;
	        ZipInputStream zin = new ZipInputStream(new FileInputStream(dir));
	        String zipFilePath = relativePath(scanRoot, dir);
	        while(true){
	            ZipEntry ent = zin.getNextEntry();
	            if(ent == null)
	                break;
	            handleZipEntry(ent, zin, zipFilePath + "/" + ent.getName(), paths, handler3);
	            zin.closeEntry();
	        }
	    }else{
	        super.dirTraversal(dir, idx, paths, fixedPath, handler);
	    }
	}

	protected void handleZipEntry(ZipEntry entry, ZipInputStream zin,
	    String relativePath, List<CompiledExpress> paths, ScanHandler3 handler)
	{
	    //log.fine("zip entry:"+ relativePath);
	    if(isPathMatch(relativePath, paths) && !isPathExclude(relativePath)){
	        handler.processZipEntry(zin, entry, relativePath);
	    }
	}
}
