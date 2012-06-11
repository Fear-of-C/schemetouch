package machination.webjava.trees.schemegrammar.generated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;

public class TemplateLiteral extends SchemeSyntax implements Template{

	private SchemeObject literalObject;
	private SyntaxRelationship rel;
	
	public TemplateLiteral(SchemeObject lit){
		this.literalObject = lit;
	}
	
	@Override
	public Map<String, List<TemplateVar>> getVariables() {
		return Collections.emptyMap();
	}

	@Override
	public SchemeObject transform(SchemeObject parent,
			Map<String, List<SchemeObject>> matched, int position) {
		return parent.getFactory().cloneObject(parent, literalObject, literalObject.getEnvironment());
	}

	@Override
	public Set<? extends SchemeObject> getPossible(SchemeObject owner) {
		return Collections.singleton(owner.getFactory().cloneObject(owner.getParent(), literalObject, literalObject.getEnvironment()));
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		return o.getString().equals(literalObject.getString());
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return literalObject.getString();
	}

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return matchStructure(toTry);
	}

}
