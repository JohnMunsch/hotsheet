/*
 * ItemComparator.java
 *
 * Created on November 9, 2001, 2:16 PM
 */
package com.johnmunsch.rss;

import java.util.Date;

/**
 * 
 *
 * @author John Munsch
 */
public class ItemComparator implements java.util.Comparator {
    private int firstAttribute = ItemStore.NONE;
    private int secondAttribute = ItemStore.NONE;
    private int thirdAttribute = ItemStore.NONE;
    
    /** Creates new ItemComparator */
    public ItemComparator(int _firstAttribute, int _secondAttribute, 
        int _thirdAttribute) {
        
        firstAttribute = _firstAttribute;
        secondAttribute = _secondAttribute;
        thirdAttribute = _thirdAttribute;
    }
   
    public int compare(Object obj, Object obj1) {
        int retVal = 0;

        // If o1 < o2 we return -1.
        // If o1 = o2 we return 0.
        // if o1 > o2 we return 1.
        
        // Compare on the primary attribute first, then if that is equal, the
        // secondary, etc. But only if the attributes are equal at a given
        // level do we give any consideration to the next level.
        switch (firstAttribute) {
            case ItemStore.CHANNEL_TITLE:
            case ItemStore.ITEM_TITLE:
            case ItemStore.ITEM_RETRIEVAL_DATE:
            case ItemStore.SCORE:
            case ItemStore.VIEWED:
                retVal = compareByAttribute(obj, obj1, firstAttribute);
                break;
            default:
                break;
        }
        
        if (retVal != 0) {
            return retVal;
        }
        
        // The two items are identical on the primary attribute (or no primary
        // was specified).
        switch (secondAttribute) {
            case ItemStore.CHANNEL_TITLE:
            case ItemStore.ITEM_TITLE:
            case ItemStore.ITEM_RETRIEVAL_DATE:
            case ItemStore.SCORE:
            case ItemStore.VIEWED:
                retVal = compareByAttribute(obj, obj1, secondAttribute);
                break;
            default:
                break;
        }
        
        if (retVal != 0) {
            return retVal;
        }
        
        // The two items are identical on both the primary and secondary
        // attributes (or one or both weren't specified).
        switch (thirdAttribute) {
            case ItemStore.CHANNEL_TITLE:
            case ItemStore.ITEM_TITLE:
            case ItemStore.ITEM_RETRIEVAL_DATE:
            case ItemStore.SCORE:
            case ItemStore.VIEWED:
                retVal = compareByAttribute(obj, obj1, thirdAttribute);
                break;
            default:
                break;
        }
        
        return retVal;
    }
    
    private int compareByAttribute(Object obj, Object obj1, int attribute) {
        Integer objScore, obj1Score;

        switch (attribute) {
            case ItemStore.CHANNEL_TITLE:
                String objTitle = ((Item) obj).getChannel().getTitle();
                String obj1Title = ((Item) obj1).getChannel().getTitle();
                
                return (objTitle.compareTo(obj1Title));
            case ItemStore.ITEM_TITLE:
                objTitle = ((Item) obj).getTitle();
                obj1Title = ((Item) obj1).getTitle();

                return (objTitle.compareTo(obj1Title));
            case ItemStore.ITEM_RETRIEVAL_DATE:
                Date objDate = ((Item) obj).getRetrieved();
                Date obj1Date = ((Item) obj).getRetrieved();
                
                return (objDate.compareTo(obj1Date));
            case ItemStore.SCORE:
                String score = ((Item) obj).getProperty("score");
                
                if (score != null) {
                    objScore = new Integer(score);
                } else {
                    objScore = new Integer(50);
                }
                
                score = ((Item) obj1).getProperty("score");
                
                if (score != null) {
                    obj1Score = new Integer(score);
                } else {
                    obj1Score = new Integer(50);
                }
                
                return (objScore.compareTo(obj1Score));
            case ItemStore.VIEWED:
                boolean objViewed = (((Item) obj).getProperty("viewed") != 
                    null) ? true : false;
                boolean obj1Viewed = (((Item) obj1).getProperty("viewed") != 
                    null) ? true : false;

                // compareTo() is not defined for Boolean because it doesn't
                // make any sense normally. Is true "greater than" false? Well
                // that kind of depends upon the context doesn't it. In our case
                // we are going to declare that true is greater than false and
                // sort accordingly.
                if (objViewed == obj1Viewed) {
                    return (0);
                } else if (objViewed == true) {
                    // obj1Viewed must be == false.
                    return (1);
                } else {
                    return (-1);
                }
            default:
                return 0;
        }
    }
}
