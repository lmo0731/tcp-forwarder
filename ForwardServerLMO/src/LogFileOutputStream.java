
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir <munkhochir@mobicom.mn>
 */
public class LogFileOutputStream extends FileOutputStream {

    boolean server = true;

    public LogFileOutputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public LogFileOutputStream(FileDescriptor fdObj) {
        super(fdObj);
    }

    public LogFileOutputStream(String name) throws FileNotFoundException {
        super(name);
    }

    public LogFileOutputStream(File file, boolean append) throws FileNotFoundException {
        super(file, append);
    }

    public LogFileOutputStream(String name, boolean append) throws FileNotFoundException {
        super(name, append);
    }

    public void writeClient(int b) throws IOException {
        if (server) {
            super.write("\n----------client----------:\n".getBytes());
        }
        super.write(b); //To change body of generated methods, choose Tools | Templates.
        server = false;
    }

    public void writeServer(int b) throws IOException {
        if (!server) {
            super.write("\n----------server----------:\n".getBytes());
        }
        super.write(b); //To change body of generated methods, choose Tools | Templates.
        server = true;
    }
}
