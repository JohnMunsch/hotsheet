/*
 * Item.java
 *
 * Created on March 19, 2001, 12:44 PM
 */
package com.johnmunsch.rss;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.*;

/**
 * Represents a single item from an RSS channel. Normally items consist of a
 * pair or triplet of a title for the item, the link to that item, and 
 * optionally a description of the item.
 *
 * @author John Munsch
 */
public class Item implements Serializable {
    private String title;
    private URL link = null;
    private String description = new String();
    private Channel origin;
    private Date retrieved = new Date();
    private Properties properties = null;
    
    private static Category cat = Category.getInstance(Item.class.getName());

    /** 
     * Creates a new Item with the minimal expected data.
     */
    public Item(Channel _origin, String _title, String _link) {
        origin = _origin;
        title = _title;
        try {
            link = new URL(_link);
        } catch (MalformedURLException mue) {
            cat.error(mue);
        }
    }

    /** 
     * Creates new Item with a description. 
     */
    public Item(Channel _origin, String _title, String _link, 
        String _description) {

        origin = _origin;
        title = _title;
        try {
            link = new URL(_link);
        } catch (MalformedURLException mue) {
            cat.error(mue);
        }
        description = _description;
    }

    /**
     * Creates a new Item with a description and a retrieval date. 
     */
    public Item(Channel _origin, String _title, String _link, 
        String _description, Date _retrieved) {

        origin = _origin;
        title = _title;
        try {
            link = new URL(_link);
        } catch (MalformedURLException mue) {
            cat.error(mue);
        }
        description = _description;
        retrieved = _retrieved;
    }

    /**
     * Indicates whether some other object is "equal to" this one. This should
     * have all the "Object" class meanings for this function.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            Item rValue = (Item) obj;

            if (title.equals(rValue.title) && link.equals(rValue.link) &&
                description.equals(rValue.description)) {

                return (true);
            }
        }

        return (false);
    }

    /**
     * Generates a reasonably unique hash code for this item. Currently it uses
     * the hash codes for the title and the link added together.
     */
    public int hashCode() {
        // Return a semi-unique hash code for this item.
        return (title.hashCode() + link.hashCode());
    }

    /**
     * Generates a nicely formatted string representation of this item.
     */
    public String toString() {
        StringBuffer returnValue = new StringBuffer("   Item from " + 
            origin.getTitle() + "\n");
        returnValue.append("============\n");
        returnValue.append("      Title: " + title + "\n");
        returnValue.append("       Link: " + link.toString() + "\n");

        if (description != null) {
            returnValue.append("Description: " + description + "\n");
        }
        returnValue.append("  Retrieved: " + retrieved.toString() + "\n");

        returnValue.append("\n");

        return (returnValue.toString());
    }

    /**
     * Returns the channel from which this object was generated.
     */
    public Channel getChannel() {
        return (origin);
    }

    /**
     * Returns the date and time this item was retrieved.
     */
    public Date getRetrieved() {
        return (retrieved);
    }

    /**
     * Returns the properties set for this object or null if no properties have
     * been set. Most of the functionality for dealing with the properties on an
     * item have to be accessed through this object. Only the most basic gets
     * and sets have been provided directly on the item's interface.
     */
    public Properties getProperties() {
        return (properties);
    }

    /**
     * Provides direct access to a getProperty function on the Properties
     * object.
     */
    public String getProperty(String key) {
        if (properties == null) {
            return (null);
        } else {
            return (properties.getProperty(key));
        }
    }
    
    /**
     * Provides direct access to a getProperty function on the Properties
     * object.
     */
    public String getProperty(String key, String defaultValue) {
        if (properties == null) {
            return (defaultValue);
        } else {
            return (properties.getProperty(key, defaultValue));
        }
    }
    
    /**
     * Provides direct access to the setProperty function on the Properties
     * object.
     */
    public Object setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        
        return (properties.setProperty(key, value));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    //                                               Accessors for RSS item data
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get the title of this item.
     */
    public String getTitle() {
        return (title);
    }

    /**
     * Get a link to the item. There is a small chance this could be null if
     * the URL string for this item was malformed and could not be turned into
     * a URL object.
     */
    public URL getLink() {
        return (link);
    }

    /**
     * The description (if any) for this item. This is frequently an empty 
     * string.
     */
    public String getDescription() {
        return (description);
    }
}
