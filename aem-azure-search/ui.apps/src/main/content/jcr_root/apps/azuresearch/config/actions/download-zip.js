const Request = require('request');
const Config = require('config');
const AWS = require('aws-sdk');
const ZipStream = require('zip-stream');
const Stream = require('stream');

//recursive function to get urls and add them to the zip stream
function addNextFile(zip, queue) {
    //console.log("[Debug] In addNextFile()");
      var elem = queue.shift();
      var imageStream = Request(elem.url);
      zip.entry(imageStream, { name: elem.name }, err => {
          if(err){
            //if error, return the error message
            //console.log("[Error] In addNextFile(): ",err);
            return "Error in adding files to the zip file";
          }
          if(queue.length > 0){
            //if more items in queue, recursively call the same function with the next item
            //console.log("[Debug] Adding next file...");
              addNextFile(zip, queue);
          }
          else{
            //all items in queue are added to the zip file. finalize and close out the zip now.
            console.log("[Debug] Finalizing zip...");
              zip.finalize();
              return "Zip finalized";
          }
      });
}

//upload zip stream to S3 bucket and get signedUrl from s3 back
function generateZip(zip, queue, s3, bucketName, keyName, signedUrlExpireSeconds) {
  //Starting the stream of requests to the zip file here
  console.log("[Debug] In generateZip");
    const resultVal = new Promise(function(resolve, reject) {
      //upload zip stream to S3 bucket
      const uploadToS3Promise = uploadToS3(s3, bucketName, keyName, zip);
      //get signed url for user to download zip file from S3
      const getSignedUrlPromise = getSignedUrl(s3, bucketName, keyName, signedUrlExpireSeconds);
      //wait till both the above calls are done and then return the signed url back to the caller
      Promise.all([uploadToS3Promise,getSignedUrlPromise]).then(function(result){
        console.log("[Debug] Result from initial Promise.all: ",result[1]);
        var returnVal = {
                          'statusCode': 200,
                          'headers': { 'Content-Type': 'application/json' },
                          'body': result[1]
                        };
        resolve(returnVal);
      }).catch(function(err){
        console.log("[Error] Error in initial Promise.all in generateZip() ",err);
        reject("[Error] Error in initial Promise.all in generateZip() "+err);
      });
    });
    return resultVal;
}

//upload zip stream to S3 bucket
function uploadToS3(s3, bucketName, keyName, stream) {
  const params = {Bucket: bucketName, Key: keyName, Body: stream};
  return s3.upload(params).promise();
}

//get signed url for a key in S3 bucket
function getSignedUrl(s3, bucketName, keyName, signedUrlExpireSeconds) {
  console.log("[Debug] now getting pre-signed url from S3...");
  var gsuParams = {Bucket: bucketName, Key: keyName, Expires: signedUrlExpireSeconds};
  const signedUrl = new Promise(function(resolve, reject) {
    s3.getSignedUrl('getObject', gsuParams, function (err, url) {
      if (err) {
        console.log("[Error] in getting presigned url from s3: ", err);
        reject("[Error] in getting presigned url from s3: "+err);
      }
      if (url) {
        console.log('The URL is', url);
        // presignedURL = url;
        resolve(url);
      }
    });
  });
  return signedUrl;
}

// Sample params input to this function
// {
//   "bucketName": "",
//   "accessKeyId": "",
//   "secretAccessKey": "",
//   "region": "us-east-1",
//   "keyName": "rootFolder/download-<timestamp>.zip",
//   "urlExpireSeconds": 300,
//   "urls": [
//     {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332249_alternate1?$rl_392_pdp$", "name":"alternate1_1332249.jpg"},
//     {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332420_lifestyle?$rl_470_pdp$", "name":"lifestyle_1332420.jpg"},
//     {"url":"http://s7d9.scene7.com/is/image/PoloGSI/s7-1332425_alternate10?$rla10_506_630$", "name":"alternate10_1332425.png"}
//   ]
// }

//this is the main entrypoint to this runtime action
function main(params) {

      console.log("[Debug] Start of Download Zip Action");
      //console.log("[Debug] params: ",params);
      //Get parameters

      //list of urls and filenames that this action will download and stream to a zip in an S3 bucket
      let queue = params.urls;
      //Bucket name of AWS Bucket where we will store the zip file
      let bucketName = params.bucketName;
      //Key name of AWS key within above bucket which points to the zip file
      //Key can be folder: folder1/folder2/foo.txt
      let keyName = params.keyName;
      //This variable decides when to expire the signed url to the zip file in S3
      let signedUrlExpireSeconds = params.urlExpireSeconds;

      const accessKeyId = params.accessKeyId;
      const secretAccessKey = params.secretAccessKey;
      const region = params.region;

      //Set up AWS config based on input parameters
      console.log("[Debug] Setting up AWS Config ");

      AWS.config.update(
          {
              accessKeyId,
              secretAccessKey,
              region
          }
      );
      //Create S3 service object
      let s3 = new AWS.S3({apiVersion: '2006-03-01'});
      console.log("[Debug] Completed setting up AWS Config ");
      //Create Zip Stream
      const zip = new ZipStream();
      //Start requesting the urls and streaming them to the zip file just created
      addNextFile(zip,queue);
      //wait till zip is generated and uploaded to s3 and a signed url is available before returning
      //the result to the caller
      return Promise.all([generateZip(zip, queue, s3, bucketName, keyName, signedUrlExpireSeconds)])
      .then(function(result){
        console.log("[RESULT]: ", result[0]);
        return result[0];
      }).catch(function(err){
        console.log("[Error] Error in final Promise.all in main() ",err);
        var errObj = { error:{ err }};
        return errObj;
      });
}

module.exports.main = main;
