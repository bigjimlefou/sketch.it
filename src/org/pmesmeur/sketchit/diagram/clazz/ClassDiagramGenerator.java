package org.pmesmeur.sketchit.diagram.clazz;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.VisibilityUtil;
import org.jetbrains.annotations.NotNull;
import org.pmesmeur.sketchit.diagram.plantuml.PlantUmlWriter;

import java.util.*;
import com.intellij.openapi.diagnostic.Logger;


public class ClassDiagramGenerator {
    private static final Logger LOG = Logger.getInstance(ClassDiagramGenerator.class);

    private final PlantUmlWriter plantUmlWriter;
    private final Project project;
    private final Module module;
    private final Set<PsiClass> managedClasses;
    private final List<String> patternsToExclude;
    private final List<String> packages;
    private final VirtualFile sourceDirectory;
    private final String title;
    private final boolean hideMethods;
    private final boolean hideAttributes;
    private final boolean hideInnerClasses;


    public static ClassDiagramGenerator.Builder newBuilder(PlantUmlWriter plantUmlWriter,
                                                           Project project,
                                                           Module module) {
        return new ClassDiagramGenerator.Builder(plantUmlWriter, project, module);
    }



    public static class Builder {
        private final PlantUmlWriter plantUmlWriter;
        private final Project project;
        private final Module module;
        private final List<String> patternsToExclude;
        private VirtualFile sourceDirectory;
        private String title;
        private boolean hideMethods = false;
        private boolean hideAttributes = false;
        private boolean hideInnerClasses = false;


        public Builder(PlantUmlWriter plantUmlWriter, Project project, Module module) {
            this.plantUmlWriter = plantUmlWriter;
            this.project = project;
            this.module = module;
            this.patternsToExclude = new ArrayList<String>();
        }


        public Builder exclude(String patternToExclude) {
            patternsToExclude.add(patternToExclude);
            return this;
        }


        public Builder sourceDirectory(VirtualFile sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
            return this;
        }


        public Builder title(String title) {
            this.title = title;
            return this;
        }


        public Builder hideMethods(boolean hideMethods) {
            this.hideMethods = hideMethods;
            return this;
        }


        public Builder hideAttributes(boolean hideAttributes) {
            this.hideAttributes = hideAttributes;
            return this;
        }


        public Builder hideInnerClasses(boolean hideInnerClasses) {
            this.hideInnerClasses = hideInnerClasses;
            return this;
        }


        public ClassDiagramGenerator build() {
            return new ClassDiagramGenerator(this);
        }
    }



    protected ClassDiagramGenerator(Builder builder) {
        this.plantUmlWriter = builder.plantUmlWriter;
        this.project = builder.project;
        this.module = builder.module;
        this.patternsToExclude = builder.patternsToExclude;
        this.sourceDirectory = builder.sourceDirectory;
        this.title = builder.title;
        this.hideMethods = builder.hideMethods;
        this.hideAttributes = builder.hideAttributes;
        this.hideInnerClasses = builder.hideInnerClasses;
        this.packages = new ArrayList<String>();
        this.managedClasses = createListOfClassesToManage();
    }



    private Set<PsiClass> createListOfClassesToManage() {
        Finder finder = new Finder(project, module, patternsToExclude);

        Set<PsiClass> classes = finder.getClasses();
        if (classes.isEmpty()) {
            throw new NoSuchElementException("No classes found");
        }

        Set<String> packageSet = finder.getPackages();
        for (String pkg : packageSet) {
            packages.add(pkg);
        }
        Collections.sort(packages, new StringLengthComparator());

        return filterClasses(classes);
    }



    private Set<PsiClass> filterClasses(Set<PsiClass> classes) {
        Set<PsiClass> newSet = new HashSet<PsiClass>();

        for (PsiClass clazz : classes) {
            PsiElement parentElement = clazz.getParent();
            PsiElement owningDirectory = parentElement.getParent();
            if (sourceDirectory == null ||
                    (owningDirectory instanceof PsiDirectory &&
                     ((PsiDirectory) owningDirectory).getVirtualFile().equals(sourceDirectory))) {
                newSet.add(clazz);
            }
        }

        return newSet;
    }



    public void generate()
    {
        plantUmlWriter.startDiagram(title);

        List<PsiClass> classes = getListOfManagedClassesOrderedAlphabetically();
        for (PsiClass clazz : classes) {
            declareClass(clazz);
        }


        for (PsiClass clazz : classes) {
            declareClassRelationships(clazz);
        }

        plantUmlWriter.endDiagram();
    }



    private List<PsiClass> getListOfManagedClassesOrderedAlphabetically() {
        List<PsiClass> classes = new ArrayList<PsiClass>(managedClasses);
        Collections.sort(classes, new PsiClassComparator());

        return classes;
    }



    private void declareClass(PsiClass clazz) {
        String packageName = ((PsiJavaFile) clazz.getContainingFile()).getPackageName();
        List<String> packageStack = computePackageStack(packageName);

        declareClass(packageStack, clazz);

        if (!hideInnerClasses) {
            declareInnerClasses(clazz);
        }
    }



    private void declareClass(List<String> packageStack, PsiClass clazz) {
        if (clazz.isEnum()) {
            plantUmlWriter.declareEnum(packageStack, clazz.getName());
        } else {

            if (clazz.isInterface()) {
                plantUmlWriter.startInterfaceDeclaration(packageStack, clazz.getName());
            } else if (clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
                plantUmlWriter.startAbstractClassDeclaration(packageStack, clazz.getName());
            } else {
                plantUmlWriter.startClassDeclaration(packageStack, clazz.getName());
            }

            declareClassMembers(clazz);
            plantUmlWriter.endClassDeclaration(packageStack);

        }
    }



