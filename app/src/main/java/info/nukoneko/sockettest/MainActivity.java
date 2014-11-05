package info.nukoneko.sockettest;

import android.app.Activity;
import android.content.Intent;
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
 * Created by Atsumi on 2014/11/04.
 */
public class MainActivity extends Activity {

    Button socketTest;
    Button authTest;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socketTest = (Button)findViewById(R.id.b_socket_test);
        authTest = (Button) findViewById(R.id.b_auth_test);
        setContentText();
        setContentEvent();
    }
    private void setContentText(){
        socketTest.setText("ソケット通信のテスト");
        authTest.setText("Node.jsの認証サーバーのテスト");
    }
    private void setContentEvent(){
        socketTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketTestActivity.class);
                startActivity(intent);
            }
        });

        authTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });
    }
}
