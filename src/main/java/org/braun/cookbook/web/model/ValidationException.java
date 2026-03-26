package org.braun.cookbook.web.model;

import jakarta.faces.application.FacesMessage;

/**
 *
 * @author mbraun
 */
public class ValidationException extends Exception {

    private final String fieldName;
    private final String[] parameter;
    
    public ValidationException(String fieldName, String message, String... parameter) {
        super(message);
        this.fieldName = fieldName;
        this.parameter = parameter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String[] getParameter() {
        return parameter;
    }
    
    public FacesMessage toFacesMessage() {
        return new FacesMessage(FacesMessage.SEVERITY_ERROR, getMessage(), getMessage());
    }
    
    public String getFieldName(String container) {
        return ("this".equals(fieldName)) ? container : fieldName;
    }
}
