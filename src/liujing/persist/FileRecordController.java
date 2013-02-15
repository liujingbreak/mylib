package liujing.persist;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

public class FileRecordController{
    private static Logger log = Logger.getLogger(FileRecordController.class.getName());
    protected RandomAccessFile rFile;
    protected IndexController indexCtl;
    protected IndexRecycleController indexRecycleCtl;
    protected ContentRecycleController contentRecycle;
    private File targetFile;
    private File indexRecycleFile;
    private File indexFile;
    private File contentRecycleFile;
    private boolean closed = false;

    public FileRecordController(File file)throws FileNotFoundException{
        targetFile = file;
        indexRecycleFile = new File(file.getParentFile(), file.getName()+ "-idx.del");
        indexFile = new File(file.getParentFile(), file.getName()+ ".idx");
        contentRecycleFile = new File(file.getParentFile(), file.getName()+ ".del");

        file.getParentFile().mkdirs();

        rFile = new RandomAccessFile(file, "rw");
        indexRecycleCtl = new IndexRecycleController(indexRecycleFile);
        indexCtl = new IndexController(indexFile, indexRecycleCtl);
        contentRecycle = new ContentRecycleController(contentRecycleFile);
    }

    public File getTargetFile(){
        return targetFile;
    }

    public FileObjectRef lastRecordRef()throws IOException{
        int lastIndex = indexCtl.count() - 1;
        if(lastIndex < 0){
            return null;
        }
        FileObjectRef ref = new FileObjectRef(lastIndex);
        return ref;
    }

    /**
    @return -1 if there is no record
    */
    public int lastIndex(){
        try{
            return indexCtl.count() - 1;
        }catch(IOException e){
            throw new FileRecordException("Failed retrieve last index", e);
        }
    }

    public void close(){
        try{
            if(closed)
                return;
            rFile.close();
            indexCtl.file.close();
            indexRecycleCtl.file.close();
            contentRecycle.file.close();
        }catch(IOException e){
            throw new FileRecordException("Failed to close", e);
        }
    }

    public void deleteFiles(){
        close();
        targetFile.delete();
        indexRecycleFile.delete();
        indexFile.delete();
        contentRecycleFile.delete();
    }

    /**
    @return index num of the new record
    */
    @Deprecated
    public int add(byte[] data)throws IOException{
        long p = rFile.length();
        rFile.seek(p);
        rFile.write(data);

        return indexCtl.allocateIndex(p, data.length);
    }

    @Deprecated
    public int add(byte[] data, int off, int len)throws IOException{
        long p = rFile.length();
        rFile.seek(p);
        rFile.write(data, off, len);
        return indexCtl.allocateIndex(p, len);
    }

    public int allocate(int len)throws IOException{
        long p = rFile.length();
        rFile.setLength(p + len);
        return indexCtl.allocateIndex(p, len);
    }

    public int allocate(int len, byte[] data)throws IOException{
        int extendLen = len;
        if(data.length > len){
            extendLen = data.length;
        }
        long p = rFile.length();
        rFile.setLength(p + extendLen);
        int idx = indexCtl.allocateIndex(p, data.length, extendLen);
        rFile.seek(p);
        rFile.write(data);
        //if(log.isLoggable(Level.FINE))
        //    log.fine("<a#" + idx + " offset:" + p + " len:" + data.length + ">");
        return idx;
    }

    public void update(int index, byte[] data)throws IOException{
        indexCtl.loadIndex(index);
        long offset = indexCtl.getOffset();
        int capacity = indexCtl.getCapacity();
        int newLen = data.length;
        if(newLen > capacity){
            offset = increaseCapacity(newLen);
        }else{
            indexCtl.write(index, offset, data.length, capacity);
        }
        rFile.seek(offset);
        rFile.write(data);

        //if(log.isLoggable(Level.FINE))
        //    log.fine("<u#" + index + " offset:" + offset + " len:" + data.length + ">");
    }

    protected long increaseCapacity(int minLen)throws IOException{
        long endOffset = indexCtl.getOffset() + indexCtl.getCapacity();
        int newLen = (indexCtl.getCapacity() * 3)/2 + 1;
        if(newLen < minLen)
            newLen = minLen;
        if( endOffset == rFile.length() )
        {   //this block is the last one in the file, we don't need to move it
            rFile.setLength(indexCtl.getOffset() + newLen);
            indexCtl.write(indexCtl.getCurrentIndex(), indexCtl.getOffset(),
                minLen, newLen);
            return indexCtl.getOffset();
        }else{
            contentRecycle.add(indexCtl.getOffset(), indexCtl.getCapacity());
            long offset = rFile.length();
            indexCtl.write(indexCtl.getCurrentIndex(), offset, minLen, newLen);
            rFile.setLength(offset + newLen);
            return offset;
        }
    }

