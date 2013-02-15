package liujing.util.dirscan;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
/**
Pattern usage:
Special symbols: "{", "}", ",", "!", "**", "*"
<pre>for example:
    {dir1,dir2}\**\*
    **\*\!*.class
    !{dir1,dir2}\**\*.java
    **\!dir1\*
</pre>
*/
public class DirectoryScan2 implements Serializable{
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(DirectoryScan2.class.getName());
    private Collection<String> defaultIncludes = new ArrayList<String>();
    private List<List<CompiledExpress>> includesPat = new ArrayList<List<CompiledExpress>>();
    private List<List<CompiledExpress>> excludesPat = new ArrayList<List<CompiledExpress>>();
    private List<File> absoluteDirs = new ArrayList<File>();
    protected transient File scanRoot;

	private boolean skipHiddenFile = true;
	private boolean defaultIncludeAll = true;
	private boolean enableDirectory = false;

	public DirectoryScan2(){
	    //initDefaultIncludes();
	}

	public DirectoryScan2(boolean defaultIncludeAll){
	    setDefaultIncludeAll(defaultIncludeAll);
	    //initDefaultIncludes();
	}

	public DirectoryScan2(Collection<String> includes){
	    setIncludes(includes);
	}

	public DirectoryScan2(Collection<String> includes, boolean defaultIncludeAll){
	    setDefaultIncludeAll(defaultIncludeAll);
	    setIncludes(includes);
	}

	public DirectoryScan2(Collection<String> includes, Collection<String> excludes){
		setIncludes(includes);
		setExcludes(excludes);
	}

	public DirectoryScan2(Collection<String> includes, Collection<String> excludes,
	    boolean defaultIncludeAll){
	    setDefaultIncludeAll(defaultIncludeAll);
		setIncludes(includes);
		setExcludes(excludes);
	}

	public void setDefaultIncludeAll(boolean includeAll){
	    defaultIncludeAll = includeAll;
	}

	private void initDefaultIncludes(){
	    if(!defaultIncludeAll)
	        return;
	    
	    if(includesPat == null || includesPat.size() == 0){
	        defaultIncludes.clear();
            defaultIncludes.add("**/*");
            compileIncludeList(defaultIncludes);
        }
	}

	public void setIncludes(Collection<String> includes){
	    //if((includes == null || includes.size() == 0) && defaultIncludeAll)
	    //    initDefaultIncludes();
	    //else
	    compileIncludeList(includes);
	}

	public void setExcludes(Collection<String> excludes){
	    compileExcludeList(excludes);
	}

	/**
	@param enable if true, scan result will includes all matched directroies as well as files
	*/
	public void enableDirectory(boolean enable){
	    enableDirectory = enable;
	}

	protected void compileIncludeList(Collection<String> includes){
	    includesPat.clear();
	    absoluteDirs.clear();
	    Iterator<String> inIt = includes.iterator();
	    while(inIt.hasNext()){
	        String exp = inIt.next();
	        boolean startsWithSlash = exp.startsWith("/") || exp.startsWith("\\");
	        boolean startsWidthDisk = (exp.length() > 1 && exp.charAt(1) == ':');
	        List<CompiledExpress> pats = compilePattern(exp);
	        if(startsWithSlash){
	            absoluteDirs.add(new File(File.separator));
	            pats.remove(0);
	        }else if(startsWidthDisk){
	            absoluteDirs.add(new File((String)pats.get(0).str));
	            pats.remove(0);
	        }else{
	            absoluteDirs.add(null);
	        }
	        includesPat.add(pats);
	    }
	}

	protected void compileExcludeList(Collection<String> excludes){
	    excludesPat.clear();
	    Iterator<String> it = excludes.iterator();
	    while(it.hasNext()){
	        String exp = it.next();
	        List<CompiledExpress> pats = compilePattern(exp);
	        excludesPat.add(pats);
	    }
	}


	protected class CompiledExpress implements Serializable{
	    private static final long serialVersionUID = 1L;
	    String str;

	    Pattern pat;
	    /** there no is "!" in path pattern*/
	    boolean include;

	    public CompiledExpress(String s){
	        str = s;
	    }

	    public CompiledExpress(Pattern p, boolean include){
	        pat = p;
	        this.include = include;
	    }
	}


