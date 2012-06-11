package machination.webjava.client.scheme.biwa;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeFactory;
import machination.webjava.trees.scheme.SchemeObject;

public class BSString extends SchemeObject {

	private String value;
	
	public BSString(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, String value) {
		super(parent, factory, environment);
		this.value = value;
	}

	@Override
	public String getDatum() {
		return value;
	}

	@Override
	public String getString(){
		return '"' + value + '"';
	}
}
