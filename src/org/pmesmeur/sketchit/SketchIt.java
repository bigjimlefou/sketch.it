package org.pmesmeur.sketchit;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.pmesmeur.sketchit.diagram.UmlDiagramsGenerator;


public class SketchIt extends AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public SketchIt() {
        // Set the menu item name.
        super("Text _Boxes");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
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
            Notifications.Bus.notify(new Notification("Settings Error", "Sketch.It!", "Project not yet loaded: please wait and relaunch", NotificationType.WARNING));
        } else {
            generatePlantUmlDiagram(project);
            Notifications.Bus.notify(new Notification("Settings Error", "Sketch.It!", "UML model generated successfully", NotificationType.INFORMATION));
        }
    }



    private void generatePlantUmlDiagram(Project project) {
        project.save();

        UmlDiagramsGenerator umlDiagramsGenerator = new UmlDiagramsGenerator(project);

        umlDiagramsGenerator.generateComponentDiagram();
        umlDiagramsGenerator.generateClassDiagrams();
    }

}

