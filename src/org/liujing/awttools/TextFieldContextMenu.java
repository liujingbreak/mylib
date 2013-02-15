package org.liujing.awttools;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
//import org.liujing.tools.*;

/**
	usage:
	<code>
	static TextFieldContextMenu cm=new TextFieldContextMenu();
	cm.addBoundComponent(JTextComponent text);
	</code>
	The bound JTextComponent is referenced by a WeakHashMap.
*/
public class TextFieldContextMenu implements 
	MouseListener,PopupMenuListener,ActionListener,CaretListener
	,MouseMotionListener,KeyListener
{
	protected JPopupMenu popMenu=new JPopupMenu();
	protected JMenuItem itemCp=new JMenuItem("Copy");
	protected JMenuItem itemCut=new JMenuItem("Cut");
	protected JMenuItem itemPa=new JMenuItem("Paste");
	protected JMenuItem itemSelAll=new JMenuItem("Select All");
	protected JMenuItem itemUndo=new JMenuItem("Undo");
	protected JMenuItem itemRedo=new JMenuItem("Redo");
	protected JMenuItem itemCol=new JMenuItem("Column");
	
	protected WeakHashMap<JTextComponent,UndoManager> undoMgs=null;
	private JTextComponent currentSrcTextComp;
	//protected UndoManager undoMg=new UndoManager();
	protected boolean colModel=false;
	
	public TextFieldContextMenu()
	{
		itemCp.addActionListener(this);
		itemCut.addActionListener(this);
		itemPa.addActionListener(this);
		itemSelAll.addActionListener(this);
		itemUndo.addActionListener(this);
		itemRedo.addActionListener(this);
		itemCol.addActionListener(this);
		
		popMenu.add(itemCp);		
		popMenu.add(itemCut);
		popMenu.add(itemPa);
		popMenu.add(itemSelAll);
		popMenu.add(itemUndo);
		popMenu.add(itemRedo);
		popMenu.addSeparator();
		//popMenu.add(itemCol);
		popMenu.addPopupMenuListener(this);
		undoMgs=new WeakHashMap<JTextComponent,UndoManager>();
		/*
		GraphicsEnvironment environment =GraphicsEnvironment.getLocalGraphicsEnvironment();
        	GraphicsDevice device = environment.getDefaultScreenDevice();
		Font[] fonts=environment.getAllFonts();
		for(int i=0;i<fonts.length;i++){
			System.out.println(fonts[i].getName());
		}*/
	}
	public void addItem(JMenuItem item)
	{
		popMenu.add(item);
	}
	/**
	call this  method to bound it to a certain text component
	*/
	@Deprecated
	public void setTo(JTextComponent component)
	{
		addBoundComponent(component);
		/*component.addMouseListener(this);
		component.add(popMenu);
		currentSrcTextComp=component;
		currentSrcTextComp.getDocument().addUndoableEditListener(this);*/
	}
	
	public void addBoundComponent(JTextComponent component)
	{
		component.addMouseListener(this);
		UndoManager undom=new UndoManager();
		undoMgs.put(component,undom);
		component.getDocument().addUndoableEditListener(new TheUndoableEditListener(undom));
		component.addKeyListener(this);
	}
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode()==KeyEvent.VK_CONTEXT_MENU){
			currentSrcTextComp=(JTextComponent)e.getComponent();
			UndoManager undoMg=undoMgs.get(currentSrcTextComp);
			itemUndo.setEnabled(undoMg.canUndo());
			itemRedo.setEnabled(undoMg.canRedo());
			
			popMenu.show(e.getComponent(),1, 1);
		}
	}
	public void keyReleased(KeyEvent e)
	{
	}
	public void keyTyped(KeyEvent e){}
 

	public void mouseClicked(MouseEvent e)
	{	
	}
	public void mouseEntered(MouseEvent e)
	{}
	public void mouseExited(MouseEvent e)
	{}
	public void mousePressed(MouseEvent e)
	{
		maybeShowPopup(e);
	}
	public void mouseReleased(MouseEvent e)
	{
		maybeShowPopup(e);
	}
	
	public void popupMenuCanceled(PopupMenuEvent e) 
	 {
	 	//System.out.println("menu cancel:");
	 }
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
	public void popupMenuWillBecomeVisible(PopupMenuEvent e){}  

	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource()==itemCp){
			currentSrcTextComp.copy();return;
		}
		if(e.getSource()==itemPa){
			currentSrcTextComp.paste();
			return;
		}
		if(e.getSource()==itemCut){
			currentSrcTextComp.cut();
			return;
		}
		if(e.getSource()==itemSelAll){
			currentSrcTextComp.selectAll();
			return;
		}
		if(e.getSource()==itemUndo){
			undoMgs.get(currentSrcTextComp).undo();
			return;
		}
		if(e.getSource()==itemRedo){
			undoMgs.get(currentSrcTextComp).redo();
			return;
		}
		if(e.getSource()==itemCol){
			if(!colModel){
				currentSrcTextComp.addCaretListener(this);
				currentSrcTextComp.addMouseMotionListener(this);
				itemCol.setText("enable column mode");
			}
			else{
				currentSrcTextComp.removeCaretListener(this);
				currentSrcTextComp.removeMouseMotionListener(this);
				itemCol.setText("disable column mode");
			}
			colModel=!colModel;
			return;
		}
	}
	
	public void caretUpdate(CaretEvent e) 
	{
		try{
		//log(e.getMark()+" "+e.getDot());
		if(e.getMark()!=e.getDot()){
			Rectangle r=currentSrcTextComp.modelToView(e.getMark());
			log(r.x+","+r.y);
			
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void mouseDragged(MouseEvent e)
	{
		//log("dragged");
	}
 	public void mouseMoved(MouseEvent e) {}

	public static void printCompSize(JComponent c)
	{
		log("\ngetPreferredSize()="+c.getPreferredSize()+
			"\ngetMaximumSize()="+c.getMaximumSize()+
			"\ngetMinimumSize()="+c.getMinimumSize()+
			"\ngetSize()="+c.getSize()+
			"\ngetAlignmentX()="+c.getAlignmentX() );
		
	}
	protected void maybeShowPopup(MouseEvent e)
	{
		if (e.isPopupTrigger()) {
			currentSrcTextComp=(JTextComponent)e.getComponent();
			UndoManager undoMg=undoMgs.get(currentSrcTextComp);
			itemUndo.setEnabled(undoMg.canUndo());
			itemRedo.setEnabled(undoMg.canRedo());
			
			popMenu.show(e.getComponent(),e.getX(), e.getY());
		}
	}
	protected static void log(String s)
	{
		System.out.println(new Throwable().getStackTrace()[0].toString()+"-"+s);
	}
	protected class TheUndoableEditListener implements UndoableEditListener
	{
		UndoManager undoManager;
		public TheUndoableEditListener(UndoManager undoM)
		{
			undoManager=undoM;
		}
		public void undoableEditHappened(UndoableEditEvent e) 
		{
			undoManager.addEdit(e.getEdit());
		}
	}
}
