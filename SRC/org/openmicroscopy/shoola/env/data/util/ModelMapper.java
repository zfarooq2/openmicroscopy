/*
 * org.openmicroscopy.shoola.env.data.util.ModelMapper
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

package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import ome.util.Filter;
import ome.util.Filterable;
import omero.RLong;
import omero.RString;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetAnnotationLinkI;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenAnnotationLinkI;
import omero.model.ScreenI;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TextAnnotation;
import omero.model.TextAnnotationI;
import omero.model.UrlAnnotation;
import omero.model.UrlAnnotationI;
import omero.model.Well;
import omero.model.WellAnnotationLink;
import omero.model.WellAnnotationLinkI;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

/** 
 * Helper class to map {@link DataObject}s into their corresponding
 * {@link IObject}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ModelMapper
{
   
    /**
     * Helper field used to unlink an <code>IObject</code> and its related
     * collections.
     */
    private static Filter unloader = new CollectionUnloader();
    
    /**
     *  Utility inner class to unload all collections linked to a given 
     *  <code>IObject</code> e.g. a {@link Project} linked to its
     *  {@link Dataset}s.
     */
    private static class CollectionUnloader
        implements Filter
    {
    	
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Filterable)
         */
        public Filterable filter(String arg0, Filterable arg1) { return arg1; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Collection)
         */
        public Collection filter(String arg0, Collection arg1) { return null; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Map)
         */
        public Map filter(String arg0, Map arg1) { return arg1; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Object)
         */
        public Object filter(String arg0, Object arg1) { return arg1; }
        
    }
    
    /**
     * Unlinks the collections linked to the specified {@link IObject}.
     * 
     * @param object The object.
     */
    public static void unloadCollections(IObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("The object mustn't be null.");
        //TODO: check what to do
        //object.acceptFilter(unloader);
    }

    /**
     * Unlinks the specified child from its parent and returns the link to 
     * remove.
     * 
     * @param child     The child to unlink.
     * @param parent    The child's parent.
     * @return See above
     */
    public static IObject unlinkChildFromParent(IObject child, IObject parent)
    {
        if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Image) child).copyDatasetLinks();
            Iterator i = links.iterator();
            DatasetImageLink link = null;
            long parentID = parent.getId().val;
            while (i.hasNext()) {
                link = (DatasetImageLink) i.next();
                if (link.getParent().getId().val == parentID) 
                	break;  
            }
            return link;
        } else if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Project) parent).copyDatasetLinks();
            Iterator i = links.iterator();
            ProjectDatasetLink link = null;
            long childID = child.getId().val;
            while (i.hasNext()) {
                link = (ProjectDatasetLink) i.next();
                if (link.getChild().getId().val == childID) {
                    return link;  
                }
            }
            //return link;
        } 
        throw new IllegalArgumentException("Parent not supported.");
    }

    /**
     * Links the  {@link IObject child} to its {@link IObject parent}.
     * 
     * @param child     The child. 
     * @param parent    The parent.
     * @return The link.
     */
    public static IObject linkParentToChild(IObject child, IObject parent)
    {
        if (parent == null) return null;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project unloadedProject = new ProjectI(parent.getId().val, false);
            Dataset unloadedDataset = new DatasetI(child.getId().val, false);
            ProjectDatasetLink l = new ProjectDatasetLinkI();
            l.link(unloadedProject, unloadedDataset);
            return l;
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset unloadedDataset = new DatasetI(parent.getId().val, false);
            Image unloadedImage = new ImageI(child.getId().val, false);
            
            DatasetImageLink l = new DatasetImageLinkI();
            l.link(unloadedDataset, unloadedImage);
            return l;
        } else if (parent instanceof Screen) {
            if (!(child instanceof Plate))
                throw new IllegalArgumentException("Child not valid.");
            Screen unloadedScreen = new ScreenI(parent.getId().val, false);
            Plate unloadedPlate = new PlateI(child.getId().val, false);
            
            ScreenPlateLink l = new ScreenPlateLinkI();
            l.link(unloadedScreen, unloadedPlate);
            return l;
        }
        return null;
    }
    
    /**
     * Links the newly created {@link IObject child} to its
     * {@link IObject parent}. This method should only be invoked to add a 
     * newly created child.
     * 
     * @param child     The newly created child. 
     * @param parent    The parent of the newly created child.
     */
    public static void linkParentToNewChild(IObject child, IObject parent)
    {
        if (parent == null) return;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        List l;
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project p = (Project) parent;
            Dataset d = (Dataset) child;
            
            l = d.copyProjectLinks();
            if (l == null) return;
            ProjectDatasetLink link;
            Iterator it = l.iterator();
            long id = p.getId().val;
            while (it.hasNext()) {
                link = (ProjectDatasetLink) it.next();
                if (id == link.getParent().getId().val)
                	p.addProjectDatasetLink2(link, false);
            }
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset p = (Dataset) parent;
            Image d = (Image) child;
            l = d.copyDatasetLinks();
            if (l == null) return;
            DatasetImageLink link;
            Iterator it = l.iterator();
            long id = p.getId().val;
            while (it.hasNext()) {
                link = (DatasetImageLink) it.next();
                if (id == link.getParent().getId().val)
                	p.addDatasetImageLink2(link, false);
            }
        } else
            throw new IllegalArgumentException("DataObject not supported.");
    }
    
    /**
     * Converts the specified <code>DataObject</code> into its corresponding 
     * <code>IObject</code>.
     * 
     * @param child     The child to create.
     * @param parent    The child's parent.
     * @return The {@link IObject} to create.
     */
    public static IObject createIObject(DataObject child, DataObject parent)
    {
        if (child instanceof ProjectData) {
            ProjectData data = (ProjectData) child;
            Project model = new ProjectI();
            model.setName(new RString(data.getName()));
            model.setDescription(new RString(data.getDescription()));
            return model;
        } else if (child instanceof DatasetData) {
            DatasetData data = (DatasetData) child;
            Dataset model = new DatasetI();
            model.setName(new RString(data.getName()));
            model.setDescription(new RString(data.getDescription()));
            if (parent != null)
            	model.linkProject(new ProjectI(new Long(parent.getId()), false));
            return model;
        } else if (child instanceof ImageData) {
            if (!(parent instanceof DatasetData))
                throw new IllegalArgumentException("Parent not valid.");
            ImageData data = (ImageData) child;
            Image model = new ImageI();
            model.setName(new RString(data.getName()));
            model.setDescription(new RString(data.getDescription()));
            if (parent instanceof DatasetData) 
                model.linkDataset(new DatasetI(new Long(parent.getId()), 
                                            false));
            return model; 
        } else if (child instanceof ScreenData) {
        	ScreenData data = (ScreenData) child;
        	Screen model = new ScreenI();
        	model.setName(new RString(data.getName()));
        	model.setDescription(new RString(data.getDescription()));
            return model;
        }
        throw new IllegalArgumentException("Child and parent are not " +
        		"compatible.");
    }
    
    /**
     * Unlinks the specified child and the parent and returns the 
     * updated child <code>IObject</code>.
     * 
     * @param child     The child to remove.
     * @param parent    The parent of the child.
     * @return See above. 
     */
    public static IObject removeIObject(IObject child, IObject parent)
    {
        if ((child instanceof Dataset) && (parent instanceof Project)) {
            Project mParent = (Project) parent;
            List s = mParent.copyDatasetLinks();
            Iterator i = s.iterator();
            while (i.hasNext()) { 
                mParent.removeProjectDatasetLink2((ProjectDatasetLink) i.next(),
                		false);
            }
            return mParent;
        } 
        throw new IllegalArgumentException("DataObject not supported.");
    }
   
    /**
     * Creates a new annotation <code>IObject</code>.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          Can either be a <code>DatasetData</code>
     *                          or a <code>ImageData</code>. Mustn't be
     *                          <code>null</code>.
     * @param data              The annotation to create.
     * @return See above.
     */
    public static IObject createAnnotationAndLink(IObject annotatedObject,
                                    AnnotationData data)
    {
    	Annotation annotation = createAnnotation(data);
    	if (annotation == null) return null;
    	return linkAnnotation(annotatedObject, annotation);
    }
    
    /**
     * Creates a new annotation <code>IObject</code>.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          Can either be a <code>DatasetData</code>
     *                          or a <code>ImageData</code>. Mustn't be
     *                          <code>null</code>.
     * @param data              The annotation to create.
     * @return See above.
     */
    public static Annotation createAnnotation(AnnotationData data)
    {
    	Annotation annotation = null;
    	if (data instanceof TextualAnnotationData) {
    		annotation = new TextAnnotationI();
    		((TextAnnotation) annotation).setTextValue(new RString(
    										data.getContentAsString()));
    	} else if (data instanceof RatingAnnotationData) {
    		int rate = ((RatingAnnotationData) data).getRating();
			if (rate == RatingAnnotationData.LEVEL_ZERO) return null;
    		annotation = new LongAnnotationI();
    		annotation.setNs(new RString(
    				RatingAnnotationData.INSIGHT_RATING_NS));
    		((LongAnnotation) annotation).setLongValue(new RLong(
    										(Long) data.getContent()));
    	} else if (data instanceof URLAnnotationData) {
    		annotation = new UrlAnnotationI();
    		try {
    			((UrlAnnotation) annotation).setTextValue(
    					new RString(data.getContentAsString()));
			} catch (Exception e) { //Need to propagate that.
				return null;
			}
    		
    	} else if (data instanceof TagAnnotationData) {
    		annotation = new TagAnnotationI();
    		((TagAnnotation) annotation).setTextValue(
    				new RString(data.getContentAsString()));
    	}
    	return annotation;
    }
    
    /**
     * Links the annotation to the passed object.
     * 
     * @param annotatedObject	The object to annotate.
     * @param annotation		The annotation to link.
     * @return See above.
     */
    public static IObject linkAnnotation(IObject annotatedObject,
    									Annotation annotation) 
    {
    	if (annotation == null) return null;
    	if (annotatedObject instanceof Dataset) {
    		Dataset m = (Dataset) annotatedObject;
    		DatasetAnnotationLink l = new DatasetAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Image) {
    		Image m = (Image) annotatedObject;
    		ImageAnnotationLink l = new ImageAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Project) {
    		Project m = (Project) annotatedObject;
    		ProjectAnnotationLink l = new ProjectAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Annotation) {
    		Annotation ann = (Annotation) annotatedObject;
    		AnnotationAnnotationLink l = new AnnotationAnnotationLinkI();
    		l.setParent(ann);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Screen) {
    		Screen m = (Screen) annotatedObject;
    		ScreenAnnotationLink l = new ScreenAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Plate) {
    		Plate m = (Plate) annotatedObject;
    		PlateAnnotationLink l = new PlateAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	} else if (annotatedObject instanceof Well) {
    		Well m = (Well) annotatedObject;
    		WellAnnotationLink l = new WellAnnotationLinkI();
    		l.setParent(m);
    		l.setChild(annotation);
    		return l;
    	}
    	return null;
    }
    
    /**
     * Links the annotated object and its annotation.
     * 
     * @param annotated		The annotated object.
     * @param annotation	The annotation.
     */
    public static void setAnnotatedObject(IObject annotated, 
            IObject annotation)  
    {
    	/*
        if (annotation instanceof ImageAnnotation)
            ((ImageAnnotation) annotation).setImage((Image) annotated);
        if (annotation instanceof DatasetAnnotation)
            ((DatasetAnnotation) annotation).setDataset((Dataset) annotated);
            */
    }
    
    /**
     * Returns the annotated IObject related to the specified annotation.
     * 
     * @param annotation    The annotation.
     * @return  See above.
     */
    public static IObject getAnnotatedObject(IObject annotation)
    {
    	if (annotation instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) annotation).getParent();
    	else if (annotation instanceof AnnotationAnnotationLink)
    		return ((AnnotationAnnotationLink) annotation).getParent();
    	else if (annotation instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) annotation).getParent();
    	else if (annotation instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) annotation).getParent();
    	else if (annotation instanceof WellAnnotationLink)
    		return ((WellAnnotationLink) annotation).getParent();
    	return null;
    }
    
    /**
     * Returns the annotated IObject related to the specified annotation.
     * 
     * @param annotation    The annotation.
     * @return  See above.
     */
    public static IObject getAnnotationObject(IObject annotation)
    {
    	if (annotation instanceof DatasetAnnotationLink)
    		return ((DatasetAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ProjectAnnotationLink)
    		return ((ProjectAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ImageAnnotationLink)
    		return ((ImageAnnotationLink) annotation).getChild();
    	else if (annotation instanceof PlateAnnotationLink)
    		return ((PlateAnnotationLink) annotation).getChild();
    	else if (annotation instanceof ScreenAnnotationLink)
    		return ((ScreenAnnotationLink) annotation).getChild();
    	else if (annotation instanceof WellAnnotationLink)
    		return ((WellAnnotationLink) annotation).getChild();
    	return null;
    }
    
    /**
     * Fills the new IObject with data from the old one.
     * 
     * @param oldObject	The old object.
     * @param newObject	The object to fill.
     */
    public static void fillIObject(IObject oldObject, IObject newObject)
    {
    	if (oldObject == null || newObject == null)
    		throw new IllegalArgumentException("Object cannot be NULL.");
    	if (oldObject.getClass() != newObject.getClass())
    		throw new IllegalArgumentException("Objects should be of the " +
    				"same type.");
    	if (oldObject instanceof Project) {
    		Project n = (Project) newObject;
    		Project o = (Project) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Dataset) {
    		Dataset n = (Dataset) newObject;
    		Dataset o = (Dataset) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Image) {
    		Image n = (Image) newObject;
    		Image o = (Image) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Experimenter) {
    		Experimenter n = (Experimenter) newObject;
    		Experimenter o = (Experimenter) oldObject;
    		n.setEmail(o.getEmail());
    		n.setFirstName(o.getFirstName());
    		n.setLastName(o.getLastName());
    		n.setInstitution(o.getInstitution());
    		//n.setDefaultGroup(o.getDefaultGroup());
    	} else if (oldObject instanceof Screen) {
    		Screen n = (Screen) newObject;
    		Screen o = (Screen) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Plate) {
    		Plate n = (Plate) newObject;
    		Plate o = (Plate) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	}
    }
    
}
