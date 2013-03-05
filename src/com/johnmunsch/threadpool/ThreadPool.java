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

// The interface to be implemented by the thread pool.
public interface ThreadPool {
    public void addJob(java.lang.Runnable job);
    public Stats getStats();
}