	protected List<CompiledExpress> compilePattern(String pattern){
		String[] sc = pattern.split("[/\\\\]");
		List<CompiledExpress> paths = new ArrayList();

		for(String p: sc){
		    boolean isInclude = true;
		    if(p.startsWith("!")){
		        p = p.substring(1).trim();
		        isInclude = false;
		    }
		    CompiledExpress exp = null;
			if( (!p.equals("**")) && (p.contains("*") || p.contains("{") || !isInclude) ){
				p = "\\Q" + p + "\\E";
				p = p.replaceAll("\\*", "\\\\E.*\\\\Q");
				p = p.replaceAll("\\{", "\\\\E(?:\\\\Q");
				p = p.replaceAll("\\}", "\\\\E)\\\\Q");
				p = p.replaceAll(",", "\\\\E|\\\\Q");
				log.fine(p);
				Pattern pat = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
				exp = new CompiledExpress(pat, isInclude);
			}else{
				exp = new CompiledExpress(p);
			}
			paths.add(exp);
		}
		return paths;
	}

	public void scan(File root, ScanHandler2 handler)throws IOException{
	    initDefaultIncludes();
	    scanRoot = root;
	    Iterator<List<CompiledExpress>> it = includesPat.iterator();
	    Iterator<File> rootIt = absoluteDirs.iterator();
	    while(it.hasNext()){
	        File r = rootIt.next();
	        if(r == null)
	            r = root;
	        pathAnalyse(r, 0, it.next(), null, handler);
	    }
	}

	/**
	@param fixedPath providing an non-null string to incidate that scaning should only cover this sub folder/path
	*/
	public void scan(File root, String fixedPath, ScanHandler2 handler)throws IOException{
	    initDefaultIncludes();
	    String coverPath = fixedPath == null? "":fixedPath;
	    String skip = coverPath.replaceAll("\\\\", "/");
	    if(skip.startsWith("/")){
	        skip = skip.substring(1);
	    }
	    if(skip.endsWith("/")){
	        skip = skip.substring(0, skip.length() - 1);
	    }
	    scanRoot = root;
	    Iterator<List<CompiledExpress>> it = includesPat.iterator();
	    Iterator<File> rootIt = absoluteDirs.iterator();
	    while(it.hasNext()){
	        File r = rootIt.next();
	        if(r == null)
	            r = root;
	        pathAnalyse(r, 0, it.next(), skip, handler);
	    }
	}


	protected void pathAnalyse(File dir, int idx, List<CompiledExpress> paths,
	    String fixedPath, ScanHandler2 handler)
	            throws IOException{
	    if(skipHiddenFile && (dir.isHidden() && !dir.getPath().contains(":") ))
			return;
		if(idx < (paths.size()) ){
			if(log.isLoggable(Level.FINE))
				log.fine(dir.getPath() + " idx:"+ idx + " path: "+ paths.get(idx));
			CompiledExpress exp = paths.get(idx);
			if(exp.str != null && exp.str.equals("**"))
				dirRecursion(dir, idx, paths, fixedPath, handler);
			else if(exp.pat != null){
				dirTraversal(dir, idx, paths, fixedPath, handler);
			}else{
				dirLocate(dir, idx, paths, fixedPath, handler);
			}
		}else{
			if(handler != null && (enableDirectory || !dir.isDirectory()) &&
			    (fixedPath == null || fixedPath.length() == 0 )){
			    String relativePath = relativePath(scanRoot, dir);
			    if(!isPathExclude(relativePath))
			        handler.processFile(dir, relativePath);
			}
		}
	}

	protected void dirRecursion(File dir, int idx, List<CompiledExpress> paths, String fixedPath, ScanHandler2 handler)
	throws IOException{

		if(!dir.isDirectory())
			return;
		if(skipHiddenFile && dir.isHidden())
			return;
		pathAnalyse(dir, idx + 1, paths, fixedPath, handler);
		if(fixedPath == null || fixedPath.length() == 0){
            File[] files = dir.listFiles();

            for( File f: files){
                //pathAnalyse(f, idx + 1, paths, handler);
                dirRecursion(f, idx, paths, null, handler);
            }
        }else{
            String nextName = null;
            int p = fixedPath.indexOf("/");
            if(p >= 0){
                nextName = fixedPath.substring(0, p);
                fixedPath = fixedPath.substring(p + 1);
            }else{
                nextName = fixedPath;
                fixedPath = null;
            }
            File f = new File(dir, nextName);
            log.info("nextName="+ nextName + " f=" + f.getPath());
            if(!f.exists()){
                return;
            }
            dirRecursion(f, idx, paths, fixedPath, handler);
        }

	}

