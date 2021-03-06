package org.pmesmeur.sketchit.diagram.plantuml;

import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.List;


public class PlantUmlWriter {
    private static final Logger LOG = Logger.getInstance(PlantUmlWriter.class);

    private final OutputStream outputStream;
    private int indentation = 0;

    public PlantUmlWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }



    public void startDiagram(String title) {
        indentation = 0;
        write("@startuml");
        write("");

        if (title != null) {
            writeTitle(title);
        }

        indentation++;
    }



    private void write(String str) {
        String dataToWrite = str + "\n";
        if (!str.isEmpty()) {
            dataToWrite = indentation() + dataToWrite;
        }

        try {
            outputStream.write(dataToWrite.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String indentation() {
        int indentation = this.indentation;
        if (indentation < 0) {
            indentation = 0;
            LOG.info("Error: negative indentation!");
        }

        return CharBuffer.allocate(2 * indentation).toString().replace( '\0', ' ' );
    }



    private void writeTitle(String title) {
        String underlignedTitle = "__" + title + "__";
        write("title " + underlignedTitle + "\\n");
        write("");
    }



    public void endDiagram() {
        if (--indentation != 0) {
            LOG.info("Warning: indentation should be null when writing the end of the document!");
        }

        write("");
        writeFooter();
        write("");
        write("@enduml");
    }



    private void writeFooter() {
        write("");
        write("right footer");
        write("");
        write("");
        write("PlantUML diagram generated by SketchIt! (https://bitbucket.org/pmesmeur/sketch.it)");
        write("For more information about this tool, please contact philippe.mesmeur@gmail.com");
        write("endfooter");

    }



    public void addComponentDependency(String moduleName, String dependentModulesName) {
        write(formatComponentName(moduleName) + " --> " + formatComponentName(dependentModulesName));
    }



    private String formatComponentName(String originalName) {
        return "[" + originalName + "]";
    }



    public void startComponentDeclaration(String componentName) {
        write("component \"" + componentName + "\" {");
        indentation++;
    }



    public void endComponentDeclaration() {
        indentation--;
        write("}\n\n");
    }



    public void addSubComponent(String componentName) {
        write("    " + formatComponentName(componentName));
    }



    public void startEnumDeclaration(List<String> packageStack, String enumName) {
        startPackageStack(packageStack);
        write("enum " + enumName + " {");
        indentation++;
    }



    private void startPackageStack(List<String> packageStack) {
        for (String pkg : packageStack) {
            write("package " + pkg + " {");
            indentation++;
        }
    }



    private void endPackageStack(List<String> packageStack) {
        int nbPackage = packageStack.size();

        while (nbPackage-- > 0) {
            indentation--;
            write("}");
        }

        write("\n");
    }



    public void startClassDeclaration(List<String> packageStack, String className) {
        startPackageStack(packageStack);
        write("class " + className + " {");
        indentation++;
    }



    public void startAbstractClassDeclaration(List<String> packageStack, String abstractClassName) {
        startPackageStack(packageStack);
        write("abstract class " + abstractClassName + " {");
        indentation++;
    }



    public void startInterfaceDeclaration(List<String> packageStack, String interfaceName) {
        startPackageStack(packageStack);
        write("interface " + interfaceName + " {");
        indentation++;
    }



    public void endClassDeclaration(List<String> packageStack) {
        indentation--;
        write("}");
        endPackageStack(packageStack);
    }



    public void addClassesInheritence(String inheritedClass, String parentClass) {
        write(inheritedClass + " -up-|> " + parentClass);
    }



    public void addClassesAssociation(String fromClass, String toClass, String associationName) {
        write(fromClass + " o-- " + toClass + " : " + associationName);
    }



    public void addInnerClassesAssociation(String clazz, String innerClazz) {
        write(clazz + " +-down- " + innerClazz);
    }



    public void declareEnumValue(String enumValue) {
        write(enumValue);
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
