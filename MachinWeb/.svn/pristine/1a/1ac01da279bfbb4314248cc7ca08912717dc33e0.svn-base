package machination.webjava.client.scheme.biwa;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeFactory;
import machination.webjava.trees.scheme.SchemeObject;

public class BiwaSchemeNumber extends SchemeObject{
	public BiwaSchemeNumber(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, BiwaNumber num) {
		super(parent, factory, environment);
		this.datum = num;
		addSyntax(factory.getSyntax("number"));
	}

	private BiwaNumber datum;

	@Override
	public BiwaNumber getDatum() {
		return datum;
	}
}
