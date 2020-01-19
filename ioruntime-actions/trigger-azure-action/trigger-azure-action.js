const openwhisk = require('openwhisk');

function main(args) {
  let ow = openwhisk();
  const actionname = args.type;
  //invoke action and return activation id immediately while action runs async
  if(actionname != null){
    return ow.actions.invoke({
      name: actionname, // the name of the action to invoke
      blocking: false, // don't block until you get a response
      result: false,
      params: args
    }); 
  }
}
exports.main = main;
