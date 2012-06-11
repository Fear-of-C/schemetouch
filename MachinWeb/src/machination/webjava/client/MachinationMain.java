package machination.webjava.client;


import machination.webjava.client.scheme.biwa.BiwaSchemeFactory;
import machination.webjava.client.scheme.biwa.BiwaSchemeList;
import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.SingleSelectionModel;

/*
 * How do we fix non-scrolling bug?
 * 1) Short-term: reduce the # of displayed objects in the cellbrowser to 3 or 4.
 * 2) Long-term: limit maximum "base" of logarithmic coding by width.  So if we
 * can fit 10 object on the screen, then every cellBrowser column should have 10
 * things if possible and never more.  If we can only fit 4, then reduce the # to 4.
 * 3-4 might actually be optimal, as it allows us to subitize.  Let's figure out
 * how to do an exp coding of this type by creating possibility links based on
 * alphabetical ordering.  We can then, when the probabalizer is ready, section
 * off 3-4 "likely" options above the main list.
 * 3) For horizontal scrolling, we need something that actually scrolls or emulates
 * scrolling.  We could
 * a) change the definition of the "root" node and respond to clicks differently.  This
 * would probably involve folding/unfolding nodes as they are selected.  It's something
 * of a more elegant metahpor for what we're trying to do - these lists are designed for
 * creation and NOT for selection.
 * b) introduce a scrolling solution.
 * 
 */
public class MachinationMain implements EntryPoint {

	BiwaSchemeFactory fact;
	final SingleSelectionModel<SchemeObject> treeSelect = new SingleSelectionModel<SchemeObject>();
	
	/**
	 * The nasty thing about this is figuring out how to update all the UI elements as soon as
	 * the factory is finished loading.
	 */
	SchemeObject body;
	
	/**
	 * After UI is set up, this method begins configuring the UI to actually do stuff.
	 */
	private void configureSchemeInstant(){
		new BiwaSchemeFactory(this);
	}
	
	/**
	 * Called after the Biwa factory is definitely loaded.
	 */
	public void onConfigured(BiwaSchemeFactory toConfigure){
		fact = toConfigure;
		body = fact.startBody();
		
		onDemoLoaded();
	}
	
