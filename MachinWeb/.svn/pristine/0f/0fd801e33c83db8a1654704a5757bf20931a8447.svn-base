package machination.webjava.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import machination.webjava.trees.scheme.SchemeList;
import machination.webjava.trees.scheme.SchemeObject;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.TreeViewModel;

/**
 * We can track opening and closing using a handler and change cell behavior
 * based on it (hopefully).
 * 
 * @author nick
 *
 */
class SchemeTreeModel implements TreeViewModel{
	
	/**
	 * 
	 */
	private final MachinationMain machinationMain;

	/**
	 * @param machinationMain
	 */
	SchemeTreeModel(MachinationMain machinationMain) {
		this.machinationMain = machinationMain;
	}

	private Set<SchemeObject> opened = new HashSet<SchemeObject>();
	
	public class FoldListener implements CloseHandler<TreeNode>, OpenHandler<TreeNode>{

		@Override
		public void onOpen(OpenEvent<TreeNode> event) {
			SchemeObject o = (SchemeObject) event.getTarget().getValue(); 
			opened.add(o);
			//treeSelect.setSelected((SchemeObject) event.getTarget().getChildValue(0), true);
			//System.out.println("opening " + opened);
			causeRefresh(o);
		}
		
		private void closeRecursive(SchemeObject o){
			if(opened.contains(o)){
				//System.out.println("actually closing" + o);
				opened.remove(o);
			}else{
				return;
			}
			
			//now we go down the schemelist and remove stuff
			if(o.isList()){
				for(SchemeObject child : ((SchemeList) o)){
					closeRecursive(child);
				}
			}
		}
		
		private void causeRefresh(SchemeObject value){
			if(value.getParent() == SchemeTreeModel.this.machinationMain.body){
				rp.refresh();
			}else{
				dataCache.get(value.getParent()).refresh();
			}
		}


		@Override
		public void onClose(CloseEvent<TreeNode> event) {
			SchemeObject o = (SchemeObject) event.getTarget().getValue(); 
			closeRecursive(o);
			causeRefresh(o);
		} 
		
	}
	
	public FoldListener foldListener = new FoldListener();
	
	ListDataProvider<SchemeObject> rp = new ListDataProvider<SchemeObject>();
	
	private HashMap<SchemeObject, ListDataProvider<SchemeObject>> dataCache = new HashMap<SchemeObject, ListDataProvider<SchemeObject>>();
	
	public void refresh(){
		rp.setList(((SchemeList) this.machinationMain.body).accessConstituents());
		rp.refresh();
		for(Map.Entry<SchemeObject, ListDataProvider<SchemeObject>> e : dataCache.entrySet()){
			ListDataProvider<SchemeObject> ldp = e.getValue();
			ldp.setList(((SchemeList) e.getKey()).accessConstituents()); 
			ldp.refresh();
		}
	}
	
	public void delete(){
		SchemeObject o = this.machinationMain.treeSelect.getSelectedObject();
		//System.out.println("checking deletion for objects of which there are " + ((SchemeList) o.getParent()).getByRel(o.getRole()).size());
		dataCache.remove(o);
		o.delete();
		this.machinationMain.pp.hide();
		((SchemeTreeModel) this.machinationMain.cellTree.getTreeViewModel()).refresh();
	}
	
	public AbstractCell<SchemeObject> cell = new AbstractCell<SchemeObject>(
			"click", "keydown") {
          @Override
          public void render(Context context, SchemeObject value, SafeHtmlBuilder sb) {
            if (value != null) {	            	
            	if((value.getParent() != null) && (value.getParent() instanceof SchemeList) &&
            			!(((SchemeList) value.getParent()).isProper()) &&
            			(value == ((SchemeList) value.getParent()).get(((SchemeList) value.getParent()).size() - 1))){
            		sb.appendHtmlConstant(".  ");
            	}
            	
            	sb.appendHtmlConstant("<div class=\"treenode\">");
            	if((value instanceof SchemeList) && opened.contains(value)){
            		sb.appendEscaped(((SchemeList) value).getHeadString());
            	}else{
            		if(value.getDatum() == null){
            			sb.appendHtmlConstant("<div class=\"unspecified\">");
	            		sb.appendEscaped(value.getString());
            			sb.appendHtmlConstant("</div>");
            		}else{
            			sb.appendEscaped(value.getString());
            		}
            	}
            	sb.appendHtmlConstant("</div>");
            }else{
            	sb.appendEscaped("Scheme Body");
            }
          }
       

		@Override
		public boolean isEditing(
				com.google.gwt.cell.client.Cell.Context context,
				Element parent, SchemeObject value) {
			return SchemeTreeModel.this.machinationMain.treeSelect.isSelected(value);
		}


		@Override
		public void onBrowserEvent(Context context, Element parent,
				SchemeObject value, NativeEvent event,
				ValueUpdater<SchemeObject> valueUpdater) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
			if("click".equals(event.getType())){
				SchemeTreeModel.this.machinationMain.pp.setPixelSize(Window.getClientWidth() - 80, Window.getClientHeight() - 100);
				SchemeTreeModel.this.machinationMain.pp.setPopupPosition(
						Window.getScrollLeft() + 50, Window.getScrollTop() + 50);
				SchemeTreeModel.this.machinationMain.improprietize.setEnabled((value instanceof SchemeList) && (((SchemeList) value).size() > 1));
				SchemeTreeModel.this.machinationMain.text.setText(value.getString());
				if(value instanceof SchemeList){
					SchemeTreeModel.this.machinationMain.improprietize.setText(((SchemeList) value).isProper() ? "Proper" : "Improper");
				}
				SchemeTreeModel.this.machinationMain.pp.show();
			}
		}
		
    };
	
	@Override
	public <T> NodeInfo<?> getNodeInfo(T value) {
		//System.out.println("setting up tree " + (body == null));
		
		if((this.machinationMain.body == null) || (value == null)){
			return new DefaultNodeInfo<SchemeObject>(
					rp, cell, this.machinationMain.treeSelect, null);
		}
		
		if(value instanceof SchemeObject){
			SchemeObject o = (SchemeObject) value;
			if(o.isList() && o.isPair()){
				//then this thing has children
				SchemeList sl = (SchemeList) o;
				
				//System.out.println("Creating child list: " + objList);
				if(!dataCache.containsKey(o)){
					dataCache.put(o, new ListDataProvider<SchemeObject>(sl.accessConstituents()));
				}
				return new DefaultNodeInfo<SchemeObject>(dataCache.get(o), cell, this.machinationMain.treeSelect, null);
			}
		}
		
		return null;
	}

	@Override
	public boolean isLeaf(Object value) {
		return (value instanceof SchemeObject) && !((SchemeObject) value).isList();
	}
	
}