/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author munkhochir
 */
public interface SocketListener {

    void onStart();

    void onInput(int b);

    void onWrite(int b);

    void beforeEnd();

    void onEnd();
    
    void onError(Exception ex);
}
