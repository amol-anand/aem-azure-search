# Adding Azure Cognitive Search to Adobe Experience Manager
These are guidelines to help companies add an [Azure Cognitive Search](https://docs.microsoft.com/en-us/azure/search/) service to Adobe Experinece Manager and will work on these deployment architectures for AEM:

1. On-premise hosting
2. Managed IaaS service
3. Managed PaaS service

## Requirements
1. Running Adobe AEM instance with admin permissions
2. Microsoft Azure Subscription with the ability to provision services.

### Provisioning Azure Cognitive Search

1. From the [Azure Portal](https://portal.azure.com/), create a resource by selecting  **+ Create a resource*** and type *Azure Cognitive Search* into th **Search the Marketplace** field.
2. **Create** the resource using appropriate options.   You can start testing with the Free tier.
3. Once the search service has been created, copy the **Url** from the **Overview** page for later use.
4. Once the search service has been created, open the resource and select the **Keys** section in the *Settings*.
5. Copy the **Primary admin key** for later use.  

### Configuring Azure Search
1. Using the Uri from step 3 and the key from step 4 above you can create an index with a REST call via PowerShell, Postman, or any other tool that can make REST calls:

#### PowerShell
This uses the [CreateIndex.json](Scripts/CreateIndex.json) script that is in the same directory as the PowerScript:

    $adminKey  = "put your key here"
    
    $url = "put your search Url here"
    
    $name = "aemsearch'  #
    
    $headers = @{
        'api-key' = $adminKey
        'Content-Type' = 'application/json' 
        'Accept' = 'application/json' 
    }

    $uri = $url + "indexes/" + $name + "?api-version=2019-05-06";

    Invoke-RestMethod -Uri ($uri) -Headers $headers -Method Put -Body (Get-Content .\CreateIndex.json -Raw) | ConvertTo-Json
If successful, this will have created an index.

### Configuring Adobe Experience Manager
