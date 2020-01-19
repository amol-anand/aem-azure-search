package com.adobe.aem.poc.azuresearch.core.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class AzureDocument {
	
	
	protected String path;
	
	protected String URL;
	
	protected String authorURL;
	
	protected String name;
	
	protected String title;
	
	protected String description;
	
	protected String[] tags;
	
	protected String tagsValue;
	
	protected String html;
	
	
	
	List<NameValuePair> asPostParameter() {
		ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("type", "azuresearch/update-document"));
        postParameters.add(new BasicNameValuePair("azurePath", path));
        postParameters.add(new BasicNameValuePair("azureURL", URL));
        postParameters.add(new BasicNameValuePair("azureName", name));
        postParameters.add(new BasicNameValuePair("azureTitle", title));
        postParameters.add(new BasicNameValuePair("azureDescription", description));
        postParameters.add(new BasicNameValuePair("azureTags", tagsValue));
        postParameters.add(new BasicNameValuePair("azureContent", html));
		
		return postParameters;
	}

}
