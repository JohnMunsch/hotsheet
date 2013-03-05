/*
 * ListItemStore.java
 *
 * Created on March 28, 2001, 7:54 AM
 */
package com.johnmunsch.rss;

import java.io.*;
import java.util.*;
import javax.swing.event.EventListenerList;

import org.apache.log4j.*;

/**
 *
 * @author  John Munsch
 */
public class ListItemStore implements ItemStore {
    private List itemList = null;

    private EventListenerList listenerList = new EventListenerList();
    private ItemStoreEvent itemStoreEvent = null;
    
    private static Category cat = Category.getInstance(
        ListItemStore.class.getName());
    
    /** Creates new ListItemStore */
    public ListItemStore() {
        itemList = Collections.synchronizedList(new LinkedList());
    }

    public boolean add(Item item) {
        // Translate the convenience call into a form that can be used by the
        // main add function.
        List pending = new LinkedList();
        
        pending.add(item);

        return (this.add(pending));
    }
    
    public boolean add(Collection collection) {
        fireItemStorePreAdd(collection);
        boolean retVal = itemList.addAll(collection);
        fireItemStorePostAdd();
        
        return (retVal);
    }
    
    public boolean remove(Item item) {
        // Translate the convenience call into a form that can be used by the
        // main remove function.
        List pending = new LinkedList();
        
        pending.add(item);

        return (this.remove(pending));
    }
    
    public boolean remove(Collection collection) {
        fireItemStorePreRemove(collection);
        boolean retVal = itemList.removeAll(collection);
        fireItemStorePostRemove();
        
        return (retVal);
    }
    
    public void clear() {
        fireItemStorePreClear();
        itemList.clear();
        fireItemStorePostClear();
    }

    public Iterator iterator() {
        return (itemList.iterator());
    }
    
    public int size() {
        return (itemList.size());
    }

    public String toString() {
        StringBuffer returnValue = new StringBuffer();
        
        // Dump all of the news items we have in our item store.
        synchronized(itemList) {
            Iterator itemIterator = itemList.iterator();

            while (itemIterator.hasNext()) {
                returnValue.append(((Item) itemIterator.next()).toString());
            }
        }
        
        return (returnValue.toString());
    }
    
    public void sort(int _firstAttribute, int _secondAttribute, 
        int _thirdAttribute) {
            
        Comparator comparator = new ItemComparator(_firstAttribute, 
            _secondAttribute, _thirdAttribute);
        
        Collections.sort(itemList, comparator);
    }
    
    public void addItemStoreListener(ItemStoreListener l) {
        listenerList.add(ItemStoreListener.class, l);
    }

    public void removeItemStoreListener(ItemStoreListener l) {
        listenerList.remove(ItemStoreListener.class, l);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance 
    // is lazily created using the parameters passed into 
    // the fire method.
    protected void fireItemStorePreAdd(Collection pending) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this, pending);
                 }

                 ((ItemStoreListener) listeners[i + 1]).preAdd(itemStoreEvent);
            }
        }
    }

    protected void fireItemStorePostAdd() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this);
                 }

                 ((ItemStoreListener) listeners[i + 1]).postAdd(itemStoreEvent);
            }
        }
    }

    protected void fireItemStorePreRemove(Collection pending) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this, pending);
                 }

                 ((ItemStoreListener) listeners[i + 1]).preRemove(itemStoreEvent);
            }
        }
    }

    protected void fireItemStorePostRemove() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this);
                 }

                 ((ItemStoreListener) listeners[i + 1]).postRemove(itemStoreEvent);
            }
        }
    }
    
    protected void fireItemStorePreClear() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this);
                 }

                 ((ItemStoreListener) listeners[i + 1]).preClear(itemStoreEvent);
            }
        }
    }

    protected void fireItemStorePostClear() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        itemStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemStoreListener.class) {
                 // Lazily create the event:
                 if (itemStoreEvent == null) {
                     itemStoreEvent = new ItemStoreEvent(this);
                 }

                 ((ItemStoreListener) listeners[i + 1]).postClear(itemStoreEvent);
            }
        }
    }

    public void load(File f) {
        try {
            
            FileInputStream inputStream = new FileInputStream(f);

            // If we find the file mentioned when we constructed this object we 
            // need to serialize a linked list out of that file and use it as 
            // our existing item list.
            ObjectInputStream in = new ObjectInputStream(inputStream);
            itemList = (List) in.readObject();

            in.close();
        } catch (FileNotFoundException fnfe) {
            cat.error(fnfe);
        } catch (IOException ioe) {
            cat.error(ioe);
        } catch (ClassNotFoundException cnfe) {
            cat.error(cnfe);
        }
    }

    public void store(File f) {
        try {
            // Serialize our linked list out to a file.
            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(f));
            out.writeObject(itemList);

            // Also flushes output
            out.close();
        } catch (FileNotFoundException fnfe) {
            cat.error(fnfe);
        } catch (IOException ioe) {
            cat.error(ioe);
        }
    }
}
