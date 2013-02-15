package org.liujing.awttools;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class TestPanel extends JPanel{
	public TestPanel(){
		setLayout(new FlowLayout());
		
		add(new JButton(" OK"));
		JLabel label = new JLabel("Test label");
		//label.setMinimumSize(new Dimension(100,50));
		//label.setPreferredSize(new Dimension(100,50));
		label.setBorder(new ShadowBorder(label));
		add(label);
		
	}
}
