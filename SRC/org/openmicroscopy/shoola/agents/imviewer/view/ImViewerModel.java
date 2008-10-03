/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerModel
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

package org.openmicroscopy.shoola.agents.imviewer.view;



//Java imports
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.model.Pixels;
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.events.iviewer.CopyRndSettings;
import org.openmicroscopy.shoola.agents.imviewer.ContainerLoader;
import org.openmicroscopy.shoola.agents.imviewer.DataLoader;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.ProjectionSaver;
import org.openmicroscopy.shoola.agents.imviewer.RenderingControlLoader;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsCreator;
import org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsLoader;
import org.openmicroscopy.shoola.agents.imviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.imviewer.browser.BrowserFactory;
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.agents.imviewer.rnd.RendererFactory;
import org.openmicroscopy.shoola.agents.imviewer.util.HistoryItem;
import org.openmicroscopy.shoola.agents.imviewer.util.player.ChannelPlayer;
import org.openmicroscopy.shoola.agents.imviewer.util.player.Player;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;

/** 
* The Model component in the <code>ImViewer</code> MVC triad.
* This class tracks the <code>ImViewer</code>'s state and knows how to
* initiate data retrievals. It also knows how to store and manipulate
* the results. This class provides a suitable data loader.
* The {@link ImViewerComponent} intercepts the results of data loadings, feeds
* them back to this class and fires state transitions as appropriate.
* 
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME2.2
*/
class ImViewerModel
{

	/** Flag to indicate that the image is not compressed. */
	static final int 			UNCOMPRESSED = RenderingControl.UNCOMPRESSED;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * medium Level of compression. 
	 */
	static final int 			MEDIUM = RenderingControl.MEDIUM;
	
	/** 
	 * Flag to indicate that the image is not compressed using a
	 * low Level of compression. 
	 */
	static final int 			LOW = RenderingControl.LOW;
	
	/** The maximum width of the thumbnail. */
	private static final int    THUMB_MAX_WIDTH = 48; 

	/** The maximum height of the thumbnail. */
	private static final int    THUMB_MAX_HEIGHT = 48;
	
	/** The maximum width of the image. */
	private static final int    IMAGE_MAX_WIDTH = 512;

	/** The maximum height of the image. */
	private static final int    IMAGE_MAX_HEIGHT = 512;

	/** Index of the <code>RenderingSettings</code> loader. */
	private static final int	SETTINGS = 0;
	
	/** Index of the <code>RenderingControlLoader</code> loader. */
	private static final int	RND = 1;
	
	/** The image to view. */
	private ImageData 					image; 

	/** Holds one of the state flags defined by {@link ImViewer}. */
	private int                 		state;

	/** Reference to the component that embeds this model. */
	private ImViewer            		component;

	/** Map hosting the various loaders. */
	private Map<Integer, DataLoader>	loaders;
	
	/** The sub-component that hosts the display. */
	private Browser             		browser;
	
	/** Reference to the {@link Renderer}. */
	private Renderer            		renderer;

	/** Reference to the current player. */
	private ChannelPlayer       		player;

	/** The width of the thumbnail if the window is iconified. */
	private int                 		sizeX;

	/** The height of the thumbnail if the window is iconified. */
	private int                 		sizeY;

	/** The magnification factor for the thumbnail. */
	private double              		factor;

	/** The image icon. */
	private BufferedImage       		imageIcon;

	/** The bounds of the component requesting the viewer. */
	private Rectangle           		requesterBounds;

	/** Fit the image to the size of window, on resize. */
	private boolean						zoomFitToWindow; 

	/** The index of the selected tabbed. */
	private int							tabbedIndex;

	/** 
	 * Flag indicating to paint or not some textual information on top
	 * of the grid image.
	 */
	private boolean						textVisible;

	/** Flag indicating that a movie is played. */
	private boolean						playingMovie;
	
	/** Flag indicating that a movie is played. */
	private boolean						playingChannelMovie;

	/** Collection of history item. */
	private List<HistoryItem>			historyItems;

	/** 
	 * Flag indicating that a previous item replaced an existing one. 
	 * In that case, a new element is not added to the history list.
	 */
	private boolean						historyItemReplacement;

	/** The pixels set to copy the rendering settings from. */
	private Pixels 						pixels;

	/** 
	 * The index of the movie, not that we set to <code>-1</code>
	 * when the movie player is launched.
	 */
	private int 						movieIndex;
	
