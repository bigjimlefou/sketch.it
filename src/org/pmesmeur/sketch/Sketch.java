package org.pmesmeur.sketch;

import com.intellij.openapi.actionSystem.*;
import org.pmesmeur.sketch.diagram.UmlDiagramsGenerator;


public class Sketch extends AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public Sketch() {
        // Set the menu item name.
        super("Text _Boxes");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }



    @Override
    public void actionPerformed(AnActionEvent event) {
        UmlDiagramsGenerator umlDiagramsGenerator = new UmlDiagramsGenerator(event.getProject());
        umlDiagramsGenerator.generateComponentDiagram();
        System.out.print("\n\n\n");
        umlDiagramsGenerator.generateClassDiagrams();
    }

}

