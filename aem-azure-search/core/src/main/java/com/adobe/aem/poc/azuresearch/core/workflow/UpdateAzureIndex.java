package com.adobe.aem.poc.azuresearch.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.commons.Externalizer;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.*;
import com.day.cq.commons.jcr.JcrUtil;
import org.apache.commons.lang.StringUtils;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import java.net.URLEncoder;
import com.day.cq.contentsync.handler.util.RequestResponseFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.Session;
import java.io.ByteArrayOutputStream;
import org.apache.sling.engine.SlingRequestProcessor;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;

/**
 * Created by amol on 2/11/19.
 */
@Designate(ocd=UpdateAzureIndex.Config.class)
@Component(service = WorkflowProcess.class, property = { "process.label=Update Azure Index",
        "value = Update Azure Index" })
public class UpdateAzureIndex implements WorkflowProcess {

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

    private static final Logger LOG = LoggerFactory.getLogger(UpdateAzureIndex.class);
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    private static final String CONTENT_ROOT_PATH = "/content";

    Config config;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private PageTransformer transformer;

    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        WorkflowData workflowData = item.getWorkflowData();
        if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            if(workflowData.getPayload().toString().startsWith(CONTENT_ROOT_PATH)) {
                //make http call to invoke IO Runtime Sequence to trigger update to search index and pass in parameters
                //if any error, retry.
                try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                	ResourceResolver resourceResolver = session.adaptTo(ResourceResolver.class);	
                	)
                {
                    String path = workflowData.getPayload().toString();
                    LOG.info("AZURE SEARCH: payload path is "+path);
                    
                    Page page = resourceResolver.adaptTo(PageManager.class).getPage(path);
                    
                    AzureDocument doc = transformer.transform(page);
                    
                    if(doc != null){

                      //Send a POST call to the adobeio runtime endpoint
                      HttpPost httpPost = new HttpPost(config.adobeioRuntimeEndpoint());

                      List<NameValuePair> postParameters = doc.asPostParameter();
                      postParameters.add(new BasicNameValuePair("azureEndpoint", config.azureEndpoint()));
                      postParameters.add(new BasicNameValuePair("azureEndpointApiKey", config.azureEndpointApiKey()));


                      httpPost.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));

                      HttpResponse postResponse = httpClient.execute(httpPost);
                      LOG.debug("Response:"+postResponse);

                      if (postResponse.getStatusLine().getStatusCode() != 200) {
                        LOG.error(postResponse.getStatusLine().getReasonPhrase());
                        throw new WorkflowException("Failed : HTTP error code : "
                        + postResponse.getStatusLine().getStatusCode());
                      } else{
                        LOG.debug("200 Response for POST call");
                        BufferedReader postBr = new BufferedReader(new InputStreamReader((postResponse.getEntity().getContent())));

                        String postOutput;
                        String postResponseBody="";
                        while ((postOutput = postBr.readLine()) != null) {
                          postResponseBody = postResponseBody + postOutput;
                        }
                        LOG.info("OMNICHANNEL: Post Response: "+postResponseBody);
                      }
                    } 
                }
                catch (Exception e) {
                    LOG.error(e.toString());
                    throw new WorkflowException(e.getMessage(), e);
                }
            }
        }
    }

    @Activate
    protected void activate(final Config config) {
        this.config = config;
    }
}
