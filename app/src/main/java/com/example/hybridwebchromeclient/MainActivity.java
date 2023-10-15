package com.example.hybridwebchromeclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import java.util.List;


public class MainActivity extends Activity {
    public WebView mWebView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mWebView = (WebView) findViewById(R.id.webview);
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setNetworkAvailable(true);
        mWebView.setWebChromeClient(new ChromeClient(this));
//         String url = "http://118.40.77.130";
        String url = "file:///android_asset/index.html";
        mWebView.loadUrl(url);

//      리액트 연결
        mWebView.addJavascriptInterface(new WebBridge(mContext), "BRIDGE");
        // 웹뷰 콘솔
        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }
        });

    }

    public class NotiService extends NotificationListenerService {
        public NotiService() {
            super();
            MediaSessionManager m = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            ComponentName component = new ComponentName(this, NotiService.class);
            List<MediaController> sessions = m.getActiveSessions(component);
            Log.d("Sessions", "count: " + sessions.size());
            for (MediaController controller : sessions) {
                Log.d("Sessions", controller.toString() + " -- " + TextUtils.join(", ", controller.getMetadata().keySet()));
                Log.d("Sessions", controller.toString() + " -- " + controller.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE));
            }
        }
        // 다른 클래스 멤버 및 메서드 정의
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    class WebBridge {
        private Context mContext;

        public WebBridge(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public String testAndroid() {
            MediaController mediaController = getCurrentlyPlayingMediaController(mContext);

            if (mediaController != null) {
                // 미디어 제목, 아티스트 및 패키지 이름 가져오기
                String title = mediaController.getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE);
                String artist = mediaController.getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST);
                String packageName = mediaController.getPackageName();

                return title + artist + packageName;
            }
            System.out.println("안녕하세요 test");
            return "미디어 재생중이 아님 ㅋ";
        }

        private MediaController getCurrentlyPlayingMediaController(Context context) {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            for (MediaController controller : mediaSessionManager.getActiveSessions(null)) {
                if (controller.getPlaybackState() != null && controller.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                    return controller;
                }
            }
            return null;
        }
    }

    private final class ChromeClient extends WebChromeClient {
        public Context mCtx;

        public ChromeClient(Context cxt) {
            mCtx = cxt;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(mCtx)
                    .setTitle("확인").setMessage(message)
                    .setNeutralButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false).show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(mCtx).setTitle("확인").setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }).setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    })
                    .setCancelable(false).show();
            return true;
        }
    }
}