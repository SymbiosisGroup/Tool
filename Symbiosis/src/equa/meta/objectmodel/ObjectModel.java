package equa.meta.objectmodel;

import static equa.code.CodeNames.SYSTEM_CLASS;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import equa.code.CodeClass;
import equa.code.Language;
import equa.code.operations.ActualParam;
import equa.code.systemoperations.IndexAllowedMethod;
import equa.code.systemoperations.IsEqualMethod;
import equa.code.systemoperations.IsNaturalMethod;
import equa.code.systemoperations.UnknownMethod;
import equa.factbreakdown.CollectionNode;
import equa.factbreakdown.FactNode;
import equa.factbreakdown.ObjectNode;
import equa.factbreakdown.ParentNode;
import equa.factbreakdown.SuperTypeNode;
import equa.factbreakdown.ValueNode;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.Message;
import equa.meta.MismatchException;
import equa.meta.Model;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.meta.traceability.Category;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.Source;
import equa.meta.traceability.SystemInput;
import equa.project.Project;
import equa.util.Naming;
import equa.util.NumberIssue;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 *
 * @author FrankP
 */
@Entity
public class ObjectModel extends Model implements
    ListModel<FactType>, Publisher, Serializable, ActualParam {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private final TypeRepository typeRepository;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<FactType> artificialSingletons;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<FactType> artificialSingletonFactTypes;
    @Transient
    private transient EventListenerList listenerList;
    @Transient
    private transient BasicPublisher publisher;
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private final NumberIssue constraintNumberIssue;
    @Transient
    private boolean lightBehavior;
    private CodeClass codeClass;

    public ObjectModel() {
        listenerList = new EventListenerList();
        initPublisher();
        typeRepository = new TypeRepository();
        artificialSingletonFactTypes = new ArrayList<>();
        artificialSingletons = new ArrayList<>();
        constraintNumberIssue = new NumberIssue();

    }

    public void addValueTypes(RequirementModel rm) {
        List<String> valuetypes = new ArrayList<>();

        valuetypes.add("Date");
        valuetypes.add("Time");
        valuetypes.add("DateTime");
        valuetypes.add("TimeSec");
        valuetypes.add("DateTimeSec");
        valuetypes.add("Money");
        valuetypes.add("Name");
        valuetypes.add("Address");

        ArrayList<JCheckBox> typesToChoose = new ArrayList();

        for (String vt : valuetypes) {
            JCheckBox box = new JCheckBox(vt);
            typesToChoose.add(box);
        }

        Object[] containerObj = (Object[]) typesToChoose.toArray(new Object[typesToChoose.size()]);
        JOptionPane.showConfirmDialog(null, containerObj, "Choose ValueTypes to add", JOptionPane.OK_CANCEL_OPTION);

        for (JCheckBox cb : typesToChoose) {
            if (!cb.isSelected()) {
                valuetypes.remove(cb.getText());
            }
        }

        initValueTypes(valuetypes, rm);
    }

    public void initValueTypes(List<String> valuetypes, RequirementModel rm) {
        try {
            String typeName;
            List<String> constants;
            List<SubstitutionType> types;
            List<String> roleNames;
            List<Integer> roleNumbers;
            List<Source> sources;
            FactRequirement fact;

            if (valuetypes.contains("Date") || valuetypes.contains("DateTime")
                || valuetypes.contains("DateTimeSec")) {
                typeName = "Date";
                constants = new ArrayList<>();
                constants.add("");
                constants.add("-");
                constants.add("-");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.NATURAL);
                types.add(BaseType.NATURAL);
                types.add(BaseType.NATURAL);
                roleNames = new ArrayList<>();
                roleNames.add("year");
                roleNames.add("month");
                roleNames.add("day");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                roleNumbers.add(2);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "2015-1-1", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("Date").setValueType(true);
            }
            if (valuetypes.contains("TimeSec") || valuetypes.contains("DateTimeSec")) {
                typeName = "TimeSec";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(":");
                constants.add(":");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.NATURAL);
                types.add(BaseType.NATURAL);
                types.add(BaseType.REAL);
                roleNames = new ArrayList<>();
                roleNames.add("hour");
                roleNames.add("min");
                roleNames.add("sec");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                roleNumbers.add(2);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "12:0:0", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("TimeSec").setValueType(true);
            }
            if (valuetypes.contains("Time") || valuetypes.contains("DateTime")) {
                typeName = "Time";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(":");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.NATURAL);
                types.add(BaseType.NATURAL);
                roleNames = new ArrayList<>();
                roleNames.add("hour");
                roleNames.add("min");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "12:0", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("Time").setValueType(true);
            }
            if (valuetypes.contains("DateTimeSec")) {
                typeName = "DateTimeSec";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add("");
                types = new ArrayList<>();
                types.add(getObjectType("Date"));
                types.add(getObjectType("TimeSec"));
                roleNames = new ArrayList<>();
                roleNames.add("date");
                roleNames.add("time");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "2015-1-1 12:0:0", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("DateTimeSec").setValueType(true);
            }
            if (valuetypes.contains("DateTime")) {
                typeName = "DateTime";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add("");
                types = new ArrayList<>();
                types.add(getObjectType("Date"));
                types.add(getObjectType("Time"));
                roleNames = new ArrayList<>();
                roleNames.add("date");
                roleNames.add("time");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "2015-1-1 12:0", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("DateTime").setValueType(true);
            }
            if (valuetypes.contains("Money")) {
                typeName = "Money";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.REAL);
                types.add(BaseType.STRING);
                roleNames = new ArrayList<>();
                roleNames.add("amount");
                roleNames.add("currency");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "100 euro", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                getObjectType("Money").setValueType(true);
            }
            if (valuetypes.contains("Name")) {
                typeName = "Name";
                addAbstractObjectType(typeName, new ExternalInput("", getProject().getCurrentUser()));

                typeName = "NameWithoutMiddle";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                roleNames = new ArrayList<>();
                roleNames.add("firstName");
                roleNames.add("lastName");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "FirstName LastName", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);

                typeName = "NameWithMiddle";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add(" ");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                roleNames = new ArrayList<>();
                roleNames.add("firstName");
                roleNames.add("middleName");
                roleNames.add("lastName");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                roleNumbers.add(2);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "FirstName MiddleName LastName", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);

                ObjectType nameOT = getObjectType("Name");
                getObjectType("NameWithoutMiddle").addSuperType(nameOT);
                getObjectType("NameWithMiddle").addSuperType(nameOT);
                nameOT.setValueType(true);
            }
            if (valuetypes.contains("Address")) {
                typeName = "Address";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" ");
                constants.add(" ");
                constants.add(", ");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                types.add(BaseType.STRING);
                roleNames = new ArrayList<>();
                roleNames.add("street");
                roleNames.add("houseNr");
                roleNames.add("city");
                roleNames.add("country");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                roleNumbers.add(2);
                roleNumbers.add(3);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "Street HouseNr, City, Country", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                ObjectType address = getObjectType("Address");
                address.setValueType(true);

                typeName = "Zip";
                constants = new ArrayList<>();
                constants.add("");
                constants.add("");
                types = new ArrayList<>();
                types.add(BaseType.STRING);
                roleNames = new ArrayList<>();
                roleNames.add("code");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                sources = new ArrayList<>();
                fact = rm.addFactRequirement(Category.SYSTEM, "5661AL", new SystemInput());
                sources.add(fact);
                addObjectType(typeName, constants, types, roleNames, roleNumbers, sources);
                ObjectType zip = getObjectType("Zip");
                zip.setValueType(true);

                typeName = "ZipOfAddress";
                constants = new ArrayList<>();
                constants.add("");
                constants.add(" is the zip code of ");
                constants.add(".");
                types = new ArrayList<>();
                types.add(zip);
                types.add(address);
                roleNames = new ArrayList<>();
                roleNames.add("zip");
                roleNames.add("address");
                roleNumbers = new ArrayList<>();
                roleNumbers.add(0);
                roleNumbers.add(1);
                fact = rm.addFactRequirement(Category.SYSTEM, "0000AA is the zip code of Street HouseNr, City, Country.", new SystemInput());
                FactType zipOfAddress = addFactType(typeName, constants, types, roleNames, roleNumbers, fact);
                ObjectRole addressRole = (ObjectRole) zipOfAddress.getRole(1);
                RuleRequirement rule = rm.addRuleRequirement(Category.SYSTEM, "Every Address has at most one zip code", new SystemInput());
                new UniquenessConstraint(addressRole, rule);
                addressRole.setNavigable(true);
                addressRole.addSettable("One must get the opportunity to set the zip code of an address.");
            }

        } catch (MismatchException ex) {
            Logger.getLogger(ObjectModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DuplicateException ex) {
            Logger.getLogger(ObjectModel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Collection<FactType> getFactTypes() {
        return Collections.unmodifiableCollection(typeRepository.getFactTypeCollection());
    }

    public final void addSystemMethods() {
        codeClass = new CodeClass(this);
        codeClass.addOperation(new IsEqualMethod(this, this));
        codeClass.addOperation(new IndexAllowedMethod(this, this));
        codeClass.addOperation(new UnknownMethod(this, this));
        codeClass.addOperation(new IsNaturalMethod(this, this));
        codeClass.initSpecs();
    }

    public IsEqualMethod getIsEqualMethod() {
        return (IsEqualMethod) codeClass.getOperation("isEqual");
    }

    public IndexAllowedMethod getIndexAllowedMethod() {
        return (IndexAllowedMethod) codeClass.getOperation("indexAllowed");
    }

    public UnknownMethod getUnknownMethod() {
        return (UnknownMethod) codeClass.getOperation("isUnknown");
    }

    public IsNaturalMethod getIsNaturalMethod() {
        return (IsNaturalMethod) codeClass.getOperation("isNatural");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        listenerList = new EventListenerList();
        initPublisher();
    }

    /**
     * creation of object model with the given name; the model contains no fact
     * getFactTypeCollection
     */
    public ObjectModel(Project parent) {
        super(parent);
        typeRepository = new TypeRepository();
        artificialSingletons = new ArrayList<>();
        artificialSingletonFactTypes = new ArrayList<>();
        listenerList = new EventListenerList();
        initPublisher();
        constraintNumberIssue = new NumberIssue();
        lightBehavior = true;
    }

    public boolean requiresLightBehavior() {
        return lightBehavior;
    }

    public NumberIssue getConstraintNumberIssue() {
        return constraintNumberIssue;
    }

    /**
     * if (there are no errors) then (generation of a constructor, properties
     * and methods of all objecttypes; if withRegistries = true then all needed
     * registries are added first) else (no behavior and registries will be
     * created)
     *
     * @param withRegistries
     * @param light
     * @return a list with all errors and warnings
     */
    public List<Message> generateClasses(boolean withRegistries, boolean light) {
        addSystemMethods();

        for (FactType ft : typeRepository.getFactTypeCollection()) {
            if (ft.isClass()) {
                ft.getObjectType().clearRelations();
            }
        }

        ArrayList<Message> messages = scanModel();
        boolean withoutErrors = true;
        for (Message message : messages) {
            if (message.isError()) {
                withoutErrors = false;
                break;
            }
        }
        if (withoutErrors) {
            if (withRegistries) {
                generateRegistries();
            }

            lightBehavior = light;

            for (FactType ft : typeRepository.getFactTypeCollection()) {
                ft.setFactTypeClassIfNeeded();
                if (withRegistries) {
                    withoutErrors = scanRemove(ft, messages, withoutErrors);
                }
            }

            if (withoutErrors) {

                // creation of class skeleton, with fields and properties
                Set<ObjectType> classes = new HashSet<>();
                for (FactType ft : typeRepository.getFactTypeCollection()) {
                    if (ft.isClass()) {
                        ft.getObjectType().generateClass();
                        classes.add(ft.getObjectType());
                    }
                }

                generateConstructors(classes);
                generateMethods(classes);

                for (ObjectType ot : classes) {
                    ot.getCodeClass().initSpecs();
                }

            }

            fireListChanged();
        }

        return messages;
    }

    private boolean scanRemove(FactType ft, ArrayList<Message> messages, boolean withoutErrors) {
        final boolean INCLUDING_NON_NAVIGABLES = false;
        final boolean INCLUDING_RELATIONS_SUPERTYPE = true;
        if (ft.isRemovable()) {
            for (Relation relation : ft.relations(INCLUDING_NON_NAVIGABLES, INCLUDING_RELATIONS_SUPERTYPE)) {
                if (!relation.isResponsible() && !relation.isDerivable() /*&& !(relation instanceof BooleanRelation)*/) {
                    Relation inverse = relation.inverse();
                    if (inverse != null) {
                        ObjectType target = inverse.getOwner();
                        if (inverse.isNavigable()) {
                            if (inverse.isMandatory()) {
                                ObjectType inverseResponsible = inverse.getOwner().getResponsible();
                                if (inverseResponsible == null || !inverseResponsible.equals(ft.getObjectType())) {
                                    ObjectType responsible = null;
                                    if (ft.isObjectType()) {
                                        responsible = ft.getObjectType().getResponsible();
                                    }
                                    if (responsible == null || !responsible.equals(inverse.getOwner())) {

                                        if (!target.isRemovable()) {
                                            StringBuilder messageText = new StringBuilder();
                                            messageText.append("Error: " + target.getName() + " isn't removable ");
                                            ObjectType targetResponsible = target.getResponsible();
                                            if (targetResponsible != null) {
                                                messageText.append("(at : ").append(targetResponsible.getName()).append(")");
                                            }
                                            messageText.append(" while ").append(ft.getName()).
                                                append(", in contrary, is removable; hint:  \n\t" + "a) make the relation compositional or \n\tb) make the relation from ").
                                                append(target.getName()).append(" to ").append(ft.getName()).
                                                append(" non navigable or \n\tc) reduce the relation from ").
                                                append(ft.getName()).append(" to ").append(target.getName()).
                                                append(" to a relation to its id (in other words, consider to deobjectify the "
                                                    + "corresponding role).");

                                            messages.add(new Message(messageText.toString(), true));
                                            withoutErrors = false;
                                        }
                                    }
                                }
                            }
                            ObjectType ot = ft.getObjectType();
                            if (ot != null && (!relation.isNavigable())) {
                                ObjectType responsible = ot.getResponsible();

                                if (responsible != null
                                    && (!target.equals(responsible) && !responsible.hasSubType(target))
                                    && !target.equals(ot) && !target.hasSubType(ot)) {
                                    StringBuilder messageText = new StringBuilder();

                                    messageText.append("Error: removing of a(n) ").append(ot.getName()).
                                        append(" can lead to a dangling ").append(relation.getParent().getName() + "." + inverse.name()).
                                        append("-object;\n");
                                    messageText.append("\t\tadvice: perhaps you need to open the navigability from ").
                                        append(ot.getName()).append(" to ").append(relation.getParent().getName()).append(".").append(inverse.name()).
                                        append(";\n" + "\t\tor perhaps it is possible to deobjectify the role played by ").
                                        append(ft.getName()).append(".");
                                    messages.add(new Message(messageText.toString(), true));
                                    withoutErrors = false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return withoutErrors;
    }

    private void generateConstructors(Set<ObjectType> classes) {
        Set<ObjectType> todo = new HashSet<>(classes); // object types still without constructor
        Set<ObjectType> toRemove;
        Iterator<ObjectType> it;
        while (!todo.isEmpty()) {
            toRemove = new HashSet<>();
            for (ObjectType ot : todo) {
                it = ot.supertypes();// assumption: at most 1 supertype
                if (it.hasNext() && todo.contains(it.next())) {
                    // constructor of ot cannot be created because of missing super constructor 
                } else if (ot.creatableElementsIn(todo)) {
                    // constructor of ot cannot be created because it cannot create an object of a class still without constructor
                } else {
                    ot.generateConstructor();
                    toRemove.add(ot);
                }
            }
            todo.removeAll(toRemove);
        }

    }

    private void generateMethods(Set<ObjectType> classes) {
        for (ObjectType ot : classes) {
            ot.generateMethods();
        }
    }

    public ArrayList<Message> scanModel() {
        ArrayList<Message> messages = new ArrayList<>();
        removeArtificialFactTypes();
        boolean error = false;

        for (FactType facttype : typeRepository.getFactTypeCollection()) {
            if (!facttype.isElementary()) {
                messages.add(new Message(facttype.getName()
                    + " isn't elementary, perhaps due to the fact it's missing "
                    + "a uniqueness constraint.", true));
                error = true;
            } else {
                if (facttype.hasToManyResponsibleRoles()) {
                    messages.add(new Message("error: " + facttype.getName()
                        + " has more than one responsible role.", true));
                    error = true;
                }

                if (facttype.isObjectType()) {
                    ObjectType ot = facttype.getObjectType();
                    if (ot.isAbstract() && ot.concreteSubTypes().isEmpty()) {
                        messages.add(new Message("warning: " + ot.getName()
                            + " is abstract, but doesn't possess any concrete derived object type.", false));
                    }

                    if (ot.isReflexiveCreational()) {
                        messages.add(new Message("error: " + ot.getName()
                            + " is part of compositional cycle; it is not allowed to create yourself.", true));
                        error = true;
                    }

                    if (ot.doesHaveMoreParents()) {
                        messages.add(new Message("error: " + ot.getName()
                            + " does have more than one parent class.", true));
                        error = true;

                    }

                    if (ot.isReducable()) {
                        messages.add(new Message("warning: "
                            + "consider to deobjectify " + ot.getName(), false));
                    }

                } else {if (facttype.isMissingResponsibleRole()) {
                        messages.add(new Message("error: " + facttype.getName()
                            + " misses perhaps a responsible role which offers the opportunity to add, insert or remove a fact.\n"
                            + "\tor perhaps you have to change one of the roles into a composition role\n"
                            + "\tor perhaps one of the roles is influenced by system event", true));
                        error = true;
                   }
                    if (!facttype.hasNavigableRoles() && facttype.size() > 0) {
                        messages.add(new Message("error: " + facttype.getName()
                            + " does not have any navigable role.", true));
                        error = true;
                    }
                    if (facttype.isSuspiciousCandidateClass()) {
                        messages.add(new Message("error: " + facttype.getName() + " possesses a role without a "
                            + "uniqueness constraint;\n"
                            + "\tconsider to objectify the roles with a common uniqueness.\n"
                            + "\tor perhaps one of the roles should be a qualifier role", true));
                        error = true;
                    }
                    if (facttype.hasOptionalImmutableRoles()) {
                        messages.add(new Message("warning: " + facttype.getName() + " possesses an optional role whose"
                            + " value cannot be changed later on", false));
                    }

                    Role navigableRole = facttype.getNavigableRole();

                    if (facttype.isDerivable() && navigableRole == null) {
                        messages.add(new Message("error: derivable facttype " + facttype.getName()
                            + " must have exactly one navigable role.", true));
                        error = true;
                    }
//                    if (facttype.isDerivable() && navigableRole != null
//                        && !navigableRole.isMandatory()) {
//                        messages.add(new Message("error: derivable facttype " + facttype.getName()
//                            + " should have exactly one navigable mandatory role.", true));
//                        error = true;
//                    }

                    if (facttype.hasNonNavigableRoleWithSingleUnqueness()) {
                        messages.add(new Message("warning: facttype " + facttype.getName()
                            + " contains non navigable role with single uniqueness; this "
                            + "uniqueness will not be incorporated in the source code.", false));

                    }

                    if (facttype.hasNonNavigableMandatoryRole()) {
                        messages.add(new Message("warning: facttype " + facttype.getName()
                            + " contains non navigable role with mandatory constraint; this "
                            + "constraint will not be incorporated within the source code.", false));

                    }
                }

            }

        }
        return messages;
    }

    public ArrayList<Message> scanOverlap() {
        ArrayList<Message> messages = new ArrayList<>();
        Map<FactType, List<ObjectType>> candidates = candidatesOverlap();
        for (FactType ft : candidates.keySet()) {
            int ftSize = ft.size();
            for (ObjectType ot : candidates.get(ft)) {
                if (ftSize > ot.getFactType().size()) {
                    messages.add(new Message("Perhaps it is worthwhile "
                        + "to merge roles of "
                        + ft.getName() + " to " + ot.getName() + ".", false));
                } else if (ft.getName().compareTo(ot.getName()) > 0) {
                    messages.add(new Message("Perhaps are "
                        + ft.getName() + " and " + ot.getName()
                        + " synonyms.", false));
                }
            }
        }
        return messages;
    }

    /**
     *
     * @return a table with facttypes (size>1 and not a valuetype) and
     * objecttypes (size>1); the types of the roles of an objecttype are
     * matching with the type of the different roles of the facttype which are
     * spanned by a common uniqueness
     */
    Map<FactType, List<ObjectType>> candidatesOverlap() {
        Map<FactType, List<ObjectType>> candidatesOverlap = new HashMap<>();
        List<FactType> facttypes = new ArrayList<>();
        List<FactType> objecttypes = new ArrayList<>();
        for (FactType ft : getFactTypes()) {
            if (ft.size() > 1) {
                if (!ft.isValueType()) {
                    candidatesOverlap.put(ft, new ArrayList<>());
                    facttypes.add(ft);
                }
                if (ft.isObjectType()) {
                    objecttypes.add(ft);
                }
            }
        }
        for (FactType candidateOverlap : facttypes) {
            for (FactType ot : objecttypes) {
                if (candidateOverlap.overlaps(ot)) {
                    candidatesOverlap.get(candidateOverlap).add(ot.getObjectType());
                }
            }
        }
        return candidatesOverlap;
    }

    public void removeBehavior() {
        removeArtificialFactTypes();

        for (FactType ft : typeRepository.getFactTypeCollection()) {
            if (ft.isClass()) {
                ft.getObjectType().removeBehavior();
            }
        }
        codeClass = null;
        fireListChanged();
    }

    void generateRegistries() {
        Set<ObjectType> accessibles = new TreeSet<>();
        Set<ObjectType> needRegistry = new TreeSet<>();

        for (FactType ft : typeRepository.getFactTypeCollection()) {
            if (ft.isObjectType()) {
                ObjectType ot = ft.getObjectType();
                if (!ft.isDerivable() && !ot.isValueType()) {
                    if (ft.isSingleton()) {
                        accessibles.add(ot);
                    } else {
                        needRegistry.add(ot);
                    }
                }
            }
        }

        Set<ObjectType> indirectAccessibles = detectAccessibles(needRegistry);
        accessibles.addAll(indirectAccessibles);
        needRegistry.removeAll(indirectAccessibles);

        Set<ObjectType> toRemove = new HashSet<>();
        indirectAccessibles = new HashSet<>();
        for (ObjectType ot : needRegistry) {
            List<ObjectType> nonAccessibleSubTypes = ot.nonAccessibleConcreteSubTypes(accessibles);
            if (ot.isAbstract()) {
                if (nonAccessibleSubTypes.size() <= 1) {
                    toRemove.add(ot);
                } else {
                    indirectAccessibles.addAll(nonAccessibleSubTypes);
                    toRemove.addAll(nonAccessibleSubTypes);
                }
            } else {
                indirectAccessibles.addAll(nonAccessibleSubTypes);
                toRemove.addAll(nonAccessibleSubTypes);
            }
        }
        needRegistry.removeAll(toRemove);
        accessibles.addAll(indirectAccessibles);

        for (ObjectType ot : needRegistry) {
            generateArtificialSingleton(ot.getFactType());
        }
    }

    private Set<ObjectType> detectAccessibles(Set<ObjectType> needRegistry) {

        Set<ObjectType> accessibleCandidates = new TreeSet<>();

        boolean changed;
        do {
            changed = false;
            for (ObjectType candidate : needRegistry) {
                if (!accessibleCandidates.contains(candidate)) {
                    if (candidate.getFactType().isAccessible()) {
                        accessibleCandidates.add(candidate);
                        changed = true;
                    }
                }
            }
        } while (changed);

        return accessibleCandidates;
    }

    private FactType generateArtificialSingleton(FactType ft) {
        String objectTypeName;
        ObjectType ot = ft.getObjectType();
        objectTypeName = ot.getName();

        String registryName = "_" + objectTypeName + "Registry";
        String registryOTE = objectTypeName + "Registry";
        FactRequirement factRequirement = project.getRequirementModel().addFactRequirement(Category.SYSTEM, registryOTE,
            new SystemInput("generated registry is needed"));
        FactType registry = new FactType(registryName, registryOTE, this, factRequirement);

        List<String> constants = new ArrayList<>();
        constants.add("");
        constants.add(" has registered ");
        constants.add(".");
        List<SubstitutionType> containsTypes = new ArrayList<>();
        containsTypes.add(registry.getObjectType());
        containsTypes.add(ft.getObjectType());
        List<String> roleNames = new ArrayList<>();
        roleNames.add("");
        roleNames.add("");

        FactType contains = new FactType("_" + objectTypeName + "Registration", constants,
            containsTypes, roleNames, false, this,
            factRequirement);

        Iterator<Role> itRoles = contains.roles();
        ObjectRole registryRole = (ObjectRole) itRoles.next();
        registryRole.setComposition(true);

        ObjectRole ftrole = (ObjectRole) itRoles.next();
        ftrole.setNavigable(false);

        try {
            new UniquenessConstraint(ftrole, createRuleRequirement("Every fact of " + objectTypeName + " is unique at "
                + registry.getName() + ".", "redundancy of facts is not acceptable"));
            new MandatoryConstraint(ftrole, createRuleRequirement("Every fact of " + objectTypeName + " is registered at "
                + registry.getName() + ".", "facts must be registerd at exactly one registry, because"
                + " otherwise traceability is not easy garanteed."));
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectModel.class.getName()).log(Level.SEVERE, null, ex);
        }

        registryRole.addAddable(
            "a registry offers normally the possibility of adding new components.");
        registryRole.addRemovable(
            "a registry offers normally the possibility of removing new components.");

        artificialSingletons.add(registry);
        artificialSingletonFactTypes.add(contains);
        typeRepository.putFactType(registry);
        typeRepository.putFactType(contains);
        publisher.inform(this, "newType", null, registry);
        publisher.inform(this, "newType", null, contains);

        return registry;
    }

    private RuleRequirement createRuleRequirement(String ruletext, String justification) {
        RequirementModel rm = project.getRequirementModel();
        RuleRequirement rule = rm.addRuleRequirement(Category.SYSTEM,
            ruletext, new SystemInput(justification));
        return rule;
    }

    /**
     * case-sensitive (except first letter) searching for a facttype with given
     * name;
     *
     * @param name
     * @return could be null if there doesn't exists a type with the given
     * case-sensitive name
     */
    public FactType getFactType(String name) {
        return typeRepository.getFactType(name);
    }

    public Collection<FactType> getFactTypes(Collection<String> factTypeNames) {
        return typeRepository.getFactTypeCollection(factTypeNames);
    }

    /**
     * case-sensitive (except first letter) searching for a objecttype with
     * given name;
     *
     * @param name
     * @return the objecttype with the given name, if such an object doesn't
     * exists null wil be returned
     */
    public ObjectType getObjectType(String name) {
        FactType ft = typeRepository.getFactType(name);
        if (ft != null && ft.isObjectType()) {
            return ft.getObjectType();
        } else {
            return null;
        }
    }

    public SubstitutionType getSubstitutionType(String name) {
        SubstitutionType st = BaseType.getBaseType(name);
        if (st == null) {
            return getObjectType(name);
        } else {
            return st;
        }
    }

    /**
     * The acceptation of the renaming depends on the uniqueness of the
     * case-insensitive name in relation to the other getFactTypeCollection
     * known by this objectmodel
     *
     * @param ft
     * @param name isTypeName()
     * @throws equa.meta.DuplicateException
     * @throws equa.meta.ChangeNotAllowedException
     * @see #:Naming.isTypeName()
     * @throws DuplicateNameException if there exists another facttype with the
     * same name
     */
    public void renameFactType(FactType ft, String name) throws DuplicateException, ChangeNotAllowedException {
        String nameWithCapital = Naming.restyleWithCapital(name);
        if (!Naming.isTypeName(nameWithCapital)) {
            throw new RuntimeException("name doesn't satisfy rules of a type name");
        }
        if (ft instanceof ElementsFactType) {
            throw new ChangeNotAllowedException("name of element fact type should be changed via his collection");
        }
        checkIfNameIsUsed(nameWithCapital);
        if (nameWithCapital.startsWith("_")) {
            throw new ChangeNotAllowedException("name of fact type may not be changed "
                + "to a name starting with an underscore");
        }

        if (ft.isGenerated()) {
            FactType related;
            if (artificialSingletons.contains(ft)) {
                artificialSingletons.remove(ft);
                int index = ft.getName().length() - "Registry".length();
                related = getFactType(ft.getName().substring(0, index) + "Registration");
                artificialSingletonFactTypes.remove(related);

            } else {
                artificialSingletonFactTypes.remove(ft);
                int index = ft.getName().length() - "Registration".length();
                related = getFactType(ft.getName().substring(0, index) + "Registry");
                artificialSingletons.remove(related);
            }
            typeRepository.renameFactType(related.getName().substring(1), related);
        }

        typeRepository.renameFactType(nameWithCapital, ft);

        fireListChanged();
    }

    /**
     * if there doesn't exists a facttype with this name, then a new facttype
     * shall be registered at this objectmodel; if there exists a facttype with
     * name then the FTE must be unknown
     *
     * @param name isTypeName()
     * @see #: Naming.isTypeName()
     * @param constants is a non empty list belonging to the FTE the size of
     * constants = the number of roles + 1; size of constants >1;
     * @param source the source of this fact type
     * @param getFactTypeCollection getFactTypeCollection.size() =
     * constants.size() -1;
     * @param roleNames rolenames.size() = getFactTypeCollection.size();
     * @param roleNumbers length = getFactTypeCollection.size();
     * @return the concerning facttype
     * @throws MismatchException if a type or rolename doesn't match with the
     * existing facttype name
     */
    public FactType addFactType(String name, List<String> constants,
        List<SubstitutionType> types, List<String> roleNames,
        List<Integer> roleNumbers, Requirement source)
        throws MismatchException, DuplicateException {
        String nameWithCapital = Naming.restyleWithCapital(name);
        if (!Naming.isTypeName(nameWithCapital)) {
            throw new RuntimeException("name doesn't satisfy rules of a type name");
        }
        FactType ft;
        ft = getFactType(nameWithCapital);
        for (int i = 0; i < roleNames.size(); i++) {
            String roleName = roleNames.get(i);
            if (roleName == null || roleName.isEmpty()) {
                roleNames.set(i, Naming.withoutCapital(types.get(i).getName()));
            }
        }
        if (ft == null) {
            FactType.checkRoleNames(roleNames);
            ft = new FactType(nameWithCapital, constants, types, roleNames, false, this, source);
            this.typeRepository.putFactType(ft);
            //publisher.inform(this, "factType", null, ft);
        } else {
            if (ft.getFTE() != null) {
                throw new RuntimeException("Facttype " + name + "already possesses "
                    + "a FTE");
            } else {
                ft.checkMatch(types, roleNames, true);
                ft.addFTE(constants, roleNumbers, source);
            }
        }
        fireListChanged();
        publisher.inform(this, "newType", null, ft);
        return ft;
    }

    public equa.meta.objectmodel.CollectionType addCollectionType(CollectionNode cn, boolean sequence, Requirement source)
        throws DuplicateException, MismatchException {
        String name = cn.getTypeName();
        if (BaseType.getBaseType(name) != null) {
            throw new DuplicateException(name + " is used as name of base type");
        }
        String ftname = Naming.withCapital(name);

        String elementTypeName = cn.getElementTypeName();
        SubstitutionType substitutionType = BaseType.getBaseType(elementTypeName);
        if (substitutionType == null) {
            substitutionType = getObjectType(elementTypeName);
        }
        if (substitutionType == null) {
            throw new MismatchException(null, "element node of collection is still not registered");
        }
        List<ValueNode> valueNodes = cn.getConcreteValueNodes();
        int size = valueNodes.size();
        if (!sequence && size > 1) {
            Collections.sort(valueNodes);
//            ValueNode valueNode = valueNodes.get(0);
//            for (int i = 1; i < size; i++) {
//                int c = valueNodes.get(i).compareTo(valueNode);
//                if (c < 0) {
//                    throw new RuntimeException("values in set should be offered in an "
//                            + "ascending way");
//                } else if (c == 0) {
//                    throw new MismatchException(null, "values in set should be different");
//                }
//            }
        }
        ElementsFactType elementsFT;
        FactType collectionFT = getFactType(ftname);
        CollectionType collectionOT;
        if (collectionFT == null) {
            collectionFT = new FactType(ftname, this, cn.getBegin(), cn.getSeparator(), cn.getEnd(), source);
            this.typeRepository.putFactType(collectionFT);
            collectionOT = (CollectionType) collectionFT.getObjectType();
            if (sequence) {
                elementsFT = new ElementsSequenceFactType(ftname + "Elements",
                    collectionOT, substitutionType, cn.getElementRoleName(), this, source);
            } else {
                elementsFT = new ElementsSetFactType(ftname + "Elements",
                    collectionOT, substitutionType, cn.getElementRoleName(), this, source);
            }
            this.typeRepository.putFactType(elementsFT);

            collectionOT.setElementsFactType(elementsFT);
            if (sequence) {
                ((ElementsSequenceFactType) elementsFT).addUniqeness(collectionOT, this);
            }
            publisher.inform(this, "newType", null, elementsFT);
            publisher.inform(this, "newType", null, collectionFT);
            fireListChanged();
        } else {
            collectionOT = (CollectionType) collectionFT.getObjectType();
        }

        return collectionOT;
    }

    public void removeFactType(FactType ft) throws ChangeNotAllowedException {
        if (ft.isObjectType()) {
            ft.getObjectType().checkActivity(true);
            ft.getObjectType().remove();
        } else if (ft instanceof ElementsFactType) {
            throw new ChangeNotAllowedException(ft.getName() + " cannot removed because it"
                + " plays an identifying role with respect to "
                + ((ElementsFactType) ft).getCollectionType().getName());

        } else {
            ft.remove();
        }
        typeRepository.removeFactType(ft.getName());
        publisher.inform(this, "removedType", null, ft);
        fireListChanged();
    }

    /**
     * fact of existing facctype is added; facttype must have a FTE
     *
     * @param fn all objectnodes of fn are already registered
     * @throws equa.meta.MismatchException
     */
    public void addFact(FactNode fn) throws MismatchException, ChangeNotAllowedException {
        FactType ft = getFactType(fn.getTypeName());
        if (ft == null) {
            throw new RuntimeException("facttype " + fn.getTypeName() + " is unknown at objectmodel");
        }
        if (ft.getFTE() == null) {
            throw new MismatchException(ft.getTE(), "adding fact at facttype "
                + ft.getName() + " with unknown FTE");
        }

        ft.addFact(fn);

    }

    /**
     * if there doesn't exists an object- or facttype with this name, then a new
     * objecttype will be registered at this objectmodel; if there exists a
     * facttype then this factttype gets an OTE; the ranking of the values,
     * getFactTypeCollection and rolenames is conform the ranking known at the
     * concerning facttype
     *
     * @param typeName isTypeName()
     * @see #: Naming.isTypeName()
     * @param constants is a non empty list or null(in case of an abstract
     * objecttype) the size of constants = the number of roles + 1; if size = 1
     * then facttype is a singleton objecttype if size = 0 then facttype is an
     * abstract objecttype
     * @param source the source of this fact type
     * @param getFactTypeCollection getFactTypeCollection.size() =
     * constants.size() -1
     * @param roleNumbers length = type.size(); ranking conform OTE
     *
     * @return the facttype where this object has been assigned to
     * @throws MisMatchException if a constant, a type or rolename doesn't match
     * with the existing objecttype name
     * @throws ChangeNotAllowedException if existing facttype is already
     * objectified
     * @throws DuplicateException all non-empty rolenames must be different
     */
    public FactType addObjectType(String typeName, List<String> constants,
        List<SubstitutionType> types, List<String> roleNames,
        List<Integer> roleNumbers, Requirement source)
        throws MismatchException, ChangeNotAllowedException, DuplicateException {

        String nameWithCapital = Naming.restyleWithCapital(typeName);
        if (!Naming.isTypeName(nameWithCapital)) {
            throw new RuntimeException("name doesn't satisfy rules of a type name");
        }
        FactType.checkRoleNames(roleNames);
        FactType ft = getFactType(typeName);
        if (ft == null) {
            if (constants == null) { // abstract objecttype
                ft = new FactType(nameWithCapital, true, this, source);
            } else if (constants.size() == 1) { // singleton objecttype
                ft = new FactType(nameWithCapital, constants.get(0), this, source);
            } else { // normal objecttype
                ft = new FactType(nameWithCapital, constants, types, roleNames,
                    true, this, source);
            }
            this.typeRepository.putFactType(ft);
            publisher.inform(this, "newType", null, ft);

        } else {
            // facttype with given name is already registered at the objectmodel
            ft.checkMatch(types, roleNames, false);
            if (ft.isObjectType()) {
                ft.getObjectType().getOTE().matches(constants);
            } else {
                ft.objectify(constants, roleNumbers);
            }
        }
        //       fireListChanged();

        return ft;
    }

    /**
     * if there doesn't exists an object- or facttype with this name, then a new
     * objecttype will be registered at this objectmodel; if there exists a
     * facttype then this factttype gets an OTE; the ranking of the values,
     * getFactTypeCollection and rolenames is conform the ranking known at the
     * concerning facttype
     *
     * @param typeName isTypeName()
     * @see #: Naming.isTypeName()
     * @param constants is a non empty list or null(in case of an abstract
     * objecttype) the size of constants = the number of roles + 1; if size = 1
     * then facttype is a singleton objecttype if size = 0 then facttype is an
     * abstract objecttype
     * @param sources a list of the sources of this fact type
     * @param getFactTypeCollection getFactTypeCollection.size() =
     * constants.size() -1
     * @param roleNumbers length = type.size(); ranking conform OTE
     *
     * @return the facttype where this object has been assigned to
     * @throws MisMatchException if a constant, a type or rolename doesn't match
     * with the existing objecttype name
     * @throws ChangeNotAllowedException if existing facttype is already
     * objectified
     * @throws DuplicateException all non-empty rolenames must be different
     */
    public FactType addObjectType(String typeName, List<String> constants,
        List<SubstitutionType> types, List<String> roleNames,
        List<Integer> roleNumbers, List<Source> sources)
        throws MismatchException, ChangeNotAllowedException, DuplicateException {
        //Create and add the objectType in the method above here
        FactType ft = this.addObjectType(typeName, constants, types, roleNames, roleNumbers, (FactRequirement) sources.get(0));

        //Add the remaining sources to the objecttype.
//        sources.remove(0);
//        for (Source s : sources) {
//            ft.addSource(s);
//        }
        return ft;
    }

    /**
     * the substitutiontype of br will be objectified into a constrained
     * basetype; this constrained basetype is added to the objectmodel
     *
     * @param br
     * @param rule
     * @return the facttype which refers to the newly created constrained
     * basetype
     * @throws DuplicateException
     */
    public FactType objectifyToConstrainedBaseType(BaseValueRole br) throws DuplicateException {
        String nameWithCapital = Naming.withCapital(br.getRoleName());
        if (typeRepository.existsFactType(nameWithCapital)) {
            throw new DuplicateException("rolename of basetype is already in use as fact type name");
        }

        FactType cbt;
        cbt = new FactType(nameWithCapital, br.getSubstitutionType(), this, br.getParent().getFirstFactRequirement());
        typeRepository.putFactType(cbt);

        br.getParent().replaceBaseType(br, (ConstrainedBaseType) cbt.getObjectType());

        publisher.inform(this, "newType", null, cbt);
        fireListChanged();
        return cbt;
    }

    public void objectifyToConstrainedBaseType(BaseValueRole br, FactType cbt) throws DuplicateException {

        br.getParent().replaceBaseType(br, (ConstrainedBaseType) cbt.getObjectType());

        fireListChanged();

    }

    /**
     * object of existing objecttype is added;
     *
     * @param on all including objectnodes of on are allready registered
     */
    public void addObject(ObjectNode on) throws MismatchException, ChangeNotAllowedException {
        FactType ft = getFactType(on.getTypeName());
        if (ft == null) {
            throw new RuntimeException("objecttype " + on.getTypeName() + " is unknown at objectmodel");
        }
        if (!ft.isObjectType()) {
            throw new RuntimeException("adding object at facttype "
                + ft.getName() + " which isn't an objecttype");
        }
        ft.addFact(on);

    }

    /**
     * an inheritance relation between this super and subtype is added
     *
     * @param nameSupertype
     * @param nameSubtype must exist at objectmodel
     * @param source
     * @return the newly created supertype, if supertype didn't exist, otherwise
     * this supertype will be returned
     */
    public FactType addSuperType(String nameSupertype, String nameSubtype,
        ExternalInput source)
        throws DuplicateException, ChangeNotAllowedException {
        if (nameSubtype.isEmpty() || nameSupertype.isEmpty()) {
            return null;
        }

        FactType subtype = typeRepository.getFactType(nameSubtype);
        if (subtype == null) {
            throw new RuntimeException("subtype doesn't exist");
        }
        FactType supertype = getFactType(nameSupertype);
        if (supertype == null) {
            supertype = new FactType(nameSupertype, true, this, subtype.getFirstFactRequirement());
            //       supertype.addSource(source);
            typeRepository.putFactType(supertype);
            publisher.inform(this, "newType", null, supertype);
        }

        subtype.getObjectType().addSuperType(supertype.getObjectType());

        fireListChanged();
        return supertype;
    }

    /**
     *
     * @param typeName is a correct type name
     * @param superType is a direct supertype of subType and superType is
     * abstract
     * @param subType
     * @param projectMemberInput
     * @throws DuplicateException
     */
    public void insertInheritance(String typeName, ObjectType superType, ObjectType subType, ExternalInput projectMemberInput)
        throws DuplicateException, ChangeNotAllowedException {
        Iterator<ObjectType> itSuperType = subType.supertypes();
        boolean inheritanceFound = false;
        while (itSuperType.hasNext()) {
            if (itSuperType.next().equals(superType)) {
                inheritanceFound = true;
            }
        }
        if (!inheritanceFound) {
            throw new RuntimeException(subType.getName() + " is not a direct subtype of " + superType.getName());
        }
        if (!superType.isAbstract()) {
            throw new RuntimeException(superType.getName() + " is not abstract");
        }
        String nameWithCapital = Naming.withCapital(typeName);
        if (!Naming.isTypeName(nameWithCapital)) {
            throw new RuntimeException("type name does not fulfill rules of a correct type name");
        }

        FactType ft = getFactType(nameWithCapital);
        if (ft == null) {
            ft = new FactType(typeName, true, this, projectMemberInput);
            this.typeRepository.putFactType(ft);
            publisher.inform(this, "newType", null, ft);
            fireListChanged();
        } else if (ft.isObjectType()) {
            if (!ft.getObjectType().isAbstract()) {
                throw new ChangeNotAllowedException("type refers to existing object type which isn't abstract");
            }
        } else {
            throw new ChangeNotAllowedException("type refers to existing fact type which isn't an object type");
        }

        ObjectType ot = ft.getObjectType();
        subType.deleteSupertype(superType);
        subType.addSuperType(ot);
        ot.addSuperType(superType);
    }

    /**
     * an unidentified objecttype is added, but only if name was not in use at
     * this objectmodel
     *
     * @param name isTypeName()
     * @see isTypeName()
     * @param source
     * @return the newly created facttype
     * @throws DuplicateNameException if there exists an other facttype with the
     * same name
     */
    public FactType addUnidentifiedObjectType(String name, FactRequirement source) throws DuplicateException {
        String nameWithCapital = Naming.restyleWithCapital(name);

        if (!Naming.isTypeName(nameWithCapital)) {
            throw new RuntimeException("name doesn't satisfy rules of a type name");

        }
        checkIfNameIsUsed(nameWithCapital);
        FactType ft = new FactType(nameWithCapital, false, this, source);

        this.typeRepository.putFactType(ft);
        publisher.inform(this, "newType", null, ft);

        fireListChanged();
        return ft;
    }

    /**
     * if objecttype with typename doesn't exist: a lonely abstract object type
     * with typeName is added
     *
     * @param typeName must fullfill rules of a type name
     * @param projectMemberInput
     * @return the already existing facttype or else the newly created abstract
     * facttype
     * @throws DuplicateException if facttype with typename exist
     */
    public FactType addAbstractObjectType(String typeName, ExternalInput projectMemberInput)
        throws DuplicateException {
        if (!Naming.isTypeName(Naming.withCapital(typeName))) {
            throw new RuntimeException("name doesn't satisfy rules of a type name");
        }
        FactType ft = getFactType(typeName);
        if (ft != null) {
            if (ft.isObjectType()) {
            } else {
                throw new DuplicateException("fact type " + typeName + " already exists");
            }
        } else {
            ft = new FactType(typeName, true, this, projectMemberInput);
            this.typeRepository.putFactType(ft);
            publisher.inform(this, "newType", null, ft);
            fireListChanged();
        }
        return ft;
    }

    @Override
    public void remove(ModelElement member) {
        if (member instanceof FactType) {

            typeRepository.removeFactType(((FactType) member).getName());
            publisher.inform(this, "removedType", null, member);

            fireListChanged();
        }
    }

    /**
     * fact described by fn is removed from this objectmodel; in case of a
     * resulting empty population, the facttype will be removed out of this
     * objectmodel
     *
     * @param fn
     */
    public void removeFact(FactNode fn) throws ChangeNotAllowedException {

        FactType ft = getFactType(fn.getTypeName());
        if (ft == null) {
            return;
        }

        ft.getPopulation().removeAssociationsWithSource(fn.getExpressionTreeModel().getSource(), this);
        if (ft.getPopulation().size() == 0) {
            ft.remove();
            publisher.inform(this, "removedType", null, ft);
            fireListChanged();
        }
    }

    public void removeCollection(CollectionNode cn) throws ChangeNotAllowedException {
        FactType ft = getFactType(cn.getTypeName());
        if (ft == null) {
            return;
        }

        ft.getPopulation().removeAssociationsWithSource(cn.getExpressionTreeModel().getSource(), this);
        if (ft.getPopulation().size() == 0) {
            ft.remove();
            publisher.inform(this, "removedType", null, ft);
            fireListChanged();
        }
    }

    /**
     * concrete subnode-fact refered by sn is removed from this objectmodel; in
     * case of a resulting empty population of the concerning concrete subtype,
     * this subtype will be removed out of this objectmodel; if (abstract)
     * objecttype doesn't possess any subtype objecttype will be removed from
     * this objectmodel
     *
     * @param sn
     */
    public void removeSuperTypeNode(SuperTypeNode sn) throws ChangeNotAllowedException {
        // a supertype node includes always one child:
        ParentNode subNode = (ParentNode) sn.getSubNode();
        if (subNode instanceof SuperTypeNode) {
            removeSuperTypeNode((SuperTypeNode) subNode);

        } else {
            removeFact((FactNode) subNode);
        }

        ObjectType ot = getObjectType(sn.getTypeName());
        if (ot != null && !ot.subtypes().hasNext()) {
            removeFactType(ot.getFactType());
        }

    }

    /**
     *
     * @return an iterator over all facttypes known at this model
     */
    public Iterator<FactType> typesIterator() {
        return typeRepository.getFactTypeCollection().iterator();
    }

    /**
     *
     * @return a copy of the collection with facttypes
     */
    public Collection<FactType> types() {
        return new ArrayList<>(typeRepository.getFactTypeCollection());
    }

    /**
     *
     * @param from
     * @param unto
     * @return an iterator over a sorted list with getFactTypeNameIterator in
     * the range of [from,unto>
     */
    public Iterator<String> typeNames(String from, String unto) {
        return typeRepository.getFactTypeNameIterator(from, unto);
    }

    /**
     *
     * @return an iterator over a sorted list with all getFactTypeNameIterator
     */
    public Iterator<String> typeNames() {
        return typeRepository.getFactTypeNameIterator();
    }

    private void checkIfNameIsUsed(String nameWithCapital) throws DuplicateException {

        if (project.getVocabulary().isMember(nameWithCapital)) {
            throw new DuplicateException(nameWithCapital + " is already used "
                + "in the vocabulary of this project");

        }
    }

    @Override
    public int getSize() {
        return typeRepository.size();
    }

    @Override
    public FactType getElementAt(int index) {
        Iterator<FactType> it = typesIterator();

        int i = 0;
        while (i < index && it.hasNext()) {
            i++;
            it.next();
        }
        if (i == index && it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public void fireListChanged() {
        EventListener[] listeners = listenerList.getListeners(ListDataListener.class);
        for (EventListener l : listeners) {
            ((ListDataListener) l).contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    private void removeArtificialFactTypes() {
        try {
            for (FactType ft : artificialSingletonFactTypes) {
                removeFactType(ft);
            }
            artificialSingletonFactTypes.clear();
            for (FactType ft : artificialSingletons) {
                removeFactType(ft);
            }
            artificialSingletons.clear();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void addListener(PropertyListener listener, String property) {
        if (publisher == null) {
            initPublisher();
        }
        publisher.addListener(listener, property);

    }

    @Override
    public void removeListener(PropertyListener listener, String property) {
        publisher.removeListener(listener, property);
    }

    /**
     *
     * @param superTypeName
     * @param typeName
     * @return the list with getFactTypeCollection to get from supertypeName
     * towards typenName via inheritance relations
     * @throws RuntimeException, if such a path doesn't exist
     */
    public List<String> getPath(String superTypeName, String typeName) {
        ObjectType supertype = getObjectType(superTypeName);
        if (supertype == null) {
            return new ArrayList<>();
        }
        ObjectType destination = getObjectType(typeName);
        if (destination == null) {
            throw new RuntimeException("inheritance path with unknown destination");
        }

        List<String> path = supertype.getPath(destination);
        if (path == null) {
            throw new RuntimeException("inheritance path doesn't exist");
        }

        return path;
    }

    /**
     *
     * @param selected_types
     * @return a list with candidate facttypes with the same substititutiontypes
     * in selected_types: every substitutiontype within selected_types must have
     * the same count within the substitutiontypes of the candidate and v.v.
     */
    public List<FactType> correspondingTypes(List<SubstitutionType> selected_types) {
        ArrayList<FactType> candidates = new ArrayList<>();
        int size = selected_types.size();
        for (FactType ft : this.typeRepository.getFactTypeCollection()) {
            if (ft.size() == size) {
                if (ft.looksLike(selected_types)) {
                    if (ft.isObjectType() || ft.isObjectifiable()) {
                        candidates.add(ft);
                    }
                }
            }
        }

        return candidates;
    }

    private void initPublisher() {
        publisher = new BasicPublisher(new String[]{"newType", "removedType"});
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return "ObjectModel";
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ObjectModel) {
            return ((ObjectModel) object).project == project;
        }
        return false;
    }

    public CodeClass getCodeClass() {
        return codeClass;
    }

    @Override
    public String expressIn(Language l) {
        return SYSTEM_CLASS;
    }

    @Override
    public String callString() {
        return SYSTEM_CLASS;
    }

    public boolean possessesBehaviour() {
        for (FactType ft : getFactTypes()) {
            if (ft.isObjectType()) {
                if (ft.getObjectType().getCodeClass() != null) {
                    return true;
                }
            }
        }
        return false;
    }

}
