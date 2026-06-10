package org.braun.cookbook.backend.rest;


import jakarta.inject.Inject;
import org.braun.cookbook.backend.model.Keyword;

import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.*;
import org.braun.cookbook.backend.process.KeywordFacade;
import org.braun.cookbook.backend.process.KeywordFactory;
import org.braun.cookbook.web.ui.AdminBean;

@Path("/keyword")
@io.swagger.annotations.Api("The keyword API")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2026-03-13T15:23:31.280172027+01:00[Europe/Berlin]", comments = "Generator version: 7.7.0")
public class KeywordApi  {

    @Inject
    private AdminBean adminBean;
    
    @jakarta.ws.rs.GET
    @Path("/findAll")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = Keyword.class, responseContainer = "List", tags={ "keyword", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = Keyword.class, responseContainer = "List")
    })
    public Response findAllKeywords(@Context SecurityContext securityContext)
    throws NotFoundException {
        List<Keyword> result = KeywordFactory.getInstance().getListByName(null);
        return Response.ok().entity(result).build();
    }

    @jakarta.ws.rs.GET
    @Path("/refresh")
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = Void.class, tags={ "keyword", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "Cache refreshed", response = Void.class)
    })
    public Response refreshKeywords(@Context SecurityContext securityContext)
    throws NotFoundException {
        adminBean.refreshCaches();
        return Response.ok().build();
    }
}