	public void onDemoLoaded(){
		
		pshbtnEval.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				textBox.setText(fact.evaluateBody(body));
			}
		
		});
		pshbtnEval.setEnabled(true);
		((SchemeTreeModel) cellTree.getTreeViewModel()).rp.setList(((SchemeList) body).accessConstituents());
	}

	
	PushButton pshbtnEval;
	TextBox textBox;
	CellBrowser cellBrowser;
	CellTree cellTree;
	PopupPanel pp;
	PushButton improprietize;
	PushButton copy;
	PushButton paste;
	TextArea text;
	StackLayoutPanel editingPanel;
	
	SchemeObject clipped;
	
	static final String[] MOBILE_SPECIFIC_SUBSTRING = {  
	      "iPhone","Android","MIDP","Opera Mobi",  
	      "Opera Mini","BlackBerry","HP iPAQ","IEMobile",  
	      "MSIEMobile","Windows Phone","HTC","LG",  
	      "MOT","Nokia","Symbian","Fennec",  
	      "Maemo","Tear","Midori","armv",  
	      "Windows CE","WindowsCE","Smartphone","240x320",  
	      "176x220","320x320","160x160","webOS",  
	      "Palm","Sagem","Samsung","SGH",  
	      "SIE","SonyEricsson","MMP","UCWEB"};  	
	
	private boolean checkMobile() {
		//http://www.javaneverdie.com/gwt/want-to-show-mobile-or-phone-site-version-check-user-agent/
	      String userAgent = Window.Navigator.getUserAgent();  
	     for (String mobile: MOBILE_SPECIFIC_SUBSTRING){  
	           if (userAgent.contains(mobile)  
	             || userAgent.contains(mobile.toUpperCase())  
	             || userAgent.contains(mobile.toLowerCase())){  
	                  return true;  
	          }  
	     }  
	  
	     return false;  
	}  

	@Override
	public void onModuleLoad() {
		RootPanel rootPanel = RootPanel.get();
		
		int totalWidth = Window.getClientWidth();
		int totalHeight = checkMobile() ? 1500 : Window.getClientHeight();

		DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.EM);
		rootPanel.add(dockLayoutPanel, 10, 10);
		dockLayoutPanel.setPixelSize(totalWidth - 10, totalHeight - 30);
		
		DockLayoutPanel dockLayoutPanel_1 = new DockLayoutPanel(Unit.EM);
		dockLayoutPanel.addSouth(dockLayoutPanel_1, 2.2);
		
		pshbtnEval = new PushButton("eval");
		pshbtnEval.setEnabled(false);
		dockLayoutPanel_1.addWest(pshbtnEval, 3.4);
		
		textBox = new TextBox();
		textBox.setReadOnly(true);
		dockLayoutPanel_1.add(textBox);
		
		
		cellBrowser = new CellBrowser(new GenerateTreeModel(this), null);
		//instead, we will put the cellBrowser into a dialog box
		//dockLayoutPanel.addEast(cellBrowser, 16.7);
		text = new TextArea();
		

		editingPanel = new StackLayoutPanel(Unit.EM);
		editingPanel.add(cellBrowser, new HTML("Browse"), 2);
		//editingPanel.add(text, new HTML("View Text (edits don't record)"), 2);
		text.setSize("100%", "100%");
		text.getElement().setId("text");
				
		improprietize = new PushButton("Toggle propriety");
		improprietize.setEnabled(true);
		improprietize.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				((BiwaSchemeList) treeSelect.getSelectedObject()).switchProper();
				((SchemeTreeModel) cellTree.getTreeViewModel()).refresh();
				//System.out.println(treeSelect.getSelectedObject().getDescriptionRecursive());
			}
		
		});

		
		copy = new PushButton("Copy");
		copy.setEnabled(true);
		copy.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				/*
				 * We could (and should) just use this as an alternative copy/paste method.
				 */
				clipped = treeSelect.getSelectedObject();
			}
		});
		
		paste = new PushButton("Inject");
		paste.setEnabled(true);
		paste.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				/*
				 * We could (and should) just use this as an alternative copy/paste method.
				 */
				SchemeObject insert = fact.cloneObject(treeSelect.getSelectedObject().getParent(), clipped, treeSelect.getSelectedObject().getEnvironment());
				insert.clearSyntax();
				insert.setRole(treeSelect.getSelectedObject().getRole());
				insert.setEnvironment(treeSelect.getSelectedObject().getEnvironment());
				try{
					insert.completeSyntax();
					treeSelect.getSelectedObject().replace(insert);
					((SchemeTreeModel) cellTree.getTreeViewModel()).refresh();
					
					
				}catch(IllegalStateException e){
					textBox.setText("Can't paste non-matching object.");
				}
			}
		});

		pp = new PopupPanel(true);
		PushButton delete = new PushButton("Delete");
		delete.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				((SchemeTreeModel) cellTree.getTreeViewModel()).delete();
			}
		
		});
		
		//PushButton moveTo = new PushButton("Move to...");
		
		LayoutPanel popP = new LayoutPanel();
		popP.add(copy);
		popP.add(paste);
		popP.add(delete);
		popP.add(editingPanel);
		popP.add(improprietize);
		
		popP.setWidgetTopHeight(delete, 0, Style.Unit.PX, 30, Style.Unit.PX);
		popP.setWidgetTopHeight(improprietize, 0, Style.Unit.PX, 30, Style.Unit.PX);
		popP.setWidgetTopHeight(copy, 0, Style.Unit.PX, 30, Style.Unit.PX);
		popP.setWidgetTopHeight(paste, 0, Style.Unit.PX, 30, Style.Unit.PX);
		
		popP.setWidgetLeftRight(improprietize, 0, Style.Unit.PCT, 75, Style.Unit.PCT);
		popP.setWidgetLeftRight(copy, 25, Style.Unit.PCT, 50, Style.Unit.PCT);
		popP.setWidgetLeftRight(paste, 50, Style.Unit.PCT, 25, Style.Unit.PCT);
		popP.setWidgetLeftRight(delete, 75, Style.Unit.PCT, 0, Style.Unit.PCT);
		
		popP.setWidgetTopBottom(editingPanel, 30, Style.Unit.PX, 0, Style.Unit.PCT);
		
		pp.add(popP);
		
		SchemeTreeModel stm = new SchemeTreeModel(this);
		
		ScrollPanel scrollPanel = new ScrollPanel();
		dockLayoutPanel.add(scrollPanel);
		cellTree = new CellTree(stm, null);
		//cellTree.set
		cellTree.setDefaultNodeSize(1000);
		scrollPanel.setWidget(cellTree);
		//cellTree.setSize("390px", "273px");
		cellTree.addOpenHandler(stm.foldListener);
		cellTree.addCloseHandler(stm.foldListener);
		
		configureSchemeInstant();
	}
}
