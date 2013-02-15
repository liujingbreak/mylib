package liujing.persist;

import java.util.*;
import java.io.*;
import java.util.logging.*;
import liujing.persist.*;

/**
 PersistentTreeSet
 @author Break(Jing) Liu
*/
public class PersistentTreeSet<E>{
    /** log */
    private static Logger log = Logger.getLogger(PersistentTreeSet.class.getName());
    protected FileRecordController persist;
    protected TreeSet<FileObjectRef<E>> index;
    protected IndexComparator<E> comp;
    protected FileObjectRef<TreeSet> indexRef;
    protected boolean isWrite = false;
    protected boolean closed = false;

    public PersistentTreeSet(){

    }

    /**  create or overwrite a new file for writing index
     @param file file
     @param elementComp elementComp
     @throws FileNotFoundException if FileNotFoundException occurs
    */
    public void create(File file, PersistentComparator<E> elementComp)throws FileNotFoundException{

        persist = new FileRecordController(file);
        persist.deleteFiles();
        persist = new FileRecordController(file);
        isWrite = true;
        comp = new IndexComparator(elementComp);
        index = new TreeSet(comp);
        indexRef = new FileObjectRef(index);
        indexRef.save(persist);
    }

    /**  load for reading from an existing file
     @param file file
     @throws FileNotFoundException if FileNotFoundException occurs
    */
    public void load(File file)throws FileNotFoundException{
        load(file, 0);
    }

    /**  load for reading from an existing file
     @param file file
     @throws FileNotFoundException if FileNotFoundException occurs
    */
    public void load(File file, int indexRefNo)throws FileNotFoundException{
        persist = new FileRecordController(file);
        indexRef = new FileObjectRef(indexRefNo);
        index = indexRef.get(persist);
        comp = (IndexComparator)index.comparator();
        comp.setPersist(persist);
    }

    public void close(){
        if(isWrite){
            indexRef.save(persist);
        }
        closed = true;
    }

    @Override
    protected void finalize(){
        if(!closed)
            close();
    }

    public FileObjectRef<E> add(E value){
        FileObjectRef<E> ref = new FileObjectRef<E>(value);
        ref.save(persist);
        index.add(ref);
        isWrite = true;
        return ref;
    }

    public void add(FileObjectRef<E> ref){
        index.add(ref);
        isWrite = true;
    }

    public boolean contains(E value){
        return index.contains(new QueryPersistRef(value));
    }

    public E getPersistedValue(E value){
        return index.ceiling(new QueryPersistRef(value)).get(persist);
    }

    public SortedSet<FileObjectRef<E>> subSet(E fromElement, E toElement)
    {
         SortedSet<FileObjectRef<E>> result = index.subSet(createQueryRef(fromElement),
             createQueryRef(toElement));

         return result;
    }

    protected FileObjectRef<E> createQueryRef(E element){
        return new QueryPersistRef(element);
    }

    public static interface PersistentComparator<T> extends Comparator<T>, Serializable{

    }

    static class IndexComparator<T> implements Comparator<FileObjectRef<T>>, Serializable{
        private transient FileRecordController persist;
        private PersistentComparator<T> elementComp;

        public IndexComparator(PersistentComparator<T> elementComp){
            this.elementComp = elementComp;
        }
        /** get persist
         @return persist
        */
        public FileRecordController getPersist(){
            return persist;
        }
        /** set persist
         @param persist persist
        */
        public void setPersist(FileRecordController persist){
            this.persist = persist;
        }
        public int compare(FileObjectRef<T> o1, FileObjectRef<T> o2){
            T e1 = o1.get(persist);
            T e2 = o2.get(persist);
            return elementComp.compare(e1, e2);
        }
    }

    private static class QueryPersistRef<T> extends FileObjectRef<T>{
        T value;
        public QueryPersistRef(T value){
            this.value = value;
        }

        @Override
        public T get(FileRecordController fa){
            return value;
        }
    }
}

