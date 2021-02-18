cordova.addConstructor(function() {
    function OSTheme() {

    }

    OSTheme.prototype.darkmode = function(  successCallback, errorCallback ){
        cordova.exec(successCallback, errorCallback, "OSTheme", "darkmode", []);
    }

   
    window.OSTheme = new OSTheme()
    return window.OSTheme
});
