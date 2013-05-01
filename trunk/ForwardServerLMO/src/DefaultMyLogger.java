/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author munkhochir
 */
public class DefaultMyLogger implements MyLogger {

    @Override
    public void info(String msg) {
        System.out.println(String.format("INFO: %s", msg));
    }

    @Override
    public void error(String msg) {
        System.err.println(String.format("ERROR: %s", msg));
    }

    @Override
    public void debug(String msg) {
        System.out.println(String.format("DEBUG: %s", msg));
    }

    @Override
    public void warn(String msg) {
        System.out.println(String.format("WARN: %s", msg));
    }

    @Override
    public void fatal(String msg) {
        System.err.println(String.format("FATAL: %s", msg));
    }

    @Override
    public void info(String msg, Exception ex) {
        System.out.println(String.format("INFO: %s", msg));
        ex.printStackTrace(System.out);
    }

    @Override
    public void error(String msg, Exception ex) {
        System.err.println(String.format("ERROR: %s", msg));
        ex.printStackTrace(System.err);
    }

    @Override
    public void debug(String msg, Exception ex) {
        System.out.println(String.format("DEBUG: %s", msg));
        ex.printStackTrace(System.out);
    }

    @Override
    public void warn(String msg, Exception ex) {
        System.out.println(String.format("WARN: %s", msg));
        ex.printStackTrace(System.out);
    }

    @Override
    public void fatal(String msg, Exception ex) {
        System.err.println(String.format("FATAL: %s", msg));
        ex.printStackTrace(System.err);
    }
}
