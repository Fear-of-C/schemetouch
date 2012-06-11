package machination.webjava.trees.schemegrammar;

import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;

/**
 * T is the type of object held in this syntax.  For example, we
 * could instruct the syntax to return a set of possible characters.
 * 
 * 
 * Could assume that possibility metasyntaxes are metasyntax<metasyntax<?>>
 * This would imply that the possibilities are themselves syntaxes
 * (rather than SchemeObjects or Characters) and would appear to
 * de-awkwardify quite a bit.
 * 
 * This appears rather brilliant at the moment.  The only problem is some
 * awkwardness with SyntaxMatched objects no longer existing.  Maybe we
 * need some kind of split between things that deal in SchemeObject(s)
 * and things that don't.
 * 
 * @author nick
 *
 * @param <T>
 */
public interface MetaSyntax<T>{
	
	/**
	 * 
	 * @param toTry
	 * @return whether this syntax matches the object of the given type.
	 */
	public boolean match(T toTry);
	
	public String getName();
}
