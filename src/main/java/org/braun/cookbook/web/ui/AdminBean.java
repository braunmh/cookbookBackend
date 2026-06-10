package org.braun.cookbook.web.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import org.braun.cookbook.backend.process.KeywordFacade;
import org.braun.cookbook.backend.process.KeywordFactory;

/**
 *
 * @author mbraun
 */
@Named
@RequestScoped
public class AdminBean implements Serializable {
    
    @Inject
    private KeywordFacade keywordFacade;
    
    public void refreshCaches() {
        KeywordFactory.getInstance().refresh(keywordFacade.findAll());
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Cache für Stichworte aktualisiert", ""));
    }
}
