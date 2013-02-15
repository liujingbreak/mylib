package liujing.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import java.awt.image.*;

public class FitWidthTable extends JTable
{
	static Logger log = Logger.getLogger(FitWidthTable.class.getName());

	protected boolean deferredResize = true;
	/** paint duration */
	public long paintDur = 0;
	public long paintDur2 = 0;
	public long cellPaintCount ;

	protected BufferedImage offBuffer;
	protected BufferedImage offBuffer2;
	protected Rectangle lastClip;
	protected boolean dirtyPaint = false;
	protected boolean offScreenEnabled = false;
	protected static Color clearCol = new Color(0xff,0xff,0xff,0xff);

	public FitWidthTable()
	{
		super();
		init();
	}
	public FitWidthTable(TableModel dm)
	{
		super(dm);
		init();
	}

	public void setDeferredResize(boolean defer){
	    deferredResize = defer;
	}

	public void expandCellSize(int row, int column, Dimension preferredSize){
	    int rowHeight = getRowHeight(row);
	    if(rowHeight < preferredSize.height)
	        setRowHeight(row, preferredSize.height);
	    TableColumn col = getColumnModel().getColumn(column);
	    int colWidth = col.getPreferredWidth();
	    if(colWidth < preferredSize.width)
	        col.setPreferredWidth(preferredSize.width);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
	    Component comp = super.prepareRenderer(renderer, row, column);
	    if(deferredResize)
	        expandCellSize(row, column, comp.getPreferredSize());
	    return comp;
	}

	/**
	call this method, when all the table rows are removed, so that table can be
	resized by column headers
	*/
	public void packTableByHeaderSize(){
	    resizeTable(TableModelEvent.HEADER_ROW, TableModelEvent.HEADER_ROW,
	        TableModelEvent.ALL_COLUMNS);
	}

	/**
	useless
	*/
	public void setOffscreenEnabled(boolean b){
	    offScreenEnabled = b;
	}
	/**
	overwrite it
	*/
	protected void init()
	{
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//setDefaultRenderer(Object.class, new FitWidthTableRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
		setRowHeight(20);
		//setPreferredScrollableViewportSize(new Dimension(1000,600));

		//log.fine("before init size");
		resizeTable(0, Integer.MAX_VALUE, TableModelEvent.ALL_COLUMNS);
		//log.fine("after init size");
	}

	public int getPreferredRowHeight(int firstColumn, int lastColumn, int rowIndex) {
	    // Get the current default height for all rows
	    int height = getRowHeight();

	    // Determine highest cell in the row
	    for (int c = firstColumn; c <= lastColumn; c++) {
	        TableCellRenderer renderer = getCellRenderer(rowIndex, c);
	        Component comp = prepareRenderer(renderer, rowIndex, c);
	        int h = comp.getPreferredSize().height + (getRowMargin()>>1);
	        height = Math.max(height, h);
	        if(isCellEditable(rowIndex, c)){
	        	TableCellEditor editor = getCellEditor(rowIndex, c);
	        	comp = prepareEditor(editor, rowIndex, c);
	        	height = Math.max(height, comp.getPreferredSize().height);
	        }
	    }
	    return height;
	}


	public int getPreferredColumnWidth(int firstRow, int lastRow, int colomnIdx)
	{
		int w = getColumnModel().getColumn(colomnIdx).getPreferredWidth();
		//log.fine("get preferred width: " + w);
		Component hcomp = null;
		TableCellRenderer renderer = null;
		if(firstRow == -1 && getTableHeader() != null){
			TableCellRenderer tHrenderer=getTableHeader().getDefaultRenderer();
			//log.fine(""+ getColumnModel().getColumn(colomnIdx).getHeaderValue());
			hcomp = tHrenderer.getTableCellRendererComponent(this, getColumnModel().getColumn(colomnIdx).getHeaderValue()  , false, false, 0, colomnIdx);
			//log.fine("header "+ colomnIdx +" width: " + hcomp.getPreferredSize());
			//w = Math.max(w, hcomp.getPreferredSize().width);
			//default column PreferredWidth is bigger than header column
			//we set the initial width equal to header column width
			w = hcomp.getPreferredSize().width;
		}
		for(int r = firstRow; r <= lastRow; r++)
		{
			if(r == -1){
				continue;
			}
			renderer = getCellRenderer(r, colomnIdx);
			Component comp = prepareRenderer(renderer, r, colomnIdx);
			w = Math.max(w, comp.getPreferredSize().width + 4);
			if(isCellEditable(r, colomnIdx)){
	        	TableCellEditor editor = getCellEditor(r, colomnIdx);
	        	comp = prepareEditor(editor, r, colomnIdx);
	        	w = Math.max(w, comp.getPreferredSize().width + 4);
	        }

		}
		//log.fine("set preferred width: " + w);
		return w;
	}

