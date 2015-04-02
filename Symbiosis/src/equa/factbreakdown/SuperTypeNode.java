/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.factbreakdown;

import javax.swing.tree.TreeNode;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.objectmodel.CollectionType;
import equa.meta.objectmodel.CollectionTypeExpression;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Tuple;
import equa.meta.objectmodel.Value;
import equa.meta.traceability.ExternalInput;
import java.util.List;

/**
 *
 * @author FrankP
 */
public class SuperTypeNode extends ParentNode implements ISubstitution {

    private static final long serialVersionUID = 1L;
    private String roleName;
    public int roleNumber;

    public SuperTypeNode(ExpressionTreeModel model, ParentNode parent,
        String supertypeName, String text, String concreteTypeName,
        String roleName, int roleNumber) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, supertypeName);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
        createSubnode(text, concreteTypeName, roleName);
    }

    public SuperTypeNode(ExpressionTreeModel model, ParentNode parent,
        String supertypeName, String text, String concreteTypeName,
        String roleName, int roleNumber, Value value) throws MismatchException, ChangeNotAllowedException {
        super(model, parent, supertypeName);
        this.roleName = roleName;
        this.roleNumber = roleNumber;
        createSubnode(text, concreteTypeName, roleName, value);
    }

    private void createSubnode(String text, String concreteTypeName,
        String roleName) throws MismatchException,
        ChangeNotAllowedException {

        // shrinking of object expression at the begin and end
        String textTrimmed = text.trim();

        ExpressionNode subNode;
        ExpressionTreeModel model = getExpressionTreeModel();
        if (concreteTypeName == null) {
            subNode = new TextNode(null, textTrimmed);
        } else {
            ObjectModel om = model.getObjectModel();
            ObjectType concreteType = om.getObjectType(concreteTypeName);

            if (concreteType != null && concreteType.isParsable()) {
                Value sv = concreteType.parse(textTrimmed, null, getExpressionTreeModel().getSource());
                subNode = new ObjectNode(model, null, sv, roleName, 0, concreteType.getOTE());
            } else {
                subNode = new ObjectNode(model, null, textTrimmed, concreteTypeName, roleName, 0);
            }
        }
        model.insertNodeInto(subNode, this, 0);
    }

    private void createSubnode(String text, String concreteTypeName,
        String roleName, Value value) throws MismatchException,
        ChangeNotAllowedException {

        // shrinking of object expression at the begin and end
        String textTrimmed = text.trim();

        ExpressionNode subNode;
        ExpressionTreeModel model = getExpressionTreeModel();
        if (concreteTypeName == null) {
            subNode = new TextNode(null, textTrimmed);
        } else {
            ObjectModel om = model.getObjectModel();
            ObjectType concreteType = om.getObjectType(concreteTypeName);
            subNode = new ObjectNode(model, null, value, roleName, 0, concreteType.getOTE());
        }
        model.insertNodeInto(subNode, this, 0);
    }

    public ExpressionNode getSubNode() {
        if (getChildCount() > 0) {
            return (ExpressionNode) getChildAt(0);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        ExpressionNode subNode = getSubNode();
        if (subNode != null) {
            return getSubNode().toString();
        } else {
            return "No SubNode";
        }
    }

    @Override
    public void registerAtObjectModel() throws MismatchException,
        ChangeNotAllowedException, DuplicateException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        try {
            ParentNode parentSubNode = (ParentNode) getSubNode();
            System.out.println("register subnode of " + getTypeName() + ": " + parentSubNode.getTypeName());
            parentSubNode.registerAtObjectModel();
            om.addSuperType(getTypeName(), parentSubNode.getTypeName(), new ExternalInput("", om.getProject().getCurrentUser()));
            System.out.println("register " + getTypeName());

            FactType ft = om.getFactType(getTypeName());

        } catch (DuplicateException ex) {
            throw new MismatchException(null, ex.getMessage());
        }
    }

    @Override
    public void deregisterAtObjectModel() throws ChangeNotAllowedException {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        om.removeSuperTypeNode(this);
    }

    @Override
    public ObjectType getDefinedType() {
        ObjectModel om = getExpressionTreeModel().getObjectModel();
        return om.getFactType(getTypeName()).getObjectType();
    }

    public ObjectNode getConcreteNode() {
        ExpressionNode subNode = getSubNode();
        if (subNode instanceof ObjectNode) {
            return (ObjectNode) subNode;
        }
        return ((SuperTypeNode) subNode).getConcreteNode();
    }

    @Override
    public String getTypeDescription() {
        return getRoleName() + " : " + getTypeName();
    }

    @Override
    public String getText() {
        return getSubNode().getText();
    }

    @Override
    public String getRoleName() {
        return roleName;
    }

    @Override
    public boolean isReady() {
        ExpressionNode subNode = getSubNode();
        if (subNode != null) {
            return subNode.isReady();
        } else {
            return false;
        }
    }

    @Override
    boolean setReady(boolean ready) {
        if (ready == this.ready) {
            return false;
        }

        boolean backup = this.ready;
        try {
            if (ready) {
                if (getSubNode().isReady()) {
                    this.ready = true;
                    registerAtObjectModel();
                    ParentNode parentNode = getParent();
                    if (parentNode != null) {
                        parentNode.calculateReadyness();
                    }
                } else {
                    return false;
                }
            } else {
                // adjust readyness of subnode
                this.ready = false;
                deregisterAtObjectModel();
                ExpressionTreeModel model = getExpressionTreeModel();
                model.setReady(getSubNode(), false);
            }

            return true;

        } catch (MismatchException | ChangeNotAllowedException | DuplicateException exc) {
            this.ready = backup;
            return false;
        }
    }

    @Override
    public boolean allSubNodesAreReady() {
        return getSubNode().isReady();
    }

    @Override
    public boolean allValueSubNodesAreReady() {
        return getSubNode().isReady();
    }

    @Override
    public int getRoleNumber() {
        return roleNumber;
    }

    @Override
    public void setRoleNumber(int nr) {
        roleNumber = nr;
    }

    @Override
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public int getIndex(TreeNode node) {
        if (getSubNode() == node) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    int roleNumber(int childNr) {
        return 0;
    }

    @Override
    ObjectNode addObjectNodeAt(int nr, int from, int unto, String typeName,
        String roleName, int roleNumber)
        throws MismatchException, ChangeNotAllowedException, DuplicateException {

        if (nr != 0) {
            throw new RuntimeException("supertype node has only one child with index 0");
        }
        ExpressionNode textNode = getSubNode();
        String text = textNode.getText();
        ObjectNode subNode;
        ExpressionTreeModel model = getExpressionTreeModel();

        ObjectModel om = model.getObjectModel();
        ObjectType concreteType = om.getObjectType(typeName);

        if (concreteType != null && concreteType.getOTE().isParsable()) {
            Value sv = concreteType.parse(text, null, getExpressionTreeModel().getSource());
            subNode = new ObjectNode(model, null, sv, roleName, 0, concreteType.getOTE());
        } else {
            subNode = new ObjectNode(model, null, text, typeName, roleName, 0);
        }

        model.removeNodeFromParent(textNode);
        model.insertNodeInto(subNode, this, 0);

        return subNode;
    }

    @Override
    CollectionNode addCollectionNodeAt(int nr, int from, int unto, String collectionName, String roleName, int i, String begin, String separator, String end, String elementName, String elementRoleName, boolean sequence) throws MismatchException, ChangeNotAllowedException, DuplicateException {
        if (nr != 0) {
            throw new RuntimeException("supertype node has only one child with index 0");
        }
        ExpressionNode textNode = getSubNode();
        String text = textNode.getText();
        ExpressionTreeModel model = getExpressionTreeModel();

        ObjectModel om = model.getObjectModel();
        CollectionType ct = (CollectionType) om.getObjectType(collectionName);
        CollectionNode collectionNode;
        ObjectType st = om.getObjectType(elementName);
        boolean abstract_element = st != null && st.isAbstract();
        boolean parsable = (ct != null) && ct.isParsable() && !abstract_element;

        if (ct == null) {
            if (abstract_element) {
                // new collection type with supertype nodes and value nodes
                collectionNode = new CollectionNode(model, null, collectionName, text, roleName, roleNumber,
                    begin, separator, end, elementName, elementRoleName, sequence,
                    true);
            } else {
                // new collection type with value nodes
                collectionNode = new CollectionNode(model, null, collectionName, text, roleName, roleNumber,
                    begin, separator, end, elementName, elementRoleName, sequence,
                    false);
            }
        } else {
            // known collection node 
            if (parsable) {
                // collection node with object nodes or value leafs  
                Tuple sv = (Tuple) ct.parse(text, null, model.getSource());
                this.remove(0);
                collectionNode = new CollectionNode(model, roleName, roleNumber, sv);

                // model.insertNodeInto(tn, collectionNode, 0);
                model.setUnconditionallyReady(collectionNode);

            } else {   // OTE of collection node is only not parsable in case of abstract roles
                // collection node with supertype nodes and value nodes

                collectionNode = new CollectionNode(model, null, collectionName, text, roleName, roleNumber,
                    begin, separator, end, elementName, elementRoleName, sequence, true);

            }
        }
        model.insertNodeInto(collectionNode, this, 0);
        model.nodeStructureChanged(this);

        return collectionNode;
    }
}
