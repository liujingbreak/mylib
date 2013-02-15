package org.liujing.util;

import java.nio.*;
import java.io.*;
import java.util.*;
import webl.util.*;
import java.nio.channels.*;
import java.nio.charset.*;

/**
save large amount string array data in hard disk, and buffer it.

*/
public class BufferedStringArray
{
/*	protected int size=0;
	protected File fBuffFile=null;
	protected File fHeadMapFile=null;
	protected BufferedOutputStream _out=null;
	protected long lWritePos=0;
	protected ArrayList posList;
	protected FileChannel fileChannel;
	protected CharBuffer buff;
	
	public BufferedStringArray()
	{
		String title=BufferedStringArray.class.getName()+Thread.currentThread().getName();
		fBuffFile=new File(title+"_tempBuffer.dat");
		fHeadMapFile=new File(title+"_tempHead.dat");

		
		fileChannel=new FileOutputStream(fBuffFile,false).getChannel();
		_out=new BufferedOutputStream(new FileOutputStream(fBuffFile,false),0x10000);
		posList=new ArrayList();
	}
	
	public void add(String str)
	{
		if(buff==null){
			buff=CharBuffer.allocate(0x8000);
			buff.clear();
		}
		if(buff.remaining()<str.length()){
			buff.flip();
			fileChannel.write(encode(buff));
			buff.compact();
		}
		
		if(str.length()>buff.remaining()){
			CharBuffer buff1=CharBuffer.allocate(str.length());
			buff.flip();
			buff1.put(buff);
			buff1=buff;
		}
		buff.put(str);
		posList.add(new Long(lWritePos));
		//lWritePos+=b.length;
		size++;
	}

	public void get(int i)
	{
		
	}
	public int size()
	{
		return size;
	}

	protected void _checkAndFlush()
	{
		
	}
	public static ByteBuffer encode(CharBuffer in)
	{
		Charset charset = Charset.forName("ISO-8859-1");
		CharsetEncoder encoder=charset.newEncoder();
		return encoder.encode(in);
	}*/
}
