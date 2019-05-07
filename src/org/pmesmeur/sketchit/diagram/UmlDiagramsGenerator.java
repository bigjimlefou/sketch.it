package org.pmesmeur.sketchit.diagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Set;


public class UmlDiagramsGenerator {
    private final Project project;

    public UmlDiagramsGenerator(Project project) {
        this.project = project;
    }



    public void generateComponentDiagram() {
        UmlComponentDiagram umlComponentDiagram = new UmlComponentDiagram(project);
        umlComponentDiagram.generate();
    }



    public void generateClassDiagrams() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            generateModuleClassDiagram(module);
            generateModuleClassDiagramForEachSourceDirectory(module);
        }
   }



    private void generateModuleClassDiagram(Module module) {
        UmlModuleClassDiagram umlModuleClassDiagram = new UmlModuleClassDiagram(project, module);
        umlModuleClassDiagram.generate();
    }



    private void generateModuleClassDiagramForEachSourceDirectory(Module module) {
        JavaFileFinder javaFileFinder = new JavaFileFinder(project, module);

        Set<VirtualFile> directories = javaFileFinder.getFoundDirectories();
        for (VirtualFile directory : directories) {
            generateModuleClassDiagramForSourceDirectory(module, directory);
        }
    }



    private void generateModuleClassDiagramForSourceDirectory(Module module, VirtualFile directory) {
        UmlSourceDirectoryClassDiagram umlSourceDirectoryClassDiagram = new UmlSourceDirectoryClassDiagram(project, module, directory);
        umlSourceDirectoryClassDiagram.generate();
    }

}