    public void delete(int index)throws IOException{
        indexCtl.loadIndex(index);
        if(indexCtl.getOffset() + indexCtl.getCapacity() >= rFile.length()){
            //this block is the last one in the file
            rFile.setLength(indexCtl.getOffset());
        }else{
            contentRecycle.add(indexCtl.getOffset(), indexCtl.getCapacity());
        }
        indexCtl.delete(index);
    }

    /**
    @return length of the bytes
    */
    public int get(int index, byte[] retrieve, int off)throws IOException{
        indexCtl.loadIndex(index);
        long offset = indexCtl.getOffset();
        int len = indexCtl.getContentLen();
        rFile.seek(offset);
        rFile.read(retrieve, off, len);
        return len;
    }

    public byte[] getBytes(int index)throws IOException{
        indexCtl.loadIndex(index);
        long offset = indexCtl.getOffset();
        int len = indexCtl.getContentLen();
        byte[] b = new byte[len];
        rFile.seek(offset);
        rFile.read(b, 0, len);
        return b;
    }

    public void truncate(){
        try{
            rFile.setLength(0);
            indexCtl.truncate();
            indexRecycleCtl.truncate();
            contentRecycle.truncate();
        }catch(IOException e){
            throw new FileRecordException("Failed to truncate", e);
        }
    }

    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    public void compact()throws IOException{
        log.info("compact "+ targetFile.getName());
        File newFile = new File(targetFile.getPath() + ".new");
        FileChannel newChannel = new RandomAccessFile(newFile, "rw").getChannel();
        FileChannel oldChannel = rFile.getChannel();

        indexCtl.file.seek(0);
        while(indexCtl.file.getFilePointer() < indexCtl.file.length()){
            long indexFileP = indexCtl.file.getFilePointer();
            indexCtl.load();
            if(! indexCtl.isIndexDeleted()){
                long newOffset = newChannel.position();
                copy(oldChannel, newChannel, buffer, indexCtl.getOffset(),
                    indexCtl.getContentLen());
                indexCtl.file.seek(indexFileP);
                indexCtl.store(newOffset, indexCtl.getContentLen(), indexCtl.getCapacity());
            }
        }
        contentRecycle.truncate();
        newChannel.close();
        oldChannel.close();
        rFile.close();
        String oldFilePath = targetFile.getPath();
        File oldF = new File(targetFile.getPath() + ".old");
        File newF = new File(oldFilePath);
        if(oldF.exists()){
            log.info("delete existing "+ oldFilePath + ": " + oldF.delete());
        }
        targetFile.delete();
        //boolean b = targetFile.renameTo(oldF);
        newFile.renameTo(newF);
    }

    private void copy(FileChannel from, FileChannel to, ByteBuffer buf,
        long offset, int len)throws IOException
    {
        buf.clear();
        from.position(offset);
        int rest = len;
        while(rest != 0 || buf.position() != 0){
            if(rest > 0){
                if(rest < buf.remaining()){
                    buf.limit(buf.position() + rest);
                }
                rest -= from.read(buf);
                buf.flip();
            }
            to.write(buf);
            buf.compact();
        }
    }

    public String toString(){
        try{
            StringBuilder s = new StringBuilder("[Total num: ");
            s.append(indexCtl.count());
            s.append(" | recycled index: ");
            s.append(indexRecycleCtl.count());
            s.append(" | recycled blocks: ");
            s.append(contentRecycle.count());
            s.append(" | recycled block length: ");
            s.append(contentRecycle.countRecycledLen());
            s.append("]");
            return s.toString();
        }catch(IOException e){
            return e.toString();
        }
    }

    public String dump()throws IOException{
        StringBuilder s = new StringBuilder();
        for(int i = 0, c = indexCtl.count(); i < c; i++){
            indexCtl.loadIndex(i);
            if(indexCtl.isIndexDeleted()){
                continue;
            }
            s.append("[#");
            s.append(i);
            s.append(" offset:");
            s.append(indexCtl.getOffset());
            s.append(" length:");
            s.append(indexCtl.getContentLen());
            s.append("]\n");
        }
        return s.toString();
    }

    @Override
    protected void finalize()throws Throwable{
        close();
    }

    class IndexController{
        private static final int RECORD_ENTRY_SIZE = Long.SIZE/Byte.SIZE + ((Integer.SIZE/Byte.SIZE)*2);
        private int currIndex;
        private long currOffset;
        private int contentLen;
        private int capacity;
        private RandomAccessFile file;
        private IndexRecycleController recycleCtl;

        public IndexController(File indexFile, IndexRecycleController recycleCtl)throws FileNotFoundException{
            file = new RandomAccessFile(indexFile, "rw");
            this.recycleCtl = recycleCtl;
        }

        public void loadIndex(int index)throws IOException{
            file.seek(index * RECORD_ENTRY_SIZE);
            currIndex = index;
            load();
        }

