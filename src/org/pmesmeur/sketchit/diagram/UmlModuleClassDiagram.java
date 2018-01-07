package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketchit.diagram.clazz.ClassDiagramGenerator;

import java.io.IOException;
import java.io.OutputStream;


class UmlModuleClassDiagram extends UmlDiagram {
    private final Project project;
    private final Module module;



    public UmlModuleClassDiagram(Project project, Module module) {
        this.project = project;
        this.module = module;
    }



    @Override
    protected VirtualFile getOutputFile() throws IOException {
        VirtualFile moduleFile = module.getModuleFile();
        VirtualFile moduleDirectory = moduleFile.getParent();

        return getClassDiagramOutputStream(moduleDirectory);
    }



    private VirtualFile getClassDiagramOutputStream(VirtualFile moduleDirectory) throws IOException {
        String outputFileName = createOutputFileName(moduleDirectory.getName());
        return moduleDirectory.findOrCreateChildData(this, outputFileName);
    }



    @Override
    protected void generateDiagram(OutputStream outputStream) {
        ClassDiagramGenerator classDiagramGenerator = getDiagramGeneratorBuilder(outputStream).build();
        classDiagramGenerator.generate();
    }



    private ClassDiagramGenerator.Builder getDiagramGeneratorBuilder(OutputStream outputStream) {
        String title = module.getName().toUpperCase() + "'s Class Diagram";

        return ClassDiagramGenerator.newBuilder(outputStream, project, module)
                .title(title)
                .exclude("test");
    }

}
