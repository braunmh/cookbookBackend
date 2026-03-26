package org.braun.cookbook.backend.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author mbraun
 */
@Entity
@Table(name = "keyword", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name_upper"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Keyword.findAll", query = "SELECT k FROM KeywordEntity k where k.id > 0 order by k.name"),
    @NamedQuery(name = "Keyword.findById", query = "SELECT k FROM KeywordEntity k WHERE k.id = :id"),
    @NamedQuery(name = "Keyword.findByName", query = "SELECT k FROM KeywordEntity k WHERE k.name = :name"),
    @NamedQuery(name = "Keyword.findAllRoots", query = "SELECT k FROM KeywordEntity k LEFT JOIN k.parent p WHERE p.id = 0 and not k.id = 0 ORDER BY k.name"),
    @NamedQuery(name = "Keyword.findByNameUpper", query = "SELECT k FROM KeywordEntity k LEFT JOIN k.synonyms s WHERE k.nameUpper = :nameUpper or s.name = :symName")})
public class KeywordEntity implements Serializable {

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
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name_upper", nullable = false, length = 255)
    private String nameUpper;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keywordId")
    private Collection<SynonymEntity> synonyms;

    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = true)
    @ManyToOne(optional = false)
    private KeywordEntity parent;
    
    public KeywordEntity() {
    }

    public KeywordEntity(Long id) {
        this.id = id;
    }

    public KeywordEntity(Long id, String name, String nameUpper) {
        this.id = id;
        this.name = name;
        this.nameUpper = nameUpper;
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

    public String getNameUpper() {
        return nameUpper;
    }

    public void setNameUpper(String nameUpper) {
        this.nameUpper = nameUpper;
    }

    @XmlTransient
    public Collection<SynonymEntity> getSynonyms() {
        return synonyms;
    }

    public void setSynonymCs(Collection<SynonymEntity> synonyms) {
        this.synonyms = synonyms;
    }

    @XmlTransient
    public KeywordEntity getParent() {
        return parent;
    }

    public void setParent(KeywordEntity parent) {
        this.parent = parent;
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
        if (!(object instanceof KeywordEntity)) {
            return false;
        }
        KeywordEntity other = (KeywordEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.braun.cookbook.persistence.Keyword[ id=" + id + " ]";
    }
    
}
