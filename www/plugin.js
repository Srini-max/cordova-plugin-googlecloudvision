cordova.addConstructor(function() {
    function CloudVision() {

    }

    CloudVision.prototype.readtext = function(cameragallerytype,successCallback,errorCallback){
        cordova.exec(successCallback, errorCallback, "CloudVision", "CloudVision", [cameragallerytype]);
    }


    window.CloudVision = new CloudVision()
    return window.CloudVision
});
