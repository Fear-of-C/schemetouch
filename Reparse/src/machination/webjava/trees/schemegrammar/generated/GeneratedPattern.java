package machination.webjava.trees.schemegrammar.generated;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.ChildedSyntax;
import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.PredefSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;

/**
 * 
 * @author nick
 *
 */
public class GeneratedPattern extends ChildedSyntax{
	
	/**
	 * The Object from which we generated this pattern.
	 */
	private SchemeObject patternObj;
	
	/**
	 * Stores a set of all pattern variables.
	 */
	private HashSet<PatternVar> patternVars;
	
	public HashSet<PatternVar> getPatternVars() {
		return patternVars;
	}

	public GeneratedPattern(SchemeSymbol name, SchemeObject patternObj, Set<String> literals){
		super(name.getString()+ ": " + patternObj.getString());
		this.patternObj = patternObj;
		this.patternVars = new HashSet<PatternVar>();
		//System.out.println("Generating pattern " + patternObj.getString());
		if(patternObj.isList()){
			SchemeList patternList = (SchemeList) patternObj;
			if(name.getString().equals(patternList.first().getString())){
				//so in reality, we are creating a named syntax pattern
				//which should also raise the priority!
				setSymbol((SchemeSymbol) patternList.first());
				patternList = patternList.rest(1);
			}
			//if the pattern is just a name, this will remain an empty list
			//for instance, (and) becomes #t
			setChildren(new ArrayList<SyntaxRelationship>(patternList.size()));
			for(int i = 0; i < patternList.size(); i++){
				SchemeObject o = patternList.get(i);
				if(patternObj.getFactory().matchesLiteral(o, "_") || o.getString().equals(name.getString())){
					o = name;
				}else if(patternObj.getFactory().matchesLiteral(o, "...")){
					getChildren().get(i - 1).num = -1;
					continue;
				}
				//System.out.println("Adding pattern child " + o.getString());
				addChild(new SyntaxRelationship(
						this, getSyntaxFromPattern(name, o, literals), o.getString(), 1));
			}
			setDot(((ChildedSyntax)patternObj.getSyntax()).getDot());
			
		}else{
			throw new UnsupportedOperationException("Pattern must be list");
		}
		setPriority(1);
		//System.out.println("Generated Pattern " + getPriority() + " - " + this + patternObj.getString());
	}
	
	/**
	 * Takes no args but assumes that all variables have been set up by a scope propagation.
	 * 
	 * This should go recursive and get scope requests from children.
	 */
	public void configureScopes(){
		for(SyntaxRelationship childRel : getChildren()){
			PossibleSyntax ps = childRel.child;
			if(ps instanceof PatternVar){
				PatternVar pVar = (PatternVar) ps;
				//every pattern var knows what other pattern vars are in its scope
				//it could also have patterns in its scope!  or could it?
				//not as identifiers - only as definitions
				//and sometimes variables will define as other vars
				//let's figure out how we might work around this for now...
				/*the other thing to deal with is that we don't have declarations
				 *we could have another method propagate declarations around
				 *maybe it uses a Map returned by this
				 * 
				 * what happens when a definition is an expression of vars?
				 * especially if they have been recombined st. it doesn't
				 * match to anything that exists in the pattern?
				 * then we may need to introduce phantom definitions
				 */
				
			}
		}
	}

	public PossibleSyntax getSyntaxFromPattern(SchemeSymbol name, SchemeObject po, Set<String> literals){
		if(literals.contains(po.getString())){
			return new PossibilitySyntax(new PredefSyntax(po.getString()));
		}
		if(name.getString().equals(po.getString())){
			return new PossibilitySyntax(new PredefSyntax(name.getString()));
		}
		if(po.isList() || po.isVector()){
			GeneratedPattern p = new  GeneratedPattern(name, po, literals);
			patternVars.addAll(p.patternVars);
			return new PossibilitySyntax(p);
		}		
		if(po.isSymbol()){
			PatternVar p = new PatternVar((SchemeSymbol) po,this);
			this.patternVars.add(p);
			return p;
		}
		
		return null;
	}
}
