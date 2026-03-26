package org.braun.cookbook.backend.process;

import org.braun.cookbook.backend.entity.KeywordEntity;
import org.braun.cookbook.backend.model.Keyword;

/**
 *
 * @author mbraun
 */
public class KeywordMapper {
    
    private static final KeywordMapper instance = new KeywordMapper();
    
    private KeywordMapper() {}
    
    public static KeywordMapper getInstance() {
        return instance;
    }
    
    public Keyword map(KeywordEntity in) {
        if (in == null) {
            return null;
        }
        Keyword out = new Keyword()
                .id(in.getId())
                .name(in.getName())
                .path(getPath(in))
                .parentId((in.getParent() == null) ? Long.valueOf(0) : in.getParent().getId())
                .nameUpper(in.getNameUpper())
                .synonyms(in.getSynonyms().stream().map(s -> s.getName()).toList());
        return out;
    }
        
    private String getPath(KeywordEntity in) {
        if (in.getParent() == null) {
            return in.getName();
        } else {
            String parents = getPath(in.getParent()) + "|" + in.getName();
            return parents;
        }
    }
    
    public KeywordEntity map(Keyword in) {
        KeywordEntity out = new KeywordEntity(in.getId());
        out.setName(in.getName());
        out.setNameUpper(in.getName().toUpperCase());
        out.setParent(new KeywordEntity());
        out.getParent().setId(in.getParentId());
        return out;
    }
}
