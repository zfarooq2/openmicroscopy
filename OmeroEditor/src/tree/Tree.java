/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package tree;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fields.FieldPanel;

import tree.edit.EditAddField;
import tree.edit.EditClearFields;
import tree.edit.EditCopyDefaultValues;
import tree.edit.EditDataFieldType;
import tree.edit.EditDeleteField;
import tree.edit.EditDemoteFields;
import tree.edit.EditDuplicateFields;
import tree.edit.EditImportFields;
import tree.edit.EditLockFields;
import tree.edit.EditMoveFieldsDown;
import tree.edit.EditMoveFieldsUp;
import tree.edit.EditMultiplyValues;
import tree.edit.EditPromoteFields;
import tree.edit.EditRequiredField;
import tree.edit.EditUnlockFields;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;
import ui.fieldEditors.FieldEditor;
import ui.formFields.FormFieldNumber;
import validation.XMLSchema;
import xmlMVC.ConfigConstants;

// Tree manages the tree data structure
// also knows which fields are currently selected (applies actions to these)

public class Tree 
	implements  
	ITreeManager {
	
	// this enum specifies a constructor that takes a String name, returned by toString();
	public enum Actions {MOVE_FIELDS_UP("Move Fields Up"), MOVE_FIELDS_DOWN("Move Fields Down"), 
		DELTE_FIELDS("Delete Fields"), ADD_NEW_FIELD("Add New Field"), DEMOTE_FIELDS("Demote Fields"), 
		PROMOTE_FIELDS("Promote Fields"), DUPLICATE_FIELDS("Duplicate Fields"), UNDO_LAST_ACTION("Undo Last Action"), REDO_ACTION("Redo"), 
		IMPORT_FIELDS("Import Fields"), LOAD_DEFAULTS("Load Default Values"), 
		LOAD_DEFAULTS_HIGHLIGHTED_FIELDS("Load Defaults for Highlighted Fields"), 
		CLEAR_FIELDS("Clear Fields"), CLEAR_FIELDS_HIGHLIGHTED_FIELDS("Clear Fields for Highlighted Fields"), 
		 REQUIRED_FIELDS("Set Highlighted Fields as mandatory");
		private Actions(String name){
			this.name = name;
		}
		private String name;
		public String toString() {
			return name;
		}
	}
	
	private DataFieldNode rootNode;		// the root of the dataField tree. 
	
	private File file;		// the file that this tree is built from
	private boolean treeEdited = false;
	private boolean xmlValidationOn = false;
	
	private ArrayList<DataFieldNode> highlightedFields;
	
	public final static String ELEMENT = "element";
	
	private SelectionObserver selectionObserver;
	private XMLUpdateObserver xmlUpdateObserver;
	
	UndoManager undoManager;         // history list
	UndoableEditSupport undoSupport; // event support
	
	
	/**
	 * Creates a completely blank tree. 
	 * This is so that other classes can build a tree themselves, using the 
	 * methods of ITreeModel. 
	 */
	public Tree() {
		initialise();
	}
	
	public Tree(Document document, SelectionObserver selectionObserver, XMLUpdateObserver xmlObserver) {
		
		this.selectionObserver = selectionObserver;
		this.xmlUpdateObserver = xmlObserver;
		
		Element rootElement = document.getDocumentElement();
			
		LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

		parseElementToMap(rootElement, allAttributes);
			 
		rootNode = new DataFieldNode(allAttributes, this);
		
		initialise();
			
		buildTreeFromDOM(rootNode, rootElement);
	}
	
	public Tree(Document document) {
		
		Element rootElement = document.getDocumentElement();
			
		LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

		parseElementToMap(rootElement, allAttributes);
			 
		rootNode = new DataFieldNode(allAttributes, this);
		
		initialise();
			
		buildTreeFromDOM(rootNode, rootElement);
	}
	
	public Tree(SelectionObserver selectionObserver, XMLUpdateObserver xmlObserver) {
		//rootNode = new DataFieldNode(this);
		this.selectionObserver = selectionObserver;
		this.xmlUpdateObserver = xmlObserver;
		
		initialise();
		
		openBlankProtocolFile();
	}
	
	private void initialise() {
		
		highlightedFields = new ArrayList<DataFieldNode>();
		
		// initialize the undo.redo system
	      undoManager = new UndoManager();
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
	}
	
// use this entry point to access as many of the tree manipulation and data-structure commands as possible
	public void editTree(Actions action) {
		
		switch (action) {
		
			case UNDO_LAST_ACTION: {
				if (undoManager.canUndo())
					undoManager.undo();
				break;
			}
			case REDO_ACTION: {
				if (undoManager.canRedo())
					undoManager.redo();
				break;
			}
		
			case ADD_NEW_FIELD: {
				addDataField();
				break;
			}
			case DELTE_FIELDS: {
				deleteDataFields();
				break;
			}
			case MOVE_FIELDS_UP: {
				moveFieldsUp();
				break;
			}
			case MOVE_FIELDS_DOWN: {
				moveFieldsDown();
				break;
			}
			case PROMOTE_FIELDS: {
				promoteDataFields();
				break;
			}
			case DEMOTE_FIELDS: {
				demoteDataFields();
				break;
			}
			case DUPLICATE_FIELDS: {
				duplicateAndInsertDataFields();
				break;
			}
			case LOAD_DEFAULTS: {
				copyDefaultValuesToInputFields();
				break;
			}
			case LOAD_DEFAULTS_HIGHLIGHTED_FIELDS: {
				copyDefaultValuesToInputForHighlightedFields();
				break;
			}
			case CLEAR_FIELDS: {
				clearFields();
				break;
			}
			case CLEAR_FIELDS_HIGHLIGHTED_FIELDS: {
				clearFieldsforHighlightedFields();
				break;
			}
			case REQUIRED_FIELDS: {
				setRequiredFields();
				break;
			}
		}
		
	}


	//	 start a blank protocol - used by "default" Tree constructor
	private void openBlankProtocolFile() {
		
		// the root of the dataField tree
		rootNode = new DataFieldNode(this);
		DataField rootField = rootNode.getDataField();
		
		rootField.setAttribute(DataFieldConstants.INPUT_TYPE, DataFieldConstants.PROTOCOL_TITLE, false);
		//rootField.setAttribute(DataFieldConstants.ELEMENT_NAME, "Title - click to edit", false);
		
		DataFieldNode newNode = new DataFieldNode(this);// make a new default-type field
		rootNode.addChild(newNode);
	}
	
	private void buildTreeFromDOM(DataFieldNode dfNode, Element inputElement) {
		
		NodeList children = inputElement.getChildNodes();
		
		for (int i=0; i < children.getLength(); i++) {
			
			// skip any empty (text) nodes
			Node node = children.item(i);
	
			 
			 if (node != null && (node.getNodeType() == Node.ELEMENT_NODE)) {
				 Element element = (Element)node; 
				 LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

				 parseElementToMap(element, allAttributes);
				 DataFieldNode newNode = new DataFieldNode(allAttributes, dfNode, this);
				 dfNode.addChild(newNode);	// sets dfNode as parent
				 buildTreeFromDOM(newNode, element);
			 }
			 
			 // if there is a text node (a string of text between element tags), apply is to the PARENT node
			 if (node != null && (node.getNodeType() == Node.TEXT_NODE)) {
				 String textValue = node.getTextContent().trim();
				 if (textValue.length() > 0){
					 // set this attribute of the parent node, false: don't add to undo/redo queue
					 dfNode.getDataField().setAttribute(DataFieldConstants.TEXT_NODE_VALUE, node.getTextContent(), false);
				 }
			 }
		}
		
	}
	
	/* copyDefaultValuesToInputFields()
	 * iterates through every node...
	 * takes the "default" attribute value for each dataField, and loads it into the "value" attribute.
	 * each fields' panel is then updated.
	 */
	private void copyDefaultValuesToInputFields() {
		
		// the constructor of this edit command also carries out the command
		UndoableEdit edit = new EditCopyDefaultValues(rootNode);
		undoSupport.postEdit(edit);
		
		selectionChanged();		// to update undo button
	}
	
	private void copyDefaultValuesToInputForHighlightedFields() {
		// the constructor of this edit command also carries out the command
		UndoableEdit edit = new EditCopyDefaultValues(getHighlightedFields());
		undoSupport.postEdit(edit);
		
		selectionChanged();		// to update undo button
	}
	
	/**
	 * This adds a time-stamp (UTCmillisecs) to each field, to indicate
	 * that they are locked (and when). 
	 * Other attributes in the lockingAttributes map will also be added, 
	 * to describe the User, Locking Level etc. 
	 * 
	 * @param lockingAttributes		A map of additional attributes that define the lock
	 */
	public void lockHighlightedFields(Map<String, String> lockingAttributes) {
		
		// if fields are highlighted, lock them.
		if (getHighlightedFields().size() > 0) {
			UndoableEdit edit = new EditLockFields(getHighlightedFields(),
					lockingAttributes);
			undoSupport.postEdit(edit);
			setTreeEdited(true);
		}
		
		selectionChanged();		// to update undo button etc
	}
	
	
	private void setRequiredFields() {
		// if fields are highlighted, set required for them.
		
		if (getHighlightedFields().size() > 0) {
			UndoableEdit edit = new EditRequiredField(getHighlightedFields());
			undoSupport.postEdit(edit);
			setTreeEdited(true);
		}
		
		selectionChanged();		// to update undo button etc
	}
	
	// clear the variable "value" for any field that has one
	// do the whole tree (start at root node)
	private void clearFields() {
		
		UndoableEdit edit = new EditClearFields(rootNode);
		undoSupport.postEdit(edit);
		
		selectionChanged();		// to update undo button
	}
	
	// clear the variable "value" for any field that has one
	// just do the highlighted fields (and all their children)
	private void clearFieldsforHighlightedFields() {
		
		UndoableEdit edit = new EditClearFields(getHighlightedFields());
		undoSupport.postEdit(edit);
		
		selectionChanged();		// to update undo button
	}

	/**
	 * For all highlighted fields that are Number Fields, the numerical value
	 * they contain is multiplied by the factor argument. 
	 * Fields of other types are ignored. 
	 * 
	 * @param factor	The value to multiply numerical field values by. 
	 */
	public void multiplyValueOfSelectedFields(float factor) {
		
		UndoableEdit edit = new EditMultiplyValues(highlightedFields, factor);
		undoSupport.postEdit(edit);
		
		selectionChanged();		// to update undo button
	}
	
	
	// used to read XML element and populate a hash-map to use for dataField creation
	private void parseElementToMap(Element element, LinkedHashMap<String, String> allAttributes) {
		 String attributeValue;
		 String attribute;
		 
		 NamedNodeMap attributes = element.getAttributes();
		 for (int i=0; i<attributes.getLength(); i++) {
			 attribute = attributes.item(i).getNodeName();
			 attributeValue = attributes.item(i).getNodeValue();
	
			 if (attributeValue != null) {
				allAttributes.put(attribute, attributeValue);
			 }
		 }
		 
		 String elementName = element.getNodeName();
		 // the 'old' version-1 xml used "inputType" attribute.
		 // if this attribute exists, need to convert it to the new type 
		 // eg. "Fixed Step" becomes "FixedStep"
		 // otherwise, need to use the NodeName as the inputType (as in the new version)
		 if (allAttributes.get(DataFieldConstants.INPUT_TYPE) != null) {
			 String oldInputType = allAttributes.get(DataFieldConstants.INPUT_TYPE);
			 allAttributes.put(DataFieldConstants.INPUT_TYPE, DataField.getNewInputTypeFromOldInputType(oldInputType));
		 } else {
			 /* 
			  * InputType is null: Therefore this is the 'new' xml version: 
			  * Use <NodeName> for inputType IF the inputType is recognised.
			  */
			 if (DataFieldConstants.isInputTypeRecognised(elementName))
				 allAttributes.put(DataFieldConstants.INPUT_TYPE, elementName);
			 else 
				 allAttributes.put(DataFieldConstants.INPUT_TYPE, DataFieldConstants.CUSTOM);
		 }
		 
		 // if the xml file's elements don't have "elementName" attribute, use the <tagName>
		 if (allAttributes.get(DataFieldConstants.ELEMENT_NAME) == null) {
			 allAttributes.put(DataFieldConstants.ELEMENT_NAME, elementName);
		 }
	}


//	 duplicate a dataField and add it at specified index
	private void duplicateAndInsertDataFields() {
		
		if (highlightedFields.isEmpty()) return;
		
		// highlighted fields change while adding. Make a copy first
		ArrayList<DataFieldNode> tempArray = new ArrayList<DataFieldNode>(highlightedFields);
		
		// add a copy of tempArray after highlightedFields
		copyAndInsertDataFields(tempArray, highlightedFields);
		
		// add the undo action 	// highlightedFields will now be the newly added fields
		UndoableEdit edit = new EditDuplicateFields(highlightedFields);
		undoSupport.postEdit(edit);
		
		setTreeEdited(true);
	}
	
	
	/**
	 * Creates a new root node. 
	 * This will effectively delete the existing tree. 
	 * Removes all references to existing fields (clears highlighted fields). 
	 * This action is not added to undo/redo.
	 * ITreeModel interface. 
	 * 
	 * @param node
	 */
	public void setRootNode(DataFieldNode node) {
		
		highlightedFields.clear();
		
		rootNode = node;
	}

	// add a blank dataField
	private void addDataField() {
		// add the undo action (include rootNode reference, in case no highlighted fields)
		// undoActions.push(new TreeAction(Actions.ADD_NEW_FIELD, highlightedFields, rootNode));
		setTreeEdited(true);
		
		DataFieldNode newNode = new DataFieldNode(this);// make a new default-type field
		addDataField(newNode);	// adds after last highlighted field, or last child of root
		
		UndoableEdit edit = new EditAddField(newNode);
		undoSupport.postEdit(edit);
	}
	
	//	 add a new dataField after the last highlighted dataField
	private void addDataField(DataFieldNode newNode) {
		
		// get selected Fields and add dataField after last selected one
	
		DataFieldNode parentNode = null;
		int indexToInsert = 0;
		
		if (highlightedFields.size() > 0) {
			indexToInsert = highlightedFields.get(highlightedFields.size()-1).getMyIndexWithinSiblings() + 1;
			parentNode = highlightedFields.get(0).getParentNode();
		} else {
			// add after last child of protocol (if there are any!)
			indexToInsert = getRootNode().getChildren().size();
			parentNode = rootNode;
		}
		
		NodeManagerMethods.addDataField(newNode, parentNode, indexToInsert);
		
		nodeSelected(newNode, true); // select the new node
	}

	/**
	 * Used to take a list of Nodes, eg from clip-board, or another tree,
	 * to duplicate them, then insert them after the last highlighted
	 * field (or after last child of root if none highlighted)
	 * 
	 * @param dataFieldNodes
	 */
	public void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes) {
		copyAndInsertDataFields(dataFieldNodes, highlightedFields);
		
		// add the undo action 	// highlightedFields will now be the newly added fields
		UndoableEdit edit = new EditImportFields(highlightedFields);
		undoSupport.postEdit(edit);
		setTreeEdited(true);
	}
	
	/**
	 * Duplicates the dataFieldNodes, and inserts them as siblings to 
	 * selectedFields (after the last selected field)
	 * 
	 * @param dataFieldNodes	The nodes to duplicate
	 * @param selectedFields	Add as siblings to these nodes, after last one.
	 */
	private void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes, 
			ArrayList<DataFieldNode> selectedFields) {
		
		int indexToInsert = 0;
		DataFieldNode parentNode = null;
		//get the parent and index to start adding
		if (selectedFields.isEmpty()) {
			indexToInsert = rootNode.getChildren().size();	// will add after last one
			parentNode = rootNode;
		} else {
			DataFieldNode lastHighlightedField = selectedFields.get(selectedFields.size() -1);
			parentNode = lastHighlightedField.getParentNode();
			indexToInsert = lastHighlightedField.getMyIndexWithinSiblings() + 1;
		}
		NodeManagerMethods.copyAndInsertDataFields(dataFieldNodes, parentNode, indexToInsert);
		
	}
	
	private void demoteDataFields() {
		if (highlightedFields.isEmpty()) return;
		
		try {
			NodeManagerMethods.demoteDataFields(highlightedFields);
			
			// add the undo action 
			UndoableEdit edit = new EditDemoteFields(highlightedFields);
			undoSupport.postEdit(edit);

			setTreeEdited(true);
		} catch (Exception ex) {
			System.out.println("Tree. demoteDataFields Exception: " + ex.getMessage());
		}
	}
	
	private void promoteDataFields() {
		if (highlightedFields.isEmpty()) return;
		
		try {
			// create an undo Action based on the currently highlighted fields, their children etc.
			UndoableEdit edit = new EditPromoteFields(highlightedFields);
			
			NodeManagerMethods.promoteDataFields(highlightedFields);
			
			// if promoting went OK, add the undo action 
			// add the undo action 
			undoSupport.postEdit(edit);
			
			setTreeEdited(true);
		} catch (Exception ex) {
			System.out.println("Tree. promoteDataFields Exception: " + ex.getMessage());
		}
	}
	
	//	 if the highlighted fields have a preceding sister, move it below the highlighted fields
	private void moveFieldsUp() {
		
		if (highlightedFields.size() == 0) return;
		
		try {
			UndoableEdit edit = new EditMoveFieldsUp(highlightedFields);
			
			NodeManagerMethods.moveFieldsUp(highlightedFields);
			
			undoSupport.postEdit(edit);
			setTreeEdited(true);
		} catch (IndexOutOfBoundsException ex) {
			// System.out.println("Tree.moveFieldsUp() indexOutOfBounds exception");
		}
	}
	
	//	 if the highlighted fields have a preceding sister, move it below the highlighted fields
	private void moveFieldsDown() {
		
		if (highlightedFields.size() == 0) return;
		
		try {
			UndoableEdit edit = new EditMoveFieldsDown(highlightedFields);
			
			NodeManagerMethods.moveFieldsDown(highlightedFields);
			
			undoSupport.postEdit(edit);
			setTreeEdited(true);
		} catch (IndexOutOfBoundsException ex) {
			// ignore
		}
	}
	
	// used to export the tree to DOM document
	public static void buildDOMfromTree(DataFieldNode treeRootNode, Document document) {

		//DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// DocumentBuilder db = dbf.newDocumentBuilder();
			//document = db.newDocument();
			DataField rootField = treeRootNode.getDataField();
			
			String elementName = rootField.getInputType();
		
			
			boolean customElement = false;
			if ((rootField.getInputType() == null)) customElement = true;
			else if (rootField.getInputType().equals(DataFieldConstants.CUSTOM)) customElement = true;
			
			// if custom XML element, use the elementName attribute as the element Name
			if (customElement) elementName = rootField.getName();
			if (elementName == null) elementName = ELEMENT; 	// just in case!
			
			Element element = document.createElement(elementName);  
			
			// get all attributes of the dataField
			HashMap<String, String> allAttributes = rootField.getAllAttributes();
			// also get attributes required for Protocol Editor xsd schema (unless custom)
			if (!customElement) {
				Map<String, String> schemaAttributes = XMLSchema.getRootAttributes();
				allAttributes.putAll(schemaAttributes);
			}
			
			parseAttributesMapToElement(allAttributes, element);
			
			document.appendChild(element);
			// System.out.println("Tree.buildDOMfromTree appendedChild: " + element.getNodeName());
			
			buildDOMchildrenFromTree(document, treeRootNode, element);
			
			
		} catch (DOMException ex) {
			throw ex;
		}
		
	}
	
	// recursive function to build DOM from tree
	private static void buildDOMchildrenFromTree(Document document, DataFieldNode rootNode, Element rootElement) {
		
		ArrayList<DataFieldNode> childNodes = rootNode.getChildren();
		if (childNodes.size() == 0) return;

		
		for (DataFieldNode child: childNodes) {
			
			DataField dataField = child.getDataField();
			
			boolean customElement = dataField.isCustomInputType();
					
			String elementName = dataField.getInputType();
			
			// if custom XML element, use the elementName attribute as the element Name
			if (customElement) elementName = dataField.getName();
			if (elementName == null) elementName = ELEMENT; 	// just in case!
			
			Element element = document.createElement(elementName);
			
			HashMap<String, String> allAttributes = dataField.getAllAttributes();
			parseAttributesMapToElement(allAttributes, element);
			
			// if custom xml Element that has a text node value, save it! 
			if (customElement) {
				String text = dataField.getAttribute(DataFieldConstants.TEXT_NODE_VALUE);
				if (text != null)
					element.setTextContent(text);
			}
			
			rootElement.appendChild(element);
			
			buildDOMchildrenFromTree(document, child, element);
		}  // end for
	}
	
	// copies each dataField's attribute Hash Map into element's attributes
	private static void parseAttributesMapToElement(HashMap<String, String> allAttributes, Element element) {
		
		String inputType = allAttributes.get(DataFieldConstants.INPUT_TYPE);
		
		System.out.println("Tree parseAttributesMapToElement: inputType = " + inputType);
		
		boolean customElement = false;
		if ((inputType == null)) customElement = true;
		else if (inputType.equals(DataFieldConstants.CUSTOM)) customElement = true;
		
		System.out.println("Tree parseAttributesMapToElement: customElement = " + customElement);
		
		
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String key = (String)keyIterator.next();
			String value = allAttributes.get(key);
			
			// if you want to recreate original xml, don't include "extra" attributes
			if (customElement) {
				if (key.equals(DataFieldConstants.ELEMENT_NAME) || key.equals(DataFieldConstants.SUBSTEPS_COLLAPSED)
						|| key.equals(DataFieldConstants.TEXT_NODE_VALUE) || key.equals(DataFieldConstants.INPUT_TYPE)
						|| key.equals(DataFieldConstants.BACKGROUND_COLOUR))
					continue;
			}
			
			// for all new - versions of xml, don't save inputType attribute (this is now the tag name)
			if (key.equals(DataFieldConstants.INPUT_TYPE)) continue;
			
			// only save non-null values (and don't save "")
			if ((value != null) && (value.length() > 0)) {
				element.setAttribute(key, value);
				// System.out.println("Tree.parseAttributesMapToElement key = " + key + ", value = " + value);
			}
		}
	}
	
