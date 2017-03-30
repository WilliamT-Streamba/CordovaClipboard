package com.verso.cordova.clipboard;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ClipDescription;

public class Clipboard extends CordovaPlugin {

    private static final String actionCopy = "copy";
    private static final String actionPaste = "paste";
    private static final String copyListener = "listenForCopy";

    /**
     * Constructor.
     */
    public Clipboard() {}

    /**
    * Sets the context of the Command. This can then be used to do things like
    * get file paths associated with the Activity.
    *
    * @param cordova The context of the main Activity.
    * @param webView The CordovaWebView Cordova is running in.
    */

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
      super.initialize(cordova, webView);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        ClipboardManager clipboard = (ClipboardManager) cordova.getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (action.equals(actionCopy)) {
            try {
                String text = args.getString(0);
                ClipData clip = ClipData.newPlainText("Text", text);

                clipboard.setPrimaryClip(clip);

                callbackContext.success(text);

                return true;
            } catch (JSONException e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
            } catch (Exception e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
            }
        } else if (action.equals(actionPaste)) {
            if (!clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.NO_RESULT));
            }

            try {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String text = item.getText().toString();

                if (text == null) text = "";

                callbackContext.success(text);

                return true;
            } catch (Exception e) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.toString()));
            }
        } else if (action.equals(copyListener)) {
                  cordova.getActivity().runOnUiThread(new Runnable() {
          public void run() {
            webView.loadUrl("javascript:console.log('clipboard-called-copy-listener');");
          }
        });
          ClipboardManager.OnPrimaryClipChangedListener primaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
              cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                  webView.loadUrl("javascript:cordova.fireDocumentEvent('itemCopiedToClipboard')");
                  webView.loadUrl("javascript:console.log('getting there');");
                }
              });
            }
          };
          clipboard.addPrimaryClipChangedListener(primaryClipChangedListener);
          return true;
        }

        return false;
    }
}


