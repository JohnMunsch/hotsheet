/*
 * Channel.java
 *
 * Created on March 19, 2001, 12:34 PM
 */
package com.johnmunsch.rss;

import java.awt.MediaTracker;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.*;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * By using a replacement entity resolver we can get around a problem that
 * occurs if Netscape again decides to remove the RSS DTD file. It happened
 * once before and it brought most RSS tools with validating parsers to a
 * grinding halt because they could not validate against a missing DTD. The
 * entity resolver fixes that and for 0.91 version files (the largest number) it
 * auto substitutes a local copy of the DTD to be used. This of course also has
 * the side effect of speeding things up because the DTD does not have to be
 * retrieved over and over again.
 */
class FixResolver implements EntityResolver {
    private static Category cat = Category.getInstance(
        FixResolver.class.getName());

    /**
     * @param publicId
     * @param systemId
     *
     * @return  If null is returned then the entity will be resolved normally
     * (i.e. looked up on the WWW). If not, then we have done a substitution for
     * an entity that we recognized.
     */    
    public InputSource resolveEntity(String publicId, String systemId) {
        if (systemId.toLowerCase().equals(
            "http://my.netscape.com/publish/formats/rss-0.91.dtd")) {

            // return a special input source
            try {
                InputSource returnValue = new InputSource(new InputStreamReader(
                    getClass().getResource("/com/johnmunsch/rss/resources/rss-0.91.dtd").openStream()));
                return (returnValue);
            } catch (java.io.IOException ioe) {
                cat.error(ioe);
                return (null);
            }
        } else {
            // use the default behaviour
            return null;
        }
    }
}

/**
 * Channel is the fundamental element of RSS library. You must have a channel
 * before you can do anything because you must have an address from which the
 * RSS data will be retrieved.
 *
 * Once you have a channel and that channel is associated with a URI you can
 * retrieve the channel and get its current set of items.
 *
 * @author  johnm
 */
public class Channel implements Serializable {
    private String uri = new String();
    private Date lastRetrieved = null;
    private ImageIcon imageIcon = null;

    // From the RSS file itself.
    private String title = new String();
    private URL link = null;
    private String description = new String();
    private String language = new String();

    private URL imageURL = null;
    private URL imageLink = null;
    private String imageDescription = new String();

    private ItemHistory itemHistory = null;
    
    private static Category cat = Category.getInstance(Channel.class.getName());

    /** 
     * Creates a bare channel object.
     */
    public Channel() {
    }
    
    /**
     * Creates a new Channel object and fills in a URI for it to use.
     *
     * @param _uri The address from which this RSS channel may be pulled.
     */
    public Channel(String _uri) {
        setURI(_uri);
    }

    /**
     * This constructor is mainly useful for those times when we already
     * have complete information on the channel at the time we create it.
     * For example, if we are creating channels from a list of all
     * available channels (say an OCS list from syndic8.com) then we
     * already know what the name of the channel is when we create it.
     *
     * @param _uri The address from which this RSS channel may be pulled.
     * @param _title The title of this RSS channel.
     * @param _link A link to the homepage for the channel.
     * @param _description A description of the channel.
     * @param _language The channel's language.
     * @param _imageURL The URL for an image (if any) that may be used to
     * represent the channel.
     * @param _imageLink What the image links to (may be different from the 
     * channel link above.
     * @param _imageDescription A description associated with the image.
     */    
    public Channel(String _uri, String _title, URL _link, String _description,
        String _language, URL _imageURL, URL _imageLink, 
        String _imageDescription) {

        setURI(_uri);
        setTitle(_title);
        setLink(_link);
        setDescription(_description);
        setLanguage(_language);
        setImageURL(_imageURL);
        setImageLink(_imageLink);
        setImageDescription(_imageDescription);
    }

    /**
     * The URI is what makes a channel a channel. It refers to the location of
     * an RSS channel. All the rest of the data about a channel can actually be
     * parsed from the RSS XML data after it is retrieved.
     */
    public String getURI() {
        return uri;
    }

    /**
     * This allows us to update the URI of a given channel.
     */
    public String setURI(String _uri) {
        uri = _uri;

        return uri;
    }