//	 delete the highlighted dataFields 
	private void deleteDataFields() {
		if (highlightedFields.isEmpty()) return;
		
		// add the undo action 
		// undoActions.push(new TreeAction(Actions.DELTE_FIELDS, highlightedFields));
		UndoableEdit edit = new EditDeleteField(highlightedFields);
		undoSupport.postEdit(edit);
		
		setTreeEdited(true);
		
		NodeManagerMethods.deleteDataFields(highlightedFields);
		highlightedFields.clear();
		
	}

	// called (via dataField) by clicking on FormField to highlight it
	public void nodeSelected(DataFieldNode selectedNode, boolean clearOthers) {
		
		// always need to deselect rootNode
		rootNode.setHighlighted(false);
		if (selectedNode.getParentNode() == null) {
			rootNode.setHighlighted(true);
		}
		
		if (clearOthers) {
			// clear highlighting from all other dataField panels
			for (DataFieldNode highlightedNode: highlightedFields) {
				highlightedNode.setHighlighted(false);
			} 
			highlightedFields.clear();
			
		} else {	
			// if user tries to select multiple fields, they must have same parent
			// otherwise duplicate and delete operations become very confusing!
			// if no parent node, then this is protocol root node.
			
			DataFieldNode clickedNodeParent = selectedNode.getParentNode();
			
			for (int i=highlightedFields.size()-1; i>=0; i--) {
				DataFieldNode parent = highlightedFields.get(i).getParentNode();
				
				// if parent of an already selected field is not the same as..
				// the clicked-field's parent, de-select it.
				if (!(parent.equals(clickedNodeParent))) {
					highlightedFields.get(i).setHighlighted(false);
					highlightedFields.remove(i);
				}
			}
		}
		
		// add dataField to selected fields (if not protocol (root) field)
		if (selectedNode.getParentNode() != null) 
			addToHighlightedFields(selectedNode);
		
		selectionChanged();
	}
	
