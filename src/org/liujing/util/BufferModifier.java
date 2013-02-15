package org.liujing.util;

import java.io.*;
import java.util.logging.*;
import java.util.*;

public class BufferModifier{
	
	public static Logger log = Logger.getLogger(BufferModifier.class.getName());
	
	private int currOffset = 0;
	
	private List<ModifyRequest> requests = new ArrayList();
	private Comparator<ModifyRequest> comp;
	private boolean lineColumnMode = false;
	
	public BufferModifier(){
		comp = new Comparator<ModifyRequest>(){
			public int compare(ModifyRequest o1, ModifyRequest o2){
				return o1.offset - o2.offset;
			}
			public boolean equals(Object obj){
				return this.equals(obj);
			}
		};
	}
	
	public void insertText(int offset, String text){
		requests.add(new InsertRequest(offset,text));
	}
	
	public void insertText(int line, int column, String text){
		lineColumnMode = true;
		requests.add(new InsertRequest(line,column,text));
	}
	
	public void deleteText(int offset, int len){
		requests.add(new DeleteRequest(offset,len));
	}
	
	public void deleteText(int line, int column, int len){
		lineColumnMode = true;
		requests.add(new DeleteRequest(line, column,len));
	}
	
	public void replaceText(int offset, int len, String replace){
		requests.add(new ReplaceRequest(offset,len, replace));
	}
	
	public void replaceText(int line, int column, int len, String replace){
		lineColumnMode = true;
		requests.add(new ReplaceRequest(line, column,len, replace));
	}
	
	public void processFile(File target, File newFile, boolean backup) throws IOException{
		
	}
	
	public void process(TextModifier impl)throws IOException{
		if(lineColumnMode){
			LineColumn2Offset lc2o = new LineColumn2Offset(impl.getReader());
			lc2o.setSortLocations(requests);
			lc2o.convert();
		}else{
			Collections.sort(requests,comp);
		}
		Iterator<ModifyRequest> it = requests.iterator();
		while(it.hasNext()){
			ModifyRequest req = it.next();
			int delt = 0;
			if(req instanceof InsertRequest){
				InsertRequest r = (InsertRequest)req;
				impl.insert(r.offset + delt, r.offset, r.text);
				delt += r.text.length();
			}else if(req instanceof DeleteRequest){
				DeleteRequest r = (DeleteRequest)req;
				impl.delete(r.offset + delt, r.offset, r.length);
				delt -= r.length;
			}else if(req instanceof ReplaceRequest){
				ReplaceRequest r = (ReplaceRequest)req;
				impl.replace(r.offset + delt, r.offset, r.length, r.text);
				delt = delt + r.text.length() - r.length;
			}
		}
	}
	
	public static interface TextModifier{
		public Reader getReader();
		public void insert(int fixedOffset, int offset,String t);
		public void replace(int fixedOffset, int offset, int len, String replace);
		public void delete(int fixedOffset, int offset,int len);
	}
	
	public static class StringModifier implements TextModifier{
		private String origin;
		private StringBuilder buf = new StringBuilder();
		private int lastOffset = 0;
		private boolean done = false;
		
		public StringModifier(String origin){
			this.origin = origin;
		}
		
		public Reader getReader(){
			return new StringReader(origin);
		}
		
		public void insert(int fixedOffset, int offset,String t){
			log.info("insert ||" + offset);
			buf.append(origin.substring(lastOffset, offset));
			buf.append(t);
			lastOffset = offset;
			log.info("lastOffset: "+lastOffset + " offset:"+offset);
		}
		public void replace(int fixedOffset, int offset, int len, String replace){
			log.info("replace ||" + offset);
			buf.append(origin.substring(lastOffset, offset));
			buf.append(replace);
			lastOffset = offset + len;
			log.info("lastOffset: "+lastOffset + " offset:"+offset);
		}
		
		public void delete(int fixedOffset, int offset,int len){
			log.info("delete ||" + offset+" "+origin.substring(lastOffset, offset));
			buf.append(origin.substring(lastOffset, offset));
			lastOffset = offset + len;
			log.info("lastOffset: "+lastOffset + " offset:"+offset+ " len:"+len);
		}
		
		public String getResult(){
			if(!done){
				buf.append(origin.substring(lastOffset));
				done = true;
			}
			return buf.toString();
		}
	}
	
	protected static class ModifyRequest implements LineColumnInfo{
		public int line = 0;
		public int column = 0;
		public int offset = -1;
		public int length = 0;
		public String text;
		public ModifyRequest(int offset,int len,String t){
			this.offset = offset;
			this.text = t;
			length = len;
		}
		
		public ModifyRequest(int line, int column, int len,String t){
			this.line = line;
			this.column = column;
			this.text = t;
			length = len;
		}
		
		public int getLine(){
			return line;
		}
		public int getColumn(){
			return column;
		}
		public void setOffset(int o){
			offset = o;
		}
	}
	
	protected static class InsertRequest extends ModifyRequest{
		public InsertRequest(int offset,String t){
			super(offset,0,t);
		}
		
		public InsertRequest(int line, int column, String t){
			super(line, column,0,t);
		}
	}
	
	protected static class DeleteRequest extends ModifyRequest{
		public DeleteRequest(int offset, int len){
			super(offset,len,null);
		}
		
		public DeleteRequest(int line, int column, int len){
			super(line, column, len,null);
		}
	}
	
	protected static class ReplaceRequest  extends ModifyRequest{
		public ReplaceRequest(int offset, int len, String replace){
			super(offset,len,replace);
		}
		
		public ReplaceRequest(int line, int column, int len, String replace){
			super(line, column,len,replace);
		}
	}
	
}

