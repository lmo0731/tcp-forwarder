/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author munkhochir
 */
public interface MyLogger {

    void info(String msg);

    void error(String msg);

    void debug(String msg);

    void warn(String msg);

    void fatal(String msg);

    void info(String msg, Exception ex);

    void error(String msg, Exception ex);

    void debug(String msg, Exception ex);

    void warn(String msg, Exception ex);

    void fatal(String msg, Exception ex);
}
