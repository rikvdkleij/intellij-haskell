package com.powertuple.intellij.haskell;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.powertuple.intellij.haskell.psi.HaskellFile;
import com.powertuple.intellij.haskell.psi.HaskellVarid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HaskellUtil {
    public static List<HaskellVarid> findProperties(Project project, String key) {
        List<HaskellVarid> result = null;
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, HaskellFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            HaskellFile simpleFile = (HaskellFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                HaskellVarid[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, HaskellVarid.class);
                if (properties != null) {
                    for (HaskellVarid property : properties) {
                        if (key.equals(property.getName())) {
                            if (result == null) {
                                result = new ArrayList<HaskellVarid>();
                            }
                            result.add(property);
                        }
                    }
                }
            }
        }
        return result != null ? result : Collections.<HaskellVarid>emptyList();
    }

    public static List<HaskellVarid> findProperties(Project project) {
        List<HaskellVarid> result = new ArrayList<HaskellVarid>();
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, HaskellFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            HaskellFile simpleFile = (HaskellFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (simpleFile != null) {
                HaskellVarid[] properties = PsiTreeUtil.getChildrenOfType(simpleFile, HaskellVarid.class);
                if (properties != null) {
                    Collections.addAll(result, properties);
                }
            }
        }
        return result;
    }
}
