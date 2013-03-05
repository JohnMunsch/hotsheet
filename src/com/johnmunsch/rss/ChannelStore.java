/*
 * ChannelStore.java
 *
 * Created on March 30, 2001, 1:37 PM
 */
package com.johnmunsch.rss;

import java.io.File;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An interface that defines the characteristics that all channel stores need
 * to have. They may be implemented using serialization, JDBC, or something
 * completely different but they need all of these methods so they can all be
 * treated uniformly.
 *
 * A later version of the channel store may do away with functions like add,
 * remove, etc. and simply require that all inheritors implement the functions
 * of the AbstractList interface. That is a superset of what is available here.
 *
 * @author  johnm
 */
public interface ChannelStore {
    public boolean add(Channel channel);
    public boolean add(Collection collection);
    
    public boolean remove(Channel channel);
    public boolean remove(Collection collection);
    
    public void clear();

    public Iterator iterator();

    public int size();

    public List retrieve(boolean updateChannels);

    public ItemHistory getItemHistory();
    public ItemHistory setItemHistory(ItemHistory itemHistory);

    public void addChannelStoreListener(ChannelStoreListener l);
    public void removeChannelStoreListener(ChannelStoreListener l);

    public void load(File f);
    public void store(File f);
}
