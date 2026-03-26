package org.braun.cookbook.web;

import org.apache.commons.lang3.StringUtils;

import jakarta.faces.application.ViewHandler;
import jakarta.faces.application.ViewHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.util.Constants;

public class CustomViewHandler extends ViewHandlerWrapper {

    private static final Logger LOG = LogManager.getLogger();

    private final ViewHandler wrappped;

    private boolean rewriteForProxy;

    public CustomViewHandler(ViewHandler wrappped) {
        super();
        this.wrappped = wrappped;
        rewriteForProxy = false;
    }

    @Override
    public ViewHandler getWrapped() {
        return wrappped;
    }

    @Override
    public String getActionURL(FacesContext context, String viewId) {
        String url = super.getActionURL(context, viewId);
        LOG.debug("The getActionURL: " + url);
        url = addContextPath(context, url);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String pfdlgcid = params.get(Constants.DialogFramework.CONVERSATION_PARAM);

        if (url.contains(Constants.DialogFramework.CONVERSATION_PARAM)) {
            return url;
        } else {
            if (pfdlgcid == null) {
                return url;
            } else {
                if (url.indexOf('?') == -1) {
                    return url + "?pfdlgcid=" + pfdlgcid;
                } else {
                    return url + "&pfdlgcid=" + pfdlgcid;
                }
            }
        }
    }

    @Override
    public String getRedirectURL(FacesContext context, String viewId, Map<String, List<String>> parameters, boolean includeViewParams) {
        String url = super.getRedirectURL(context, viewId, parameters, includeViewParams);
        LOG.debug("The getRedirectURL: " + url);
        return url;
    }

    @Override
    public String getResourceURL(FacesContext context, String path) {
        String url = super.getResourceURL(context, path);
        LOG.debug("The getResourceURL: = " + url);
        return addContextPath(context, url);
    }

    private String addContextPath(FacesContext context, String url) {
        if (rewriteForProxy) {
            final HttpServletRequest request = ((HttpServletRequest) context.getExternalContext().getRequest());
            String result = url;
            if (url.startsWith("/")) {
                int subpath = StringUtils.countMatches(getPath(request), "/") - 1;
                String pathPrefix = "";
                if (subpath > 0) {
                    while (subpath > 0) {
                        pathPrefix += "/..";
                        subpath--;
                    }
                    pathPrefix = StringUtils.removeStart(pathPrefix, "/");
                }
                result = pathPrefix + result;
            }
            return result;
        } else {
            return url;
        }
    }

    private String getPath(final HttpServletRequest request) {
        try {
            return StringUtils.replace(new URI(request.getRequestURI()).getPath(), "//", "/");
        } catch (final URISyntaxException e) {
            return StringUtils.EMPTY;
        }
    }

}
