package org.pmesmeur.sketchit.diagram.plantuml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


public class PlantUmlWriter {

    private final OutputStream outputStream;

    public PlantUmlWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }



    public void startDiagram(String title) {
        write("@startuml");
        write("");

        if (title != null) {
            writeTitle(title);
        }
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



    private void writeTitle(String title) {
        String underlignedTitle = "__" + title + "__";
        write("title " + underlignedTitle + "\\n");
        write("");
    }



    public void endDiagram() {
        write("");
        write("@enduml");
    }



    public void addComponentDependency(String moduleName, String dependentModulesName) {
        write(formatComponentName(moduleName) + " --> " + formatComponentName(dependentModulesName));
    }



    private String formatComponentName(String originalName) {
        return "[" + originalName + "]";
    }



    public void startComponentDeclaration(String componentName) {
        write("component \"" + componentName + "\" {");
    }



    public void endComponentDeclaration() {
        write("}\n\n");
    }



    public void addSubComponent(String componentName) {
        write("    " + formatComponentName(componentName));
    }



    public void declareEnum(List<String> packageStack, String enumName) {
        startPackageStack(packageStack);
        write("enum " + enumName);
        endPackageStack(packageStack);
    }



    private void startPackageStack(List<String> packageStack) {
        for (String pkg : packageStack) {
            write("package " + pkg + " {");
        }
    }



    private void endPackageStack(List<String> packageStack) {
        int nbPackage = packageStack.size();

        while (nbPackage-- > 0) {
            write("}");
        }

        write("\n");
    }



    public void startClassDeclaration(List<String> packageStack, String className) {
        startPackageStack(packageStack);
        write("class " + className + " {");
    }



    public void startAbstractClassDeclaration(List<String> packageStack, String abstractClassName) {
        startPackageStack(packageStack);
        write("abstract class " + abstractClassName + " {");
    }



    public void startInterfaceDeclaration(List<String> packageStack, String interfaceName) {
        startPackageStack(packageStack);
        write("interface " + interfaceName + " {");
    }



    public void endClassDeclaration(List<String> packageStack) {
        write("}");
        endPackageStack(packageStack);
    }



    public void addClassesInheritence(String inheritedClass, String parentClass) {
        write(inheritedClass + " -up--|> " + parentClass);

    }



    public void addClassesAssociation(String fromClass, String toClass, String associationName) {
        write(fromClass + " o-- " + toClass + " : " + associationName);

    }



    public void declareField(String visibility, String fieldType, String name) {
        String fieldDeclaration = computeMethodDeclaration(visibility, fieldType, name);
        writeMember(fieldDeclaration);
    }


    private void writeMember(String member) {
        write("  " + member);
    }


    private String computeMethodDeclaration(String visibility, String fieldType, String fieldName) {
        return computeVisibility(visibility) + " " + fieldName + " : " + fieldType;
    }



    private String computeVisibility(String visibility) {
        if (visibility == "public") {
            visibility = "+";
        } else if (visibility == "protected") {
            visibility = "#";
        } else if (visibility == "private") {
            visibility = "-";
        } else if (visibility == "packageLocal") {
            visibility = "~";
        }

        return visibility;
    }



    public void declareStaticField(String visibility, String fieldType, String name) {
        String fieldDeclaration = computeMethodDeclaration(visibility, fieldType, name);
        writeStaticMember(fieldDeclaration);
    }



    private void writeStaticMember(String fieldDeclaration) {
        write("  {static} " + fieldDeclaration);
    }



    public void declareMethod(String visibility, String methodName) {
        String methodDeclaration = computeMethodDeclaration(visibility, methodName);
        writeMember(methodDeclaration);
    }



    private String computeMethodDeclaration(String visibility, String methodName) {
        return computeVisibility(visibility) + " " + methodName + "()";
    }



    public void declareStaticMethod(String visibility, String methodName) {
        String methodDeclaration = computeMethodDeclaration(visibility, methodName);
        writeStaticMember(methodDeclaration);
    }



    public void declareAbstractMethod(String visibility, String methodName) {
        String methodDeclaration = computeMethodDeclaration(visibility, methodName);
        writeAbstractMember(methodDeclaration);
    }



    private void writeAbstractMember(String methodDeclaration) {
        write("  {abstract} " + methodDeclaration);
    }

}
