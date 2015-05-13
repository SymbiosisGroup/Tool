/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.requirements.ActionRequirement;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.project.ProjectRole;
import equa.util.Naming;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author frankpeeters
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ObjectRole extends Role {

    private static final long serialVersionUID = 1L;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private ObjectType ot;
    @Column
    private boolean navigable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private SettablePermission settable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private AdjustablePermission adjustable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private AddablePermission addable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private RemovablePermission removable;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private InsertablePermission insertable;
    @Column
    private boolean composition;
    private boolean isEventSource;
    private List<RoleEvent> events;
    protected DefaultValueConstraint defaultValue;
    @Transient
    private transient ObjectType toDisconnect;

    void removePermissions() {
        deleteAddable();
        deleteAdjustable();
        deleteInsertable();
        deleteRemovable();
        deleteSettable();
        setComposition(false);
        setIsEventSource(false);
    }

    @Override
    void removeUniquenessConstraint(UniquenessConstraint constraint) {
        removePermissions();
        for (RoleEvent event : events) {
            event.remove();
        }
        events.clear();
        super.removeUniquenessConstraint(constraint);
    }

    public ObjectRole() {
    }

    public ObjectRole(ObjectType ot, FactType parent) {
        super(parent);
        this.ot = ot;
        navigable = true;
        settable = null;
        adjustable = null;
        removable = null;
        insertable = null;
        if (ot.isSingleton()) {
            composition = true;
        } else {
            composition = false;
            addable = null;
        }
        events = new ArrayList<>();
        toDisconnect = null;
        ot.involvedIn(this);
        defaultValue = null;
    }

    @Override
    public boolean hasDefaultValue() {
        return defaultValue != null && ot.isValueType();
    }

    @Override
    public String getDefaultValue() {
        if (defaultValue == null || !ot.isValueType()) {
            return null;
        } else {
            return defaultValue.getValue();
        }
    }

    public void setDefaultValue(Value value) throws MismatchException, ChangeNotAllowedException {
        if (this.defaultValue != null && this.defaultValue.getValue().equalsIgnoreCase(value.toString())) {
            return;
        }

        if (!ot.isValueType()) {
            return;
        }

        String rolename = getRoleName();
        if (rolename.isEmpty()) {
            rolename = Naming.withoutCapital(ot.getName());
        }

        if (this.defaultValue != null) {
            this.defaultValue.remove();
        }

        RuleRequirement rule = (RuleRequirement) value.sources().get(0);

        this.defaultValue = new DefaultValueConstraint(this, value, rule);
    }

    public void addEvent(Requirement rule, FactType eventCondition, boolean negation,
        boolean extending, boolean removing, boolean updating,
        String eventHandler) throws ChangeNotAllowedException {
        for (RoleEvent event : events) {
            if (event.getCondition() == eventCondition && event.isNegation() == negation
                && event.isExtending() == extending && event.isRemoving() == removing
                && event.isUpdating() == updating) {
                return;
            }
        }
        events.add(new RoleEvent(rule, this, eventCondition, negation,
            extending,  updating, removing, eventHandler));
    }

    public boolean removeEvent(FactType condition, boolean negation,
        boolean extending, boolean removing, boolean updating) {
        RoleEvent toRemove = null;
        for (RoleEvent event : events) {
            if (event.getCondition() == condition && event.isNegation() == negation
                && event.isExtending() == extending && event.isRemoving() == removing
                && event.isUpdating() == updating) {

                toRemove = event;
            }
        }
        if (toRemove != null) {
            events.remove(toRemove);
            toRemove.remove();
            return true;
        } else {
            return false;
        }
    }

    public void removeEvents() {
        for (int i = events.size() - 1; i >= 0; i--) {
            events.get(i).remove();
            events.remove(events.get(i));
        }
    }

    public List<RoleEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public AddablePermission getAddable() {
        return addable;
    }

    public RemovablePermission getRemovable() {
        return removable;
    }

    public SettablePermission getSettable() {
        return settable;
    }

    public AdjustablePermission getAdjustable() {
        return adjustable;
    }

    public InsertablePermission getInsertable() {
        return insertable;
    }

    @Override
    public boolean isAddable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return addable != null && isNavigable() && !isDerivable() && isMultiple();
    }

    @Override
    public boolean isInsertable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return insertable != null && isNavigable() && !isDerivable() && isMultiple();
    }

    @Override
    public ObjectType getSubstitutionType() {
        return ot;
    }

    @Override
    public boolean isNavigable() {
        if (isQualifier()) {
            return false;
        }
//        Role counterpart = getParent().counterpart(this);
//        if (counterpart!=null && counterpart.getSubstitutionType().isSingleton()){
//            return false;
//        }
        return navigable;
    }

    @Override
    public void setNavigable(boolean navigable) {
        if (this.navigable == navigable) {
            return;
        }
        if (getParent().isDerivable()) {
            getParent().getDerivableConstraint().remove();
        }
        if (navigable == false) {
            this.navigable = false;

            if (addable != null) {

                addable.remove();

            }
            if (removable != null) {
                removable.remove();
            }
            if (settable != null) {
                settable.remove();
            }
            if (adjustable != null) {
                adjustable.remove();
            }
            if (insertable != null) {
                insertable.remove();
            }

        } else {
            this.navigable = true;
        }

        getParent().correctQualifyingRoles(
            this);

        getParent().fireListChanged();

        publisher.inform(
            this, "navigable", null, navigable);
    }

    @Override
    public boolean isAbstract() {
        return ot.isAbstract();
    }

    @Override
    SubstitutionType disconnect() {
        toDisconnect = ot;
        toDisconnect.resignFrom(this);
        return toDisconnect;
    }

    @Override
    void reconnect() {
        ot = toDisconnect;
        toDisconnect = null;
        ot.involvedIn(this);
    }

    public void setIsEventSource(boolean isEventSource) {
        this.isEventSource = isEventSource;
    }

    @Override
    public boolean isComposition() {
        if (isCandidateComposition()) {
            return composition;
        } else {
            return false;
        }
    }

    @Override
    public boolean setComposition(boolean composition) {
        if (composition == this.composition) {
            return false;
        }

        //   if (getParent().qualifiersOf(this).isEmpty()) {
        if (composition) {
            if (isCandidateComposition()) {
                this.composition = true;
                Iterator<Role> itRoles = getParent().roles();
                while (itRoles.hasNext()) {
                    Role otherRole = itRoles.next();
                    if (otherRole != this && otherRole instanceof ObjectRole) {
                        ObjectRole objectRole = (ObjectRole) otherRole;
                        objectRole.deleteSettable();
                        objectRole.deleteAdjustable();
                        objectRole.deleteInsertable();
                        objectRole.deleteAddable();
                        objectRole.deleteRemovable();
                    }
                }
                // default: relation can be added
                addAddable("composition relation goes normally along with adding facilities");
                //addRemovable("composition relation goes normally along with removing facilities");

                getParent().fireListChanged();
                publisher.inform(this, "composition", null, true);
                return true;
            } else {
                return false;
            }
        } else {
            this.composition = false;
            getParent().fireListChanged();
            publisher.inform(this, "composition", null, false);
            return true;
        }

    }

    @Override
    public void deleteMandatoryConstraint() {
        Role counterpart = getParent().counterpart(this);
        if (counterpart != null) {
            if (!counterpart.isComposition()) {
                super.deleteMandatoryConstraint();
            }
            counterpart.removeDefaultValue();
        } else {
            super.deleteMandatoryConstraint();
        }
    }

    @Override
    public void removeDefaultValue() {
        if (defaultValue != null) {

            defaultValue.remove();
            defaultValue = null;

        }
    }

    void deleteDefaultValue() {
        if (defaultValue != null) {
            defaultValue = null;
        }
    }

    @Override
    public boolean isSettable() {
        return settable != null && !isDerivable() && isNavigable();
    }

    @Override
    public boolean isAdjustable() {
        return adjustable != null && !isDerivable() && isNavigable();
    }

    @Override
    public boolean isRemovable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return removable != null && isNavigable() && !isDerivable()
            && (isMultiple() || !isMandatory());
    }

    /**
     * settable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     *
     */
    public void addSettable(String justification) {
        if (isCandidateSettable()) {
            if (settable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement rule = rm.addActionRequirement(getCategory(),
                    "XX",
                    new ExternalInput(justification, projectRole));

                this.settable = new SettablePermission(this, rule);
                getParent().fireListChanged();
            }
        }
    }

    private Category getCategory() {
        return getParent().getCategory();
    }

    public void addAdjustable(String justification) {
        if (isCandidateAdjustable()) {
            if (adjustable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement action = rm.addActionRequirement(getCategory(),
                    "XX",
                    new ExternalInput(justification, projectRole));

                this.adjustable = new AdjustablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    public void addInsertable(String justification) {
        if (isCandidateInsertable()) {
            if (insertable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                ActionRequirement action = rm.addActionRequirement(getCategory(),
                    "Some actor of the (sub)system must get the opportunity "
                    + "to insert a fact of " + getParent().getFactTypeString()
                    + " later on.",
                    new ExternalInput(justification, projectRole));
                this.insertable = new InsertablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    void deleteSettable() {
        if (settable != null) {
            settable = null;
            publisher.inform(this, "settable", null, isSettable());
        }
    }

    void deleteAdjustable() {
        if (adjustable != null) {
            adjustable = null;
        }
    }

    void deleteInsertable() {
        if (insertable != null) {
            insertable = null;
        }
    }

    @Override
    public boolean isCandidateAddable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return !ot.isValueType() && isMultiple() && (this.isResponsible() || this.isCandidateResponsible())
            && !ot.getFactType().isDerivable();
    }

    @Override
    public boolean isCandidateAdjustable() {
        if (!hasSingleTarget() || ot.isValueType()) {
            return false;
        }
        Role counterpart = getParent().counterpart(this);
        if (counterpart == null || !counterpart.getSubstitutionType().isNumber()) {
            return false;
        } else {
            return (this.isResponsible() || this.isCandidateResponsible())
                && !ot.getFactType().isDerivable();
        }
    }

    @Override
    public boolean isCandidateSettable() {
        if (!hasSingleTarget() || (ot.isValueType() && isMandatory())) {
            return false;
        }
        return (!this.isMappingRole() && this.isResponsible() || this.isCandidateResponsible())
            && !ot.getFactType().isDerivable();
    }

    @Override
    public boolean isCandidateInsertable() {
        List<Role> qualifiers = getParent().qualifiersOf(this);
        if (qualifiers.isEmpty() || ot.isValueType()) {
            return false;
        } else {
            FrequencyConstraint fc = getFrequencyConstraint();
            if (fc != null && fc.getMax() == fc.getMin()) {
                return false;
            }
            return (this.isResponsible() || this.isCandidateResponsible())
                && !ot.getFactType().isDerivable();
        }
    }

    @Override
    public boolean isCandidateRemovable() {
        FrequencyConstraint fc = getFrequencyConstraint();
        if (fc != null && fc.getMax() == fc.getMin()) {
            return false;
        }
        return !ot.isValueType() && (this.isMultiple() || !this.isMandatory())
            && (this.isResponsible() || this.isCandidateResponsible())
            && !ot.getFactType().isDerivable();
    }

    public boolean isCandidateEventSource() {
        return isResponsible() || (isNavigable() && !getParent().isValueType());
    }

    @Override
    public boolean isCandidateComposition() {
        if (ot.getFactType().isDerivable()) {
            return false;
        }

        Role counterpart = getParent().counterpart(this);
        if (counterpart != null) {
            if (counterpart.getSubstitutionType().isValueType()) {
                return false;
            } else if (((ObjectRole) counterpart).composition) {
                return false;
            } else {
                if (!counterpart.isMandatory() || counterpart.isMultiple()) {
                    return false;
                }
            }
            return true;
        } else {
            return !getSubstitutionType().isValueType() && getParent().isObjectType();
        }
    }

    SubstitutionType targetType() {
        if (getParent().isObjectType()) {
            return getParent().getObjectType();
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart == null) {
                return BaseType.BOOLEAN;
            } else {
                return counterpart.getSubstitutionType();
            }
        }
    }

    /**
     * addable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     */
    public final void addAddable(String justification) {
        if (isCandidateAddable()) {
            if (addable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                SubstitutionType target = targetType();
                Category cat = null;
                if (target instanceof BaseType) {
                    cat = getCategory();
                } else {
                    cat = ((ObjectType) target).getFactType().getCategory();
                }
                ActionRequirement action = rm.addActionRequirement(cat,
                    "Some actor of the system must get the opportunity to add a "
                    + target.getName(),
                    new ExternalInput(justification, projectRole));
                addable = new AddablePermission(this, action);
                publisher.inform(this, "addable", null, isAddable());
            }
        }
    }

    void deleteAddable() {
        if (addable != null) {
            addable = null;
            publisher.inform(this, "addable", null, isAddable());
        }
    }

    /**
     * removable constraint will be added; concerning rule requirement made by
     * active project member will be added to
     *
     * @param justification made by active project member
     *   * @param cat category of removable constraint
     */
    public void addRemovable(String justification) {
        if (isCandidateRemovable()) {
            if (removable == null) {
                ObjectModel om = (ObjectModel) getParent().getParent();
                RequirementModel rm = om.getProject().getRequirementModel();
                ProjectRole projectRole = om.getProject().getCurrentUser();
                SubstitutionType target = targetType();
                Category cat = null;
                if (target instanceof BaseType) {
                    cat = getCategory();
                } else {
                    cat = ((ObjectType) target).getFactType().getCategory();
                }
                ActionRequirement action = rm.addActionRequirement(cat,
                    "Some actor of the system must get the opportunity to remove a "
                    + target.getName(),
                    new ExternalInput(justification, projectRole));
                this.removable = new RemovablePermission(this, action);
                getParent().fireListChanged();
            }
        }
    }

    void deleteRemovable() {
        if (removable != null) {
            removable = null;
            publisher.inform(this, "removable", null, isRemovable());
        }
    }

    @Override
    public boolean isSeqNr() {
        return false; //ot.isSuitableAsIndex();
    }

//    @Override
//    public void setSeqNr(boolean seqNr) {
//    }
    /**
     *
     * @param at must be a supertype of actual substitutiontype of this role
     */
    void generalize(ObjectType at) {
        this.ot.resignFrom(this);
        this.ot = at;
        at.involvedIn(this);
    }

    /**
     * preconditions: a) substitutiontype must possess exactly one subtype b)
     * substitutiontype must be abstract
     */
    void specialize() {
        ObjectType subtype = ot.subtypes().next();
        this.ot.resignFrom(subtype);
        this.ot = subtype;
        subtype.involvedIn(this);
    }

    @Override
    public void remove() {
        if (addable != null) {
            addable.remove();
            addable = null;
        }

        if (removable != null) {
            removable.remove();
            removable = null;
        }

        if (settable != null) {
            settable.remove();
            settable = null;
        }

        if (adjustable != null) {
            adjustable.remove();
            adjustable = null;
        }

        if (insertable != null) {
            insertable.remove();
            insertable = null;
        }

        for (RoleEvent event : events) {
            event.remove();
        }
        events.clear();

        if (defaultValue != null) {
            defaultValue.remove();
            defaultValue = null;
        }

        this.ot.resignFrom(this);
        if (ot.isSolitary()) {
            ot.getFactType().remove();
        }
        super.remove();

    }

    @Override
    public String getConstraintString() {
        Iterator<StaticConstraint> it = constraints();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            sb.append(it.next().getDescription()).append(" ");
        }
        if (isQualifier()) {
            if (isSeqNr()) {
                sb.append("seq ");
            } else {
                sb.append("map ");
            }
        }
        if (!isNavigable()) {
            sb.append("!nav ");
            if (hasDefaultValue()) {
                sb.append(" " + defaultValue.getValue());

            }
        } else {
            String eventSource = "sys ";

            if (isHidden()) {
                sb.append("hid ");
            }
            if (isAddable()) {
                sb.append("add ");
                eventSource = "";
            }
            if (isSettable()) {
                sb.append("set ");
                eventSource = "";
            }
            if (isAdjustable()) {
                sb.append("adj ");
                eventSource = "";
            }
            if (isInsertable()) {
                sb.append("ins ");
                eventSource = "";
            }
            if (isRemovable()) {
                sb.append("rem ");
                eventSource = "";
            }
            if (isComposition()) {
                sb.append("comp ");
                eventSource = "";
            }

            for (RoleEvent event : events) {
                Requirement rule = (Requirement) event.mostRecentSource();
                sb.append(rule.getId()).append(" ");
                eventSource = "";
            }
            if (isEventSource()) {
                sb.append(eventSource);
            }
        }
        return sb.toString().trim();

    }

    public void setSubstitutionType(ObjectType selected) throws ChangeNotAllowedException {
        if (ot.hasSuperType(selected) || ot.hasSubType(selected)) {

            if (ot.containsTuplesOfOtherSubtypes(selected)) {
                throw new ChangeNotAllowedException("Object of other subtype is involved within this fact type");
            }

            String oldName = ot.getName();
            ot.resignFrom(this);

            ot = selected;
            ot.involvedIn(this);
            getParent().notifyTypeExpressions();

            // trial to improve the name of the fact type
            ObjectModel om = (ObjectModel) getParent().getParent();
            int from = getParent().getName().indexOf(oldName);
            if (from != -1) {
                String name = getParent().getName();
                String newName = name.substring(0, from) + ot.getName() + name.substring(from + oldName.length());
                try {
                    om.renameFactType(getParent(), newName);
                } catch (DuplicateException exc) {
                }
            }

        } else {
            throw new ChangeNotAllowedException("selected is not a super or subtype of " + ot.getName());
        }
    }

    @Override
    public void expandSubstitutionType(ObjectType ot) {
        try {
            setSubstitutionType(ot);

        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectRole.class
                .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    boolean isResponsibleForNonVT() {
        if (!isNavigable()) {
            return false;
        }

        FactType parent = getParent();
        if (parent.isObjectType()) {
            return isResponsible();
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart != null) {
                // binary fact type
                SubstitutionType st = counterpart.getSubstitutionType();
                return isResponsible() && !st.isTerminal() && !st.isValueType();
            } else {
                return false;
            }
        }
    }

    public boolean isEventSource() {
        return isEventSource;
    }

    @Override
    public boolean isResponsible() {
        if (/*!getParent().isValueType() &&*/ (isAddable() || isRemovable() || isSettable() || isInsertable() || isAdjustable() || isComposition()
            || isBoolean())) {
            return true;
        }
        if (isEventSource) {
            return true;
        }
        Role counterpart = getParent().counterpart(this);
        if (counterpart != null) {
            if (!counterpart.isNavigable() && isNavigable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void remove(ModelElement member) {
        //try {
        if (member.equals(addable)) {

            addable = null;

        } else if (member.equals(adjustable)) {

            adjustable = null;

        } else if (member.equals(settable)) {

            settable = null;

        } else if (member.equals(insertable)) {

            insertable = null;

        } else if (member.equals(removable)) {

            removable = null;
        } else if (member.equals(defaultValue)) {

            defaultValue = null;
        } else {

            super.remove(member);
        }
    }

    @Override
    public String getName() {
        return "Role " + getNamePlusType();
    }

    @Override
    public boolean equals(Object object) {
        return object == this;
    }

    @Override
    public boolean isCreational() {
        if (isComposition()) {
            return true;
        }

        boolean creational = (isAddable() || isInsertable()
            || isSettable());

        if (getParent().isObjectType()) {
            return creational;
        } else {
            Role counterpart = getParent().counterpart(this);
            if (counterpart == null) {
                return false;
            }
            return creational && counterpart.isMandatory();
        }
    }

    @Override
    public boolean isMutable() {
        return isAddable() || isInsertable() || isRemovable() || isSettable() || isAdjustable() || isEventSource;
    }

    @Override
    public boolean isAutoIncr() {
        return false;
    }

    @Override
    public boolean isCandidateAutoIncr() {
        return false;
    }

    @Override
    public boolean isMappingRole() {
        if (isQualifier()) {
            return false;
        }
        List<Role> qualifiers = getParent().qualifiersOf(this);
        if (qualifiers.isEmpty()) {
            return false;
        } else {
            return qualifiers.size() > 1 || !qualifiers.get(0).isSeqNr();
        }

    }

    @Override
    boolean isBoolean() {
        FactType ft = getParent();
        if (ft.isObjectType()) {
            return false;
        }
        int qualifiers = ft.qualifiersOf(this).size();
        if (ft.size() - qualifiers == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCandidateDefaultValue() {
       return false;
    }

}
