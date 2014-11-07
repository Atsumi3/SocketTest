package info.nukoneko.sockettest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import attendance4j.Attendance;
import attendance4j.JsonValidator;
import attendance4j.conf.Async;
import attendance4j.conf.AsyncCallback;
import attendance4j.conf.ConnectRun;
import attendance4j.conf.ConnectUtil.method;
import attendance4j.conf.ConnectUtil.Protocol;

/**
 * Created by TEJNEK on 2014/11/05.
 */
public class AuthenticationActivity extends Activity {

    String serverIP = "192.168.0.6";
    Attendance attendance;
    FirstResult firstResult;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenication);
        if(attendance == null) attendance = new Attendance();
        attendance.getNotice();
        ((EditText)findViewById(R.id.ip)).setText(serverIP);
        findViewById(R.id.b_auth_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAuth();
            }
        });
    }
    public class FirstResult{
        private String _hash;
        private Long timeStamp;
        private Long lecture;
        private String hash;

        public FirstResult(String result){
            try {
                ObjectMapper object = new ObjectMapper();
                JsonNode root = object.readValue(result, JsonNode.class);
                this._hash = root.get("hash").asText("");
                this.timeStamp = root.get("timestamp").asLong(0);
                this.lecture = root.get("lecture").asLong(0);
                this.hash = generateHash().replace('+', '-').replace('/', '_');
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private String generateHash() {
            try {
                String key = String.valueOf(Attendance.nonce + this.timeStamp + this.lecture);
                SecretKey sk = new SecretKeySpec(key.getBytes(), "HmacSHA1");
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(sk);
                byte[] result = mac.doFinal(this._hash.getBytes());
                return org.java_websocket.util.Base64.encodeBytes(result);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    public String createBaseUri(String endPoint){
        String host = ((EditText)findViewById(R.id.ip)).getText().toString();
        String port = ((EditText)findViewById(R.id.port)).getText().toString();
        if(port.equals(""))port = "80";
        String base = "";
        if(host.indexOf("/") > 0){
            String[] _p = host.split("/");
            String _ = "";
            for (int i = 1; i < _p.length; i++){
                _ += _p[i] + "/";
            }
            base = _p[0] + ":" + port + "/" + _;
        }else{
            base = host + ":" + port;
        }
        return base + "/" + endPoint;
    }

    public void doAuth(){
        new Async<String>(new AsyncCallback<String>() {
            @Override
            public String doFunc(Object... params) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("nonce", Attendance.nonce);
                return ConnectRun.send(method.POST, Protocol.HTTP, createBaseUri("auth"), map);
            }

            @Override
            public void onResult(String result) {
                if (result == null) return;
                firstResult = new FirstResult(result);
                new Async<String>(new AsyncCallback<String>() {
                    @Override
                    public String doFunc(Object... params) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("hash", firstResult.hash);
                        return ConnectRun.send(method.POST, Protocol.HTTP, createBaseUri("auth"), map);
                    }
                    @Override
                    public void onResult(String result) {
                        ((TextView)findViewById(R.id.resp_data)).setText(String.valueOf(
                                parseJson(result)?"認証成功":"認証失敗\n" + new JsonValidator().setText(result).validate()
                        ));
                    }
                }).run();
            }
        }).run();
    }

    public boolean parseJson(String json){
        try {
            ObjectMapper object = new ObjectMapper();
            JsonNode root = object.readValue(json, JsonNode.class);
            return root.get("auth").asBoolean(false);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
