
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir
 */
public class ForwardClient extends Thread {

    Socket socket;
    SocketListener listener = new DefaultSocketListener();

    public ForwardClient(Socket socket) {
        this.socket = socket;
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ex) {
            }
        }
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void send(int b) {
        listener.onWrite(b);
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        if (socket == null) {
            return;
        }
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
            listener.onStart();
            while (true) {
                int b = in.read();
                if (b == -1) {
                    break;
                }
                listener.onInput(b);
            }
        } catch (IOException ex) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ex) {
                }
            }
            listener.onEnd();
        }
    }
}
