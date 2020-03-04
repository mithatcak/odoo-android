package com.app.example;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.printer.sdk.PosFactory;
import android.printer.sdk.bean.BarCodeBean;
import android.printer.sdk.bean.TextData;
import android.printer.sdk.bean.enums.ALIGN_MODE;
import android.printer.sdk.constant.BarCode;
import android.printer.sdk.interfaces.IPosApi;
import android.printer.sdk.interfaces.OnPrintEventListener;
import android.printer.sdk.util.PowerUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.app.example.utils.FingerLib;

import cn.pda.serialport.SerialDriver;

/* printer libraries */

public class MainActivity extends AppCompatActivity {

    //printer stuff
    private IPosApi mPosApi;
    private int mConcentration=25;

    //finger print stuff
    private static FingerLib m_szHost;

    private String DEVICEFINGERPRINT = "U9000";
    private String DEVICEPRINTER = "S60";


    //Webview stuff
    private WebView webView;
    ProgressDialog mProgressDialog;

    private String HTTPS_URL = BuildConfig.SERVER_URL;
    private String BASE_HTTPS_URL = BuildConfig.SERVER_BASE_URL;

    private String cookie;

    private boolean isSSLErrorDialogShown = false;

    static String DEVICENAME;

    public boolean IsPrinter = false;
    public boolean IsFingerPrint = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Check Device Compatible with Printer
        DEVICENAME = getDeviceName();
        CheckIsPrinter(DEVICENAME);




        //Init Printer
        if(IsPrinter)
            initPos ();
        else if(IsFingerPrint)
//            m_szHost.SZOEMHost_Lib_Init(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading... ");
        mProgressDialog.show();

        Log.d(DEVICENAME,"&&&& THIS IS ON CREATE");
        webView = findViewById(R.id.webview);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setAppCachePath("/data/data" + getPackageName() + "/cache");
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setSupportZoom(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        CookieManager.setAcceptFileSchemeCookies(true);
        webView.loadUrl(HTTPS_URL);

        webView.setWebViewClient(new HelloWebViewClient());

    }

    private String getDeviceName(){
        return Build.DEVICE;
    }



    //HANDLE DEEP LINK
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(DEVICENAME,"NEW INTENT #####");

        Uri data = intent.getData();
        if(data!=null){
            Log.d(data.toString(),"?>?>> URL NAME !!!!!!!!!");

//            String redirect = b.getString("EXTRA_SESSION_URL");
                Log.d(data.toString(), "### REDIRECTING TO...");
//            webView.loadUrl( "javascript:window.location.reload( true )" );
//            webView.loadUrl(redirect.toString());

//            CookieManager.getInstance().setCookie(redirect,cookie);
//                webView.loadUrl("javascript:document.open();document.close();");
                webView.loadUrl(data.toString());


        }



    }


    @Override
    protected void onResume() {
        super.onResume ();
        if(IsPrinter){
            super.onResume ();
            mPosApi.resume ();
            PowerUtils.powerOnOrOff (1, "1");
        }

//        String sessionId = getIntent().getStringExtra("EXTRA_SESSION_ID");

        Log.d(DEVICENAME,"#$$$ RESUMING APPLICATION");


    }

    @Override
    protected void onPause() {
        super.onPause ();
        if(IsPrinter){
            mPosApi.stop ();
            PowerUtils.powerOnOrOff (1, "0");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if(IsPrinter){
            if (mPosApi != null) {
                mPosApi.closePos ();
                mPosApi.closeDev ();
                PosFactory.Destroy ();
            }
            PowerUtils.powerOnOrOff (1, "0");
        }
    }


    ///WEBVIEW STUFF

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!url.contains(BASE_HTTPS_URL)){
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

            if(IsPrinter){
                Log.d(DEVICENAME,"PRINTER INITIATED!!!!");
//                print_barcode();
                print_text();
//                mPosApi.printFeatureList();
                mPosApi.printStart ();
            }
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


    ///PRINTER STUFF

    public void initPos() {
        PowerUtils.powerOnOrOff (1, "1");
        PosFactory.registerCommunicateDriver (this, new SerialDriver ()); // 注册串口类 Register serial driver
        mPosApi=PosFactory.getPosDevice (); // 获取打印机实例 get printer driver
        mPosApi.setPrintEventListener (onPrintEventListener);
        mPosApi.openDev ("/dev/ttyS2", 115200, 0);
        mPosApi.setPos ().setAutoEnableMark (false)
                .setEncode (2)
                .setLanguage (-1)
                .setPrintSpeed (-1)
                .setMarkDistance (-1).init ();// 初始化打印机 init printer
    }


    private void CheckIsPrinter(String DeviceName){
        if(DeviceName.equals(DEVICEPRINTER))
            IsPrinter = true;
        else if (DeviceName.equals(DEVICEFINGERPRINT))
            IsFingerPrint = true;
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
        barCodeBean.setText ("1");
        //barCodeBean.setText ("2S_201910140122126");
        // barCodeBean.setText ("12345%$()ABcdq");
        barCodeBean.setBarType (BarCode.CODE128);
        mPosApi.addBarCode (barCodeBean, ALIGN_MODE.ALIGN_CENTER);
        mPosApi.addFeedPaper (true, 3);
    }

    public void print_text(){
        TextData textData1=new TextData ();
        textData1.addConcentration (mConcentration);
        textData1.addFont (BarCode.FONT_ASCII_12x24);
        textData1.addTextAlign (BarCode.ALIGN_CENTER);
        textData1.addFontSize (BarCode.NORMAL);
        textData1.addText ("what's up");
        textData1.addText ("\n");
        textData1.addText ("\n");
        textData1.addText ("\n");
        textData1.addText ("\n");
        textData1.addText ("\n");
        mPosApi.addText (textData1);
//        mPosApi.printStart ();
    }



    ///FINGER PRINT READER STUFF

//    public void OnOpenDeviceBtn() {
//        if (m_szHost.OpenDevice(m_szDevice, m_nBaudrate) == 0) {
//            EnableCtrl(true);
//            m_btnOpenDevice.setEnabled(false);
//            m_btnCloseDevice.setEnabled(true);
//        }
//    }


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
