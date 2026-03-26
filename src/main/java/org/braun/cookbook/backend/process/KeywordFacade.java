package org.braun.cookbook.backend.process;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import org.braun.cookbook.backend.dao.KeywordDao;
import org.braun.cookbook.backend.entity.KeywordEntity;
import org.braun.cookbook.backend.model.Keyword;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
public class KeywordFacade {
    
    @Inject
    private KeywordDao keywordDao;
    
    public Keyword getByName(String name) {
        return KeywordMapper.getInstance().map(keywordDao.getByName(name));
    }
    
    public void setKeywordDao(KeywordDao kd) {
        keywordDao = kd;
    }
    
    public List<Keyword> findAll() {
        List<KeywordEntity> tmp = keywordDao.findAll();
        List<Keyword> res = new ArrayList<>();
        for (KeywordEntity k : tmp) {
            res.add(KeywordMapper.getInstance().map(k));
        }
        return res;
    }
    
    public Keyword insert(Keyword value) {
        keywordDao.create(KeywordMapper.getInstance().map(value));
        return getByName(value.getName());
    }
    
}
