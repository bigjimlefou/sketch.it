package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;

public class SketchItFile {

    private final VirtualFile outputFile;
    private OutputStream outputStream = null;


    public SketchItFile(VirtualFile outputFile) {
        this.outputFile = outputFile;
    }


    public VirtualFile getvf() {
        return outputFile;
    }

    public OutputStream getOutputStream() {
        if (outputStream == null) {
            try {
                outputStream = outputFile.getOutputStream(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputStream;
    }



    public void close() throws IOException {
        getOutputStream().close();
    }


    public void delete() {
        if (outputFile != null) {
            try {
                outputFile.delete(this);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
