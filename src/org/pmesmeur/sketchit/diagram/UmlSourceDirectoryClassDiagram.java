package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketchit.diagram.clazz.ClassDiagramGenerator;
import org.pmesmeur.sketchit.diagram.plantuml.PlantUmlWriter;

import java.io.IOException;


class UmlSourceDirectoryClassDiagram extends UmlDiagram {
    private final Project project;
    private final Module module;
    private final VirtualFile moduleDirectory;



    public UmlSourceDirectoryClassDiagram(Project project,
                                          Module module,
                                          VirtualFile moduleDirectory) {
        this.project = project;
        this.module = module;
        this.moduleDirectory = moduleDirectory;
    }



    @Override
    protected VirtualFile getOutputFile() throws IOException {
        String outputFileName = createOutputFileName(moduleDirectory.getName());
        return moduleDirectory.findOrCreateChildData(this, outputFileName);
    }



    @Override
    protected void generateDiagram(PlantUmlWriter plantUmlWriter) {
        ClassDiagramGenerator classDiagramGenerator = getDiagramGeneratorBuilder(plantUmlWriter).build();
        classDiagramGenerator.generate();
    }



    private ClassDiagramGenerator.Builder getDiagramGeneratorBuilder(PlantUmlWriter plantUmlWriter) {
        String title = moduleDirectory.getName().toUpperCase() + "'s Class Diagram";

        return ClassDiagramGenerator.newBuilder(plantUmlWriter, project, module)
                .title(title)
                .sourceDirectory(moduleDirectory);
    }

}
