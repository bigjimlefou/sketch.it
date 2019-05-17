package org.pmesmeur.sketchit;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.pmesmeur.sketchit.diagram.UmlDiagramsGenerator;
import org.pmesmeur.sketchit.ui.ExceptionDialog;
import org.pmesmeur.sketchit.ui.Notifyer;


public class SketchIt extends AnAction {

    private static final Logger LOG = Logger.getInstance(SketchIt.class);

    public SketchIt() {
        super();
    }



    @Override
    public void actionPerformed(final AnActionEvent event) {
        runInsideAnIntellijWriteAction(event);
    }



    private void runInsideAnIntellijWriteAction(final AnActionEvent event) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                actionBody(event);
            }
        });
    }



    private void actionBody(AnActionEvent event) {
        Project project = event.getProject();

        if (!project.isInitialized()) {
            Notifyer.warning("Project not yet loaded: please wait and relaunch");
        } else {
            generatePlantUmlModel(project);
        }
    }



    private void generatePlantUmlModel(Project project) {
        LOG.info("Starting generation of PlantUML model for project " + project.getName());
        generatePlantUmlDiagramWithExceptionHandling(project);
        LOG.info("Ending generation of PlantUML model for project " + project.getName());
    }



    private void generatePlantUmlDiagramWithExceptionHandling(Project project) {
        try {
            generatePlantUmlDiagram(project);
        } catch (Throwable e) {
            ExceptionDialog.show(project, e);
        }
    }



    private void generatePlantUmlDiagram(Project project) {
        project.save();

        UmlDiagramsGenerator umlDiagramsGenerator = new UmlDiagramsGenerator(project);

        umlDiagramsGenerator.generateComponentDiagram();
        umlDiagramsGenerator.generateClassDiagrams();

        Notifyer.info("PlantUML model generated successfully");
    }

}

