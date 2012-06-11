package machination.webjava.trees.scheme;

/**
 * Raw instances of this type are placeholder symbols - they are placeholder objects that specifically
 * refer to symbols and can be entered into the environment.
 * @author nick
 *
 */
public class SchemeSymbol extends SchemeObject{
	
	/**
	 * We track symbols used to ensure that we don't accidentally give
	 * 2 the same "unspecified" name.  This is a wee bit dangerous, as
	 * we may find that overflows could occur.
	 * TODO: make sure we don't load anything big enough for overflows here
	 */
	private static long count = 0;
	public long id = -1;
	
	private String name;

	public SchemeSymbol(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment) {
		super(parent, factory, environment);
		this.id = count++;
		name = "__unspecifieds" + id;
	}
	
	public SchemeSymbol(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, long id) {
		super(parent, factory, environment);
		this.id = id;
		name = "__unspecifieds" + id;
	}
	
	public SchemeSymbol(SchemeObject parent, SchemeFactory factory,
			SchemeEnv environment, String name) {
		super(parent, factory, environment);
		this.name = name + "_" + id++;
	}
	

	public boolean isSymbol(){
		return true;
	}
	
	public SchemeEnv getBindingEnv(){
		return getEnvironment().bindingEnvironment(this);
	}
	
	@Override
	public String getString(){
		if(getDatum() == null){
			return name;
		}
		return super.getString();
	}

}
