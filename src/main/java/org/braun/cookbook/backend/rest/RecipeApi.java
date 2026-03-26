package org.braun.cookbook.backend.rest;


import io.swagger.annotations.ApiParam;
import jakarta.inject.Inject;

import java.util.List;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.*;
import org.braun.cookbook.backend.model.RecipeShort;
import org.braun.cookbook.backend.model.RecipeSolr;
import org.braun.cookbook.backend.model.Suggestion;
import org.braun.cookbook.backend.process.ConditionParseException;
import org.braun.cookbook.backend.process.RecipeFacade;

@Path("/recipe")
@io.swagger.annotations.Api("the recipe API")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2026-03-13T15:23:31.280172027+01:00[Europe/Berlin]", comments = "Generator version: 7.7.0")
public class RecipeApi  {

    @Inject
    private RecipeFacade recipeFacade;
    
    @Path("/search")
    @jakarta.ws.rs.GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = RecipeShort.class, responseContainer = "List", tags={ "recipe", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = RecipeShort.class, responseContainer = "List")
    })
    public Response searchByAttributes(@ApiParam(value = "blank separted list of words") @QueryParam("content")  String content,@ApiParam(value = "List of keyword ids") @QueryParam("keywords")  List<Long> keywords,@ApiParam(value = "rating from 1 to 5") @QueryParam("rating")  Integer rating,@ApiParam(value = "Directory") @QueryParam("directory")  String directory,@ApiParam(value = "Directory") @QueryParam("evaluated")  Boolean evaluated,@ApiParam(value = "") @QueryParam("date_from")  String dateFrom,@ApiParam(value = "") @QueryParam("date_to")  String dateTo,@Context SecurityContext securityContext)
    throws NotFoundException {
        try {
            List<RecipeShort> result = recipeFacade.searchByAttributes(content, keywords, rating, directory, evaluated, dateFrom, dateTo);
            return Response.ok().entity(result).build();
        } catch (ConditionParseException e) {
            return Response.serverError().build();
        }
    }
    @jakarta.ws.rs.GET
    @Path("/suggest/content")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = Suggestion.class, responseContainer = "List", tags={ "recipe", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = Suggestion.class, responseContainer = "List")
    })
    public Response suggestContent(@ApiParam(value = "part of word to search for") @QueryParam("value")  String value,@Context SecurityContext securityContext)
    throws NotFoundException {
        List<Suggestion> result = recipeFacade.getSuggestion(RecipeSolr.FIELD_CONTENT_SUGGEST, value);
        return Response.ok().entity(result).build();
    }
    @jakarta.ws.rs.GET
    @Path("/suggest/directory")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = Suggestion.class, responseContainer = "List", tags={ "recipe", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = Suggestion.class, responseContainer = "List")
    })
    public Response suggestDirectory(@ApiParam(value = "part of word to search for") @QueryParam("value")  String value,@Context SecurityContext securityContext)
    throws NotFoundException {
        List<Suggestion> result = recipeFacade.getSuggestion(RecipeSolr.FIELD_PATH_PARENT, value);
        return Response.ok().entity(result).build();
    }
}
