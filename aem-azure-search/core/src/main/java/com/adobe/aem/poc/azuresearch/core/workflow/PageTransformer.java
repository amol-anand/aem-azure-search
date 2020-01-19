package com.adobe.aem.poc.azuresearch.core.workflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;

@Component()
public class PageTransformer {
	
	private static final Logger LOG = LoggerFactory.getLogger(PageTransformer.class);
	
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    /** Service to create HTTP Servlet requests and responses */
    @Reference
    private RequestResponseFactory requestResponseFactory;

    /** Service to process requests through Sling */
    @Reference
    private SlingRequestProcessor requestProcessor;

    @Reference
    private Externalizer externalizer;
	
	
	public AzureDocument transform (Page page) throws ServletException, IOException {
		
		Resource PageResource = page.adaptTo(Resource.class);
		String path = page.getPath();
        ValueMap props = page.getContentResource().getValueMap();
        
        AzureDocument document = new AzureDocument();

        document.path = JcrUtil.createValidName(StringUtils.lowerCase(StringUtils.trim(path)), JcrUtil.HYPHEN_LABEL_CHAR_MAPPING);
        LOG.info("AZURE SEARCH: azurePath is "+ document.path);

        document.URL = externalizer.publishLink(PageResource.getResourceResolver(),path) + ".html";
        LOG.info("AZURE SEARCH: azureURL is "+ document.URL);

        document.authorURL = path + ".nocloudconfigs.html";
        LOG.info("AZURE SEARCH: authorURL is "+ document.authorURL);

        document.name = PageResource.getName();
        LOG.info("AZURE SEARCH: azureName is "+ document.name);


        document.name = props.get("jcr:title", document.name);
        LOG.info("AZURE SEARCH: azureTitle is "+ document.title);

        document.description = props.get("jcr:description", "");
        LOG.info("AZURE SEARCH: azureDescription is "+ document.description);

        document.tags = props.get("cq:tags", String[].class);
        //LOG.info("AZURE SEARCH: azureTags is "+azureTags);

        document.tagsValue = "";
        if(document.tags != null){
            document.tagsValue = String.join(",", document.tags);
        }
        LOG.info("AZURE SEARCH: azureTagsVal is "+ document.tagsValue);

        // Make call to get HTML rep of the resource if it is a page.
        /* Setup request */
        HttpServletRequest req = requestResponseFactory.createRequest("GET", document.authorURL);
        WCMMode.DISABLED.toRequest(req);

        /* Setup response */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse resp = requestResponseFactory.createResponse(out);

        /* Process request through Sling */
        requestProcessor.processRequest(req, resp, PageResource.getResourceResolver());
        document.html = out.toString();
		
        return document;
	}

}
