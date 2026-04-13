package org.braun.cookbook.backend.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobStatus;

/**
 *
 * @author mbraun
 */
@Entity
@Table(name = "job")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "JobEntity.findAll", query = "SELECT j FROM JobEntity j"),
    @NamedQuery(name = "JobEntity.findById", query = "SELECT j FROM JobEntity j WHERE j.id = :id"),
    @NamedQuery(name = "JobEntity.findByType", query = "SELECT j FROM JobEntity j WHERE j.type = :type"),
    @NamedQuery(name = "JobEntity.findByMessage", query = "SELECT j FROM JobEntity j WHERE j.message = :message")})
public class JobEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 32)
    @Column(name = "type")
    private String type;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "message")
    private String message;
 
    @Lob
    @Size(max = 65535)
    @Column(name = "information")
    private String information;
    
    @Size(max = 32)
    @Column(name = "status")
    private String status;
    
    @Column(name = "started")
    @Temporal(TemporalType.TIMESTAMP)
    private Date started;
    
    @Column(name = "finished")
    @Temporal(TemporalType.TIMESTAMP)
    private Date finished;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    public JobEntity() {
    }

    public JobEntity(Long id) {
        this.id = id;
    }

    public JobEntity(Long id, String type, String message) {
        this.id = id;
        this.type = type;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof JobEntity)) {
            return false;
        }
        JobEntity other = (JobEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.braun.cookbook.backend.entity.Job[ id=" + id + " ]";
    }


    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
