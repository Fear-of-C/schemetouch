package machination.webjava.trees.schemegrammar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeObject;

public class CharacterSyntax implements MetaSyntax<Character>{

	private String regex;
	private Set<Character> possibleCharacters;
	private int num;
	private String name;
	
	public CharacterSyntax(String regex, int num) {
		this.regex = regex;
		possibleCharacters = new HashSet<Character>();
		for(char i = 0; i < 256; i++){
			if((new Character(i)).toString().matches(regex)){
				possibleCharacters.add(i);
			}
		}
		this.num = num;
	}
	
	public CharacterSyntax(String name, String regex, int num){
		this(regex, num);
		this.name = name;
	}

	public int getNum(){
		return num;
	}
	
	@Override
	public String toString() {
		return regex;
	}
	
	public String getRegex(){
		return regex;
	}
	
	public Set<Character> getPossible(){
		return possibleCharacters;
	}
	
	@Override
	public boolean match(Character c){
		return possibleCharacters.contains(c);
	}

	@Override
	public String getName() {
		return name;
	}

}
