package org.pmesmeur.sketch.diagram;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.pmesmeur.sketch.diagram.clazz.ClassDiagramGenerator;
import org.pmesmeur.sketch.diagram.component.ComponentDiagramGenerator;

import java.io.IOException;
import java.io.OutputStream;


public class UmlDiagramsGenerator {

    public static final String OUTPUT_FILE_NAME = "components.plantuml";
    private final Project project;

    public UmlDiagramsGenerator(Project project) {
        this.project = project;
    }



    public void generateComponentDiagram() {
        try {
            OutputStream outputStream = getOutputStream();

            ComponentDiagramGenerator componentDiagramGenerator =
                    ComponentDiagramGenerator.newBuilder(outputStream, project)
                            .exclude("test")
                            .exclude("feature")
                            .build();
            componentDiagramGenerator.generate();

            outputStream.close();
        } catch (IOException e) {
            System.out.println("ERROR");
            e.printStackTrace();
        }
    }



    @NotNull
    private OutputStream getOutputStream()  throws IOException {
        VirtualFile childData = project.getBaseDir().findOrCreateChildData(this, OUTPUT_FILE_NAME);
        return childData.getOutputStream(this);
    }
*/


    public void generateClassDiagrams() {
        ClassDiagramGenerator classDiagramGenerator = new ClassDiagramGenerator(project);
        classDiagramGenerator.generate();
    }

}
