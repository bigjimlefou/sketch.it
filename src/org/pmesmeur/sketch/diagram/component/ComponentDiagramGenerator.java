package org.pmesmeur.sketch.diagram.component;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ComponentDiagramGenerator {

    private final OutputStream outputStream;
    private final Project project;
    private final List<String> patternsToExclude;
    private final Set<Module> managedModules;


    public static Builder newBuilder(OutputStream outputStream, Project project) {
        return new Builder(outputStream, project);
    }



    public static class Builder {
        private final OutputStream outputStream;
        private final Project project;
        private final List<String> patternsToExclude;


        public Builder(OutputStream outputStream, Project project) {
            this.outputStream = outputStream;
            this.project = project;
            this.patternsToExclude = new ArrayList<String>();
        }


        public Builder exclude(String patternToExclude) {
            patternsToExclude.add(patternToExclude);
            return this;
        }


        public ComponentDiagramGenerator build() {
            return new ComponentDiagramGenerator(this);
        }

    }



    protected ComponentDiagramGenerator(Builder builder) {
        this.outputStream = builder.outputStream;
        this.project = builder.project;
        this.patternsToExclude = builder.patternsToExclude;
        this.managedModules = computeManagedModuleList();
    }



    private Set<Module> computeManagedModuleList() {
        Set<Module> managedModules = new HashSet<Module>();

        ModuleManager moduleManager = ModuleManager.getInstance(project);
        for (Module module : moduleManager.getModules()) {
            if (!excluded(module))
                managedModules.add(module);
        }

        return managedModules;
    }



    private boolean excluded(Module module) {
        return excluded(module.getName());
    }



    private boolean excluded(String moduleName) {
        for (String patternToExclude : patternsToExclude) {
            if (moduleName.contains(patternToExclude)) {
                return true;
            }
        }

        return false;
    }



    public void generate() {
        write("@startuml");
        write("");

        ModulesHierarchyGenerator modulesHierarchyGenerator = new ModulesHierarchyGenerator(managedModules);
        modulesHierarchyGenerator.generate(outputStream);

        for (Module module : managedModules) {
            printModuleDependencies(module);
        }

        write("");
        write("@enduml");
    }



    private void write(String s) {
        String dataToWrite = s + "\n";
        try {
            outputStream.write(dataToWrite.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void printModuleDependencies(Module module) {
        String moduleName = module.getName();

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        String[] dependentModulesNames = moduleRootManager.getDependencyModuleNames();

        for (String dependentModulesName : dependentModulesNames) {
            if (!excluded(dependentModulesName)) {
                printModuleDependency(moduleName, dependentModulesName);
            }
        }
    }



    private void printModuleDependency(String moduleName, String dependentModulesName) {
        write(componentName(moduleName) + " --> " + componentName(dependentModulesName));
    }



    private String componentName(String originalName) {
        return "[" + originalName + "]";
    }

}
