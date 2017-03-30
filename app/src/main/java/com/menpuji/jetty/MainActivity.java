package com.menpuji.jetty;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                File keystoreFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "keystore.bks");
                try {
                    InputStream in = getAssets().open("menpuji.bks");
                    inputStream2File(in, keystoreFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Server server = new Server();

                // HTTP Configuration
                // HttpConfiguration is a collection of configuration information
                // appropriate for http and https. The default scheme for http is
                // <code>http</code> of course, as the default for secured http is
                // <code>https</code> but we show setting the scheme to show it can be
                // done. The port for secured communication is also set here.
                HttpConfiguration http_config = new HttpConfiguration();
                http_config.setSecureScheme("https");
                http_config.setSecurePort(8443);
                http_config.setOutputBufferSize(32768);

                // HTTP connector
                // The first server connector we create is the one for http, passing in
                // the http configuration we configured above so it can get things like
                // the output buffer size, etc. We also set the port (8080) and
                // configure an idle timeout.
                ServerConnector http = new ServerConnector(server,
                        new HttpConnectionFactory(http_config));
                http.setPort(8080);
                http.setIdleTimeout(30000);

                // SSL Context Factory for HTTPS
                // SSL requires a certificate so we configure a factory for ssl contents
                // with information pointing to what keystore the ssl connection needs
                // to know about. Much more configuration is available the ssl context,
                // including things like choosing the particular certificate out of a
                // keystore to be used.
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStoreType("BKS");
                sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
                sslContextFactory.setKeyStorePassword("qazwsx1234");
                sslContextFactory.setKeyManagerPassword("qazwsx1234");
                sslContextFactory.setTrustStorePassword("qazwsx1234");

                // HTTPS Configuration
                // A new HttpConfiguration object is needed for the next connector and
                // you can pass the old one as an argument to effectively clone the
                // contents. On this HttpConfiguration object we add a
                // SecureRequestCustomizer which is how a new connector is able to
                // resolve the https connection before handing control over to the Jetty
                // Server.
                HttpConfiguration https_config = new HttpConfiguration(http_config);
                SecureRequestCustomizer src = new SecureRequestCustomizer();
                https_config.addCustomizer(src);

                // HTTPS connector
                // We create a second ServerConnector, passing in the http configuration
                // we just made along with the previously created ssl context factory.
                // Next we set the port and a longer idle timeout.
                ServerConnector https = new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(https_config));
                https.setPort(8443);
                https.setIdleTimeout(500000);
                server.setConnectors(new Connector[]{http, https});
                server.setHandler(new AbstractHandler() {
                    @Override
                    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                        System.out.println(target);
                    }
                });
                try {
                    server.start();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void inputStream2File(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            int length;
            byte[] buffer = new byte[8192];
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
