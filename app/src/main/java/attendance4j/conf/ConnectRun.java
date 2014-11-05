package attendance4j.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static attendance4j.conf.ConnectUtil.Protocol.HTTP;
import static attendance4j.conf.ConnectUtil.Protocol.HTTPS;

/**
 * Created by TEJNEK on 2014/11/05.
 */
public class ConnectRun {
    private static CookieManager manager = null;
    public static List<String> cookieString = new LinkedList<String>();

    public static String send(ConnectUtil.method _method, ConnectUtil.Protocol protocol, String host, Map<String, Object> param) {
        String method = _method.toString();
        try {
            String apiBaseURI = ((protocol == HTTP)?"http://":"https://") + host;
            //パラメータを作成する。
            String baseParam = "";
            if (param != null) {
                for (Map.Entry<String, Object> e : param.entrySet()) {
                        baseParam += e.getKey() + "=" + e.getValue() + "&";
                }
                if (baseParam.indexOf("&") > 0)
                    baseParam = baseParam.substring(0, baseParam.length() - 1);
                if (_method.equals(ConnectUtil.method.GET)) apiBaseURI += "?" + baseParam;
            }

            //接続開始
            URL url = new URL(apiBaseURI);
            //もしクッキーマネージャが作成されてなかったら作成(最初の接続)
            if (manager == null) {
                if (cookieString.size() == 2 && !cookieString.get(0).equals("") && !cookieString.get(1).equals("")) {
                    CookieStore store = new CookieManager().getCookieStore();
                    for (String cookie : cookieString) {
                        String[] cookieParams = cookie.split(",");
                        HttpCookie httpCookie = new HttpCookie(cookieParams[0], cookieParams[3]);
                        httpCookie.setDomain(cookieParams[1]);
                        httpCookie.setPath(cookieParams[2]);
                        URI uri = null;
                        uri.create(apiBaseURI);
                        store.add(uri, httpCookie);
                    }
                    manager = new CookieManager(store, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
                } else {
                    manager = new CookieManager();
                }
            }
            CookieHandler.setDefault(manager);
            //SSLのチェックをしないため、変なURLに飛ばないようにしましょう。
            disableCertificateChecking();

            System.out.println(apiBaseURI);

            if (protocol == HTTPS) {
                HttpsURLConnection con;
                con = (HttpsURLConnection) url.openConnection();
                con.setInstanceFollowRedirects(false);
                con.setUseCaches(false);

                //デフォルトのパラメータ指定はGETなので、POSTだったらPOSTに切り替える
                con.setRequestMethod(method.toString());

                con.setRequestProperty("Accept-Language", "ja");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                //POSTの際のパラメータを設定
                if (_method.equals(ConnectUtil.method.POST) && param != null) {
                    con.setDoOutput(true);
                    PrintWriter printWriter = new PrintWriter(con.getOutputStream());
                    printWriter.print(baseParam);
                    printWriter.close();
                }

                String ret = new String();
                System.out.println(method + " " + apiBaseURI + " " + con.getResponseCode());
                //200 OK 以外のステータスコードが返ってきた場合の処理
                if (con.getResponseCode() != 200) {
                    return "Error:" + con.getResponseCode();
                }

                //UTF-8 でデータを読取
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String str;
                while (null != (str = bufferReader.readLine())) {
                    ret += str;
                } //読み取ったデータを格納
                bufferReader.close(); //ストリームを閉じる

                con.disconnect(); //接続を切って
                return ret; //返す
            } else {
                HttpURLConnection con;
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(method.toString());
                con.setInstanceFollowRedirects(false);
                con.setUseCaches(false);


                con.setRequestProperty("Accept-Language", "ja");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                //POSTの際のパラメータを設定
                if (_method.equals(ConnectUtil.method.POST) && param != null) {
                    con.setDoOutput(true);
                    PrintWriter printWriter = new PrintWriter(con.getOutputStream());
                    printWriter.print(baseParam);
                    printWriter.close();
                }

                String ret = new String();
                System.out.println(method + " " + apiBaseURI + " " + con.getResponseCode());
                //200 OK 以外のステータスコードが返ってきた場合の処理
                if (con.getResponseCode() != 200) {
                    return "Error:" + con.getResponseCode();
                }
                //UTF-8 でデータを読取
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String str;
                while (null != (str = bufferReader.readLine())) {
                    ret += str;
                } //読み取ったデータを格納
                bufferReader.close(); //ストリームを閉じる
                con.disconnect(); //接続を切って
                return ret; //返す
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }
    public static String convertToUnicode(String original) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < original.length(); i++) {
            sb.append(String.format("\\u%04X", Character.codePointAt(original, i)));
        }
        String unicode = sb.toString();
        return unicode;
    }

    private static String generateBoundary() {
        String chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
        Random rand = new Random();
        String boundary = "";
        for (int i = 0; i < 40; i++) {
            int r = rand.nextInt(chars.length());
            boundary += chars.substring(r, r + 1);
        }
        return boundary;
    }

    private static void disableCertificateChecking() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            System.out.println("CertificateChecking Error : " + e.toString());
        }
    }

    //Accessor
    public static void setCookieManager(CookieManager _manager) {
        manager = _manager;
    }

    public static CookieManager getCookieManager() {
        return manager;
    }
}