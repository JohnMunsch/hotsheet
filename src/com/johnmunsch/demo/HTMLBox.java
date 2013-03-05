/*
 * HTMLBox.java
 *
 * Created on May 4, 2001, 9:16 AM
 */
package com.johnmunsch.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import com.johnmunsch.rss.*;

/**
 *
 * @author  John Munsch
 */
public class HTMLBox {
    /** Creates new HTMLBox */
    public HTMLBox(Channel channel, List list, String filename, 
        boolean generateJavaScript) {

        // Note: This code could probably be cleaned up a lot by using the 
        // Element Construction Set from Apache 
        // (http://jakarta.apache.org/ecs/index.html). But I didn't want the 
        // examples to depend on any outside libraries or code other than JAXP 
        // and the JDK.
        StringBuffer output = new StringBuffer();
        
        if (generateJavaScript) {
            output.append("document.writeln(\"");
        }

        output.append("<table class='block'>");
        output.append("<tr><td class='channelTitle'>");
        output.append(channel.getTitle());
        output.append("</td></tr>");
        if (!channel.getImageURL().toString().equals("")) {
            output.append("<tr><td align='center'><img src=\'");
            output.append(channel.getImageURL().toString());
            output.append("\'></td></tr>");
        }
        output.append("<tr><td>");
        
        // Create an iterator for the list of items retrieved and iterate
        // through them. Print out a string representation of each item to show
        // what was parsed from the RSS file.
        Iterator i = list.iterator();
        
        while (i.hasNext()) {
            Item tempItem = (Item) i.next();
            
            output.append("<a class='itemTitle' href=\'");
            output.append(tempItem.getLink().toString());
            output.append("\'>");
            output.append(tempItem.getTitle());
            output.append("</a><br>");
        }
        
        output.append("</td></tr>");
        output.append("</table>");

        if (generateJavaScript) {
            output.append("\");");
        }
        
        // Open the target file and write the string to it.
        try {
            FileOutputStream ostream = new FileOutputStream(new File(filename));
            ostream.write(output.toString().getBytes());
            ostream.close();
        } catch (java.io.FileNotFoundException fnfe) {
        } catch (java.io.IOException ioe) {
        }
    }

    /**
     * A more sophisticated example of using the RSS classes. This example loads
     * up an RSS channel from a file and then generates an HTML or JavaScript 
     * representation of that channel.
     *
     * JavaScript is an option because the resulting file can be copied onto a
     * web server and referred to by another HTML file as an external
     * JavaScript. Then users would download it separately from a main page that
     * would refer to it and they would get up to date insertions of news onto
     * a page.
     *
     * For example you would insert this into an HTML page:
     *     <SCRIPT LANGUAGE="JavaScript" SRC="/headlines/entertainment.js"></SCRIPT>
     *
     * Then you would use this HTMLBox to generate the JavaScript file
     * entertainment.js. That file could be updated and copied to the server
     * on a regular basis and the headlines on the HTML page would seem to
     * update regularly even on websites where the webpages are not being served
     * up using dynamic web page technologies like JSP, ASP, or PHP.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        boolean generateJavaScript = true;

        if (args.length < 2) {
            // Output usage instructions.
            System.out.println("HTMLBox source destination");
            System.out.println("  source         The URL of an RSS channel");
            System.out.println("  destination    The name of the file to contain the HTML or JavaScript");
            
            System.exit(1);
        }

        // First we have to create a channel. We supply the URI from which the
        // RSS data will be retrieved.
        Channel channel = new Channel(args[0]);
        
        // Then we retrieve it. The boolean value indicates whether the channel
        // object should be updated with new data based upon the RSS that is
        // retrievfsed (i.e. channel title, main page URL, a channel descriptiong,
        // and image are all generally available in most RSS files). This will
        // normally be set to true but you could skip it if you didn't need the
        // data.
        List list = channel.retrieve(true);
        
        // Generate the file.
        HTMLBox htmlBox = new HTMLBox(channel, list, args[1], generateJavaScript);
        
        System.exit(0);
    }
}
