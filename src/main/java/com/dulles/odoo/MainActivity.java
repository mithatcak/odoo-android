package com.dulles.odoo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* printer libraries */
import android.printer.sdk.PosFactory;
import android.printer.sdk.bean.TextData;
import android.printer.sdk.constant.BarCode;
import android.printer.sdk.interfaces.IPosApi;
import android.printer.sdk.interfaces.OnPrintEventListener;
import android.printer.sdk.util.PowerUtils;
import android.printer.sdk.bean.BarCodeBean;
import android.printer.sdk.bean.enums.ALIGN_MODE;

import androidx.appcompat.app.AppCompatActivity;



/*fingerprint library*/
//import com.app.example.utils.FingerLib;


public class MainActivity extends AppCompatActivity {

    //printer stuff
    private IPosApi mPosApi;
    private int mConcentration=30;

    //finger print stuff
//    private static FingerLib m_szHost;

    //DEVICE MANAGEMENT
    private final static String DEVICE_FINGER_PRINT = "U9000";
    private final static String DEVICE_PRINTER = "S60";
    static String DEVICE_NAME;
    public boolean IsPrinter = false;
    public boolean IsFingerPrint = false;

    //WebView stuff
    private WebView webView;
    ProgressDialog mProgressDialog;
    ProgressDialog eProgressDialog;
    private String HTTPS_URL = BuildConfig.SERVER_URL;
    private String BASE_HTTPS_URL = BuildConfig.SERVER_BASE_URL;
    private boolean isSSLErrorDialogShown = false;
    private final static String PRINT_LABEL = "print=yes";
    private final static String BARCODE_KEY = "MO";
    private final static String PRINT_FLAG = "print";

