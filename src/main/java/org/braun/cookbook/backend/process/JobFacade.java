package org.braun.cookbook.backend.process;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.braun.cookbook.backend.dao.JobDao;
import org.braun.cookbook.backend.entity.JobEntity;
import org.braun.cookbook.backend.mapping.JobMapper;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.common.CookBookException;
import org.braun.cookbook.common.CookBookException.AlreadyRunningException;

/**
 *
 * @author mbraun
 */
@Named
@Stateless
public class JobFacade {

    @Inject
    private JobDao jobDao;
    
    public Job begin(BackgroundJobType type) throws AlreadyRunningException {
        JobEntity entity = jobDao.findByType(type);
        if (entity == null) {
            Job job = new Job().type(type).status(JobStatus.running).started(new Date()).message("Started");
            jobDao.create(JobMapper.getInstance().map(job));
            job = JobMapper.getInstance().map(jobDao.findByType(type));
            return job;
        } else {
            if (JobStatus.running.name().equals(entity.getStatus())) {
                throw CookBookException.newAlreadyRunningException(type);
            }
            Job job = new Job().id(entity.getId()).type(type).status(JobStatus.running).started(new Date()).message("Started");
            jobDao.merge(JobMapper.getInstance().map(job));
            return job;
        }
    }
    
    public Job end(JobResult jobResult) {
        Job job = JobMapper.getInstance().map(jobDao.findByType(jobResult.getType()));
        if (job == null) {
            return null;
        }
        job.status(jobResult.getStatus())
                .message(jobResult.getMessage())
                .finished(new Date())
                .addAllInformation(jobResult.getInformation());
        jobDao.merge(JobMapper.getInstance().map(job));
        return job;
    }
    
    public List<Job> findAll() {
        List<JobEntity> list = jobDao.findAll();
        List<Job> result = new ArrayList<>(list.size());
        for (JobEntity entity : list) {
            result.add(JobMapper.getInstance().map(entity));
        }
        return result;
    }
    
    public Job findByType(BackgroundJobType type) {
        return JobMapper.getInstance().map(jobDao.findByType(type));
    }

    public void setJobDao(JobDao jobDao) {
        this.jobDao = jobDao;
    }
    
}
