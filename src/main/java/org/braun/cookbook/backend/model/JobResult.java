package org.braun.cookbook.backend.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author mbraun
 */
public class JobResult {
    
    private BackgroundJobType type;
    
    private JobStatus status;
    
    private String message;
    
    private List<String> information;

    public BackgroundJobType getType() {
        return type;
    }

    public void setType(BackgroundJobType type) {
        this.type = type;
    }

    public JobResult type(BackgroundJobType value) {
        type = value;
        return this;
    }
    
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public JobResult status(JobStatus value) {
        status = value;
        return this;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JobResult message(String value) {
        message = value;
        return this;
    }
    
    public List<String> getInformation() {
        if (information == null) {
            information = new ArrayList<>();
        }
        return information;
    }

    public JobResult information(Collection<String> information) {
        getInformation().addAll(information);
        return this;
    }

    @Override
    public String toString() {
        return "JobResult{" + "type=" + type + ", status=" + status + ", message=" + message + ", information=" + information + '}';
    }

}
