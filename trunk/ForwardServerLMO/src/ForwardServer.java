
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    int forwardPort;
    String forwardHost;
    SocketListener listener = new DefaultSocketListener();
    final List<ForwardClient> clients = new ArrayList<ForwardClient>();

    public ForwardServer(int port, MyLogger logger) {
        this.port = port;
        this.logger = (logger != null ? logger : new DefaultMyLogger());
        try {
            socket = new ServerSocket(port);
            logger.info("Forwarder server started on " + port);
        } catch (IOException ex) {
            socket = null;
            logger.error("starting server socket", ex);
        }
    }

    void setForwardServer(String host, int port) {
        forwardPort = port;
        forwardHost = host;
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public void run() {
        try {
            while (socket != null) {
                Socket clientSocket = socket.accept();
                Socket serviceSocket = null;
                try {
                    serviceSocket = new Socket(forwardHost, forwardPort);
                } catch (Exception ex) {
                    logger.error("Starting forwarding server ", ex);
                }
                final ForwardClient client = new ForwardClient(clientSocket);
                clients.add(client);
                final ForwardClient server = new ForwardClient(serviceSocket);
                String id = "" + new Date().getTime();
                File logReq = new File(forwardHost + "_" + forwardPort + "_" + id + "_req");
                File logRes = new File(forwardHost + "_" + forwardPort + "_" + id + "_res");
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
                final OutputStream reqOutImpl = (reqOut != null ? reqOut : null);
                final OutputStream resOutImpl = (resOut != null ? resOut : null);
                client.setListener(new SocketListener() {
                    @Override
                    public void onStart() {
                        logger.info("Forwarder client successfully started.");
                    }

                    @Override
                    public void onInput(int b) {
                        if (server != null) {
                            server.send(b);
                        }
                    }

                    @Override
                    public void onWrite(int b) {
                    }

                    @Override
                    public void onEnd() {
                        if (server != null) {
                            server.close();
                        }
                        logger.info("Forwarder client's connection ended.");
                        try {
                            clients.remove(client);
                        } catch (Exception ex) {
                        }
                    }
                });
                server.setListener(new SocketListener() {
                    @Override
                    public void onStart() {
                        logger.info("Successfully bound to forward server.");
                    }

                    @Override
                    public void onInput(int b) {
                        if (reqOutImpl != null) {
                            try {
                                reqOutImpl.write(b);
                            } catch (Exception ex) {
                            }
                        }
                    }

                    @Override
                    public void onWrite(int b) {
                        if (resOutImpl != null) {
                            try {
                                resOutImpl.write(b);
                            } catch (IOException ex) {
                            }
                        }
                    }

                    @Override
                    public void onEnd() {
                        if (client != null) {
                            client.close();
                        }
                        if (reqOutImpl != null) {
                            try {
                                reqOutImpl.flush();
                            } catch (IOException ex) {
                            }
                            try {

                                reqOutImpl.close();
                            } catch (IOException ex) {
                            }
                        }
                        if (resOutImpl != null) {
                            try {
                                resOutImpl.flush();
                            } catch (IOException ex) {
                            }
                            try {
                                resOutImpl.close();
                            } catch (IOException ex) {
                            }
                        }
                        logger.info("Connection of forward server ended.");
                    }
                });
                if (client != null) {
                    client.start();
                }
                if (server != null) {
                    server.start();
                }
            }
        } catch (IOException ex) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
            logger.info("Forwarder server shut down.");
        }
    }
}
