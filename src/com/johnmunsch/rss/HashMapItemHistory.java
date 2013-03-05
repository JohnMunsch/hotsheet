/*
 * ItemHistory.java
 *
 * Created on July 11, 2001, 2:54 PM
 */
package com.johnmunsch.rss;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * The item history keeps the track of items that have been retrieved for some
 * period of time.
 *
 * @author  John Munsch
 */
public class HashMapItemHistory implements ItemHistory, Serializable {
    private HashMap historyMap = new HashMap();
    private int daysToKeep = 30;
    
    public boolean inHistory(Item item) {
        // Generate a unique key for the news item.
        String itemDigest = createDigest(item);

        // If we can find the item digest already in our history then we
        // discard the item. If we can't find it then we add it to the 
        // history.
        if (historyMap.containsKey(itemDigest)) {
            return true;
        } else {
            // Add the item to the history along with the date the item was
            // retrieved (so we can remove the item later and our history 
            // won't grow forever).
            historyMap.put(itemDigest, item.getRetrieved());
            
            return false;
        }
    }

    public void load(File f) {
        try {
            FileInputStream inputStream = new FileInputStream(f);

            // If we find the file mentioned when we constructed this object we 
            // need to serialize a linked list out of that file and use it as 
            // our existing item list.
            ObjectInputStream in = new ObjectInputStream(inputStream);
            historyMap = (HashMap) in.readObject();

            in.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (ClassNotFoundException cnfe) {
            System.err.println(cnfe);
        }
    }
    
    public void store(File f) {
        try {
            // Serialize our linked list out to a file.
            ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(f));
            out.writeObject(historyMap);

            // Also flushes output
            out.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                                          Helper Functions
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates an MD5 hash of the title and the link for a given news item.
     * Since MD5 is a very good one way hash of some data, this should not match
     * up by accident with any other news item that has a different title and
     * data.
     *
     * We go with hashes because they are shorter and thus the comparisons will
     * be quicker and also because they are smaller than the links and titles
     * they stand in for, so we don't have to keep as much data.
     */
    private String createDigest(Item item) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            
            md5.update(item.getTitle().getBytes());
            md5.update(item.getLink().toString().getBytes());
            
            return (new String(md5.digest()));
        } catch (NoSuchAlgorithmException nsae) {
            return (null);
        }
    }
    
    /**
     * Delete old items from the history.
     */
    private void purge() {
        Calendar temp = Calendar.getInstance();
        temp.add(Calendar.DATE, -daysToKeep);
        Date cutoff = temp.getTime();
        
        // Go through the history with an iterator and remove all the items 
        // which have a date older than our general expiration date.
        Iterator historyIterator = historyMap.entrySet().iterator();

        while (historyIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) historyIterator.next();
            
            if (((Date) entry.getValue()).before(cutoff)) {
                historyIterator.remove();
            }
        }
    }
}