	protected void dirTraversal(File dir, int idx, List<CompiledExpress> paths, String fixedPath, ScanHandler2 handler)throws IOException{
		if(log.isLoggable(Level.FINE))
			log.fine(dir.getPath() + " idx:"+ idx );
		if(!dir.isDirectory())
			return;
		if(skipHiddenFile && dir.isHidden())
			return;
		if(fixedPath == null || fixedPath.length() == 0){
            File[] files = dir.listFiles();
            for( File f: files){
                Pattern pat = paths.get(idx).pat;
                boolean isInclude = paths.get(idx).include;
                boolean matches = pat.matcher(f.getName()).matches();
                if((isInclude && matches) || ((!isInclude) && !matches)){
                    pathAnalyse(f, idx + 1, paths, null, handler);
                }
            }
        }else{
            String nextName = null;
            int p = fixedPath.indexOf("/");
            if(p >= 0){
                nextName = fixedPath.substring(0, p);
                fixedPath = fixedPath.substring(p + 1);
            }else{
                nextName = fixedPath;
                fixedPath = null;
            }
            File f = new File(dir, nextName);
            if(!f.exists()){
                return;
            }
            Pattern pat = paths.get(idx).pat;
            boolean isInclude = paths.get(idx).include;
            boolean matches = pat.matcher(f.getName()).matches();
            if((isInclude && matches) || ((!isInclude) && !matches)){
                pathAnalyse(f, idx + 1, paths, fixedPath, handler);
            }
        }
	}

	private void dirLocate(File dir, int idx, List<CompiledExpress> paths, String fixedPath, ScanHandler2 handler)throws IOException{
		if(fixedPath == null || fixedPath.length() == 0){
            File f = new File(dir, paths.get(idx).str);
            if(f.exists()){
                pathAnalyse(f, idx + 1, paths, null, handler);
            }else{
                if(log.isLoggable(Level.FINE))
                    log.fine(f.getPath() + " does not exist" );
            }
        }else{
            String nextName = null;
            int p = fixedPath.indexOf("/");
            if(p >= 0){
                nextName = fixedPath.substring(0, p);
                fixedPath = fixedPath.substring(p + 1);
            }else{
                nextName = fixedPath;
                fixedPath = null;
            }
            if(paths.get(idx).str.equals(nextName)){
                File f = new File(dir, nextName);
                if(f.exists()){
                    pathAnalyse(f, idx + 1, paths, fixedPath, handler);
                }else{

                }
            }
        }
	}

	public boolean isPathInclude(File root, File target)throws IOException{
	    if( (!enableDirectory) && target.isDirectory())
	        return false;
	    return isPathInclude(relativePath(root, target));
	}

	public boolean isPathInclude(String relativePath){
	    if(includesPat == null)
	        throw new IllegalArgumentException("Include list is null");
	    Iterator<List<CompiledExpress>> it = includesPat.iterator();
	    while(it.hasNext()){
	        List<CompiledExpress> compiledPath = it.next();
	        if(isPathMatch(relativePath, compiledPath))
	            return true;
	    }
	    return false;
	}

	public boolean isPathExclude(File root, File target)throws IOException{
	    return isPathExclude(relativePath(root, target));
	}

	public boolean isPathExclude(String relativePath){
	    if(excludesPat == null)
	        throw new IllegalArgumentException("Exclude list is null");
	    Iterator<List<CompiledExpress>> it = excludesPat.iterator();
	    while(it.hasNext()){
	        List<CompiledExpress> compiledPath = it.next();
	        if(isPathMatch(relativePath, compiledPath))
	            return true;
	    }
	    return false;
	}

	protected boolean isPathMatch(String relativePath, List<CompiledExpress> compiledPath){
	    String[] dirs = relativePath.split("[/\\\\]");
		return analysePath(compiledPath, 0, dirs, 0);
	}

