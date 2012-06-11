package machination.webjava.client.scheme.biwa;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeFactory;
import machination.webjava.trees.scheme.SchemeObject;

/**
 * Has a similar problem to numeric types: this object wants to be yet another
 * form of BiwaObject, but biwa doesn't usually do that.
 * @author nick
 *
 */
public class BiwaSchemeBool extends SchemeObject{

	private BiwaObject value;
	
	public BiwaSchemeBool(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, BiwaObject string) {
		super(parent, factory, environment);
		this.value = string;
	}

	@Override
	public BiwaObject getDatum() {
		if(!value.isBool()){
			throw new IllegalStateException("Boolean is not boolean type " + value);
		}
		return value;
	}
	
	@Override
	public String getString(){
		return getDatum().to_write();
	}
}