	@Override
	public void setDefaultEditor(Class<?> columnClass, TableCellEditor editor){
	    super.setDefaultEditor(columnClass, editor);
	    resizeTable(0, Integer.MAX_VALUE, TableModelEvent.ALL_COLUMNS);
	}
	/**
	you can override it, to update table layout like execute packTable() or others.
	*/

	public  void tableChanged(TableModelEvent e)
	{
	    dirtyPaint = true;
		try{
			super.tableChanged(e);
		}catch(RuntimeException ex){
			log.log(Level.INFO, "", ex);
			//throw e;
		}
		if(!deferredResize){
			if( e.getColumn()>= 0 && ( e.getType()==e.INSERT ||e.getType()==e.UPDATE))
			{
			    log.fine("tableChanged " + e.getFirstRow() + "->"
                + e.getLastRow() + " type:" + e.getType()+ " count:"
                + getRowCount());
				resizeTable(e.getFirstRow(), e.getLastRow(), e.getColumn());
			}
		}
	}

	public void paint(Graphics g){
	    long now = System.nanoTime();
	    paintDur = 0;
	    paintDur2 = 0;
	    cellPaintCount = 0;
	    if(!offScreenEnabled){
	        super.paint(g);

	    }else{


	    Rectangle clipR = g.getClipBounds();
	    if(offBuffer == null || dirtyPaint || lastClip == null){
	        log.info(" \n-----------------\npaint all ="+ dirtyPaint);
            paintAll(g, clipR);
            lastClip = clipR;
        }else if(lastClip.x != clipR.x || lastClip.y!= clipR.y){
            if(clipR != null && !clipR.intersects(lastClip)){
                log.info(" \n-----------------\nno intersects =");
                paintAll(g, clipR);
            }else{
                log.info(" \n-----------------\nintersects ");
                Rectangle rBottom = new Rectangle();
                int bottom0 = lastClip.y + lastClip.height;
                int bottom1 = clipR.y + clipR.height;
                if(bottom0 < bottom1 ){
                    rBottom.y = bottom0;
                    rBottom.height = bottom1 - bottom0;
                    rBottom.x = Math.min(lastClip.x, clipR.x);
                    rBottom.width = clipR.width;
                }
                Rectangle rTop = new Rectangle();
                if(lastClip.y > clipR.y){
                    rTop.y = clipR.y;
                    rTop.height = lastClip.y - clipR.y;
                    rTop.x = Math.min(lastClip.x, clipR.x);
                    rTop.width = clipR.width;
                }
                Graphics offg = offBuffer.createGraphics();
                if(offBuffer2 == null){
                    offBuffer2 = UiWidgetUtility.createDefaultImage(clipR.width, clipR.height);
                }else{
                    UiWidgetUtility.clearImage(offBuffer2);
                }
                Graphics g2 = offBuffer2.createGraphics();
                Graphics fakeG = g2.create(-clipR.x, -clipR.y, getWidth(), getHeight());
                fakeG.drawImage(offBuffer, lastClip.x, lastClip.y, this);

                //Rectangle copyArea = clipR.intersection(lastClip);
                if(rBottom.height >0){
                    log.info("scroll down");
                    fakeG.setClip(rBottom.x, rBottom.y, rBottom.width, rBottom.height);
                    super.paint(fakeG);
                }
                if(rTop.height >0){
                    log.info("scroll up " + rTop);
                    fakeG.setClip(rTop.x, rTop.y, rTop.width, rTop.height);
                    super.paint(fakeG);
                }
                BufferedImage tmp = offBuffer;
                offBuffer = offBuffer2;
                offBuffer2 = tmp;

                g.drawImage(offBuffer,clipR.x,clipR.y, this);
            }
            lastClip = clipR;
        }
        else{
            g.drawImage(offBuffer,0,0, this);
        }
        }

	    long now0 = System.nanoTime() - now;

	    //log.info("t : " + (now0>>10) +
	    //    ", duration time: " + (paintDur>>10) + ", other=" + ((now0 - paintDur)>>10) +
	    //    "\n cell validate time="+ (paintDur2>>10) + ", other cell time=" + ((paintDur - paintDur2)>>10)
	    //    + "\n cell count="+ cellPaintCount+
	    //    " clip bounds=" + g.getClipBounds()
	    //    + " w=" + getWidth() + " h="+ getHeight()
	    //   );
	}

