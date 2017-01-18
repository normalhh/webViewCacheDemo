package normal.com.cachedemo;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.utils.EmptyUtils;
import com.blankj.utilcode.utils.StringUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by Android Studio.
 * User: Anonymous
 * Date: 2017/1/12
 * Time: 下午2:24
 * Desc: CustomWebViewClient is using for...
 */

class CustomWebViewClient extends WebViewClient {

    private ResourceUrlCache resourceUrlCache = new ResourceUrlCache();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return super.shouldOverrideUrlLoading(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        super.onPageCommitVisible(view, url);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        return super.shouldInterceptRequest(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        // 重写此方法获取网页中需要加载的各项资源文件
        // 获取本地的URL主域名
        String curDomain = request.getUrl().getHost();
        //取不到domain就直接返回，把接下俩的动作交给webview自己处理
        if (curDomain == null) {
            return null;
        }
        //读取当前webview正准备加载URL资源
        String url = request.getUrl().toString();
        try {
            //根据资源url获取一个你要缓存到本地的文件名，一般是URL的MD5
            String resFileName = getResourcesFileName(url);
            if (EmptyUtils.isEmpty(resFileName) || "favicon.ico".equals(resFileName) || resFileName.contains("iplookup") || resFileName
                    .contains("userlogin.do")) {
                return null;
            }
            //这里是处理本地缓存的URL，缓存到本地，或者已经缓存过的就直接返回而不去网络进行加载
            this.resourceUrlCache.register(url, url.replace(BaseApplication.properties.getProperty("SERVERURL"), ""), request
                    .getRequestHeaders().get("Accept"), "UTF-8", ResourceUrlCache.ONE_HOUR);
            return this.resourceUrlCache.load(url);
        } catch (Exception e) {
            Logger.e(e.getMessage());
        }
        return null;
    }

    public String getResourcesFileName(String url) {
        String name = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (!StringUtils.isEmpty(name)) {
            return name;
        } else {
            return "";
        }
    }

    private boolean isReadFromCache(String url) {
        return true;
    }

    @Override
    public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
        super.onTooManyRedirects(view, cancelMsg, continueMsg);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        super.onFormResubmission(view, dontResend, resend);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        super.onReceivedClientCertRequest(view, request);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return super.shouldOverrideKeyEvent(view, event);
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        super.onUnhandledKeyEvent(view, event);
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        super.onReceivedLoginRequest(view, realm, account, args);
    }
}
