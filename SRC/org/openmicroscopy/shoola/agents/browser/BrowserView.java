/*
 * org.openmicroscopy.shoola.agents.browser.BrowserView
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser;

import org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener;

import edu.umd.cs.piccolo.PCanvas;

/**
 * The view component of the top-level browser MVC architecture.  Where the
 * thumbnails are physically drawn.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BrowserView extends PCanvas
                         implements BrowserModelListener, ProgressListener
{
    private BrowserModel browserModel;
    private BrowserTopModel overlayModel;
    private BrowserEnvironment env;

    private void init()
    {
        env = BrowserEnvironment.getInstance();
    }

    /**
     * Constructs the browser view with the two backing models-- one for the
     * thumbnails and the other for any sticky overlays.
     * 
     * @param browserModel The thumbnail/canvas model.
     * @param overlayModel The overlay/sticky node model.
     */
    public BrowserView(BrowserModel browserModel, BrowserTopModel overlayModel)
    {
        init();

        if (browserModel == null || overlayModel == null)
        {
            sendInternalError("Null parameters in BrowserView constructor");
        }
        else
        {
            this.browserModel = browserModel;
            this.overlayModel = overlayModel;
        }
    }
    
    public void updateThumbnails()
    {
        // relayout children and all that good stuff
    }

    /**
     * Show the overlay (sticky) nodes.
     */
    public void showModalNodes()
    {
        // TODO: fill in code
    }

    /**
     * Hide the overlay (sticky) nodes.
     */
    public void hideModalNodes()
    {
        // TODO: fill in code
    }
    
    /**
     * Responds to a model-triggered update.
     * @see org.openmicroscopy.shoola.agents.browser.BrowserModelListener#modelUpdated()
     */
    public void modelUpdated()
    {
        // TODO Auto-generated method stub

    }
    
    /**
     * Indicates to the user that an iterative, potentially time-consuming
     * process has started.
     * 
     * @param piecesOfData The number of steps in the process about to start.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processStarted(int)
     */
    public void processStarted(int piecesOfData)
    {
        // bring up process view window?
        // TODO: make BProgressIndicator
    }
    
    /**
     * Indicates to the user that a process has advanced a step.
     * 
     * @param info The message to display.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processAdvanced(java.lang.String)
     */
    public void processAdvanced(String info)
    {
        // TODO: advance BProgressIndicator, show message
    }

    /**
     * Display that the process has failed for some reason.
     * 
     * @param The displayed reason why a process failed.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processFailed(java.lang.String)
     */
    public void processFailed(String reason)
    {
        // TODO: close BProgressIndicator, launch User notifier?
    }
    
    /**
     * Display that a process has succeeded.
     * @see org.openmicroscopy.shoola.agents.browser.datamodel.ProgressListener#processSucceeded()
     */
    public void processSucceeded()
    {
        // TODO: close BProgressIndicator, nothing more (success implicit)
    }


    // send internal error through the BrowserEnvironment pathway
    private void sendInternalError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportInternalError(message);
    }

    // send general error through the BrowserEnvironment pathway
    private void sendError(String message)
    {
        MessageHandler handler = env.getMessageHandler();
        handler.reportError(message);
    }
}
