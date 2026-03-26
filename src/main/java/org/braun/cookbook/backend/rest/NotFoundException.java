package org.braun.cookbook.backend.rest;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2026-03-13T15:23:31.280172027+01:00[Europe/Berlin]", comments = "Generator version: 7.7.0")
public class NotFoundException extends ApiException {
    private int code;
    public NotFoundException (int code, String msg) {
        super(code, msg);
        this.code = code;
    }
}
