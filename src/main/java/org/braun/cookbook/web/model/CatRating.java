package org.braun.cookbook.web.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mbraun
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class CatRating extends Catalogue<Integer> {
    
    public static final List<CatRating> values =
        Arrays.asList(
            new CatRating().id(0).value(null).name(""),
            new CatRating().id(1).value(1).name("*****"),
            new CatRating().id(2).value(2).name("****"),
            new CatRating().id(3).value(3).name("***"),
            new CatRating().id(4).value(4).name("**"),
            new CatRating().id(5).value(5).name("*")
        );

    @XmlAttribute
    private Integer value;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }
    
    @Override
    public CatRating id(int id) {
        setId(id);
        return this;
    }

    @Override
    public CatRating name(String name) {
        setName(name);
        return this;
    }

    @Override
    public CatRating value(Integer value) {
        setValue(value);
        return this;
    }
    
    public static CatRating findById(Integer id) {
        if (id == null || id < 0) {
            return values.get(0).copy();
        }
        if (id > values.size()) {
            id = values.size();
        }
        return values.get(id).copy();
    }
    
    public CatRating copy() {
        return new CatRating().id(getId()).name(getName()).value(getValue());
    }
}
