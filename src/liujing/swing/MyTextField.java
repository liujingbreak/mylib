package liujing.swing;

import javax.swing.*;
import org.liujing.awttools.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.logging.*;

public class MyTextField extends JTextField
{
    /** log */
    private static Logger log = Logger.getLogger(MyTextField.class.getName());
	private static TextFieldContextMenu contexMenu=new TextFieldContextMenu();
	public MyTextField()
	{
		contexMenu.addBoundComponent(this);
	}
	public MyTextField(String text)
	{
		super(text);
		contexMenu.addBoundComponent(this);
	}
	public MyTextField(Document doc,
                  String text,
                  int columns)
	{
		super(doc,text,columns);
		contexMenu.addBoundComponent(this);
	}

	public MyTextField(String text,
                  int columns)
	{
		super(text,columns);
		contexMenu.addBoundComponent(this);
	}

	public MyTextField(int columns)
	{
		super(columns);
		contexMenu.addBoundComponent(this);
	}

	@Override
	protected void paintComponent(Graphics g){
	    super.paintComponent(g);
	}
}
