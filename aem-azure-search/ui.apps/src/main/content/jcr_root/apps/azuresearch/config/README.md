# Ralph Lauren Studio - Adobe I/O Runtime Actions

This project contains the code that can be used to build Adobe I/O Runtime actions. Adobe I/O is Adobe's serverless platform. Actions created here help offload functionality from AEM to run in the serverless platform and reduce load on AEM instances. Learn more about building and deploying serverless actions in Adobe I/O Runtime by checking out this developer guide: https://github.com/AdobeDocs/adobeio-runtime/blob/master/guides/creating_actions.md#deploying-zip-actions


## Modules

The main parts of the project are:

* actions: Folder containing each individual action that relates to this project.
* actions/config: For any environment variables that your actions might need and you don't want to pass to the action via POST data.
* manifest.yaml: YAML file with the configurations for each action that you want to deploy
* package.json: NPM modules added to this project for your actions to use


## Ralph Lauren I/O Runtime namespace

Ralph Lauren has their own namespace to deploy actions to under the Adobe I/O Runtime platform. The details can be found at https://rl-edam.atlassian.net/wiki/spaces/AM/pages/680329574/Ralph+Lauren+Adobe+I+O+Runtime

### Install Openwhisk CLI
https://github.com/AdobeDocs/adobeio-runtime/blob/master/tools/wsk_install.md

### Common wsk commands

Get a list of recent calls to the action with activation ids
```
wsk activation list
```

Get logs from a particular activation
```
wsk activation logs <activation-id>
```

Get more information about a particular activation
```
wsk activation get <activation-id>
```

Updates the action to be accessible by a URL and specifies which nodejs version to use
```
wsk action update ralphlauren/download-zip --web true --kind nodejs:10
```

Get the URL to use when calling the action. This should only be called after updating the action to be accessible from the web which is the previous command
```
wsk action get ralphlauren/download-zip --url
```

### Setting up the wskdeploy CLI

We are using wskdeploy to deploy complex actions to I/O Runtime. wskdeploy allows us to add external dependencies and package up our action easily using a YAML file.
Download and Install wskdeploy from https://github.com/apache/openwhisk-wskdeploy if you do not already have it.

To install `wskdeploy`, you either use brew:
 ```
 brew install wskdeploy
 ```
 or [download it](https://github.com/apache/incubator-openwhisk-wskdeploy/releases) and make sure it&rsquo;s in the system&rsquo;s `$PATH`.


## Build and Deploy

To build and deploy all the actions in this project to your I/O Runtime namespace, go to the root folder 'aio-runtime-actions'

    ./wskdeploy

Note: Before running 'wskdeploy', please make sure your ~/.wskprops file has the right namespace information from above


## How to call this action

POST Url:
```
Testing:
https://adobeioruntime.net/api/v1/web/amolanand/ralphlauren/download-zip.json?result=true

Production:
https://adobeioruntime.net/api/v1/web/ralphlauren/ralphlauren/download-zip.json?result=true
```

POST Body
```
{
  "bucketName": "",
  "accessKeyId": "",
  "secretAccessKey": "",
  "region": "",
  "keyName": "rootFolder/download-<timestamp>.zip",
  "urlExpireSeconds": 300,
  "urls": [
    {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332249_alternate1?$rl_392_pdp$", "name":"alternate1_1332249.jpg"},
    {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332420_lifestyle?$rl_470_pdp$", "name":"lifestyle_1332420.jpg"},
    {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332425_alternate10?$rla10_506_630$", "name":"alternate10_1332425.png"}
  ]
}
```
