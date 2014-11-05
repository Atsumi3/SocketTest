package info.nukoneko.sockettest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

/**
 * Created by TEJNEK on 2014/11/05.
 */
public class SocketTestActivity extends Activity {
    Handler handler;
    SocketIO mSocketIO;
    Button mConnectButton;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_test);
        ((TextView)findViewById(R.id.myIp)).setText("Local IP Address : \\t" + getIPAddress());
        mConnectButton = (Button) findViewById(R.id.connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText host = (EditText) findViewById(R.id.host);
                EditText post = (EditText) findViewById(R.id.port);
                if (host.length() > 0 && post.length() > 0) {
                    //findViewById(R.id.connectArea).setVisibility(View.GONE);
                    connect(host.getText().toString(), post.getText().toString());
                }
            }
        });
        findViewById(R.id.sendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText event = (EditText) findViewById(R.id.event);
                EditText message = (EditText) findViewById(R.id.message);
                EditText data = (EditText) findViewById(R.id.data);
                if(event.length() > 0 && message.length() > 0 && data.length() > 0){
                    sendMessage(event.getText().toString(), message.getText().toString(), data.getText().toString());
                }
            }
        });
    }

    private IOCallback ioCallback = new IOCallback() {
        @Override
        public void onDisconnect() {
            printn("onDisconnect");

            mConnectButton.setEnabled(true);
        }

        @Override
        public void onConnect() {
            printn("onConnect");
        }

        @Override
        public void onMessage(String s, IOAcknowledge ioAcknowledge) {
            printn("onMessage");
            printn(s);
        }

        @Override
        public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
            printn("onMessage");
            printn(jsonObject.toString());
            try {
                printn("Server said:" + jsonObject.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void on(String s, IOAcknowledge ioAcknowledge, Object... objects) {
            printn("onEvent");
            printn("EVENT: " + s);
        }

        @Override
        public void onError(SocketIOException e) {
            e.printStackTrace();
        }
    };
    private void connect(String host, String port) {
        try {
            mSocketIO = new SocketIO("http://" + host +":"+port);
            mSocketIO.connect(ioCallback);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String event, String message, String data){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(message, data);
            mSocketIO.emit(event, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void printn(String text){
        TextView textView = (TextView)findViewById(R.id.respData);
        textView.setText( text + "\n" + textView.getText().toString());
    }
    private static String getIPAddress(){
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    String address = addresses.nextElement().getHostAddress();

                    //127.0.0.1と0.0.0.0以外のアドレスが見つかったらそれを返す
                    if (!"127.0.0.1".equals(address) && !"0.0.0.0".equals(address)) {
                        return address;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return "127.0.0.1";
    }
}
