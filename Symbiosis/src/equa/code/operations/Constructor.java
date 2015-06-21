package equa.code.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import equa.code.CodeClass;
import equa.code.Field;
import equa.code.ImportType;
import equa.code.IndentedList;
import equa.code.Language;
import equa.meta.DuplicateException;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.BooleanSingletonRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.BaseValue;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.ObjectType;
import equa.meta.objectmodel.Range;
import equa.meta.objectmodel.SubstitutionType;
import equa.util.Naming;
import java.util.HashSet;

public class Constructor extends OperationWithParams {

    private static final long serialVersionUID = 1L;
    private final List<Relation> relations;

    /**
     * creation of a constructor in hehalf of parent with give params, based on
     * source
     *
     * @param relations
     * @param parent
     * @param source
     * @throws DuplicateException if there exists at parent a constructor with
     * the same signature
     */
    public Constructor(List<Relation> relations, ObjectType parent, CodeClass source) throws DuplicateException {
        super(parent, null, source);
        this.relations = relations;
        List<Param> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        boolean addId = true;
        // collecting parameters of supertype-constructor
        for (ObjectType supertype : parent.allSupertypes()) {
            Iterator<Param> itParams = supertype.getCodeClass().constructorParams();
            while (itParams.hasNext()) {
                Param param = itParams.next();
                if (!param.occursIn(params)) {
                    if (param.getRelation().isPartOfId()) {
                        addId = false;
                    }
                    String paramName = detectUniqueName(param.getName(), names);
                    Param paramNew = new Param(paramName, param.getType(), param.getRelation());
                    params.add(paramNew);
                    if (param.isAutoIncr()) {
                        paramNew.setAutoIncr();
                    }
                }
            }
        }

        for (Relation relation : relations) {
            if (((addId && (relation.isPartOfId() || relation.isMandatory()))
                || (!addId && (!relation.isPartOfId() && relation.isMandatory())))
                && !relation.isDerivable() && !relation.targetType().isSingleton()
                && relation.isNavigable() && relation.hasNoDefaultValue()) {
                // only mandatory non derivable relations without default value
                // are important

                String paramName;
                SubstitutionType target = relation.targetType();
                Relation mandatorialRel = null;
                ObjectType targetOt = null;
                if (target instanceof ObjectType) {
                    targetOt = (ObjectType) target;
                    mandatorialRel = targetOt.retrieveMandatorialRelationTo(parent);
                }
                if (mandatorialRel != null) {
                    Relation inverseMandatorialRel = mandatorialRel.inverse();
                    if (inverseMandatorialRel.isResponsible()/* && mandatorialRel.isMandatory()*/) {
                        // pick up necessary init-data in behalf of call
                        // constructor
                        Iterator<Param> itParams = targetOt.getCodeClass().constructorParams();
                        while (itParams.hasNext()) {
                            Param param = itParams.next();
                            if (!param.isAutoIncr()) {
                                if (!param.getRelation().equals(mandatorialRel)) {
                                    // no parameter which is based on this
                                    // particular relation

                                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                                        paramName = param.getRelation().getPluralName();
                                    } else {
                                        paramName = param.getName();
                                    }
                                    if (!relation.equals(param.getRelation())) {
                                        paramName = relation.name() + Naming.withCapital(paramName);
                                        // FP change by JH rejected
                                    }
                                    paramName = detectUniqueName(paramName, names);
                                    Param paramNew;
                                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                                        // sometimes an objecttype needs a
                                        // frozen collection of
                                        // values; in that case you need that
                                        // collection
                                        // at initialize-time
                                        paramNew = new Param(paramName, new CT(CollectionKind.COLL,
                                            param.getType()), relation);

                                    } else {
                                        Param parentParam = new Param(relation.name(), relation.targetType(), relation);
                                        paramNew = new SubParam(param.getRelation(), parentParam);
                                        // new Param(paramName, param.getType(), relation);
                                    }

                                    params.add(paramNew);
                                }
                            } else {

                            }
                        }
                    } else { // mandatorial reverse and not responsible
                        Param param;
                        if (relation.hasMultipleTarget()) {
                            param = new Param(relation.name(), new CT(CollectionKind.COLL, targetOt), relation);
                        } else {
                            param = new Param(relation.name(), targetOt, relation);
                        }
                        params.add(param);
                    }
                } else { // no mandatorial reverse

                    paramName = detectUniqueName(relation.name(), names);
                    if (relation.hasMultipleTarget() || relation.isSeqRelation()) {
                        // sometimes an objecttype needs a frozen collection
                        // of
                        // values; in that case you need that collection
                        // at initialize-time
                        params.add(new Param(relation.getPluralName(), new CT(CollectionKind.LIST, target), relation));
                    } else {
                        Param param = new Param(paramName, target, relation);
                        if (relation.isAutoIncr()) {
                            param.setAutoIncr();
                        }
                        params.add(param);
                    }

                }
            }
        }

        setParams(params);
    }

    /**
     *
     * @return name of the parent object type.
     */
    @Override
    public String getName() {
        return ((ObjectType) getParent()).getName();
    }

    /**
     *
     * @return 1, always.
     */
    @Override
    public int order() {
        return 1;
    }

    @Override
    public int compareTo(Operation o) {
        if (order() != o.order()) {
            return order() - o.order();
        }

        return ((Constructor) o).getParent().getName().compareTo(getParent().getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Constructor) {
            return ((Constructor) o).getParent().equals(getParent());
        }
        return false;
    }

    /**
     * renaming of constructor is impossible.
     *
     * @param newName is not used.
     */
    @Override
    public void rename(String newName) {
        // renaming of constructor is impossible
    }

    /**
     *
     * @return Abbreviation of the access modifier, the name of constructor and
     * type of parameter(s).
     */
    @Override
    public String getNameParamTypesAndReturnType() {
        return getAccess().getAbbreviation() + " " + getName() + paramList(false);
    }

    private String searchParamName(Relation r) {
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                return p.getName();
            }
        }
        return null;
    }

    private List<String> searchParams(Relation r) {
        List<String> pl = new ArrayList<>();
        for (Param p : getParams()) {
            if (p.getRelation().equals(r)) {
                pl.add(p.getName());
            }
        }
        return pl;
    }

    @Override
    public IndentedList getCode(Language l) {
        IndentedList list = new IndentedList();
        // single inheritance only
        Iterator<ObjectType> supers = ((ObjectType) getParent()).supertypes();
        // inheritance, call super constructor
        if (supers.hasNext()) {
            ObjectType superOt = supers.next();
            Constructor superC = superOt.getCodeClass().getConstructor();
            List<Param> superParams = superC.getParams();
            List<String> superParamNames = new ArrayList<>();
            for (Param p : superParams) {
                String search = searchParamName(p.getRelation());
                superParamNames.add(search);
            }
            list.addLinesAtCurrentIndentation(l.constructorHeaderAndSuper(this, superParamNames));
            // no inheritance, call a normal constructor with all values
        } else {
            list.addLinesAtCurrentIndentation(l.operationHeader(this));
        }
        // the remaining params are assigned with this.
        Iterator<Field> fields = getCodeClass().getFields();
        while (fields.hasNext()) {
            Field f = fields.next();
            Relation r = f.getRelation();
            if (r == null) {
                if (f.isAutoIncr()) {
                    list.addLineAtCurrentIndentation(l.assignment(l.thisKeyword() + l.memberOperator() + f.getName(), "1"));
                }

            } else if (r.hasNoDefaultValue()) {
                if (r.isMandatory()) {
                    if (r.targetType() instanceof ObjectType) {
                        ObjectType ot = (ObjectType) r.targetType();
                        if (getParent().equals(ot.getResponsible())) {
                            List<String> constructorParams = new ArrayList<>();
                            Constructor otConstructor = ot.getCodeClass().getConstructor();

                            // Iterator<Param> it = ot.getCodeClass().constructorParams();
                            for (Param otParam : otConstructor.getParams()) {
                                if (otParam.isAutoIncr()) {
                                    String fieldName = otParam.getRelation().getAutoIncrFieldName();
                                    constructorParams.add(l.autoIncr(fieldName));
                                } else {
                                    Param param = search(otParam);
                                    if (param == null) {
                                        constructorParams.add(l.thisKeyword());
                                    } else {
                                        constructorParams.add(param.getName());

                                    }
                                }
                            }

                            list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(f.getName(),
                                l.callConstructor(ot.getName(), constructorParams.toArray(new String[0]))));

                        } else {
                            list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), searchParamName(r)));
                        }

                    } else {
                        list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), searchParamName(r)));
                    }
                } else {// optional field
                    if (r.hasMultipleTarget()) {
                        list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), l.assignCollection(r.collectionType())));
                    } else {
                        if (r.targetType() instanceof BaseType) {
                            BaseType bt = (BaseType) r.targetType();
                            if (bt == BaseType.BOOLEAN) {
                                list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), "false"));
                            } else {
                                if (bt.getUndefinedString() == null) {
                                } else {
                                    list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), bt.getUndefinedString()));
                                }
                            }
                        }
                    }
                }
            } else { // default value
                if (!r.hasMultipleTarget()) {
//                    if (r.targetType().equals(BaseType.STRING)) {
//                        list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), l.stringSymbol() + r.getDefaultValue() + l.stringSymbol()));
//                    } else 
                    {
                        list.addLineAtCurrentIndentation(l.thisKeyword() + l.memberOperator() + l.assignment(r.fieldName(), l.defaultValue(r)));
                    }
                } else { // multiple default value
                    list.addLineAtCurrentIndentation(l.assignment(r.fieldName(), l.assignCollection(r.collectionType())));
                    if (r.isSeqRelation()) {
                        if (r.getMinFreq() > 0) {
                            IndentedList body = new IndentedList();
                            body.addLineAtCurrentIndentation(l.addCollection(l.thisKeyword() + l.memberOperator() + r.fieldName(), CollectionKind.LIST, l.defaultValue(r)));
                            list.addLinesAtCurrentIndentation(l.forLoop(0, r.getMinFreq(), body));
                        }
                    } else if (r.isSetRelation()) {
                        if (r.getMinFreq() > 0) {
                            IndentedList body = new IndentedList();
                            body.addLineAtCurrentIndentation(l.addCollection(r.fieldName(), CollectionKind.SET, l.defaultValue(r)));
                            list.addLinesAtCurrentIndentation(l.forLoop(0, r.getMinFreq(), body));
                        }
                    } else if (r.isMapRelation()) {
                        MapType mt = (MapType) r.collectionType();
                        if (mt.getKeyType() instanceof ConstrainedBaseType) {
                            ConstrainedBaseType cbt = (ConstrainedBaseType) mt.getKeyType();
                            Iterator<Range> ranges = cbt.getValueConstraint().ranges();
                            while (ranges.hasNext()) {
                                Range range = ranges.next();
                                BaseType bt = range.getLower().getType();
                                if (bt.equals(BaseType.INTEGER) || bt.equals(BaseType.NATURAL)) {
                                    int lower = Integer.parseInt(range.getLower().getName());
                                    int upper = Integer.parseInt(range.getUpper().getName());
                                    for (int i = lower; i <= upper; i++) {
                                        list.addLineAtCurrentIndentation(l.putStatement(l.thisKeyword() + l.memberOperator() + r.fieldName(), l.newInstance(cbt, String.valueOf(i)), l.defaultValue(r)) + l.endStatement());
                                    }
                                }
                            }
                            Iterator<BaseValue> baseValues = cbt.getValueConstraint().values();
                            while (baseValues.hasNext()) {
                                BaseValue bv = baseValues.next();
                                String key = bv.getName();
                                String value = l.defaultValue(r);
                                if (cbt.getBaseType().equals(BaseType.STRING)) {
                                    key = l.stringSymbol() + bv.getName() + l.stringSymbol();
                                }
                                if (bv.getType().equals(BaseType.STRING)) {
                                    value = l.stringSymbol() + l.defaultValue(r) + l.stringSymbol();
                                }
                                list.addLineAtCurrentIndentation(l.putStatement(l.thisKeyword() + l.memberOperator() + r.fieldName(), l.newInstance(cbt, key), value) + l.endStatement());
                            }
                        }
                    }
                }
            }

        }

        
        list.addLinesAtCurrentIndentation(l.bodyClosure());
        return list;
    }

    private Param search(Param otParam) {
        for (Param p : getParams()) {
            if (p.getRelation().equals(otParam.getRelation())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Set<ImportType> getImports() {
        return new HashSet();
    }

    @Override
    public String callString() {
        return "new " + getName() + paramCallList();
    }

    @Override
    public void initSpec() {
        ObjectType ot = (ObjectType) getParent();

        IBooleanOperation isCorrectValue = (IBooleanOperation) getCodeClass().getOperation("isCorrectValue");
        if (isCorrectValue != null) {
            setPreSpec(new BooleanCall(isCorrectValue, false));
        }

        Map<String, String> propMapping = new HashMap<>();
        Set<Property> properties = ot.getCodeClass().publicProperties();
        for (Property property : properties) {
            if (!property.isDerivable()) {
                String key;
                key = property.callString();

                List<Param> paramsRelation = getParams(property.relation);
                if (!paramsRelation.isEmpty()) {
                    if (property.relation.targetType() instanceof ObjectType) {
                        ObjectType target = (ObjectType) property.relation.targetType();
                        ObjectType responsible = target.getResponsible();
                        if (responsible != null && responsible.equals(ot)) {
                            propMapping.put(key, target.getCodeClass().getConstructor().callString(paramsRelation));
                        } else {
                            if (property.relation.hasMultipleTarget()) {
                                propMapping.put(key, paramsRelation.get(0).getName() + "(i)");
                            } else {
                                propMapping.put(key, paramsRelation.get(0).getName());
                            }
                        }
                    } else {
                        if (property.relation.hasMultipleTarget()) {
                            propMapping.put(key, paramsRelation.get(0).getName() + "(i)");
                        } else {
                            propMapping.put(key, paramsRelation.get(0).getName());
                        }
                    }
                } else if (property.relation.isMandatory()) {
                    if (!property.relation.hasNoDefaultValue()) {

                        propMapping.put(key, Language.JAVA.defaultValue(property.relation));
                    } else {
                        propMapping.put(key, Naming.withoutCapital(property.getReturnType().getSpec()));
                    }
                } else if (property.getReturnType().getType() instanceof CT) {
                    propMapping.put(property.callString(), "empty");
                } else {
                    propMapping.put(key, "?");
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(self()).append(" is created with properties ").append(propMapping.toString());

        for (Param param : getParams()) {
            Relation relation = param.getRelation();
            if (relation.isResponsible()) {
                Relation inverse = relation.inverse();
                if (inverse != null && inverse.isNavigable()) {
                    sb.append(" AND ").append(param.getName());
                    if (inverse.hasMultipleTarget()) {
                        sb.append(".contains(").append(self()).append(")");
                    } else {
                        sb.append(" knows ").append(self());
                    }
                }
            }
        }
        setPostSpec(new InformalPredicate(sb.toString()));

    }

    private List<Param> getParams(Relation relation) {
        List<Param> params = new ArrayList<>();
        for (Param param : getParams()) {
            if (param.getRelation().equals(relation)) {
                params.add(param);
            }
        }
        return params;
    }

    @Override
    public String callString(List<? extends ActualParam> actualParams) {
        if (actualParams.size() == getParams().size()) {
            return "new " + super.callString(actualParams);
        } else {
            return "new " + super.callString(getParams());
        }

    }

//    @Override
//    public boolean isUnspecified() {
//        return false;
//    }
//
//    @Override
//    public void setUnspecified(boolean abstrct) {
//        throw new UnsupportedOperationException("Constructor cannot be abstract.");
//    }
    public boolean adaptName(CodeClass codeClass) {
        return false;
    }

    @Override
    public boolean canTrigger(RoleEvent e) {
        return e.isNeededWhileUpdating();
    }

    @Override
    public boolean isFinal() {
        return false;
    }

}
