package org.braun.cookbook.backend.dao;

import jakarta.ejb.Stateless;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.braun.cookbook.backend.entity.KeywordEntity;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
public class KeywordDao extends AbstractDao<KeywordEntity, Long> {

    @PersistenceContext(unitName = "cookbook")
    private EntityManager em;

    public KeywordDao() {
        super(KeywordEntity.class);
    }
    
    public KeywordDao(EntityManager em) {
        this();
        this.em = em;
    }
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void create(KeywordEntity entity) {
        Query query = getEntityManager().createNativeQuery("insert into keyword (name, name_upper, parent_id) values(?, ?, ?)")
                .setParameter(1, entity.getName())
                .setParameter(2, entity.getNameUpper())
                .setParameter(3, entity.getParent().getId());
        query.executeUpdate();
    }
    
    public KeywordEntity getByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        TypedQuery<KeywordEntity> query = getEntityManager()
                .createNamedQuery("Keyword.findByNameUpper", KeywordEntity.class)
                .setParameter("nameUpper", name.toUpperCase())
                .setParameter("symName", name.toUpperCase());
        List<KeywordEntity> res = query.getResultList();
        if (res.isEmpty()) {
            return null;
        }
        return res.get(0);
    }

    @Override
    public List<KeywordEntity> findAll() {
        TypedQuery<KeywordEntity> query = getEntityManager()
                .createNamedQuery("Keyword.findAll", KeywordEntity.class);
        return query.getResultList();
    }
    
    
}
