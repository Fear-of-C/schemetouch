package machination.webjava.trees.schemegrammar;

import java.util.Collections;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

public class PredefSyntax extends SchemeSyntax {

	private String name;
	
	protected PredefSyntax(){}
	
	public PredefSyntax(String name){
		this.name = name;
	}
	
	@Override
	public Set<? extends SchemeObject> getPossible(SchemeObject owner) {
		return Collections.singleton(owner.getFactory().makeLiteral(this));
	}

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return matchStructure(toTry);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Structure is everything!
	 */
	@Override
	public boolean matchStructure(SchemeObject o) {
		return o.getString().equals(name);
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		// TODO Auto-generated method stub
		
	}

}
