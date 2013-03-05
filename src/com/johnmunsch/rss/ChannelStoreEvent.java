/*
 * ChannelStoreEvent.java
 *
 * Created on June 28, 2001, 10:03 AM
 */
package com.johnmunsch.rss;

import java.util.Collection;

/**
 *
 * @author  JMunsch
 */
public class ChannelStoreEvent extends java.util.EventObject {
    private Collection pending;
    
    /** Creates new ChannelStoreEvent */
    public ChannelStoreEvent(Object source) {
        super(source);
        
        pending = null;
    }

    public ChannelStoreEvent(Object source, Collection _pending) {
        super(source);
        
        pending = _pending;
    }
    
    /**
     * Get the list of items which we are about to add or have retrieved. If
     * this event is a pre-add event then the collection is a list of channels
     * to be added. If the event is a post retrieve event then the collection
     * is the list of items we retrieved from our channels.
     */
    public Collection getPending() {
        return (pending);
    }
}
