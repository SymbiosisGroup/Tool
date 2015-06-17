package equa.meta.objectmodel;

import equa.factbreakdown.AbstractValue;
import equa.factbreakdown.UnparsableValue;
import static equa.code.CodeNames.AUTO_INCR_NEXT;
import static equa.code.CodeNames.TEMPLATE;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import equa.code.CodeClass;
import equa.code.Field;
import equa.code.ImportedOperation;
import equa.code.IndentedList;
import equa.code.Language;
import equa.code.LanguageOH;
import equa.code.MetaOH;
import equa.code.OperationHeader;
import equa.code.Util;
import equa.code.operations.AccessModifier;
import equa.code.operations.ActualParam;
import equa.code.operations.AddBaseTypeMethod;
import equa.code.operations.AddObjectTypeMethod;
import equa.code.operations.AdjustMethod;
import equa.code.operations.ChangeIdMethod;
import equa.code.operations.ClearSingletonMethod;
import equa.code.operations.CompareToMethod;
import equa.code.operations.Constructor;
import equa.code.operations.ContainsMethod;
import equa.code.operations.CountMethod;
import equa.code.operations.EqualsMethod;
import equa.code.operations.GetSingletonMethod;
import equa.code.operations.HashCodeMethod;
import equa.code.operations.IdentifyingPropertiesMethod;
import equa.code.operations.IndexMethod;
import equa.code.operations.IndexOfMethod;
import equa.code.operations.IndexedProperty;
import equa.code.operations.InsertBaseTypeMethod;
import equa.code.operations.InsertObjectTypeMethod;
import equa.code.operations.IsDefinedMethod;
import equa.code.operations.IsRemovableMethod;
import equa.code.operations.MaxCountMethod;
import equa.code.operations.Method;
import equa.code.operations.MinCountMethod;
import equa.code.operations.MoveMethod;
import equa.code.operations.Null;
import equa.code.operations.Operation;
import equa.code.operations.Param;
import equa.code.operations.PropertiesMethod;
import equa.code.operations.Property;
import equa.code.operations.PutMethod;
import equa.code.operations.RegisterMethod;
import equa.code.operations.Strip;
import equa.code.operations.RemoveAtMethod;
import equa.code.operations.RemoveMethod;
import equa.code.operations.STorCT;
import equa.code.operations.SearchCollectionMethod;
import equa.code.operations.SearchLexicalMethod;
import equa.code.operations.SearchMethod;
import equa.code.operations.SubParam;
import equa.code.operations.SubsequenceMethod;
import equa.code.operations.ToStringMethod;
import equa.meta.ChangeNotAllowedException;
import equa.meta.DuplicateException;
import equa.meta.MismatchException;
import equa.meta.NotParsableException;
import equa.meta.SyntaxException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.CollectionIdRelation;
import equa.meta.classrelations.FactTypeRelation;
import equa.meta.classrelations.IdRelation;
import equa.meta.classrelations.ObjectTypeRelation;
import equa.meta.classrelations.Relation;
import equa.meta.requirements.FactRequirement;
import equa.meta.requirements.Requirement;
import equa.meta.traceability.ExternalInput;
import equa.meta.traceability.ModelElement;
import equa.meta.traceability.ParentElement;
import equa.meta.traceability.Source;
import equa.project.Project;
import equa.util.Naming;
import fontys.observer.BasicPublisher;
import fontys.observer.PropertyListener;
import fontys.observer.Publisher;
import java.util.Collections;

/**
 *
 * @author FrankP
 */
@Entity
public class ObjectType extends ParentElement implements SubstitutionType, Serializable, Publisher, ActualParam {

    private static final long serialVersionUID = 1L;
    public static final int CONSTRAINED_BASETYPE = 1;
    public static final int ABSTRACT_OBJECTTYPE = 0;
    public static final int UNIDENTIFIED_OBJECTTYPE = -1;
    /**
     * **********************************************************************
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    private TypeExpression ote;
    @Column
    private boolean _abstract;
    @Column
    private boolean comparable;

    @Column
    protected boolean valueType;
    @Column
    private boolean hiddenId;
    @OneToMany
    protected Collection<Role> plays;
    @OneToMany
    private Collection<ObjectType> supertypes;
    @OneToMany
    private Collection<Interface> interfaces;
    @OneToMany
    private Collection<ObjectType> subtypes;
    @Transient
    protected CodeClass codeClass;
    @Transient
    protected BasicPublisher publisher;
    @Transient
    protected List<Relation> relations;

    private final Map<OperationHeader, Algorithm> algorithms = new HashMap<>();
    private Set<String> importsAlgorithms = new HashSet<>();
    private Set<String> fieldsAlgorithms;
    private Set<String> constantsAlgorithms;
    private Initializer initializer;

    public Initializer getInitializer() {
        return initializer;
    }

    public void setInitializer(Requirement rule) {
        if (isValueType() || isSingleton()) {
            if (getFactType().isGenerated()) {
                // remove starting underscore
                ObjectModel om = (ObjectModel) this.getFactType().getParent();
                try {
                    om.renameFactType(getFactType(), getName().substring(1));
                } catch (DuplicateException | ChangeNotAllowedException ex) {
                    Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
            this.initializer = new Initializer(rule, this);
        }
    }

    public void removeInitializer() {
        initializer = null;
    }

    /**
     * objecttype with OTE based on constants will be created; objecttype will
     * be not abstract; it plays no where a role; it has no super- and subtype
     *
     * @param parent parent corresponds with this objecttype (they have the same
     * name)
     * @param constants of the OTE (constants.size() = number of roles +1)
     * @param the ranking of the roles of the OTE in relation to the ranking of
     * the roles at the parent-facttype
     */
    ObjectType(FactType parent, List<String> constants, List<Integer> roleNumbers) {
        super(parent, null);
        init(parent, constants, roleNumbers);
        this.fieldsAlgorithms = new HashSet<>();
        this.constantsAlgorithms = new HashSet<>();
    }

    /**
     * constructor needed in behalf of constructor of a singleton objecttype
     *
     * @param parent
     * @param constant
     */
    ObjectType(FactType parent, String constant) {
        super(parent, null);
        ArrayList<String> constants = new ArrayList<>();
        constants.add(Naming.withoutCapital(constant.trim()));
        init(parent, constants, null);
        this.fieldsAlgorithms = new HashSet<>();
        this.constantsAlgorithms = new HashSet<>();
    }

    ObjectType(FactType parent, int kind) {
        super(parent, null);
        ArrayList<String> constants;
        constants = new ArrayList<>();
        if (kind == CONSTRAINED_BASETYPE) {
            constants.add("");
            constants.add("");
        }
        init(parent, constants, null);
        this.fieldsAlgorithms = new HashSet<>();
        this.constantsAlgorithms = new HashSet<>();
    }

    ObjectType(FactType parent) {
        super(parent, null);
        ote = new TypeExpression(parent.getFTE());
        _abstract = false;
        comparable = false;

        valueType = false;
        hiddenId = false;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(1);
        subtypes = new ArrayList<>(1);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
        this.fieldsAlgorithms = new HashSet<>();
        this.constantsAlgorithms = new HashSet<>();
    }

    ObjectType(FactType parent, CollectionTypeExpression ote) {
        super(parent, null);
        this.ote = ote;
        _abstract = false;
        comparable = false;

        valueType = false;
        hiddenId = true;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(1);
        subtypes = new ArrayList<>(1);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
        this.fieldsAlgorithms = new HashSet<>();
        this.constantsAlgorithms = new HashSet<>();
    }

    private boolean isNotConstructor(Entry<OperationHeader, ImportedOperation> e, Language l) {
        return !e.getKey().getName(l).equals(getName() + TEMPLATE);
    }

    public Set<String> getImports() {
        return importsAlgorithms;
    }

    public Set<String> getFields() {
        return fieldsAlgorithms;
    }

    public Set<String> getConstants() {
        return constantsAlgorithms;
    }

    private void addImports(List<String> imports) {
        if (importsAlgorithms == null) {
            importsAlgorithms = new HashSet<>();
        }
        this.importsAlgorithms.clear();
        this.importsAlgorithms.addAll(imports);
    }

    private void addFields(Set<String> fields) {
        if (fieldsAlgorithms == null) {
            fieldsAlgorithms = new HashSet<>();
        }
        this.fieldsAlgorithms.clear();
        this.fieldsAlgorithms.addAll(fields);
    }

    private void addConstants(Set<String> constants) {
        if (constantsAlgorithms == null) {
            constantsAlgorithms = new HashSet<>();
        }
        this.constantsAlgorithms.clear();
        this.constantsAlgorithms.addAll(constants);
    }

