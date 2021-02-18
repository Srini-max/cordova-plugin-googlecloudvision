#import "darkmode.h"
@implementation darkmode


-(void)darkmode:(CDVInvokedUrlCommand*)command{
  CDVPluginResult * pluginResult = nil;

    if (@available(iOS 12.0, *)) {
      switch (UIScreen.mainScreen.traitCollection.userInterfaceStyle) {
      case UIUserInterfaceStyleDark:
        // put your dark mode code here
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: @"true"];
        break;
      case UIUserInterfaceStyleLight:
      case UIUserInterfaceStyleUnspecified:
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_OK messageAsString: @"false"];
        break;
      default:
        pluginResult = [CDVPluginResult resultWithStatus: CDVCommandStatus_ERROR];
        break;
      }
    }

    [self.commandDelegate sendPluginResult: pluginResult callbackId: command.callbackId];

}

@end
