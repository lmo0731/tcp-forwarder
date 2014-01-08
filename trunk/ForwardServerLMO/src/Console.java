
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir
 */
public class Console {

    private Map<Integer, ForwardServer> servers = new HashMap<Integer, ForwardServer>();
    private List<ActionListener> listeners = new ArrayList<ActionListener>();

    public Console() {
    }

    private void addServer(final int port, final String fhost, final int fport) throws IllegalArgumentException {
        final Logger log4j = Logger.getLogger("" + port);
        //BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%p: %C:%L %m%n")));
        try {
            log4j.addAppender(new DailyRollingFileAppender(new PatternLayout(""), "" + port + ".log", "yyyy-MM-dd"));
        } catch (IOException ex) {
        }
        StringBuilder prefixsb = new StringBuilder();
        boolean validServer = false;

        if (port > 65535) {
            throw new IllegalArgumentException("local port is invlaid");
        }
        if (port > 65535) {
            throw new IllegalArgumentException("remote port is invlaid");
        }
        try {
            Inet4Address.getByName(fhost);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("remote host is unreachable");
        }
        prefixsb.append("[" + fhost + ":" + fport + "] ");
        validServer = true;
        final String prefix = prefixsb.toString();
        MyLogger logger = new MyLogger() {
            @Override
            public void info(String msg) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.INFO, prefix + msg, null);
            }

            @Override
            public void error(String msg) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.ERROR, prefix + msg, null);
            }

            @Override
            public void debug(String msg) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.DEBUG, prefix + msg, null);
            }

            @Override
            public void warn(String msg) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.WARN, prefix + msg, null);
            }

            @Override
            public void fatal(String msg) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.FATAL, prefix + msg, null);
            }

            @Override
            public void info(String msg, Exception ex) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.INFO, prefix + msg, ex);
            }

            @Override
            public void error(String msg, Exception ex) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.ERROR, prefix + msg, ex);
            }

            @Override
            public void debug(String msg, Exception ex) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.DEBUG, prefix + msg, ex);
            }

            @Override
            public void warn(String msg, Exception ex) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.WARN, prefix + msg, ex);
            }

            @Override
            public void fatal(String msg, Exception ex) {
                StackTraceElement[] stes = Thread.currentThread().getStackTrace();
                log4j.log(stes[1].getClassName(), Level.FATAL, prefix + msg, ex);
            }
        };
        final ForwardServer server = new ForwardServer(port, fhost, fport, logger);
        server.setListener(new SocketListener() {
            @Override
            public void onStart() {
                servers.put(port, server);
                for (ActionListener listener : listeners) {
                    listener.success("successfully started on " + port);
                }
            }

            @Override
            public void onInput(int b) {
            }

            @Override
            public void onWrite(int b) {
            }

            @Override
            public void onEnd() {
                servers.remove(port);
            }

            @Override
            public void beforeEnd() {
            }

            @Override
            public void onError(Exception ex) {
                for (ActionListener listener : listeners) {
                    listener.fail("server error", ex);
                }
            }
        });
        server.start();
    }

    private void closeServer(int port) {
        ForwardServer server = servers.get(port);
        if (server != null) {
            server.close();
            for (ActionListener listener : listeners) {
                listener.success("successfully closed " + port);
            }
        } else {
            for (ActionListener listener : listeners) {
                listener.success("port not bound");
            }
        }
    }

    private String getServers() {
        String ret = "";
        for (ForwardServer server : servers.values()) {
            ret += server.toString() + "\n";
        }
        return ret;
    }

    public void shell(final String name, final InputStream in, final OutputStream out, final ActionListener listener) {
        listeners.add(listener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedWriter bw = new ShellBufferedWriter(name, new OutputStreamWriter(out));
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                try {
                    bw.flush();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        String[] words = line.split("(\\s)+");
                        if (words[0].equals("exit") || words[0].equals("quit")) {
                            bw.write("stopping...");
                            bw.newLine();
                            break;
                        } else if (words[0].equals("add")) {
                            try {
                                Integer localport = Integer.parseInt(words[1]);
                                String remoteHost = words[2];
                                Integer remoteport = Integer.parseInt(words[3]);
                                addServer(localport, remoteHost, remoteport);
                            } catch (Exception ex) {
                                listener.fail("add error", ex);
                                bw.write(words[0] + " LOCALPORT REMOTEHOST REMOTEPORT");
                                bw.newLine();
                            }
                        } else if (words[0].equals("help")) {
                            bw.write("add - port nemeh");
                            bw.newLine();
                            bw.write("remove, stop - port hasah");
                            bw.newLine();
                            bw.write("exit, quit - garah");
                            bw.newLine();
                            bw.write("list, show, view - ajillaj bga portuudiig harah");
                            bw.newLine();
                            bw.write("help - tuslamj (ene hesgiig harna)");
                            bw.newLine();
                        } else if (words[0].equals("stop") || words[0].equals("remove")) {
                            try {
                                Integer localport = Integer.parseInt(words[1]);
                                closeServer(localport);
                            } catch (Exception ex) {
                                listener.fail("stop error", ex);
                                bw.write(words[0] + " LOCALPORT");
                                bw.newLine();
                            }
                        } else if (words[0].equals("list") || words[0].equals("show") || words[0].equals("view")) {
                            bw.write(getServers());
                            bw.newLine();
                        } else if (words[0].isEmpty()) {
                        } else {
                            bw.write("help command-g ashiglaj zov command-uudiig harna uu");
                            bw.newLine();
                        }
                        bw.flush();
                    }
                } catch (Exception ex) {
                    listener.fail("console error", ex);
                } finally {
                    try {
                        bw.close();
                    } catch (IOException ex) {
                    }
                    try {
                        br.close();
                    } catch (IOException ex) {
                    }
                    listeners.remove(listener);
                }
            }
        }, "console[" + name + "]").start();
    }

    public static void main(String... args) {
        Console console = new Console();
        console.shell("console", System.in, System.out, new ActionListener() {
            @Override
            public void success(String msg) {
                System.out.println(msg);
            }

            @Override
            public void fail(String msg, Exception ex) {
                System.out.println(msg + ": " + ex.getMessage());
            }
        });
        //console.addServer(1521, "192.168.50.42", 1521);
    }
}
