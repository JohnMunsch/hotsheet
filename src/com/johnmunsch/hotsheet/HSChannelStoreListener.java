/*
 * HSChannelStoreListener.java
 *
 * Created on August 21, 2001, 1:24 PM
 */
package com.johnmunsch.hotsheet;

import java.util.List;
import javax.swing.SwingUtilities;

import org.apache.log4j.*;
import com.johnmunsch.rss.*;

/**
 *
 * @author  JMunsch
 */
class HSChannelStoreListener implements ChannelStoreListener {
    private static Category cat = Category.getInstance(
        HSChannelStoreListener.class.getName());
    
    private ItemStore itemStore = null;

    public HSChannelStoreListener(ItemStore _itemStore) {
        itemStore = _itemStore;
    }
    
    public void preAdd(ChannelStoreEvent evt) {
    }
    
    public void postAdd(ChannelStoreEvent evt) {
    }
    
    public void preRemove(ChannelStoreEvent evt) {
    }
    
    public void postRemove(ChannelStoreEvent evt) {
    }
    
    public void preClear(ChannelStoreEvent evt) {
    }
    
    public void postClear(ChannelStoreEvent evt) {
    }
    
    public void preRetrieve(ChannelStoreEvent evt) {
    }
    
    public void retrieved(ChannelStoreEvent evt, Channel channel, int value, 
        int maximum) {
            
        // System.err.println("Channel: " + channel.getTitle() + " " + value + "/" + maximum);

        HSMain.getInstance().setLabel(channel.getTitle());
        HSMain.getInstance().setRange(maximum);
        HSMain.getInstance().setValue(value);

        Runnable updateUI = new Runnable() {
            public void run() {
                HSMain.getInstance().updateStatusAndProgress();
            }
        };
 
        SwingUtilities.invokeLater(updateUI);
    }
    
    public void postRetrieve(ChannelStoreEvent evt) {
        if (evt.getPending().size() > 0) {
            // Then we add them to our item store.
            itemStore.add(evt.getPending());

            // The following functionality could actually be added to the
            // item store listener so it could handle the response to an update
            // in the model. That would be a better separation of model and
            // view code.

            // We added some items during the retrieval process. Let's play a
            // sound.
            if (new Boolean(HSSettings.getInstance().getProperties().getProperty(
                "sound.newItems", "false")).booleanValue()) {

                Misc.playSound("/com/johnmunsch/hotsheet/resources/NewNews.wav");
            }

            // Then we prompt the user interface to tell it to update because
            // there are changes in the model.
            Runnable updateUI = new Runnable() {
                public void run() {
                    HSMain.getInstance().updateItemList();
                }
            };

            SwingUtilities.invokeLater(updateUI);
        }

        HSMain.getInstance().setLabel("");
        HSMain.getInstance().setValue(0);
        HSMain.getInstance().setRange(0);
        
        HSMain.getInstance().updateStatusAndProgress();
        HSMain.getInstance().allowRefreshing(true);
    }
}
