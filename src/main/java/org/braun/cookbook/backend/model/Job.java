package org.braun.cookbook.backend.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mbraun
 */
public class Job implements Serializable {
    
    private Long id;
    
    private BackgroundJobType type;
    
    private JobStatus status;
    
    private Date started;
    
    private Date finished;
    
    private String message;
    
    private List<String> information;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job id(Long value) {
        id = value;
        return this;
    }
    
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Job status(JobStatus value) {
        status = value;
        return this;
    }
    
    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Job started(Date value) {
        started = value;
        return this;
    }
    
    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public Job finished(Date value) {
        finished = value;
        return this;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BackgroundJobType getType() {
        return type;
    }

    public void setType(BackgroundJobType type) {
        this.type = type;
    }

    public Job type(BackgroundJobType value) {
        type = value;
        return this;
    }
    
    public Job message(String value) {
        message = value;
        return this;
    }
    
    public List<String> getInformation() {
        if (information == null) {
            information = new ArrayList<>();
        }
        return information;
    }

    public Job addInformation(String value) {
        if (value != null) {
            getInformation().add(value);
        }
        return this;
    }

    public Job addAllInformation(List<String> value) {
        if (value != null) {
            getInformation().addAll(value);
        }
        return this;
    }
}