    public void importAlgorithms(File f) throws SyntaxException {
        String filetext = Util.getString(f);
        Project project = ((ObjectModel) getParent().getParent()).getProject();
        Source source;
        source = new ExternalInput("", project.getCurrentUser());
        Language l = project.getLastUsedLanguage();

        addImports(l.getImports(filetext));
        addConstants(l.getConstants(filetext));
        addFields(l.getFields(filetext));

        Map<OperationHeader, ImportedOperation> map = l.getOperations(filetext, getName() + TEMPLATE);
        Map<OperationHeader, Algorithm> algorithmsOld = new HashMap<>(algorithms);
        //removeLanguageAlgorithms();
        algorithms.clear();
        for (Entry<OperationHeader, ImportedOperation> e : map.entrySet()) {
            if (isNotConstructor(e, l)) {
                if (algorithmsOld.containsKey(e.getKey())) {
                    addAlgorithm(e.getKey(), e.getValue().getBody(), e.getValue().getApi(), algorithmsOld.get(e.getKey()).isRemovable(),
                        l, source);
                    algorithmsOld.remove(e.getKey());
                } else {
                    addAlgorithm(e.getKey(), e.getValue().getBody(), e.getValue().getApi(), true, l, source);
                }
            }
        }

        // adding non-removable algorithms
        for (Entry<OperationHeader, Algorithm> e : algorithmsOld.entrySet()) {
            if (!e.getValue().isRemovable()) {
                if (!e.getValue().sources().isEmpty()) {
                    source = e.getValue().sources().get(0);
                }
                addAlgorithm(e.getKey(), new IndentedList(), e.getValue().getAPI(), false, e.getValue().getLanguage(),
                    source);
            }
        }

    }

    public Map<OperationHeader, Algorithm> algorithmsMap() {
        if (supertypes.isEmpty()) {
            return new HashMap<>(algorithms);
        }
        Map<OperationHeader, Algorithm> algs = new HashMap<>(algorithms);
        algs.putAll(abstractAlgorithms());

        return algs;
    }

    public Collection<Algorithm> algorithms() {
        return algorithmsMap().values();
    }

    public Map<OperationHeader, Algorithm> abstractAlgorithms() {
        Map<OperationHeader, Algorithm> algs = new HashMap<>();

        if (!supertypes.isEmpty()) {
            ObjectType superType = supertypes.iterator().next();
            for (Entry<OperationHeader, Algorithm> entry : superType.algorithmsMap().entrySet()) {
                if (entry.getValue().getCode() == null) {
                    //entry.getValue().setCode(new IndentedList());
                    algs.put(entry.getKey(), entry.getValue());
                }
            }
            algs.putAll(superType.abstractAlgorithms());
        }

        return algs;
    }

    public Algorithm getAlgorithm(OperationHeader header) {
        return algorithms.get(header);
    }

    public Algorithm removeAlgorithm(OperationHeader header) {
        // algorithms.clear(); return null;
        Algorithm toRemove = algorithms.get(header);
        if (toRemove != null) {
            toRemove.remove();
        }
        return toRemove;
    }

    void removeAlgorithms() {
        Set<OperationHeader> headers = algorithms.keySet();
        for (OperationHeader header : headers) {
            removeAlgorithm(header);
        }
    }

    public void removeLanguageAlgorithms() {
        Iterator<OperationHeader> algos = algorithms.keySet().iterator();
        Set<OperationHeader> toRemove = new HashSet<>();
        while (algos.hasNext()) {
            OperationHeader oh = algos.next();
            if (oh instanceof LanguageOH) {
                toRemove.add(oh);
            }
        }
        for (OperationHeader oh : toRemove) {
            algorithms.remove(oh);
        }
    }

    Algorithm addAlgorithm(OperationHeader oh, IndentedList code, IndentedList api, boolean removable, Language l, Source source) {
        Algorithm a = algorithms.get(oh);
        if (a == null) {
            a = new Algorithm(this, source, l);
            algorithms.put(oh, a);
        }
        a.setCode(code, source);
        a.setAPI(api);
        a.setRemovable(removable);
        return a;
    }

    public OperationHeader addAlgorithm(String name, AccessModifier access, boolean property, boolean classMethod,
        STorCT returnType, List<Param> params, IndentedList code, IndentedList api, boolean removable,
        Language l, Source source) {
        OperationHeader oh = new MetaOH(name, access, property, classMethod, returnType, params);
        addAlgorithm(oh, code, api, removable, l, source);
        return oh;
    }

    boolean isLight() {
        return ((ObjectModel) this.getFactType().getParent()).requiresLightBehavior();
    }

    final void init(FactType parent, List<String> constants, List<Integer> roleNumbers) {
        if (!constants.isEmpty()) {
            String firstConstant = constants.get(0);
            if (!firstConstant.isEmpty()) {
                if (firstConstant.length() > 1) {
                    char secondChar = firstConstant.charAt(1);
                    if (!Character.isUpperCase(secondChar)) {
                        constants.set(0, Naming.withoutCapital(firstConstant));
                    }
                } else {
                    constants.set(0, Naming.withoutCapital(firstConstant));
                }
            }
        }

        if (roleNumbers == null) {
            ote = new TypeExpression(parent, constants);
        } else {
            ote = new TypeExpression(parent, constants, roleNumbers);
        }
        _abstract = false;
        comparable = false;

        valueType = false;
        hiddenId = false;
        plays = new ArrayList<>();
        supertypes = new ArrayList<>(1);
        interfaces = new ArrayList<>(2);
        subtypes = new ArrayList<>(2);
        codeClass = new CodeClass(this, null);
        publisher = new BasicPublisher(new String[]{"subtypesIterator", "supertypesIterator", "behavior", "ote", "abstract", "ordered",
            "valueType"});
    }

    /**
     *
     * @return true is this objecttype is abstract, otherwise false
     */
    public boolean isAbstract() {
        return _abstract;
    }

