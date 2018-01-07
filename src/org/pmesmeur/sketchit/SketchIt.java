package org.pmesmeur.sketchit;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
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
        UmlDiagramsGenerator umlDiagramsGenerator = new UmlDiagramsGenerator(event.getProject());

        umlDiagramsGenerator.generateComponentDiagram();
        umlDiagramsGenerator.generateClassDiagrams();
    }

}

