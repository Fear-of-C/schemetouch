package machination.webjava.trees.schemegrammar;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * Defines a syntax that comes from the environment.  This means that we match any defined syntax
 * object.  Problem comes from trying to match childed syntax with this as 1st element - it's
 * not really supposed to do THAT.  This should really be part of the possibilitySyntax hierarchy?
 * It is able to match virtually anything, so it should be.  It will then reference all of the
 * dynamically generated syntaxes in the environment, plus all of the named syntaxes in the grammar.
 * 
 * This should pretty much only contain children, since the environment should only have
 * PossibilitySyntax objects available.  This means we can factor out code into the abstract
 * PossibleSyntax class, which calls the abstract getChildren() and getPossible() methods.
 * 
 * The hardest part about using this is its utter inability to handle situations in which the environment
 * is not defined.  The 2nd hardest part is that it is probably not re-usable across positions in the tree,
 * since PossibilitySyntax were not designed to be callable via SchemeObject.  We could either a) clone it
 * or b) use it atomically.
 * 
 * We could attempt to add some factory functionality OR to make getChildren depend on the SchemeObject
 * being passed in.  The environment could work like some kind of weird locking mechanism, which
 * would make sense if it must be sequentialized anyway.  In most cases, it must.
 * 
 * Note that this class absolutely cannot match without the environment being configured.
 * With no environment, it matches NOTHING AT ALL, structurally or not.
 * 
 * Do we need placeholders to make a DynamicSyntax able to match structurally without
 * having environment?  In theory, environment should be configured by top level when
 * we hit this, so we should be alright - it is a violation to allow the top environment
 * to depend on its internals.
 * 
 * Seriously consider whether we can port things like this into Scala.
 * 
 * @author nick
 *
 */
public class DynamicSyntax extends PossibleSyntax{

	@Override
	public Set<SchemeSyntax> getPossible(SchemeObject position) {
		return Collections.emptySet();
	}

	@Override
	public boolean match(SchemeObject toTry) {
		//does NOT get to use structural data as limiting!
		//also may not have specified orderings
		for(PossibleSyntax child : getChildren(toTry)){
			if(child.match(toTry)){
				return true;
			}
		}
		return false;
	}

	//TODO: somehow implement NAMED search on the returned set in order to epically speed things up
	@Override
	public Set<PossibleSyntax> getChildren(SchemeObject context) {
		if(context == null){
			return Collections.emptySet();
		}
		Set<PossibleSyntax> children = new LinkedHashSet<PossibleSyntax>();
		SchemeEnv current = context.getEnvironment();
		//System.out.println("Environments in dynamic trying to match " + context.getString() + ":");
		do{
			Set<SchemeSymbol> syms = current.enumerateSyntax();
			//System.out.print(current +" ");
			for(SchemeSymbol s : syms){
				//System.out.print(s.getString() + ", ");
				children.add(current.findSyntax(s));
				if(current.findSyntax(s) == null){
					throw new IllegalStateException("null added to dynamic syntax: " + s.getString());
				}
			}
			//System.out.print(" _  ");
		}while((current = current.getParent()) != null);
		//System.out.println();
		//System.out.println("defined-syntaxes: " + children + " <- " + context.getEnvironment());
		return children;
	}

	@Override
	public String getName() {
		return "defined-syntax";
	}	
}
