package org.braun.cookbook.backend.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.Keyword;

/**
 *
 * @author mbraun
 */
public class KeywordFactory {
    private static final Logger LOG = LogManager.getLogger();
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final Lock READ_LOCK = LOCK.readLock();
    private static final Lock WRITE_LOCK = LOCK.writeLock();
    private final List<Keyword> nodes;
    private final Map<String, Keyword> keywordsByName;
    private final Map<Long, Keyword> keywordsById;
    
    private static KeywordFactory instance = new KeywordFactory();
    
    private KeywordFactory() {
        nodes = new ArrayList<>();
        keywordsByName = new HashMap<>();
        keywordsById = new HashMap<>();
    }
    
    public static KeywordFactory getInstance() {
        return instance;
    }
    
    public void refresh(List<Keyword> keywords) {
        try {
            WRITE_LOCK.lock();
            nodes.clear();
            nodes.addAll(keywords);
            keywordsById.clear();
            keywordsByName.clear();
            for (Keyword k : nodes) {
                keywordsById.put(k.getId(), k);
                keywordsByName.put(k.getNameUpper(), k);
                for (String s : k.getSynonyms()) {
                    keywordsByName.put(s, k);
                }
            }
        } catch (Exception e) {
            LOG.error("Aquiring WriteLock failed", e);
        } finally {
            WRITE_LOCK.unlock();
        }
    }
    
    public Keyword getByName(String name) {
        if (name == null) {
            return null;
        }
        return keywordsByName.get(name.toUpperCase());
    }
    
    public Keyword getById(Long id) {
        if (id == null) {
            return null;
        }
        return keywordsById.get(id);
    }
    
    public Keyword getById(String id) {
        if (id == null) {
            return null;
        }
        try {
            return keywordsById.get(Long.valueOf(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public List<Keyword> getListByName(String name) {
        List<Keyword> result = new ArrayList<>();
        try {
            READ_LOCK.lock();
            if (name == null || name.isEmpty()) {
                for (Keyword n : nodes) {
                    Keyword k = new Keyword();
                    k.setId(n.getId());
                    k.setName(n.getName());
                    k.setNameUpper(n.getNameUpper());
                    k.setPath(n.getPath());
                    result.add(k);
                }
            } else {
                name = name.toUpperCase();
                for (Keyword n : nodes) {
                    if (n.getNameUpper().contains(name)) {
                        Keyword k = new Keyword();
                        k.setId(n.getId());
                        k.setName(n.getName());
                        k.setNameUpper(n.getNameUpper());
                        k.setPath(n.getPath());
                        result.add(k);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Aquiring ReadLock failed", e);
        } finally {
            READ_LOCK.unlock();
        }
        return result;
    }

}
