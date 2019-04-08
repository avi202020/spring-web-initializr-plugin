package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiJavaFile;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringInitializrCodeGenerator;

public class RepositoryGenerator extends SpringInitializrCodeGenerator implements TemplateReader {

    private static final String RESOURCE_REPOSITORY_NAME_TEMPLATE = "%sResourceRepository";
    private static final String REPOSITORY_ANNOTATION_QN = "org.springframework.stereotype.Repository";

    private String packagePath;

    public RepositoryGenerator(PsiClass psiClass, String packagePath) {
        super(psiClass, psiClass.getProject());
        this.packagePath = packagePath;

    }

    @Override
    public PsiClass generate() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(packagePath.replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createRepositoryInterface(pdPackage);
    }

    private PsiClass createRepositoryInterface(PsiDirectory psiDirectory) {
        String resourceRepositoryName = String.format(RESOURCE_REPOSITORY_NAME_TEMPLATE, psiClass.getName());
        PsiJavaFile resourceRepositoryFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceRepositoryName, this.packagePath);

        PsiClass resourceRepository = getElementFactory().createInterface(resourceRepositoryName);

        addQualifiedAnnotationNameTo(REPOSITORY_ANNOTATION_QN, resourceRepository);

        String crudRepositoryQn = String.format("org.springframework.data.repository.CrudRepository<%s,%s>", psiClass.getQualifiedName(), extractResourceIdQualifiedName());
        addQualifiedExtendsToClass(crudRepositoryQn, resourceRepository);

        getJavaCodeStyleManager().shortenClassReferences(resourceRepository);
        resourceRepositoryFile.add(resourceRepository);
        return resourceRepository;
    }


}