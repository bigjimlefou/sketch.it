package org.pmesmeur.sketch.diagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.pmesmeur.sketch.diagram.clazz.ClassDiagramGenerator;
import org.pmesmeur.sketch.diagram.component.ComponentDiagramGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;


public class UmlDiagramsGenerator {

    public static final String COMPONENT_DIAGRAM_FILE_NAME = "components.plantuml";
    public static final String CLASS_DIAGRAM_FILE_NAME = "classes.plantuml";
    private final Project project;

    public UmlDiagramsGenerator(Project project) {
        this.project = project;
    }



    public void generateComponentDiagram() {
        try {
            OutputStream outputStream = getComponentDiagramOutputStream();

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
    private OutputStream getComponentDiagramOutputStream()  throws IOException {
        VirtualFile childData = project.getBaseDir().findOrCreateChildData(this, COMPONENT_DIAGRAM_FILE_NAME);
        return childData.getOutputStream(this);
    }



    public void generateClassDiagrams() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            generateModuleClassDiagram(module);
            generateModuleClassDiagramForEachSourceDirectory(module);
        }
   }



    private void generateModuleClassDiagram(Module module) {
        try {
            OutputStream outputStream = getClassDiagramOutputStream(module);
            ClassDiagramGenerator classDiagramGenerator =
                    ClassDiagramGenerator.newBuilder(outputStream, project, module)
                                         .exclude("test")
                                         .build();
            classDiagramGenerator.generate();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    @NotNull
    private OutputStream getClassDiagramOutputStream(Module module) throws IOException {
        VirtualFile moduleFile = module.getModuleFile();
        VirtualFile moduleDirectory = moduleFile.getParent();

        return getClassDiagramOutputStream(moduleDirectory);
    }



    private OutputStream getClassDiagramOutputStream(VirtualFile moduleDirectory) throws IOException {
        VirtualFile childData = moduleDirectory.findOrCreateChildData(this, CLASS_DIAGRAM_FILE_NAME);
        return childData.getOutputStream(this);
    }



    private void generateModuleClassDiagramForEachSourceDirectory(Module module) {
        JavaFileFinder javaFileFinder = new JavaFileFinder(project, module);

        Set<VirtualFile> directories = javaFileFinder.getFoundDirectories();
        for (VirtualFile directory : directories) {
            generateModuleClassDiagramForSourceDirectory(module, directory);
        }
    }



    private void generateModuleClassDiagramForSourceDirectory(Module module, VirtualFile directory) {
        try {
            OutputStream outputStream = getClassDiagramOutputStream(directory);
            ClassDiagramGenerator classDiagramGenerator =
                    ClassDiagramGenerator.newBuilder(outputStream, project, module)
                            .sourceDirectory(directory)
                            .build();
            classDiagramGenerator.generate();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
