package org.braun.cookbook.common;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Provides simple way how to obtain the instance of managed beans in
 * non-managed environment (POJOs, etc).
 *
 * @author Vaclav Svejcar (v.svejcar@norcane.cz)
 */
public class ManualContext {

    private final Context ctx;
    private final BeanManager beanManager;

    public ManualContext() {
        try {
            ctx = new InitialContext();

            // manual JNDI lookupCDI for the CDI bean manager (JSR 299)
            beanManager = (BeanManager) ctx.lookup(
                    "java:comp/BeanManager");
        } catch (NamingException ex) {
            throw new IllegalStateException(
                    "cannot perform JNDI lookup for CDI BeanManager");
        }
    }

    /**
     * Returns the instance of the CDI managed bean, specified by its class type
     * and optionally by annotations/qualifiers.
     *
     * @param <T> class type of the desired CDI managed bean
     * @param theClass class of the desired CDI manager bean
     * @param annotations CDI manager bean annotations/qualifiers
     * @return instance of the CDI managed bean
     */
    public <T> T lookupCDI(Class<T> theClass, Annotation... annotations) {
        Iterator<Bean<?>> it = beanManager.getBeans(theClass, annotations).iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException(
                    "cannot find instance of the requested type: "
                    + theClass.getName());
        }
        Bean<T> bean = (Bean<T>) it.next();
        CreationalContext cCtx = beanManager.createCreationalContext(bean);
        T beanReference = (T) beanManager.getReference(bean, theClass, cCtx);

        return beanReference;
    }
}
