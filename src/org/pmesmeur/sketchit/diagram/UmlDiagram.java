package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketchit.diagram.plantuml.PlantUmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;


abstract class UmlDiagram {
    private static final Logger LOG = Logger.getInstance(UmlDiagram.class);

    private static final String FILE_EXTENSION = "plantuml";

    public void generate() {
        SketchItFile sketchItFile = null;

        try {
            sketchItFile = new SketchItFile(getOutputFile());

            generateDiagram(sketchItFile);

            sketchItFile.close();
        } catch (NoSuchElementException e) {
            LOG.info("Output file empty: deleting it");
            sketchItFile.delete();
        } catch (IOException e) {
            LOG.info("Error while generating diagram");
            e.printStackTrace();
        }
    }



    protected abstract VirtualFile getOutputFile() throws IOException;



    private void generateDiagram(SketchItFile sketchItFile) {
        generateDiagram(new PlantUmlWriter(sketchItFile.getOutputStream()));
    }



    protected abstract void generateDiagram(PlantUmlWriter plantUmlWriter);


    private void deleteEmptyFile(VirtualFile outputFile) {
        if (outputFile != null) {
            try {
                outputFile.delete(this);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }



    protected String createOutputFileName(String name) {
        return name + "." + FILE_EXTENSION;
    }

}
