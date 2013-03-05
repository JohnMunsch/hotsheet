import java.io.*;
import org.jdom.*; 
import org.jdom.input.*;
import org.jdom.output.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;

/**
 * Class that parses an OCS file extract & creates a xml file in my own XML format
 */
public class ParseUse1 {
    Namespace hs = Namespace.getNamespace("hs", "http://www.hotsheet.com");
    Namespace dc = Namespace.getNamespace("dc", "http://purl.org/metadata/dublin_core#");
    Namespace rdf = Namespace.getNamespace("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    Namespace ocs = Namespace.getNamespace("ocs", "http://alchemy.openjava.org/ocs/directory#");

    /**
     * Method gets created document.. Manages document processing
     * Handle output.
     */
    public void inter() {
        try {
            //Create document for my file.
            Document my=createDocument(hs);

            my = parseOCS(my);
            Thread.sleep(1000);
            my = parseULand(my);

            XMLOutputter outputter = new XMLOutputter("   ", true);
            outputter.output(my, System.out);//To Console
            FileOutputStream fos=new FileOutputStream("harshad.xml");
            outputter.output(my,fos);//To File
            fos.close();
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        } //end catch
    } // end inter()

    /**
     * Parses an OCS file
     * @param   my  
     * @return  Document  
     * @exception   JDOMException  
     * @exception   IOException  
     * @exception   FileNotFoundException  
     */
    public Document parseOCS(Document my) throws JDOMException, 
        IOException, FileNotFoundException {

        // Assume filename argument
        String filename = "OCSExtract.rdf";

        //Building with SAXBuilder without validation
        //SAXBuilder is the recommended way for building JDOM documents

        SAXBuilder b = new SAXBuilder();
        // Create document for OCS
        Document doc = b.build(new File(filename));

        Element myRoot = my.getRootElement();

        //Get root element
        Element ele = doc.getRootElement();

        //Get all description tags into a list
        List titleChildren = ele.getChildren("description",rdf);

        System.out.println("Size of List is "+ titleChildren.size());

        //Use Iterator to go thru each element
        Iterator Li = titleChildren.iterator();

        while (Li.hasNext()) {
            Element el = ((Element)Li.next());//Cast into Element

            Element myel = new Element("channel",hs);
            myel.setAttribute("updateCount","3");

            Element myel1 = new Element("channelTitle",hs);
            Element myel2 = new Element("channelLink",hs);

            Attribute attr = el.getAttribute("about");
            myel2.setText(attr.getValue());

            if (el.getChild("title", dc) != null) {
                Element dcChild = el.getChild("title",dc);
                myel1.setText(dcChild.getTextTrim());
            }

            //Creating my XML structure
            myRoot.addContent(myel);
            myel.addContent(myel1);
            myel1.addContent(myel2);
        }    //end while

        return my;
    }//end ParseOCS()

    /**
     * Parses a Userland file servExtract.xml
     * @param   my
     * @return  Document
     * @exception   JDOMException
     * @exception   IOException
     * @exception   FileNotFoundException
     */
    public Document parseULand(Document my) throws JDOMException,IOException,FileNotFoundException {
        // Assume filename argument
        String filename = "servExtract.xml";

        //Building with SAXBuilder without validation
        SAXBuilder b = new SAXBuilder();
        
        // Create document for Userland Format
        Document doc = b.build(new File(filename));

        Element myRoot = my.getRootElement();

        //Get root element
        Element ele = doc.getRootElement();
        
        if (ele.getChild("services") != null) {
            Element services=ele.getChild("services");

            if (services.getChild("service") != null) {
                //Get all description tags into a list
                List titleChildren = services.getChildren("service");
                System.out.println("Size of List is " + titleChildren.size());
                
                //Use Iterator to go thru each element
                Iterator Li = titleChildren.iterator();

                while (Li.hasNext()) {
                    Element el = ((Element)Li.next());//Cast into Element

                    Element myel = new Element("channel",hs);
                    myel.setAttribute("updateCount","3");

                    Element myel1 = new Element("channelTitle",hs);
                    Element myel2 = new Element("channelLink",hs);

                    if (el.getChild("description") != null) {
                        Element descChild = el.getChild("description");
                        myel1.setText(descChild.getTextTrim());
                    }

                    if (el.getChild("url") != null) {
                        Element urlChild = el.getChild("url");
                        myel2.setText(urlChild.getTextTrim());
                    }

                    //Creating my XML structure
                    myRoot.addContent(myel);
                    myel.addContent(myel1);
                    myel1.addContent(myel2);
                }    //end while
            }//end service if
        }//end services if

        return my;
    }//end ParseULand()

    /**
     * Method that creates new Document
     * @param   hs
     * @return  Document
     */
    public static Document createDocument(Namespace hs) {
        // Create the root element
        // Namespace hs=Namespace.getNamespace("hs", "http://www.hotsheet.com#");
        Element channelStore = new Element("channelStore",hs);
        
        //create the document
        Document myDocument = new Document(channelStore);
        
        return myDocument;
    }

    /**
     * Main method
     * @param   args  
     */
    public static void main(String[] args) {
        ParseUse1 pu=new ParseUse1();
        pu.inter();
    } //end main
}//end class
