package machination.webjava.client.scheme.biwa;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeFactory;
import machination.webjava.trees.scheme.SchemeObject;
import machination.webjava.trees.scheme.SchemeSymbol;

public class BiwaSchemeSymbol extends SchemeSymbol{

	private BiwaSymbol datum;
	
	public BiwaSchemeSymbol(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, BiwaSymbol datum) {
		super(parent, factory, environment);
		this.datum = datum;
	}
	
	public BiwaSchemeSymbol(SchemeObject parent, BiwaSchemeFactory factory,
			SchemeEnv environment, String name){
		super(parent, factory, environment);
		this.datum = factory.getScheme().makeSymbol(name);
	}

	@Override
	public BiwaSymbol getDatum() {
		return datum;
	}

	@Override
	public String getString(){
		return getDatum().to_write();
	}
}
