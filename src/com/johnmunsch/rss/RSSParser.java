/*
 * RSSParser.java
 *
 * Created on March 19, 2001, 12:37 PM
 */
package com.johnmunsch.rss;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A parser that is capable of parsing RSS 0.90 and 0.91 .
 *
 * @author  johnm
 */
class RSSParser {
    private static Category cat = Category.getInstance(
        RSSParser.class.getName());
    
    /**
     * Exception handling needs serious work. We need to pass out a variety of
     * errors related to bad XML and bad RSS document structure. These can then
     * be displayed, logged, or ignored as needed.
     *
     * @param channel The channel to be retrieved and parsed.
     * @param updateChannel  This is needed so we can update our information 
     * with the current channel link, etc. All of these can change over time 
     * for a given channel and we want to have current data.
     */
    static List parse(Element root, Channel channel, 
        boolean updateChannel) throws java.net.MalformedURLException {

        LinkedList itemList = new LinkedList();

        // Now look for nodes at the channel level within this file.
        NodeList channels = root.getChildNodes();

        int numChannels = channels.getLength();

        for (int c = 0; c < numChannels; c++) {
            Node channelNode = channels.item(c);

            if ((channelNode.getNodeType() == Node.ELEMENT_NODE) && 
                (channelNode.getNodeName().equals("channel"))) {

                NodeList channelNodes = channelNode.getChildNodes();

                int numChannelNodes = channelNodes.getLength();

                for (int i = 0; i < numChannelNodes; i++) {
                    Node child = channelNodes.item(i);

                    switch (child.getNodeType()) {
                        case Node.ELEMENT_NODE:
                            String childName = child.getNodeName();
                            if (childName.equals("title")) {
                                Node textNode = child.getFirstChild();

                                if (updateChannel) {
                                    channel.setTitle(textNode.getNodeValue());
                                }
                            } else if (childName.equals("link")) {
                                Node textNode = child.getFirstChild();

                                if (updateChannel) {
                                    channel.setLink(new URL(textNode.getNodeValue()));
                                }
                            } else if (childName.equals("description")) {
                                Node textNode = child.getFirstChild();

                                if (updateChannel && (textNode != null)) {
                                    channel.setDescription(textNode.getNodeValue());
                                }
                            } else if (childName.equals("language")) {
                                Node textNode = child.getFirstChild();

                                if (updateChannel) {
                                    channel.setLanguage(textNode.getNodeValue());
                                }
                            } else if (childName.equals("image")) {
                                // The image associated with the channel has
                                // its own set of subnodes. We pass parsing it
                                // off to another method just to keep our code
                                // a little simpler.
                                parseImage(child, channel, updateChannel);
                            } else if (childName.equals("item")) {
                                // Like the image node, the item node has its
                                // own set of subnodes so we farm parsing it off
                                // to its own method.
                                Item item = parseItem(child, channel);

                                if (item != null) {
                                    itemList.add(item);
                                }
                            }
                            break;
                    }
                }
            } // if 
        } // for

        return (itemList);
    }

    private static void parseImage(Node node, Channel channel, boolean updateChannel) 
        throws MalformedURLException {

        NodeList imageNodes = node.getChildNodes();

        int numImageNodes = imageNodes.getLength();

        for (int i = 0; i < numImageNodes; i++) {
            Node child = imageNodes.item(i);

            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    String childName = child.getNodeName();

                    if (childName.equals("url")) {
                        Node textNode = child.getFirstChild();

                        if (updateChannel && (textNode != null)) {
                            channel.setImageURL(new URL(textNode.getNodeValue()));
                        }
                    } else if (childName.equals("link")) {
                        Node textNode = child.getFirstChild();
                        
                        if (updateChannel && (textNode != null)) {
                            channel.setImageLink(new URL(textNode.getNodeValue()));
                        }
                    } else if (childName.equals("description")) {
                        Node textNode = child.getFirstChild();

                        if (updateChannel && (textNode != null)) {
                            channel.setImageDescription(textNode.getNodeValue());
                        }
                    }
            }
        }
    }
    
    private static Item parseItem(Node itemNode, Channel channel) {
        String title = null;
        String description = null;
        String link = null;

        NodeList itemNodes = itemNode.getChildNodes();
        int numItemNodes = itemNodes.getLength();

        for (int i = 0; i < numItemNodes; i++) {
            Node child = itemNodes.item(i);

            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    String childName = child.getNodeName();
                    if (childName.equals("title")) {
                        Node textNode = child.getFirstChild();

                        if (textNode != null) {
                            title = textNode.getNodeValue();
                        } else {
                            title = "";
                        }
                    } else if (childName.equals("description")) {
                        Node textNode = child.getFirstChild();

                        if (textNode != null) {
                            description = textNode.getNodeValue();
                        } else {
                            description = "";
                        }
                    } else if (childName.equals("link")) {
                        Node textNode = child.getFirstChild();

                        if (textNode != null) {
                            link = textNode.getNodeValue();
                        } else {
                            link = "";
                        }
                    }
                    break;
            }
        }

        if ((title != null) && (link != null)) {
            if (description == null) {
                return (new Item(channel, title, link));
            } else {
                return (new Item(channel, title, link, description));
            }
        } else {
            // Insufficient information to build an item.
            return (null);
        }
    }
}
