/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import symbiosis.meta.MismatchException;
import symbiosis.meta.NotParsableException;
import symbiosis.meta.classrelations.BooleanRelation;
import symbiosis.meta.classrelations.FactTypeRelation;
import symbiosis.meta.classrelations.ObjectTypeRelation;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.requirements.FactRequirement;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementModel;
import symbiosis.project.Project;
import symbiosis.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class CollectionType extends ObjectType implements Serializable {

    private static final long serialVersionUID = 1L;
    private ElementsFactType elementsFT;

    CollectionType(FactType parent, String begin, String separator, String end) {
        super(parent, new CollectionTypeExpression(parent, Naming.withoutCapital(begin), separator, end));
        this.elementsFT = null;
    }

    public SubstitutionType getElementType() {
        return elementsFT.getElementType();
    }

    void setElementsFactType(ElementsFactType eft) {
        elementsFT = eft;
    }

    public int maxSize() {
        FrequencyConstraint fc = elementsFT.roles.get(0).getFrequencyConstraint();
        if (fc == null) {
            return -1;
        } else {
            return fc.getMax();
        }
    }

    public FrequencyConstraint getFrequencyConstraint() {
        return elementsFT.roles.get(0).getFrequencyConstraint();

    }

    public boolean mayBeEmpty() {
        return !elementsFT.roles.get(0).isMandatory();
    }

    @Override
    public boolean isComparable() {
        return true;
    }

    @Override
    public String getKind() {
        if (isSequence()) {
            return "SQ";
        }
        return "ST";
    }

    public Role getElementRole() {
        return elementsFT.getElementRole();
    }

    public List<Value> elementsOf(String collection) {
        return elementsFT.elementsOf(collection);
    }

    void addElements(Tuple collection, List<Value> elements,
            RequirementModel rm) throws MismatchException {
        elementsFT.addFacts(collection, elements, rm);
    }

    public boolean isSequence() {
        return elementsFT instanceof ElementsSequenceFactType;
    }

    CollectionTuple addCollectionTuple(List<Value> values, Requirement source) throws MismatchException {
        FactType ft = getFactType();
        CollectionTuple collection = ft.getPopulation().addCollectionTuple(values, source);
        Project project = ((ObjectModel)ft.getParent()).getProject();
        this.addElements(collection, values, project.getRequirementModel());
        return collection;
    }

    @Override
    public Value parse(String expression, String separator, Requirement source)
            throws MismatchException {
        CollectionTypeExpression ote = (CollectionTypeExpression) getOTE();
        if (!ote.isParsable()) {
            throw new NotParsableException(ote, "OTE OF " + getName() + " NOT PARSABLE");
        }

        String expressionPart;
        if (separator == null) {
            expressionPart = expression;
        } else {
            int unto = expression.indexOf(separator);
            if (unto == -1) {
                throw new RuntimeException("separator not present");
            }
            expressionPart = expression.substring(unto);
        }
        ParseResult result = ote.parse(expressionPart, null, source);
        List<Value> elements = result.getValues();
        //addSource(source);

        return addCollectionTuple(elements, source);
    }

    @Override
    public void setValueType(boolean valueType) {
        if (this.valueType == valueType) {
            return;
        }

        if (valueType) {
            this.valueType = true;
            for (Role role : plays) {
                if (!role.isDerivable() && !(role.getParent() instanceof ElementsFactType)) {
                    role.setNavigable(false);
                }
            }
        } else {
            this.valueType = false;
        }

        getFactType().fireListChanged();

        publisher.inform(this, "valueType", null, isValueType());

    }

    List<Relation> playsRoleRelations() {
        ArrayList<Relation> list = new ArrayList<>();

        for (Role role : plays) {
            if (!(role.getParent() instanceof ElementsFactType) && role.isNavigable() && !role.isQualifier()) {
                Relation relation;
                if (role.getParent().isClass()) {
                    relation = new ObjectTypeRelation(this, role);
                } else if (role.getParent().nonQualifierSize() == 1) {
                    relation = new BooleanRelation(this, role);
                } else {
                    relation = new FactTypeRelation(this, role);
                }
                list.add(relation);
            }
        }
        return list;
    }

    @Override
    public void remove() {
        elementsFT.remove();
        elementsFT = null;
        super.remove();
    }

    void remove(CollectionTuple tuple) {
        if (elementsFT != null) {
            elementsFT.removeTuplesOf(tuple);
        }
        getFactType().getPopulation().remove(tuple);
    }

}
