package org.braun.cookbook.web.ui;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.BackgroundJobTypeFactory;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.backend.process.BackgroundTask;
import org.braun.cookbook.backend.process.JobFacade;
import org.braun.cookbook.common.ManualContext;
import org.omnifaces.cdi.ViewScoped;

/**
 *
 * @author mbraun
 */
@Named
@ViewScoped
public class JobBean implements Serializable {
    
    @Inject
    private JobFacade jobFacade;
    
    private List<Job> jobs;
    
    public List<Job> getJobs() {
        if (jobs == null) {
            jobs = jobFacade.findAll();
            for (BackgroundJobType type : BackgroundJobType.values()) {
                if (BackgroundJobTypeFactory.getBackgroudJobClass(type) == null) {
                    continue;
                }
                if (!jobs.stream().anyMatch(j -> j.getType() == type)) {
                    jobs.add(new Job().type(type).status(JobStatus.successful).message("Not initialized"));
                }
            }
        }
        return jobs;
    }
    
    public String refresh() {
        jobs = null;
        jobs = getJobs();
        return null;
    }
    
    public void startJob(ActionEvent event) {
        Job job = (Job) event.getComponent().getAttributes().get("job");
        if (JobStatus.running == job.getStatus()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Job already running"));
            return;
        }
        BackgroundTask task = new ManualContext().lookupCDI(BackgroundJobTypeFactory.getBackgroudJobClass(job.getType()));
        task.execute();
    }
}