	/** The rendering setting related to a given set of pixels. */
	private Map							renderingSettings;
	
	/** The metadata viewer. */
	private MetadataViewer				metadataViewer;
	
	/** Flag indicating if the metadata are loaded. */
	private boolean						metadataLoaded;
	
	/** The ID of the last selected pixels set. */
	private long						currentPixelsID;
	
	/** Reference to the rendering control. */
	private RenderingControl    		currentRndControl;

	/** The rendering settings set by another user. */
	private RndProxyDef					alternativeSettings;
	
	/** The id of the selected user. */
	private long						selectedUserID;
	
	/** 
	 * Flag to compute the magnification factor when the image
	 * is set for the first time.
	 */
	private boolean						initMagnificationFactor;
	
	/** Computes the values of the {@link #sizeX} and {@link #sizeY} fields. */
	private void computeSizes()
	{
		if (sizeX == -1 && sizeY == -1) {
			sizeX = THUMB_MAX_WIDTH;
			sizeY = THUMB_MAX_HEIGHT;
			double x = sizeX/(double) getMaxX();
			double y =  sizeY/(double) getMaxY();
			if (x > y) factor = x;
			else factor = y;
			double ratio =  (double) getMaxX()/getMaxY();
			if (ratio < 1) sizeX *= ratio;
			else if (ratio > 1 && ratio != 0) sizeY *= 1/ratio;
		}
	}

	/** 
	 * Computes the magnification factor.
	 * 
	 * @return See above
	 */
	private double initZoomFactor()
	{
		if (initMagnificationFactor) return -1;
		int maxX = getMaxX();
		int maxY = getMaxY();
		initMagnificationFactor = true;
		if (maxX > IMAGE_MAX_WIDTH || maxY >IMAGE_MAX_HEIGHT) {
			double ratioX = (double) IMAGE_MAX_WIDTH/maxX;
			double ratioY = (double) IMAGE_MAX_HEIGHT/maxY;
			if (ratioX < ratioY) return ratioX;
			return ratioY;
		}
		return -1;
	}
	
	/**
	 * Creates a new object and sets its state to {@link ImViewer#NEW}.
	 * 
	 * @param image  	The image.
	 * @param bounds    The bounds of the component invoking the 
	 *                  {@link ImViewer}.
	 */
	ImViewerModel(ImageData image, Rectangle bounds)
	{
		this.image = image;
		requesterBounds = bounds;
		state = ImViewer.NEW;
		initMagnificationFactor = false;
		sizeX = sizeY = -1;
		zoomFitToWindow = false; 
		tabbedIndex = ImViewer.VIEW_INDEX;
		textVisible = true;
		movieIndex = -1;
		loaders = new HashMap<Integer, DataLoader>();
		metadataViewer = MetadataViewerFactory.getViewer(image, false);
		metadataLoaded = false;
		currentPixelsID = image.getDefaultPixels().getId();
		selectedUserID = -1;
	}

	/**
	 * Called by the <code>ImViewer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 * 
	 * @param component The embedding component.
	 */
	void initialize(ImViewer component)
	{ 
		this.component = component;
		browser = BrowserFactory.createBrowser(component, image.getId(), 
										ImViewerFactory.getPreferences());
	}
	
	/**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
	ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) ImViewerAgent.getRegistry().lookup(
				LookupNames.CURRENT_USER_DETAILS);
	}

	/**
	 * Sets the settings set by another user.
	 * 
	 * @param alternativeSettings The value to set.
	 * @param userID              The id of the user who set the settings.
	 */
	void setAlternativeSettings(RndProxyDef alternativeSettings, long userID)
	{
		this.alternativeSettings = alternativeSettings;
		selectedUserID = userID;
	}
	
	/**
	 * Returns the id of the owner of the alternatives settings.
	 * 
	 * @return See above.
	 */
	long getAlternativeSettingsOwnerId() { return selectedUserID; }

	/**
	 * Compares another model to this one to tell if they would result in
	 * having the same display.
	 *  
	 * @param other The other model to compare.
	 * @return <code>true</code> if <code>other</code> would lead to a viewer
	 *          with the same display as the one in which this model belongs;
	 *          <code>false</code> otherwise.
	 */
	boolean isSameDisplay(ImViewerModel other)
	{
		if (other == null) return false;
		return true;//;((other.pixelsID == pixelsID) && (other.imageID == imageID));
	}

	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
	String getImageName() { return image.getName(); }

