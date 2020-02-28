package com.app.example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.CookieManager;


/* printer libraries */
import android.printer.sdk.util.PowerUtils;
import android.printer.sdk.PosFactory;
import cn.pda.serialport.SerialDriver;
import android.printer.sdk.interfaces.IPosApi;
import android.printer.sdk.interfaces.OnPrintEventListener;
import android.printer.sdk.constant.BarCode;
import android.printer.sdk.bean.BarCodeBean;
import android.printer.sdk.bean.enums.ALIGN_MODE;

public class MainActivity extends AppCompatActivity {

    //printer stuff
    private IPosApi mPosApi;
    private int mConcentration=25;


    //Webview stuff
    private WebView webView;
    ProgressDialog mProgressDialog;
    ProgressDialog progressDialog;

    private String HTTPS_URL ="https://erp-dev.dullesglass.com/web#action=776";
    //private String HTTP_URL ="http://example.com";

    private boolean isSSLErrorDialogShown = false;
//
//    example.com

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Init Printer
        initPos ();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading... ");
        mProgressDialog.show();
        webView = (WebView) findViewById(R.id.webview);


        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setAppCacheEnabled(true);
//        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setSupportZoom(false);

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);

        webView.loadUrl(HTTPS_URL);

        webView.setWebViewClient(new HelloWebViewClient());
    }

    public void initPos() {
        PowerUtils.powerOnOrOff (1, "1");
        PosFactory.registerCommunicateDriver (this, new SerialDriver ()); // 注册串口类 Register serial driver
        mPosApi=PosFactory.getPosDevice (); // 获取打印机实例 get printer driver
        mPosApi.setPrintEventListener (onPrintEventListener);
        mPosApi.openDev ("/dev/ttyS2", 115200, 0);
        mPosApi.initPos (); // 初始化打印机 init printer
    }


    public OnPrintEventListener onPrintEventListener=new OnPrintEventListener () {

        String message = "Printer Status";
        @Override
        public void onPrintState(int state) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            AlertDialog alertDialog = builder.create();
            switch (state) {
                case BarCode.ERR_POS_PRINT_SUCC:
                    message = "Print success";
                    break;
                case BarCode.ERR_POS_PRINT_FAILED:
                    message = "Error. Print failed.";
                    break;
                case BarCode.ERR_POS_PRINT_HIGH_TEMPERATURE:
                    message = "Error. High temperature.";
                    break;
                case BarCode.ERR_POS_PRINT_NO_PAPER:
                    message = "Error. No paper.";
                    break;
                case 4:

                    break;
            }
            alertDialog.setTitle("Printer Status");
            alertDialog.setMessage(message);
//            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // Ignore SSL certificate errors
//                    isSSLErrorDialogShown = true;
//                    handler.proceed();
//                }
//            });
//
//            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                    handler.cancel();
//                }
//            });
            alertDialog.show();
        }
    };

    public void print_barcode() {
        int height=80;
        BarCodeBean barCodeBean=new BarCodeBean ();
        barCodeBean.setConcentration (mConcentration);
        barCodeBean.setHeight (height);
        barCodeBean.setWidth (2);// 条码宽度1-4; Width value 1 2 3 4
        barCodeBean.setText ("987654321012");
        //barCodeBean.setText ("2S_201910140122126");
        // barCodeBean.setText ("12345%$()ABcdq");
        barCodeBean.setBarType (BarCode.CODE128);
        mPosApi.addBarCode (barCodeBean, ALIGN_MODE.ALIGN_CENTER);
        mPosApi.addFeedPaper (true, 2);
    }

    @Override
    protected void onResume() {
        super.onResume ();
        mPosApi.resume ();
        PowerUtils.powerOnOrOff (1, "1");
    }

    @Override
    protected void onPause() {
        super.onPause ();
        mPosApi.stop ();
        PowerUtils.powerOnOrOff (1, "0");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (mPosApi != null) {
            mPosApi.closePos ();
            mPosApi.closeDev ();
            PosFactory.Destroy ();
        }
        PowerUtils.powerOnOrOff (1, "0");
    }


    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!url.contains("erp-dev.dullesglass")){
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }
            view.setVisibility(View.GONE);
            mProgressDialog.show();
            mProgressDialog.setMessage("Loading...");
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressDialog.dismiss();
            view.setVisibility(View.VISIBLE);
            super.onPageFinished(view, url);
            print_barcode();
            mPosApi.printStart ();
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            if (!isSSLErrorDialogShown) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    AlertDialog alertDialog = builder.create();
                String message = "SSL Certificate error.";
                switch (error.getPrimaryError()) {
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted.";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired.";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "The certificate Hostname mismatch.";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid.";
                        break;
                }

                message += " Do you want to continue anyway?";
                alertDialog.setTitle("SSL Certificate Error");
                alertDialog.setMessage(message);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ignore SSL certificate errors
                        isSSLErrorDialogShown = true;
                        handler.proceed();
                    }
                });

                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        handler.cancel();
                    }
                });
                alertDialog.show();
            }else{
                handler.proceed();
            }
        }
    }


//    private void startWebView(String url) {
//
//        WebSettings settings = webView.getSettings();
//
//        settings.setJavaScriptEnabled(true);
//        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//
//        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setUseWideViewPort(true);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setAppCacheEnabled(true);
//        webView.getSettings().setUseWideViewPort(false);
//        webView.getSettings().setSupportZoom(false);
//
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Loading...");
//        progressDialog.show();
//
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//            }
//
//            @Override
//            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//                Toast.makeText(MainActivity.this, "Error:" + description, Toast.LENGTH_SHORT).show();
//
//            }
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
////                super.onReceivedSslError(view, handler, error);
//                handler.proceed();
//            }
//        });
//        webView.loadUrl(url);
//    }
}
