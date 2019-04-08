package ore.plugins.idea.spring.web.initializr.generator;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import ore.plugins.idea.base.functionality.TemplateReader;
import ore.plugins.idea.spring.web.initializr.generator.base.SpringInitializrCodeGenerator;
import ore.plugins.idea.utils.FormatUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ServiceGenerator extends SpringInitializrCodeGenerator implements TemplateReader {

    private static final String RESOURCE_SERVICE_NAME_TEMPLATE = "%sResourceService";
    private static final String SERVICE_ANNOTATION_QN = "org.springframework.stereotype.Service";

    private String packagePath;
    private PsiClass resourceRepository;

    public ServiceGenerator(PsiClass psiClass, String packagePath, PsiClass resourceRepository) {
        super(psiClass, psiClass.getProject());
        this.packagePath = packagePath;
        this.resourceRepository = resourceRepository;
    }


    @Override
    public PsiClass generate() {
        String fullPackagePath = getProjectRootManager().getContentRoots()[0].getPath().concat(DEFAULT_JAVA_SRC_PATH).concat(packagePath.replaceAll("\\.", "/"));
        VirtualFile vfPackage = createFolderIfNotExists(fullPackagePath);
        PsiDirectory pdPackage = getPsiManager().findDirectory(vfPackage);
        return createResourceService(pdPackage);
    }

    private PsiClass createResourceService(PsiDirectory psiDirectory) {
        String resourceServiceName = String.format(RESOURCE_SERVICE_NAME_TEMPLATE, psiClass.getName());
        PsiJavaFile resourceServiceFile = createJavaFileInDirectoryWithPackage(psiDirectory, resourceServiceName, packagePath);

        PsiClass resourceService = getElementFactory().createClass(resourceServiceName);
        addQualifiedAnnotationNameTo(SERVICE_ANNOTATION_QN, resourceService);

        String resourceServiceQualifiedName = String.format("spring.web.initializr.base.service.ResourceService<%s,%s,%s>", psiClass.getQualifiedName(), psiClass.getQualifiedName(), extractResourceIdQualifiedName());
        addQualifiedExtendsToClass(resourceServiceQualifiedName, resourceService);

        PsiField resourceRepositoryElement = getElementFactory().createField(FormatUtils.toFirstLetterLowerCase(Objects.requireNonNull(resourceRepository.getName())), getElementFactory().createType(resourceRepository));
        PsiUtil.setModifierProperty(resourceRepositoryElement, PsiModifier.PRIVATE, true);
        PsiUtil.setModifierProperty(resourceRepositoryElement, PsiModifier.FINAL, true);

        List<PsiField> constructorArguments = Collections.singletonList(resourceRepositoryElement);

        PsiMethod constructor = extractConstructorForClass(resourceService, constructorArguments, constructorArguments, Collections.singletonList(resourceRepositoryElement.getNameIdentifier().getText()));
        PsiUtil.setModifierProperty(constructor, PsiModifier.PUBLIC, true);
        addAutowiredTo(constructor);
        resourceService.add(constructor);

        getJavaCodeStyleManager().shortenClassReferences(resourceService);
        resourceServiceFile.add(resourceService);

        return resourceService;
    }


}