	/**
	 * Returns the current state.
	 * 
	 * @return One of the flags defined by the {@link ImViewer} interface.  
	 */
	int getState() { return state; }

	/**
	 * Sets the object in the {@link ImViewer#DISCARDED} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void discard()
	{
		state = ImViewer.DISCARDED;
		//Shut down the service
		OmeroImageService svr = ImViewerAgent.getRegistry().getImageService();
		long pixelsID = image.getDefaultPixels().getId();
		svr.shutDown(pixelsID);
		svr.shutDownDataSink(pixelsID);
		Iterator i = loaders.keySet().iterator();
		Integer index;
		while (i.hasNext()) {
			index =  (Integer) i.next();
			(loaders.get(index)).cancel();
		}
		
		if (renderer != null) renderer.discard();
		if (player == null) return;
		player.setPlayerState(Player.STOP);
		player = null;
	}

	/**
	 * Returns the sizeX.
	 * 
	 * @return See above.
	 */
	int getMaxX() { return currentRndControl.getPixelsDimensionsX(); }

	/**
	 * Returns the sizeY.
	 * 
	 * @return See above.
	 */
	int getMaxY() { return currentRndControl.getPixelsDimensionsY(); }

	/**
	 * Returns the maximum number of z-sections.
	 * 
	 * @return See above.
	 */
	int getMaxZ() { return currentRndControl.getPixelsDimensionsZ()-1; }

	/**
	 * Returns the maximum number of timepoints.
	 * 
	 * @return See above.
	 */
	int getMaxT() { return currentRndControl.getPixelsDimensionsT()-1; }

	/**
	 * Returns the currently selected z-section.
	 * 
	 * @return See above.
	 */
	int getDefaultZ() { return currentRndControl.getDefaultZ(); }

	/**
	 * Returns the currently selected timepoint.
	 * 
	 * @return See above.
	 */
	int getDefaultT() { return currentRndControl.getDefaultT(); }

	/**
	 * Returns the currently selected color model.
	 * 
	 * @return See above.
	 */
	String getColorModel() { return currentRndControl.getModel(); }
	
	/**
	 * Returns an array of <code>ChannelData</code> object.
	 * 
	 * @return See above.
	 */
	ChannelMetadata[] getChannelData()
	{ 
		return currentRndControl.getChannelData();
	}

	/**
	 * Returns the <code>ChannelData</code> object corresponding to the
	 * given index.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	ChannelMetadata getChannelData(int index)
	{ 
		return currentRndControl.getChannelData(index);
	}

	/**
	 * Returns the color associated to a channel.
	 * 
	 * @param w The OME index of the channel.
	 * @return See above.
	 */
	Color getChannelColor(int w) { return currentRndControl.getRGBA(w); }

	/**
	 * Returns <code>true</code> if the channel is mapped, <code>false</code>
	 * otherwise.
	 * 
	 * @param w	The channel's index.
	 * @return See above.
	 */
	boolean isChannelActive(int w) { return currentRndControl.isActive(w); }
	
	/** 
	 * Fires an asynchronous retrieval of the rendering control. 
	 * 
	 * @param pixelsID The id of the pixels set to load.
	 */
	void fireRenderingControlLoading(long pixelsID)
	{
		currentPixelsID = pixelsID;
		DataLoader loader = new RenderingControlLoader(component, pixelsID, 
												RenderingControlLoader.LOAD);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}

	/** Fires an asynchronous retrieval of the rendering control. */
	void fireRenderingControlReloading()
	{
		DataLoader loader = new RenderingControlLoader(component, 
									image.getDefaultPixels().getId(), 
										RenderingControlLoader.RELOAD);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}

	/** Fires an asynchronous retrieval of the rendering control. */
	void fireRenderingControlResetting()
	{
		DataLoader loader = new RenderingControlLoader(component, 
										image.getDefaultPixels().getId(), 
										RenderingControlLoader.RESET);
		loader.load();
		if (loaders.get(RND) != null)
			loaders.get(RND).cancel();
		loaders.put(RND, loader);
		state = ImViewer.LOADING_RENDERING_CONTROL;
	}
	
