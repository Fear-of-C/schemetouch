package machination.webjava.trees.schemegrammar;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * References the use of an "use" keyword in the grammar.
 * @author nick
 *
 */
public class DefiniteSyntax extends SchemeSyntax implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4394270089859111505L;
	private String definedAs;
	private ChildedSyntax parent;
	
	protected DefiniteSyntax(){}
	
	public DefiniteSyntax(String definedAs, ChildedSyntax parent){
		this.definedAs = definedAs;
		this.parent = parent;
	}
	
	@Override
	public Set<SchemeSymbol> getPossible(SchemeObject owner) {
		List<SchemeEnv.VariableDeclaration> vd = parent.getVarDecl(owner.getParent(), definedAs);
		//System.out.println(vd);
		Set<SchemeSymbol> possible = new HashSet<SchemeSymbol>(vd.size());
		for(SchemeEnv.VariableDeclaration v : vd){
			possible.add(v.decl);
		}
		return possible;
	}

	/**
	 * This is a weird idea - take the object in question, decide what is possible for it,
	 * and then return whether or not it matches its own possibility list.
	 */
	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		for(SchemeObject p : getPossible(toTry)){
			if(p.getString().equals(toTry.getString())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String getName() {
		return definedAs;
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		//TODO: figure out if this really works
		return o instanceof SchemeSymbol;
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		// TODO Auto-generated method stub
		
	}

}
