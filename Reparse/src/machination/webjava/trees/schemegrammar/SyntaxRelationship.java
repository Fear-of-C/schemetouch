package machination.webjava.trees.schemegrammar;

/**
 * 
 * @author nick
 *
 * Lightweight class designed to represent how 2 syntaxes relate.  Names the child by role and specifies
 * how many of that child should exist.
 * 
 * Need to rethink how this actually works - does it map to a possibility syntax?  A Scheme Syntax?
 * 
 * Everything that a childed list holds should be some kind of object in Scheme, but we need a way
 * for it to link into stuff like literals and digits (which are actually Scheme Objects).
 *
 */
public class SyntaxRelationship{

	public static final int INF = -1;
	public static final int DOT_TAIL = -2;
	
	protected SyntaxRelationship(){}
	
	public SyntaxRelationship(ChildedSyntax parent, PossibleSyntax child, String role,
			int num) {
		this.parent = parent;
		this.child = child;
		this.role = role;
		this.num = num;
	}
	
	public ChildedSyntax parent;
	public PossibleSyntax child;
	public String role;
	public int num;
	
	@Override
	public String toString(){
		return "SR " + parent + " - " + child + " role: " + role + " num: " + num;
	}
}

/*
 * Options for tracking scope across things:
 * 1) Allow the variables to specify scope as in macros, but to make up for the fact that grammars do not
 * have uniqueness, define a grammatical point where scope of a certain type stops searching and goes
 * down another branch.  This means we have a fixed "branch down" path.
 * 2) Keep putting things in syntax relationship.  Maybe we distinguish between uses, declarations, delscopes, etc.
 * 2.5) If most things don't create scope, it eases up our declscope propogation.
 * 3) Make our grammar definitions more like macro definitions.  Specify which variables make and take scope
 * and then have the tree get it exactly - might be the best way.  We could actually force the grammar
 * to specify all 3 points, or the grammar parser could look up.
 * 4) Keep everything the way that it is, but drill down later.
 */
