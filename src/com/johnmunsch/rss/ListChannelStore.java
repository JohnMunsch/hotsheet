/*
 * ListChannelStore.java
 *
 * Created on March 30, 2001, 1:40 PM
 */
package com.johnmunsch.rss;

import java.io.*;
import java.util.*;
import javax.swing.event.EventListenerList;

import org.apache.log4j.*;

import com.johnmunsch.threadpool.*;

/**
 *
 * @author  johnm
 */
public class ListChannelStore implements ChannelStore {
    private LinkedList channelList = new LinkedList();

    private EventListenerList listenerList = new EventListenerList();
    private ChannelStoreEvent channelStoreEvent = null;

    private ThreadPool threadPool = null;
    private Object lock = new Object();
    private int count;
    private int jobs;
    
    private ItemHistory mItemHistory = null;

    private static Category cat = Category.getInstance(
        ListChannelStore.class.getName());
    
    // This class is passed in as the "job" to the addJob method on the thread 
    // pool. The only requirement for this class is that it must implement 
    // java.lang.Runnable.
    private class RetrieveJob implements java.lang.Runnable {
        private Channel channel = null;
        private List itemList = null;
        private boolean updateChannels = false;
        
        public RetrieveJob(Channel _channel, List _itemList, 
            boolean _updateChannels) {

            channel = _channel;
            itemList = _itemList;
            updateChannels = _updateChannels;
        }

        public void run() {
            try {
                itemList.addAll(channel.retrieve(updateChannels));
            } catch (Exception e) {
                cat.error(e);
            }

            synchronized(lock) {
                count++;
                
                fireChannelStoreRetrieved(channel, count, jobs);

                if (count == jobs) {
                    fireChannelStorePostRetrieve(itemList);
                }
            }
        }
    } 
    
    /** Creates new ListChannelStore */
    public ListChannelStore() {
        threadPool = new ThreadPoolImpl(0, 6, 1000);
    }

    public boolean add(Channel channel) {
        // Translate the convenience call into a form that can be used by the
        // main add function.
        List pending = new LinkedList();
        
        pending.add(channel);
      
        return (this.add(pending));
    }

    public boolean add(Collection collection) {
        fireChannelStorePreAdd(collection);
        boolean retVal = channelList.addAll(collection);
        fireChannelStorePostAdd();

        return (retVal);
    }

    public boolean remove(Channel channel) {
        // Translate the convenience call into a form that can be used by the
        // main remove function.
        List pending = new LinkedList();
        
        pending.add(channel);

        return (this.remove(pending));
    }

    public boolean remove(Collection collection) {
        fireChannelStorePreRemove(collection);
        boolean retVal = channelList.removeAll(collection);
        fireChannelStorePostRemove();
        
        return (retVal);
    }

    public void clear() {
        fireChannelStorePreClear();
        channelList.clear();
        fireChannelStorePostClear();
    }

    public Iterator iterator() {
        return (channelList.iterator());
    }

    public int size() {
        return (channelList.size());
    }

    public List retrieve(boolean updateChannels) {
        List returnValue = Collections.synchronizedList(new LinkedList());

        fireChannelStorePreRetrieve();
        Iterator i = channelList.iterator();

        // Set the number of channels for which we are going to create jobs.
        count = 0;
        jobs = channelList.size();

        while (i.hasNext()) {
            Channel channel = (Channel) i.next();

            threadPool.addJob(new RetrieveJob(channel, returnValue, true));
        }
        
        return (returnValue);
    }

    public ItemHistory getItemHistory() {
        return mItemHistory;
    }
    
    /**
     * If you choose to set an item history for the channel store it will be
     * used if the channel store gets called upon to create a default list of
     * channels. At no other time will it be used because the channel store is
     * normally given channels fully constructed and ready to use.
     */
    public ItemHistory setItemHistory(ItemHistory itemHistory) {
        mItemHistory = itemHistory;
        
        return mItemHistory;
    }
    
    public void addChannelStoreListener(ChannelStoreListener l) {
        listenerList.add(ChannelStoreListener.class, l);
    }

