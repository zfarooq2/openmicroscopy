/*
 * org.openmicroscopy.shoola.env.data.views.calls.ImagesLoader
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

package org.openmicroscopy.shoola.env.data.views.calls;



//Java imports
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DatasetData;
import pojos.ImageData;


/** 
 * Command to retrieve the images contained in the specified containers.
 * The containers can either be <code>Dataset</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImagesLoader
    extends BatchCallTree
{
	
    /** The results of the call. */
    private Set         results;
    
    /** Loads the specified tree. */
    private BatchCall   loadCall;

    /**
     * Creates a {@link BatchCall} to retrieve the user images.
     * 
     * @param userID	The ID of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long userID)
    {
        return new BatchCall("Loading user's images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.getExperimenterImages(userID);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the images contained 
     * in the container specified by the set of IDs.
     * 
     * @param nodeType  	The type of the node.
     * @param nodeIDs   	A set of the IDs of top-most containers.
     * @param userID		The Id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeImagesInContainerBatchCall(final Class nodeType,
                                        			final List nodeIDs,
                                        			final long userID)
    {
        return new BatchCall("Loading container tree: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
                results = os.getImages(nodeType, nodeIDs, userID);
            }
        };
    }
    
    /**
     * Creates a a {@link BatchCall} to retrieve images before or after
     * a given date depending on the passed parameter.
     * 
     * @param startTime The timestamp identifying the lower bound.
     * @param endTime	The timestamp identifying the date.
     * @param userID	The Id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final Timestamp startTime, 
    						final Timestamp endTime, final long userID)
    {
        return new BatchCall("Loading images: ") {
            public void doCall() throws Exception
            {
                OmeroDataService os = context.getDataService();
				results =  os.getImagesPeriod(startTime, endTime, userID);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * 
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the root node of the requested tree.
     * 
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return results; }
    
    /** 
     * Creates a new instance. 
     * 
     * @param rootLevelID	The ID of the user.
     */
    public ImagesLoader(long rootLevelID)
    {
        loadCall = makeBatchCall(rootLevelID);
    }

    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
     * 
     * @param nodeType	The type of the root node. Can only be one out of:
     * 					{@link DatasetData}.
     * @param nodeIDs	A set of the IDs of top-most containers.
     * @param userID	The Id of the user.
     */
    public ImagesLoader(Class nodeType, List nodeIDs, long userID)
    {
        if (nodeType == null) 
            throw new IllegalArgumentException("No node type.");
        if (nodeIDs == null || nodeIDs.size() == 0)
            throw new IllegalArgumentException("Collection of node ID" +
                                                " not valid.");
        
        if (nodeType.equals(DatasetData.class) || 
            nodeType.equals(ImageData.class))
            loadCall = makeImagesInContainerBatchCall(nodeType, nodeIDs, 
                    									userID);
        else throw new IllegalArgumentException("Unsupported type: "+
                nodeType);
    }
    
    /**
     * Creates a new instance. If bad arguments are passed, we throw a runtime
	 * exception so to fail early and in the call.
	 * 
     * @param startTime The timestamp identifying the start of a period.
     * @param endTime	The timestamp identifying the date.
     * @param userID	The Id of the user.
     */
    public ImagesLoader(Timestamp startTime, Timestamp endTime, long userID)
    {
    	loadCall = makeBatchCall(startTime, endTime, userID);
    }
    
}
