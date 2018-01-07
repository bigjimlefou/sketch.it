package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketchit.diagram.component.ComponentDiagramGenerator;

import java.io.IOException;
import java.io.OutputStream;


class UmlComponentDiagram extends UmlDiagram {
    private final Project project;


    public UmlComponentDiagram(Project project) {
        this.project = project;
    }



    @Override
    protected VirtualFile getOutputFile() throws IOException {
        String outputFileName = createOutputFileName(project.getName());
        return project.getBaseDir().findOrCreateChildData(this, outputFileName);
    }



    @Override
    protected void generateDiagram(OutputStream outputStream) {
        ComponentDiagramGenerator componentDiagramGenerator = getDiagramGeneratorBuilder(outputStream).build();
        componentDiagramGenerator.generate();
    }



    private ComponentDiagramGenerator.Builder getDiagramGeneratorBuilder(OutputStream outputStream) {
        String title = project.getName().toUpperCase() + "'s Component Diagram";

        return ComponentDiagramGenerator.newBuilder(outputStream, project)
                .title(title)
                .exclude("test")
                .exclude("feature");
    }

}
