/*
 * HelloRSS.java
 *
 * Created on May 3, 2001, 4:05 PM
 */
package com.johnmunsch.demo;

import java.util.Iterator;
import java.util.List;

import com.johnmunsch.rss.*;

/**
 *
 * @author  John Munsch
 */
public class HelloRSS {
    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        String rssLocation = "http://www.gamedev.net/xml";
        
        if (args.length > 0) {
            rssLocation = args[0];
        }
        
        // First we have to create a channel. We supply the URI from which the
        // RSS data will be retrieved.
        Channel channel = new Channel(rssLocation);
        
        // Then we retrieve it. The boolean value indicates whether the channel
        // object should be updated with new data based upon the RSS that is
        // retrieved (i.e. channel title, main page URL, a channel descriptiong,
        // and image are all generally available in most RSS files). This will
        // normally be set to true but you could skip it if you didn't need the
        // data.
        List list = channel.retrieve(true);
        
        // Create an iterator for the list of items retrieved and iterate
        // through them. Print out a string representation of each item to show
        // what was parsed from the RSS file.
        Iterator i = list.iterator();
        
        while (i.hasNext()) {
            System.out.println(i.next().toString());
        }
        
        System.exit(0);
    }
}
