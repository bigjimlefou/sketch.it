package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;


abstract class UmlDiagram {
    private static final String FILE_EXTENSION = "plantuml";

    public void generate() {
        VirtualFile outputFile = null;

        try {
            outputFile = getOutputFile();
            OutputStream outputStream = outputFile.getOutputStream(this);

            generateDiagram(outputStream);

            outputStream.close();
        } catch (NoSuchElementException e) {
            if (outputFile != null) {
                try {
                    outputFile.delete(this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while generating diagram");
            e.printStackTrace();
        }
    }


    protected abstract VirtualFile getOutputFile() throws IOException;
    protected abstract void generateDiagram(OutputStream outputStream);


    protected String createOutputFileName(String name) {
        return name + "." + FILE_EXTENSION;
    }

}
