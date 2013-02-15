package org.liujing.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import javax.swing.text.*;

import org.liujing.awttools.TextFieldContextMenu;
/**
use getInstance(), debug()

@author liujing
*/
public class LogWindow extends JFrame
{
	protected JTextPane showText=new MyTextPane();
	protected JScrollBar showTextSclBar=null;
	protected static LogWindow _ui=null;
	//protected ScrollWindow scroller=new ScrollWindow();
	protected SimpleAttributeSet defaultAttr=new SimpleAttributeSet();
	protected SimpleAttributeSet debugAttr=new SimpleAttributeSet();
	protected SimpleAttributeSet errorAttr=new SimpleAttributeSet();
	protected SimpleAttributeSet infoAttr=new SimpleAttributeSet();
	//private SwingTask runner=new SwingTask();
	protected LogWindow()
	{
		super("Log");
		setBounds(500,100,600,500);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE );

		JPanel mainPanel=new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

		//showText.setPreferredSize(new Dimension(650,300));
		//showText=new JTextArea();
		JScrollPane scrShowText=new JScrollPane(showText);

		//showText.addComponentListener(new ResultTextListener());
		//showText.setFont(new Font("Verdana",Font.PLAIN,12));
		showTextSclBar=scrShowText.getVerticalScrollBar();
		scrShowText.setPreferredSize(new Dimension(250,300));
		new TextFieldContextMenu().setTo(showText);//add context menu
		getContentPane().add(mainPanel);
		mainPanel.add(scrShowText);
		addWindowListener(new DebugFrameWindowListener());

		StyleConstants.setBold(debugAttr,true);
		StyleConstants.setForeground(debugAttr,Color.BLUE);
		StyleConstants.setBold(errorAttr,true);
		StyleConstants.setForeground(errorAttr,Color.RED);
	}

	public synchronized static  LogWindow getInstance()
	{
		if(_ui==null){
			try{
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}catch(Exception e){e.printStackTrace();}
			//SwingUtilities.updateComponentTreeUI
			_ui=new LogWindow();
			_ui.setVisible(true);
			_ui.pack();
		}
		return _ui;
	}
	public void append(String text)
	{
		append(text,defaultAttr);
	}
	public void appendLine(String text)
	{
		appendLine(text,defaultAttr);
	}
	public void appendLine(String text,AttributeSet attr)
	{
		append(text,attr);
		append("\n",attr);
	}
	public void append(String text,AttributeSet attr)
	{
		//try{
			//System.out.println("feaewefew");
			SwingUtilities.invokeLater(new SwingTask(text,attr));
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run(){
						showTextSclBar.setValue(showTextSclBar.getMaximum());
					}
				});
			//SwingUtilities.invokeLater(scroller);
		//}catch(BadLocationException be){
		//	be.printStackTrace();
		//}
	}
	private class SwingTask implements Runnable
	{
		String text;
		AttributeSet attr;
		public SwingTask(String text,AttributeSet attr)
		{
			this.text=text;
			this.attr=attr;
		}
		public void run() {
			try{
				StyledDocument doc=showText.getStyledDocument();
				doc.insertString(doc.getLength(),text,attr);

			}catch(BadLocationException be){
			be.printStackTrace();
			}
		}
	}
	public void debug(String text)
	{

		append("D ",debugAttr);
		appendLine(text);
		showText.setCaretPosition(showText.getDocument().getLength());
	}
	public void debug(String text,Throwable thr)
	{
		synchronized(showText){
			append("D ",debugAttr);
			append(text);
			StringWriter sw=new StringWriter();
			thr.printStackTrace(new PrintWriter(sw));
			append(sw.toString());
		}
	}
	public void error(String text)
	{
		append("E ",errorAttr);
		appendLine(text);
		showText.setCaretPosition(showText.getDocument().getLength());
	}
	public void error(String text,Throwable thr)
	{
		synchronized(showText){
			append("E ",errorAttr);
			append(text);
			StringWriter sw=new StringWriter();
			thr.printStackTrace(new PrintWriter(sw));
			append(sw.toString());
		}
	}

	class DebugFrameWindowListener extends WindowAdapter
	{
		public void windowClosed(WindowEvent e)
		{
			_ui=null;
		}
	}

	class ResultTextListener extends ComponentAdapter
	{
		public void componentResized(ComponentEvent e)
		{
			showTextSclBar.setValue(showTextSclBar.getMaximum());
			//System.out.println(Thread.currentThread().getName()+" resized");
		}
	}

	public static class MyTextPane extends JTextPane
	{
		/**
   * overridden from JEditorPane   * to suppress line wraps   *   * @see setSize
   */
   	public boolean getScrollableTracksViewportWidth() {
		if (getParent() instanceof JViewport) {
			javax.swing.plaf.TextUI ui = getUI();

			return (((JViewport)getParent()).getWidth() > ui.getPreferredSize(this).width);
		}
		return false;
	}



	 /**
	   * overridden from JeditorPane
	   * to suppress line wraps
	*   * @see getScrollableTracksViewportWidth
	   */
	/*public void setSize(Dimension d) {
	    if(d.width < getParent().getSize().width)
		{
	d.width = getParent().getSize().width;
	    }
	    super.setSize(d);
	 }*/

	}
	public static class ScrollWindow implements Runnable
	{
		public void run()
		{
			System.out.println("scroll");
		}
	}
	public static void main(String args[])
	{

		LogWindow.getInstance().showTextSclBar.setValue(0);
		String s="-----------------------------------------------------------------------------\n";
		System.out.println("start");
		LogWindow.getInstance().debug(s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s+s);
	}
}
