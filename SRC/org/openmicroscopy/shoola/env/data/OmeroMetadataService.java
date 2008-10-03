/*
 * org.openmicroscopy.shoola.env.data.OmeroMetadataService 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data;



//Java imports
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import pojos.AnnotationData;
import pojos.DataObject;

/** 
 * List of methods to retrieve metadata.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface OmeroMetadataService
{

	/** Indicates to retrieve the tags. */
	public static final int LEVEL_TAG = 0;
	
	/** Indicates to retrieve the tag sets. */
	public static final int LEVEL_TAG_SET = 1;
	
	/** Indicates to retrieve the tag sets and the tags. */
	public static final int LEVEL_ALL = 2;
	
	/**
	 * Retrieves the textual annotations
	 * 
	 * @param type		The type of object the annotations are related to.
	 * @param id		The id of the object.
	 * @param userID	The id of the user, or <code>-1</code> if no user
	 * 					specified. 
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Collection loadTextualAnnotations(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the tags linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who tagged the object or 
     * 					<code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTags(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the attachments linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadAttachments(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the urls linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadUrls(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the ratings linked to an object identifying by the specified
	 * type and id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadRatings(Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads all annotations related to the object specified by the class
	 * type and the id.
	 * 
	 * @param type 		The type of the object.
     * @param id		The id of the object.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadStructuredAnnotations(Class type, long id, 
												long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads data related to the specified object
	 * 
	 * @param object 	The object to handle.
     * @param userID	The id of the user who added attachments to the object 
     * 					or <code>-1</code> if the user is not specified.
     * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public StructuredDataResults loadStructuredData(DataObject object, 
													long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads the collection of objects containing information about the 
	 * user who viewed the image i.e. rating, rendering settings.
	 * 
	 * @param imageID	The id of the image.
	 * @param pixelsID	The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadViewedBy(long imageID, long pixelsID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the specified data object and returns the annotated object.
	 * 
	 * @param toAnnotate	The object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotation 	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject annotate(DataObject toAnnotate, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the object and returns the annotated object.
	 * 
	 * @param type  		The type of object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param id			The id of the object to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotation 	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject annotate(Class type, long id, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Annotates the specified data objects.
	 * 
	 * @param toAnnotate	The collection of objects to annotate. 
	 * 						Mustn't be <code>null</code>.
	 * @param annotation 	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public List<DataObject> annotate(Set<DataObject> toAnnotate, 
									AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Removes the specified annotation from the object.
	 * Returns the updated object.
	 * @param annotation	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @param object		The object to handle. Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public DataObject removeAnnotation(AnnotationData annotation, 
									DataObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Removes all annotations of a given type from the specified object.
	 * 
	 * @param object			The object to handle. 
	 * 							Mustn't be <code>null</code>.
	 * @param annotationType	The type of annotation to clear.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(DataObject object, Class annotationType)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Removes all annotations from the specified object.
	 * 
	 * @param object	The object to handle. Mustn't be <code>null</code>.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(DataObject object)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Clears the annotation related to a given type object.
	 * 
	 * @param type				The type of object the annotations are 
	 * 							related to. 
	 * @param id				The object's id.
	 * @param annotationType	The type of annotation to delete.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public void clearAnnotation(Class type, long id, Class annotationType)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Loads all annotations of a given type.
	 * 
	 * @param annotationType 	The type of annotation to retrieve.
	 * @param objectType		Restrict search to a given type of object.
	 * @param objectID			Resctrict the search to a given object,
	 * 							Pass <code>-1</code> if no object taken into
	 * 							account.
	 * @param userID			The id of the user the annotations are related
	 * 							to.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadAnnotations(Class annotationType, Class objectType, 
									long objectID, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Saves the object, adds (resp. removes) annotations to(resp. from)
	 * the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveData(Collection<DataObject> data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(Collection<DataObject> data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Saves the objects contained in the specified objects, 
	 * adds (resp. removes) annotations to(resp. from) the object if any.
	 * 
	 * @param data		The data object to handle.
	 * @param toAdd		Collection of annotations to add.
	 * @param toRemove	Collection of annotations to remove.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Object saveBatchData(TimeRefObject data, 
							List<AnnotationData> toAdd, 
							List<AnnotationData> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param file		The file to write the data into.
	 * @param fileID	The id of the file to download.
	 * @param size		The size of the file to download
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public File downloadFile(File file, long fileID, long size)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * 
	 * @param nodeType
	 * @param nodeIds
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Map<Long, Collection> loadRatings(Class nodeType, 
			List<Long> nodeIds, long userID) 
			throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
	 * @param nodeType
	 * @param nodeIds
	 * @param annotationType
	 * @param terms
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotation(Class nodeType, List<Long> nodeIds, 
		Class annotationType, List<String> terms, long userID) 
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * Returns a sub-collection of the passed collection of nodes
	 * annotated by the passed type of annotation.
	 * 
	 * @param nodeType
	 * @param nodeIds
	 * @param annotationType
	 * @param annotated
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotated(Class nodeType, List<Long> nodeIds, 
		Class annotationType, boolean annotated, long userID) 
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * 
	 * @param nodeType
	 * @param ids
	 * @param filter
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection filterByAnnotation(Class nodeType, List<Long> ids, 
										FilterContext filter, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * 
	 * @param id
	 * @param images
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTagsContainer(Long id, boolean images, long userID)
		throws DSOutOfServiceException, DSAccessException;

	/**
	 * 
	 * @param id
	 * @param images
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTagSetsContainer(Long id, boolean images,
										long userID)
		throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Loads the tags linked to images or without links if the passed level
	 * equals {@link #LEVEL_TAG}, loads the tags linked to other tags or without
	 * links if the passed level is {@link #LEVEL_TAG_SET}.
	 * 
	 * @param level  Either {@link #LEVEL_TAG} or {@link #LEVEL_TAG_SET}.
	 * @param userID The id of the user who owns the tag or tag sets.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                   in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	public Collection loadTags(int level, long userID)
		throws DSOutOfServiceException, DSAccessException;
	
}