    /**
     * Returns the last date when this channel was _successfully_ parsed. That
     * is, attempts to call parse that don't work because the channel is 
     * unavailable or returns unparsable data do not reset this value to a new
     * date. Only a successful attempt to parse will change this date.
     *
     * @return Returns the date this channel was last successfully parsed or 
     * null if it has never been parsed successfully.
     */
    public Date getLastRetrieved() {
        return lastRetrieved;
    }

    /**
     * If a channel has an associated image (an optional element) then we can
     * get it in ImageIcon form ready for drawing from this function. If the
     * channel does not have an associated image or if the channel has never
     * been retrieved then this function will return null.
     */
    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    /**
     * If the channel has an item history associated with it, it can eliminate
     * items that it has seen before whenever it does a retrieval. Without it
     * redundant items will appear.
     *
     * Note: Usually one item history will be shared by multiple channel 
     * objects. You do not have to create a new one for each channel.
     */
    public ItemHistory getItemHistory() {
        return itemHistory;
    }

    /**
     * If the channel has an item history associated with it, it can eliminate
     * items that it has seen before whenever it does a retrieval. Without it
     * redundant items will appear.
     *
     * This function will allow you to set the item history that this particular
     * channel will use. Set it to null if you want to turn off history 
     * checking.
     *
     * Note: Usually one item history will be shared by multiple channel 
     * objects. You do not have to create a new one for each channel.
     */
    public ItemHistory setItemHistory(ItemHistory _itemHistory) {
        itemHistory = _itemHistory;
        
        return itemHistory;
    }
    
