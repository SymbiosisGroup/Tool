/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.meta.objectmodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import symbiosis.meta.ChangeNotAllowedException;
import symbiosis.meta.MismatchException;
import symbiosis.meta.requirements.FactRequirement;
import symbiosis.meta.requirements.Requirement;
import symbiosis.meta.requirements.RequirementModel;
import symbiosis.meta.requirements.RuleRequirement;
import symbiosis.meta.traceability.Category;
import symbiosis.meta.traceability.SystemInput;
import symbiosis.util.Naming;

/**
 *
 * @author frankpeeters
 */
public class ElementsSequenceFactType extends ElementsFactType {

    private static final long serialVersionUID = 1L;

    ElementsSequenceFactType(String ftname, CollectionType ct, SubstitutionType st,
            String roleName, ObjectModel parent, Requirement source) {
        super(ftname, ct, parent, source);

        //initRoles
        roles = new ArrayList<>(3);
        Role role;

        role = new ObjectRole(ct, this);
        roles.add(role);
        role.setRoleName(ct.getName());

        role = new BaseValueRole(BaseType.NATURAL, this);
        roles.add(role);
        role.setRoleName("nr");

        if (st instanceof BaseType) {
            role = new BaseValueRole((BaseType) st, this);
        } else {
            role = new ObjectRole((ObjectType) st, this);
        }
        roles.add(role);
        role.setRoleName(roleName);
        roles.get(1).qualified = roles.get(0);

        //initFTE
        List<String> constants = new ArrayList<>();
        constants.add("");
        constants.add(" contains at index ");
        constants.add(" ");
        constants.add(".");

        fte = new ElementsFactTypeExpression(this, constants);
    }

    void addUniqeness(CollectionType ct, ObjectModel parent) {
        ArrayList<Role> subRoles = new ArrayList<>();
        subRoles.add(roles.get(0));
        subRoles.add(roles.get(1));
        try {
            String uniqueString = "Two different facts about " + getFactTypeString() + " with the same combined value on "
                    + "<" + Naming.withoutCapital(ct.getName()) + " : " + ct.getName() + " , nr : Natural>" + " are not allowed.";
            RuleRequirement rule = parent.getProject().getRequirementModel().
                    addRuleRequirement(Category.SYSTEM,
                            uniqueString,
                            new SystemInput("elements in sequence are identified with sequence and index"));
            new UniquenessConstraint(subRoles, rule);
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ElementsSequenceFactType.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public SubstitutionType getElementType() {
        return roles.get(2).getSubstitutionType();
    }

    @Override
    public Role getElementRole() {
        return roles.get(2);
    }

    @Override
    public List<Value> elementsOf(String collectionID) {
        Population pop = getPopulation();
        Iterator<Tuple> itTuples = pop.tuples();
        List<Value> elements = new ArrayList<>();
        while (itTuples.hasNext()) {
            Tuple tuple = itTuples.next();
            Tuple collection = (Tuple) tuple.getItem(0).getValue();
            String usedcollection = collection.getItem(0).getValue().toString();
            if (usedcollection.equals(collectionID)) {
                elements.add(tuple.getItem(2).getValue());
            }
        }
        return elements;
    }

    @Override
    void addFacts(Tuple collection, List<Value> elements,
            RequirementModel rm) throws MismatchException {
        List<SubstitutionType> types = new ArrayList<>();
        types.add(getCollectionType());
        types.add(BaseType.NATURAL);
        SubstitutionType et = getElementType();
        types.add(et);

        SystemInput input = new SystemInput("Assignment of element to collection is needed.");
        String collectionString = collection.toString();
        int nr = 0;
        for (Value value : elements) {
            FactRequirement source = rm.addFactRequirement(Category.SYSTEM,
                    collectionString
                    + " contains at index " + nr + " " + value.toString() + ".", input);
            List<Value> elementValues = new ArrayList<>();
            elementValues.add(collection);
            elementValues.add(new BaseValue(nr + "", BaseType.NATURAL));
            elementValues.add(value);
            getPopulation().addTuple(elementValues, roles, types, source);
            nr++;
        }
    }

}
