package org.pmesmeur.sketch.diagram;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.pmesmeur.sketch.diagram.component.ComponentDiagramGenerator;


public class UmlDiagramsGenerator {

    private final Project project;

    public UmlDiagramsGenerator(Project project) {
        this.project = project;
    }



    public void run() {
        ComponentDiagramGenerator componentDiagramGenerator =
                ComponentDiagramGenerator.newBuilder(project)
                                         .exclude("test")
                                         .exclude("feature")
                                         .build();
        componentDiagramGenerator.generate();

        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile vf : vFiles) {
            System.out.println("file: " + vf.getPath());
        }

        printModulesDependencies();
    }



    private void printModulesDependencies() {
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            printModuleDependencies(module);
        }
    }



    private void printModuleDependencies(Module module) {
        System.out.println("* Module: " + module.getName());

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        String[] dependentModulesNames = moduleRootManager.getDependencyModuleNames();

        for (String dependentModulesName : dependentModulesNames) {
            System.out.println("  - " + dependentModulesName);
        }
    }

}