	/**
	Test if the folder path matches target dir pattern in the includes list, that
	means the folder might contain any file which matches entire pattern.
	@param targetDir the directory
	*/
	public boolean maybePathInclude(File root, File targetDir)throws IOException{
	    return maybePathInclude(relativePath(root, targetDir));
	}

	/**
	Test if the folder path matches target dir pattern in the includes list, that
	means the folder might contain any file which matches entire pattern.
	@param relativeDirPath the relative path of the directory
	*/
	public boolean maybePathInclude(String relativeDirPath)throws IOException{
	    if(includesPat == null)
	        throw new IllegalArgumentException("Include list is null");
	    Iterator<List<CompiledExpress>> it = includesPat.iterator();
	    while(it.hasNext()){
	        List<CompiledExpress> compiledPath = it.next();
	        if(maybeFoldMatch(relativeDirPath, compiledPath))
	            return true;
	    }
	    return false;
	}
	/**
	test the path not by the entire compiled path, but by len - 1 of compiled path
	*/
	protected boolean maybeFoldMatch(String relativePath, List<CompiledExpress> compiledPath){
	    String[] dirs = relativePath.split("[/\\\\]");
		return analyseFolder(compiledPath, 0, dirs, 0);
	}

	protected static String relativePath(File rootDir, File target)throws IOException{
	    String ca = rootDir.getCanonicalPath();
		String fa = target.getCanonicalPath();
		String path = null;
		//log.info("currDir=" + ca + " f=" + fa);
		
		if(fa.startsWith(ca)){
			int start = (ca.endsWith("\\") || ca.endsWith("/"))? ca.length(): (ca.length() +1);
			if(start >= fa.length())
			    path = "";
			else
			    path = fa.substring(start);
		}else{
			path = target.getPath();
		}
		return path;
	}

	private boolean analysePath( List<CompiledExpress> compiledPath, int patIdx, String[] dirs, int dirIdx){
	    return analysePath(compiledPath, compiledPath.size(), patIdx, dirs, dirIdx);
	}

	private boolean analysePath( List<CompiledExpress> compiledPath, int testLen, int patIdx, String[] dirs, int dirIdx){

		if(patIdx < testLen ){
			if(dirIdx >= dirs.length)
				return false;
			CompiledExpress p = compiledPath.get(patIdx);
			if(p.str != null && p.str.equals("**")){
				patIdx ++;
				while(dirIdx < dirs.length){
					if(analysePath(compiledPath, patIdx, dirs, dirIdx))
						return true;
					dirIdx++;
				}
				return false;
			}else if(p.pat != null){
			    boolean isInclude = p.include;
			    boolean matches = p.pat.matcher(dirs[dirIdx]).matches();
				if((isInclude && matches) || ((!isInclude) && !matches))
					return analysePath(compiledPath, patIdx +1, dirs, dirIdx +1);

			}else{
				if(p.str.equalsIgnoreCase(dirs[dirIdx]))
					return analysePath(compiledPath, patIdx +1, dirs, dirIdx +1);
			}
			return false;
		}else{
			return dirIdx == dirs.length;
		}
	}

	private boolean analyseFolder( List<CompiledExpress> compiledPath, int patIdx, String[] dirs, int dirIdx){
		if(dirIdx == dirs.length)
		    return true;
		else if(patIdx < compiledPath.size() -1 ){
			if(dirIdx >= dirs.length)
				return false;
			CompiledExpress p = compiledPath.get(patIdx);
			if(p.str != null && p.str.equals("**")){
				//always true
				patIdx ++;
				dirIdx = dirs.length;
				return true;
			}else if(p.pat != null){
			    boolean isInclude = p.include;
			    boolean matches = p.pat.matcher(dirs[dirIdx]).matches();
				if((isInclude && matches) || ((!isInclude) && !matches))
					return analyseFolder(compiledPath, patIdx +1, dirs, dirIdx +1);

			}else{
				if(p.str.equalsIgnoreCase(dirs[dirIdx]))
					return analyseFolder(compiledPath, patIdx +1, dirs, dirIdx +1);
			}
			return false;
		}else{
			return false;
		}
	}
}
