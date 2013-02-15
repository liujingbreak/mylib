package liujing.persist;

public class FileRecordException extends RuntimeException{
    public FileRecordException(){
        super();
    }

    public FileRecordException(String msg){
        super(msg);
    }

    public FileRecordException(String msg, Throwable cause){
        super(msg, cause);
    }

}
