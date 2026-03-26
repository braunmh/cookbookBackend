package org.braun.cookbook.backend.process;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mbraun
 */
public class StatusFactory {
    private static final Logger LOG = LogManager.getLogger();
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final Lock READ_LOCK = LOCK.readLock();
    private static final Lock WRITE_LOCK = LOCK.writeLock();

    private static final StatusFactory INSTANCE = new StatusFactory();
    
    private boolean indexStatus;
    
    private StatusFactory() {
        indexStatus = false;
    }

    public static StatusFactory getInstance() {
        return INSTANCE;
    }
    
    public boolean getIndexStatus() {
        try {
            READ_LOCK.lock();
            return indexStatus;
        } catch (Exception e) {
            LOG.error("Aquire StatusFactory-ReadLock failed", e);
            return indexStatus;
        } finally {
            READ_LOCK.unlock();
        }
    }
    
    public boolean aquireIndexStatusBussy() {
        try {
            WRITE_LOCK.lock();
            if (!indexStatus) {
                indexStatus = true;
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("Aquire StatusFactory-WriteLock failed", e);
            return indexStatus;
        } finally {
            WRITE_LOCK.unlock();
        }
    }
    
    public boolean aquireStatusStatusDone() {
        try {
            WRITE_LOCK.lock();
            if (indexStatus) {
                indexStatus = false;
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("Aquire StatusFactory-WriteLock failed", e);
            return indexStatus;
        } finally {
            WRITE_LOCK.unlock();
        }
    }
    
}
