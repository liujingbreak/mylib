package liujing.util;

import java.util.*;
import java.util.logging.*;
import java.awt.image.*;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.*;
import java.awt.Color;
import liujing.util.dirscan.*;

/**
Image file consolidation function, it consolidate multiple images into single image
file with certain layout options, like horizontal or vertical ...
*/
public class ImageConsolidation{
    private static Logger log = Logger.getLogger(ImageConsolidation.class.getName());

    private boolean horizontal = false;

    private String _format;
    private File _outFile;
    private File _rootDir = new File("."); // root of source dir
    private List<String> _imageFilesPath;

    private int width;
    private int height;
    private int borderWidth = 0;
    private File cssFile;
    private PrintWriter cssWriter;
    /**
    @param horizontal false for vertical creation
    */
    public ImageConsolidation(boolean horizontal){
        this.horizontal = horizontal;
    }

    public ImageConsolidation(){
    }

    /**
    The main method for consolidation function
    */
    public void create(List<File> files, File outFile, String format)
    throws IOException{
        caculateSize(files);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if(format.equalsIgnoreCase("png"))
            type = BufferedImage.TYPE_4BYTE_ABGR;
        _create(files, out, format, type);
        out.close();
        if(cssWriter != null)
            cssWriter.close();
    }

    protected void caculateSize(List<File> files)throws IOException{
        for(File file: files){
            Dimension dim = ImageDim.getImageDimension(file);
            int bw = borderWidth << 1;
            if(horizontal){
                width += dim.width + bw;
                height = height > (dim.height + bw)? height: (dim.height + bw);
            }else{
                width = width > (dim.width + bw)? width: (dim.width + bw);
                height += dim.height + bw;
            }
        }
        log.info("size: " + width + ", " + height);
    }

    /**
    @param bufferedImgType BufferedImage.TYPE_3BYTE_BGR or BufferedImage.TYPE_4BYTE_ABGR
    */
    protected void _create(List<File> files, OutputStream imageOut, String format,
        int bufferedImgType)throws IOException
    {
        BufferedImage bundleImage = new BufferedImage(width, height, bufferedImgType);
		Graphics2D g = bundleImage.createGraphics();
		int dw = borderWidth << 1;
		writeCssCommon();
		if(!horizontal){
			int y = 0;
			for(File f : files){
				BufferedImage image = javax.imageio.ImageIO.read(f);
				drawBorder(g, 0, y, width, image.getHeight() + dw);
				g.drawImage(image, borderWidth, y + borderWidth, null);
				writeCss(f.getName(), 0, y, image.getWidth(), image.getHeight());
				y += image.getHeight() + (borderWidth << 1);
			}
		}else{
			int x = 0;
			for(File f : files){
				BufferedImage image = javax.imageio.ImageIO.read(f);
				drawBorder(g, x, 0, image.getWidth() + dw, height);
				g.drawImage(image, x + borderWidth, 0, null);
				writeCss(f.getName(), x, 0, image.getWidth(), image.getHeight());
				x += image.getWidth() + (borderWidth << 1);
			}
		}
		g.dispose();
		javax.imageio.ImageIO.write(bundleImage, format, imageOut);
	}

	protected void drawBorder(Graphics2D g,int x, int y, int width, int height){

	    g.setColor(Color.BLACK);
	    int dw = borderWidth << 1;
	    for(int i = 0; i < borderWidth; i++){
	        int delt = (i << 1);
	        if( i == borderWidth - 8)
	            g.setColor(Color.WHITE);
	        else if(i == borderWidth - 5)
	            g.setColor(Color.BLACK);
	        g.drawRect(x + i, y + i, width - delt, height - delt);
	    }

	}

	private void writeCssCommon(){
	    if(cssWriter == null)
	        return;
	    cssWriter.println("");
	    cssWriter.println(".common{");
	    cssWriter.print(" background-image:url(");
	    cssWriter.print(_outFile.getName());
	    cssWriter.println(");");
	    cssWriter.print("}");
	}

	protected void writeCss(String name,
	    int x, int y, int width, int height)
	{
	    if(cssWriter == null)
	        return;
	    cssWriter.println("");
	    cssWriter.print("/* ");
	    cssWriter.print(name);
	    cssWriter.println(" */");
	    cssWriter.println(".xxx{");
	    cssWriter.print(" background-position: ");
	    if(x > 0)
	        cssWriter.print("-");
	    cssWriter.print(x);
	    cssWriter.print("px ");
	    if(y>0)
	        cssWriter.print("-");
	    cssWriter.print(y);
	    cssWriter.println("px;");
	    cssWriter.print(" width:");
	    cssWriter.print(width);
	    cssWriter.println("px;");
	    cssWriter.print(" height:");
	    cssWriter.print(height);
	    cssWriter.println("px;");
	    cssWriter.println("}");
	}

	private static class Handler implements ScanHandler2{
	    private List<File> files = new ArrayList<File>();

	    public void processFile(File f, String relativePath){
	        files.add(f);
	        System.out.println("add image file: "+ relativePath);
	    }

	}

	public static void main(String[] args)throws Exception{
	    if(args.length <1 || args[1].equalsIgnoreCase("-h") || args[1].equalsIgnoreCase("/h")
	        || args[1].equalsIgnoreCase("?"))
	    {
	        System.out.println("usage: \n\t java liujing.util.ImageConsolidation [-h|-v|-b] -f <format> -o <path> <File path or DirScan pattern> ...");
	        System.out.println("\t-o\toutput path");
	        System.out.println("\t-c\tcss file path(it will generate or append to the css file)");
	        System.out.println("\t-f\toutput format name like \"jpg\" , \"png\"");
	        System.out.println("\t-h\thorizontal consolidation ");
	        System.out.println("\t-v\tvertical consolidation (default)");
	        System.out.println("\t-s\tsource directory");
	        System.out.println("\t-b\tdrow border");
	        return;
	    }
	    DirectoryScan2 scanner = new DirectoryScan2(false);
	    Handler h = new Handler();

	    ImageConsolidation cs = new ImageConsolidation();
	    cs.cmdOption(args);
	    scanner.setIncludes(cs._imageFilesPath);
	    scanner.scan(cs._rootDir,  h);
	    cs.create(h.files, cs._outFile, cs._format);
	}

	private void cmdOption(String[] args)throws Exception{
	    _imageFilesPath = new ArrayList();
	    for(int i = 0; i < args.length; i++){
	        String a = args[i];
	        if(a.equals("-h")){
	            horizontal = true;
	        }else if(a.equals("-c")){
	            i++;
	            cssFile = new File(args[i]);
	            cssWriter = new PrintWriter(new FileOutputStream(cssFile, true));
	        }else if(a.equals("-z")){
	            horizontal = false;
	        }
	        else if(a.equals("-f")){
	             i++;
	            _format = args[i];
	        }else if(a.equals("-o")){
	             i++;
	            _outFile = new File(args[i]);
	        }else if(a.equals("-s")){
	            i++;
	            _rootDir = new File(args[i]);
	        }else if(a.equals("-b")){
	            borderWidth = 20;
	        }else{
	            _imageFilesPath.add(a);
	        }
	    }
	}
}
