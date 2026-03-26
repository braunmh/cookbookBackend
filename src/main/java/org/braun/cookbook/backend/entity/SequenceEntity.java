package org.braun.cookbook.backend.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author mbraun
 */
@Entity
@Table(name = "sequencetab")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Sequence.findAll", query = "SELECT s FROM SequenceEntity s"),
    @NamedQuery(name = "Sequence.findByName", query = "SELECT s FROM SequenceEntity s WHERE s.name = :name"),
    @NamedQuery(name = "Sequence.findByLastSeq", query = "SELECT s FROM SequenceEntity s WHERE s.lastSeq = :lastSeq"),
    @NamedQuery(name = "Sequence.findByAllocatation", query = "SELECT s FROM SequenceEntity s WHERE s.allocatation = :allocatation")})
public class SequenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "lastSeq")
    private long lastSeq;
    @Basic(optional = false)
    @NotNull
    @Column(name = "allocatation")
    private long allocatation;

    public SequenceEntity() {
    }

    public SequenceEntity(String name) {
        this.name = name;
    }

    public SequenceEntity(String name, int lastSeq, int allocatation) {
        this.name = name;
        this.lastSeq = lastSeq;
        this.allocatation = allocatation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(long lastSeq) {
        this.lastSeq = lastSeq;
    }

    public long getAllocatation() {
        return allocatation;
    }

    public void setAllocatation(long allocatation) {
        this.allocatation = allocatation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SequenceEntity)) {
            return false;
        }
        SequenceEntity other = (SequenceEntity) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.braun.cookbook.backend.entity.Sequence[ name=" + name + " ]";
    }
    
}