    private ConnectivityManager connectivityManager;


    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgressDialog.dismiss();
                    webView.reload();
                }
            });
        }
        @Override
        public void onLost(Network network) {
            super.onLost(network);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                mProgressDialog.setMessage("No Internet Connection... ");
                mProgressDialog.show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Check Device Compatible with Printer
        DEVICE_NAME = getDeviceName();
        SetDeviceType(DEVICE_NAME);

        //Init Printer
        if (IsPrinter) {
            Log.d(DEVICE_NAME, "PRINTER INITIATED!!!!");
            initPos();
        }
//        else if(IsFingerPrint)
//            m_szHost.SZOEMHost_Lib_Init(this, m_txtStatus, m_FpImageViewer, runEnableCtrl, m_spDevice);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCanceledOnTouchOutside(false);

        eProgressDialog = new ProgressDialog(this);
        eProgressDialog.setCanceledOnTouchOutside(false);

        mProgressDialog.setMessage("Loading... ");
        mProgressDialog.show();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);



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
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        CookieManager.setAcceptFileSchemeCookies(true);
        webView.loadUrl(HTTPS_URL);

        webView.setWebViewClient(new HelloWebViewClient());

    };


    ///Printer Stuff
    public void initPos() {
        PowerUtils.powerOnOrOff (1, "1");
        mPosApi=PosFactory.getPosDevice (this); // 获取打印机实例 get printer driver
        mPosApi.setPrintEventListener (onPrintEventListener);
        mPosApi.openDev ();
        mPosApi.setPos (); // 初始化打印机 init printer
        mPosApi.setEncode (2);
        mPosApi.setLanguage (15);
        mPosApi.setMarkDistance (115);
    }

    private String getDeviceName(){
        return Build.DEVICE;
    }

    //HANDLE DEEP LINK
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Uri data = intent.getData();
        if(data!=null){
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
                mPosApi.closeDev ();
                PosFactory.Destroy ();
            }
            PowerUtils.powerOnOrOff (1, "0");
        }
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }


    ///WebView Stuff
    private class HelloWebViewClient extends WebViewClient {
        boolean errorOccurred = false; // Global variable

        @Override
        public boolean shouldOverrideUrlLoading(WebView view,  WebResourceRequest request) {
            String url = request.getUrl().toString();
            if(!url.contains(BASE_HTTPS_URL)){
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }
            view.setVisibility(View.GONE);
            mProgressDialog.show();
            mProgressDialog.setMessage("Loading...");
            errorOccurred = false;
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(!errorOccurred){
                eProgressDialog.dismiss();
            }
            mProgressDialog.dismiss();
            view.setVisibility(View.VISIBLE);
//            parseAndPrintLabelInfo("https://dev.troysys.com/barcode_scanner_interface_mobile/static/www/index.html?print=yes&MO=MO/45252-1/1&O=ONL1000037968-D&C=Ali\Rezaiyan-Nojani&S=FedEx\Ground%20Home&D=2020-05-28&LC=TRY/Stock/E3-2&Prime=false&Rush=false&Reorder=false&Sku=SH8X8TR10MMFP#/batch_scan_product/41/287454");

            //Check if URL asks for label printing
            if(url.contains(PRINT_LABEL)){
                parseAndPrintLabelInfo(url);
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            errorOccurred = true;
//            view.loadUrl("about:blank");
            eProgressDialog.setMessage("No Internet Connection... ");
            eProgressDialog.show();
        }

        @Override
        public void onReceivedHttpError (WebView view,
                                         WebResourceRequest request,
                                         WebResourceResponse errorResponse){
            eProgressDialog.setMessage("Ooops Something Bad Happened... ");
            eProgressDialog.show();
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

    private void SetDeviceType(String DeviceName){
        if(DeviceName.equals(DEVICE_PRINTER))
            IsPrinter = true;
        else if (DeviceName.equals(DEVICE_FINGER_PRINT))
            IsFingerPrint = true;
    }

    //Parse params from URL then print label
    public void parseAndPrintLabelInfo(String url){
        if(!IsPrinter){
            Log.d(DEVICE_NAME,"Device doesn't have printer!");
            return;
        }
        Map<String, String> label = new HashMap<>();
        Uri uri = Uri.parse(url);

        Set<String> paramNames = uri.getQueryParameterNames();
        for (String key: paramNames) {
            String value = uri.getQueryParameter(key);
            if(key.equals(BARCODE_KEY)) {
                print_barcode(value);
            }
            else if(!key.equals(PRINT_FLAG)){
                label.put(key,value);
            }
        }
        print_text(label);
        mPosApi.addMark ();
        mPosApi.printStart ();
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
            alertDialog.show();
        }
    };

    public void print_barcode(String input) {
        int height=60;
        BarCodeBean barCodeBean=new BarCodeBean ();
        barCodeBean.setConcentration (mConcentration);
        barCodeBean.setHeight (height);
        barCodeBean.setWidth (2);// 条码宽度1-4; Width value 1 2 3 4
        barCodeBean.setText (input);
        barCodeBean.setBarType (BarCode.CODE128);
        mPosApi.addBarCode (barCodeBean, ALIGN_MODE.ALIGN_CENTER);
        mPosApi.addFeedPaper (true, 1);
    }

    public void print_text(Map<String,String> label){
        TextData textData1=new TextData ();
        textData1.addConcentration (mConcentration);
        textData1.addFont (BarCode.FONT_ASCII_12x24);
        textData1.addTextAlign (BarCode.ALIGN_CENTER);
        textData1.addFontSize (BarCode.NORMAL);
        for (Map.Entry<String, String> entry : label.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
//            if(value.equals("true")) {
//                textData1.addText(key);
//                textData1.addText("\n");
//            }
//            else if(!value.equals("false")){
//                value = value.replace("\\\\", " ");
//                textData1.addText(key + ": " + value);
//                textData1.addText("\n");
//            }
            if(!value.equals("true") && !value.equals("false")){
                value = value.replace("\\\\", " ");
                textData1.addText(key + ": " + value);
                textData1.addText("\n");
            }
        }
        mPosApi.addText (textData1);
    }

    ///FINGER PRINT READER STUFF

//    public void OnOpenDeviceBtn() {
//        if (m_szHost.OpenDevice(m_szDevice, m_nBaudrate) == 0) {
//            EnableCtrl(true);
//            m_btnOpenDevice.setEnabled(false);
//            m_btnCloseDevice.setEnabled(true);
//        }
//    }

}
