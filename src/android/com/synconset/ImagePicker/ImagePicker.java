/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";

	private CallbackContext callbackContext;
	private JSONObject params;

	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
      if(Build.VERSION.SDK_INT >= 23){
        boolean checkCameraPersion = PermissionHelper.hasPermission(this, Manifest.permission.CAMERA);
        boolean checkExterbakStorage = PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        String perssions[] = new String[]{};
        if (!checkCameraPersion && !checkExterbakStorage) {
          perssions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else if (!checkCameraPersion) {
          perssions = new String[]{Manifest.permission.CAMERA};
        } else if (!checkExterbakStorage) {
          perssions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        if (!checkCameraPersion||!checkExterbakStorage) {
          PermissionHelper.requestPermissions(this,2,perssions);
        } else {
          getPictures();
        }
      }else{
        getPictures();
      }
		}
		return true;
	}

  private void getPictures() throws JSONException {
    Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
    int max = 20;
    int desiredWidth = 0;
    int desiredHeight = 0;
    int quality = 100;
    if (this.params.has("maximumImagesCount")) {
      max = this.params.getInt("maximumImagesCount");
    }
    if (this.params.has("width")) {
      desiredWidth = this.params.getInt("width");
    }
    if (this.params.has("height")) {
      desiredHeight = this.params.getInt("height");
    }
    if (this.params.has("quality")) {
      quality = this.params.getInt("quality");
    }
    intent.putExtra("MAX_IMAGES", max);
    intent.putExtra("WIDTH", desiredWidth);
    intent.putExtra("HEIGHT", desiredHeight);
    intent.putExtra("QUALITY", quality);
    if (this.cordova != null) {
      this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    switch (requestCode) {
      case 2:
        if (isAllPerssionsGranted(grantResults)) {
          getPictures();
        } else {
          Toast.makeText(cordova.getActivity(),"打开相册失败，请在（设置->应用管理）为本应用开启相机及读取文件权限！",Toast.LENGTH_LONG).show();
          return;
        }
        break;
      default:
        super.onRequestPermissionResult(requestCode,permissions,grantResults);
    }
  }

  private boolean isAllPerssionsGranted(int[] grantResults) {
    for (int i = 0; i < grantResults.length; i++) {
      if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			JSONArray res = new JSONArray(fileNames);
			this.callbackContext.success(res);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}
}
