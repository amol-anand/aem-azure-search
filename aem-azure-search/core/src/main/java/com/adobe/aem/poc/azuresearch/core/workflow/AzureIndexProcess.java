package com.adobe.aem.poc.azuresearch.core.workflow;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.engine.SlingRequestProcessor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.Externalizer;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import com.day.cq.wcm.api.WCMMode;

/**
 * Created by amol on 2/11/19.
 */
@Designate(ocd=AzureIndexProcess.Config.class)
@Component(service = WorkflowProcess.class, immediate = true,
    property = {"process.label=Azure Search - Update Index"})
public class AzureIndexProcess implements WorkflowProcess {

  @ObjectClassDefinition(name="Update Azure Index Config",
                         description = "Update Azure Index with AEM Content")
  public static @interface Config {
      @AttributeDefinition(name = "Azure Endpoint",
                            description = "URL to the Azure Index endpoint you want to update")
      String azureEndpoint() default "";

      @AttributeDefinition(name = "Azure Endpoint Api-Key",
                            description = "Api Key for the Azure Index endpoint you want to update")
      String azureEndpointApiKey() default "";

      @AttributeDefinition(name = "Adobe IO Runtime Endpoint",
                           description = "URL of the AIO Runtime Endpoint that will talk to Azure Search")
      String adobeioRuntimeEndpoint() default "";
  }

    private static final Logger LOG = LoggerFactory.getLogger(AzureIndexProcess.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    private static final String CONTENT_ROOT_PATH = "/content";
    private String azureEndpoint;
    private String azureEndpointApiKey;
    private String adobeioRuntimeEndpoint;
    private ResourceResolver resourceResolver;

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

    @Override
    public void execute(final WorkItem item, final WorkflowSession session, final MetaDataMap args) throws WorkflowException {
        final WorkflowData workflowData = item.getWorkflowData();
        if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            if(workflowData.getPayload().toString().startsWith(CONTENT_ROOT_PATH)) {
                //make http call to invoke IO Runtime Sequence to trigger update to search index and pass in parameters
                //if any error, retry.
                try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
                {
                    final String path = workflowData.getPayload().toString();
                    LOG.info("AZURE SEARCH: payload path is "+path);

                    resourceResolver = session.adaptTo(ResourceResolver.class);
                    final Session jcrSession = session.adaptTo(Session.class);
                    LOG.info("AZURE SEARCH: resourceResolver details: "+resourceResolver);
                    LOG.info("AZURE SEARCH: jcrSession details: "+jcrSession);

                    final Resource resource = resourceResolver.getResource(path);

                    final ValueMap props = resource.getChild("jcr:content").getValueMap();

                    final String azurePath = JcrUtil.createValidName(StringUtils.lowerCase(StringUtils.trim(path)), JcrUtil.HYPHEN_LABEL_CHAR_MAPPING);
                    LOG.info("AZURE SEARCH: azurePath is "+azurePath);

                    final String azureURL = externalizer.publishLink(resourceResolver,path) + ".html";
                    LOG.info("AZURE SEARCH: azureURL is "+azureURL);

                    final String authorURL = path + ".nocloudconfigs.html";
                    LOG.info("AZURE SEARCH: authorURL is "+authorURL);

                    final String azureName = resource.getName();
                    LOG.info("AZURE SEARCH: azureName is "+azureName);

                    String azureTitle = "";
                    if(props.containsKey("jcr:title")){
                      azureTitle = props.get("jcr:title", azureName);
                    }
                    LOG.info("AZURE SEARCH: azureTitle is "+azureTitle);

                    String azureDescription = "";
                    if(props.containsKey("jcr:description")){
                      azureDescription = props.get("jcr:description", "");
                    }
                    LOG.info("AZURE SEARCH: azureDescription is "+azureDescription);

                    final String[] azureTags = props.get("cq:tags", String[].class);
                    //LOG.info("AZURE SEARCH: azureTags is "+azureTags);

                    String azureTagsVal = "";
                    if(azureTags != null){
                        azureTagsVal = String.join(",", azureTags);
                    }
                    LOG.info("AZURE SEARCH: azureTagsVal is "+azureTagsVal);

                    // Make call to get HTML rep of the resource if it is a page.
                    /* Setup request */
                    final HttpServletRequest req = requestResponseFactory.createRequest("GET", authorURL);
                    WCMMode.DISABLED.toRequest(req);

                    /* Setup response */
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    final HttpServletResponse resp = requestResponseFactory.createResponse(out);

                    /* Process request through Sling */
                    requestProcessor.processRequest(req, resp, resourceResolver);
                    final String html = out.toString();

                    if(html != null){
                      LOG.info("AZURE SEARCH: html output is not null");
                      LOG.debug("AZURE SEARCH: authorURLResponse: "+html);

                      //Send a POST call to the adobeio runtime endpoint
                      final HttpPost httpPost = new HttpPost(adobeioRuntimeEndpoint);

                      final ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                      postParameters.add(new BasicNameValuePair("type", "azuresearch/update-document"));
                      postParameters.add(new BasicNameValuePair("azureEndpoint", azureEndpoint));
                      postParameters.add(new BasicNameValuePair("azureEndpointApiKey", azureEndpointApiKey));
                      postParameters.add(new BasicNameValuePair("azurePath", azurePath));
                      postParameters.add(new BasicNameValuePair("azureURL", azureURL));
                      postParameters.add(new BasicNameValuePair("azureName", azureName));
                      postParameters.add(new BasicNameValuePair("azureTitle", azureTitle));
                      postParameters.add(new BasicNameValuePair("azureDescription", azureDescription));
                      postParameters.add(new BasicNameValuePair("azureTags", azureTagsVal));
                      postParameters.add(new BasicNameValuePair("azureContent", html));

                      httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

                      final HttpResponse postResponse = httpClient.execute(httpPost);
                      LOG.debug("Response:"+postResponse);

                      if (postResponse.getStatusLine().getStatusCode() != 200) {
                        LOG.error(postResponse.getStatusLine().getReasonPhrase());
                        throw new WorkflowException("Failed : HTTP error code : "
                        + postResponse.getStatusLine().getStatusCode());
                      } else{
                        LOG.debug("200 Response for POST call");
                        final BufferedReader postBr = new BufferedReader(new InputStreamReader(postResponse.getEntity().getContent()));

                        String postOutput;
                        String postResponseBody="";
                        while ((postOutput = postBr.readLine()) != null) {
                          postResponseBody = postResponseBody + postOutput;
                        }
                        LOG.info("OMNICHANNEL: Post Response: "+postResponseBody);
                      }
                    } else {
                      LOG.error("AZURE SEARCH: html is null!!");
                    }
                }
                catch (final Exception e) {
                    LOG.error(e.toString());
                    throw new WorkflowException(e.getMessage(), e);
                }
                finally{
                    if(resourceResolver != null&&resourceResolver.isLive()){
                        resourceResolver.close();
                    }
                }
            }
        }
    }

    @Activate
    protected void activate(final Config config) {
        adobeioRuntimeEndpoint = config.adobeioRuntimeEndpoint();
        azureEndpoint = config.azureEndpoint();
        azureEndpointApiKey = config.azureEndpointApiKey();
    }
}
