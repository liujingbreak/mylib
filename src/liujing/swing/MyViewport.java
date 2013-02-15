package liujing.swing;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.applet.Applet;
import javax.swing.plaf.ViewportUI;
import javax.swing.*;

import javax.swing.event.*;
import javax.swing.border.*;
import javax.accessibility.*;
import liujing.swing.*;
/**
 MyViewport
 This component has a improved performance for transparent scroll panel,
 It uses 2 BufferedImage instances to cache view.
 @author Break(Jing) Liu
*/
public class MyViewport extends JViewport{
    /** log */
    private static Logger log = Logger.getLogger(MyViewport.class.getName());

    private BufferedImage buffImg;
    private BufferedImage buffImg2;
    //private Point viewPosition = new Point();
    private boolean scrolling = false;
    private Point scrolledPosition = new Point();

    public MyViewport(){
    }


    public void paint(Graphics g)
    {

        int width = getWidth();
        int height = getHeight();

        if ((width <= 0) || (height <= 0)) {
            return;
        }
        Dimension viewSize = getView().getSize();
        if( viewSize.width <= 0 || viewSize.height <= 0)
            return;
        //log.info("\n--------\nclip="+g.getClipBounds() + " scrolling="+ scrolling);
        Point viewloc = getViewPosition();
        //log.info("ViewPosition="+ viewloc);
        Rectangle clip = g.getClipBounds();

        boolean newly = newBuffImg();
        if(!scrolling || newly || !scrolledPosition.equals(viewloc)){
            //if(!scrolledPosition.equals(viewloc)){
            //    log.info("scrolledPosition="+scrolledPosition + ", viewloc="+ viewloc);
            //}
            UiWidgetUtility.clearImage(buffImg, clip.x, clip.y, clip.width, clip.height);
            Graphics buffG = buffImg.createGraphics();
            buffG.setClip(clip.x, clip.y, clip.width, clip.height);
            buffG.translate(-viewloc.x, -viewloc.y);
            //log.info("buff clip="+ buffG.getClipBounds()+ " newly="+ newly);
            getView().paint(buffG);
            buffG.dispose();
        }
        scrolling = false;
        g.drawImage(buffImg, 0, 0, this);
    }


    /**  prepareBuffImg
     @return true if newly created
    */
    protected boolean newBuffImg(){
        Dimension viewSize = getView().getSize();
        int w = viewSize.width < getWidth()? viewSize.width:getWidth();
        int h = viewSize.height < getHeight()? viewSize.height:getHeight();

        if(buffImg == null || w > buffImg.getWidth() || h > buffImg.getHeight())
        {
            log.fine(" -- "+ w + ","+h);
            buffImg = UiWidgetUtility.createDefaultImage(w, h);
            return true;
        }else{
            //UiWidgetUtility.clearImage(buffImg);
            return false;
        }
    }

    protected boolean newBuffImg2(){
        Dimension viewSize = getView().getSize();
        int w = viewSize.width < getWidth()? viewSize.width:getWidth();
        int h = viewSize.height < getHeight()? viewSize.height:getHeight();
        if(buffImg2 == null || w > buffImg2.getWidth() || h > buffImg2.getHeight())
        {
            log.info(" -- "+ w + ","+ h);
            buffImg2 = UiWidgetUtility.createDefaultImage(w,h);
            return true;
        }else{
            //UiWidgetUtility.clearImage(buffImg2);
            return false;
        }
    }


    public void setViewPosition(Point p){

        Component view = getView();
        if (view == null) {
            return;
        }
        Point viewPosition = getViewPosition();
        int newX = p.x;
        int newY = p.y;
        if ((viewPosition.x != newX) || (viewPosition.y != newY)) {
            //log.fine("set view pos oldX="+viewPosition.x + " newX="+newX+
            //    ", oldY="+viewPosition.y + " newY="+ newY);
            boolean paintAll = scroll(viewPosition, p);
            view.setLocation(-newX, -newY);
            if(!paintAll){
                RepaintManager rm = RepaintManager.currentManager(this);
                rm.paintDirtyRegions();
                //log.info("----- repaintmanagar paint: " + Thread.currentThread().getName());
                //rm.markCompletelyClean((JComponent)getView());
                //rm.markCompletelyClean(this);
                //rm.markCompletelyClean((JComponent)getParent());
            }
            fireStateChanged();
        }
    }

    protected boolean scroll(Point oldPosition, Point newPosition)
    {
        Dimension size = getExtentSize();
        if(newBuffImg2())
            return true;
        RepaintManager rm = RepaintManager.currentManager(this);
        JComponent jview = (JComponent)getView();
        Rectangle dirty = rm.getDirtyRegion(jview);
        //log.info("!!! dirty = "+ dirty + " "+ jview.getVisibleRect());
        if (dirty != null && dirty.width > 0 && dirty.height>0){
            log.fine(" paint all !!!");
            return true;
        }
        //log.info("newPosition="+ newPosition);
        UiWidgetUtility.clearImage(buffImg2);
        Graphics buffG = buffImg2.createGraphics();
        buffG.translate(-newPosition.x, -newPosition.y);
        Rectangle oldClip = new Rectangle(oldPosition.x, oldPosition.y, size.width, size.height);
        Rectangle newClip = new Rectangle(newPosition.x, newPosition.y, size.width, size.height);
        Rectangle overlap = newClip.intersection(oldClip);
        if(overlap != null && overlap.width >0 && overlap.height >0){
            buffG.setClip(overlap.x, overlap.y, overlap.width, overlap.height);
            buffG.drawImage(buffImg, oldPosition.x, oldPosition.y, this);
            Rectangle[] dirtyRects = SwingUtilities.computeDifference(newClip, oldClip);
            for(Rectangle rect : dirtyRects){
                //log.info("load dirty to buff "+ rect);
                buffG.setClip(rect.x, rect.y, rect.width, rect.height);
                getView().paint(buffG);
            }
        }else{
            buffG.setClip(newClip.x, newClip.y, newClip.width, newClip.height);
            getView().paint(buffG);
        }

        scrolledPosition.x = newPosition.x;
        scrolledPosition.y = newPosition.y;
        scrolling = true;
        BufferedImage tmp = buffImg;
        buffImg = buffImg2;
        buffImg2 = tmp;
        buffG.dispose();
        //return buffG;
        return false;
    }

    public void scrollRectToVisible(Rectangle contentRect) {
        super.scrollRectToVisible(contentRect);
    }

    private Component getRepaintRoot(){
        Component comp = this;
        while(!comp.isOpaque()){
            comp = comp.getParent();
        }
        return comp;
    }
/*
    public Point getViewPosition() {
        return new Point(viewPosition);
    }
*/
}

