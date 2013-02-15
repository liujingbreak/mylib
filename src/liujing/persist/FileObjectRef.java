package liujing.persist;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class FileObjectRef<T> implements Serializable{
    static final Logger log = Logger.getLogger(FileObjectRef.class.getName());
    static final long serialVersionUID = 1L;

    protected int fileRecordIndex = -1;

    private transient T cacheObject;

    private transient boolean dirty = false;

    public FileObjectRef(){
    }

    public FileObjectRef(T value){
        set(value);
    }

    protected FileObjectRef(int index){
        fileRecordIndex = index;
    }

    public void save(FileRecordController fa){
        try{
            setValue(fa, cacheObject);
        }catch(IOException e){
            throw new FileRecordException("Failed to save", e);
        }
    }

    public void load(FileRecordController fa){
        try{
            cacheObject = getValue(fa);
        }catch(IOException e){
            throw new FileRecordException("Failed to load", e);
        }catch(ClassNotFoundException e){
            throw new FileRecordException("Failed to load", e);
        }
    }

    public void delete(FileRecordController fa){
        try{
            fa.delete(fileRecordIndex);
        }catch(IOException e){
            throw new FileRecordException("Failed to delete", e);
        }
    }

    public T get(FileRecordController fa){
            if(cacheObject == null){
                load(fa);
            }
            return cacheObject;
    }

    public void set(T v){
        cacheObject = v;
        dirty = true;
    }

    public void releaseCache(){
        cacheObject = null;
        dirty = false;
    }

    public int getFileRecordIdx(){
        return fileRecordIndex;
    }

    protected void setValue(FileRecordController fa, T v)throws IOException{
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(v);
        out.close();
        byte[] bytes = bout.toByteArray();
        setBytes(fa, bytes);
    }

    protected T getValue(FileRecordController fa)throws IOException, ClassNotFoundException{
        try{
            if(fileRecordIndex == -1)
                return null;
            byte[] b = fa.getBytes(fileRecordIndex);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object o = in.readObject();
            in.close();
            return (T)o;
        }catch(IOException ioe){
            log.severe("Exception encountered in index "+ fileRecordIndex);
            throw ioe;
        }
    }

    protected void setBytes(FileRecordController fa, byte[] bytes)throws IOException{

        if(fileRecordIndex == -1){
            fileRecordIndex = fa.allocate(bytes.length + 10, bytes);
        }else{
            fa.update(fileRecordIndex, bytes);
        }
        if(log.isLoggable(Level.FINE))
            log.fine("write [#" + fileRecordIndex + " len:" + bytes.length + "]");
    }

    private void writeObject(java.io.ObjectOutputStream out)throws IOException{
        if(fileRecordIndex < 0){
            throw new IOException("file Record is not saved before the reference serialization, content:"
                + cacheObject);
        }
        out.writeInt(fileRecordIndex);
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException{
        fileRecordIndex = in.readInt();
    }


}
