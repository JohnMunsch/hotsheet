/*
 * ChannelStoreListener.java
 *
 * Created on June 28, 2001, 10:26 AM
 */
package com.johnmunsch.rss;

/**
 *
 * @author  JMunsch
 */
public interface ChannelStoreListener extends java.util.EventListener {
    public void preAdd(ChannelStoreEvent evt);
    public void postAdd(ChannelStoreEvent evt);
    
    public void preRemove(ChannelStoreEvent evt);
    public void postRemove(ChannelStoreEvent evt);
    
    public void preClear(ChannelStoreEvent evt);
    public void postClear(ChannelStoreEvent evt);
    
    public void preRetrieve(ChannelStoreEvent evt);
    public void retrieved(ChannelStoreEvent evt, Channel channel, int value, 
        int maximum);
    public void postRetrieve(ChannelStoreEvent evt);
}

