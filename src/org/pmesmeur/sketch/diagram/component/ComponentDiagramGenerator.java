package org.pmesmeur.sketch.diagram.component;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ComponentDiagramGenerator {

    private final Project project;
    private final List<String> patternsToExclude;
    private final Set<Module> managedModules;


    public static Builder newBuilder(Project project) {
        return new Builder(project);
    }



    public static class Builder {
        private final Project project;
        private final List<String> patternsToExclude;


        public Builder(Project project) {
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
        System.out.println("@startuml");
        System.out.println("");

        ModulesHierarchyGenerator modulesHierarchyGenerator = new ModulesHierarchyGenerator(managedModules);
        modulesHierarchyGenerator.generate();

        for (Module module : managedModules) {
            printModuleDependencies(module);
        }

        System.out.println("");
        System.out.println("@enduml");
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
        System.out.println(componentName(moduleName) + " --> " + componentName(dependentModulesName));
    }



    private String componentName(String originalName) {
        return "[" + originalName + "]";
    }

}
