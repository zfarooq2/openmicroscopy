/*
 * org.openmicroscopy.shoola.agents.datamng.editors.ProjectEditorManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.datamng.editors;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.ProjectData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ProjectEditorManager
	implements ActionListener
{
	private static final int	SAVE = 0;	
	private static final int	RELOAD = 1;
	
	private ProjectData			model;
	private ProjectEditor		view;
	private JButton 			saveButton, reloadButton;
	
	public ProjectEditorManager(ProjectEditor view, ProjectData model)
	{
		this.view = view;
		this.model = model;
	}
	
	ProjectData getProjectData()
	{
		return model;
	}

	/** Initializes the listeners. */
	void initListeners()
	{
		saveButton = view.getSaveButton();
		reloadButton = view.getReloadButton();
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		reloadButton.addActionListener(this);
		reloadButton.setActionCommand(""+RELOAD);
	}
	
	/** Handles event fired by the buttons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int     index = Integer.parseInt(s);
			switch (index) { 
				case SAVE:
					save();
					break;
				case RELOAD:
					reload();
					break;
			}// end switch  
		} catch(NumberFormatException nfe) {
		   throw nfe;  //just to be on the safe side...
		} 
	}
	
	void setProjectFields(Object value, int row)
	{
		saveButton.setEnabled(true);
		switch (row) {
			case 1:
				model.setName((String) value);
				break;
			case 2:
				model.setDescription((String) value);	
		}
		
	}
	
	/** */
	private void save()
	{
		System.out.println("name: "+model.getName());
	}
	
	/** */
	private void reload()
	{
	}
}	
	
