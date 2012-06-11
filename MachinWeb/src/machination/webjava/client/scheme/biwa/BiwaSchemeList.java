package machination.webjava.client.scheme.biwa;

import machination.webjava.trees.scheme.SchemeEnv;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;


public class BiwaSchemeList extends SchemeList {

	private BiwaPair datum;
	
	public BiwaSchemeList(SchemeObject parent, BiwaSchemeFactory factory,
			SchemeEnv environment, BiwaPair datum) {
		super(parent, factory, environment);
		this.datum = datum;
		BiwaPair current = datum;
		while(true){
			try{
				constituents.add(factory.fromBiwaObject((BiwaObject) current.getCar(), this, environment));
			}catch(ClassCastException ce){
				//we got a string!
				constituents.add(new BSString(this, factory, environment, current.getCarString()));
			}
			BiwaObject candidate = (BiwaObject) current.getCdr();
			
			if(factory.getScheme().isNil(candidate)){
				break;
			}
			
			if(candidate.isPair()){
				current = (BiwaPair) candidate;
			}else{
				constituents.add(factory.fromBiwaObject(candidate, this, environment));
				setProper(false);
				break;
			}
		}
	}


	/**
	 * Creates a nil list.
	 * @param parent
	 * @param biwaSchemeFactory
	 * @param newEnv
	 */
	public BiwaSchemeList(SchemeObject parent,
			BiwaSchemeFactory biwaSchemeFactory, SchemeEnv newEnv) {
		super(parent, biwaSchemeFactory, newEnv);
		datum = getNil();
	}
	
	/**
	 * This is a very abusive way of working - we are going to call "nil" a pair
	 * and let the handler deal with making sure we don't treat it as one.
	 * @return
	 */
	private native BiwaPair getNil()/*-{
		return BiwaScheme.nil;
	}-*/;
	
	@Override
	public boolean isPair(){
		return !factory.isNil(this);
	}


	@Override
	public void shift(SchemeObject front) {
		super.shift(front);
		//then we update the datum here
		datum = datum.shift(front.getDatum());
	}
	
	@Override
	protected void add(SchemeObject o) {
		if(factory.isNil(this)){
			shift(o);
			if(getParent() != null){
				((SchemeList) getParent()).datumReplaced(this);
			}
		}else{
			super.add(o);
			int location = constituents.indexOf(o);
			if(getHead() != null){
				location++;
			}
			if(location >= size() - 1){
				getDatum().appendChild(o.getDatum());
				//System.out.println("should have appended child " + o.getString());
			}else{
				getDatum().insertChild(location, o.getDatum());
				//System.out.println("should have inserted child " + o.getString());
			}
		}
	}


	@Override
	protected void remove(SchemeObject o) {
		//System.out.println("Removing " + o);
		int location = constituents.indexOf(o);
		if(getHead() != null){
			location++;
		}
		super.remove(o);
		if(location == 0){
			datum = (BiwaPair) getDatum().getCdr();
			if(getParent() != null){
				((SchemeList) getParent()).datumReplaced(this);
			}
		}else{
			getDatum().removeChild(location);
		}
	}
	
	
	
	@Override
	public void datumReplaced(SchemeObject owner) {
		super.datumReplaced(owner);
		int location = constituents.indexOf(owner);
		if(getHead() != null){
			location ++;
		}
		getDatum().replaceChild(location, owner.getDatum());
	}


	@Override
	protected void replaceObject(SchemeObject old, SchemeObject n) {
		super.replaceObject(old, n);
		datumReplaced(n);
	}


	@Override
	public BiwaPair getDatum(){
		return datum;
	}
	
	@Override
	public String getString(){
		return getDatum().to_write();
	}

	public void switchProper(){
		proper = !proper;
		if(proper){
			BiwaPair lastPair =  getDatum().seekPosition(size() - 2);
			lastPair.setCdr(BiwaPair.makePair(lastPair.getCdr(), BiwaPair.getNil()));
		}else{
			BiwaPair last2Pair = getDatum().seekPosition(size() - 2);
			last2Pair.setCdr(((BiwaPair) last2Pair.getCdr()).getCar());
		}
	}
}
