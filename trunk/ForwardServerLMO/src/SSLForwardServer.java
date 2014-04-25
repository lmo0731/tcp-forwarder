
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir
 */
public class SSLForwardServer extends ForwardServer {

    public SSLForwardServer(int port, String fhost, int fport, MyLogger logger) {
        super(port, fhost, fport, logger);
    }

    @Override
    public void createSocket() {
        try {
            String ksName = "ks/lig.keystore";
            char ksPass[] = "simulator".toCharArray();
            char ctPass[] = "simulator".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(ksName), ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, ctPass);
            SSLContext sc = SSLContext.getInstance("TLSv1");
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            socket = (SSLServerSocket) ssf.createServerSocket(port);
            logger.info("SSL Forwarder server started on " + port);
            listener.onStart();
        } catch (IOException ex) {
            socket = null;
            logger.error("starting server socket", ex);
            throw new IllegalArgumentException("local port: " + ex.getMessage());
        } catch (Exception ex) {
            socket = null;
            logger.error("starting server socket", ex);
            throw new IllegalArgumentException("Internal error, " + ex.getMessage(), ex);
        }
    }

    @Override
    public String toString() {
        return "" + port + ((forwardHost != null) ? ("\t" + forwardHost + "\t" + forwardPort) : "");
    }
}
