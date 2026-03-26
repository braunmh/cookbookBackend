package org.braun.cookbook.web.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author mbraun
 * @param <T> Type of Value
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
    CatRating.class,
})
public abstract class Catalogue<T extends Comparable<T>> implements Serializable, Comparable<Catalogue<T>> {
    
    @XmlElement
    private int id;
    
    @XmlElement
    private String name;
    
    public Catalogue() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract Catalogue<T> id(int id);
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract Catalogue<T> name(String name);
    
    public abstract T getValue();

    public abstract void setValue(T value);
    
    public abstract Catalogue<T> value(T value);
    
    @Override
    public int compareTo(Catalogue<T> o) {
        if (getValue() == null && (o == null || o.getValue() == null)) {
            return 0;
        }
        if (o == null || o.getValue() == null) {
            return 1;
        }
        
        if (getValue() == null) {
            return -1;
        }
        
        return getValue().compareTo(o.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(getValue());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Catalogue<?> other = (Catalogue<?>) obj;
        return Objects.equals(this.getValue(), other.getValue());
    }

    @Override
    public String toString() {
        return "Catalogue{" + "id=" + id + ", name=" + name + ", value=" + getValue() + '}';
    }

    public boolean isEmpty() {
        return id == 0;
    }
        
    public abstract Catalogue<T> copy();
}
