package machination.webjava.trees.schemegrammar;

import java.util.LinkedHashSet;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * This does NOT current match procedures - that's why we have the breakdown of expressions.
 * 
 * Procedure may eventually need to match like a DynamicSyntax, especially if we want any
 * kind of argument checking.
 * 
 * @author nick
 *
 */
public class Identifier extends SchemeSyntax{
	
	private String name;
	
	public String getName() {
		return name;
	}

	public Identifier(String name) {
		this.name = name;
	}

	public LinkedHashSet<SchemeSymbol> getPossible(SchemeObject owner) {
		//System.out.println("listing environment " + owner.getEnvironment());
		LinkedHashSet<SchemeSymbol> id = new LinkedHashSet<SchemeSymbol>();
		SchemeEnv current = owner.getEnvironment();
		while(current != null){
			id.addAll(current.enumerateIdentifiers());
			current = current.getParent();
		}
		return id;
	}
	
	/*@Override
	public boolean match(SchemeObject toTry){
		boolean result = super.match(toTry);
		if(result){
			toTry.getEnvironment().registerUse((SchemeSymbol) toTry);
		}
		return result;
	}*/

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		//System.out.println("matching " + toTry + " with identifier " + this + " result " + (toTry.isSymbol() && toTry.getEnvironment().isDefined((SchemeSymbol) toTry) &&
		//		(toTry.getFactory().getSyntax(toTry.getString()) == null)));
		return toTry.isSymbol() && toTry.getEnvironment().isDefined((SchemeSymbol) toTry) &&
			(toTry.getFactory().getSyntax(toTry.getString()) == null);
	}

	/**
	 * Does not use environment - only determines whether the thing mentioned
	 * could become an identifier (it should be a symbol).
	 */
	@Override
	public boolean matchStructure(SchemeObject o) {
		return (o instanceof SchemeSymbol) && (o.getFactory().getSyntax(o.getString()) == null);
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		// TODO Auto-generated method stub
		
	}
	
}