	//public void repaint(long tm, int x, int y, int width, int height) {
    //    //log.log(Level.INFO, "", new Throwable());
    //    super.repaint(tm, x, y, width, height);
    //
    //}


	protected  void 	resizeAndRepaint(){
	    dirtyPaint = true;
	    super.resizeAndRepaint();
	}

	protected void paintAll(Graphics g, Rectangle clipR){
	    Graphics offG = getOffscreen(clipR.x, clipR.y, clipR.width, clipR.height);
        super.paint(offG);
        g.drawImage(offBuffer,clipR.x,clipR.y, this);
        dirtyPaint = false;
        offG.dispose();
    }


	protected Graphics getOffscreen(int x, int y, int width, int height){
	    //log.info("paintOffscreen");
	    int newW = Math.min(getWidth(), width);
	    int newH = Math.min(getHeight(), height);
	    Graphics2D g = null;
	    if(offBuffer == null || offBuffer.getWidth() < newW || offBuffer.getHeight() < newH){
	        //offBuffer = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB_PRE);
	        offBuffer = UiWidgetUtility.createDefaultImage(newW, newH);
	        g = offBuffer.createGraphics();
	    }else{
	        g = offBuffer.createGraphics();
	        UiWidgetUtility.clearGraph(g, 0, 0 , getWidth(), getHeight());
	    }
        g.setClip(0, 0, width, height);
        return g.create(-x, -y, getWidth(), getHeight());
	}




	protected void resizeTable(int firstRow, int lastRow, final int column)
	{

		if(log.isLoggable(Level.FINE))
			log.fine(" row:"+ firstRow + " ~ " + lastRow + " column:" + column + " count: " + getRowCount());
		int rowCount = getRowCount() - 1;
		if(rowCount < lastRow)
			lastRow = rowCount;
		if(firstRow == TableModelEvent.HEADER_ROW){
			lastRow = getRowCount() - 1;
		}
		int firstColumn = column;
		int lastColumn = column;
		if(column == TableModelEvent.ALL_COLUMNS){
			firstColumn = 0;
			lastColumn = getColumnCount() - 1;
			if(lastColumn < 0){
				log.fine("no column");
				return;
			}
		}

		for(int i = firstColumn; i <= lastColumn; i++){
			int width = getPreferredColumnWidth(firstRow, lastRow, i);
			getColumnModel().getColumn(i).setPreferredWidth(width);
			//log.fine("set column "+ i + " width="+ width);
		}
		for (int r = firstRow; r <= lastRow; r++) {
			if(r == -1 )
				continue;
	        int h = getPreferredRowHeight(firstColumn, lastColumn, r );
	        if (getRowHeight(r) != h) {
	            setRowHeight(r, h);
	            //log.fine("set row height "+r+:" "+h);
	        }
	    }

	  //Below section is for setting last column's width to fill the blank area
	  //when the table is in a scrollPane whose viewport width is bigger
	  /*
	  SwingUtilities.invokeLater(
	      new Runnable(){
	          public void run(){
	              Component p = FitWidthTable.this.getParent();
                TableColumnModel columnModel = FitWidthTable.this.getColumnModel();

                if(p instanceof JViewport &&
                    (column == columnModel.getColumnCount()-1 || column == TableModelEvent.ALL_COLUMNS)){
                    Dimension portSize = ((JViewport)p).getExtentSize();
                    int deltaWidth = portSize.width - columnModel.getTotalColumnWidth();
                    log.info(" column " + FitWidthTable.this.getClass().getName()+ " " + FitWidthTable.this.hashCode()
                        + " portWidth=" + portSize.width + ", totalColumnWidth=" + columnModel.getTotalColumnWidth());

                    if(deltaWidth > 0){
                        log.info("adjust column width to fit viewport for " + deltaWidth + " "+ FitWidthTable.this);
                        TableColumn c = columnModel.getColumn(columnModel.getColumnCount()-1);
                        c.setPreferredWidth(c.getWidth() + deltaWidth);
                    }
                }
	          }
	      });
	      */
	}
}