    public void removeChannelStoreListener(ChannelStoreListener l) {
        listenerList.remove(ChannelStoreListener.class, l);
    }

    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance 
    // is lazily created using the parameters passed into 
    // the fire method.
    protected void fireChannelStorePreAdd(Collection pending) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this, pending);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).preAdd(channelStoreEvent);
            }
        }
    }

    protected void fireChannelStorePostAdd() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).postAdd(channelStoreEvent);
            }
        }
    }
    
    protected void fireChannelStorePreRemove(Collection pending) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this, pending);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).preRemove(channelStoreEvent);
            }
        }
    }

    protected void fireChannelStorePostRemove() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).postRemove(channelStoreEvent);
            }
        }
    }
    
    protected void fireChannelStorePreClear() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).preClear(channelStoreEvent);
            }
        }
    }

    protected void fireChannelStorePostClear() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).postClear(channelStoreEvent);
            }
        }
    }
    
    protected void fireChannelStorePreRetrieve() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).preRetrieve(channelStoreEvent);
            }
        }
    }

    protected void fireChannelStoreRetrieved(Channel channel, 
        int value, int maximum) {

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                // Lazily create the event:
                if (channelStoreEvent == null) {
                    channelStoreEvent = new ChannelStoreEvent(this);
                }

                ((ChannelStoreListener) listeners[i + 1]).retrieved(
                    channelStoreEvent, channel, value, maximum);
            }
        }
    }

    protected void fireChannelStorePostRetrieve(Collection pending) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        channelStoreEvent = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChannelStoreListener.class) {
                 // Lazily create the event:
                 if (channelStoreEvent == null) {
                     channelStoreEvent = new ChannelStoreEvent(this, pending);
                 }

                 ((ChannelStoreListener) listeners[i + 1]).postRetrieve(channelStoreEvent);
            }
        }
    }

    public void load(File f) {
        try {
            FileInputStream inputStream = new FileInputStream(f);
        
            // If we find the file mentioned when we constructed this object we need
            // to serialize a linked list out of that file and use it as our 
            // existing item list.
            ObjectInputStream in = new ObjectInputStream(inputStream);
            channelList = (LinkedList) in.readObject();

            in.close();
        } catch (FileNotFoundException fnfe) {
            cat.error("Channel list not found. Creating default list of channels...");
            
            createDefaultChannelList();
        } catch (IOException ioe) {
            cat.error("Error reading channel list. Using default list of channels...");
            
            createDefaultChannelList();
        } catch (ClassNotFoundException cnfe) {
            cat.error("The channel object has changed and old channel list cannot be read. Using default channel list...");
            
            createDefaultChannelList();
        }
    }

    public void store(File f) {
        try {
            // Serialize our linked list out to a file.
            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(f));
            out.writeObject(channelList);

            // Also flushes output
            out.close();
        } catch (FileNotFoundException fnfe) {
            cat.error(fnfe);
        } catch (IOException ioe) {
            cat.error(ioe);
        }
    }
    
    private void createDefaultChannelList() {
        String[] hardwired = {
            "http://www.johnmunsch.com/index.xml",
            "http://www.donthorp.net/index.xml",
            "http://betanews.com/mnn.php3",
            "http://www.dpreview.com/news/dpr.rdf",
            "http://www.jsurfer.org/backend.php",
            "http://freshmeat.net/backend/fm-newsletter.rdf",
            "http://www.gamedev.net/xml",
            "http://www.javable.com/eng/rss.shtml",
            "http://www.LinuxNews.com/backend/weblog.rdf",
            "http://linuxtoday.com/backend/my-netscape.rdf",
            "http://www.pdabuzz.com/netscape.txt",
            "http://www.moreover.com/cgi-local/page?index_xml+rss",
            "http://www.slashdot.org/slashdot.rdf",
            "http://www.wired.com/news_drop/netcenter/netcenter.rdf",
            "http://www.xmlhack.com/rsscat.php"
        };
        
        for (int i = 0; i < hardwired.length; i++) {
            Channel channel = new Channel(hardwired[i]);
            channel.setItemHistory(mItemHistory);
            channelList.add(channel);
        }
    }
}
