
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author munkhochir <munkhochir@mobicom.mn>
 */
public class ShellBufferedWriter extends BufferedWriter {

    private String name;

    public ShellBufferedWriter(String name, Writer out) {
        super(out);
        this.name = name;
    }

    public ShellBufferedWriter(String name, Writer out, int sz) {
        super(out, sz);
        this.name = name;
    }

    @Override
    public void flush() throws IOException {
        this.write(String.format("[%s]$ ", name));
        super.flush(); //To change body of generated methods, choose Tools | Templates.
    }
}
