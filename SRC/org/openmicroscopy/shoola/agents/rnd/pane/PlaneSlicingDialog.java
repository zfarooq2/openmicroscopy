/*
 * org.openmicroscopy.shoola.agents.rnd.pane.PlaneSlicingDialog
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

package org.openmicroscopy.shoola.agents.rnd.pane;


//Java imports
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies


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
class PlaneSlicingDialog
	extends JDialog
{
	private static final int			WIDTH_WIN = 440;
	private static final int			HEIGHT_WIN = 350;
	private static final int			HEIGHT_PANEL = 100;
	
	private static final String[]   	RANGE;
	static {
		RANGE = new String[7];
		RANGE[0] = "1-bit plane";
		RANGE[1] = "2-bit plane";
		RANGE[2] = "3-bit plane";
		RANGE[3] = "4-bit plane";
		RANGE[4] = "5-bit plane";
		RANGE[5] = "6-bit plane";
		RANGE[6] = "7-bit plane";         
	}
	
	private	JRadioButton				radioStatic, radioDynamic;
	private JComboBox					range;
		
	private PlaneSlicingPanel			psPanel;
	private PlaneSlicingStaticPanel		pssPanel;
	private PlaneSlicingDialogManager	manager;
	
	PlaneSlicingDialog(QuantumPaneManager control)
	{
		super(control.getReferenceFrame(), "Plane Slicing", true);
		manager = new PlaneSlicingDialogManager(this, control);
		int yStart, yEnd;
		//TODO: retrieve user settings.
		int start = control.getCurOutputStart();
		int end = control.getCurOutputEnd();
		yStart = manager.convertRealIntoGraphics(start);
		yEnd = manager.convertRealIntoGraphics(end);
		psPanel = new PlaneSlicingPanel(yStart, yEnd);
		pssPanel = new PlaneSlicingStaticPanel();
		manager.setOutputStartBox(yStart);
		manager.setOutputEndBox(yEnd);
		initialize(0); //TODO: user settings.
		manager.attachListeners();
		buildGUI();
	}

	JComboBox getRange()
	{
		return range;
	}
	
	JRadioButton getRadioStatic()
	{
		return radioStatic;
	}

	JRadioButton getRadioDynamic()
	{
		return radioDynamic;
	}
	
	/** Returns the dynamic panel. */
	PlaneSlicingPanel getPSPanel()
	{
		return psPanel;
	}
	
	/** Returns the static panel. */
	PlaneSlicingStaticPanel getPSSPanel()
	{
		return pssPanel;
	}
	
	/** Returns the manager. */
	PlaneSlicingDialogManager getManager()
	{
		return manager;
	}

	/** Initializes the component. */
	private void initialize(int index)
	{
		String txtDynamic = "<html>Highlights a range,<br>" +
							"reduces others to a constant level (cf. (1))." +
							"</html>";
		String txtStatic = "<html>Highlights a range,<br>" +
							"preserves others (cf. (2)).</html>";
		radioStatic =	new JRadioButton(txtStatic);
		radioDynamic =	new JRadioButton(txtDynamic);
		radioDynamic.setSelected(true);
		range = new JComboBox(RANGE);
		range.setSelectedIndex(index);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		Container contentPane = super.getContentPane();
		contentPane.setLayout(null);
		psPanel.setBounds(0, 0, PlaneSlicingPanel.WIDTH, 
						PlaneSlicingPanel.HEIGHT);
		pssPanel.setBounds(PlaneSlicingPanel.WIDTH, 0, PlaneSlicingPanel.WIDTH, 
						PlaneSlicingPanel.HEIGHT);					 
		contentPane.add(psPanel);
		contentPane.add(pssPanel);
		contentPane.add(buildRadioGroupPanel());
		setSize(WIDTH_WIN, HEIGHT_WIN);
		setResizable(false);
		super.repaint();
	}
	
	/** 
	 * Build a panel containing the radiogroup.
	 *
	 * @return the above mentioned.
	 */    
	private JPanel buildRadioGroupPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		radioDynamic.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(radioDynamic);
		group.add(radioStatic);		
		p.add(radioDynamic);
		p.add(radioStatic);
		p.add(buildComboBoxPanel());
		p.setBounds(0, PlaneSlicingPanel.HEIGHT, WIDTH_WIN, HEIGHT_PANEL);
		return p;
	}
	
	/** 
	 * Display the combox int a JPanel.
	 * @return See above.
	 */
	private JPanel buildComboBoxPanel()
	{
		JPanel p = new JPanel();
		JLabel txt = new JLabel(" Select a range: ");
		int hTxt, wLabel = 110;		
		Dimension d = range.getPreferredSize();
		hTxt = (int) d.getHeight();
		Dimension dLabel = new Dimension(wLabel, hTxt),
				  dPanel = new Dimension(wLabel+(int) d.getWidth(), hTxt);
		p.setLayout(null);
		// size Jlabel
		txt.setBounds(0, 0, wLabel, hTxt);
		txt.setMaximumSize(dLabel);
		txt.setMinimumSize(dLabel);	
		range.setBounds(wLabel, 0, (int) d.getWidth()+20, hTxt);
		p.setPreferredSize(dPanel);
		p.setSize(dPanel);
		p.add(txt);
		p.add(range);  
		return p;	
	}

}
