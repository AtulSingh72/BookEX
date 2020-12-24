package com.beatmockerz.bookex;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {

    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    WebView myWebView;
    ProgressBar progressBar;
    SwipeRefreshLayout finalMySwipeRefreshLayout1;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(MainActivity.this.getApplicationContext(), "Failed to Upload!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        myWebView = (WebView) findViewById(R.id.bookex);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        myWebView.setWebChromeClient( new MyWebChromeClient());
        myWebView.setWebViewClient( new webClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUserAgentString(USER_AGENT);

        myWebView.getSettings().setJavaScriptEnabled(true);
        final Activity activity = this;
        WebSettings mWebSettings = myWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportZoom(false);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAllowContentAccess(true);
        myWebView.loadUrl("http://book-exchnge.herokuapp.com");
        myWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                request.setMimeType(mimetype);
                //------------------------COOKIE!!------------------------
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                //------------------------COOKIE!!------------------------
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });
        finalMySwipeRefreshLayout1 = findViewById(R.id.swiperefresh);
        finalMySwipeRefreshLayout1.setColorSchemeResources(
                R.color.colorPrimaryDark);
        finalMySwipeRefreshLayout1.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // This method performs the actual data-refresh operation.
                // The method calls setRefreshing(false) when it's finished.
                myWebView.loadUrl(myWebView.getUrl());
            }
        });
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                myWebView.scrollTo(0,0);
                ObjectAnimator anim = ObjectAnimator.ofInt(myWebView, "scrollY",
                        myWebView.getScrollY(), 0);
                anim.setDuration(400);
                anim.start();
            }
        });
        final float[] current_visibility = {0f};
        myWebView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Log.i("Scrolled", "+++ scrollchanged "+myWebView.getScrollY());
                if(myWebView.getScrollY() >= 248 && current_visibility[0] == 0f) {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(fab, "alpha",
                            current_visibility[0], 1f);
                    fab.setVisibility(View.VISIBLE);
                    anim.setDuration(200);
                    anim.start();
                    current_visibility[0] = 1f;
                    fab.setVisibility(View.VISIBLE);
                }
                else if(myWebView.getScrollY() < 248 && current_visibility[0] == 1f) {
                    ObjectAnimator anim = ObjectAnimator.ofFloat(fab, "alpha",
                            current_visibility[0], 0f);

                    anim.setDuration(200);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            fab.setVisibility(View.GONE);
                        }
                    });
                    anim.start();
                    current_visibility[0] = 0f;
                }
            }
        });
        Log.i("Height: ", String.valueOf(myWebView.getHeight()));
        Log.i("Width: ", String.valueOf(myWebView.getWidth()));
    }

    class MyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                intent = fileChooserParams.createIntent();
            }
            try
            {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e)
            {
                uploadMessage = null;
                Toast.makeText(MainActivity.this.getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
    }

    class webClient extends WebViewClient {
        public boolean  shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            finalMySwipeRefreshLayout1.setRefreshing(false);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
