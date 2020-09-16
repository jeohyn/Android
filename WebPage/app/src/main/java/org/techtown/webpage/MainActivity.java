package org.techtown.webpage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText webSite;
    WebView webView;

    Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webSite=findViewById(R.id.webSite);
        webView=findViewById(R.id.webView);

        //make setting
        WebSettings webSettings=webView.getSettings();
        //Tells the WebView to enable JavaScript execution.
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new ViewClient());

        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ////trim() : erase white space
                final String urlStr=webSite.getText().toString().trim();
                RequestThread thread=new RequestThread(urlStr);
                thread.start();
                webView.loadUrl(webSite.getText().toString());
            }
        });

    }

    class RequestThread extends Thread{
        String urlStr;

        public RequestThread(String str){
            urlStr=str;
        }

        public void run(){
            try{
                final String output=request(urlStr);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //a url connection with support for HTTP-specific features
        private String request(String urlStr){
            StringBuilder output=new StringBuilder();
            try{
                URL url=new URL(urlStr);
                HttpURLConnection conn=(HttpURLConnection)url.openConnection();
                if(conn!=null){//set a timeout value(millisec) to be used when opening a communications link to the resource
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    //doInput : specifies whether this URLConnection allows receiving data
                    conn.setDoInput(true);
                    //doOutput : specifies whether this URLConnection allows sending data
                    conn.setDoOutput(true);

                    boolean redirect=false;

                    int resCode=conn.getResponseCode();

                    //HTTP_OK : 200. 정상
                    //HTTP_MOVED_TEMP : 302. Temporary Redirect
                    //HTTP_MOVED_PERM : 301. Moved Permanently
                    //HTTP_SEE_OTHER : 303. See Other
                    //http://freeminderhuni.blogspot.com/2019/07/bit.html
                    if(resCode!=HttpURLConnection.HTTP_OK){
                        //if temporary redirect or moved permanently or see other(==there's no page that request)
                        if(resCode==HttpURLConnection.HTTP_MOVED_TEMP||resCode==HttpURLConnection.HTTP_MOVED_PERM||resCode==HttpURLConnection.HTTP_SEE_OTHER)
                            redirect=true;
                    }

                    if(redirect){
                        //location : used to redirect the recipient to a location other than the Request-URI for completion of the request of identification of a new resource
                        String newUrl=conn.getHeaderField("Location");
                        //Set-Cookie : used to send a cookie from the server to user agent, so the user agent can send it back to the server later
                        String cookies=conn.getHeaderField("Set-Cookie");
                        conn=(HttpURLConnection)new URL(newUrl).openConnection();
                        conn.setRequestProperty("Cookie", cookies);
                        System.out.println("Redirect to URL : "+newUrl);
                    }
                    BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line=null;
                    while(true){
                        line=reader.readLine();
                        if(line==null)
                            break;
                        output.append(line+"\n");
                    }
                    reader.close();
                    conn.disconnect();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return output.toString();
        }
    }
    private class ViewClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url){
            view.loadUrl(url);
            return true;
        }
    }
}

/*
StringBuilder
A mutable sequence of characters. This class provides an API compatible with StringBuffer, but with no guarantee of synchronization.
This class is designed for use as a drop-in replacement for StringBuffer in places where the string buffer was being used by a single thread (as is generally the case).
Where possible, it is recommended that this class be used in preference to StringBuffer as it will be faster under most implementations.

URL
The recommended way to manage the encoding and decoding of URLs is to use URI, and to convert between these two classes using toURI() and URI#toURL().

Redirection
technique for moving visitors to a different web page than the one they request,
usually because the page requested is unavailable.
 */