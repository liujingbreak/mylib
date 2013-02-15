package liujing.swing;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.geom.*;

/**
 MyPanel
 @author Break(Jing) Liu
*/
public class MyPanel extends JPanel{
    private Color transBackColor;
    /** log */
    private static Logger log = Logger.getLogger(MyPanel.class.getName());
    private BufferedImage bgImage;
    private BufferedImage bgBuffer;
    private VolatileImageRenderer offscreen;
    private Color bgMaskCol;
    //private RenderCommand offscreenRenderer;
    private Dimension oldSize = new Dimension(0, 0);
    private boolean bgRepeatX = false;
    private boolean bgRepeatY = false;
    private boolean scaleToFit = false;
    private boolean enableMask = false;
    private RoundRectangle2D.Float maskRect;

    public MyPanel(){
        //offscreenRenderer = this;
        //offscreen = new VolatileImageRenderer(this, offscreenRenderer);

    }

    public void setBackground(Color col){
        setOpaque(false);
        transBackColor = col;
    }

    public void setBgImgMask(Color col){
        bgMaskCol = col;
        if(bgImage != null)
            paintOffscreen();
    }

    /**  setBackgroundImg
     @param input input
     @param repeatX repeatX
     @param repeatY repeatY
     @param keepRatioByWidth keepRatioByWidth
     @param keepRatioByHeight keepRatioByHeight
     @throws IOException if IOException occurs
    */
    public void setBackgroundImg(InputStream input, boolean repeatX,
        boolean repeatY, boolean scaleToFit)
    throws IOException
    {
         setBackgroundImg(ImageIO.read(input), repeatX, repeatY, scaleToFit);
    }

    /**  setBackgroundImg
     @param path path
     @param repeatX repeatX
     @param repeatY repeatY
     @param keepRatioByWidth keepRatioByWidth
     @param keepRatioByHeight keepRatioByHeight
     @throws IOException if IOException occurs
    */
    public void setBackgroundImg(String path, boolean repeatX,
        boolean repeatY, boolean scaleToFit)
    throws IOException
    {
        InputStream input = MyPanel.class.getClassLoader().getResourceAsStream(path);
        setBackgroundImg(input, repeatX, repeatY, scaleToFit);
    }

    public void setBackgroundImg(BufferedImage image, boolean repeatX,
        boolean repeatY, boolean scaleToFit)
    {
        boolean repaintOffscr = bgImage != null;
         bgImage = image;
         if(bgImage == null){
             log.warning("null image background");
         }
         bgRepeatX = repeatX;
         bgRepeatY = repeatY;
         this.scaleToFit = scaleToFit;
         if(repaintOffscr)
             paintOffscreen();
    }

    public void setMaskEnabled(boolean enabled, float arcw, float arch){
        enableMask = enabled;
        if(enableMask){
            if(maskRect == null)
                maskRect = new RoundRectangle2D.Float(0f,0f,
                    (float)getWidth(), (float)getHeight(), arcw, arch);
            else{
                maskRect.arcwidth  = arcw;
                maskRect.archeight = arch;
            }
        }
    }

    public void paint(Graphics g){
        if(maskRect != null){
            maskRect.width = (float)getWidth();
            maskRect.height = (float)getHeight();
            Graphics2D g2d = (Graphics2D)g;
            g2d.clip(maskRect);
        }
        super.paint(g);
    }

    @Override
	protected void paintComponent(Graphics oldG){
	    if(transBackColor != null){
            Graphics g = oldG.create();
            g.setColor(transBackColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
	    }
	    if(bgImage != null){
            if(oldSize.width != getWidth() || oldSize.height != getHeight()){
                paintOffscreen();
                oldSize.width = getWidth();
                oldSize.height = getHeight();
                //log.info("resize "+getWidth() + ", " + getHeight());
            }
            oldG.drawImage(bgBuffer,0,0, this);
        }
	    super.paintComponent(oldG);
	}

	protected void paintOffscreen(){
	    if(getWidth() <= 0 || getHeight() <= 0)
	        return;
	    //log.info("paintOffscreen");
        bgBuffer = new BufferedImage(getWidth(), getHeight(), bgImage.getType());
        Graphics2D g = bgBuffer.createGraphics();
        UiWidgetUtility.paintBackgroundImage(g, bgImage, bgRepeatX, bgRepeatY,
	        scaleToFit, getSize().width, getSize().height);
	    if(bgMaskCol != null){
            g.setColor(bgMaskCol);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
	    g.dispose();
	}

}

