package machination.webjava.trees.scheme;

import java.util.List;

import machination.webjava.trees.schemegrammar.ChildedSyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.PredefSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;



/**
 * This class is responsible for 
 * 1) generating empty lists for the Scheme pattern
 * 2) Converting raw Scheme into Scheme Objects (whether or not there's anything to do at this stage).
 * 3) Mapping Scheme Objects to headers.
 * 4) Handling the Objects?  This might make it easier to wrap existing Scheme systems w/out duplicating Objects.
 * -also removes the doubt about generics and SchemeObjects
 * -doesn't add extra instanceof checks unless we have a persistence engine anyway
 * 5) Tracks headers and environment
 * -this may include macro expansion
 * 
 * It would be REALLY REALLY COOL if we could do something of a SchemeOrder traversal, plus it would
 * solve a whole lot of problems with iterating through the tree.
 * 
 * This class can use common headers for multiple uses of the same syntactic constructs.  This saves a ton of Object
 * creation.
 * 
 * This class should hold a grammar.  Those syntactic constructs with names will go into the base environment.
 * Those without will be findable only from hardcode/plugins.
 * 
 * @author nick
 *
 */
public abstract class SchemeFactory{
	
	public abstract SchemeObject fromObject(SchemeObject parent, Object body, SchemeEnv ke);

	public abstract boolean isNil(SchemeObject obj);
	
	public abstract SchemeEnv getEnv();

	public SchemeEnv subEnvironment(SchemeEnv env){
		return new DefaultSchemeEnv(env);
	}

	public abstract SchemeObject lowerEnvironmentSymbol(SchemeObject parent,
			SchemeSymbol origin);

	public boolean matchesLiteral(SchemeObject o, String string) {
		return o.getString().equals(string);
	}
	
	public abstract SchemeObject makeLiteral(PredefSyntax literalSyntax);

	public abstract SchemeList makeEmptyList(SchemeObject parent);
	
	/**
	 * This should only happen by hardcode - the code-as-data part of this
	 * program should know about symbols.
	 * 
	 * @param string
	 * @return
	 */
	public abstract PossibleSyntax getSyntax(String string);
	
	public SchemeObject holdSyntax(SchemeObject owner,
			List<PossibleSyntax> pHold, SchemeSyntax sHold, SyntaxRelationship role) {
		SchemeObject placeHolder;
		if(((pHold!= null) && (pHold.get(pHold.size() - 1).equals(getSyntax("newSymbol")) ||
				pHold.get(pHold.size() - 1).equals(getSyntax("identifier")))) ||
				((sHold != null) && sHold.equals(getSyntax("newSymbol").getSingleton())) ||
				((role != null) && getSyntax("newSymbol").equals(role.child))){
			placeHolder = new SchemeSymbol(null, this, owner.getEnvironment());
		}else if((role!= null) && (role.child.getSingleton() instanceof ChildedSyntax)){
			placeHolder = role.child.getSingleton().getPossible(owner).iterator().next();
		}
		else{
			placeHolder = new SchemeObject(null, this, owner.getEnvironment());
		}
		//System.out.println("placedHolder for " + pHold + ":" + sHold + " is a " + placeHolder.getClass());
		placeHolder.setRole(role);
		if(pHold != null){
			placeHolder.configureSyntax(pHold);
		}
		placeHolder.startMatch().matchOriginal = sHold;
		return placeHolder;
	}

	public abstract SchemeObject cloneObject(SchemeObject parent, SchemeObject o,
			SchemeEnv environment);

	public abstract SchemeObject fromString(SchemeObject owner, String literal);

	
}