        private void load()throws IOException{
            currOffset = file.readLong();
            contentLen = file.readInt();
            capacity = file.readInt();
        }

        public int allocateIndex(long offset, int capacity)throws IOException{
            return allocateIndex(offset, 0, capacity);
        }

        public int allocateIndex(long offset, int len, int capacity)throws IOException{
            int index = recycleCtl.get();
            long fileLen = file.length();
            if(index < 0){
                index = (int)(fileLen / (long)RECORD_ENTRY_SIZE);
            }
            write(index, offset, len, capacity);
            return index;
        }

        public void write(int index, long contentOffset, int contentLen,
            int capacity)throws IOException
        {
            file.seek(index * RECORD_ENTRY_SIZE);
            currIndex = index;
            store(contentOffset, contentLen, capacity);
            //file.writeLong(contentOffset);
            //file.writeInt(contentLen);
            currOffset = contentOffset;
            contentLen = contentLen;
        }

        private void store(long offset, int len, int capacity)throws IOException{
            file.writeLong(offset);
            file.writeInt(len);
            file.writeInt(capacity);
            //log.fine("offset:" + offset + " len:"+ len + " cap:"+ capacity);
        }

        private void delete(int index)throws IOException{
            if( isLastIndex(index)){
                file.setLength(file.length() - (long)RECORD_ENTRY_SIZE);
            }else{
                recycleCtl.add(index);
                write(index, -1, -1, -1);
            }
        }

        private boolean isLastIndex(int index)throws IOException{
            return index + 1 == count();
        }

        public boolean isIndexDeleted()throws IOException{
            return currOffset == -1;
        }

        public int getCurrentIndex(){
            return currIndex;
        }

        public long getOffset(){
            return currOffset;
        }

        public int getContentLen(){
            return contentLen;
        }

        public int getCapacity(){
            return capacity;
        }

        public int count()throws IOException{
            return (int)(file.length()/(long)RECORD_ENTRY_SIZE);
        }

        public void truncate()throws IOException{
            file.setLength(0);
        }
    }

    private class IndexRecycleController{
        private RandomAccessFile file;
        private static final int RECORD_ENTRY_SIZE = Integer.SIZE/Byte.SIZE;
        private static final int HEAD_SIZE = Long.SIZE/Byte.SIZE;

        public IndexRecycleController(File f)throws FileNotFoundException{
            file = new RandomAccessFile(f, "rw");
        }

        public void truncate()throws IOException{
            file.setLength(0);
        }

        public void add(int index)throws IOException{
            long last = getFileLength();
            file.seek(last);
            file.writeInt(index);
            file.seek(0);
            log.info(index + " file point=" + (file.getFilePointer()) + " last = " + last);
            file.writeLong(last + RECORD_ENTRY_SIZE);

        }

        public boolean hasMore()throws IOException{
            return getFileLength() > 0;
        }

        public int get()throws IOException{
            long last = getFileLength();
            if(last <= HEAD_SIZE){
                return -1;
            }
            last = last - RECORD_ENTRY_SIZE;
            file.seek(last);
            int index = file.readInt();
            file.seek(0);
            file.writeLong(last);
            reduceFileSize(last);
            return index;
        }

        public int count()throws IOException{
            return (int) ((getFileLength() - HEAD_SIZE) / (long)RECORD_ENTRY_SIZE);
        }

        private void reduceFileSize(long lastOffset)throws IOException{
            if((file.length() - lastOffset) > 102400){
                file.setLength(lastOffset);
                log.info("reduce recycle file size to "+ lastOffset);
            }
        }

        private long getFileLength()throws IOException{
            if(file.length() > HEAD_SIZE){
                file.seek(0);
                return file.readLong();
            }else{
                file.seek(0);
                file.writeLong(HEAD_SIZE);
                return HEAD_SIZE;
            }
        }
    }

    private class ContentRecycleController{
        private RandomAccessFile file;
        private static final int RECORD_ENTRY_SIZE = Long.SIZE/Byte.SIZE + Integer.SIZE/Byte.SIZE;

        public ContentRecycleController(File f)throws FileNotFoundException{
            file = new RandomAccessFile(f, "rw");
        }

        public void add(long offset, int len)throws IOException{
            file.seek(file.length());
            file.writeLong(offset);
            file.writeInt(len);
        }

        public int count()throws IOException{
            return (int)(file.length() / (long)RECORD_ENTRY_SIZE);
        }

        public long countRecycledLen()throws IOException{
            file.seek(0);
            long rlen = 0;
            long filelen = file.length();
            while(file.getFilePointer() < filelen){
                long offset = file.readLong();
                int len = file.readInt();
                if(len >=0 )
                    rlen += len;
            }
            return rlen;
        }

        public void truncate()throws IOException{
            file.setLength(0);
        }
    }

    public static void main(String[] args){
        try{
            FileRecordController p = new FileRecordController(new File(args[0]));
            System.out.println(p.dump());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
