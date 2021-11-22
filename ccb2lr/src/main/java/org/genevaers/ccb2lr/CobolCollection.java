package org.genevaers.ccb2lr;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.parse.ANTLRParser.parserRule_return;

public class CobolCollection {
    
	private List<CobolField> fields = new ArrayList<>();
	private CobolField currentField;
	private GroupField recordGroup;
	private int expansionsRequired = 0;

    public void addCobolField(CobolField newField) {
		if(currentField == null) {
			recordGroup = (GroupField) newField;
		} else {
			if(newField.getSection() == currentField.getSection()) {
				currentField.addSibling(newField);
			} else if ( newField.getSection() > currentField.getSection()) {
				currentField.addChild(newField);
			} else {
				CobolField s = findSibling(newField);
				s.addSibling(newField);
			}
		}
		fields.add(newField);
		currentField = newField;
	}

	private CobolField findSibling(CobolField newField) {
		//section < than currentGroup - Find the LAST match not the first
		//CobolField grp = currentField;
		CobolField prnt = currentField.getParent();
		while(prnt != null && prnt.getSection() != newField.getSection() ) {
			// if(prnt.getType() == FieldType.GROUP || prnt.getType() == FieldType.OCCURSGROUP) {
			// 	grp = prnt;
			// }
			prnt = prnt.getParent();
		}
		// CobolField n = currentGroup.parent;
		// while (s <=  n.getSection()) {
		// 	if(n.getType() == FieldType.GROUP || n.getType() == FieldType.OCCURSGROUP) {
		// 		currentGroup = (GroupField) n;
		// 	}
		// 	n = n.next();
		// }
		return prnt;
	}

    public List<CobolField> getFields() {
        return fields;
    }

    public GroupField getRecordGroup() {
        return recordGroup;
    }

	public void resolvePositions() {
		countExpansionsRequired();
		int e = 1;
		while(expansionsRequired > 0) {
			expandOccursGroups();
			refreshFields();
			CCB2Dot.write(this, Paths.get("expandedgroupInGroupOccurs" + e + ".gv"));
			countExpansionsRequired();
			e++;
		}
		refreshFields();
		int pos = 1;
		CobolField c = recordGroup.getFirstChild();
		while(c != null) {
			pos = c.resolvePosition(pos);
			c = c.next();
		}
	}

	private void countExpansionsRequired() {
		expansionsRequired = 0;
		CobolField c = recordGroup.getFirstChild();
		while(c != null) {
			if(c.getType() == FieldType.OCCURSGROUP) {
				expansionsRequired++;
			}
			c = c.next();
		}
	}

	private void refreshFields() {
		fields.clear();
		fields.add(recordGroup);
		CobolField n = recordGroup.next();
		while(n != null) {
			fields.add(n);
			n = n.next();
		}
	}

	private void expandOccursGroups() {
		CobolField n = recordGroup.next();
		while(n != null) {
			if(n.getType() == FieldType.OCCURSGROUP) {
				expandOccursGroup((OccursGroup)n, "");
				n = n.getNextSibling();
			} else {
				n = n.next();
			}
		}
	}

	private void expandOccursGroup(OccursGroup og, String parentExt) {
        boolean connectSibling = false;
        if(og.getPreviousSibling() != null) {
            connectSibling = true;
        }
        CobolField connectToMe = null;
        for(int t=1; t<=og.getTimes(); t++) {
			String ext = parentExt + "-" + String.format("%02d", t);
			OccursGroup newMe = makeNewMe(og, t, ext);
            //CobolField child = og.next(); 
			copyAndRenameOriginalChildren(og, newMe, ext);
            //expandChildFields(newMe, ext, child);

            if(t == 1) {
                if(connectSibling) {
                    og.getPreviousSibling().setNextSibling(newMe);
                    newMe.setPreviousSibling(og.getPreviousSibling());
                } else {
                    og.getParent().replaceFirstChild(newMe); 
                    newMe.parent = og.getParent();
                }
                connectToMe = newMe;
            } else {
                //we are a sibling of the first newME
                if(connectToMe != null) {
                    connectToMe.setNextSibling(newMe);
                    newMe.setPreviousSibling(connectToMe);
                    connectToMe = newMe;
                }
            }
        }
        if(connectToMe != null) {
			if(og.getNextSibling() != null) {
            	connectToMe.nextSibling = og.getNextSibling();
				og.getNextSibling().setPreviousSibling(connectToMe);
			} else if(og.getParent() != null) {
				connectToMe.parent = og.getParent();
			}
        }
	}

	private OccursGroup makeNewMe(OccursGroup og, int t, String ext) {
		OccursGroup newMe = (OccursGroup) CobolFieldFactory.makeNamelessFieldFrom(og);
		String newName = og.getName() + ext;
		newMe.setName(newName);
		newMe.resetOccurs();
		return newMe;
}		

	private void copyAndRenameOriginalChildren(GroupField origin, GroupField newMe, String ext) {
		CobolField ochild = origin.getFirstChild();
		
		while(ochild != null) {
			CobolField newChild = makeNewChild(ochild, ext);
			newMe.addChild(newChild);
			if(ochild.getType() == FieldType.OCCURSGROUP || ochild.getType() == FieldType.GROUP) {
				copyAndRenameOriginalChildren((GroupField)ochild, (GroupField)newChild, ext);
			} else {
				CobolField sib = makeNewChild(newChild, ext);
				ochild.addChild(sib);
			}
			//child = child.next();
			ochild = ochild.getNextSibling();
		}
	}

	private CobolField makeNewChild(CobolField child, String ext) {
		CobolField newChild = CobolFieldFactory.makeNamelessFieldFrom(child);
		String newChildName = child.getName() + ext;
		newChild.setName(newChildName);
		return newChild;
	}
}
