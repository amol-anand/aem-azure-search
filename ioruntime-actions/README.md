# AEM Azure Search Integration - Adobe I/O Runtime Actions

This project contains the code that can be used to build Adobe I/O Runtime actions required by the AEM-Azure Search Integration. Adobe I/O is Adobe's serverless platform. Actions created here help offload functionality from AEM to run in the serverless platform and reduce load on AEM instances. Learn more about building and deploying serverless actions in Adobe I/O Runtime by checking out this developer guide: https://github.com/AdobeDocs/adobeio-runtime/blob/master/guides/creating_actions.md#deploying-zip-actions


## Modules

The main parts of the project are:

* actions: Folder containing each individual action that relates to this project.
* actions/config: For any environment variables that your actions might need and you don't want to pass to the action via POST data.
* manifest.yaml: YAML file with the configurations for each action that you want to deploy
* package.json: NPM modules added to this project for your actions to use



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


## Details of each action are listed below

POST Url:
```
Testing:
Production:
```

POST Body
```

```
