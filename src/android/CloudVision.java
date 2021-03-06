package com.cordova.google.cloudvision;

import org.apache.cordova.* ;
import org.json.JSONArray;

import android.content.res.Configuration;
import android.provider.Settings;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import android.app.PendingIntent;
import android.app.Activity;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.LabeledIntent;
import android.os.Build;
import android.text.Html;
import android.util.Base64;
import android.view.Gravity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageProperties;
import com.google.api.services.vision.v1.model.WebDetection;
import com.google.api.services.vision.v1.model.WebEntity;
import com.google.api.services.vision.v1.model.WebImage;
import com.google.api.services.vision.v1.model.WebPage;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;

import com.google.common.io.BaseEncoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CloudVision extends CordovaPlugin {
  private static final String CLOUD_VISION_API_KEY = "AIzaSyA2bn4jglfLJza1BWj4YEVE4cYwxAgEu2U";

  public static final String FILE_NAME = "temp.jpg";
  private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
  private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

  private static final String TAG = "CloudVisionPlugin";
  private static final int GALLERY_PERMISSIONS_REQUEST = 0;
  private static final int GALLERY_IMAGE_REQUEST = 1;
  public static final int CAMERA_PERMISSIONS_REQUEST = 2;
  public static final int CAMERA_IMAGE_REQUEST = 3;

  private TextView mImageDetails;
  private ImageView mMainImage;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.i(TAG, "Initializing OCR Plugin");
  }

  public boolean execute(String action, CordovaArgs args, JSONArray data, CallbackContext callbackContext) {

    if (action.equals("readtext")) {
      try {
        if (args.optString(0) == "Camera") {
          startCamera();
        } else {
          startGalleryChooser();
        }

      } catch(Exception e) {
        Log.d(TAG, "Error: " + e.getMessage());
        callbackContext.error("Error: " + e.getMessage());
      }
      return true;

    } else {
      Log.d(TAG, "Wrong Function... nothing to do with this plugin");
      return false;
    }
  }
  public void startGalleryChooser() {
   // if (requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      Intent intent = new Intent();
      intent.setType("image/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      startActivityForResult(Intent.createChooser(intent, "Select a photo"), GALLERY_IMAGE_REQUEST);
 //   }
  }
 public static String getSignature(@NonNull PackageManager pm, @NonNull String packageName) {
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (packageInfo == null
                    || packageInfo.signatures == null
                    || packageInfo.signatures.length == 0
                    || packageInfo.signatures[0] == null) {
                return null;
            }
            return signatureDigest(packageInfo.signatures[0]);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String signatureDigest(Signature sig) {
        byte[] signature = sig.toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(signature);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
   public static boolean requestPermission(
            Activity activity, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String s : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) {
                permissionsNeeded.add(s);
            }
        }

        if (granted) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }


    public static boolean permissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {
        if (requestCode == permissionCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
  public void startCamera() {
  //  if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
      intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
  //  }
  }

  public File getCameraFile() {
    File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    return new File(dir, FILE_NAME);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
      uploadImage(data.getData());
    } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
      Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
      uploadImage(photoUri);
    }
  }

  @Override
  public void onRequestPermissionsResult(
  int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
    case CAMERA_PERMISSIONS_REQUEST:
      if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
        startCamera();
      }
      break;
    case GALLERY_PERMISSIONS_REQUEST:
      if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
        startGalleryChooser();
      }
      break;
    }
  }

  public void uploadImage(Uri uri) {
    if (uri != null) {
      try {
        // scale the image to save on bandwidth
        Bitmap bitmap = scaleBitmapDown(
        MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);

        callCloudVision(bitmap);

      } catch(IOException e) {
        Log.d(TAG, "Image picking failed because " + e.getMessage());
      }
    } else {
      Log.d(TAG, "Image picker gave us a null image.");
    }
  }

  private void callCloudVision(final Bitmap bitmap) throws IOException {

    // Do the real work in an async task, because we need to use the network anyway
    new AsyncTask < Object,
    Void,
    String > () {@Override
      protected String doInBackground(Object...params) {
        try {
          HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
          JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

          VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
            /**
                 * We override this so we can inject important identifying fields into the HTTP
                 * headers. This enables use of a restricted cloud platform API key.
                 */
            @Override
            protected void initializeVisionRequest(VisionRequest < ?>visionRequest)
            throws IOException {
              super.initializeVisionRequest(visionRequest);

              String packageName = getPackageName();
              visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

              String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

              visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
            }
          };

          Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
          builder.setVisionRequestInitializer(requestInitializer);

          Vision vision = builder.build();

          BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
          batchAnnotateImagesRequest.setRequests(new ArrayList < AnnotateImageRequest > () {
            {
              AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

              // Add the image
              Image base64EncodedImage = new Image();
              // Convert the bitmap to a JPEG
              // Just in case it's a format that Android understands but Cloud Vision
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
              byte[] imageBytes = byteArrayOutputStream.toByteArray();

              // Base64 encode the JPEG
              base64EncodedImage.encodeContent(imageBytes);
              annotateImageRequest.setImage(base64EncodedImage);

              // add the features we want
              // TODO: to add or remove features just (un)comment the blocks below
              annotateImageRequest.setFeatures(new ArrayList < Feature > () {
                {

                  Feature textDetection = new Feature();
                  textDetection.setType("TEXT_DETECTION");
                  textDetection.setMaxResults(10);
                  add(textDetection);

                  Feature landmarkDetection = new Feature();
                  landmarkDetection.setType("LANDMARK_DETECTION");
                  landmarkDetection.setMaxResults(10);
                  add(landmarkDetection);

                  Feature logoDetection = new Feature();
                  logoDetection.setType("LOGO_DETECTION");
                  logoDetection.setMaxResults(10);
                  add(logoDetection);

                  Feature faceDetection = new Feature();
                  faceDetection.setType("FACE_DETECTION");
                  faceDetection.setMaxResults(10);
                  add(faceDetection);

                  Feature imageProperties = new Feature();
                  imageProperties.setType("IMAGE_PROPERTIES");
                  imageProperties.setMaxResults(10);
                  add(imageProperties);

                  Feature webDetection = new Feature();
                  webDetection.setType("WEB_DETECTION");
                  webDetection.setMaxResults(10);
                  add(webDetection);
                }
              });

              // Add the list of one thing to the request
              add(annotateImageRequest);
            }
          });

          Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
          // Due to a bug: requests to Vision API containing large images fail when GZipped.
          annotateRequest.setDisableGZipContent(true);
          Log.d(TAG, "created Cloud Vision request object, sending request");

          BatchAnnotateImagesResponse response = annotateRequest.execute();
          return convertResponseToString(response);

        } catch(GoogleJsonResponseException e) {
          Log.d(TAG, "failed to make API request because " + e.getContent());
        } catch(IOException e) {
          Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
        }
        return "Cloud Vision API request failed. Check logs for details.";
      }

      protected void onPostExecute(String result) {}
    }.execute();
  }

  public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

    int originalWidth = bitmap.getWidth();
    int originalHeight = bitmap.getHeight();
    int resizedWidth = maxDimension;
    int resizedHeight = maxDimension;

    if (originalHeight > originalWidth) {
      resizedHeight = maxDimension;
      resizedWidth = (int)(resizedHeight * (float) originalWidth / (float) originalHeight);
    } else if (originalWidth > originalHeight) {
      resizedWidth = maxDimension;
      resizedHeight = (int)(resizedWidth * (float) originalHeight / (float) originalWidth);
    } else if (originalHeight == originalWidth) {
      resizedHeight = maxDimension;
      resizedWidth = maxDimension;
    }
    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
  }

  // TODO: analyse this method to understand how to get Cloud Vision data
  private String convertResponseToString(BatchAnnotateImagesResponse response) {
    String message = "";
    List < EntityAnnotation > annotations;
    AnnotateImageResponse annotateImageResponse = response.getResponses().get(0);

    message += "\n___\n# WEB DETECTION \n";
    WebDetection webDetection = annotateImageResponse.getWebDetection();
    if (webDetection != null) {
      List < WebEntity > webEntities = webDetection.getWebEntities();
      if (webEntities != null) {
        message += "\n§ Web Entities:\n";
        for (WebEntity webEntity: webEntities) {
          message += String.format(Locale.US, "> %.3f: %s \n", webEntity.getScore(), webEntity.getDescription());
        }
      }

      List < WebImage > fullMatchingImages = webDetection.getFullMatchingImages();
      if (fullMatchingImages != null) {
        message += "\n§ Full Matching Images:\n";
        for (WebImage fullMatchingImage: fullMatchingImages) {
          message += String.format(Locale.US, "> %s \n", fullMatchingImage.getUrl());
        }
      }

      List < WebImage > partialMatchingImages = webDetection.getPartialMatchingImages();
      if (partialMatchingImages != null) {
        message += "\n§ Partial Matching Images\n";
        for (WebImage partialMatchingImage: partialMatchingImages) {
          message += String.format(Locale.US, "> %s \n", partialMatchingImage.getUrl());
        }
      }

      List < WebImage > visuallySimilarImages = webDetection.getVisuallySimilarImages();
      if (visuallySimilarImages != null) {
        message += "\n§ Visually Similar Images\n";
        for (WebImage visuallySimilarImage: visuallySimilarImages) {
          message += String.format(Locale.US, "> %s \n", visuallySimilarImage.getUrl());
        }
      }

      List < WebPage > pagesWithMatchingImages = webDetection.getPagesWithMatchingImages();
      if (pagesWithMatchingImages != null) {
        message += "\n§ Pages With Matching Images\n";
        for (WebPage pageWithMatchingImage: pagesWithMatchingImages) {
          message += String.format(Locale.US, "> %s \n", pageWithMatchingImage.getUrl());
        }
      }
    }

    message += "\n___\n# TEXT\n";
    annotations = annotateImageResponse.getTextAnnotations();
    if (annotations != null) {
      message += String.format(Locale.US, "> Locale: %s.\n%s\n", annotations.get(0).getLocale(), annotations.get(0).getDescription());
    } else {
      message += "nothing\n";
    }

    message += "\n___\n# LANDMARKS\n";
    annotations = annotateImageResponse.getLandmarkAnnotations();
    if (annotations != null) {
      for (EntityAnnotation annotation: annotations) {
        message += String.format(Locale.US, "> %.3f: %s (%s) \n", annotation.getScore(), annotation.getDescription(), annotation.getLocations());
      }
    } else {
      message += "nothing\n";
    }

    message += "\n___\n# LOGOS\n";
    annotations = annotateImageResponse.getLogoAnnotations();
    if (annotations != null) {
      for (EntityAnnotation annotation: annotations) {
        message += String.format(Locale.US, "%.3f: %s \n", annotation.getScore(), annotation.getDescription());
      }
    } else {
      message += "nothing\n";
    }

    message += "\n___\n# FACES\n";
    List < FaceAnnotation > faceAnnotations = annotateImageResponse.getFaceAnnotations();
    if (faceAnnotations != null) {
      for (FaceAnnotation annotation: faceAnnotations) {
        message += String.format(Locale.US, "> position:%s anger:%s joy:%s surprise:%s headwear:%s \n", annotation.getBoundingPoly(), annotation.getAngerLikelihood(), annotation.getJoyLikelihood(), annotation.getSurpriseLikelihood(), annotation.getHeadwearLikelihood());
      }
    } else {
      message += "nothing\n";
    }

    message += "\n___\n# IMAGE PROPERTIES:\n\n";
    ImageProperties imagePropertiesAnnotation = annotateImageResponse.getImagePropertiesAnnotation();
    if (imagePropertiesAnnotation != null) {
      message += String.format(Locale.US, "> %s \n", imagePropertiesAnnotation.getDominantColors());
    } else {
      message += "nothing";
    }
    Log.d(TAG, "Result :-" + message);
    return message;
  }
}
