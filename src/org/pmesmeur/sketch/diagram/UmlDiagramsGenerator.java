package org.pmesmeur.sketch.diagram;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketch.diagram.component.ComponentDiagramGenerator;

import java.io.IOException;
import java.io.OutputStream;


public class UmlDiagramsGenerator {

    private final Project project;

    public UmlDiagramsGenerator(Project project) {
        this.project = project;
    }



    public void run() {
        try {
            VirtualFile childData = project.getBaseDir().createChildData(this, "components.plantuml");
            OutputStream outputStream = childData.getOutputStream(this);

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

}
