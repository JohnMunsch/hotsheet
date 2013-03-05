// The original source of this code was an article by Tarak Modi in Java Pro
// magazine October 2000. There is a link to the original article on Modi's 
// website: 
//          http://tmodi.home.att.net/
//
// The articles to read include "Why Thread Pools are Important in Java" and 
// "Improve Java Performance" which has an update to the code.
//
// A few changes have been made to the updated version so that it has some
// javadocs in the code, the settings for the minimum, maximum, etc. number of
// threads are passed in via the constructor (as they should have been from the
// beginning), and there aren't any "if" conditions with a single hanging
// statement and no bracketing after them. The latter is extremely bad 
// programming style and very mistake prone.
package com.johnmunsch.threadpool;

import org.apache.log4j.*;

// Thread Pool implementation class.
import java.util.ArrayList;
import java.lang.IllegalArgumentException;
import java.lang.InterruptedException;
import java.lang.NumberFormatException;
import java.lang.Runnable;
import java.lang.Thread;

/**
 * 
 */
public class ThreadPoolImpl implements ThreadPool {
    // max number of threads that can be created
    private int _maxThreads = -1;
    // min number of threads that must always be present
    private int _minThreads = -1;
    // max idle time after which a thread may be killed
    private int _maxIdleTime = -1;
    // A list of pending jobs
    private ArrayList _pendingJobs = new ArrayList();

    // A list of all idle threads
    private ThreadArrayList _idleThreads = new ThreadArrayList();
    // A list of all busy threads
    private ThreadArrayList _busyThreads = new ThreadArrayList();

    static final Category cat = Category.getInstance(
        ThreadPoolImpl.class.getName());
    
    private class ThreadArrayList extends ArrayList {
        public ThreadElement remove() {
            ThreadElement elem = new ThreadElement(Thread.currentThread());
            int i = indexOf(elem);
            if (i == -1) {
                return null;
            }

            return (ThreadElement) remove(i);
        }
    }

    /**
     * This class represents an element in the ThreadArrayList.
     */
    private class ThreadElement {
        // if true then the thread can process a new job
        private boolean _idle;
        // serves as the key 
        private Thread _thread;

        public ThreadElement(Thread thread) {
            _thread = thread;
            _idle = true;
        }

        public boolean equals(Object o) {
            if (o instanceof ThreadElement) {
                return (((ThreadElement) o)._thread == _thread);
            }

            return false;
        }                              
    }

    /**
     * Each thread in the pool is an instance of this class.
     */
    private class PoolThread extends Thread {
        private Object _lock;

        // pass in the pool instance for synchronization.
        public PoolThread(Object lock) {
            _lock = lock;
        }

        // This is where all the action is...
        public void run() {
            Runnable job = null;                  

            while (true) {
                while (true) {
                    synchronized (_lock) {
                        // Keep processing jobs until none availble
                        if (_pendingJobs.size() == 0) {
                            cat.debug("Idle Thread...");

                            ThreadElement elem = _busyThreads.remove();
                            if (elem == null) {
                                return;
                            }

                            elem._idle = true;
                            _idleThreads.add(elem);
                            break;
                        }

                        // Remove a job from the pending list.
                        job = (Runnable)_pendingJobs.remove(_pendingJobs.size()-1);                                          
                    }               

                    // run the job
                    job.run();
                    job = null;
                }            

                try {
                    synchronized (this) {
                        // if no idle time specified, 
                        // wait till notified.
                        if (_maxIdleTime == -1) {
                            wait();
                        } else {
                            wait(_maxIdleTime);
                        }
                    }                        
                } catch( InterruptedException e ) {
                    // Cleanup if interrupted
                    synchronized (_lock) {
                        cat.debug("Interrupted...");
                        _idleThreads.remove();
                    }
                    return;
                }

                // Just been notified or the wait timed out
                synchronized(_lock) {
                    // If there are no jobs, that means we "idled" out.
                    if (_pendingJobs.size() == 0) {
                        if (_minThreads != -1 && totalThreads() > _minThreads) {
                            cat.debug("Thread timed out...");
                            _idleThreads.remove();
                            return;    
                        }
                    }
                }
            }
        }
    }

    public ThreadPoolImpl(int minThreads, int maxThreads, int maxIdleTime) 
        throws IllegalArgumentException {

        if (maxThreads < 1) {
            throw new IllegalArgumentException(
                "maxThreads must be an integral value greater than 0");
        }
        _maxThreads = maxThreads;

        if (minThreads < 0) {
            throw new IllegalArgumentException(
                "minThreads must be an integral " + 
                "value greater than or equal to 0");
        }

        if (minThreads > _maxThreads) {
            throw new IllegalArgumentException(
                "minThreads cannot be greater than maxThreads");
        }
        _minThreads = minThreads;

        if (maxIdleTime < 1) {
            throw new IllegalArgumentException(
                "maxIdleTime must be an integral value greater than 0");
        }
        _maxIdleTime = maxIdleTime;
    }

    /**
     * The fundamental function of the thread pool. Note that it will never
     * block or keep you from inserting yet another job into the queue. So, if
     * you have a large (100,000 or more?) list of jobs to queue up, all will
     * go into the list. For large job objects that could be a bad thing and you
     * will need to find some mechanism to reduce the number of jobs you attempt
     * to add at one time.
     */
    synchronized public void addJob(java.lang.Runnable job) {
        _pendingJobs.add(job);             

        if (_idleThreads.size() == 0) {
            // All threads are busy

            if (_maxThreads == -1 || totalThreads() < _maxThreads) {
                // We can create another thread...
                cat.debug("Creating a new Thread...");
                
                ThreadElement e = new ThreadElement(new PoolThread(this));                            
                e._idle = false;
                e._thread.start();
                _busyThreads.add(e);
                return;
            }

            // We are not allowed to create any more threads
            // So, just return.
            // When one of the busy threads is done, 
            // it will check the pending queue and will see this job.
            cat.debug("Max Threads created and all threads in the pool are busy.");
        } else {
            // There is at least one idle thread.
            cat.debug("Using an existing thread...");
            ThreadElement elem = 
                (ThreadElement) _idleThreads.remove(_idleThreads.size() - 1);
            elem._idle = false;
            _busyThreads.add(elem);
            
            synchronized(elem._thread) {
                elem._thread.notify();
            }
        }
    }

    /**
     * Returns a stats object that tells the state of the thread pool at the
     * time the function was called.
     */
    synchronized public Stats getStats() {
        Stats stats = new Stats();
        stats.maxThreads = _maxThreads;
        stats.minThreads = _minThreads;
        stats.maxIdleTime = _maxIdleTime;
        stats.pendingJobs = _pendingJobs.size();           
        stats.numThreads = totalThreads();        
        stats.jobsInProgress = _busyThreads.size();

        return(stats);       
    }

    ////////////////////////////////////////////////////////////////////////////
    //                                                          Helper Functions
    ////////////////////////////////////////////////////////////////////////////

    // Note: All private methods must always be called from a synchronized 
    // method!!

    /**
     * Called by the thread pool to find the total number of threads in the 
     * pool. May be less than or equal to the maximum allowable threads in the 
     * pool.
     */
    private int totalThreads() {
        return(_busyThreads.size() + _idleThreads.size());
    }       
}