	/** Fires an asynchronous retrieval of the rendered image. */
	void fireImageRetrieval()
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		state = ImViewer.LOADING_IMAGE;
		//OmeroImageService os = ImViewerAgent.getRegistry().getImageService();
		try {
			//component.setImage(os.renderImage(pixelsID, pDef));
			component.setImage(currentRndControl.renderPlane(pDef));
		} catch (Exception e) {
			component.reload(e);
		}
	}

	/**
	 * This method should only be invoked when we save the displayed image
	 * and split its components.
	 * 
	 * @return See above.
	 */
	BufferedImage getSplitComponentImage()
	{
		PlaneDef pDef = new PlaneDef();
		pDef.t = getDefaultT();
		pDef.z = getDefaultZ();
		pDef.slice = omero.romio.XY.value;
		//state = ImViewer.LOADING_IMAGE;
		try {
			return currentRndControl.renderPlane(pDef);
		} catch (Exception e) {
			component.reload(e);
		}
		return null;
	}

	/**
	 * Sets the rendering control.
	 * 
	 * @param rndControl 	The object to set.
	 */
	void setRenderingControl(RenderingControl rndControl)
	{
		loaders.remove(RND);
		this.currentRndControl = rndControl;
		
		if (renderer == null) {
			renderer = RendererFactory.createRenderer(component, rndControl,
					ImViewerFactory.getPreferences());
			state = ImViewer.RENDERING_CONTROL_LOADED;
			double f = initZoomFactor();
			if (f > 0)
				browser.initializeMagnificationFactor(f);
			try {
				if (alternativeSettings != null)
					currentRndControl.resetSettings(alternativeSettings);
				alternativeSettings = null;
			} catch (Exception e) {}
		} else {
			renderer.setRenderingControl(rndControl);
		}
	} 

	/**
	 * Returns the {@link Browser}.
	 * 
	 * @return See above.
	 */
	Browser getBrowser() { return browser; }

	/**
	 * Sets the zoom factor.
	 * 
	 * @param factor The factor to set.
	 * @param reset	 Pass <code>true</code> to reset the magnification factor.
     * 				 <code>false</code> to set it.
	 */
	void setZoomFactor(double factor, boolean reset)
	{ 
		browser.setZoomFactor(factor, reset);
	}

	/**
	 * Returns the zoom factor.
	 * 
	 * @return The factor to set.
	 */
	double getZoomFactor() { return browser.getZoomFactor(); }

	/**
	 * This method determines if the browser image should be resized to fit 
	 * the window size if the window is resized. 
	 * 
	 * @param option see above.
	 */
	void setZoomFitToWindow(boolean option) { zoomFitToWindow = option; }

	/**
	 * This method determines if the browser image should be resized to fit 
	 * the window size if the window is resized.
	 *  
	 * @return <code>true</code> if image should resize on window resize. 
	 */ 
	boolean getZoomFitToWindow() { return zoomFitToWindow; }

	/**
	 * Sets the retrieved image, returns the a magnification or <code>-1</code>
	 * if no magnification factor computed. 
	 * 
	 * @param image The image to set.
	 * @return See above.
	 */
	double setImage(BufferedImage image)
	{
		state = ImViewer.READY; 
		browser.setRenderedImage(image);
		//update image icon
		computeSizes();
		imageIcon = Factory.magnifyImage(factor, image);
		return initZoomFactor();
	}

	/**
	 * Returns <code>true</code> if the magnification factor was set 
	 * when the image was first loaded, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isInitMagnificationFactor() { return initMagnificationFactor; }
	
	/**
	 * Sets the color model.
	 * 
	 * @param colorModel	The color model to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setColorModel(String colorModel)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (ImViewer.GREY_SCALE_MODEL.equals(colorModel))
			currentRndControl.setModel(colorModel);
		else if (ImViewer.RGB_MODEL.equals(colorModel) ||
				(ImViewer.HSB_MODEL.equals(colorModel)))
			currentRndControl.setModel(ImViewer.HSB_MODEL);
	}

	/**
	 * Sets the selected plane.
	 * 
	 * @param z The z-section to set.
	 * @param t The timepoint to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setSelectedXYPlane(int z, int t)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (t >= 0 && t != getDefaultT()) currentRndControl.setDefaultT(t);
		if (z >= 0 && z != getDefaultZ()) currentRndControl.setDefaultZ(z);
	}

	/**
	 * Sets the color for the specified channel.
	 * 
	 * @param index The channel's index.
	 * @param c     The color to set.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelColor(int index, Color c)
		throws RenderingServiceException, DSOutOfServiceException
	{
		currentRndControl.setRGBA(index, c);
	}

	/**
	 * Sets the channel active.
	 * 
	 * @param index The channel's index.
	 * @param b     Pass <code>true</code> to select the channel,
	 *              <code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setChannelActive(int index, boolean b)
		throws RenderingServiceException, DSOutOfServiceException
	{
		currentRndControl.setActive(index, b);
	}  

	/**
	 * Returns the number of channels.
	 * 
	 * @return See above.
	 */
	int getMaxC() { return currentRndControl.getPixelsDimensionsC(); }

	/** 
	 * Returns the number of active channels.
	 * 
	 * @return See above.
	 */
	int getActiveChannelsCount() { return getActiveChannels().size(); }

	/**
	 * Returns a list of active channels.
	 * 
	 * @return See above.
	 */
	List<Integer> getActiveChannels()
	{
		return currentRndControl.getActiveChannels();
	}

	/** 
	 * Starts the channels movie player, invokes in the event-dispatcher 
	 * thread for safety reason.
	 * 
	 * @param play  Pass <code>true</code> to play the movie, <code>false</code>
	 *              to stop it.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void playMovie(boolean play)
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (player != null && !play) {
			player.setPlayerState(Player.STOP);
			List l = player.getChannels();
			if (l != null) {
				Iterator i = l.iterator();
				while (i.hasNext()) 
					setChannelActive(((Integer) i.next()).intValue(), true);
			}
			player = null;
			state = ImViewer.READY;
			playingChannelMovie = false;
			return;
		}
		playingChannelMovie = true;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				player = new ChannelPlayer(component);
				player.setPlayerState(Player.START);
			}
		});
		state = ImViewer.CHANNEL_MOVIE;
	}

	/**
	 * Returns <code>true</code> if playing movie across channels,
	 * <code>false</code> oherwise.
	 * 
	 * @return See above.
	 */
	boolean isPlayingChannelMovie() { return playingChannelMovie; }
	
	/**
	 * Sets the state.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; };
	
	/**
	 * Returns the {@link Renderer}.
	 * 
	 * @return See above.
	 */
	Renderer getRenderer() { return renderer; }

	/**
	 * Returns the displayed image.
	 * 
	 * @return See above.
	 */
	BufferedImage getDisplayedImage() { return browser.getDisplayedImage(); }

	/**
	 * Returns the original image returned by the image service.
	 * 
	 * @return See above.
	 */
	BufferedImage getOriginalImage() { return browser.getRenderedImage(); }

	/**
	 * Returns the image displayed in the grid view.
	 * 
	 * @return See above.
	 */
	BufferedImage getGridImage() { return browser.getGridImage(); }

	/**
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeX() { return currentRndControl.getPixelsSizeX(); }

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeY() { return currentRndControl.getPixelsSizeY(); }

	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	float getPixelsSizeZ() { return currentRndControl.getPixelsSizeZ(); }

	/**
	 * Returns <code>true</code> if the unit bar is painted on top of 
	 * the displayed image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUnitBar() { return browser.isUnitBar(); }

	/**
	 * Returns an iconified version of the displayed image.
	 * 
	 * @return See above.
	 */
	BufferedImage getImageIcon() { return imageIcon; }

	/**
	 * Returns the bounds of the component invoking the {@link ImViewer},
	 * or <code>null</code> if not available.
	 * 
	 * @return See above.
	 */
	Rectangle getRequesterBounds() { return requesterBounds; }

	/**
	 * Returns the ID of the pixels set.
	 * 
	 * @return See above.
	 */
	long getPixelsID() { return currentPixelsID; }

	/**
	 * Returns the index of the selected tabbed.
	 * 
	 * @return See above.
	 */
	int getTabbedIndex() { return tabbedIndex; }

	/**
	 * Sets the tabbed index.
	 * 
	 * @param index The value to set.
	 */
	void setTabbedIndex(int index) { tabbedIndex = index; }

	/**
	 * Returns a 3-dimensional array of boolean value, one per color band.
	 * The first (resp. second, third) element is set to <code>true</code> 
	 * if an active channel is mapped to <code>RED</code> (resp. 
	 * <code>GREEN</code>, <code>BLUE</code>), to <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	boolean[] hasRGB()
	{
		boolean[] rgb = new boolean[3];
		rgb[0] = currentRndControl.hasActiveChannelRed();
		rgb[1] = currentRndControl.hasActiveChannelGreen();
		rgb[2] = currentRndControl.hasActiveChannelBlue();
		return rgb;
	}

	/**
	 * Returns <code>true</code> if the textual information is painted on 
	 * top of the grid image, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isTextVisible() { return textVisible; }

	/**
	 * Sets to <code>true</code> if the textual information is painted on 
	 * top of the grid image, <code>false</code> otherwise.
	 * 
	 * @param textVisible The value to set.
	 */
	void setTextVisible(boolean textVisible) { this.textVisible = textVisible; }

	/**
	 * Returns the image displayed in the annotator view.
	 * 
	 * @return See above.
	 */
	BufferedImage getAnnotateImage() { return browser.getAnnotateImage(); }

	/**
	 * Returns the ID of the viewed image.
	 * 
	 * @return See above.
	 */
	long getImageID() { return image.getId(); }

	/** 
	 * Saves the rendering settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void saveRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		if (currentRndControl != null) {
			RndProxyDef def = currentRndControl.saveCurrentSettings(); 
			if (def != null) {
				if (renderingSettings != null) 
					renderingSettings.put(ImViewerAgent.getUserDetails(), def);
			}
		}
	}

	/**
	 * Sets to <code>true</code> when the movie is played, to <code>false</code>
	 * otherwise.
	 * 
	 * @param play  The value to set.
	 * @param index The movie index.
	 */
	void setPlayingMovie(boolean play, int index)
	{ 
		playingMovie = play; 
		movieIndex = index;
	}

	/**
	 * Returns <code>true</code> if a movie is played, to <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isPlayingMovie() { return playingMovie; }

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>RED</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelRed(int index)
	{
		return currentRndControl.isChannelRed(index);
	}

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>GREEN</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelGreen(int index)
	{
		return currentRndControl.isChannelGreen(index);
	}

	/**
	 * Returns <code>true</code> if the channel is mapped
	 * to <code>BLUE</code>, <code>false</code> otherwise.
	 * 
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelBlue(int index)
	{
		return currentRndControl.isChannelBlue(index);
	}

	/**
	 * Returns a collection of pairs 
	 * (active channel's index, active channel's color).
	 * 
	 * @return See above.
	 */
	Map getActiveChannelsMap()
	{
		List l = getActiveChannels();
		Map<Integer, Color> m = new HashMap<Integer, Color>(l.size());
		Iterator i = l.iterator();
		Integer index;
		while (i.hasNext()) {
			index = (Integer) i.next();
			m.put(index, getChannelColor(index.intValue()));
		}
		return m;
	}

	/** 
	 * Creates a new history item and adds it to the list of elements.
	 * Returns the newly created item.
	 * 
	 *  @return See above.
	 */
	HistoryItem createHistoryItem()
	{
		//Make a smaller image
		BufferedImage img = browser.getRenderedImage();
		double ratio = 1;
		int w = img.getWidth();
		int h = img.getHeight();
		if (w < ImViewer.MINIMUM_SIZE || h < ImViewer.MINIMUM_SIZE) ratio = 1;
		else {
			if (w >= h) ratio = (double) ImViewer.MINIMUM_SIZE/w;
			else ratio = (double) ImViewer.MINIMUM_SIZE/h;
		}
		BufferedImage thumb = Factory.magnifyImage(ratio, img);
		HistoryItem i = new HistoryItem(currentRndControl.getRndSettingsCopy(), 
										thumb);
		if (historyItems == null) historyItems = new ArrayList<HistoryItem>();
		historyItems.add(i);
		return i;
	}

	/**
	 * Returns the first history item.
	 * 
	 * @return See above.
	 */
	HistoryItem getFirstHistoryItem()
	{
		if (historyItems == null) return null;
		return historyItems.get(0);
	}
	
	/**
	 * Removes the item from the list.
	 * 
	 * @param node The node to remove.
	 */
	void removeHistoryItem(HistoryItem node)
	{
		if (historyItems != null) historyItems.remove(node);
	}

	/** Removes the last item from the list. */
	void removeLastHistoryItem()
	{
		if (historyItems != null && historyItems.size() > 1) {
			//historyItems.remove(historyItems.size()-1);
		}
	}
	
	/** Clears the history. */
	void clearHistory()
	{
		if (historyItems != null) {
			HistoryItem node = historyItems.get(0);
			historyItems.clear();
			historyItems.add(node);
		}
		historyItemReplacement = false;
	}

	/**
	 * Returns <code>true</code> if an history item has been added to the 
	 * list when the user is swapping between rendering settings, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isHistoryItemReplacement() { return historyItemReplacement; }

	/**
	 * Sets the value of the {@link #historyItemReplacement} flag.
	 * 
	 * @param b The value to set.
	 */
	void setHistoryItemReplacement(boolean b)
	{
		historyItemReplacement = b;
	}

	/**
	 * Returns the collection of history items.
	 * 
	 * @return See above.
	 */
	List getHistory() { return historyItems; }

	/**
	 * Partially resets the rendering settings.
	 * 
	 * @param settings  The value to set.
	 * @param reset		Pass <code>true</code> to reset the controls, 
	 * 					<code>false</code> otherwise.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetMappingSettings(RndProxyDef settings, boolean reset) 
		throws RenderingServiceException, DSOutOfServiceException
	{
		//rndControl.resetMappingSettings(settings);
		currentRndControl.resetSettings(settings);
		if (reset) renderer.resetRndSettings();
	}

	/**
	 * Resets the rendering settings.
	 * Returns <code>true</code> if it is possible to copy the rendering 
	 * settings, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 * @throws DSAccessException  			If the data cannot be retrieved.
	 */
	boolean resetSettings() 
		throws RenderingServiceException, DSOutOfServiceException,
				DSAccessException
	{
		//First check that the pixels are compatible
		if (pixels == null) {
			OmeroImageService 
			os = ImViewerAgent.getRegistry().getImageService();
			pixels = os.loadPixels(ImViewerFactory.getRefPixelsID());
		}
		if (currentRndControl.validatePixels(pixels)) {
			currentRndControl.resetSettings(
					ImViewerFactory.getRenderingSettings());
			renderer.resetRndSettings();
			return true;
		}
		return false;
	}

	/** 
	 * Resets the default settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void resetDefaultRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{ 
		currentRndControl.resetDefaults(); 
	}
	
	/** 
	 * Sets the original default settings. 
	 * 
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setOriginalRndSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{ 
		currentRndControl.setOriginalRndSettings(); 
	}
	
	/** Sets the reference to the pixels set to <code>null</code>. */
	void copyRndSettings() { pixels = null; }

	/**
	 * Returns <code>true</code> if we have rendering settings to paste,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasRndToPaste() 
	{ 
		return (ImViewerFactory.getRenderingSettings() != null); 
	}

	/** Posts a {@link CopyRndSettings} event. */
	void copyRenderingSettings()
	{
		CopyRndSettings evt = new CopyRndSettings(
				image.getDefaultPixels().getId(), 
				currentRndControl.getRndSettingsCopy());
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		bus.post(evt);
	}

	/**
	 * Returns the movie index.
	 * 
	 * @return See above.
	 */
	int getMovieIndex() { return movieIndex; }
	
	/**
	 * Returns <code>true</code> if the image is compressed, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImageCompressed() { return currentRndControl.isCompressed(); }
	
	/**
	 * Sets the compressiong level.
	 * 
	 * @param compressionLevel 	One of the compression level defined by 
	 * 							{@link RenderingControl} I/F.
	 */
	void setCompressionLevel(int compressionLevel)
	{
		currentRndControl.setCompression(compressionLevel);
		component.renderXYPlane();
	}

	/**
	 * Returns the compression level.
	 * 
	 * @return See above.
	 */
	int getCompressionLevel()
	{
		return currentRndControl.getCompressionLevel();
	}
	
	/**
	 * Fires an asynchronous retrieval of the rendering settings 
	 * linked to the currently viewed set of pixels.
	 */
	void fireRenderingSettingsRetrieval()
	{
		DataLoader loader = new RenderingSettingsLoader(component, 
						image.getDefaultPixels().getId());
		loader.load();
		if (loaders.get(SETTINGS) != null)
			loaders.get(SETTINGS).cancel();
		loaders.put(SETTINGS, loader);
	}
	
	/**
	 * Returns the rendering settings linked to the currently viewed set
	 * of pixels.
	 * 
	 * @return See above.
	 */
	Map getRenderingSettings() { return renderingSettings; }
	
	/** 
	 * Sets the rendering settings linked to the currently viewed set
	 * of pixels.
	 * 
	 * @param map The map to set.
	 */
	void setRenderingSettings(Map map)
	{
		renderingSettings = map;
		loaders.remove(SETTINGS);
	}

	/**
	 * Applies the settings set by the selected user.
	 * 
	 * @param exp	The user to handle.
	 * @throws RenderingServiceException 	If an error occured while setting 
	 * 										the value.
	 * @throws DSOutOfServiceException  	If the connection is broken.
	 */
	void setUserSettings(ExperimenterData exp)
		throws RenderingServiceException, DSOutOfServiceException
	{
		RndProxyDef rndDef = (RndProxyDef) renderingSettings.get(exp);
		currentRndControl.resetSettings(rndDef);
	}
	
	/**
	 * Returns the id of the owner of the image.
	 * 
	 * @return See above.
	 */
	long getOwnerID() { return image.getOwner().getId(); }

	/**
	 * Returns the metadata viewer.
	 * 
	 * @return See above.
	 */
	MetadataViewer getMetadataView() { return metadataViewer; }

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasMetadataToSave() { return metadataViewer.hasDataToSave(); }

	/** Saves the data. */
	void saveMetadata() { metadataViewer.saveData(); }

	/** Loads the data. */
	void loadMetadata()
	{ 
		if (!metadataLoaded) {
			metadataLoaded = true;
			metadataViewer.activate(); 
		}
	}

	/** Shows the image details. */
	void showImageDetails()
	{
		 if (metadataViewer != null)  metadataViewer.showImageInfo();
	}
	
	 /**
     * Returns the collection of pixels sets linked to the image.
     * 
     * @return See above.
     */
    List<PixelsData> getPixelsSets() { return image.getAllPixels(); }

    /**
     * Returns the collection of pixels ID.
     * 
     * @return See above.
     */
    List<Long> getPixelsIDs()
    {
    	Iterator<PixelsData> i = getPixelsSets().iterator();
    	List<Long> ids = new ArrayList<Long>();
    	while (i.hasNext()) 
			ids.add(i.next().getId());
		return ids;
    }

    /** Resets the history when switching to a new rendering control.*/
	void resetHistory() { historyItems = null; }

	/**
	 * Starts an asynchronous call to render a preview of the projected image.
	 * 
	 * @param ref Object with the projection's parameters.
	 */
	void fireRenderProjected(ProjectionRef ref)
	{
		ProjectionParam param = new ProjectionParam(getPixelsID(), 
				ref.getStartZ(), ref.getEndZ(), ref.getStepping(), 
				ref.getType());
		param.setChannels(ref.getChannels());
		ProjectionSaver loader = new ProjectionSaver(component, param, 
				                  ProjectionSaver.PREVIEW);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous call to project image.
	 * 
	 * @param ref Object with the projection's parameters.
	 */
	void fireProjectImage(ProjectionRef ref)
	{
		List<Integer> channels = ref.getChannels();
		ProjectionParam param = new ProjectionParam(getPixelsID(), 
				ref.getStartZ(), ref.getEndZ(), ref.getStepping(), 
				ref.getType(), ref.getStartT(), ref.getEndT(), 
				channels, ref.getImageName());
		param.setDescription(ref.getImageDescription());
		param.setDatasets(ref.getDatasets());
		ProjectionSaver loader = new ProjectionSaver(component,  param, 
							ProjectionSaver.PROJECTION);
		loader.load();
	}
	
	/**
	 * Starts an asynchronous retrieval of the containers containing the 
	 * image.
	 */
	void fireContainersLoading()
	{
		ContainerLoader loader = new ContainerLoader(component, getImageID());
		loader.load();
	}
    
	/**
	 * Returns the type of pixels.
	 * 
	 * @return See above.
	 */
	String getPixelsType() { return image.getDefaultPixels().getPixelType(); }

	/**
	 * Starts an asynchronous creation of the rendering settings
	 * for the pixels set.
	 * 
	 * @param indexes	The indexes of the projected channels.
	 * @param image 	The projected image.
	 */
	void firePojectedRndSettingsCreation(List<Integer> indexes, ImageData image)
	{
		RndProxyDef def = currentRndControl.getRndSettingsCopy();
		RenderingSettingsCreator l = new RenderingSettingsCreator(component, 
				image, def, indexes);
		l.load();
	}
	
}
