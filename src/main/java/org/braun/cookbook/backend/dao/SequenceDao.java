package org.braun.cookbook.backend.dao;

import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.braun.cookbook.backend.entity.SequenceEntity;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
public class SequenceDao extends AbstractDao<SequenceEntity, String> {
    
    @PersistenceContext(unitName = "cookbook")
    private EntityManager entityManager;

    public SequenceDao() {
        super(SequenceEntity.class);
    }

    public SequenceDao(EntityManager entityManager) {
        this();
        this.entityManager = entityManager;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }
}
