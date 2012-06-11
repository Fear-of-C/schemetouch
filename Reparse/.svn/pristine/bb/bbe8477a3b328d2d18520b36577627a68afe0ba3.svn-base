package machination.webjava.trees.scheme;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import machination.webjava.trees.schemegrammar.PossibleSyntax;



/**
 * This will be so much cooler when we get environmental (as well as tree-wise)
 * replacements to work.  Essentially causes auto-refactoring to just happen.
 * 
 * Have a design decision
 * a) We could keep the current notion of passing around symbols for potential
 * things.
 * pros
 * -Already (mostly) implemented
 * -uses a robust class that is capable of recognizing hygeine
 * -more intuitive handling of "open" objects
 * cons
 * -requires symbol generation overhead at every eumeration
 * -requires toString/getString call implicit in 
 * -might not make sense to define treed Scheme symbols outside of their enviroment
 * --creates match ambiguity when a specified symbol is passed in
 * b) We could switch all environmental function to work in strings.
 * pros
 * -simpler
 * -faster
 * -allows client code to decide when to make symbol objects
 * -allows easier mixing/separation of identifiers with syntax
 * cons
 * -requires other classes to look things up using strings
 * -how do we deal with open objects?
 * -how do we deal with hygeine?
 * -
 * 
 * Let's try hybrid!!!
 * -we definitely need to check if strings exist in the environment
 * -we can also check objects, first by checking if they are open,
 * and then by 
 * -in the previous case, open objects somehow must link to their
 * original definer
 * -might have some problem with the way that SchemeSyntax is set
 * up to return possible Scheme Objects.  This is probably an
 * optimization issue to work out later.
 * -we need to return prototype objects.  This is for compatibility
 * with the childed schemesyntax which returns a singleton of its
 * list structure.
 * -these prototypes will have an environment set but not respond to
 * a parent.  that is probably completely fine.  The factory clones
 * them into parented objects when requested.
 * -this dictates a custom equality metric on Scheme Objects that
 * ignores position in tree but could still use environment
 * and definition point
 * 
 * A symbol is 1 of 4 things:
 * 1) an identifier
 * 2) a syntactic symbol
 * 3) a placeholder for something that will be defined
 * 4) a hygenic symbol that inserts a generated symbol
 * 
 * @author nick
 *
 */
public interface SchemeEnv extends ReplaceListener{
	
	/**
	 * Associates a variable's delclaration and definition objects.
	 * 
	 * @author nick
	 *
	 */
	public class VariableDeclaration{
		
		public VariableDeclaration(SchemeSymbol decl, SchemeObject def) {
			super();
			this.decl = decl;
			this.def = def;
		}
		public SchemeSymbol decl;
		public SchemeObject def;
		
		public String toString(){
			return "VD " + (decl == null ? null : decl.getString()) + " -- " + 
				(def == null ? null : def.getString());
		}
	}
	
	/**
	 * Checks whether the object in question maps to a kind of definition.
	 * It may have a definition, or it may be one of those "open" objects.
	 * 
	 * @param o
	 * @return
	 */
	public boolean isDefined(SchemeSymbol sym);
	
	public boolean includes(SchemeEnv other);
	
	/**
	 * @param o
	 * @return the environment that bound the object, 
	 */
	public SchemeEnv bindingEnvironment(SchemeSymbol o);
	
	/**
	 * 
	 * @param sym
	 * @return the declaration of sym
	 */
	public VariableDeclaration get(SchemeSymbol sym);
	
	/**
	 * This should be a symbol that can take a defined syntax.
	 * 
	 * This could really be anything.  It's up to the environment
	 * to know that something is a procedure and not a defined-syntax.
	 * 
	 * @param o
	 * @return
	 */
	public PossibleSyntax findSyntax(SchemeSymbol sym);
	
	/**
	 * 
	 * @param sym
	 * @return a list of use locations for name
	 */
	public SortedSet<SchemeObject> getAllUses(SchemeSymbol name);
	
	/**
	 * Adds a symbol to the environment with a defining object.
	 * 
	 * May add a raw (non-symbol) object.  We could exploit this to allow
	 * some kind of variable folding/unfolding.
	 */
	public void define(SchemeSymbol sym, SchemeObject definition);
	
	/**
	 * Similar to above method, but takes an associated pair.
	 * @param vd
	 */
	public void put(VariableDeclaration vd);
	
	public void putAll(Collection<VariableDeclaration> vds);

	/**
	 * Warning: might return a very long set or be slow.
	 * 
	 * This method has some issues, because it's returning SchemeSymbols
	 * that correspond to a non-existent parent.  It could, for instance,
	 * keep "prototypes" of all symbols, and then ask the factory to
	 * clone them.  This is in fact likely to be the best way to go -
	 * the UI only has to clone one of them, and it already knows
	 * what kind of syntax the symbol obeys (identifier or defined-syntax).
	 * 
	 * @return
	 */
	public abstract Set<SchemeSymbol> enumerateSymbols();
	
	public abstract Set<SchemeSymbol> enumerateIdentifiers();
	
	public abstract Set<SchemeSymbol> enumerateSyntax();
	
	/**
	 * Enumerates method signatures.
	 * 
	 * @return a mapping of procedure name to a possibleSyntax representing it.
	 * This is primarily significant when we have procedures that contain varargs and procedures
	 * that may have multiple definitions.
	 */
	public abstract Map<String, PossibleSyntax> enumerateProcedures();
	
	/**
	 * 
	 * @return all variables defined in child and not in this
	 */
	public abstract Set<SchemeSymbol> getDifference(SchemeEnv child);

	public SchemeEnv getParent();
	
	public void registerUse(SchemeSymbol use);
	
	public void deleteUse(SchemeSymbol use);
	
	public Set<SchemeSymbol> enumerateAllSyntax();
	
	/**
	 * Adds a new macro to the environment.
	 * 
	 * @param sym
	 * @param p
	 */
	void putSyntax(SchemeSymbol sym, PossibleSyntax p);
	
	public void finishSyntax();

	public PossibleSyntax findSyntax(String string);
	
	public PossibleSyntax findProcedure(String s);
	
	/**
	 * Accelerates matching by searching procedures in a logarithmic or O(1) way.  Idea is that if something matches a library
	 * procedure, we don't bother matching it with everything else (including all other library procedures).
	 * 
	 * Furthermore, if this fails, then we SKIP all library functions and go to user space.
	 * 
	 * Identifiers already have fast lookup, so no need to run this on them.
	 * 
	 * @param name
	 * @return
	 */
	public PossibleSyntax namedSyntaxLookup(String name);
	
}
