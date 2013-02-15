package org.liujing.util;

import java.util.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileOperator
{
	private Writer logWriter=null;
	public static String endLine="\n";



	public void startTrace(File fileToWriteLog)throws IOException
	{
		logWriter=new BufferedWriter(new FileWriter(fileToWriteLog));
		endLine=System.getProperty("line.separator");
	}
	public void endTrace()throws IOException
	{
		logWriter.close();
		logWriter=null;
	}
	
	public void copyFile(File src, File dest) throws IOException
	{
		if (src.isDirectory())
			throw new IOException("[" + src + "] is a directory");
		if (dest.getAbsolutePath().endsWith(File.separator)
				|| dest.isDirectory())
		{
			if (!dest.exists())
				dest.mkdirs();
			dest = new File(dest.getAbsolutePath() + File.separator
					+ src.getName());
		}
		else
		{
			if (dest.getParentFile() != null)
				if (!dest.getParentFile().exists())
					dest.getParentFile().mkdirs();
		}
		FileInputStream fis = new FileInputStream(src);
		FileChannel fcin = fis.getChannel();
		FileOutputStream fos = new FileOutputStream(dest);
		FileChannel fcout = fos.getChannel();
		ByteBuffer buf = ByteBuffer.allocate(1024);
		try
		{
			while (fcin.read(buf) > 0)
			{
				buf.flip();
				fcout.write(buf);
				buf.clear();
			}
			buf = null;

		}
		finally
		{
			fcout.close();
			fcin.close();
		}
		dest.setLastModified(src.lastModified());
		System.out.println("Copy file " + src.getAbsolutePath() + " to "
				+ dest.getAbsolutePath());
		if(logWriter!=null)
			_trace(dest);
	}

	public int copyFilesToDir(File srcDir,String fileNamePostfix,File destDir)
	throws IOException{
		File[] files=srcDir.listFiles();
		int copyCount=0;
		for(int i=0;i<files.length;i++)
		{
			if(fileNamePostfix!=null)
				fileNamePostfix=fileNamePostfix.toLowerCase();
			String fileName=files[i].getName();
			
			
			if(files[i].isDirectory()){
				copyFilesToDir(files[i],fileNamePostfix,new File(destDir,fileName));
			}
			else{
				if(fileNamePostfix!=null && (fileName.length()>4 && fileName.toLowerCase().endsWith(fileNamePostfix)))
				{
					copyFile(files[i],new File(destDir,fileName));
					copyCount++;
				}else if(fileNamePostfix==null){
					copyFile(files[i],new File(destDir,fileName));
					copyCount++;
				}
			}
		}
		return copyCount;
	}
	public int copyFilesToDir(File srcDir,File destDir)
	throws IOException{
		return copyFilesToDir(srcDir,null,destDir);
	}

	public List findFiles(File dir,String nameRegexPattern)
	{
		File[] files=dir.listFiles();
		List foundFiles=new ArrayList();
		for(int i=0;i<files.length;i++)
		{
			String fileName=files[i].getName();
			if(fileName.matches(nameRegexPattern)){
				foundFiles.add(files[i]);
				System.out.println("find "+files[i].getAbsolutePath());
			}else if(files[i].isDirectory()){
				List lst=findFiles(files[i],nameRegexPattern);
				foundFiles.addAll(lst);
			}
		}
		return foundFiles;
	}
	
	public void delete(File f)
	{
		if(f.isDirectory()){
			File []subF=f.listFiles();
			for(int i=0;i<subF.length;i++){
				delete(subF[i]);
			}
			f.delete();
		}
		else{
			f.delete();
		}			
	}
	protected void _trace(File f)throws IOException
	{
		logWriter.write(f.getAbsolutePath());
		logWriter.write(endLine);
	}
	
	
	protected  void finalize()
	{
		try{
		if(logWriter!=null)
			logWriter.close();
		}catch(IOException e)
		{}
	}


}
