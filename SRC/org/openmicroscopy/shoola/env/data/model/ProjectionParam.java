/*
 * org.openmicroscopy.shoola.env.data.model.ProjectionParam
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
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.List;


//Third-party libraries

//Application-internal dependencies
import omero.constants.projection.ProjectionType;

import org.openmicroscopy.shoola.env.data.OmeroImageService;
import pojos.DatasetData;


/** 
 * Utility class storing the projection's parameters.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ProjectionParam
{

	/** The <code>Maximum</code> intensity projection (MIP) */
    public static final int 	MAXIMUM_INTENSITY = 
    								OmeroImageService.MAX_INTENSITY;
    
    /** The <code>Mean</code> intensity projection */
    public static final int 	MEAN_INTENSITY = 
    								OmeroImageService.MEAN_INTENSITY;
    
    /** The <code>Sum</code> intensity projection */
    public static final int 	SUM_INTENSITY = OmeroImageService.SUM_INTENSITY;
    
    /** Identifies the type used to store pixel values. */
	public static final String 	INT_8 = OmeroImageService.INT_8;

	/** Identifies the type used to store pixel values. */
	public static final String 	UINT_8 = OmeroImageService.UINT_8;

	/** Identifies the type used to store pixel values. */
	public static final String 	INT_16 = OmeroImageService.INT_16;

	/** Identifies the type used to store pixel values. */
	public static final String 	UINT_16 = OmeroImageService.UINT_16;

	/** Identifies the type used to store pixel values. */
	public static final String 	INT_32 = OmeroImageService.INT_32;

	/** Identifies the type used to store pixel values. */
	public static final String 	UINT_32 = OmeroImageService.UINT_32;

	/** Identifies the type used to store pixel values. */
	public static final String 	FLOAT = OmeroImageService.FLOAT;

	/** Identifies the type used to store pixel values. */
	public static final String 	DOUBLE = OmeroImageService.DOUBLE;
	
	/** The first timepoint to project. */
	private int               	startT;
	
	/** The last timepoint to project. */
	private int               	endT;
	
	/** The first z-section to project. */
	private int               	startZ;
	
	/** The last z-section to project. */
	private int               	endZ;
	
	/** The stepping used while projecting. Default is <code>1</code>. */
	private int               	stepping;
	
	/** The projection's algorithm. */
	private int              	algorithm;
	
	/** The collection of datasets where to store the projected image. */
	private List<DatasetData>	datasets;
	
	/** The name of the projected image. */
	private String           	name;
	
	/** The description of the projected image. */
	private String           	description;
	
	/** The type of pixels. */
	private String				pixelsType;
	
	/** The collection of channels to project. */
	private List<Integer> 	  	channels;
	
	/** The id of the pixels set to project. */
	private long			  	pixelsID;
	
	/**
	 * Checks if the passed algorithm is supported or not.
	 * 
	 * @param value The value to check.
	 */
	private void checkAlgorithm(int value)
	{
		//for some strange reasons, cannot use a switch
		if (value == MAXIMUM_INTENSITY) return;
		if (value == MEAN_INTENSITY) return;
		if (value == SUM_INTENSITY) return;
		throw new IllegalArgumentException("Algorithm not valid");
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID 	The id of the pixels set to project.
	 * @param startZ	The first z-section to project.	
	 * @param endZ		The last z-section to project.
	 * @param stepping	The stepping used while projecting.
	 * @param algorithm	The projection's algorithm.	
	 * @param startT	The first timepoint to project. 
	 * @param endT		The last timepoint to project.
	 * @param channels	The collection of channels to project.
	 * @param name		The name of the projected image.
	 */
	public ProjectionParam(long pixelsID, int startZ, int endZ, int stepping, 
						int algorithm, int startT, int endT,
						List<Integer> channels, String name)
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels ID not valid.");
		if (startZ > endZ) 
			throw new IllegalArgumentException("Optical Interval not valid.");
		if (name == null)
			throw new IllegalArgumentException("Image name cannot be null.");
		checkAlgorithm(algorithm);
		this.algorithm = algorithm;
		if (stepping < 1) stepping = 1;
		this.startZ = startZ;
		this.endZ = endZ;
		this.endT = startT;
		this.endT = endT;
		this.stepping = stepping;
		this.name = name;
		this.pixelsID = pixelsID;
		this.channels = channels;
		pixelsType = null;
	}
	
	/**
	 * Creates a new instance. This is constructor should only be used
	 * to host the parameters required to do a projection preview.
	 * 
	 * @param pixelsID 	The id of the pixels set to project.
	 * @param startZ	The first z-section to project.	
	 * @param endZ		The last z-section to project.
	 * @param stepping	The stepping used while projecting.
	 * @param algorithm	The projection's algorithm.	
	 */
	public ProjectionParam(long pixelsID, int startZ, int endZ, int stepping, 
			int algorithm)
	{
		this(pixelsID, startZ, endZ, stepping, algorithm, -1, -1, null, "");
	}
	
	/**
	 * Sets the description of the projected image.
	 * 
	 * @param description The value to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Returns the first timepoint to project. 
	 * 
	 * @return See above.
	 */
	public int getStartT() { return startT; }
	
	/**
	 * Returns the last timepoint to project. 
	 * 
	 * @return See above.
	 */
	public int getEndT() { return endT; }
	
	/**
	 * Returns the first z-section to project. 
	 * 
	 * @return See above.
	 */
	public int getStartZ() { return startZ; }
	
	/**
	 * Returns the last z-section to project. 
	 * 
	 * @return See above.
	 */
	public int getEndZ() { return endZ; }
	
	/**
	 * Returns the projeciton's algorithm. 
	 * 
	 * @return See above.
	 */
	public int getAlgorithm() { return algorithm; }
	
	/**
	 * Returns the freqence between each step. Default is <code>1</code>.
	 * 
	 * @return See above.
	 */
	public int getStepping() { return stepping; }
	
	/**
	 * Returns the name of the projected image.
	 * 
	 * @return See above.
	 */
	public String getName() { return name; }
	
	/**
	 * Returns the id of the pixels set to project.
	 * 
	 * @return See above.
	 */
	public long getPixelsID() { return pixelsID; }
	
	/**
	 * Returns the type of the newly created pixels set.
	 * This value shoudl only be set when the projection's algorithm is
	 * <code>Sum projection</code>.
	 * 
	 * @return See above.
	 */
	public String getPixelsType() { return pixelsType; }
	
	/**
	 * Returns the collection of datasets to add the created image to.
	 * 
	 * @return See above.
	 */
	public List<DatasetData> getDatasets() { return datasets; }
	
	/**
	 * Returns the collection of channels to project.
	 * 
	 * @return See above
	 */
	public List<Integer> getChannels() { return channels; }
	
	/**
	 * Sets the datasets to add the projected image to.
	 * 
	 * @param datasets The value to set.
	 */
	public void setDatasets(List<DatasetData> datasets)
	{ 
		this.datasets = datasets;
	}
	
	/**
	 * Returns the description of the image.
	 * 
	 * @return See above.
	 */
	public String getDescription() { return description; }
	
	/**
	 * Sets the channels to project.
	 * 
	 * @param channels The value to set.
	 */
	public void setChannels(List<Integer> channels)
	{ 
		this.channels = channels;
	}
	
	/**
	 * Returns the type of projection.
	 * 
	 * @return See above.
	 */
	public ProjectionType getProjectionType()
	{
		return convertType(getAlgorithm());
	}
	
	/**
	 * Returns the {@link ProjectionType} constants corresponding to the passed
	 * value.
	 * 
	 * @param type Identifier to the @link ProjectionType}.
	 * @return See above.
	 */
	public static ProjectionType convertType(int type)
	{
		if (ProjectionType.MAXIMUMINTENSITY.value() == type)
			return ProjectionType.MAXIMUMINTENSITY;
		if (ProjectionType.MEANINTENSITY.value() == type)
			return ProjectionType.MEANINTENSITY;
		if (ProjectionType.SUMINTENSITY.value() == type)
			return ProjectionType.SUMINTENSITY;
		return null;
	}
	
}
