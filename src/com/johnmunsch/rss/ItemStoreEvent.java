/*
 * ItemStoreEvent.java
 *
 * Created on June 28, 2001, 9:44 AM
 */
package com.johnmunsch.rss;

import java.util.Collection;

/**
 *
 * @author  JMunsch
 */
public class ItemStoreEvent extends java.util.EventObject {
    private Collection pending;
    
    /** Creates new ItemStoreEvent */
    public ItemStoreEvent(Object source) {
        super(source);
        
        pending = null;
    }

    public ItemStoreEvent(Object source, Collection _pending) {
        super(source);
        
        pending = _pending;
    }
    
    public Collection getPending() {
        return (pending);
    }
}
