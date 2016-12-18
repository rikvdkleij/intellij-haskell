package intellij.haskell.util.index;

import com.intellij.openapi.project.ProjectManager;
import intellij.haskell.HaskellFileType;
import intellij.haskell.HaskellFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import intellij.haskell.HaskellNotificationGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HaskellModuleIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> HASKELL_MODULE_INDEX = ID.create("HaskellModuleIndex");
    private static final int INDEX_VERSION = 0;
    private static final EnumeratorStringDescriptor KEY_DESCRIPTOR = new EnumeratorStringDescriptor();
    private static final MyDataIndexer INDEXER = new MyDataIndexer();
    public static final FileBasedIndex.InputFilter HASKELL_MODULE_FILTER = new FileBasedIndex.InputFilter() {
        @Override
        public boolean acceptInput(@NotNull VirtualFile file) {
            //noinspection ObjectEquality
            boolean b1 = file.getFileType() == HaskellFileType.INSTANCE();
            boolean b2 = file.isInLocalFileSystem();
            return b1 && b2;
            // to avoid renaming modules that are somewhere in a lib folder
            // and added as a library. Can get nasty otherwise.
        }
    };

    @NotNull
    public static List<HaskellFile> getFilesByModuleName(@NotNull Project project, @NotNull String moduleName, @NotNull GlobalSearchScope searchScope) {
        final PsiManager psiManager = PsiManager.getInstance(project);
        Collection<VirtualFile> virtualFiles = getVirtualFilesByModuleName(moduleName, searchScope);
        return ContainerUtil.mapNotNull(virtualFiles, new Function<VirtualFile, HaskellFile>() {
            @Override
            public HaskellFile fun(VirtualFile virtualFile) {
                final PsiFile psiFile = psiManager.findFile(virtualFile);
                return psiFile instanceof HaskellFile ? (HaskellFile)psiFile : null;
            }
        });
    }

    @NotNull
    public static Collection<VirtualFile> getVirtualFilesByModuleName(@NotNull String moduleName, @NotNull GlobalSearchScope searchScope) {
        return FileBasedIndex.getInstance().getContainingFiles(HASKELL_MODULE_INDEX, moduleName, searchScope);
    }

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return HASKELL_MODULE_INDEX;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return INDEXER;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return KEY_DESCRIPTOR;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return HASKELL_MODULE_FILTER;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return INDEX_VERSION;
    }

    private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
        @NotNull
        @Override
        public Map<String, Void> map(@NotNull FileContent inputData) {
            final PsiFile psiFile = inputData.getPsiFile();
            final String moduleName = psiFile instanceof HaskellFile
                    ? ((HaskellFile) psiFile).getModuleName().get()
                    : null;
            HaskellNotificationGroup.logWarningEvent(ProjectManager.getInstance().getDefaultProject(), "module index !!!!!!!!!!!!!!!!!!!!!" + moduleName);
            if (moduleName == null) { return Collections.emptyMap(); }
            return Collections.singletonMap(moduleName, null);
        }
    }
}
