package machination.webjava.trees.schemegrammar.generated;

import java.util.List;
import java.util.Map;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;

public interface Template {

	/**
	 * Decides whether to emit or replace.
	 * 
	 * @param parent - the parent in the newly constructed replacement
	 * @param matched - a map of pattern var name to 
	 * @return a SchemeObject ready for insertion in place of this template var
	 */
	public SchemeObject transform(SchemeObject parent, Map<String, List<SchemeObject>> matched, int position);
	
	public Map<String, List<TemplateVar>> getVariables();
				
}
