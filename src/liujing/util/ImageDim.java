package liujing.util;

import java.awt.Dimension;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import javax.imageio.stream.ImageInputStream;
import java.util.logging.Logger;

public class ImageDim {
    private final static Logger log = Logger.getLogger(ImageDim.class.getName());
    
    public static Dimension getImageDimension(File f) throws IOException {
        ImageInputStream stream = null;
        try{
            stream = ImageIO.createImageInputStream(f);
            Iterator iter = ImageIO.getImageReaders(stream);
            if (!iter.hasNext()) {
                throw new RuntimeException("No image in file "+ f.getPath());
                //return null;
            }
    
            ImageReader reader = (ImageReader)iter.next();
            reader.setInput(stream, true, true);
            Dimension dim = new Dimension(reader.getWidth(0), reader.getHeight(0));
            //log.info("getImageMetadata: " + reader.getImageMetadata(0));
            reader.dispose();
            return dim;
        }catch(Exception ex){
            throw new RuntimeException("Failed to retrieve image size", ex);
        }finally{
            if(stream != null)
                stream.close();
        }
    }
    
    public static void main(String[] args)throws Exception{
        File targetFile = new File(args[0]);
        System.out.println(getImageDimension(targetFile));
    }
}
