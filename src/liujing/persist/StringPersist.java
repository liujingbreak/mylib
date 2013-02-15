package liujing.persist;

import java.util.*;
import java.util.logging.*;
import java.io.*;

public class StringPersist extends FileObjectRef<String> implements Serializable{
    static final long serialVersionUID = 1L;
    
    public StringPersist(){}
    
    public StringPersist(String value){
        super(value);
    }
    
    @Override
    protected void setValue(FileRecordController fa, String v)throws IOException{
        setString(fa, v);
    }
    
    @Override
    protected String getValue(FileRecordController fa)throws IOException, ClassNotFoundException{
        return getString(fa);
    }
    
    protected void setString(FileRecordController fa, String v)throws IOException{
        setBytes(fa, v.getBytes());
    }
    
    protected String getString(FileRecordController fa)throws IOException, ClassNotFoundException{
        if(fileRecordIndex == -1)
            return null;
        byte[] b = fa.getBytes(fileRecordIndex);
        
        return new String(b);
    }
}
