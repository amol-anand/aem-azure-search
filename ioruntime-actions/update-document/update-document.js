const Request = require('request');

function main(args) {

    let searchAction = "mergeOrUpload"
    // let azureIndex = args.azureIndex;
    let azurePath = args.azurePath;
    let azureURL = args.azureURL;
    let azureName = args.azureName;
    let azureTitle = args.azureTitle;
    let azureDescription = args.azureDescription;
    let azureTags = args.azureTags.split(",");
    console.log("Tags: ",azureTags);
    let azureContent = args.azureContent;
    let azureEndpoint = args.azureEndpoint;
    console.log("Endpoint is: "+azureEndpoint);
    let azureEndpointApiKey = args.azureEndpointApiKey;
    /** Send POST request to Azure endpoint **/

    let azureDocument = {
      "@search.action":searchAction,
      "Path":azurePath,
      "URL":azureURL,
      "Name":azureName,
      "Title":azureTitle,
      "Description":azureDescription,
      "Tags":azureTags,
      "Content":azureContent
    };
    let post_data = {
      "value":[azureDocument]
    };
    //console.log("post_data: %j",post_data);

    let cOptions = {
      url: azureEndpoint,
      method: 'POST',
      body: post_data,
      json: true,
      headers: {
        'content-type': 'application/json',
        'api-key': azureEndpointApiKey
      }
    };

    Request.post(cOptions, function(cerror, cresponse, cbody){
      if (!cerror && cresponse.statusCode == 200) {
        console.log("RESPONSE: %j",cresponse);
      } else{
        console.log("Response Status Code",cresponse.statusCode);
        console.log("ERROR: %j",cerror);
        console.log("RESPONSE: %j",cresponse);
      }

    }); //end email trigger request
}
exports.main = main;
