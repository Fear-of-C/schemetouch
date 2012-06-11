package machination.webjava.trees.scheme;

import java.util.ArrayList;

import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;


/**
 * Represents a Scheme "body."  This is not a normal SchemeObject, as it does not hold a parent,
 * but it does hold an environment.
 * 
 * @author nick
 *
 */
public class SchemeBody extends SchemeObject{

	private ArrayList<SchemeObject> statements;
	
	public static final String NO_PARENT = 
		"This is the body object.  It does not exist normally in the tree.";
	
	public SchemeBody(SchemeFactory factory) {
		super(null, factory, factory.getEnv());
		statements = new ArrayList<SchemeObject>();
		PossibleSyntax ps = factory.getSyntax("body");
		setBottomSyntax(ps);
	}

	@Override
	public void addSyntax(PossibleSyntax ps) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public ArrayList<SchemeObject> getDatum() {
		return statements;
	}

	@Override
	public SyntaxMatched getMatch() {
		// TODO Auto-generated method stub
		return super.getMatch();
	}

	@Override
	public void registerReplaceListener(ReplaceListener r) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public void removeReplaceListener(ReplaceListener r) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public void replace(SchemeObject next) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public boolean schemeEquals(SchemeObject other) {
		// TODO Auto-generated method stub
		return super.schemeEquals(other);
	}

	@Override
	public void setBottomSyntax(PossibleSyntax p) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public void setEnvironment(SchemeEnv environment) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public void setParent(SchemeObject parent) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	@Override
	public void setRole(SyntaxRelationship role) {
		throw new UnsupportedOperationException(NO_PARENT);
	}

	
}
