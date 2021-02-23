# Cordova Google Cloud Vision


This plugin **Read Text from the paper/image using google Cloud Vision API**.


### Example
```js
CloudVision.readtext('cameragallerytype',
  function(res) {
    alert("Text read from API :- "+res);
  },
  function(err) {
    alert(err);
  });

```
