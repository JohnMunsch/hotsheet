/*
 * ListenersTest.java
 *
 * Created on July 1, 2001, 10:38 PM
 */
package com.johnmunsch.demo;

import com.johnmunsch.rss.*;
import java.util.Iterator;
import java.util.List;

class DemoItemStoreListener implements ItemStoreListener {
    public void preAdd(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.preAdd: ");
        
        Iterator iterator = evt.getPending().iterator();
        
        while (iterator.hasNext()) {
            Item item = (Item) iterator.next();
            
            System.out.println(item.toString());
        }
    }
    
    public void postAdd(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.postAdd");
    }
    
    public void preRemove(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.preRemove: ");

        Iterator iterator = evt.getPending().iterator();
        
        while (iterator.hasNext()) {
            Item item = (Item) iterator.next();
            
            System.out.println(item.toString());
        }
    }
    
    public void postRemove(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.postRemove");
    }
    
    public void preClear(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.preClear");
    }
    
    public void postClear(ItemStoreEvent evt) {
        System.out.println("DemoItemStoreListener.postClear");
    }
}

class DemoChannelStoreListener implements ChannelStoreListener {
    public void preAdd(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.preAdd: ");

        Iterator iterator = evt.getPending().iterator();
        
        while (iterator.hasNext()) {
            Channel channel = (Channel) iterator.next();
            
            System.out.println(channel.toString());
        }
    }
    
    public void postAdd(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.postAdd");
    }
    
    public void preRemove(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.preRemove: ");

        Iterator iterator = evt.getPending().iterator();
        
        while (iterator.hasNext()) {
            Channel channel = (Channel) iterator.next();
            
            System.out.println(channel.toString());
        }
    }
    
    public void postRemove(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.postRemove");
    }
    
    public void preClear(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.preClear");
    }
    
    public void postClear(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.postClear");
    }
    
    public void preRetrieve(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.preRetrieve");
    }
    
    public void retrieved(ChannelStoreEvent evt, Channel channel, int value, 
        int maximum) {

        System.out.println("DemoChannelStoreListener.retrieved");
    }
    
    public void postRetrieve(ChannelStoreEvent evt) {
        System.out.println("DemoChannelStoreListener.postRetrieve");
    }
}

/**
 *
 * @author  John Munsch
 */
public class ListenersTest {
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        // Create a channel store and attach a listener to it.
        ChannelStore channelStore = new ListChannelStore();
        DemoChannelStoreListener demoChannelStoreListener = 
            new DemoChannelStoreListener();
        channelStore.addChannelStoreListener(demoChannelStoreListener);

        // Add some channels to the channel store.
        channelStore.add(new Channel("http://freshmeat.net/backend/fm.rdf"));
        channelStore.add(new Channel("http://www.gamedev.net/xml"));
        channelStore.add(new Channel("http://www.wired.com/news_drop/netcenter/netcenter.rdf"));
        
        // Remove a channel from the channel store.
        channelStore.remove(new Channel("http://www.gamedev.net/xml"));
        
        // Create an item store and attach a listener to it.
        ItemStore itemStore = new ListItemStore();
        DemoItemStoreListener demoItemStoreListener = new DemoItemStoreListener();
        itemStore.addItemStoreListener(demoItemStoreListener);
        
        // Retrieve the channels in the in channel store.
        List itemList = channelStore.retrieve(true);

        // Add the items found from retrieving the channels to the item store.
        itemStore.add(itemList);
        
        // Remove the first item from the item store. Note: If no items were
        // retrieved you might not see a message for this step.
        Iterator i = itemStore.iterator();
        
        while (i.hasNext()) {
            Item item = (Item) i.next();
            itemStore.remove(item);
            break;
        }        
        
        // Clear the items out of the item store.
        itemStore.clear();
        
        // Clear the channels out of the channel store.
        channelStore.clear();

        System.exit(0);
    }
}
