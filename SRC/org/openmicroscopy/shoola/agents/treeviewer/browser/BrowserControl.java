/*
 * org.openmicroscopy.shoola.agents.treemng.browser.BrowserControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer.browser;


//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CloseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.CollapseAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ShowNameAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SortByDateAction;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.TagAnnotationData;

/** 
 * The Browser's Controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserControl
    implements ChangeListener
{

    /** Identifies the <code>Collapse</code> action. */
	static final Integer    COLLAPSE = new Integer(0);

	/** Identifies the <code>Close</code> action. */
	static final Integer    CLOSE = new Integer(1);

	/** Identifies the <code>Sort</code> action. */
	static final Integer    SORT = new Integer(2);

	/** Identifies the <code>Sort by Date</code> action. */
	static final Integer    SORT_DATE = new Integer(3);
    
    /** Identifies the <code>Partial Name</code> action.*/
    static final Integer    PARTIAL_NAME = new Integer(4);
   
    /** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser     			model;
    
    /** Reference to the View. */
    private BrowserUI   			view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(COLLAPSE, new CollapseAction(model));
        actionsMap.put(CLOSE, new CloseAction(model));
        actionsMap.put(SORT, new SortAction(model));
        actionsMap.put(SORT_DATE, new SortByDateAction(model));
        actionsMap.put(PARTIAL_NAME, new ShowNameAction(model));
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserUI) initialize} method 
     * should be called straight after to link this Controller to the other 
     * MVC components.
     * 
     * @param model  Reference to the {@link Browser} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    BrowserControl(Browser model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap<Integer, Action>();
        createActions();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }
    
    /**
     * Returns the node hosting the experimenter passing a child node.
     * 
     * @param node The child node.
     * @return See above.
     */
    TreeImageDisplay getDataOwner(TreeImageDisplay node)
    {
    	if (node == null) return null;
    	TreeImageDisplay parent = node.getParentDisplay();
    	Object ho;
    	if (parent == null) {
    		ho = node.getUserObject();
    		if (ho instanceof ExperimenterData)
    			return node;
    		return null;
    	}
    	ho = parent.getUserObject();
    	if (ho instanceof ExperimenterData) 
    		return parent;
    	return getDataOwner(parent);
    }

    /**
     * Reacts to tree expansion events.
     * 
     * @param display   The selected node.
     * @param expanded  Pass <code>true</code> if the node is expanded,
     * 					<code>false</code> otherwise.
     */
    void onNodeNavigation(TreeImageDisplay display, boolean expanded)
    {
    	
    	if (!expanded) {
    		model.cancel();
    		return;
    	}
        int state = model.getState();
        if ((state == Browser.LOADING_DATA) ||
             (state == Browser.LOADING_LEAVES)) 
             //|| (state == Browser.COUNTING_ITEMS)) 
             return;
        
        Object ho = display.getUserObject();
        model.setSelectedDisplay(display); 
        if (model.getBrowserType() == Browser.IMAGES_EXPLORER &&
        	!display.isChildrenLoaded() && ho instanceof ExperimenterData) {
        	model.countExperimenterImages(display);
        	return;
        }
        if (display.isChildrenLoaded()) {
        	List l = display.getChildrenDisplay();
			//if (display.getChildCount() != l.size()) {
	
        		//view.setLeavesViews(l, (TreeImageSet) display);
        	//} else {
        		if (view.isFirstChildMessage(display)) {
        			view.setLeavesViews(l, (TreeImageSet) display);
        		}
        	//}
        	return;
        }
        if (ho instanceof ProjectData) {
        	if (display.numberItems == 0) return;
        }
        view.loadAction(display);
        if (display instanceof TreeImageTimeSet) {
        	TreeImageTimeSet node = (TreeImageTimeSet) display;
        	model.loadExperimenterData(getDataOwner(display), node);
        	return;
        }
        if ((ho instanceof DatasetData) || (ho instanceof TagAnnotationData) 
        		|| (ho instanceof PlateData)) {
        	model.loadExperimenterData(getDataOwner(display), display);
        } else if (ho instanceof ExperimenterData) {
        	model.loadExperimenterData(display, null);
        }
    }
    
    /** 
     * Brings up the popup menu. 
     * 
     * @param index The index of the menu.
     */
    void showPopupMenu(int index) { model.showPopupMenu(index); }
    
    /** 
     * Reacts to click events in the tree.
     * 
     *  @param added The collection of added paths.
     */
    void onClick(List<TreePath> added)
    {
    	JTree tree = view.getTreeDisplay();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) return;
        TreeImageDisplay node;
        TreePath path;
        if (paths.length == 1) {
        	node = (TreeImageDisplay) paths[0].getLastPathComponent();
        	model.setSelectedDisplay(node);
    		return;
        }
     	//more than one node selected.
    	TreeImageDisplay previous = model.getLastSelectedDisplay();
    	Class ref = previous.getUserObject().getClass();
    	
    	List<TreeImageDisplay> l = new ArrayList<TreeImageDisplay>();
    	List<TreePath> toRemove = new ArrayList<TreePath>();
    	if (added != null) {
    		Iterator<TreePath> i = added.iterator();
        	while (i.hasNext()) {
    			path = i.next();
    			node = (TreeImageDisplay) path.getLastPathComponent();
    			if (node.getUserObject().getClass().equals(ref)) 
    				l.add(node);
    			else toRemove.add(path);
    		}
    	}
    	
    	if (toRemove.size() > 0) {
    		String text = "";
        	if (ImageData.class.equals(ref)) text = "images.";
        	else if (ProjectData.class.equals(ref)) text = "projects.";
        	else if (DatasetData.class.equals(ref)) text = "datasets.";
        	else if (TagAnnotationData.class.equals(ref)) text = "tags.";
        	 UserNotifier un = 
                 TreeViewerAgent.getRegistry().getUserNotifier();
             un.notifyInfo("Tree selection", "You can only select "+text);
             view.removeTreePaths(toRemove);
    	}
    	paths = tree.getSelectionPaths();
    	
    	TreeImageDisplay[] nodes = new TreeImageDisplay[paths.length];
    	for (int j = 0; j < paths.length; j++) {
			nodes[j] = (TreeImageDisplay) paths[j].getLastPathComponent();
		}
    	model.setSelectedDisplays(nodes);
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }
	
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
    	int state = model.getState();
    	switch (state) {
			case Browser.BROWING_DATA:
				
				break;
	
			default:
				break;
		}
		view.onStateChanged(state == Browser.READY);
    }

}
