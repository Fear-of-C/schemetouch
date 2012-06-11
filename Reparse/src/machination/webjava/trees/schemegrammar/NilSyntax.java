package machination.webjava.trees.schemegrammar;

import java.util.Collections;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

/**
 * Somehow, this should count as an identifier...
 * 
 * It probably gets thrown in as one of the base identifiers or... something.
 * 
 * We might not even actually use this class at all.
 * 
 * @author nick
 *
 */
public class NilSyntax extends PossibleSyntax {
	
	public NilSyntax(){
		nilChild.setPriority(1);
	}
	
	SchemeSyntax nilChild = new ChildedSyntax("nil"){
		
		@Override
		public Set<SchemeObject> getPossible(SchemeObject owner) {
			SchemeObject theList = owner.getFactory().makeEmptyList(null);
			theList.startMatch().matchOriginal = this;
			return Collections.singleton(theList);
		}

		@Override
		public String getName() {
			return "nil";
		}

		@Override
		public boolean matchUnscoped(SchemeObject toTry) {
			return matchStructure(toTry);
		}

		@Override
		public boolean matchStructure(SchemeObject o) {
			return o.getFactory().isNil(o);
		}

		@Override
		public void setSymbol(SchemeSymbol s) {
			throw new UnsupportedOperationException("Nil has no symbol.");
		}
	};

	@Override
	public String getName() {
		return "nil";
	}

	@Override
	public Set<SchemeSyntax> getPossible(SchemeObject position) {
		return Collections.singleton(nilChild);
	}

	@Override
	public Set<PossibleSyntax> getChildren(SchemeObject context) {
		return Collections.emptySet();
	}

}
