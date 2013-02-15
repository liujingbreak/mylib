package liujing.util.event;

import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 EventBus
 @author Break(Jing) Liu
*/
public class EventBus<T extends EventObject>{
    /** log */
    private static Logger log = Logger.getLogger(EventBus.class.getName());

    static Map<Class<? extends EventObject>, EventBus> busMap = new HashMap();

    protected Set<EventBusListener<T>> listenerList;

    public static <E extends EventObject> EventBus<E> getGlobalBus(Class<E> cls){
        EventBus<E> bus = busMap.get(cls);
        if(bus == null){
            bus = new EventBus<E>();
            busMap.put(cls, bus);
        }
        return bus;
    }

    /**  construct a local EventBus
    */
    public EventBus(){
        listenerList = new HashSet();
    }

    /**  construct a local EventBus with a listener
     @param listener listener
    */
    public EventBus(EventBusListener<T> listener){
        this();
        addListener(listener);
    }

    public void addListener(EventBusListener<T> listener){
        listenerList.add(listener);
    }

    /**  addEventListener same as addListener()
     @param listener listener
    */
    public void addEventListener(EventBusListener<T> listener){
        addListener(listener);
    }

    public void removeListener(EventBusListener<T> listener){
        listenerList.remove(listener);
    }

    /**  removeEventListener same as removeListener()
     @param listener listener
    */
    public void removeEventListener(EventBusListener<T> listener){
        listenerList.remove(listener);
    }

    public void fireEvent(T event){
        for(EventBusListener<T> listener : listenerList){
            listener.handleEvent(event);
        }
    }
    /**  dispatchEvent same as the fireEvent()
     @param event event
    */
    public void dispatchEvent(T event){
        fireEvent(event);
    }
}

