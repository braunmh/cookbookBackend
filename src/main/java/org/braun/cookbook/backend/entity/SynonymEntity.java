package org.braun.cookbook.backend.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author mbraun
 */
@Entity
@Table(name = "synonym", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Synonym.findAll", query = "SELECT s FROM SynonymEntity s"),
    @NamedQuery(name = "Synonym.findById", query = "SELECT s FROM SynonymEntity s WHERE s.id = :id"),
    @NamedQuery(name = "Synonym.findByName", query = "SELECT s FROM SynonymEntity s WHERE s.name = :name")})
public class SynonymEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @JoinColumn(name = "keyword_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private KeywordEntity keywordId;

    public SynonymEntity() {
    }

    public SynonymEntity(Long id) {
        this.id = id;
    }

    public SynonymEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KeywordEntity getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(KeywordEntity keywordId) {
        this.keywordId = keywordId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SynonymEntity)) {
            return false;
        }
        SynonymEntity other = (SynonymEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.braun.cookbook.persistence.Synonym[ id=" + id + " ]";
    }
    
}
