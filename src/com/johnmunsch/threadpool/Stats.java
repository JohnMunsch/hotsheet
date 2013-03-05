// The original source of this code was an article by Tarak Modi in Java Pro
// magazine October 2000. There is a link to the original article on Modi's 
// website: 
//          http://tmodi.home.att.net/
//
// A few changes have been made from the original version so that it has some
// javadocs in the code, the settings for the minimum, maximum, etc. number of
// threads are passed in via the constructor (as they should have been from the
// beginning), and there aren't any "if" conditions with a single hanging
// statement and no bracketing after them. The latter is extremely bad 
// programming style and very mistake prone.
package com.johnmunsch.threadpool;

/**
 * This class holds the statistics of the Thread Pool from which it was 
 * returned.
 */
public class Stats {
    public int maxThreads;
    public int minThreads;
    public int maxIdleTime;
    public int numThreads;
    public int pendingJobs;
    public int jobsInProgress;

    public String toString() {
        java.lang.StringBuffer sb = new java.lang.StringBuffer();
        String strMax = (maxThreads == -1) 
            ? "No limit" : new Integer(maxThreads).toString();
        String strMin = (minThreads == -1) 
            ? "No limit" : new Integer(minThreads).toString();

        sb.append("maxThreads = " + strMax + "\n");
        sb.append("minThreads = " + strMin + "\n");
        sb.append("maxIdleTime = " + maxIdleTime + "\n");
        sb.append("numThreads = " + numThreads + "\n");
        sb.append("pendingJobs = " + pendingJobs + "\n");
        sb.append("jobsInProgress = " + jobsInProgress + "\n");
        
        return(sb.toString());
    }
}
