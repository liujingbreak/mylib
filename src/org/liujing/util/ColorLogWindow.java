package org.liujing.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;

import org.liujing.awttools.TextFieldContextMenu;

public class ColorLogWindow extends LogWindow
{
	protected JTextPane showText=new JTextPane();
	
	protected static ColorLogWindow _ui=null;
	protected SimpleAttributeSet defaultAttr=new SimpleAttributeSet();
	
	protected ColorLogWindow()
	{
		//StyleConstants.
		setBounds(500,100,600,500);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE );

		JPanel mainPanel=new JPanel();		
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));


		//showText=new JTextArea();
		JScrollPane scrShowText=new JScrollPane(showText);

		showText.addComponentListener(new ResultTextListener());
		//showText.setFont(new Font("Verdana",Font.PLAIN,12));
		showTextSclBar=scrShowText.getVerticalScrollBar();
		scrShowText.setPreferredSize(new Dimension(250,300));
		new TextFieldContextMenu().setTo(showText);//add context menu
		getContentPane().add(mainPanel);
		mainPanel.add(scrShowText);
		addWindowListener(new DebugFrameWindowListener());
	}
	public synchronized static  ColorLogWindow getInstance()
	{
		if(_ui==null){
			try{
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}catch(Exception e){e.printStackTrace();}
			//SwingUtilities.updateComponentTreeUI
			_ui=new ColorLogWindow();
			_ui.setVisible(true);
			_ui.pack();
		}
		return _ui;
	}
	public void append(String text)
	{
		try{
			System.out.println("feaewefew");
			StyledDocument doc=showText.getStyledDocument();
			doc.insertString(doc.getLength(),text,defaultAttr);
			showText.setCaretPosition(doc.getLength());
			showText.insertComponent(new JButton("ok"));
			//showText.append(text);
			//showText.setCaretPosition(showText.getDocument().getLength());
		}catch(BadLocationException be){
			be.printStackTrace();
		}
	}
	public static void main(String args[])
	{
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
		ColorLogWindow.getInstance().append("test\n");
		ColorLogWindow.getInstance().append("test2\n");
	}
}
