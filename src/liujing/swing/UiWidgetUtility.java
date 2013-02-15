package liujing.swing;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.util.logging.*;
import java.awt.image.*;

/**
 UiWidgetUtility
 @author Break(Jing) Liu
*/
public class UiWidgetUtility{
    /** log */
    private static Logger log = Logger.getLogger(UiWidgetUtility.class.getName());

    public UiWidgetUtility(){
    }

    public static GraphicsConfiguration getGrapConfig(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        return gc;
    }

    public static BufferedImage createDefaultImage(int width, int height){
        return getGrapConfig().
        createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        //return new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR );
    }

    public static void clearGraph(Graphics g, int x, int y, int w, int h){
        Graphics2D cg = (Graphics2D)g.create();
        cg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f));
        cg.fillRect(x,y,w,h);
        cg.dispose();
    }

    public static void clearImage(BufferedImage image){
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f));
        g.fillRect(0,0,image.getWidth(), image.getHeight());
        g.dispose();
    }

    public static void clearImage(BufferedImage image, int x, int y, int w, int h){
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f));
        g.fillRect(x,y,w, h);
        g.dispose();
    }

    public static void setOpaque(Container container, boolean opaque){
        int count = container.getComponentCount();
        for(int i =0; i<count; i++){
            Component child = container.getComponent(i);
            if(child instanceof JComponent){
                JComponent comp = (JComponent)child;
                if(comp instanceof JPanel || comp instanceof JTable
                    || comp instanceof JScrollPane || comp instanceof JButton
                    || comp instanceof JCheckBox
                )
                {

                    if(comp instanceof JScrollPane){
                        comp.setOpaque(opaque);
                        if(!opaque){
                            JScrollPane scroll = (JScrollPane)comp;
                            Component view = scroll.getViewport().getView();
                            scroll.setViewport(new MyViewport());
                            scroll.setViewportView(view);
                        }
                        ((JScrollPane)comp).getViewport().setOpaque(opaque);
                        ((JScrollPane)comp).getHorizontalScrollBar().setOpaque(opaque);
                        ((JScrollPane)comp).getVerticalScrollBar().setOpaque(opaque);
                        //setOpaque(((JScrollPane)comp).getViewport(), opaque);
                    }else if(comp instanceof JPanel || comp instanceof JTabbedPane){
                        comp.setOpaque(opaque);
                        setOpaque(comp, opaque);
                    }else{
                        comp.setOpaque(opaque);
                    }
                }
            }
        }
    }

    public static void paintBackgroundImage(Graphics g, BufferedImage image,
	    boolean repeatX, boolean repeatY, boolean scaled, int width, int height)
	{
	    int x =0,y = 0;
	    int newWidth = image.getWidth(), newHeight = image.getHeight();
	    if(scaled && (width != image.getWidth() || height != image.getHeight())){
	        float whRatio = (float)image.getWidth()/ (float)image.getHeight();
	        float userWhRatio = (float)width/(float)height;
	        if(userWhRatio > whRatio){
	            // width > height, should stick with height
	            newHeight = height;
	            newWidth = (int)( (float)height * whRatio );
	        }else{
	            newWidth = width;
	            newHeight = (int)( (float)width / whRatio);
	        }
	    }

	    if(repeatX && repeatY){
	        int restW = width;
	        int restH = height;

	        while(restH > 0){
	            x = 0;
	            restW = width;
                while(restW > 0){
                    g.drawImage(image, x, y, newWidth, newHeight, null);
                    restW -= newWidth;
                    x += newWidth;
                }
                restH -= newHeight;
                y += newHeight;
            }
	    }else if(repeatX){
	        int restW = width;
	        while(restW > 0){
                g.drawImage(image, x, y, newWidth, newHeight, null);
                restW -= newWidth;
                x += newWidth;
            }
	    }else if(repeatY){
	        int restH = height;
	        while(restH > 0){
                g.drawImage(image, x, y, newWidth, newHeight, null);
                restH -= newHeight;
                y += newHeight;
            }
	    }
	}

	public static ImageIcon createLoadingIcon(int width, int height){
	    BufferedImage img = getGrapConfig().createCompatibleImage(width, height);
	    return new ImageIcon(img);
	}

}

