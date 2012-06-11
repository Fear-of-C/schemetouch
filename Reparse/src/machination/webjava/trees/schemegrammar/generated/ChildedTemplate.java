package machination.webjava.trees.schemegrammar.generated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;
import machination.webjava.trees.schemegrammar.ChildedSyntax;
import machination.webjava.trees.schemegrammar.PossibilitySyntax;
import machination.webjava.trees.schemegrammar.PossibleSyntax;
import machination.webjava.trees.schemegrammar.SchemeSyntax;
import machination.webjava.trees.schemegrammar.SyntaxMatched;
import machination.webjava.trees.schemegrammar.SyntaxRelationship;

/**
 * Needs to hold symbols and map scope.
 * 
 * Scope rules:
 * 1) Pattern variables or other symbols might repeat here.  If we had some kind of uniqueness
 * to child naming, then we could put declarations and declscopes atop the entire tree and
 * just ignore where they actually appear.
 * 2) Scope requirements can remain per-variable, assuming that they are allowed to run up the
 * parent tree.
 * 
 * This is interesting - we have a scoping system that in some ways mirrors the name conflict
 * resolution system.  Child link names must be unique in the pattern, while in other syntax,
 * it's often legal to shadow such things.
 * 
 * The parsing of the macro already takes care of validating the scope at template declaration
 * time.  The expansion of the macro needs to propogate meanings of symbols back up to the pattern.
 * For example, it should recognize that cond clauses are entering an if statement and know
 * how to handle them in that case.  This should be doable just by having symbols and their
 * scopes and meanings propagate back into the pattern.
 * 
 * @author nick
 *
 */
public class ChildedTemplate extends ChildedSyntax implements Template{

	private static int uniqueIds = 0;

	private SchemeObject templateObj;
	
	private SchemeEnv baseEnv;
	
	public SchemeEnv getBaseEnv() {
		return baseEnv;
	}

	public ChildedTemplate(SchemeObject obj, Map<String, PatternVar> pVars, SchemeEnv baseEnv) {
		super(obj.getString());
		this.templateObj = obj;
		this.baseEnv = baseEnv;
		if(obj.isList()){
			SchemeList templateList = (SchemeList) obj;
			setChildren(new ArrayList<SyntaxRelationship>(templateList.size()));
			for(int i = 0; i < templateList.size(); i++){
				SchemeObject o = templateList.get(i);
				if(templateObj.getFactory().matchesLiteral(o, "...")){
					getChildren().get(i - 1).num = -1;
					continue;
				}
				getChildren().add(new SyntaxRelationship(
						this, getSyntaxFromTemplate(o, pVars), o.getString(), 1));
			}
			setDot(((ChildedSyntax) obj.getSyntax()).getDot());
			return;
		}
		if(obj.isVector()){
			//TODO: account for vectors
		}
		setPriority(2);
		throw new UnsupportedOperationException("Should not have generated a template: " + obj.getString());
	}

	private PossibleSyntax getSyntaxFromTemplate(SchemeObject o, Map<String, PatternVar> pVars) {
		if(o.isList() || o.isVector()){
			return new PossibilitySyntax(new ChildedTemplate(o, pVars, baseEnv));
		}
		if(o.isSymbol()){
			if(pVars.containsKey(o.toString())){
				//then it's a pattern variable
				return pVars.get(o.toString());
			}
			//then it's an introduced symbol with no pattern associated
			return new PossibilitySyntax(new TemplateVar((SchemeSymbol) o, this));
			
		}
		return new PossibilitySyntax(new TemplateLiteral(o));
		//throw new UnsupportedOperationException("Can't generate template syntax for " + o.getString());
	}

	/**
	 * Actually performs the macro transformation.  This class always represents a list.  We should
	 * extend it to contain vectors.
	 * 
	 * @param matchedPatterns
	 * @return
	 */
	@Override
	public SchemeObject transform(SchemeObject parent, Map<String, List<SchemeObject>> matchedPatterns, int position){
		SchemeList l1 = templateObj.getFactory().makeEmptyList(parent);
		
		int lastNum = getChildren().size() - 1;
		SyntaxRelationship rel = getChildren().get(lastNum);
		int num = rel.num;
		Template t = (Template) rel.child;
		
		if(num == SyntaxRelationship.DOT_TAIL){
			lastNum--;
			SchemeObject dottedOn = t.transform(parent, matchedPatterns, 0);
			if(dottedOn.isList()){
				l1 = (SchemeList) dottedOn;
			}else{
				l1.shift(dottedOn);
			}
		}
	
		for(int i = lastNum; i >= 0; i--){
			rel = getChildren().get(i);
			t = (Template) rel.child;
			num = rel.num;
			if(num == SyntaxRelationship.INF){
				int count = position;
				SchemeObject done = null;
				while((done = t.transform(l1, matchedPatterns, count)) != null){
					l1.shift(done);
					count ++;
				}
			}
			l1.shift(t.transform(l1, matchedPatterns, 0));
		}
		return l1;
	}
	
	@Override
	public Map<String, List<TemplateVar>> getVariables(){
		Map<String, List<TemplateVar>> r = new HashMap<String, List<TemplateVar>>();
		for(SyntaxRelationship rel : getChildren()){
			Map<String, List<TemplateVar>> childVars = ((Template) rel.child.getSingleton()).getVariables();
			for(Map.Entry<String, List<TemplateVar>> entry : childVars.entrySet()){
				if(!r.containsKey(entry.getKey())){
					r.put(entry.getKey(), new ArrayList<TemplateVar>(entry.getValue().size()));
				}
				r.get(entry.getKey()).addAll(entry.getValue());
			}
		}
		return r;
	}

}
















