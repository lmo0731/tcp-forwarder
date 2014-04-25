
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir
 */
public class ForwardServer extends Thread {

    final MyLogger logger;
    ServerSocket socket;
    int port;
    Integer forwardPort = null;
    String forwardHost = null;
    SocketListener listener = new DefaultSocketListener();
    final List<ForwardClient> clients = new ArrayList<ForwardClient>();
    SocketFactory factory;
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

    public ForwardServer(int port, String fhost, int fport, boolean ssl, MyLogger logger) {
        this.port = port;
        this.forwardPort = fport;
        this.forwardHost = fhost;
        this.logger = (logger != null ? logger : new DefaultMyLogger());
        if (ssl) {
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                factory = sc.getSocketFactory();
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        } else {
            factory = SocketFactory.getDefault();
        }
    }

//    void setForwardServer(String host, int port) {
//        forwardPort = port;
//        forwardHost = host;
//        if (forwardHost != null && forwardPort != null) {
//            logger.info("Forward to " + forwardHost + ":" + forwardPort);
//        }
//    }
    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    public void createSocket() {
        try {
            socket = new ServerSocket(port);
            logger.info("Forwarder server started on " + port);
            listener.onStart();
        } catch (IOException ex) {
            socket = null;
            logger.error("starting server socket", ex);
            throw new IllegalArgumentException("local port: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            createSocket();
            while (socket != null) {
                Socket localSocket = socket.accept();
                logger.info("Client connected from " + localSocket.getInetAddress());
                Socket remoteSocket = null;
                String prefix = "server_" + port;
                if (forwardHost != null && forwardPort != null) {
                    remoteSocket = factory.createSocket(forwardHost, forwardPort);
                    prefix += "_" + forwardHost + "_" + forwardPort;
                }
                final ForwardClient localClient = new ForwardClient(localSocket);
                final ForwardClient remoteClient = new ForwardClient(remoteSocket);
                String id = "" + new Date().getTime();
                File logReq = new File(prefix + "_" + id + "_req");
                File logRes = new File(prefix + "_" + id + "_res");
                File log = new File(prefix + "_" + id + "_log");
                if (!logReq.exists()) {
                    try {
                        logReq.createNewFile();
                    } catch (IOException ex) {
                    }
                }
                if (!logRes.exists()) {
                    try {
                        logRes.createNewFile();
                    } catch (IOException ex) {
                    }
                }
                if (!log.exists()) {
                    try {
                        log.createNewFile();
                    } catch (IOException ex) {
                    }
                }
                FileOutputStream reqOut = null;
                try {
                    reqOut = new FileOutputStream(logReq);
                } catch (FileNotFoundException ex) {
                }
                FileOutputStream resOut = null;
                try {
                    resOut = new FileOutputStream(logRes);
                } catch (FileNotFoundException ex) {
                }
                LogFileOutputStream logOut = null;
                try {
                    logOut = new LogFileOutputStream(log);
                } catch (FileNotFoundException ex) {
                }
                final OutputStream reqOutImpl = (reqOut != null ? reqOut : null);
                final OutputStream resOutImpl = (resOut != null ? resOut : null);
                final LogFileOutputStream logOutImpl = (logOut != null ? logOut : null);
                localClient.setListener(new SocketListener() {
                    @Override
                    public void onStart() {
                        clients.add(localClient);
                        logger.info("Client successfully started.");
                        remoteClient.start();
                    }

                    @Override
                    public void onInput(int b) {
                        listener.onInput(b);
                        if (remoteClient != null) {
                            remoteClient.send(b);
                        }
                    }

                    @Override
                    public void onWrite(int b) {
                        listener.onWrite(b);
                    }

                    @Override
                    public void onEnd() {
                        if (remoteClient != null) {
                            remoteClient.close();
                        }
                    }

                    @Override
                    public void beforeEnd() {
                        if (remoteClient != null) {
                            remoteClient.close();
                        }
                        logger.info("Client's connection ended.");
                        try {
                            clients.remove(localClient);
                        } catch (Exception ex) {
                        }
                    }

                    @Override
                    public void onError(Exception ex) {
                    }
                });
                remoteClient.setListener(new SocketListener() {
                    @Override
                    public void onStart() {
                        logger.info("Successfully bound to forward server.");
                    }

                    @Override
                    public void onInput(int b) {
                        if (localClient != null) {
                            localClient.send(b);
                        }
                        if (resOutImpl != null) {
                            try {
                                resOutImpl.write(b);
                                logOutImpl.writeServer(b);
                            } catch (Exception ex) {
                            }
                        }
                    }

                    @Override
                    public void onWrite(int b) {
                        if (reqOutImpl != null) {
                            try {
                                reqOutImpl.write(b);
                                logOutImpl.writeClient(b);
                            } catch (IOException ex) {
                            }
                        }
                    }

                    @Override
                    public void onEnd() {
                        if (localClient != null) {
                            localClient.close();
                        }
                        if (reqOutImpl != null) {
                            try {
                                reqOutImpl.flush();
                            } catch (IOException ex) {
                            }
                            try {

                                resOutImpl.flush();
                            } catch (IOException ex) {
                            }
                            try {
                                logOutImpl.flush();
                            } catch (IOException ex) {
                            }
                        }
                        if (resOutImpl != null) {
                            try {
                                reqOutImpl.close();
                            } catch (IOException ex) {
                            }
                            try {
                                resOutImpl.close();
                            } catch (IOException ex) {
                            }
                            try {
                                logOutImpl.close();
                            } catch (IOException ex) {
                            }
                        }
                        logger.info("Forward server connection ended.");
                    }

                    @Override
                    public void beforeEnd() {
                    }

                    @Override
                    public void onError(Exception ex) {
                        listener.onError(ex);
                    }
                });
                localClient.start();
            }
        } catch (Exception ex) {
            listener.onError(ex);
        } finally {
            listener.beforeEnd();
            this.close();
            for (ForwardClient client : clients) {
                client.close();
            }
            logger.info("Server shut down on " + port);
            listener.onEnd();
        }
    }

    @Override
    public String toString() {
        return "" + port + ((forwardHost != null) ? ("\t" + forwardHost + "\t" + forwardPort) : "");
    }
}
