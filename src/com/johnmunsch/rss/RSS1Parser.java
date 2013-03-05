/*
 * RSS1Parser.java
 *
 * Created on March 29, 2001, 8:48 PM
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
 * A parser for the RDF based RSS 1.0 format. Although it sounds like it has 
 * something to do with the original RSS, it is actually separate and evolving
 * separately. 
 *
 * @author  John Munsch
 */
class RSS1Parser {
    private static Category cat = Category.getInstance(
        RSS1Parser.class.getName());
    
    static List parse(Element root, Channel channel, 
        boolean updateChannel) throws java.net.MalformedURLException {

        LinkedList itemList = new LinkedList();

        // Now look for the channel nodes within this file.
        NodeList nodes = root.getChildNodes();

        int numNodes = nodes.getLength();

        for (int i = 0; i < numNodes; i++) {
            Node child = nodes.item(i);

            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    String childName = child.getNodeName();

                    if (childName.equals("channel")) {
                        parseChannel(child, channel, updateChannel);
                    } else if (childName.equals("image")) {
                        parseImage(child, channel, updateChannel);
                    } else if (childName.equals("item")) {
                        Item item = parseItem(child, channel);

                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    break;
            }
        }

        // Lastly, let's extract all the items within this file. Each of
        // the items will have a separate item object created for it and
        // it will be populated with data from this file.

        return (itemList);
    }

    private static void parseChannel(Node node, Channel channel, 
        boolean updateChannel) throws java.net.MalformedURLException {
            
        NodeList channelNodes = node.getChildNodes();

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

                        if (updateChannel) {
                            channel.setDescription(textNode.getNodeValue());
                        }
                    } else if (childName.equals("language")) {
                        Node textNode = child.getFirstChild();

                        if (updateChannel) {
                            channel.setLanguage(textNode.getNodeValue());
                        }
                    }
            }
        }
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

                        if (updateChannel) {
                            channel.setImageURL(new URL(textNode.getNodeValue()));
                        }
                    } else if (childName.equals("link")) {
                        Node textNode = child.getFirstChild();
                        
                        if (updateChannel) {
                            channel.setImageLink(new URL(textNode.getNodeValue()));
                        }
                    } else if (childName.equals("description")) {
                        Node textNode = child.getFirstChild();

                        if (updateChannel) {
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
                        title = textNode.getNodeValue();
                    } else if (childName.equals("description")) {
                        Node textNode = child.getFirstChild();
                        description = textNode.getNodeValue();
                    } else if (childName.equals("link")) {
                        Node textNode = child.getFirstChild();
                        link = textNode.getNodeValue();
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
            cat.error("Channel Error: " + channel.getURI());
            cat.error("Insufficient information to build an item.");

            return (null);
        }
    }
}
