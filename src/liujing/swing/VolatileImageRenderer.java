package liujing.swing;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
/**
 VolatileImageRenderer
 usage:<br>
 Create 1 instance and bind it to a component's life cycle.
 Call repaintOffscreen() to rerender offscreen image at any time,
 call copyToScreen() in Component's paintComponent() method.
 Objective:<BR>
 repaintOffscreen() is heavy operation, should be called only when the paint content is changed.
 copyToScreen() is very fast, can be called frequently.
 @author Break(Jing) Liu
*/
public class VolatileImageRenderer{
    /** log */
    private static Logger log = Logger.getLogger(VolatileImageRenderer.class.getName());
    private VolatileImage vImg;
    private int width;
    private int height;
    private JComponent comp;
    private RenderCommand renderer;

    public VolatileImageRenderer(JComponent comp, RenderCommand renderer){
        this.comp = comp;
        this.renderer = renderer;
    }

    public VolatileImageRenderer(JComponent comp, RenderCommand renderer,
        int width, int height){
        this.width = width;
        this.height = height;
        this.comp = comp;
        this.renderer = renderer;
    }

    public void resize(int width, int height){
        this.width = width;
        this.height = height;
        if(vImg != null){
            vImg.flush();
            vImg = null;
        }
    }

    public void repaintOffscreen() {
        if(vImg == null)
            vImg = comp.createVolatileImage(width, height);
        do {
            if (vImg.validate(comp.getGraphicsConfiguration()) ==
                VolatileImage.IMAGE_INCOMPATIBLE)
            {
                vImg.flush();
                // old vImg doesn't work with new GraphicsConfig; re-create it
                vImg = comp.createVolatileImage( width, height);
            }
            Graphics2D g = vImg.createGraphics();
            //
            // miscellaneous rendering commands...
            //
            log.fine("re-render");
            renderer.render(g);
            g.dispose();
        } while (vImg.contentsLost());
    }

    public void copyToScreen(Graphics g, int x, int y)
    {
        if(vImg == null){
            repaintOffscreen();
        }
        do {
            int returnCode = vImg.validate(comp.getGraphicsConfiguration());
            if (returnCode == VolatileImage.IMAGE_RESTORED) {
                // Contents need to be restored
                repaintOffscreen();      // restore contents
            } else if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                vImg.flush();
                // old vImg doesn't work with new GraphicsConfig; re-create it
                vImg = comp.createVolatileImage(width, height);
                repaintOffscreen();
            }
            g.drawImage(vImg, x, y, comp);
        }while (vImg.contentsLost());
    }


}

