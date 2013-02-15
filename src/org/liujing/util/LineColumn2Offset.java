package org.liujing.util;

import java.io.*;
import java.util.logging.*;
import java.util.*;

public class LineColumn2Offset{
	private static Logger log = Logger.getLogger(LineColumn2Offset.class.getName());
	private List locations = new ArrayList();
	private LocationComp comp = new LocationComp();
	private Reader target;
	/**
	@param input target text buffer
	*/
	public LineColumn2Offset(Reader input){
		target = input;
	}
	
	public void addLocation( LineColumnInfo location ){
		locations.add(location);
	}
	
	public void setSortLocations(List<? extends LineColumnInfo> locations){
		this.locations = locations;
	}
	
	/**
	Convert the line number and column number info to offset info
	*/
	public void convert()throws IOException{
		if(locations.size()<=0)
			return;
		//sort locations
		Collections.sort(locations,comp);
		int currLine = 1;
		int targetOffset = 0;
		int nextChar = target.read();
		
		Iterator locIt = locations.iterator();
		while(locIt.hasNext()){
			LineColumnInfo nextLoc = (LineColumnInfo)locIt.next();
			int nextLineNo = nextLoc.getLine();
			
			if(log.isLoggable(Level.FINE))
				log.fine("next line is "+nextLineNo+" currLine is "+currLine);
			
			while(nextChar != -1 && currLine < nextLineNo){				
				if(nextChar == '\n'){
					currLine++;
					targetOffset++;
					nextChar = target.read();
					if(nextChar == '\r'){
						targetOffset++;
						nextChar = target.read();
					}
				}else{
					targetOffset++;
					nextChar = target.read();
				}
			}
			if(log.isLoggable(Level.FINE))
				log.fine("targetOffset is "+ (targetOffset + nextLoc.getColumn() - 1));
			nextLoc.setOffset(targetOffset + nextLoc.getColumn() - 1);
		}
	}
	
	public static class LCLocation implements LineColumnInfo{
		public int line = 0;
		public int column = 0;
		public int offset = -1;
		public LCLocation(int line, int column){
			this.line = line;
			this.column = column;
		}
		
		public int getLine(){
			return line;
		}
		
		public int getColumn(){
			return column;
		}
	
		public int getOffset(){
			return offset;
		}
		
		public void setOffset(int o){
			offset = o;
		}
	}
	
	private class LocationComp implements Comparator<LineColumnInfo>{
		public LocationComp(){
		}
		
		public int compare(LineColumnInfo o1, LineColumnInfo o2){
			int i = o1.getLine() - o2.getLine();
			if(i!=0)
				return i;
			else
				return o1.getColumn() - o2.getColumn();
		}
		
		public boolean equals(Object obj){
			return this.equals(obj);
		}
	}
}
