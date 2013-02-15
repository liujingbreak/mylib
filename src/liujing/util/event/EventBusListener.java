package liujing.util.event;

import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 EventBusListener
 @author Break(Jing) Liu
*/
public interface EventBusListener<E extends EventObject> extends EventListener{

    void handleEvent(E event);
}

