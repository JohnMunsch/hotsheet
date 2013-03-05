/*
 * ItemHistory.java
 *
 * Created on July 11, 2001, 2:54 PM
 */
package com.johnmunsch.rss;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * The item history keeps the track of items that have been retrieved for some
 * period of time.
 *
 * @author  John Munsch
 */
public interface ItemHistory {
    public abstract boolean inHistory(Item item);

    public abstract void load(File f);
    public abstract void store(File f);
}
