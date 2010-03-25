/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.util.List;

import omero.model.IObject;
import omero.grid.FileSet;

/**
 *  A config class used to control listing
 *
 * @since Beta4.1
 */
public class FileSetI extends FileSet {
    
    FileSetI() {
        importableImage = false;
        file = null;
	    name = "";
        usedFiles = null;
    }
    
    public void setImportableImage(boolean importableImage) {
        importableImage = importableImage;
    }
    
    public boolean getImportableImage() {
        return importableImage;
    }

    public void setName(String name) {
        name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setFile(IObject file) {
        file = file;
    }
    
    public IObject getFile() {
        return file;
    }

    public void setUsedFiles(List<IObject> usedFiles) {
        usedFiles = usedFiles;
    }
    
    public List<IObject> getUsedFiles() {
        return usedFiles;
    }


}

