package org.braun.cookbook.backend.process;

import jakarta.annotation.Resource;
import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.EJBContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import java.util.concurrent.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.BackgroundJobType;
import org.braun.cookbook.backend.model.Job;
import org.braun.cookbook.backend.model.JobResult;
import org.braun.cookbook.backend.model.JobStatus;
import org.braun.cookbook.common.CookBookException;

/**
 *
 * @author mbraun
 */
public abstract class BackgroundTask {
    
    private static final Logger LOG = LogManager.getLogger();

    @PersistenceContext(unitName = "cookbook")
    private EntityManager entityManager;

    @Inject
    private JobFacade jobFacade;
    
    @Resource
    private EJBContext context;

    public abstract BackgroundJobType getTaskName();
    
    @Asynchronous
    public Future<Boolean> execute() {
        JobResult result;
        try {
            UserTransaction userTransaction = context.getUserTransaction();
            userTransaction.begin();
            jobFacade.begin(getTaskName());
            result = doExecute();
            jobFacade.end(result);
            userTransaction.commit();
            return (result.getStatus() == JobStatus.successful)
                    ? new AsyncResult<>(Boolean.TRUE)
                    : new AsyncResult<>(Boolean.FALSE);
        } catch (NotSupportedException | SystemException | RollbackException
                | HeuristicMixedException | HeuristicRollbackException e) {
            LOG.error("Executing Job failed with Exception", e);
            return new AsyncResult<>(Boolean.FALSE);
        } catch (CookBookException.AlreadyRunningException e) {
            return new AsyncResult<>(Boolean.FALSE);
        }
    } 
    
    public abstract JobResult doExecute();

    protected EntityManager getEntityManager() {
        return entityManager;
    }
    
    public void setJobFacade(JobFacade jobFacade) {
        this.jobFacade = jobFacade;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
}
