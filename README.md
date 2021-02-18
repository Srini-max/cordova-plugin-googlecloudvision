# Cordova OSTheme DarkMode


This plugin **only detects if dark (also called night) mode is enable or disable**.


### Install
```
cordova plugin add cordova-ostheme-darkmode
```

### Example
```js
OSTheme.darkmode(
  function(res) {
    alert("Dark Mode Enabled :- "+res);
  },
  function(err) {
    alert(err);
  });

```
