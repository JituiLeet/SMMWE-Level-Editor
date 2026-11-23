package com.jituileet.smmweeditor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int REQUEST_SAVE = 4101;
    private static final int REQUEST_OPEN = 4102;
    private WebView web;
    private String pendingName, pendingMime, pendingBase64;
    private ValueCallback<Uri[]> fileCallback;

    private class Bridge {
        @JavascriptInterface public void saveFile(String name, String base64, String mime) {
            runOnUiThread(() -> {
                pendingName = name; pendingBase64 = base64; pendingMime = mime;
                Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(mime == null ? "application/octet-stream" : mime);
                i.putExtra(Intent.EXTRA_TITLE, name);
                startActivityForResult(i, REQUEST_SAVE);
            });
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(i, REQUEST_OPEN);
                return true;
            }
        });
        setContentView(web);
        web.loadUrl("file:///android_asset/editor/web/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SAVE) {
            if (resultCode == RESULT_OK && data != null && pendingBase64 != null) {
                try {
                    byte[] bytes = android.util.Base64.decode(pendingBase64, android.util.Base64.DEFAULT);
                    OutputStream out = getContentResolver().openOutputStream(data.getData());
                    out.write(bytes); out.close();
                    web.evaluateJavascript("document.getElementById('status').textContent='文件已保存'", null);
                } catch (Exception ex) {
                    web.evaluateJavascript("alert('保存失败：' + " + org.json.JSONObject.quote(ex.getMessage()) + ")", null);
                }
            }
            pendingBase64 = null;
        } else if (requestCode == REQUEST_OPEN && fileCallback != null) {
            Uri[] result = resultCode == RESULT_OK && data != null ? new Uri[]{data.getData()} : null;
            fileCallback.onReceiveValue(result); fileCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
