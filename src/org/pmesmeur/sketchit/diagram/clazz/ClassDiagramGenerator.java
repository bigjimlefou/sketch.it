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


public class ClassDiagramGenerator {
    private final PlantUmlWriter plantUmlWriter;
    private final Project project;
    private final Module module;
    private final Set<PsiClass> managedPsiClasses;
    private final List<String> patternsToExclude;
    private final List<String> packages;
    private final VirtualFile sourceDirectory;
    private final String title;


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


        public ClassDiagramGenerator build() {
            return new ClassDiagramGenerator(this);
        }

        public Builder sourceDirectory(VirtualFile sourceDirectory) {
            this.sourceDirectory = sourceDirectory;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }
    }



    protected ClassDiagramGenerator(Builder builder) {
        this.plantUmlWriter = builder.plantUmlWriter;
        this.project = builder.project;
        this.module = builder.module;
        this.patternsToExclude = builder.patternsToExclude;
        this.sourceDirectory = builder.sourceDirectory;
        this.title = builder.title;
        this.packages = new ArrayList<String>();
        this.managedPsiClasses = computeManagedPsiClasses();
    }



    private Set<PsiClass> computeManagedPsiClasses() {
        Finder finder = new Finder(project, module, patternsToExclude);

        Set<PsiClass> classes = finder.getClasses();
        if (classes.isEmpty()) {
            throw new NoSuchElementException("No classes found");
        }

        Set<String> packageSet = finder.getPackages();
        for (String packag : packageSet) {
            packages.add(packag);
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

        for (PsiClass clazz : managedPsiClasses) {
            declareClass(clazz);
        }


        for (PsiClass clazz : managedPsiClasses) {
            declareClassRelationships(clazz);
        }

        plantUmlWriter.endDiagram();
    }



    private void declareClass(PsiClass clazz) {
        String packageName = ((PsiJavaFile) clazz.getContainingFile()).getPackageName();

        List<String> packageStack = computePackageStack(packageName);


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
        declareClassAttributes(clazz);
        declareClassMethods(clazz);
    }



    private void declareClassAttributes(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (!typeBelongsToCurrentProject(field.getType()) &&
                    field.getContainingClass().equals(clazz)) {
                declareClassField(field);
            }
        }
    }



    private boolean typeBelongsToCurrentProject(PsiType type) {
        PsiClass typeClass = PsiTypesUtil.getPsiClass(type);
        return typeClass != null && typeClass.getProject().equals(project);
    }



    private void declareClassField(PsiField field) {
        String visibility = getVisibility(field.getModifierList());
        String fieldType = field.getType().getInternalCanonicalText();

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
        declareClassInheritence(clazz);
        declareClassAssociations(clazz);
    }



    private void declareClassInheritence(PsiClass clazz) {
        PsiClass superClass = clazz.getSuperClass();
        if (superClass != null) {
            plantUmlWriter.addClassesInheritence(clazz.getName(), superClass.getName());
        }
    }



    private void declareClassAssociations(PsiClass clazz) {
        for (PsiField field : clazz.getAllFields()) {
            if (typeBelongsToCurrentProject(field.getType()) &&
                    field.getContainingClass().equals(clazz) &&
                    !field.hasModifierProperty(PsiModifier.STATIC)) {
                plantUmlWriter.addClassesAssociation(clazz.getName(),
                                                     field.getType().getPresentableText(),
                                                     field.getName());
            }
        }
    }



    private class StringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return o1.length() - o2.length();
        }

    }

}
