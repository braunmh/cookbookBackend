package org.braun.cookbook.backend.rest;


import org.braun.cookbook.backend.model.Keyword;

import java.util.List;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.*;
import org.braun.cookbook.backend.process.KeywordFactory;

@Path("/keyword/findAll")
@io.swagger.annotations.Api("The keyword API")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2026-03-13T15:23:31.280172027+01:00[Europe/Berlin]", comments = "Generator version: 7.7.0")
public class KeywordApi  {

    @jakarta.ws.rs.GET
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
}