    /**
     * Two channels are treated as being equal if they both are retrieved from
     * the same URI. So even if the name of one channel has been changed or they
     * have retrieved different images, if the URIs are the same, they are
     * considered to <i>be</i> the same.
     */
    public boolean equals(Object obj) {
        if ((obj == this) || ((obj instanceof Channel) && 
            this.uri.equals(((Channel) obj).uri))) {

            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Genereates a nicely formatted string version of this object. Very handy
     * for debugging or sanity checking your work.
     */
    public String toString() {
        return uri;
    }

    public String toString2() {
        StringBuffer returnValue = new StringBuffer("Channel Info\n");
        returnValue.append("============\n");
        returnValue.append("        URI: " + uri + "\n");
        returnValue.append("      Title: " + title + "\n");
        
        if (link != null) {
            returnValue.append("       Link: " + link.toString() + "\n");
        }
        
        returnValue.append("Description: " + description + "\n");
        returnValue.append("   Language: " + language + "\n");
        
        if (imageURL != null) {
            returnValue.append("      Image: " + imageURL.toString() + "\n");
        }
        
        returnValue.append("\n");
        
        return returnValue.toString();
    }

    /**
     * Retrieves the current set of items for an RSS channel. This list may
     * include items which were retrieved before. It is up to the caller to
     * eliminate duplicates if necessary (or to use the duplicate elimination
     * facilities of the item store).
     */
    public List retrieve(boolean updateChannel) {
        List returnValue = new LinkedList();

        try {
            Document document;
            DocumentBuilderFactory docBuilderFactory = 
                DocumentBuilderFactory.newInstance();
            docBuilderFactory.setIgnoringElementContentWhitespace(true);
            docBuilderFactory.setIgnoringComments(true);
            
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new FixResolver());

            // As odd as it seems, there is no version of parse that will take a
            // URL object so using the string version seemed to make the most 
            // sense.
            document = docBuilder.parse(uri);
            
            // Get the root node for RSS.
            Element root = document.getDocumentElement();
            
            // Confirm that this is an RSS 0.90, 0.91 or 0.92 before proceeding.
            if (root.getNodeName().equals("rss")) {
                NamedNodeMap attributes = root.getAttributes();
                
                Node versionNode = attributes.getNamedItem("version");
                String versionValue = versionNode.getNodeValue();
                
                if (versionValue.equals("0.90") || 
                    versionValue.equals("0.91") || 
                    versionValue.equals("0.92")) {

                    // Call the regular RSS parser.
                    returnValue = RSSParser.parse(root, this, 
                        updateChannel);
                }
            } else if (root.getNodeName().equals("rdf:RDF")) {
                // Call the RSS 1.0 parser.
                returnValue = RSS1Parser.parse(root, this, updateChannel);
            }

            // Remove from the items we just retrieved any that we have
            // retrieved in the past.
            if (itemHistory != null) {
                Iterator itemIterator = returnValue.iterator();

                while (itemIterator.hasNext()) {
                    Item item = (Item) itemIterator.next();

                    if (itemHistory.inHistory(item)) {
                        itemIterator.remove();
                    }
                }
            }

            // Record this as the time of last retrieval.
            lastRetrieved = new Date();
        } catch (SAXParseException spe) {
            cat.error("Channel Error: " + uri);
            cat.error("Parsing error, line " + spe.getLineNumber()
                + ", uri " + spe.getSystemId(), spe);
        } catch (SAXException se) {
            Exception x = se.getException();

            cat.error("Channel Error: " + uri);
            cat.error((x == null) ? se : x, se);
        } catch (Exception e) {
            cat.error("Channel Error: " + uri);
            cat.error(e, e);
        }

        return returnValue;
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                            Accessors for RSS channel data
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Gets the title of an RSS channel (this is usually the name of the website
     * which generated the channel). May return a blank string if the
     * channel has never been retrieved.
     */
    public String getTitle() {
        return (title);
    }

    /**
     * Allows setting the title of an RSS channel.
     */
    public String setTitle(String _title) {
        title = _title;

        return (title);
    }

    /**
     * Returns the main link for an RSS channel (this is usually the address
     * of the main page for the website that generated the channel). May return
     * null if this the channel has never been retrieved.
     */
    public URL getLink() {
        return (link);
    }

    /**
     * Allows setting the link for an RSS channel.
     */
    public URL setLink(URL _link) {
        link = _link;
        
        return (link);
    }

    /**
     * A description of the RSS channel. This may be an empty string if the
     * channel has never been retrieved.
     */
    public String getDescription() {
        return (description);
    }

    /**
     * Allows setting the description of the RSS channel.
     */
    public String setDescription(String _description) {
        description = _description;
        
        return (description);
    }
    
    /**
     * Returns a string describing the language of the RSS channel. This is
     * not currently filled in by the parser and will always return an empty
     * string.
     */
    public String getLanguage() {
        return (language);
    }
    
    /**
     * Allows setting the language of the RSS channel.
     */
    public String setLanguage(String _language) {
        language = _language;
        
        return (language);
    }
    
    /**
     * Gets the URL of the image (if any) for this RSS channel. The image is
     * optional for an RSS channel so this may return null. It may also return
     * null if the channel has never been retrieved.
     *
     * Note: If you are planning to draw the image then getImageIcon may be
     * a more useful function to call.
     */
    public URL getImageURL() {
        return (imageURL);
    }
    
    /**
     * Allows setting the image URL for the RSS channel.
     */
    public URL setImageURL(URL _imageURL) {
        imageURL = _imageURL;

        // Whenever a new value is assigned to the imageURL and that value is
        // non-null we retrieve the image associated with it and cache it for
        // use by any program that might want to draw the channel's icon.
        if (imageURL != null) {
            imageIcon = new ImageIcon(imageURL);
            if (imageIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                imageIcon = null;
            }
        }

        return (imageURL);
    }
    
    /**
     * Gets the address that the image should link to for the channel. RSS
     * allows a separate link for the channel and the image for a channel.
     * Note that this may return null if the channel has never been retrieved.
     */
    public URL getImageLink() {
        return (imageLink);
    }
    
    /**
     * Allows setting the address that the image should line to for the RSS
     * channel.
     */
    public URL setImageLink(URL _imageLink) {
        imageLink = _imageLink;
        
        return (imageLink);
    }
    
    /**
     * Gets any description for the image. RSS allows a description and link
     * for the image vs. the channel. This may return an empty string if the
     * channel has never been retrieved.
     */
    public String getImageDescription() {
        return (imageDescription);
    }
    
    /**
     * Allows setting the image description for the RSS channel.
     */
    public String setImageDescription(String _imageDescription) {
        imageDescription = _imageDescription;
        
        return (imageDescription);
    }
}
