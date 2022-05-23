package edu.harvard.iq.dataverse.util.urlrewrite;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

public class RewriteViewHandler extends ViewHandlerWrapper {

    private static final Logger logger = Logger.getLogger(RewriteViewHandler.class.getCanonicalName());

    private static final Map<String, String> URLS = new HashMap<>();

    private final ViewHandler wrapped;

    public RewriteViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
        Map<String, String> rewrittenUrls = RewrittenURLs.getInstance().getURLs();

        if (rewrittenUrls != null && !rewrittenUrls.isEmpty()) {
            rewrittenUrls.entrySet().stream().forEach((entry) -> {
                URLS.put(entry.getValue(), entry.getKey());
            });
        }
    }

    @Override
    public String getActionURL(FacesContext context, String viewId) {
        String actionURL = super.getActionURL(context, viewId);

        String actionURLWithoutParam;
        String actionParams;
        int indexOfParam = actionURL.indexOf('?');
        if (indexOfParam != -1) {
            actionURLWithoutParam = actionURL.substring(indexOfParam);
            actionParams = actionURL.substring(0, indexOfParam);
        } else {
            actionURLWithoutParam = actionURL;
            actionParams = "";
        }
        String contextPath = context.getExternalContext().getRequestContextPath();
        String action = actionURLWithoutParam.substring(contextPath.length());

        if (URLS.containsKey(action)) {
            String newURL = contextPath + URLS.get(action) + actionParams;
            return newURL;
        }
        return actionURL;
    }

    @Override
    public ViewHandler getWrapped() {
        return wrapped;
    }

}
