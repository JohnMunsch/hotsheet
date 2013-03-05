/*
 * HSSettings.java
 *
 * Created on August 2, 2001, 9:23 PM
 */
package com.johnmunsch.hotsheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.*;

/**
 * Acts as a singleton repository of all the settings for the entire 
 * application. Handles loading and storing of those settings for the program.
 *
 * @author Don Thorp
 */
public class HSSettings {
    // Singleton instance of HSSettings.
    private static HSSettings me = null;
        
    // File objects in hotsheet
    private File userHomePath = null;
    private File hsPath = null;
    private File channelStoreFile = null;
    private File itemHistoryFile = null;
    private File itemStoreFile = null;
    private File propertiesFile = null;
    private File oldChannelStoreFile = null;
    private File oldItemHistoryFile = null;
    private File oldItemStoreFile = null; 
    
    private Properties properties = new Properties();
    
    private static Category cat = Category.getInstance(
        HSSettings.class.getName());
    
    /** Creates new HSSettings */
    private HSSettings() {
        userHomePath = new File(System.getProperty("user.home"));
        
        hsPath = new File(userHomePath, ".hotsheet");
        channelStoreFile = new File(hsPath, "channelStore");
        itemHistoryFile = new File(hsPath, "itemHistory");
        itemStoreFile = new File(hsPath, "itemStore");
        propertiesFile = new File(hsPath, "HotSheet.properties");
        oldChannelStoreFile = new File(userHomePath, "channelStore");
        oldItemHistoryFile = new File(userHomePath, "itemHistory");
        oldItemStoreFile = new File(userHomePath, "itemStore");

        relocateSettings();
        load();
    }

    /**
     * Retrieve the instance of the HSSettings object
     */
    public static HSSettings getInstance() {
        if (me == null) {
            me = new HSSettings();
        }
        
        return me;
    }
    
    /** 
     * Return the File object corresponding to the user's home directory 
     */
    public File getUserHomePath() {
        return userHomePath;
    }
        
    /** 
     * Return the File object to the HotSheet directory. 
     */
    public File getHSPath() {        
        return hsPath;
    }
    
    /**
     * Return the File object to the channelStore 
     */
    public File getChannelStoreFile() {
        return channelStoreFile;
    }
    
    /** 
     * Return the File object to the itemHistory 
     */
    public File getItemHistoryFile() {
        return itemHistoryFile;
    }
    
    /** 
     * Return the File object to the itemStore 
     */
    public File getItemStoreFile() {
        return itemStoreFile;
    }

    /** 
     * Return the File object to the old channelStore 
     */
    public File getOldChannelStoreFile() {
        return oldChannelStoreFile;
    }

    /**
     * Return the File object to the old itemHistory 
     */
    public File getOldItemHistoryFile() {
        return oldItemHistoryFile;
    }
    
    /** 
     * Return the File object to the old itemStore 
     */
    public File getOldItemStoreFile() {
        return oldItemStoreFile;
    }
    
    /**
     * Loads the properties for the application.
     */
    public void load() {
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException ioe) {
            // If we failed to load the properties then we want to set up some
            // default logging properties.
            setDefaultLoggingProperties();
        }
    }
    
    /**
     * Saves the current properties settings for the application.
     */
    public void store() {
        try {
            properties.store(new FileOutputStream(propertiesFile), null);
        } catch (IOException ioe) {
            cat.error(ioe);
        }
    }

    /**
     * Returns the properties set for the application.
     */
    public Properties getProperties() {
        return (properties);
    }

    // =========================================================================
    // Helper functions
    // -------------------------------------------------------------------------
    
    /** 
     * Relocate settings from the user's home directory to the hotsheet 
     * directory. This is a function to deal with legacy configurations where
     * the files were stored in a poor location.
     */
    public boolean relocateSettings() {
        boolean success = true;

        try {            
            if (!hsPath.exists()) {
                hsPath.mkdir();
                
                // Copy old files to new directory.
                if (getOldChannelStoreFile().exists()) {
                    copyFile(getOldChannelStoreFile(), getChannelStoreFile());
                }
                
                if (getOldItemStoreFile().exists()) {
                    copyFile(getOldItemStoreFile(), getItemStoreFile());
                }
                
                if (getOldItemHistoryFile().exists()) {
                    copyFile(getOldItemHistoryFile(), getItemHistoryFile());
                }
            }   
        } catch (IOException ioe) {
            cat.error(ioe);
            
            success = false;
        }
        
        return success;
    }

    /**
     * Copy source file to destination file.
     */
    public void copyFile(File source, File dest) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(dest);

            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(fos);

            byte[] buf = new byte[2048];

            int bytes = bis.read(buf);
            while (bytes != -1) {
                bos.write(buf, 0, bytes);
                bytes = bis.read(buf);
            }
            
            buf = null;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    cat.error(e);
                }
            }
            
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    cat.error(e);
                }
            }
            
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    cat.error(e);
                }
            }
            
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    cat.error(e);
                }
            }
        }
    }

    private void setDefaultLoggingProperties() {
        // Set root category priority to DEBUG and its only appender to R.
        properties.setProperty("log4j.rootCategory", "INFO, Default");

        properties.setProperty("log4j.appender.Default", 
            "org.apache.log4j.FileAppender");
        String logLocation = new File(hsPath, "log").toString();
        properties.setProperty("log4j.appender.Default.file", logLocation);
        properties.setProperty("log4j.appender.Default.append", 
            "false");

        // Default uses XMLLayout.
        properties.setProperty("log4j.appender.Default.layout", 
            "org.apache.log4j.xml.XMLLayout");
    }
}
