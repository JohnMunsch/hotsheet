/*
 * JCenterableDialog.java
 *
 * Created on July 2, 2002, 11:36 AM
 */
package com.johnmunsch.hotsheet;

import java.awt.Dimension;
import java.awt.Point;

/**
 *
 * @author  JMunsch
 */
public class JCenterableDialog extends javax.swing.JDialog {
    // Constants used to set how we want this dialog box centered.
    static final int DO_NOT_CENTER = 0;
    static final int VS_PARENT = 1;
    static final int VS_SCREEN = 2;
    
    /** Creates a new instance of JCenterableDialog */
    public JCenterableDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    
    public void center(int centerVs) {
        Dimension otherSize = null;
        Dimension dialogSize = null;
        Point otherLocation = null;

        if ((centerVs != VS_PARENT) && (centerVs != VS_SCREEN)) {
            return;
        }
        
        // Center the dialog as directed.
        switch (centerVs) {
            case VS_PARENT:
                otherSize = getParent().getSize();
                otherLocation = getParent().getLocation();
                break;
            case VS_SCREEN:
                otherSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                otherLocation = new Point(0, 0);
                break;
        }
        
        dialogSize = getSize();
        
        Point offset = new Point((otherSize.width - dialogSize.width) / 2, 
            (otherSize.height - dialogSize.height) / 2);
        
        setLocation(otherLocation.x + offset.x, otherLocation.y + offset.y);
    }
}
