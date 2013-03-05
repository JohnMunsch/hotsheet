/*
 * HeadlineStore.java
 *
 * Created on March 19, 2001, 12:40 PM
 */
package com.johnmunsch.rss;

import java.io.File;

import java.util.Collection;
import java.util.Iterator;

/**
 * An interface for the storage mechanism that holds the set of all known items.
 * This can be a temporary store that is just in memory, a longer term
 * solution that uses Java serialization to hold news items on disk between
 * sessions, or a long term solution that uses a JDBC database to store all of
 * the items found.
 *
 * @author  johnm
 */
public interface ItemStore {
    public final int NONE = 0;
    public final int CHANNEL_TITLE = 1;
    public final int ITEM_TITLE = 2;
    public final int ITEM_RETRIEVAL_DATE = 3;
    public final int SCORE = 4;
    public final int VIEWED = 5;

    public boolean add(Item item);
    public boolean add(Collection collection);
    
    public boolean remove(Item item);
    public boolean remove(Collection collection);
    
    public void clear();

    public Iterator iterator();

    public int size();

    public void sort(int _firstAttribute, int _secondAttribute, 
        int _thirdAttribute);
    
    public void addItemStoreListener(ItemStoreListener l);
    public void removeItemStoreListener(ItemStoreListener l);

    public void load(File f);
    public void store(File f);
}
