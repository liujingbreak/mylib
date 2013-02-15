package org.liujing.awttools;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.datatransfer.*;
import java.util.logging.*;

public abstract class DnDFileTransferHandler extends TransferHandler
{
	private static Logger log=Logger.getLogger(DnDFileTransferHandler.class.getName());
	
	public DnDFileTransferHandler(){}
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
	{
		return true;
	}
	public boolean importData(JComponent c, Transferable t) {
		if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
			try{
				Object o=t.getTransferData(DataFlavor.javaFileListFlavor);
				return processImportFiles(c,(java.util.List)o);
				//return true;
			}catch(Exception ex){
				log.log(Level.SEVERE,"Drag and drop failed.",ex);
				return false;
			}
		}
		return false;
	}
	
	protected abstract boolean processImportFiles(JComponent c,java.util.List files);
}