    private void declareInnerClasses(PsiClass clazz) {
        for (PsiClass innerClass : clazz.getAllInnerClasses()) {
            declareClass(innerClass);
        }
    }



    private List<String> computePackageStack(String packageName) {
        List<String> packageStack = new ArrayList<String>();

        for (String pkg : packages) {
            if (packageName.startsWith(pkg)) {
                packageStack.add(pkg);
            }
        }

        return packageStack;
    }



    private void declareClassMembers(PsiClass clazz) {
        if (!hideAttributes) {
            declareClassAttributes(clazz);
        }

        if (!hideMethods) {
            declareClassMethods(clazz);
        }
    }



    private void declareClassAttributes(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (getFieldDisplayType(clazz, field) == FieldDisplayType.ATTRIBUTE) {
                declareClassField(field);
            }
        }
    }



    private void declareClassField(PsiField field) {
        String visibility = getVisibility(field.getModifierList());
        String fieldType = field.getType().getPresentableText();

        if (!field.hasModifierProperty(PsiModifier.STATIC)) {
            plantUmlWriter.declareField(visibility, fieldType, field.getName());
        } else {
            plantUmlWriter.declareStaticField(visibility, fieldType, field.getName());
        }

    }



    @NotNull
    private String getVisibility(PsiModifierList methodModifiers) {
        return VisibilityUtil.getVisibilityModifier(methodModifiers);
    }


    private void declareClassMethods(PsiClass clazz) {
        for (PsiMethod method : clazz.getAllMethods()) {
            if (method.getContainingClass().equals(clazz)) {
                declareClassMethod(method);
            }
        }
    }



    private void declareClassMethod(PsiMethod method) {
        String visibility = getVisibility(method.getModifierList());

        if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            plantUmlWriter.declareAbstractMethod(visibility, method.getName());
        } else if (method.hasModifierProperty(PsiModifier.STATIC)) {
            plantUmlWriter.declareStaticMethod(visibility, method.getName());
        } else {
            plantUmlWriter.declareMethod(visibility, method.getName());
        }
    }



    private void declareClassRelationships(PsiClass clazz) {
        declareInterfaceImplementation(clazz);
        declareClassInheritence(clazz);
        declareClassAssociations(clazz);

        if (!hideInnerClasses) {
            declareInnerClassesAssociations(clazz);
        }
    }



    private void declareInterfaceImplementation(PsiClass clazz) {
        PsiReferenceList implementsList = clazz.getImplementsList();
        for (PsiClassType implementedInterface : implementsList.getReferencedTypes()) {
            plantUmlWriter.addClassesInheritence(clazz.getName(), implementedInterface.getName());
        }
    }



    private void declareClassInheritence(PsiClass clazz) {
        PsiClass superClass = clazz.getSuperClass();
        if (superClass != null && !superClassIsObject(superClass)) {
            plantUmlWriter.addClassesInheritence(clazz.getName(), superClass.getName());
        }
    }



    private boolean superClassIsObject(PsiClass superClass) {
        String superClassName = superClass.getName();
        String superClassPackage = ((PsiJavaFile) superClass.getContainingFile()).getPackageName();

        return superClassName.equals("Object") && superClassPackage.equals("java.lang");
    }



    private void declareClassAssociations(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (getFieldDisplayType(clazz, field) == FieldDisplayType.AGGREGATION) {
                plantUmlWriter.addClassesAssociation(clazz.getName(),
                                                     field.getType().getPresentableText(),
                                                     field.getName());
            }
        }
    }



    private void declareInnerClassesAssociations(PsiClass clazz) {
        for (PsiClass innerClass : clazz.getAllInnerClasses()) {
            plantUmlWriter.addInnerClassesAssociation(clazz.getName(), innerClass.getName());
        }

        for (PsiClass innerClass : clazz.getAllInnerClasses()) {
            declareClassRelationships(innerClass);
        }
    }



    private class StringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }

    }



    private class PsiClassComparator implements Comparator<PsiClass> {

        @Override
        public int compare(PsiClass class1, PsiClass class2) {
            String name1 = class1.getName();
            String name2 = class2.getName();

            return name1.compareTo(name2) ;
        }

    }



    enum FieldDisplayType {
        NONE,
        ATTRIBUTE,
        AGGREGATION
    }



    FieldDisplayType getFieldDisplayType(PsiClass clazz, PsiField field) {
        if (!field.getContainingClass().equals(clazz)) {
            return FieldDisplayType.NONE;
        }

        if (field.getContainingClass().equals(clazz) &&
                typeBelongsToCurrentProject(field.getType()) &&
                !typeContainsGeneric(field) &&
                !field.hasModifierProperty(PsiModifier.STATIC)) {
            return FieldDisplayType.AGGREGATION;
        }

        return FieldDisplayType.ATTRIBUTE;
    }



    private boolean typeContainsGeneric(PsiField field) {
        String presentableText = field.getType().getPresentableText();
        return presentableText.contains("<") || presentableText.contains(">");
    }



    private boolean typeBelongsToCurrentProject(PsiType type) {
        PsiClass typeClass = PsiTypesUtil.getPsiClass(type);
        if (typeClass == null) {
            return false;
        }


        return classBelongsToProject(typeClass);
    }



    private boolean classBelongsToProject(PsiClass clazz) {
        PsiFile classFile = clazz.getContainingFile();
        if (classFile == null) {
            return false;
        }

        return isBinaryFile(classFile);
    }



    private boolean isBinaryFile(PsiFile containingFile) {
        return containingFile.getFileType().isBinary() == false;
    }

}
