package org.liujing.awttools;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.image.*;
import java.awt.*;
import java.util.logging.*;

public class ShadowBorder implements Border{
	private static Logger log = Logger.getLogger(ShadowBorder.class.getName());
	private BufferedImage backgroundImg;
	private Insets insets;
	private static BufferedImage[] images;
	private static String PATH = "org/liujing/awttools/image/border01/";
	
	public ShadowBorder(JComponent c){
		insets = new Insets(8,8,14,14);
		int miniBordHeight = createImages();
		if(c.isMinimumSizeSet()){
			int minHeight=c.getMinimumSize().height;
			if(minHeight< miniBordHeight){
				minHeight=miniBordHeight;
				c.setMinimumSize(new Dimension(c.getMinimumSize().width, minHeight));
			}			
		}else{
			c.setMinimumSize(new Dimension(Integer.MAX_VALUE, miniBordHeight));
		}
	}
	
	protected static synchronized int createImages(){
		try{
			images = new BufferedImage[8];
			images[0]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderTopLeft.png"));
			images[1]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderTop.png"));
			images[2]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderTopRight.png"));
			images[3]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderLeft.png"));
			images[4]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderRight.png"));
			images[5]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderBottomLeft.png"));
			images[6]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderBottom.png"));
			images[7]=javax.imageio.ImageIO.read(
				ShadowBorder.class.getClassLoader().getResourceAsStream(
					PATH+"BorderBottomRight.png"));
			int miniBordHeight=images[0].getHeight()+ images[5].getHeight()+40;
			return miniBordHeight;
			
		}catch(java.io.IOException ioex){
			log.log(Level.SEVERE,"failed to load border image",ioex);
			return 0;
		}
	}
	
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height){
		g.drawImage(images[0], x, y, null);
		//log.info("01");
		fillBgImgByRow(x+images[0].getWidth(),y ,width-images[0].getWidth()-images[2].getWidth(),images[1],g);
		//log.info("02");
		g.drawImage(images[2], x+width-images[2].getWidth(), y, null);
		fillBgImgByColumn(x, y+images[0].getHeight(), height-images[0].getHeight()-images[5].getHeight(), images[3], g);
		//log.info("03");
		fillBgImgByColumn(x+width-images[4].getWidth() , y+images[2].getHeight(),
			height-images[2].getHeight()-images[7].getHeight(),images[4], g);
		
		
		g.drawImage(images[5], x, height-images[5].getHeight()+y, null);
		fillBgImgByRow(x+images[5].getWidth(),y+height-images[6].getHeight() ,width-images[5].getWidth()-images[7].getWidth(),images[6],g);
		g.drawImage(images[7], x+width-images[7].getWidth(), height-images[7].getHeight()+y, null);
	}
	
	private static void fillBgImgByColumn(int x, int y, int height, BufferedImage image, Graphics g){
		int startY=0;
		do{
			g.drawImage(image, x, y + startY, null);
			startY += image.getHeight();
			
		}while(startY < height);			
	}
	private static void fillBgImgByRow(int x, int y, int width, BufferedImage image, Graphics g){
		int startX=0;
		do{
			g.drawImage(image, x + startX, y, null);
			startX += image.getWidth();			
		}while(startX < width);			
	}
	
	public Insets getBorderInsets(Component c){
		return new Insets(8,8,14,14);
	}
	
	public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = 8;
        insets.top = 8;
        insets.right = 14;
        insets.bottom = 14;
        return insets;
    }
    
	public boolean isBorderOpaque(){
		return true;
	}
	
}
