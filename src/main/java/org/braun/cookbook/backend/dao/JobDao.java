package org.braun.cookbook.backend.dao;

import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.braun.cookbook.backend.entity.JobEntity;
import org.braun.cookbook.backend.model.BackgroundJobType;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
public class JobDao extends AbstractDao<JobEntity, Long> {
    
    @PersistenceContext(unitName = "cookbook")
    private EntityManager em;

    public JobDao() {
        super(JobEntity.class);
    }
    
    public JobDao(EntityManager em) {
        this();
        this.em = em;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    public JobEntity findByType(BackgroundJobType type) {
        TypedQuery<JobEntity> query = getEntityManager().
                createNamedQuery("JobEntity.findByType", JobEntity.class).setParameter("type", type.name());
        List<JobEntity> res = query.getResultList();
        return (res.isEmpty()) ? null : res.get(0);
    }
}
