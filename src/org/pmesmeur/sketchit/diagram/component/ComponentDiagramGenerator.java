package org.pmesmeur.sketchit.diagram.component;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import org.pmesmeur.sketchit.diagram.plantuml.PlantUmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ComponentDiagramGenerator {

    private final PlantUmlWriter plantUmlWriter;
    private final Project project;
    private final List<String> patternsToExclude;
    private final Set<Module> managedModules;
    private final String title;


    public static Builder newBuilder(PlantUmlWriter plantUmlWriter, Project project) {
        return new Builder(plantUmlWriter, project);
    }



    public static class Builder {
        private final PlantUmlWriter plantUmlWriter;
        private final Project project;
        private final List<String> patternsToExclude;
        private String title;


        public Builder(PlantUmlWriter plantUmlWriter, Project project) {
            this.plantUmlWriter = plantUmlWriter;
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

        public Builder title(String title) {
            this.title = title;
            return this;
        }
    }



    protected ComponentDiagramGenerator(Builder builder) {
        this.plantUmlWriter = builder.plantUmlWriter;
        this.project = builder.project;
        this.patternsToExclude = builder.patternsToExclude;
        this.title = builder.title;
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
        plantUmlWriter.startDiagram(title);

        ModulesHierarchyGenerator modulesHierarchyGenerator = new ModulesHierarchyGenerator(managedModules);
        modulesHierarchyGenerator.generate(plantUmlWriter);

        for (Module module : managedModules) {
            printModuleDependencies(module);
        }

        plantUmlWriter.endDiagram();
    }



    private void printModuleDependencies(Module module) {
        String moduleName = module.getName();

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        String[] dependentModulesNames = moduleRootManager.getDependencyModuleNames();

        for (String dependentModulesName : dependentModulesNames) {
            if (!excluded(dependentModulesName)) {
                plantUmlWriter.addComponentDependency(moduleName, dependentModulesName);
            }
        }
    }

}
