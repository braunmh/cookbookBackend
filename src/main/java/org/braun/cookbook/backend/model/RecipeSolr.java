package org.braun.cookbook.backend.model;

import java.util.Date;
import java.util.List;
import org.apache.solr.client.solrj.beans.Field;
import org.braun.cookbook.backend.model.recipe.ContentElement;
import org.braun.cookbook.backend.model.recipe.Ingredient;
import org.braun.cookbook.backend.model.recipe.Ingredients;
import org.braun.cookbook.backend.model.recipe.ListElement;
import org.braun.cookbook.backend.model.recipe.ListItem;
import org.braun.cookbook.backend.model.recipe.StructureElement;
import org.braun.cookbook.backend.process.KeywordFactory;

/**
 *
 * @author mbraun
 */
public class RecipeSolr extends AbstractSolr {

    public static final String FIELD_CONTENT_SUGGEST = "contentSuggest";
    public static final String FIELD_PATH_PARENT = "pathParent";
    
    @Field
    private String content;
    @Field
    private String creator;
    @Field
    private boolean evaluated;
    @Field
    private String id;
    @Field
    private List<Long> keywordIds;
    @Field
    private Date created;
    @Field
    private Date modified;
    @Field
    private String name;
    @Field
    private String path;
    @Field
    private String pathParent;
    @Field 
    private String contentSuggest;
    @Field
    private Date published;
    @Field
    private int rating;
    @Field
    private String title;
    @Field
    private int type;
    @Field
    private String url;
    @Field
    private String source;

    public RecipeSolr() {}
    
    public RecipeSolr(Recipe in) {
        created = from(in.getCreated());
        published = from(in.getPublished());
        modified = from(in.getModified());
        // creator = ?
        evaluated = in.isEvaluated();
        id = in.getId();
        path = in.getRelativeName();
        int i = path.lastIndexOf('/');
        pathParent = (i > 0) ? path.substring(0, i) : "";
        rating = in.getRating();
        title = replacePointByBlank(in.getTitle());
        url = in.getSource().getUrl();
        source = replacePointByBlank(in.getSource().getValue());
        keywordIds = in.getCategories().getCategories().stream().map(c -> from(c.getName())).toList();
        StringBuilder sb = new StringBuilder(title);
        addText(in.getDescription().getContent(), sb);
        sb.append(" ").append(source);
        for (Long k : keywordIds) {
            if (k != null) {
                Keyword kt = KeywordFactory.getInstance().getById(k);
                if (kt != null) {
                    sb.append(" ").append(kt.getName());
                }
            }
        }
        for (Ingredients is : in.getIngredients()) {
            sb.append(" ").append(is.getTitle());
            for (Ingredient ig : is.getIngredients()) {
                sb.append(" ").append(ig.getValue());
            }
        }
        content = sb.toString();
        content = replacePointByBlank(content);
        contentSuggest = content;
        type = 1;
    }
    
    public RecipeShort toRecipeShort() {
        RecipeShort out = new RecipeShort().id(getId()).rating(getRating()).title(getTitle()).path(getPath()).score(getScore());
        if (getKeywordIds() != null) {
            for (Long keywordId : getKeywordIds()) {
                Keyword k = KeywordFactory.getInstance().getById(keywordId);
                if (k != null) {
                    out.addKeywordsItem(k.getName());
                }
            }
        }
        return out;
    }
    
    private String replacePointByBlank(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('.', ' ');
    }
    
    private void addText(List<StructureElement> elements, StringBuilder sb) {
        for (StructureElement s : elements) {
            if (s == null) {
                continue;
            }
            if (s instanceof ListElement le) {
                addTextList(le.getListItems(), sb);
            } else if (s instanceof ContentElement ce) {
                sb.append(" ").append(ce.getValue());
            }
        }
    } 
    
    private void addTextList(List<ListItem> le, StringBuilder sb) {
        for (ListItem li : le) {
            sb.append(" ").append(li.getValue());
        }
    }
    
    private Long from(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Date from(Long value) {
        if (value == null) {
            return new Date();
        }
        return new Date(value);
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Long> getKeywordIds() {
        return keywordIds;
    }

    public void setKeywordIds(List<Long> keywordIds) {
        this.keywordIds = keywordIds;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getContentSuggest() {
        return contentSuggest;
    }

    public void setContentSuggest(String value) {
        this.contentSuggest = value;
    }

    public String getPathParent() {
        return pathParent;
    }

    public void setPathParent(String pathParent) {
        this.pathParent = pathParent;
    }
    
    
}
