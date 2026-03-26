package org.braun.cookbook.backend.process;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.braun.cookbook.backend.dao.SequenceDao;
import org.braun.cookbook.backend.entity.SequenceEntity;

/**
 *
 * @author mbraun
 */
@Named
@Startup
@Singleton
public class SequenceGenerator {
    
    @Inject
    private SequenceDao sequenceDao;
    private Long last;
    
    private SequenceEntity sequenceEntity;

    @PostConstruct
    void init() {
    }
    
    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
    
    public Long getNext() {
        if (sequenceEntity == null) {
            sequenceEntity = sequenceDao.find("Recipe");
            last = sequenceEntity.getLastSeq();
        }
        if (last >= sequenceEntity.getLastSeq()) {
            sequenceEntity.setLastSeq(sequenceEntity.getLastSeq() + sequenceEntity.getAllocatation());
            sequenceDao.merge(sequenceEntity);
        }
        return ++last;
    }
    
}
