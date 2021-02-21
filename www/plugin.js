cordova.addConstructor(function() {
    function CloudVision() {

    }

    CloudVision.prototype.readtext = function(successCallback,errorCallback){
        cordova.exec(successCallback, errorCallback, "CloudVision", "CloudVision", []);
    }

   
    window.CloudVision = new CloudVision()
    return window.CloudVision
});
