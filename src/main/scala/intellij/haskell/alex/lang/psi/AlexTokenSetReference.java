package intellij.haskell.alex.lang.psi;

import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Using Java because IntelliJ's Scala plugin does not work well with
 * stream API
 *
 * @author ice1000
 */
public class AlexTokenSetReference extends PsiPolyVariantReferenceBase<AlexTokenSetId> {
	public AlexTokenSetReference(@NotNull AlexTokenSetId element) {
		super(element);
	}

	@NotNull
	@Override
	public PsiElementResolveResult[] multiResolve(boolean b) {
		AlexDeclarationsSection declarations = getAlexDeclarationsSection();
		if (declarations == null) return new PsiElementResolveResult[0];
		return declarations.getDeclarationList()
				.stream()
				.map(AlexDeclaration::getTokenSetDeclaration)
				.filter(Objects::nonNull)
				.filter(declaration -> declaration.getTokenSetId().getText().equals(getElement().getText()))
				.map(PsiElementResolveResult::new)
				.toArray(PsiElementResolveResult[]::new);
	}

	@Nullable
	private AlexDeclarationsSection getAlexDeclarationsSection() {
		PsiFile file = getElement().getContainingFile();
		if (file == null) return null;
		AlexDeclarationsSection declarations = PsiTreeUtil.findChildOfType(file, AlexDeclarationsSection.class);
		if (declarations == null) return null;
		return declarations;
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		AlexDeclarationsSection declarations = getAlexDeclarationsSection();
		if (declarations == null) return EMPTY_ARRAY;
		return declarations.getDeclarationList()
				.stream()
				.map(AlexDeclaration::getTokenSetDeclaration)
				.filter(Objects::nonNull)
				.toArray();
	}
}
