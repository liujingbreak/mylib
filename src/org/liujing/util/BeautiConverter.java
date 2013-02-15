package org.liujing.util;

import java.io.*;
import java.util.logging.*;
/**
find out \r\r and convert them to a single \r.
*/
public class BeautiConverter
{
	private static Logger log=Logger.getLogger(BeautiConverter.class.getName());
	private String nameFilter=".*\\.java";
	public void setNameFilter(String reg)
	{
		nameFilter=reg;
	}
	
	public int convert(File f)
	{
		int account=0;
		if(f.isDirectory()){
			log.info("scan "+f.getPath());
			File[] subfiles= f.listFiles();
			for(int i=0;i<subfiles.length;i++){
				account+=convert(subfiles[i]);
			}
		}else{
			if(f.getName().matches(nameFilter)){
				log.fine("find "+f.getName());
				try{
					if(doFile(f)>0)
						account++;
				}catch(Exception ex){
					ex.printStackTrace();
					log.log(Level.WARNING,"",ex);
				}
				
			}else{
				log.fine(f.getName()+" doesn't match name filter");
			}
		}
		return account;
	}

	
	protected int doFile(File f)throws Exception
	{
		
		File tempFile=new File(Thread.currentThread().getName()+"-UnixTextConverter_temp.txt");
		BufferedOutputStream fout=new BufferedOutputStream(new FileOutputStream(tempFile,false));
		BufferedInputStream fin=new BufferedInputStream(new FileInputStream(f));
		int char1=fin.read();
		int char2=0;
		boolean needConvert=false;
		while(char1!=-1 && char2!=-1){
			if(char1=='\r'){
				char2=fin.read();
				if(char2=='\r'){
					fout.write(char2);
					needConvert=true;
				}else{
					fout.write(char1);
					fout.write(char2);
				}
			}else{
				fout.write(char1);
			}
			char1=fin.read();
		}
		fout.close();
		fin.close();
		if(needConvert){
			f.delete();
			tempFile.renameTo(f);
			log.info("converted");
			return 1;
		}else{
			tempFile.delete();
			return 0;
		}
	}
	
	public static void main(String[] args){
		try{
			BeautiConverter u=new BeautiConverter();
			for(int i=0;i<args.length;i++){
				int total=u.convert(new File(args[i]));
				System.out.println("convert "+total);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
