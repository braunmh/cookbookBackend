package org.braun.cookbook.backend.rest;


import io.swagger.annotations.ApiParam;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.*;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.braun.cookbook.backend.model.RecipeShort;
import org.braun.cookbook.backend.model.RecipeSolr;
import org.braun.cookbook.backend.model.Suggestion;
import org.braun.cookbook.backend.process.ConditionParseException;
import org.braun.cookbook.backend.process.RecipeFacade;
import org.braun.cookbook.util.Configuration;
import org.braun.cookbook.web.ui.ImageBean;

@Path("/recipe")
@io.swagger.annotations.Api("the recipe API")
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJerseyServerCodegen", date = "2026-03-13T15:23:31.280172027+01:00[Europe/Berlin]", comments = "Generator version: 7.7.0")
public class RecipeApi  {

    private static final Logger LOG = LogManager.getLogger();
    
    @Inject
    private ImageBean imageBean;
    
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
    @jakarta.ws.rs.GET
    @Path("/image")
    @Produces({ "image/jpeg" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = byte[].class, tags={ "recipe", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = byte[].class)
    })
    public Response getRecipeImage(@ApiParam(value = "part of word to search for", required = true) @QueryParam("imagePath") @NotNull  String imagePath,@Context SecurityContext securityContext)
    throws NotFoundException {
        if (imagePath.startsWith(ImageBean.PREFIX)) {
            byte[] image = imageBean.getImage(imagePath.substring(ImageBean.PREFIX.length()));
            return Response.ok().entity(image).build();
        }
        try (InputStream inputStream = new FileInputStream(Configuration.getInstance().getContentDirectory() + "/" + imagePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[2048];
            int length;
            while ((length = inputStream.read(buffer)) > -1) {
                baos.write(buffer, 0, length);
            }
            return Response.ok().entity(baos.toByteArray()).build();
        } catch (IOException e) {
            LOG.error("Reading image {}", imagePath);
        }
        return Response.ok().build();
    }
    
    @jakarta.ws.rs.GET
    @Path("/rating")
    @io.swagger.annotations.ApiOperation(value = "", notes = "", response = Void.class, tags={ "recipe", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "Update recipe successful", response = Void.class)
    })
    public Response rateRecipe(
            @ApiParam(value = "Path of the Recipe to change", required = true) @QueryParam("path") @NotNull  String path,
            @ApiParam(value = "Value to set for rating msut be between 0 and 5", required = true) @QueryParam("rating") @NotNull  Integer rating,@Context SecurityContext securityContext)
    throws NotFoundException {
        try {
            recipeFacade.rateRecipe(path, rating);
        } catch (ConditionParseException e) {
            LOG.error("rating Recipe {}, {}", path, rating);
        }
        return Response.ok().build();
    }
}
