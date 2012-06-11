package machination.webjava.trees.schemegrammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

public class TokenSyntax extends SchemeSyntax{

	private List<CharacterSyntax> characters;
	private String regex;
	private String name;
	
	private String literal = null;
	
	protected TokenSyntax(){}
	
	public TokenSyntax(String name, String literal){
		this.name = name;
		this.literal = literal;
		regex = "";
		characters = new ArrayList<CharacterSyntax>(literal.length());
		for(char c : literal.toCharArray()){
			CharacterSyntax cs = new CharacterSyntax(Character.isLetter(c) ? "" + c : "\\" + c, 1);
			characters.add(cs);
			regex += cs.getRegex();
		}
	}
	
	public TokenSyntax(String name, List<CharacterSyntax> characters){
		this.characters = characters;
		this.name = name;
		regex = "";
		for(CharacterSyntax c : characters){
			regex += c.getRegex();
			if(c.getNum() == -1){
				regex += '*';
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * What should we do here?  Return a placeholder!
	 * 
	 * The factory's holdSyntax method bares responsibility for deciding whether to make this object into a symbol.
	 */
	@Override
	public Set<? extends SchemeObject> getPossible(SchemeObject owner) {
		if(isSingleton()){
			SchemeObject r = owner.getFactory().fromString(owner, literal);
			r.startMatch().matchOriginal = this;
			return Collections.singleton(r);
		}
		return Collections.singleton(owner.getFactory().holdSyntax(owner, null, this, null));
	}
	
	public boolean isSingleton(){
		return literal != null;
	}

	@Override
	public boolean matchUnscoped(SchemeObject toTry) {
		return matchStructure(toTry);
	}

	@Override
	public boolean matchStructure(SchemeObject o) {
		//System.out.println("matching " + regex  + " - " + o.getString() + " -> "+ o.getString().matches(regex));
		return o.getString().matches(regex);
	}
	
	public boolean matchString(String s){
		return s.matches(regex);
	}

	@Override
	public void setSymbol(SchemeSymbol s) {
		throw new UnsupportedOperationException("tokensyntax doesn't support having symbols");
	}
}