    /**
     *
     * @return true, if this objecttype is a value type (only the value of this
     * objecttype is relevant; replacement by a copy has no influence on the
     * state of the system)
     */
    @Override
    public boolean isValueType() {
        if (valueType) {
            return true;
        } else {
            for (ObjectType supertype : supertypes) {
                if (supertype.isValueType()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * valuetype = true --> this objecttype will be marked as a value type
     * valuetype = false --> this objecttype is not a valuetype anymore
     *
     * @param valueType
     */
    public void setValueType(boolean valueType) {
        if (this.valueType == valueType) {
            return;
        }

        if (valueType) {
            this.valueType = true;
            for (Role role : plays) {
                if (!role.isDerivable()) {
                    role.setNavigable(false);
                }
                // if (!role.isMandatory()) {
                // try {
                // new MandatoryConstraint(role, new OtherInput("a valuetype "
                // + "is always used ones", "System"));
                // } catch (ChangeNotAllowedException ex) {
                // Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE,
                // null, ex);
                // }
                // }
                // }
            }
        } else {
            this.valueType = false;
            this.initializer = null;
        }

        for (ObjectType subtype : subtypes) {
            subtype.setValueType(valueType);
        }

        getFactType().fireListChanged();

        publisher.inform(this, "valueType", null, isValueType());
    }

    /**
     *
     * @return true if objects of this objecttype are comparable, else false
     */
    public boolean isComparable() {
        return comparable;
    }

    @Override
    public boolean isParsable() {
        return ote.isParsable() && !isSuperType();
    }

    public boolean isSuperType() {
        return isAbstract() || subtypes().hasNext();
    }

    /**
     * if (ordered = true) then this objecttype fullfills the
     * Comparable-interface else this objecttype doesn't fullfill the Comparable
     * interface
     *
     * @param comparable
     */
    public void setComparable(boolean comparable) {
        if (this.comparable == comparable) {
            return;
        }

        if (comparable == true) {

            this.comparable = true;
            Interface comparableInterface = new Interface("Comparable");
            if (!interfaces.contains(comparableInterface)) {
                interfaces.add(comparableInterface);
            }
        } else {
            Interface comparableInterface = null;
            for (Interface i : interfaces) {
                if (i.getName().equals("Comparable")) {
                    comparableInterface = i;
                }
            }
            interfaces.remove(comparableInterface);
            this.comparable = false;
        }
        publisher.inform(this, "ordered", null, isComparable());
    }

    /**
     * setting of the abstractness of this objecttype
     *
     * @param isAbstract
     * @throws ChangeNotAllowedException population of this objecttype is not
     * empty
     */
    public void setAbstract(boolean isAbstract) throws ChangeNotAllowedException {
        if (isAbstract) {
            if (getFactType().getPopulation().tuples().hasNext()) {
                throw new ChangeNotAllowedException(("POPULATION OF ") + getName() + (" IS NOT EMPTY"));
            }
        }
        this._abstract = isAbstract;
        publisher.inform(this, "abstract", null, isAbstract());
    }

    /**
     *
     * @return the object type expresssion of this objecttype
     */
    public TypeExpression getOTE() {
        return ote;
    }

    /**
     *
     * @return the objectrole who controls objects of this objecttype during the
     * whole lifetime ; if such creational-objecttype doesn't exist, null will
     * be returned
     */
    public ObjectRole getCreationalRole() {

        for (Role role : getFactType().roles) {
            if (role.isAddable() || role.isInsertable() || role.isComposition()) {
                return (ObjectRole) role;
            }
        }

        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && (counterpart.isAddable() || counterpart.isInsertable() || counterpart.isComposition()) && role.isMandatory()) {
                return (ObjectRole) counterpart;
            }
        }

        for (ObjectType supertype : supertypes) {
            ObjectRole role = supertype.getCreationalRole();
            if (role != null) {
                return role;
            }
        }

        return null;
    }

    public List<Role> getPlaysRoles() {
        return Collections.unmodifiableList((List<Role>) plays);
    }

    /**
     * supertype will be a supertype of this objecttype this objecttype is as
     * subtype registered at supertype;
     *
     * @param supertype
     * @throws ChangeNotAllowedException if supertype is by accident a subtype
     * of this objecttype or vica versa
     */
    public void addSuperType(ObjectType supertype) throws ChangeNotAllowedException {
        if (supertype == null) {
            throw new RuntimeException(("NULL NOT ALLOWED"));
        }
        if (hasSuperType(supertype)) {
            return;
        }
        if (hasSubType(supertype)) {
            throw new ChangeNotAllowedException(("SUPERTYPE ") + supertype.getName() + (" IS A SUBTYPE OF") + (" THIS OBJECTTYPE ")
                + getName());
        }
        if (supertype.equals(this)) {
            throw new ChangeNotAllowedException(("SUPERTYPE ") + supertype.getName() + (" EQUALS ") + (" THIS OBJECTTYPE ")
                + getName());
        }

        if (supertype.isSingleton()) {
            throw new ChangeNotAllowedException("A SINGLETON AS SUPERTYPE IS NOT ALLOWED.");
        }

        List<Relation> idSuper = supertype.identifyingRelations();
        if (!idSuper.isEmpty()) {
            List<Relation> idSub = identifyingRelations();
            if (!idSub.isEmpty()) {
                throw new ChangeNotAllowedException("Super- and subtype do have both identifying relations; "
                    + "they need to be identical; "
                    + "unfortunately, if identical, this situation is still not supported");
                // check on equal roles --> removing of roles of subtype; migrating population of subtype
            }
        }

        supertype.addSubType(this);
        this.supertypes.add(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    /**
     * @param type will be added as subtype
     */
    void addSubType(ObjectType type) {
        subtypes.add(type);
        publisher.inform(this, "subtypesIterator", null, subtypes.iterator());
    }

    void removeSubType(ObjectType type) {
        boolean removed = subtypes.remove(type);

        if (!removed) {
            throw new RuntimeException(("REMOVING OF SUBTYPE HAS NOT SUCCEEDED"));
        }
        removeSource(type);
        if (subtypes.isEmpty()) {
            remove();
        }
        publisher.inform(this, "subtypesIterator", null, subtypes.iterator());
    }

    void removeSubTypes() {
        List<ObjectType> toRemove = new ArrayList<>(subtypes);
        for (ObjectType subtype : toRemove) {
            subtype.supertypes.remove(this);
            removeSource(subtype);
        }
        subtypes.clear();
    }

    /**
     * the supertype of this objecttype will be removed
     *
     * @throws ChangeNotAllowedException if any tuple in the population of this
     * objecttype refers to an object which plays a role in some facttype of
     * supertype (or his ancestors)
     */
    public void removeSupertype(ObjectType supertype) throws ChangeNotAllowedException {
        if (!supertypes.contains(supertype)) {
            throw new RuntimeException(("THIS OBJECTTYPE " + getName() + " HAS NO SUPERTYPE " + supertype.getName()));
        }

        // Tuple tuple = supertypesAreUsingTuplesOf(this);
        // if (tuple != null) {
        // throw new
        // ChangeNotAllowedException(("THIS OBJECTTYPE IS USED AS SUPERTYPE IN ")
        // + tuple.toString());
        // }
        supertype.removeSubType(this);
        supertypes.remove(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    public void removeSupertypes() throws ChangeNotAllowedException {
        for (ObjectType supertype : new ArrayList<ObjectType>(supertypes)) {
            removeSupertype(supertype);
        }
    }

    void deleteSupertype(ObjectType supertype) {
        if (!supertypes.contains(supertype)) {
            throw new RuntimeException(("THIS OBJECTTYPE " + getName() + " HAS NO SUPERTYPE " + supertype.getName()));
        }

        supertype.removeSubType(this);
        supertypes.remove(supertype);
        publisher.inform(this, "supertypesIterator", null, supertypes.iterator());
    }

    /**
     * all existing subtypes of this objecttypes will be a direct subtype of ot;
     * ot will be the only subtype of this objecttype
     *
     * @param ot not equal to this objecttype
     * @throws ChangeNotAllowedException if ot is a super- or subtype of this
     * objecttype
     */
    public void insertSubtype(ObjectType ot) throws ChangeNotAllowedException {
        if (ot.equals(this)) {
            throw new RuntimeException("inserting " + ot.getName() + " as subtype at the same objecttype is not allowed.");
        }

        if (hasSubType(ot)) {
            throw new ChangeNotAllowedException(getName() + " is already supertype of " + ot.getName());
        } else if (hasSuperType(ot)) {
            throw new ChangeNotAllowedException(getName() + " is already subtype of " + ot.getName());
        } else {
            for (ObjectType subtype : subtypes) {
                subtype.addSuperType(ot);
            }
            ot.addSuperType(this);
            for (ObjectType subtype : subtypes) {
                if (!subtype.equals(ot)) {
                    subtype.removeSupertype(this);
                }
            }
        }
    }

    Tuple supertypesAreUsingTuplesOf(ObjectType ot) {
        Tuple tuple;
        for (ObjectType supertype : supertypes) {
            tuple = supertype.usesTuplesOf(ot.getFactType().getPopulation());
            if (tuple != null) {
                return tuple;
            }
            tuple = supertype.supertypesAreUsingTuplesOf(ot);
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    /**
     *
     * @return an iterator over all direct supertypes of this objecttype
     */
    public Iterator<ObjectType> supertypes() {
        return supertypes.iterator();
    }

    public int countSupertypes() {
        return supertypes.size();
    }

    public Iterator<Interface> interfaces() {
        return interfaces.iterator();
    }

    /**
     *
     * @return an iterator over all direct subtypes of this objecttype
     */
    public Iterator<ObjectType> subtypes() {
        return subtypes.iterator();
    }

    /**
     *
     * @return a list with all (direct and indirect) non-abstract subtypes
     */
    public List<ObjectType> concreteSubTypes() {
        List<ObjectType> types = new ArrayList<>();

        for (ObjectType subtype : subtypes) {
            if (!subtype.isAbstract()) {
                types.add(subtype);
            }
            types.addAll(subtype.concreteSubTypes());
        }

        return types;
    }

    // /**
    // * this objecttype is reduced to a facttype;<br> every involved role will
    // be
    // * replaced by the identifying roles of the former objecttype;<br> the
    // * type-expressions of the roletypes where a involved role belongs to will
    // * be adjusted with the help of the OTE of the former objecttype the
    // * population of the roletype where a involved rol belongs to will be
    // * adjusted to the new situation.
    // *
    // * @throws ChangeNotAllowedException if facttype doesn't possesses a
    // * facttype expression or objecttype is involved in inheritance relation
    // */
    // public void deobjectify() throws ChangeNotAllowedException {
    // // via parent regelen
    // throw new UnsupportedOperationException();
    // }
    /**
     *
     * @param type
     * @return true if type is direct or indirect a subtype of this objecttype,
     * else false
     */
    public boolean hasSubType(ObjectType type) {
        for (ObjectType ot : subtypes) {
            if (ot == type) {
                return true;
            }
            if (ot.hasSubType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param type
     * @return true if type is a (indirect) supertype of this objecttype, else
     * false
     */
    public boolean hasSuperType(ObjectType type) {
        for (ObjectType supertype : supertypes) {
            if (supertype.equals(type)) {
                return true;
            }
            if (supertype.hasSuperType(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * warning: in theory it could be possible that there exist more than one
     * mandatorial relation
     *
     * @param parent
     * @return
     */
    public Relation retrieveMandatorialRelationTo(ObjectType parent) {
        FactType ft = this.getFactType();
        for (Role idRole : ft.roles) {
            if (idRole.getSubstitutionType().equals(parent)) {
                return new IdRelation(this, idRole);
            }
        }

        for (Role role : plays) {
            FactType parentOfRole = role.getParent();
            if (parentOfRole.isClass()) {
                if (parentOfRole.getObjectType().equals(parent) && role.isMandatory()) {
                    return new ObjectTypeRelation(this, role);
                }
            } else {
                Role counterpart = parentOfRole.counterpart(role);
                if (counterpart != null && counterpart.getSubstitutionType().equals(parent) && role.isMandatory()) {
                    return new FactTypeRelation(this, role);
                }
            }
        }
        return null;
    }

    /**
     * makes an complete expression based on the value and the OTE of this
     * objecttype
     *
     * @param value
     */
    @Override
    public String makeExpression(Value value
    ) {
        if (value instanceof AbstractValue) {
            return value.toString();
        }
        return ote.makeExpression((Tuple) value);
    }

    @Override
    public Value parse(String expression, String separator, Requirement source) throws MismatchException {
        String concreteObjectExpression = expression;
        if (separator != null && !separator.isEmpty()) {
            int end = expression.indexOf(separator);
            if (end > 0) {
                concreteObjectExpression = expression.substring(0, end);
            }
        }

        if (isAbstract()) {

            return new AbstractValue(this, source, this, concreteObjectExpression);
        } else if (!ote.isParsable()) {
            return new UnparsableValue(this, source, this, concreteObjectExpression);
        } else { // delegating parsing to facttype, he knows the roles
            return getFactType().parse(expression, ote, separator, source);
        }
    }

    /**
     * parses the substitutionvalue on base of expressionParts. If
     * expressionParts is not parsable null will be returned
     *
     * @param expressionParts
     * @param source the source of expression
     * @return the constructed value based on expressionParts and source; if
     * creation is rejected null will be returned
     */
    public Value parse(List<String> expressionParts, Requirement source) throws MismatchException {
        return getFactType().parse(expressionParts, ote, source);
    }

    /**
     * this objecttype gets involved in role
     *
     * @param role this objecttype doesn't play a role in given role yet
     */
    @Override
    public void involvedIn(Role role) {
        if (plays.contains(role)) {
            throw new RuntimeException(("OBJECTTYPE IS ALREADY INVOLVED IN ROLE "));
        }
        plays.add(role);
    }

    /**
     * this substitutiontype will stop playing a role in given role
     *
     * @param role this objecttype isn't the substitutiontype of this role
     * anymore
     */
    @Override
    public void resignFrom(Role role) {
        if (!role.getSubstitutionType().equals(this)) {
            throw new RuntimeException(("CHANGE FIRST THE SUBSTITUTIONTYPE OF ") + ("THIS ROLE"));
        }
        plays.remove(role);
        if (isSolitary()) {
            remove();
        }
    }

    void resignFrom(ObjectType ot) {
        List<Role> toRemove = new ArrayList<>();
        for (Role role : plays) {
            if (role.getSubstitutionType() == ot) {
                toRemove.add(role);
            }
        }
        plays.removeAll(toRemove);
        if (isSolitary()) {
            remove();
        }
    }

    /**
     *
     * @return the name of this objecttype
     */
    @Override
    public String getName() {
        return getFactType().getName();
    }

    public String getPluralName() {
        return Naming.plural(getName());
    }

    void resignTuplesFromAllRoles() {
        for (Role role : plays) {
            role.getParent().getPopulation().clearPopulation();
        }
    }

    Tuple searchForUseOf(Tuple t) {
        for (Role r : plays) {
            Tuple tuple = r.getParent().getPopulation().anyTupleWhichUses(t, r.getNr());
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Type o) {
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectType) {
            return this.compareTo((ObjectType) o) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + this.getName().hashCode();
        return hash;
    }

    /**
     * every objecttype is a facttype too
     *
     * @return the corresponding facttype of this objecttype
     */
    public FactType getFactType() {
        return (FactType) getParent();
    }

    List<Relation> playsRoleRelations(boolean navigable, boolean withSupertypeRoles) {
        ArrayList<Relation> list = new ArrayList<>();

        for (Role role : plays) {
            if ((role.isNavigable() || !navigable) && !role.isQualifier()) {
                Relation relation;
                if (role.getParent().isClass()) {
                    relation = new ObjectTypeRelation(this, role);
                } else if (role.getParent().nonQualifierSize() == 1) {
                    relation = new BooleanRelation(this, role);
                } else {
                    Role counterpart = role.getParent().counterpart(role);
                    if (counterpart != null && !role.isMandatory() && !counterpart.isCreational() && counterpart.getSubstitutionType().isSingleton()) {
                        relation = new BooleanSingletonRelation(this, role);
                    } else {
                        relation = new FactTypeRelation(this, role);
                    }
                }
                list.add(relation);
            }
        }
        if (withSupertypeRoles) {
            for (ObjectType supertype : supertypes) {
                list.addAll(supertype.playsRoleRelations(navigable, true));
            }
        }
        return list;
    }

    /**
     *
     * @return true if this objecttype, or a supertype, plays a role somewhere,
     * otherwise false
     */
    public boolean isSolitary() {
        if (isValueType()) {
            return false;
        }
        if (plays.isEmpty()) {
            if (subtypes.isEmpty()) {
                if (supertypes.isEmpty()) {
                    return true;
                } else {
                    for (ObjectType supertype : supertypes) {
                        if (!supertype.isSolitary()) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isReducable() {
        if (isValueType()) {
            return false;
        }

        List<Relation> directNavRelations = relations(true, false);
        if (directNavRelations.size() > 1) {
            return false;
        } else if (!supertypes.isEmpty() || !subtypes.isEmpty()) {
            return false;
        } else {
            return directNavRelations.size() - identifyingRelations().size() == 0;
        }
    }

    public void checkActivity(boolean withSubTypesCheck) throws ChangeNotAllowedException {
        if (!plays.isEmpty()) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" PLAYS ROLES"));
        }

        if (withSubTypesCheck && !subtypes.isEmpty()) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" HAS SUBTYPES"));
        }

        Tuple tuple = supertypesAreUsingTuplesOf(this);
        if (tuple != null) {
            throw new equa.meta.ChangeNotAllowedException(("OBJECTTYPE ") + getName() + (" IS IN USE IN TUPLE ") + tuple.toString());
        }

    }

    @Override
    public boolean isEqualOrSupertypeOf(SubstitutionType st) {
        if (equals(st)) {
            return true;
        }
        if (st instanceof ObjectType) {
            return ((ObjectType) st).hasSuperType(this);
        } else {
            return false;
        }
    }

    /**
     *
     * @param ot is not a super or subtype of this objecttype or equal
     * @return a type t whereby t is supertype of this type and o t and there
     * exists no subtype of t with the same property; if such an objecttype
     * doesn't exist a null will be returned
     */
    public ObjectType detectCommonSupertype(ObjectType ot) {
        Set<ObjectType> set1 = allSupertypes();
        Set<ObjectType> set2 = ot.allSupertypes();
        Set<ObjectType> common = new HashSet<>();
        for (ObjectType supertype : set1) {
            if (set2.contains(supertype)) {
                common.add(supertype);
            }
        }

        if (common.isEmpty()) {
            return null;
        } else {
            Iterator<ObjectType> it = common.iterator();
            ObjectType candidate = it.next();
            while (it.hasNext()) {
                ObjectType otherCommon = it.next();
                if (candidate.hasSubType(otherCommon)) {
                    candidate = otherCommon;
                }
            }
            return candidate;
        }

    }

    /**
     *
     * @return a set with all (direct and indirect) supertypes of this
     * objecttype
     */
    public Set<ObjectType> allSupertypes() {
        Set<ObjectType> allSupertypes = new HashSet<>();
        for (ObjectType supertype : supertypes) {
            allSupertypes.add(supertype);
            allSupertypes.addAll(supertype.allSupertypes());
        }
        return allSupertypes;
    }

    /**
     *
     * @return a set with all (direct and indirect) subtypes of this objecttype
     */
    public Set<ObjectType> allSubtypes() {
        Set<ObjectType> allSubtypes = new HashSet<>();
        for (ObjectType subtype : subtypes) {
            allSubtypes.add(subtype);
            allSubtypes.addAll(subtype.allSubtypes());
        }
        return allSubtypes;
    }

    /**
     *
     * @return true if this objecttype is a singleton (there allways exists
     * exactly one object of this type) otherwise false
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    /**
     * @return the resulting class of this objecttype, could be undefined = null
     */
    public CodeClass getCodeClass() {
        //algorithms.clear();
        return codeClass;
    }

    public void generateClass() {
        relations = getFactType().relations(true, false);
        codeClass = new CodeClass(this, relations);
        generateFields(relations);
        generateProperties(relations);
    }

    void generateMethods() {
        List<Relation> relations = codeClass.getRelations();
        try {

            generateToStringMethod();
            if (isValueType()) {
                generateEqualsMethod();
            }
            if (!isLight()) {
                if (!isAbstract()) {
                    generatePropertiesMethod();
                    if (!isValueType()) {
                        generateIdentifierMethod();
                    }
                }
                generateCompareToMethod();
            }

            generateMethods(relations);

            if (!isValueType() && !isSingleton() && containsObjectFields()) {
                codeClass.addOperation(new Strip(this));
            }
        } catch (DuplicateException exc) {
            exc.printStackTrace();
        }
    }

    public boolean containsObjectFields() {
        Iterator<Field> it = codeClass.getFields();
        ObjectType responsible = getResponsible();
        while (it.hasNext()) {
            Relation relation = it.next().getRelation();
            if (relation != null) {
                SubstitutionType type = relation.targetType();
                if (!type.isValueType() && !type.equals(responsible) /*&& !type.isSingleton()*/) {
                    return true;
                }
            }
        }
        if (supertypes.isEmpty()) {
            return false;
        } else {
            return supertypes.iterator().next().containsObjectFields();
        }
    }

    void generateFields(List<Relation> relations) {
        final boolean AUTO_INCR = true;
        for (Relation r : relations) {
            if (r.isNavigable() && !r.isDerivable()/* && (r.targetType() == null || !r.targetType().isSingleton())*/) {
                codeClass.addField(new Field(r));
            }
            if (!r.isMandatory() && !r.hasMultipleTarget() && r.targetType().getUndefinedString() == null
                && !r.targetType().equals(BaseType.BOOLEAN)) {
                codeClass.addField(new Field(BaseType.BOOLEAN, r.fieldName() + "Defined", !AUTO_INCR));
            }
            String autoIncrFieldName = r.getAutoIncrFieldName();
            if (r.isResponsible()) {
                if (autoIncrFieldName != null) {
                    codeClass.addField(new Field(BaseType.NATURAL, autoIncrFieldName, AUTO_INCR));
                } else {
                    if (r.targetType() instanceof ObjectType) {
                        ObjectType ot = (ObjectType) r.targetType();
                        for (ObjectType subtype : ot.subtypes) {
                            String roleName = subtype.getFactType().hasAutoIncr();
                            if (roleName != null) {
                                autoIncrFieldName = roleName + "Next" + Naming.withCapital(subtype.getName());
                                codeClass.addField(new Field(BaseType.NATURAL, autoIncrFieldName, AUTO_INCR));
                            }
                        }
                    }
                }
            }
        }
    }

    void generateConstructor() {
        try {
            generateConstructor(codeClass.getRelations());
        } catch (DuplicateException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void generateConstructor(List<Relation> relations) throws DuplicateException {
        Constructor constructor = new Constructor(relations, this, codeClass);
        codeClass.addOperation(constructor);

        if (isSingleton()) {
            constructor.setAccessModifier(AccessModifier.PRIVATE);
            Method singleton = new GetSingletonMethod(this, codeClass);
            codeClass.addOperation(singleton);
            Method clear = new ClearSingletonMethod(this, codeClass);
            codeClass.addOperation(clear);
        }
    }

    void generateProperties(List<Relation> relations) {
        for (Relation relation : relations) {
            if (relation.isNavigable() && !relation.targetType().isSingleton()) {

                if (relation.getMinFreq() < relation.getMaxFreq() && relation.hasMultipleTarget()) {
                    if (!relation.isHidden() && !relation.isDerivable()) {
                        generateCountMethod(relation);
                    }
                }

                //  if (relation.isDerivable() && !relation.isQualifier()) {
                // MetaOH metaOH;
                // if (relation.isCollectionReturnType()) {
                // metaOH = new MetaOH(relation.getPluralName(), true,
                // relation.collectionType(), new ArrayList<>());
                // } else {
                // metaOH = new MetaOH(relation.name(), true,
                // relation.targetType(), new ArrayList<>());
                // }
                // addAlgorithm(metaOH);
                //  } else 
                {

                    Property property;
                    if (relation.isMapRelation()) {
                        property = new IndexedProperty(relation, this);
                        codeClass.addOperation(property);
                    } else if (relation.isSeqRelation()) {
                        if (!relation.isSeqAutoIncrRelation()) {
                            property = new IndexedProperty(relation, this);
                            codeClass.addOperation(property);
                        }

                    } else {
                        property = new Property(relation, this);
                        codeClass.addOperation(property);
                    }

                    if (!relation.isMandatory() && relation.targetType().getUndefinedString() == null) {
                        // unknown basevalues are risky, that's why we add a
                        // supplementary
                        // boolean property which indicates if the property is
                        // defined properly

                        if (!relation.isHidden() && !relation.targetType().equals(BaseType.BOOLEAN)) {
                            Method defined = new IsDefinedMethod(relation, this);
                            codeClass.addOperation(defined);
                        }
                    }
                }

            }
        }
    }

    void generateMethods(List<Relation> relations) throws DuplicateException {
        for (Relation relation : relations) {
            SubstitutionType st = relation.targetType();

            if (relation.hasMultipleTarget()) {

                if (!relation.isComposition()) {
                    generateContainsMethod(relation, st);
                    //   generateMoveMethod(relation);
                }

                if (st instanceof ObjectType) {
                    if (relation.isResponsible()) {
                        if (relation.isMapRelation()) {
//                            Property property = new IndexedProperty(relation, this);
//                            codeClass.addOperation(property);
                        } else {
                            generateSearchMethodObjectType(relation, (ObjectType) st);
                        }

                    }
                }

                generateAddAndInsertMethods(relation);
                generateChangeWithIndexMethods(relation);

            }

            if (relation.isCreational()) {
                ObjectType ot = (ObjectType) st;
                ot.encapsulateConstructor();

                if (ot.getFactType().isIdChangeable()) {
                    generateChangeIdMethod(relation, ot);
                }
            }

            if (!relation.multiplicity().equals("1") && !relation.isDerivable() && relation.targetType() != BaseType.BOOLEAN) {
                generateRemoveMethods(relation);
            }

            if (relation instanceof FactTypeRelation) {
                generateAdjustMethod((FactTypeRelation) relation);
            }

        }

        codeClass.eliminateDuplicateSignatures();
    }

    void generatePropertiesMethod() throws DuplicateException {
        Method propertiesMethod = new PropertiesMethod(this, codeClass);
        codeClass.addOperation(propertiesMethod);

    }

    void generateIdentifierMethod() throws DuplicateException {
        Method identifierMethod = new IdentifyingPropertiesMethod(this, codeClass);
        codeClass.addOperation(identifierMethod);
    }

    void generateChangeIdMethod(Relation relation, ObjectType ot) throws DuplicateException {
        List<Role> otherRoles = ot.getRolesPlayedByOtherSubstitutionTypes(this);
        if (!otherRoles.isEmpty()) {
            codeClass.addOperation(new ChangeIdMethod(relation, ot, otherRoles));
        }
    }

    void generateContainsMethod(Relation relation, SubstitutionType st) throws DuplicateException {
        if (!relation.isMapRelation() || relation.hasMultipleQualifiedTarget()) {
            Method containsMethod = new ContainsMethod(relation, this, st);
            codeClass.addOperation(containsMethod);
        }
    }

    void generateSearchMethodObjectType(Relation relation, ObjectType target) throws DuplicateException {
        if (target.isAbstract()) {
            // concrete subtypes ...
            List<ObjectType> concreteSubTypes = target.concreteSubTypes();
            for (ObjectType concreteSubType : concreteSubTypes) {
                if (concreteSubType.isUnderControlOf(this)) {
                    if (relation instanceof ObjectTypeRelation) {
                        generateSearchMethodObjectTypeRelation(relation, concreteSubType);
                    } else {
                        if (relation instanceof FactTypeRelation) {
                            generateSearchMethodFactTypeRelation((FactTypeRelation) relation, concreteSubType);
                        }
                    }
                }
            }
        } else {
            if (relation instanceof ObjectTypeRelation || relation instanceof CollectionIdRelation) {
                generateSearchMethodObjectTypeRelation(relation, target);
            } else {
                if (relation instanceof FactTypeRelation) {
                    generateSearchMethodFactTypeRelation((FactTypeRelation) relation, target);
                }
            }
        }
    }

    void generateChangeWithIndexMethods(Relation relation) throws DuplicateException {
        if (relation.isSeqRelation() && relation.getAutoIncrFieldName() == null) {

            if (relation.isRemovable()) {
                Method removeMethod = new RemoveAtMethod(relation, this);
                codeClass.addOperation(removeMethod);
            }

            if (relation.targetType() instanceof ObjectType) {
                Method indexOfMethod = new IndexOfMethod(relation, this);
                codeClass.addOperation(indexOfMethod);
            }

            if (!isLight()) {
//                Method subseqMethod = new SubsequenceMethod(relation, this);
//                codeClass.addOperation(subseqMethod);
            }
        }
    }

    void generateAdjustMethod(FactTypeRelation relation) throws DuplicateException {
        if (relation.isAdjustable()) {
            Method adjustMethod = new AdjustMethod(relation, this);
            codeClass.addOperation(adjustMethod);
        }
    }

    void generateAddAndInsertMethods(Relation relation) throws DuplicateException {
        if (relation.isDerivable()) {
            return;
        }

        SubstitutionType st = relation.targetType();

        if (st instanceof BaseType) {
            if (relation.isAddable() || (relation.hasMutablePermission() && relation.isSetRelation())) {
                Method addMethod;
                if (relation.isMapRelation()) {
                    addMethod = new PutMethod(relation, this, relation.targetType());
                } else {
                    addMethod = new AddBaseTypeMethod(relation, this);
                }
                if (!relation.isAddable()) {
                    addMethod.setAccessModifier(AccessModifier.NAMESPACE);
                }
                codeClass.addOperation(addMethod);
            }
            if (relation.isInsertable()) {
                Method insertMethod = new InsertBaseTypeMethod(relation, this);
                codeClass.addOperation(insertMethod);
            }
//            else if (relation.hasMutablePermission()) {
//                Method insertMethod = new InsertBaseTypeMethod(relation, this);
//                codeClass.addOperation(insertMethod);
//                insertMethod.setAccessModifier(AccessModifier.NAMESPACE);
//            }

        } else {
            ObjectType target = (ObjectType) st;
            if (target.isSingleton()) {
                return;
            }

            if (relation.isResponsible()) {
                if (relation.isCreational()) {
                    if (!target.isAbstract()) {
                        generateAddAndInsertMethodConcreteObjectType(target, relation);
                    }
                    List<ObjectType> concreteSubTypes = target.concreteSubTypes();
                    for (ObjectType concreteSubType : concreteSubTypes) {
                        if (concreteSubType.isUnderControlOf(this)) {
                            generateAddAndInsertMethodConcreteObjectType(concreteSubType, relation);
                        }
                    }
                } else {
                    generateAddAndInsertMethodConcreteObjectType(target, relation);
                }
            } else {
                generateAddAndInsertMethodConcreteObjectType(target, relation);
            }
        }
    }

    void generateAddAndInsertMethodConcreteObjectType(ObjectType concreteObjectType, Relation relation) throws DuplicateException {

        if (!relation.isNavigable()) {
            return;
        }

        if (relation instanceof IdRelation) {
            return;
        }

        Method addMethod;

//        if (relation.isCreational()) {
//            concreteObjectType.encapsulateConstructor();
//            if (relation.isMapRelation()) {
//                addMethod = new PutMethod(relation, this, concreteObjectType);
//            } else {
//                addMethod = new AddObjectTypeMethod(relation, concreteObjectType, this);
//            }
//        } else if (relation.isAddable()) {
//            if (relation.isMapRelation()) {
//                addMethod = new PutMethod(relation, this, concreteObjectType);
//            } else {
//                addMethod = new RegisterMethod(relation, this);
//            }
//        } else {
//            if (relation.isMapRelation()) {
//                addMethod = new PutMethod(relation, this, concreteObjectType);
//            } else {
//                addMethod = new RegisterMethod(relation, this);
//            }
//            addMethod.setAccessModifier(AccessModifier.NAMESPACE);
//        }
        if (relation.isCreational()) {
            concreteObjectType.encapsulateConstructor();
            if (relation.isMapRelation()) {
                addMethod = new PutMethod(relation, this, concreteObjectType);
            } else {
                addMethod = new AddObjectTypeMethod(relation, concreteObjectType, this);
            }
        } else {
            if (relation.isMapRelation()) {
                addMethod = new PutMethod(relation, this, concreteObjectType);
            } else {
                addMethod = new RegisterMethod(relation, this);
            }
            if (!relation.isAddable()) {
                addMethod.setAccessModifier(AccessModifier.NAMESPACE);
            }
        }

        codeClass.addOperation(addMethod);

        if (relation.isInsertable()) {
            Method insertMethod = new InsertObjectTypeMethod(relation, this);
            codeClass.addOperation(insertMethod);
        }
//        else if (relation.isSeqRelation()
//            && relation.hasMutablePermission()) {
//            Method insertMethod = new InsertObjectTypeMethod(relation, this);
//            codeClass.addOperation(insertMethod);
//            insertMethod.setAccessModifier(AccessModifier.NAMESPACE);
//        }

    }

    void generateCountMethod(Relation relation
    ) {

        int lower = relation.multiplicityLower();
        int upper = relation.multiplicityUpper();

        if (lower == upper) {
            return;
        }

        if (lower > 0) {
            Operation minCount = new MinCountMethod(relation, this, lower, relation.getLowerConstraint());
            codeClass.addOperation(minCount);
        }
        if (upper > 0 && upper < Integer.MAX_VALUE) {
            Operation maxCount = new MaxCountMethod(relation, this, upper, relation.getUpperConstraint());
            codeClass.addOperation(maxCount);
        }

        Method countMethod = new CountMethod(relation, this);
        codeClass.addOperation(countMethod);

    }

    void generateMoveMethod(Relation relation) throws DuplicateException {

        if (relation.isPartOfId()) {
            return;
        }

        Relation inverse = relation.inverse();
        if (inverse != null && inverse.multiplicity().equals("1") && relation.getParent().isMutable() && !relation.isComposition()) {
            // multiplicity = 1; no composition-relation; relation and inverse
            // is modifiable
            Method moveMethod = new MoveMethod(relation, this);
            codeClass.addOperation(moveMethod);
        }
    }

    void generateRemoveMethods(Relation relation) throws DuplicateException {

        if (relation.isPartOfId()) {
            return;
        }

        boolean withRemove = false;

//        if (!isLight() && relation.isNavigable() && relation.hasMultipleTarget() && relation.isRemovable()) {
//            Method removeAllMethod = new RemoveAllMethod(relation, this);
//            codeClass.addOperation(removeAllMethod);
//            withRemove = true;
//        }
        Relation inverse = relation.inverse();
        if (relation.isRemovable()) {
            Method removeMethod = new RemoveMethod(relation, this);
            codeClass.addOperation(removeMethod);
            withRemove = true;
        } else if (relation.isNavigable() && inverse != null && !inverse.isComposition()
            && (relation.hasMultipleTarget() || !relation.isMandatory() || relation.hasMutablePermission())) {
            ObjectType target = (ObjectType) relation.targetType();
            ObjectType responsible = getResponsible();
            if (inverse.isRemovable() || inverse.isSettable() || (!target.equals(responsible) && target.isRemovable())
                || relation.hasMutablePermission()) {
                // if (!relation.targetType().isSingleton())
                {
                    Method removeMethod = new RemoveMethod(relation, this);
                    codeClass.addOperation(removeMethod);
                    withRemove = true;
                }
            }
        }

        if (withRemove && relation.isResponsible()) {
            SubstitutionType target = relation.targetType();
            if (!target.isValueType()) {
                ObjectType targetObjectType = (ObjectType) target;
                Set<Relation> contacts = targetObjectType.fans();

                contacts.remove(inverse);
                if (!contacts.isEmpty()) {
                    if (!targetObjectType.codeClass.operationPresent("isRemovable")) {
                        targetObjectType.codeClass.addOperation(new IsRemovableMethod(relation, targetObjectType));
                    }
                }
            }
        }
    }

    // final boolean hasCompositionalRemovable(ObjectType
    // compositionalResponsible) {
    // if (compositionalResponsible == null) {
    // return false;
    // }
    // return !compositionalResponsible.equals(this);
    // }
    void generateSearchMethodFactTypeRelation(FactTypeRelation relation, ObjectType concreteObjectType) throws DuplicateException {

        // if (relation.getParent().size() > 2) {
        // // elementary fact type contains qualifier roles
        // Operation indexedProperty = new IndexedProperty(relation, this);
        // codeClass.addOperation(indexedProperty);
        // } else
        {
            // elementary binary fact type
            boolean indexMethodNeeded = true;

            if (relation.targetType() instanceof CollectionType) {
                Method searchMethod = new SearchCollectionMethod(relation, this);
                codeClass.addOperation(searchMethod);
            } else {
                Method searchMethod;

                searchMethod = new SearchMethod(relation, concreteObjectType, this);
                Iterator<Param> itParams = searchMethod.getParams().iterator();
                if (itParams.hasNext()) {
//                    Param param1 = itParams.next();
//                    if (param1.getType().equals(BaseType.NATURAL) && !itParams.hasNext() && !param1.getRelation().isAutoIncr()) {
//                        indexMethodNeeded = false;
//                    }

                    codeClass.addOperation(searchMethod);

                    if (searchLexicalNeeded(searchMethod.getParams())) {
                        codeClass.addOperation(new SearchLexicalMethod(relation, concreteObjectType, this));
                    }
                } else {
                    // codeClass.addOperation(searchMethod);
                }

            }
            // if (relation.isSeqRelation() && (indexMethodNeeded ||
            // relation.targetType() instanceof BaseType)) {
            // Method indexMethod = new IndexMethod(relation, this);
            // codeClass.addOperation(indexMethod);
            //
            // }
        }
    }

    void generateSearchMethodObjectTypeRelation(Relation relation, ObjectType concreteObjectType) throws DuplicateException {

        Method searchMethod = new SearchMethod(relation, concreteObjectType, this);

        Iterator<Param> itParams = searchMethod.getParams().iterator();
        Param param1 = itParams.next();
        if (param1.getType().equals(BaseType.NATURAL) && !itParams.hasNext() && !param1.getRelation().isAutoIncr()) {
        } else {
            codeClass.addOperation(searchMethod);
//            if (!relation.isSetRelation()) {
//                Method indexMethod = new IndexMethod(relation, this);
//                codeClass.addOperation(indexMethod);
//            }
        }

        if (searchLexicalNeeded(searchMethod.getParams())) {
            codeClass.addOperation(new SearchLexicalMethod(relation, concreteObjectType, this));
        }

    }

    static boolean searchLexicalNeeded(List<Param> params) {
        for (Param param : params) {
            if (param.getType() instanceof BaseType) {
            } else if (param.getType() instanceof ObjectType) {
                ObjectType ot = (ObjectType) param.getType();
                if (!ot.isAbstract()) {
                    return true;
                }
            } else {
                // CT ct = (CT) param.getType();
                // todo
            }
        }
        return false;
    }

    void generateToStringMethod() throws DuplicateException {
        if (!_abstract) {
            Method toStringMethod = new ToStringMethod(this, codeClass);
            codeClass.addOperation(toStringMethod);
        }
    }

    void generateEqualsMethod() throws DuplicateException {
        if ((!isLight() || this.isValueType()) && !this.isSingleton() && !_abstract) {
            Method equalsMethod = new EqualsMethod(this, codeClass);
            codeClass.addOperation(equalsMethod);
            Method hashCodeMethod = new HashCodeMethod(this, codeClass);
            codeClass.addOperation(hashCodeMethod);
        }
    }

    void generateCompareToMethod() throws DuplicateException {
        if (comparable) {
            Method compareToMethod = new CompareToMethod(this, codeClass);
            codeClass.addOperation(compareToMethod);
        }
    }

    void encapsulateConstructor() {
        if (!isValueType()) {
            codeClass.encapsulateConstructor();
            for (ObjectType subtype : subtypes) {
                subtype.encapsulateConstructor();
            }
        }
    }

    /**
     *
     * @return a set with the names of all public properties with
     * getter-functionality of this objecttype; properties of the supertype(s)
     * are included
     */
    public Set<String> publicProperties() {
        Set<String> properties = new TreeSet<String>();
        for (Relation relation : relations(true, true)) {
            if (!relation.isHidden()) {
                properties.add(relation.fieldName());
            }
        }
        return properties;
    }

    public int countOfProperties() {
        return relations(true, true).size();
    }

    /**
     *
     * @return a set with all identifying properties of this object type
     */
    public List<Relation> identifyingRelations() {
        return getFactType().identifyingRelations();
    }

    public List<STorCT> identifierTypes() {
        List<STorCT> identifierTypes = new ArrayList<>();
        for (Relation relation : getFactType().identifyingRelations()) {
            identifierTypes.add(relation.targetType());
        }
        return identifierTypes;
    }

    // private static String detectUniqueName(String roleName, List<String>
    // names) {
    // String name = roleName;
    // if (names.contains(name)) {
    // int nr = 1;
    // while (names.contains(name + "_" + nr)) {
    // nr++;
    // }
    // name = name + "_" + nr;
    // }
    // names.add(name);
    // return name;
    // }
    @Override
    public String toString() {
        return getName();
    }

    void setOTE(FactType parent, List<String> constants, int[] rolenrs) {
        throw new UnsupportedOperationException();
    }

    void setOTE(TypeExpression ote) {
        this.ote = ote;
    }

    private List<Role> getRolesPlayedByOtherSubstitutionTypes(ObjectType ot) {
        FactType ft = getFactType();
        List<Role> otherRoles = new ArrayList<>();
        Iterator<Role> itRoles = ft.roles();
        while (itRoles.hasNext()) {
            Role role = itRoles.next();
            if (role.getSubstitutionType() != ot && !role.isQualifier()) {
                otherRoles.add(role);
            }
        }
        return otherRoles;
    }

    /**
     *
     * @param accessibles
     * @return if this object type is accessible
     */
    boolean isAccessible() {
        // if this objecttype is responsible then he cannot be made accessible
        // as a
        // consequence of a mandatorial role he plays in a non-objectified fact
        // type

        for (Role role : allRoles()) {
            if (role.isMandatory() && !role.isResponsible()) {
                FactType parentOfRole = role.getParent();
                // if this object type plays role in non-objectified fact
                // type
                if (!parentOfRole.isObjectType()) {
                    Role counterpart = parentOfRole.counterpart(role);
                    if (counterpart != null && counterpart.isNavigable()) {
                        ObjectType st = (ObjectType) counterpart.getSubstitutionType();
                        Set<ObjectType> responsibles = st.getResponsibles();
                        if (responsibles.isEmpty() || !responsibles.contains(this)) {
                            return true;
                        }
                    }
                }
            }
        }

        if (isAbstract()) {
            for (ObjectType subtype : subtypes) {
                if (!subtype.getFactType().isAccessible()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private List<Role> allRoles() {
        List<Role> allRoles = new ArrayList<>(plays);
        for (ObjectType supertype : supertypes) {
            allRoles.addAll(supertype.allRoles());
        }
        return allRoles;
    }

    /**
     *
     * @return true if this object type plays a responsible role with respect to
     * a substitutiontype which isn't base type or value type
     */
    boolean isResponsibleForNonVT() {
        for (Role role : plays) {
            if (role.isResponsibleForNonVT()) {
                return true;
            }
        }
        return false;
    }

    public boolean creates(SubstitutionType st) {
        for (Role role : plays) {
            if (role.isCreational()) {
                if (role.getParent().isObjectType()) {
                    if (role.getParent().getObjectType().equals(st)) {
                        return true;
                    }
                } else {
                    Role counterpart = role.getParent().counterpart(role);
                    if (counterpart != null && counterpart.getSubstitutionType().equals(st)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean isDirectAccessible() {
        return true;
    }

    public boolean isDoubleRelatedWith(ObjectType ot) {
        int count = 0;
        for (Role role : plays) {
            SubstitutionType st = role.targetSubstitutionType();
            if (st != null && role.targetSubstitutionType().equals(ot)) {
                count++;
            }
        }
        return count > 1;
    }

    @Override
    public void addListener(PropertyListener listener, String property) {
        publisher.addListener(listener, property);
    }

    @Override
    public void removeListener(PropertyListener listener, String property) {
        publisher.removeListener(listener, property);
    }

    public TypeExpression getTypeExpression() {
        return ote;
    }

    public List<Relation> relations(boolean navigable, boolean withSupertypeRoles) {
        if (!navigable || withSupertypeRoles) {
            return getFactType().relations(navigable, withSupertypeRoles);
        }
        if (relations == null) {
            relations = getFactType().relations(true, false);
        }
        return relations;
    }

    public Set<Relation> fans() {
        Set<Relation> fans = new HashSet<>();
        for (Relation relation : relations(false, false)) {
            Relation inverse = relation.inverse();
            if (inverse != null && (inverse.isNavigable() || inverse.getOwner().equals(this))) {
                if (/*inverse.getOwner().getResponsible() != this && */getResponsible() != inverse.getOwner()) {
                    fans.add(inverse);
                }
            }
        }
        return fans;
    }

    List<String> getPath(ObjectType ot) {
        List<String> path = new ArrayList<>();
        for (ObjectType subtype : subtypes) {
            if (subtype.equals(ot)) {
                return path;
            }
            List<String> subpath = subtype.getPath(ot);
            if (subpath != null) {
                path.addAll(subpath);
                return path;
            }
        }
        return null;
    }

    @Override
    public boolean hasAbstractRoles() {
        return isAbstract() || getFactType().hasAbstractRoles();
    }

    @Override
    public String getUndefinedString() {
        return "null";
    }

    @Override
    public boolean isRemovable() {
        if (getFactType().isRemovable()) {
            return true;
        } else {
            if (supertypes.isEmpty()) {
                return false;
            } else {
                return supertypes.iterator().next().isRemovable();
            }
        }
    }

    @Override
    public void remove() {
        getFactType().remove();
    }

    void removeYourself() {
        super.remove();
        removeBehavior();
        removeAlgorithms();
        try {
            removeSupertypes();
        } catch (ChangeNotAllowedException ex) {
            Logger.getLogger(ObjectType.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    boolean relatedToAddableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isAddable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToSettableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isSettable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToAdjustableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isAdjustable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToRemovableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isRemovable()) {
                return true;
            }
        }
        return false;
    }

    boolean relatedToInsertableRole() {
        for (Role role : plays) {
            Role counterpart = role.getParent().counterpart(role);
            if (counterpart != null && counterpart.isInsertable()) {
                return true;
            }
        }
        return false;
    }

    void removeBehavior() {

        if (codeClass != null) {
            codeClass.remove();
            codeClass = null;
        }

    }

    @Override
    public boolean isSuitableAsIndex() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    boolean isSuperTypeResponsible() {
        for (ObjectType supertype : supertypes) {
            if (supertype.isResponsibleForNonVT()) {
                return true;
            }
        }
        return false;
    }

    boolean isReflexiveCreational() {
        if (isReflexiveCreational(this)) {
            return true;
        }

        return false;
    }

    boolean isReflexiveCreational(ObjectType forbiddenSubordinate) {
        for (Role role : plays) {
            if (role.isCreational()) {
                ObjectType subordinate = null;
                if (role.getParent().isObjectType()) {
                    subordinate = role.getParent().getObjectType();
                } else if (role.getParent().size() == 1) {
                } else {
                    Role counterpart = role.getParent().counterpart(role);
                    if (counterpart == null) {
                        // fact type with 3 or more significant roles
                    } else if (counterpart instanceof ObjectRole) {
                        subordinate = (ObjectType) counterpart.getSubstitutionType();
                    }
                }
                if (subordinate != null) {
                    if (forbiddenSubordinate.equals(subordinate)) {
                        return true;
                    }
                    if (subordinate.isReflexiveCreational(forbiddenSubordinate)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean doesHaveMoreParents() {
        return getFactType().doesHaveMoreParentRoles();
    }

    ObjectRole getParentRole() {
        ObjectRole r = getFactType().getParentRole();
        if (r != null) {
            return r;
        }
        for (Role role : plays) {
            if (!role.getParent().isObjectType()) {
                Role counterpart = role.getParent().counterpart(role);
                if (counterpart != null && counterpart.isCreational()) {
                    return (ObjectRole) counterpart;
                }
            }
        }
        return null;
    }

    void refreshFactRequirements() throws ChangeNotAllowedException {
        for (Role role : plays) {
            role.getParent().refreshFactRequirements();
        }
    }

    @Override
    public void removeMember(ModelElement member) {
        if (member instanceof Operation) {
            codeClass.removeMember(member);
        }
    }

    @Override
    public boolean isPureFactType() {
        return getFactType().isPureFactType();
    }

    boolean isMinimalAT() {
        if (!isAbstract()) {
            return false;
        }
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        int count = 0;
        for (ObjectType ot : concreteSubTypes) {
            if (!ot.isSingleton()) {
                count++;
            }
        }
        return count <= 1;
    }

    List<ObjectType> nonAccessibleConcreteSubTypes(Set<ObjectType> accessibles) {
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        List<ObjectType> nonAccessibleSubTypes = new ArrayList<>();
        for (ObjectType ot : concreteSubTypes) {
            if (!accessibles.contains(ot)) {
                nonAccessibleSubTypes.add(ot);
            }
        }
        return nonAccessibleSubTypes;
    }

    public ObjectType getResponsible() {
        if (isValueType()) {
            return null;
        }

        for (Role role : getFactType().roles) {
            if (role.isResponsible()) {
                return (ObjectType) role.getSubstitutionType();
            }
        }
        for (Role role : plays) {
            if (role.isMandatory()) {
                Role inverse = role.relatedRole(this);
                if (inverse != null && inverse.isResponsible()) {
                    return (ObjectType) inverse.getSubstitutionType();
                }
            }
        }
        for (ObjectType supertype : supertypes) {
            ObjectType responsible = supertype.getResponsible();
            if (responsible != null) {
                return responsible;
            }
        }
        return null;
    }

    public Set<ObjectType> getResponsibles() {
        Set<ObjectType> responsibles = new TreeSet<>();
        ObjectType responsible = getResponsible();
        while (responsible != null) {
            responsibles.add(responsible);
            responsible = responsible.getResponsible();
        }
        return responsibles;

    }

    public Relation getResponsibleRelation() {
        // if (isSuperTypeResponsible()) {
        // for (ObjectType ot : supertypes) {
        // if (ot.getResponsibleRelation() != null) {
        // return ot.getResponsibleRelation();
        // }
        // }
        // }
        for (Relation r : relations(false, false)) {
            Relation inverse = r.inverse();
            if (inverse != null && inverse.isResponsible()) {
                return inverse;
            }
        }
        return null;
    }

    public Set<ObjectType> mandatoryTargets() {
        Set<ObjectType> targets = new HashSet<>();
        for (Relation r : relations(true, true)) {
            if (r.isMandatory() && r.targetType() instanceof ObjectType) {
                targets.add((ObjectType) r.targetType());
            }
        }
        return targets;
    }

    boolean creatableElementsIn(Set<ObjectType> todo) {
        for (ObjectType other : todo) {
            ObjectType responsible = other.getResponsible();
            if (responsible != null && responsible.equals(this) && mandatoryTargets().contains(other)) {
                return true;
            }
        }
        return false;
    }

    ObjectType getUnmanagedSubType() {
        int countUnmanagedSubTypes = 0;
        ObjectType unmanagedSubType = null;
        for (ObjectType subType : concreteSubTypes()) {
            if (subType.getResponsible() == null) {
                countUnmanagedSubTypes++;
                unmanagedSubType = subType;
            }
        }
        if (countUnmanagedSubTypes == 1) {
            return unmanagedSubType;

        } else {
            return null;
        }
    }

    public ObjectType concreteSubType() {
        List<ObjectType> concreteSubTypes = concreteSubTypes();
        if (concreteSubTypes.size() == 1) {
            return concreteSubTypes.get(0);
        } else {
            return null;
        }

    }

    boolean isUnderControlOf(ObjectType ot) {
        ObjectType responsible = getResponsible();
        return responsible == ot;
    }

    void clearRelations() {
        relations = null;
    }

    @Override
    public List<Param> transformToBaseTypes(Param param) {
        List<Param> params = new ArrayList<>();
        if (isAbstract()) {
            params.add(param);
        } else {
            for (Relation relation : identifyingRelations()) {
                if (relation.targetType() instanceof ObjectType
                    && Objects.equals(((ObjectType) relation.targetType()).getResponsible(), this)) {
                    params.addAll(relation.targetType().transformToBaseTypes(param));
                } else {
                    SubParam subParam = new SubParam(relation, param);
                    params.addAll(relation.targetType().transformToBaseTypes(subParam));
                }
            }
        }
        return params;
    }

    @Override
    public ActualParam getUndefined() {
        return Null.NULL;
    }

    @Override
    public boolean isAddable() {
        return getFactType().isAddable();
    }

    @Override
    public boolean isSettable() {
        return getFactType().isSettable();
    }

    @Override
    public boolean isAdjustable() {
        return getFactType().isAdjustable();
    }

    @Override
    public boolean isInsertable() {
        return getFactType().isInsertable();
    }

    @Override
    public String expressIn(Language l) {
        return getName();
    }

    @Override
    public String callString() {
        return getName();
    }

    public String getKind() {
        if (isAbstract()) {
            return "AT";
        } else if (isValueType()) {
            return "VT";
        } else {
            return "OT";
        }
    }

    public String getExtendedKind() {
        return getKind();
    }

    boolean containsTuplesOfOtherSubtypes(ObjectType subtype) {
        if (subtypes.contains(subtype)) {
            for (ObjectType sub : subtypes) {
                if (!sub.equals(subtype)) {
                    if (sub.sizeOfPopulation() > 0) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public int sizeOfPopulation() {
        int size = getFactType().getPopulation().size();
        for (ObjectType subtype : subtypes) {
            size += subtype.sizeOfPopulation();
        }
        return size;
    }

    private Tuple usesTuplesOf(Population population) {
        for (Role role : plays) {
            FactType ft = role.getParent();
            Tuple tuple = ft.getPopulation().usesTuplesOf(population);
            if (tuple != null) {
                return tuple;
            }
        }
        return null;
    }

    public List<FactType> getBooleanFactTypes() {
        List<FactType> booleanFactTypes = new ArrayList<>();
        for (Role role : plays) {
            if (role.isNavigable()) {
                if (role.targetSubstitutionType().equals(BaseType.BOOLEAN)) {
                    booleanFactTypes.add(role.getParent());
                }
            }
        }
        for (ObjectType supertype : supertypes) {
            booleanFactTypes.addAll(supertype.getBooleanFactTypes());
        }
        return booleanFactTypes;
    }

//    public int countOfExternalAlgorithms() {
//
//        if (codeClass == null) {
//            return 0;
//        }
//        int count = 0;
//        for (Entry<OperationHeader, Algorithm> entry : algorithms.entrySet()) {
//            if (codeClass.getOperation(entry.getKey()) == null) {
//                count++;
//            }
//        }
//        return count;
//    }
    public boolean overrides(OperationHeader oh) {
        if (supertypes.isEmpty()) {
            return false;
        } else {
            ObjectType supertype = supertypes.iterator().next();
            return supertype.getCodeClass().getOperation(oh, true) != null;
        }
    }

}