//	 need to make sure that highlighted fields (siblings) are sorted in their sibling order
	// and that only consecutive siblings are selected
	private void addToHighlightedFields(DataFieldNode dataFieldNode) {
		
		// if empty, just add
		if (highlightedFields.size() == 0)  {
			highlightedFields.add(dataFieldNode);
			dataFieldNode.setHighlighted(true);
		}
		
		// need to highlight all fields between currently selected fields and newly selected field
		else {
			int siblingIndex = dataFieldNode.getMyIndexWithinSiblings();
			
			// get the max and minimum indexes of highlighted fields
			int highlightedIndexMax = highlightedFields.get(0).getMyIndexWithinSiblings();
			int highlightedIndexMin = highlightedFields.get(0).getMyIndexWithinSiblings();
			for (DataFieldNode highlightedField: highlightedFields) {
				int index  = highlightedField.getMyIndexWithinSiblings();
				if (index > highlightedIndexMax) highlightedIndexMax = index;
				if (index < highlightedIndexMin) highlightedIndexMin = index;
			}
			
			DataFieldNode parentNode = dataFieldNode.getParentNode();
			
			// if so, add at end of list or at the start
			if (siblingIndex > highlightedIndexMax) {
				for (int i=highlightedIndexMax +1; i<siblingIndex + 1 ; i++) {
					DataFieldNode siblingDataFieldNode = parentNode.getChild(i);
					highlightedFields.add(siblingDataFieldNode);
					siblingDataFieldNode.setHighlighted(true);
				}
			}
			if (siblingIndex < highlightedIndexMin) {
				for (int i=highlightedIndexMin -1; i>siblingIndex - 1 ; i--) {
					DataFieldNode siblingDataFieldNode = parentNode.getChild(i);
					highlightedFields.add(0, siblingDataFieldNode);
					siblingDataFieldNode.setHighlighted(true);
				}
			}
		}
		
	}


	public DataFieldNode getRootNode() {
		return rootNode;
	}
	
	/**
	 *  getSearchResults(searchWord)
	 *  returns an ArrayList of DataField objects that contain the seachWord
	 */
	public ArrayList<DataField> getSearchResults(String searchWord) {
		
		ArrayList<DataField> searchResults = new ArrayList<DataField>();
		
		Iterator <DataFieldNode>iterator = rootNode.iterator();

		while (iterator.hasNext()) {
			DataFieldNode node = iterator.next();
			DataField field = node.getDataField();
			if (field.attributesContainSearchWord(searchWord)) {
				searchResults.add(field);
			}
		}
		return searchResults;
	}
	
	
	/**
	 * called when the UI needs to display the FieldEditor.
	 * If only one field is currently selected, return it. Else return
	 * a blank panel
	 */ 
	public JPanel getFieldEditorToDisplay() {
		
		JPanel currentFieldEditor;
		
		if (highlightedFields.size() == 1) {
			currentFieldEditor = highlightedFields.get(0).getFieldEditor();
		}
		else if (rootNode.isHighlighted()) {
			currentFieldEditor = rootNode.getFieldEditor();
		}
		else
			currentFieldEditor = new FieldEditor();
		
		return currentFieldEditor;
	}
	
	// called by dataField (via Node) to notify of changes for history (NOT requiring UI update)
	public void dataFieldUpdated(AbstractUndoableEdit undoDataFieldAction) {
		
		undoSupport.postEdit(undoDataFieldAction);
		
		setTreeEdited(true);
		selectionChanged();	// xml will be validated etc.
		
	}
	
	// called by dataField to notify of changes that require re-drawing of UI. eg datField inputType change
	public void xmlUpdated() {
		setTreeEdited(true);
		if (xmlUpdateObserver != null) xmlUpdateObserver.xmlUpdated();
	}
	
	private void selectionChanged() {
		if (selectionObserver != null) {
			// System.out.println("Tree.selectionChanged");
			selectionObserver.selectionChanged();
		}
	}
	
	/**
	 * Gets a list of the currently highlighted nodes. 
	 * Used by other classes for copy and paste, or within this 
	 * class for applying tree edits. 
	 * 
	 * @return		The highlighted nodes. 
	 */
	public ArrayList<DataFieldNode> getHighlightedFields() {
		if (rootNode.isHighlighted()) {
			ArrayList<DataFieldNode> newList = new ArrayList<DataFieldNode>();
			newList.add(rootNode);
			return newList;
		}
		return highlightedFields;
	}
	
	/**
	 * This checks whether the highlighted fields
	 * have the attribute FIELD_LOCKED_UTC.
	 * Presence of this attribute indicates that the highlighted fields 
	 * are "Locked" and editing is not allowed. 
	 * 
	 * @return	true if any of the highlighted fields has the attribute FIELD_LOCKED_UTC.
	 */
	public boolean areHighlightedFieldsLocked() {
		
		if (highlightedFields.size() > 0)
			return AncestorChecker.isAttributeNotNull(DataFieldConstants.LOCKED_FIELD_UTC, highlightedFields);
	
		else if (rootNode.isHighlighted()) {
			return AncestorChecker.isAttributeNotNull(DataFieldConstants.LOCKED_FIELD_UTC, rootNode);
		}
		return false;
	}
	
	/**
	 * This method is used to get details of the highlighted fields that are locked. 
	 * Each highlighted field that is locked is represented by a HashMap, 
	 * containing "locking attributes" of the field, such as timeStamp and userName. 
	 * 
	 * @return		A list of hashMaps, corresponding to the list of highlighted locked fields. 
	 */
	public List<HashMap<String, String>> getLockedFieldsAttributes() {
		
		ArrayList<HashMap<String, String>> lockedFieldsAttributes = new ArrayList<HashMap<String, String>>();
		
		for (DataFieldNode node: highlightedFields) {
			/*
			 * Check each highlighted field to see if it is locked...
			 */
			if (AncestorChecker.isAttributeNotNull(DataFieldConstants.LOCKED_FIELD_UTC, node)) {
				
				/*
				 * If locked, get the locked attributes, add to a new HashMap and add this to the list.
				 */
				IAttributeSaver field = node.getDataField();
				HashMap<String, String> lockedAttributes= new HashMap<String, String>();
				
				lockedAttributes.put(DataFieldConstants.ELEMENT_NAME, field.getAttribute(DataFieldConstants.ELEMENT_NAME));
				lockedAttributes.put(DataFieldConstants.LOCKED_FIELD_UTC, field.getAttribute(DataFieldConstants.LOCKED_FIELD_UTC));
				lockedAttributes.put(DataFieldConstants.LOCKED_FIELD_USER_NAME, field.getAttribute(DataFieldConstants.LOCKED_FIELD_USER_NAME));
				lockedAttributes.put(DataFieldConstants.LOCK_LEVEL, field.getAttribute(DataFieldConstants.LOCK_LEVEL));
				
				lockedFieldsAttributes.add(lockedAttributes);
			}
		}
		return lockedFieldsAttributes;
	}
	
	/**
	 * This checks whether ancestors of the highlighted fields
	 * have the attribute FIELD_LOCKED_UTC.
	 * This method IGNORES the highlighted fields themselves. 
	 * Presence of this attribute indicates that ancestors of the highlighted fields
	 * are "Locked" and editing is not allowed. 
	 * 
	 * @return	true if any of the highlighted field ancestors has the attribute FIELD_LOCKED_UTC.
	 */
	public boolean areAncestorFieldsLocked() {
		
		if (highlightedFields.size() > 0)
			return AncestorChecker.isAncestorAttributeNotNull(DataFieldConstants.LOCKED_FIELD_UTC, highlightedFields);
	
		return false;
	
	}
	
	/**
	 * This checks whether ANY fields in this tree are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for the tree.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. Editing actions that 
	 * apply to the whole tree (eg Clear All Fields or Load Defaults All Fields) should be disabled 
	 * if all attributes locked, but enabled if only the template is locked. 
	 * 
	 * @return	the max "lockLevel" if any fields in the tree are locked, or null if no fields locked.
	 */
	public String getMaxLockingLevel() {
		
		Iterator<DataFieldNode> iterator = rootNode.iterator();
		String maxLockLevel = null;
		
		while (iterator.hasNext()) {
			DataFieldNode node = iterator.next();
			DataField field = node.getDataField();
			String lockLevel = field.getAttribute(DataFieldConstants.LOCK_LEVEL);
			if(lockLevel == null)
				continue;
			if (lockLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES))
				return lockLevel;
			else {
				maxLockLevel = lockLevel;
			}
		}
		return maxLockLevel;
	}
	
	/**
	 * This checks for the MAX locking level of any highlighted fields and 
	 * their children. 
	 * Used (for example) by "Load Defaults" and "Clear Fields" actions, which
	 * apply to all children of highlighted fields, and should be disabled
	 * if any highlighted fields (or their children) are fully locked. 
	 * 
	 * @return  the max "lockLevel" if any highlighted fields or children 
 	 * 			are locked, or null if none are locked.
	 */
	public String getMaxHighlightedChildLockingLevel() {
		
		String maxLockLevel = null;
		
		for (DataFieldNode node: getHighlightedFields()) {
			
			Iterator<DataFieldNode> iterator = node.iterator();
			
			while (iterator.hasNext()) {
				DataFieldNode childNode = iterator.next();
				DataField field = childNode.getDataField();
				String lockLevel = field.getAttribute(DataFieldConstants.LOCK_LEVEL);
				if(lockLevel == null)
					continue;
				if (lockLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES))
					return lockLevel;
				else {
					maxLockLevel = lockLevel;
				}
			}
		}
		
		return maxLockLevel;
	}
	
	/**
	 * This checks whether the highlighted fields are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for all the highlighted fields.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. 
	 * Editing actions that apply to the currently highlighted fields can setEnabled(), based
	 * on this locking level. 
	 * 
	 * @return	the max "lockLevel" of highlighted fields, or null if none are locked.
	 */
	public String getMaxHighlightedLockingLevel() {
		
		String maxLockLevel = null;
		
		for (DataFieldNode node: highlightedFields) {
			DataField field = node.getDataField();
			String lockLevel = field.getLockedLevel();
			if(lockLevel == null)
				continue;
			if (lockLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES))
				return lockLevel;
			else {
				maxLockLevel = lockLevel;
			}
		}
		return maxLockLevel;
	}
	
	/**
	 * This checks to see if any field marked as 
	 * "Required" (DataFieldConstants.REQUIRED_FIELD = 'true')
	 * is also not filled out (ie dataField.isFieldFilled() is false). 
	 * Used for ensuring that "required" fields are not left blank when the form is saved.
	 * 
	 * @return	True if any required field is not filled out. 
	 */
	public boolean isAnyRequiredFieldEmpty() {
		
		Iterator<DataFieldNode> iterator = rootNode.iterator();
		
		while (iterator.hasNext()) {
			DataFieldNode node = iterator.next();
			DataField field = node.getDataField();
			/*
			 * If this is a required field...
			 */
			if (field.isAttributeTrue(DataFieldConstants.REQUIRED_FIELD)) {
				/*
				 * And it isn't filled out, return false. 
				 */
				if (! field.isFieldFilled())
					return true;
			}
		}
		return false;
	}
	
	/**
	 * This checks to see if any field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (whole tree) would over-write stuff. 
	 * 
	 * @return	True if any field with a default value is not cleared.
	 */
	public boolean isAnyDefaultFieldFilled() {
		
		Iterator<DataFieldNode> iterator = rootNode.iterator();
		
		while (iterator.hasNext()) {
			DataFieldNode node = iterator.next();
			DataField field = node.getDataField();
			/*
			 * If this field has a default value...
			 */
			if (field.getAttribute(DataFieldConstants.DEFAULT) != null) {
				/*
				 * And the destination to copy this value isn't empty...
				 * return true.
				 */
				String valueAttribute = EditCopyDefaultValues.getValueAttributeForLoadingDefault(field);
				String currentValue = field.getAttribute(valueAttribute);
				if ((currentValue != null) && (currentValue.length() > 0))
					return true;
			}
		}
		return false;
	}
	
	
	/**
	 * This checks to see if any highlighted field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (highlighted fields) would over-write stuff. 
	 * 
	 * @return	True if any highlighted field with a default value is not empty.
	 */
	public boolean isAnyHighlightedDefaultFieldFilled() {
		
		for (DataFieldNode node: getHighlightedFields()) {
			
			Iterator<DataFieldNode> iterator = node.iterator();
			
			while (iterator.hasNext()) {
				DataFieldNode childNode = iterator.next();
				DataField field = childNode.getDataField();
			
				/*
				 * If this field has a default value...
				 */
				if (field.getAttribute(DataFieldConstants.DEFAULT) != null) {
					/*
					 * And the destination to copy this value isn't empty...
					 * return true.
					 */
					String valueAttribute = EditCopyDefaultValues.getValueAttributeForLoadingDefault(field);
					String currentValue = field.getAttribute(valueAttribute);
					if ((currentValue != null) && (currentValue.length() > 0))
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * keep a note of the file that corresponds to this tree
	 */ 
	public void setFile (File file) {
		this.file = file;
	}
	
	/**
	 * Get the file that corresponds to this tree.
	 * @return		The file that this tree is read from/saved to.
	 */
	public File getFile () {
		return file;
	}
	
	/**
	 * This sets the Editor version attribute of the root node of the tree. 
	 * This can be used before saving the file to XML, so that the root element of the 
	 * XML file will have an up-to-date version number.
	 * 
	 * @param versionNumber	 	A string that represents the current version of the software. eg "3.0-3.1.2".
	 */
	public void setVersionNumber(String versionNumber) {
		getRootNode().getDataField().setAttribute(ConfigConstants.VERSION, versionNumber, false);
	}
	
	/**
	 * This returns the Editor version attribute from the root node of the tree. 
	 * This can be used to check that the file (tree) is not more recent that
	 * the current version of the software (meaning users should get a more recent version). 
	 * @return
	 */
	public String getVersionNumber() {
		return getRootNode().getDataField().getAttribute(ConfigConstants.VERSION);
	}
	
	/** 
	 * when the data structure changes, edited = true. 
	 * When saved, edited = false
	 */
	public void setTreeEdited(boolean edited) {
		System.out.println("Tree Edited = " + edited);
		treeEdited = edited;
	}
	
	/**
	 * Used (for example) for seeing whether you need to save before closing. 
	 */
	public boolean isTreeEdited() {
		return treeEdited;
	}
	
	/**
	 * For UI - Undo button
	 * 
	 * @return	The name of the Undo Command
	 */
	public String getUndoCommand() {
		if (undoManager.canUndo()) {
			return undoManager.getUndoPresentationName();
		}
		else 
			return "Cannot Undo";
	}
	
	/**
	 * For UI - Redo button
	 * 
	 * @return	The name of the Redo Command
	 */
	public String getRedoCommand() {
		if (undoManager.canRedo()) {
			return undoManager.getRedoPresentationName();
		}
		else 
			return "Cannot Redo";
	}
	
	/**
	 * Can you undo a previous command?
	 * 
	 * @return	True if undo is possible.
	 */
	public boolean canUndo() {
		return undoManager.canUndo();
	}
	
	/**
	 * Can you redo a previous command?
	 * 
	 * @return	True if redo is possible.
	 */
	public boolean canRedo() {
		return undoManager.canRedo();
	}
	
	/**
	  * An undo/redo adpater. The adpater is notified when
	  * an undo edit occur(e.g. add or remove from the list)
	  * The adptor extract the edit from the event, add it
	  * to the UndoManager, and refresh the GUI
	  * http://www.javaworld.com/javaworld/jw-06-1998/jw-06-undoredo.html
	  */
	private class UndoAdapter implements UndoableEditListener {
	     public void undoableEditHappened (UndoableEditEvent evt) {
	     	UndoableEdit edit = evt.getEdit();
	     	undoManager.addEdit( edit );
	     	//refreshUndoRedo();
	     }
	  }

}
