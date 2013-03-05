/*
 * ItemStoreListener.java
 *
 * Created on June 28, 2001, 10:27 AM
 */
package com.johnmunsch.rss;

/**
 *
 * @author  JMunsch
 */
public interface ItemStoreListener extends java.util.EventListener {
    public void preAdd(ItemStoreEvent evt);
    public void postAdd(ItemStoreEvent evt);
    
    public void preRemove(ItemStoreEvent evt);
    public void postRemove(ItemStoreEvent evt);
    
    public void preClear(ItemStoreEvent evt);
    public void postClear(ItemStoreEvent evt);
}

