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
import java.util.NoSuchElementException;
import java.util.Set;


public class UmlDiagramsGenerator {

    private static final String FILE_EXTENSION = "plantuml";
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
        String outputFileName = createOutputFileName(project.getName());
        VirtualFile childData = project.getBaseDir().findOrCreateChildData(this, outputFileName);
        return childData.getOutputStream(this);
    }



    @NotNull
    private String createOutputFileName(String name) {
        return name + "." + FILE_EXTENSION;
    }



    public void generateClassDiagrams() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            generateModuleClassDiagram(module);
            generateModuleClassDiagramForEachSourceDirectory(module);
        }
   }



    private void generateModuleClassDiagram(Module module) {
        VirtualFile outputFile = null;

        try {
            outputFile = getClassDiagramOutputStream(module);
            OutputStream outputStream = outputFile.getOutputStream(this);

            ClassDiagramGenerator classDiagramGenerator =
                    ClassDiagramGenerator.newBuilder(outputStream, project, module)
                                         .exclude("test")
                                         .build();

            classDiagramGenerator.generate();
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
            e.printStackTrace();
        }

    }



    @NotNull
    private VirtualFile getClassDiagramOutputStream(Module module) throws IOException {
        VirtualFile moduleFile = module.getModuleFile();
        VirtualFile moduleDirectory = moduleFile.getParent();

        return getClassDiagramOutputStream(moduleDirectory);
    }



    private VirtualFile getClassDiagramOutputStream(VirtualFile moduleDirectory) throws IOException {
        String outputFileName = createOutputFileName(moduleDirectory.getName());
        return moduleDirectory.findOrCreateChildData(this, outputFileName);
    }



    private void generateModuleClassDiagramForEachSourceDirectory(Module module) {
        JavaFileFinder javaFileFinder = new JavaFileFinder(project, module);

        Set<VirtualFile> directories = javaFileFinder.getFoundDirectories();
        for (VirtualFile directory : directories) {
            generateModuleClassDiagramForSourceDirectory(module, directory);
        }
    }



    private void generateModuleClassDiagramForSourceDirectory(Module module, VirtualFile directory) {
        VirtualFile outputFile = null;

        try {
            outputFile = getClassDiagramOutputStream(directory);
            OutputStream outputStream = outputFile.getOutputStream(this);

            ClassDiagramGenerator classDiagramGenerator =
                    ClassDiagramGenerator.newBuilder(outputStream, project, module)
                            .sourceDirectory(directory)
                            .build();

            classDiagramGenerator.generate();
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
            e.printStackTrace();
        }
    }

}
