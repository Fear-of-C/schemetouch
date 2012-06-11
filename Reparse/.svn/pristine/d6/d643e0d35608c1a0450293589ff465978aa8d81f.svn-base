package machination.webjava.trees.schemegrammar;

import java.util.Collections;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * Syntax for anything that does not actually match.  Used as a placeholder for parse errors.
 * 
 * @author nick
 *
 */
public class InvalidSyntax extends ChildedSyntax {
	
	public static InvalidSyntax singleton = new InvalidSyntax();

	public InvalidSyntax(){
		super();
		this.priority = -1;
	}
	
	@Override
	public String getName() {
		return "Error.";
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		return true;
	}

	@Override
	public Set<SchemeObject> getPossible(SchemeObject owner) {
		return Collections.singleton(owner.getFactory().holdSyntax(owner, null, this, null));
	}

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return true;
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		throw new UnsupportedOperationException("Can't set symbol for error.");
	}

}
