/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.code;

import static equa.code.CodeNames.*;
import static equa.code.ImportType.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import equa.code.operations.AccessModifier;
import equa.code.operations.ActualParam;
import equa.code.operations.BooleanCall;
import equa.code.operations.CT;
import equa.code.operations.Call;
import equa.code.operations.ChangeIdMethod;
import equa.code.operations.CollectionKind;
import equa.code.operations.Constructor;
import equa.code.operations.IFormalPredicate;
import equa.code.operations.IRelationalOperation;
import equa.code.operations.IndexOfMethod;
import equa.code.operations.IndexedProperty;
import equa.code.operations.IsRemovableMethod;
import equa.code.operations.MapType;
import equa.code.operations.Method;
import equa.code.operations.Operation;
import equa.code.operations.OperationWithParams;
import equa.code.operations.Operator;
import equa.code.operations.Param;
import equa.code.operations.Property;
import equa.code.operations.RegisterMethod;
import equa.code.operations.RemoveMethod;
import equa.code.operations.STorCT;
import equa.code.operations.SubParam;
import equa.meta.Message;
import equa.meta.classrelations.BooleanRelation;
import equa.meta.classrelations.Relation;
import equa.meta.objectmodel.BaseType;
import equa.meta.objectmodel.ConstrainedBaseType;
import equa.meta.objectmodel.RoleEvent;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectType;
import equa.util.Naming;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frankpeeters
 */
public class Java implements Language {

    private static final long serialVersionUID = 1L;
    private static Element imports;
    private static List<String> ACCESSMODIFIERS = Arrays.asList("public", "private", "protected");
    private static List<String> MODIFIERS = Arrays.asList("abstract", "synchronized", "final", "static", "native");

    static {
        try {
            imports = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(Language.class.getResourceAsStream("resources/imports/java.xml")).getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String accessModifier(AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "public";
            case NAMESPACE:
                return "";
            case PROTECTED:
                return "protected";
            case PRIVATE:
                return "private";
        }
        throw new RuntimeException("There is an accessModifier missing");
    }

    @Override
    public String addCollection(String name, CollectionKind kind, String add) {
        String result = name + memberOperator();
        switch (kind) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += "add(" + add + ")";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += "add(" + add + ")";
                break;
            case MAP:
                result += "error";
            case SET:
                result += "add(" + add + ")";
                break;
        }
        result += ";";
        return result;
    }

    private void addClass(String name, JarOutputStream jar, String dir, String loc) throws IOException {
        jar.putNextEntry(new JarEntry(dir + name + ".class"));
        InputStream is = new FileInputStream(new File(loc + "/" + dir + name + ".class"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            out.write(ch);
        }
        jar.write(out.toByteArray());
        is.close();
        out.close();
        jar.closeEntry();
    }

    @Override
    public String and() {
        return " && ";
    }

    @Override
    public String assignCollection(STorCT type) {
        String assignment = "new ";
        CT ct = (CT) type;
        switch (ct.getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                throw new RuntimeException("undefined");
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                assignment += "ArrayList<>()";
                break;
            case MAP:
                assignment += "HashMap<>()";
                break;
            case SET:
                STorCT t = ct;
                while (t instanceof CT) {
                    t = ((CT) t).getType();
                }
                if (t instanceof ObjectType) {
                    if (((ObjectType) t).isComparable()) {
                        assignment += "TreeSet<>()";
                    } else {
                        assignment += "HashSet<>()";
                    }
                } else {
                    assignment += "TreeSet<>()";
                }
                break;
        }
        return assignment;
    }

    @Override
    public String assignment(String variable, String expression) {
        return variable + " = " + expression + ";";
    }

    @Override
    public IndentedList bodyClosure() {
        IndentedList list = new IndentedList();
        list.addLine("}", false);
        return list;
    }

    @Override
    public String bodyStart() {
        return "{";
    }

    @Override
    public String callConstructor(String otName, String... params) {
        StringBuilder result = new StringBuilder();
        result.append("new ").append(otName).append("(");
        for (int i = 0; i < params.length; i++) {
            result.append(params[i]);
            if (i + 1 < params.length) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String callMethod(String object, String name, List<? extends ActualParam> params) {
        StringBuilder result = new StringBuilder();
        if (!object.isEmpty()) {
            result.append(object).append(memberOperator());
        }
        result.append(name).append("(");
        for (int i = 0; i < params.size(); i++) {
            ActualParam p = params.get(i);
            if (p instanceof Call) {
                result.append(p.expressIn(this));
            } else if (p instanceof SubParam) {
                List<String> strings = new ArrayList<>();
                // strings.add(p.getName());
                while (p instanceof SubParam) {
                    SubParam sb = (SubParam) p;
                    strings.add(sb.getShortName());
                    p = sb.getParentParam();
                }
                if (strings.size() >= 1) {
                    result.append(strings.get(strings.size() - 1));
                    for (int j = strings.size() - 2; j >= 0; j--) {
                        result.append(memberOperator());
                        result.append(getProperty(strings.get(j)));
                    }
                }
            } else {
                result.append(p.expressIn(this));
            }
            if (i + 1 < params.size()) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String callMethod(String object, String name, String... params) {
        StringBuilder result = new StringBuilder();
        if (!object.isEmpty()) {
            result.append(object).append(memberOperator());
        }
        result.append(name).append("(");
        for (int i = 0; i < params.length; i++) {
            result.append(params[i]);
            if (i + 1 < params.length) {
                result.append(", ");
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public String cast(STorCT type, String newName, String oldName) {
        return type(type) + " " + newName + " = " + "(" + type(type) + ") " + oldName + ";";
    }

    @Override
    public String checkType(String name, STorCT type) {
        return name + " instanceof " + type(type);
    }

    @Override
    public String classClosure() {
        return "}";
    }

    @Override
    public IndentedList classHeader(AccessModifier accessModifier, ObjectType ot, boolean template, boolean withOrm) {
        StringBuilder result = new StringBuilder();
        result.append(accessModifier(accessModifier));
        if (ot.isAbstract()) {
            result.append(" abstract");
        }
        if (!template && ot.getCodeClass().isFinal()) {
            result.append(" final");
        }
        result.append(" class ");
        result.append(ot.getName());
        if (template) {
            result.append(TEMPLATE);
            result.append(" extends ").append(ot.getName());
        } else if (ot.supertypes().hasNext()) {
            result.append(" extends ").append(ot.supertypes().next().getName());
        }
        result.append(" ").append(bodyStart());
        IndentedList list = new IndentedList();
        if (withOrm) {
            list.addLineAtCurrentIndentation("@Entity");
        }
        list.addLine(result.toString(), true);
        return list;
    }

    @Override
    public String clear(Relation r) {
        String result = r.fieldName() + memberOperator();
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += "clear();";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += "clear();";
                break;
            case MAP:
                result += "clear();";
                break;
            case SET:
                result += "clear();";
                break;
        }
        return result;
    }

    @Override
    public String concatenate(String string1, String string2) {
        return string1 + " + " + string2;
    }

    @Override
    public IndentedList constructorHeaderAndSuper(Constructor c, List<String> superParams) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(operationHeader(c));
        list.addLineAtCurrentIndentation("super(");
        for (int i = 0; i < superParams.size(); i++) {
            list.addString(superParams.get(i));
            if (i + 1 < superParams.size()) {
                list.addString(", ");
            }
        }
        list.addString(");");
        return list;
    }

    @Override
    public String createInstance(STorCT type, String name, String otName, String... params) {
        StringBuilder result = new StringBuilder();
        result.append(type(type)).append(" ").append(name).append(" = ");
        result.append(callConstructor(otName, params));
        result.append(";");
        return result.toString();
    }

    @Override
    public String declarationAndAssignment(STorCT type, String variable, String expression) {
        return type(type) + " " + variable + " = " + expression + ";";
    }

    private static void deleteFiles(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFiles(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    @Override
    public String endStatement() {
        return ";";
    }

    @Override
    public String equalsStatement(String string1, String string2) {
        return "Objects.equals(" + string1 + ", " + string2 + ")";
    }

    @Override
    public IndentedList field(Field f, boolean withOrm) {
        StringBuilder sb = new StringBuilder();
        sb.append(accessModifier(f.getAccessModifier()));
        sb.append((f.isFinal() ? " final" : ""));
        sb.append((f.isClassField() ? " static " : " "));
        sb.append(type(f.getType()));
        sb.append(" ");
        sb.append(f.getName());
        sb.append(endStatement());
        IndentedList list = new IndentedList();
        list.addLineAtCurrentIndentation(sb.toString());
        return list;
    }

    @Override
    public IndentedList forEachLoop(STorCT type, String name, String collection, IndentedList body) {
        IndentedList list = new IndentedList();
        list.addLine("for (" + type(type) + " " + name + " : " + collection + ") " + bodyStart(), true);
        list.addLinesAtCurrentIndentation(body);
        list.addLinesAtCurrentIndentation(bodyClosure());
        return list;
    }

    @Override
    public IndentedList forLoop(int lower, int exit, IndentedList body) {
        IndentedList list = new IndentedList();
        list.addLine("for (int i=" + lower + "; i<" + exit + "; i++) " + bodyStart(), true);
        list.addLinesAtCurrentIndentation(body);
        list.addLinesAtCurrentIndentation(bodyClosure());
        return list;
    }

    @Override
    public boolean generate(ObjectModel om, boolean edit, boolean orm, boolean mInh, String loc) throws FileNotFoundException {
        om.getProject().setLastUsedLanguage(this);
        List<Message> messages = om.generateClasses(true, false);
        for (Message message : messages) {
            if (message.isError()) {
                return false;
            }
        }
        String root = om.getProject().getNameSpace();
        root = root.replaceAll(" ", "_");
        File dir = new File(loc + "/" + root + "/" + DOMAIN + "/");
        dir.mkdirs();
        deleteFiles(dir);

        for (FactType ft : om.types()) {
            if (ft.isClass()) {
                ObjectType ot = ft.getObjectType();
                CodeClass cc = ot.getCodeClass();
                File file = new File(loc + "/" + cc.getDirectory() + ot.getName() + ".java");
                try (PrintStream ps = new PrintStream(file)) {
                    String code = cc.getCode(this, orm);
                    ps.append(code);
                }
            }
        }

        if (edit) {
            generateTemplates(om, loc);
        }

        CodeClass cc = om.getCodeClass();
        dir = new File(loc + "/" + cc.getDirectory());
        dir.mkdirs();
        File file = new File(loc + "/" + cc.getDirectory() + SYSTEM_CLASS + ".java");
        try (PrintStream ps = new PrintStream(file)) {
            ps.append(cc.getCode(this, orm));
        }

        if (!edit) {

            try {
                generateLib(om, orm, loc);
            } catch (IOException ex) {
                Logger.getLogger(Java.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return true;
    }

    private void generateTemplates(ObjectModel om, String loc) throws FileNotFoundException {
        for (FactType ft : om.types()) {
            if (ft.getObjectType() != null) {
                CodeClass cc = ft.getObjectType().getCodeClass();
                if (cc.hasEditableOperation()) {
                    File file = new File(loc + "/" + cc.getDirectory() + ft.getObjectType().getName() + TEMPLATE + ".java");
                    PrintStream ps = new PrintStream(file);
                    ps.append(cc.getCodeForEditableOperations(this));
                    ps.close();
                }
            }
        }
    }

    private boolean generateLib(ObjectModel om, boolean orm, String loc) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector, null, null);
        List<JavaFileObject> list = getJavaFileContentsAsString(om, orm, loc);
        File dir = new File(loc + "/gen");
        dir.mkdir();
        CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector,
            Arrays.asList(new String[]{"-d", loc + "/gen"}), null, list);
        Boolean result = task.call();
        List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticsCollector.getDiagnostics();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            System.out.println(d.getLineNumber());
            System.out.println(d.getMessage(null));
        }
        if (result) {
            System.out.println("Compilation has succeeded");
        } else {
            System.out.println("Compilation fails.");
        }
        jarIt(om, loc);
        return result;
    }

    private List<JavaFileObject> getJavaFileContentsAsString(ObjectModel om, boolean orm, String loc) {
        List<JavaFileObject> list = new LinkedList<>();
        for (FactType ft : om.types()) {
            if (ft.isClass()) {
                ObjectType ot = ft.getObjectType();
                String code = ot.getCodeClass().getCode(this, orm);
                list.add(new JavaObjectFromString(loc + "/" + ot.getCodeClass().getDirectory() + ot.getName() + ".java", code));
            }
        }
        String code = om.getCodeClass().getCode(this, orm);
        list.add(new JavaObjectFromString(loc + "/" + om.getCodeClass().getDirectory() + SYSTEM_CLASS + ".java", code));
        return list;
    }

    @Override
    public String getProperty(String name) {
        return "get" + Naming.withCapital(name) + "()";
    }

    @Override
    public String hashCodeStatement(String variable) {
        return "Objects.hashCode(" + variable + ")";
    }

    @Override
    public IndentedList ifStatement(String condition, IndentedList trueStatement) {
        IndentedList list = new IndentedList();
        list.addLine("if (" + condition + ") " + bodyStart(), true);
        list.addLinesAtCurrentIndentation(trueStatement);
        list.addLinesAtCurrentIndentation(bodyClosure());
        return list;
    }

    @Override
    public IndentedList ifStatement(String condition, IndentedList trueStatement, IndentedList falseStatement) {
        IndentedList list = new IndentedList();
        list.addLine("if (" + condition + ") " + bodyStart(), true);
        list.addLinesAtCurrentIndentation(trueStatement);
        list.addLine(bodyClosure() + " else " + bodyStart(), false, true);
        list.addLinesAtCurrentIndentation(falseStatement);
        list.addLinesAtCurrentIndentation(bodyClosure());
        return list;
    }

    @Override
    public List<ImportType> imports(Relation r) {
        List<ImportType> list = new LinkedList<>();
        if (r.isCollectionReturnType() || r.isMapRelation()) {
            switch (r.collectionType().getKind()) {
                case COLL:
                    list.add(Collection);
                    break;
                case ITERATOR:
                    list.add(Iterator);
                    break;
                case LIST:
                    list.add(ImportType.List);
                    list.add(ArrayList);
                    break;
                case MAP:
                    list.add(Map);
                    list.add(HashMap);
                    break;
                case SET:
                    list.add(ImportType.Set);
                    if (r.targetType() instanceof ObjectType) {
                        if (((ObjectType) r.targetType()).isComparable()) {
                            list.add(SortedSet);
                        } else {
                            list.add(HashSet);
                        }
                    } else {
                        list.add(SortedSet);
                    }
                    break;
                case ARRAY:
                    break;
            }
        }
        return list;
    }

    private IndentedList imports(Set<ImportType> imports) {
        IndentedList list = new IndentedList();
        for (ImportType it : imports) {
            try {

                list.addLineAtCurrentIndentation("import " + Java.imports.getElementsByTagName(it.toString()).item(0).getTextContent() + ";");

            } catch (NullPointerException ex) {
                // there is no import for this language, no problem.
            }
        }
        return list;
    }

    private IndentedList importsManuallyAddedCode(Set<String> imports) {
        IndentedList list = new IndentedList();
        if (imports == null) {
            imports = new HashSet<>();
        }
        for (String imp : imports) {
            list.addLineAtCurrentIndentation("import " + imp + ";");
        }
        return list;
    }

    @Override
    public IndentedList nameSpaceAndImports(NameSpace nameSpace, Set<ImportType> imports,
        Set<String> importsManuallyAddedCode) {
        IndentedList list = new IndentedList();
        list.addLinesAtCurrentIndentation(nameSpaceStart(nameSpace));
        list.addLineAtCurrentIndentation("");
        list.addLinesAtCurrentIndentation(imports(imports));
        list.addLinesAtCurrentIndentation(importsManuallyAddedCode(importsManuallyAddedCode));
        return list;
    }

    @Override
    public String importUtilities(NameSpace namespace) {
        return "import " + namespace.toString() + ".*;";
    }

    @Override
    public String indexOf(Relation r, String param) {
        return r.fieldName() + memberOperator() + "indexOf(" + param + ")";
    }

    private void jarIt(ObjectModel om, String loc) throws IOException {
        JarOutputStream jar = new JarOutputStream(new FileOutputStream(loc + "/" + om.getProject().getName() + ".jar"));
        for (FactType ft : om.types()) {
            if (ft.isClass()) {
                ObjectType ot = ft.getObjectType();
                addClass(ot.getName(), jar, ot.getCodeClass().getDirectory(), loc);
            }
        }
        addClass(SYSTEM_CLASS, jar, om.getCodeClass().getDirectory(), loc);
        jar.close();
        deleteFiles(new File(loc + "/gen"));
    }

    @Override
    public String memberOperator() {
        return ".";
    }

    @Override
    public IndentedList nameSpaceEnd() {
        return new IndentedList();
    }

    @Override
    public IndentedList nameSpaceStart(NameSpace nameSpace) {
        IndentedList list = new IndentedList();
        StringBuilder result = new StringBuilder("package ");
        do {
            result.append(nameSpace.getName().toLowerCase());
            if (nameSpace.hasSub()) {
                result.append(".");
            }
            nameSpace = nameSpace.getSubNameSpace();
        } while (nameSpace != null);
        result.append(";");
        list.addLineAtCurrentIndentation(result.toString());
        return list;
    }

    @Override
    public String negate(String statement) {
        return "!(" + statement + ")";
    }

    @Override
    public String newInstance(STorCT type, String... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(type(type));
        sb.append("(");
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i + 1 < params.length) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String nonObjectKeyword() {
        return "object";
    }

    @Override
    public IndentedList operationHeader(Operation o) {
        StringBuilder result = new StringBuilder();
        IndentedList list = new IndentedList();
        addAPI(o, list);
        // access modifier
        AccessModifier access = o.getAccess();
        if (access == AccessModifier.PRIVATE) {
            ObjectType ot = (ObjectType) o.getParent();
            if (ot.getCodeClass().hasEditableOperation()) {
                access = AccessModifier.NAMESPACE;
            }
        }
        result.append(accessModifier(access));
        // if static add static
        if (o.isClassMethod()) {
            result.append(" static");
        }
        if (o.isFinal()) {
            result.append(" final");
        }
        // if it is a method we have a return type
        if (o instanceof Method) {
            Method m = (Method) o;
            result.append(" ").append(type(m.getReturnType().getType()));
        }
        // add the name
        result.append(" ").append(o.getName());
        // add parameters
        // if (o instanceof CountProperty) {
        // result.append("()");
        // } else
        if (o instanceof OperationWithParams) {
            OperationWithParams owp = (OperationWithParams) o;
            Iterator<Param> params = owp.getParams().iterator();
            result.append("(");
            while (params.hasNext()) {
                Param p = params.next();
                result.append(type(p.getType())).append(" ").append(p.getName());
                if (params.hasNext()) {
                    result.append(", ");
                }
            }
            result.append(")");
        }
        Iterator<equa.code.operations.Exception> exceptions = o.getExceptions();
        if (exceptions.hasNext()) {
            result.append(" throws");
        }
        while (exceptions.hasNext()) {
            equa.code.operations.Exception e = exceptions.next();
            result.append(" ").append(e.getName());
            if (exceptions.hasNext()) {
                result.append(",");
            }
        }
        if (o instanceof Method) {
            Method m = (Method) o;
            if (m.isOverrideMethod()) {
                list.addLineAtCurrentIndentation("@Override");
            }
        }
        list.addLine(result.append(" ").append(bodyStart()).toString(), true);
        if (o.getPreSpec() != null) {
            String condition = formalPredicateCondition(o, o.getPreSpec(), true);
            IndentedList ifTrue = new IndentedList();
            ifTrue.addLineAtCurrentIndentation(throwIllegalArgumentException("The PRE condition has been violated."));
            list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
        }
        if (o.getEscape() != null) {
            String condition = formalPredicateCondition(o, o.getEscape().getCondition(), false);
            IndentedList ifTrue = new IndentedList();
            if (o instanceof IRelationalOperation) {
                IRelationalOperation m = (IRelationalOperation) o;
                if (m instanceof RemoveMethod) {
                    //m.getParams().get(0).expressIn(this)
                    ifTrue.addLineAtCurrentIndentation(returnStatement(callMethod(m.getRelation().name(), IsRemovableMethod.NAME)));
                } else if (m instanceof ChangeIdMethod) {
                    ifTrue.addLineAtCurrentIndentation(returnStatement("false"));
                } else if (m instanceof Method && ((Method) m).getReturnType().getType() instanceof ObjectType) {
                    ifTrue.addLineAtCurrentIndentation(returnStatement("null"));
                } else {
                    ifTrue.addLineAtCurrentIndentation(returnStatement(""));
                }
            }
            list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
        }
        return list;
    }

    private void addAPI(Operation o, IndentedList list) {
        // documentation
        String[] commentLines = o.getSpec().split(System.getProperty("line.separator"));
        if (commentLines.length != 0) {
            list.addLineAtCurrentIndentation(docStart());
            for (String s : commentLines) {
                list.addLineAtCurrentIndentation(docLine(s));
            }
            list.addLineAtCurrentIndentation(docEnd());
        }
    }

    public void addAPI(String[] commentLines, IndentedList list) {
        // documentation
        if (commentLines.length != 0) {
            list.addLineAtCurrentIndentation(docStart());
            for (String s : commentLines) {
                list.addLineAtCurrentIndentation(docLine(s));
            }
            list.addLineAtCurrentIndentation(docEnd());
        }
    }

    private String throwIllegalArgumentException(String msg) {
        return "throw new IllegalArgumentException(" + stringSymbol() + msg + stringSymbol() + ");";
    }

    private String formalPredicateCondition(Operation o, IFormalPredicate predicate, boolean pre) {
        Iterator<BooleanCall> operands = predicate.operands();
        StringBuilder fullCondition = new StringBuilder();
        while (operands.hasNext()) {
            BooleanCall bc = operands.next();
            if (pre) {
                String condition = bc.expressIn(this);
                if (!bc.isNegated()) {
                    condition = negate(condition);
                }
                fullCondition.append(condition);
            } else {
                fullCondition.append(bc.expressIn(this));
            }
            if (operands.hasNext()) {
                if (pre) {
                    fullCondition.append(and());
                } else {
                    fullCondition.append(or());
                }
            }
        }
        return fullCondition.toString();
    }

    @Override
    public String operator(Operator operator) {
        switch (operator) {
            case MINUS:
                return " - ";
            case PLUS:
                return " + ";
            case SMALLER:
                return " < ";
            case SMALLER_OR_EQUAL:
                return " <= ";
            case GREATER_OR_EQUAL:
                return " >= ";
        }
        throw new IllegalStateException("Operator unknown");
    }

    @Override
    public String or() {
        return " || ";
    }

    @Override
    public IndentedList propertyCode(Property p) {
        IndentedList list = new IndentedList();
        if (p.isGetter()) {
            String[] commentLines = p.getSpec().split(System.getProperty("line.separator"));
            if (commentLines.length != 0) {
                list.addLineAtCurrentIndentation(docStart());
                for (String s : commentLines) {
                    list.addLineAtCurrentIndentation(docLine(s));
                }
                list.addLineAtCurrentIndentation(docEnd());
            }
            String prefix = " get";
            if (p.getReturnType().getType().equals(BaseType.BOOLEAN)) {
                prefix = " is";
            }
            list.addLine(accessModifier(p.getAccessGetter()) + " " + (p.isFinal() ? "final " : "") + type(p.getReturnType().getType()) + prefix
                + Naming.withCapital(p.getName()) + "(", true);
            if (p instanceof IndexedProperty) {
                IndexedProperty ip = (IndexedProperty) p;
                List<Param> params = ip.getterParams();
                String separator = "";
                for (Param param : params) {
                    list.addString(separator + type(param.getType()) + " ");
                    list.addString(param.expressIn(this));
                    separator = ", ";
                }
            }
            list.addString(") " + bodyStart());

            if (p.getPreSpec() != null) {
                String condition = formalPredicateCondition(p, p.getPreSpec(), true);
                IndentedList ifTrue = new IndentedList();
                ifTrue.addLineAtCurrentIndentation(throwIllegalArgumentException("The PRE condition has been violated."));
                list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
            }
            if (p.getEscape() != null) {
                String condition = formalPredicateCondition(p, p.getEscape().getCondition(), false);
                IndentedList ifTrue = new IndentedList();
                ifTrue.addLineAtCurrentIndentation(returnStatement(p.getRelation().targetType().getUndefinedString()));
                list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
            }

            if (p.getReturnType().getType() instanceof CT) {
                list.addLineAtCurrentIndentation(returnStatement(unmodifiable(p.getRelation().collectionType(), p.getRelation().fieldName())));
            } else {
                Relation responsibleRelation = p.getRelation().getOwner().getResponsibleRelation();
                if (responsibleRelation != null && p.isDerivable() && responsibleRelation.isSeqRelation()) {
                    // numbering of elements starts with 1, that's why we increment the returned index
                    list.addLineAtCurrentIndentation(returnStatement(callMethod(responsibleRelation.inverse().fieldName(),
                        responsibleRelation.getOperationName(IndexOfMethod.NAME_PREFIX), thisKeyword())
                    ));
                } else if (p instanceof IndexedProperty) {
                    list.addLineAtCurrentIndentation(returnStatement(p.getRelation().fieldName() + memberOperator()
                        + get(((IndexedProperty) p).getterParams())));
                } else {
                    list.addLineAtCurrentIndentation(returnStatement(p.getRelation().fieldName()));
                }
            }
            //list.addLinesAtCurrentIndentation(postProcessing(p));
            list.addLinesAtCurrentIndentation(bodyClosure());
        }

        if (p.isSetter()) {
            if (p.isGetter()) {
                list.addLineAtCurrentIndentation("");
            }
            String[] commentLines = p.getSetter().getSpec().split(System.getProperty("line.separator"));
            if (commentLines.length != 0) {
                list.addLineAtCurrentIndentation(docStart());
                for (String s : commentLines) {
                    list.addLineAtCurrentIndentation(docLine(s));
                }
                list.addLineAtCurrentIndentation(docEnd());
            }

            list.addLine(
                accessModifier(p.getAccessSetter()) + " " + (p.isFinal() ? "final " : "") + type(null) + " set"
                + Naming.withCapital(p.getName()) + "(", true);
            String separator = "";
            if (p instanceof IndexedProperty) {
                IndexedProperty ip = (IndexedProperty) p;
                List<Param> params = ip.getQualifiers();
                for (int i = 0; i < params.size(); i++) {
                    list.addString(separator + type(params.get(i).getType()) + " " + params.get(i).getName());
                    separator = ", ";
                }
            }

            Relation inverse = p.getRelation().inverse();

            if (p.getRelation().isCreational()) {

                // create a new object from the params.
                List<String> constructorParams = new ArrayList<>();
                ObjectType target = (ObjectType) p.getRelation().targetType();
                Iterator<Param> it = target.getCodeClass().constructorParams();
                while (it.hasNext()) {
                    Param param = it.next();
                    String name;
                    if (param.getType().equals(p.getParent())) {
                        name = thisKeyword();
                    } else {
                        if (param.getRelation().isAutoIncr()) {
                            name = autoIncr(param.getRelation().fieldName());
                        } else {
                            name = param.getName();
                        }

                        list.addString(separator + param.getType().getName() + " " + param.getName());
                        separator = ", ";
                    }
                    constructorParams.add(name);

                }

                list.addString(") " + bodyStart());

                if (p.getSetter().getPreSpec() != null) {
                    String condition = formalPredicateCondition(p, p.getSetter().getPreSpec(), true);
                    IndentedList ifTrue = new IndentedList();
                    ifTrue.addLineAtCurrentIndentation(throwIllegalArgumentException("The PRE condition has been violated."));
                    list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
                }
                if (p.getSetter().getEscape() != null) {
                    String condition = formalPredicateCondition(p, p.getSetter().getEscape().getCondition(), false);
                    IndentedList ifTrue = new IndentedList();
                    ifTrue.addLineAtCurrentIndentation(returnStatement(""));
                    list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
                }

                list.addLineAtCurrentIndentation(createInstance(target, TEMP1, target.getName(), constructorParams.toArray(new String[0])));

                // setting of the field
                if (p instanceof IndexedProperty) {
                    list.addLineAtCurrentIndentation(p.getRelation().fieldName()
                        + memberOperator() + setAtStatement(((IndexedProperty) p).setterParams(), TEMP1));
                } else {
                    list.addLineAtCurrentIndentation(assignment(thisKeyword() + memberOperator() + p.getRelation().fieldName(), TEMP1));
                }

                // optional field of primitive type needs a setting of the concerning defined-field:
                if (!p.getRelation().isMandatory() && p.getRelation().targetType().getUndefinedString() == null
                    && !p.getRelation().targetType().equals(BaseType.BOOLEAN)) {
                    list.addLineAtCurrentIndentation(assignment(p.getRelation().fieldName() + "Defined", "true"));
                }

            } else {//not creational 
                list.addString(separator + type(p.getReturnType().getType()) + " " + p.getName() + ") " + bodyStart());

                if (p.getSetter().getPreSpec() != null) {
                    String condition = formalPredicateCondition(p, p.getSetter().getPreSpec(), true);
                    IndentedList ifTrue = new IndentedList();
                    ifTrue.addLineAtCurrentIndentation(throwIllegalArgumentException("The PRE condition has been violated."));
                    list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
                }
                if (p.getSetter().getEscape() != null) {
                    String condition = formalPredicateCondition(p, p.getSetter().getEscape().getCondition(), false);
                    IndentedList ifTrue = new IndentedList();
                    ifTrue.addLineAtCurrentIndentation(returnStatement(""));
                    list.addLinesAtCurrentIndentation(ifStatement(condition, ifTrue));
                }

                if (inverse != null && inverse.isNavigable() && p.getAccessSetter().equals(AccessModifier.PUBLIC)) {

                    // removing old value at the inverse side
                    IndentedList trueStatement = new IndentedList();
                    String removable = "";
                    if (inverse.hasMultipleTarget()) {
                        removable = thisKeyword();
                    }

                    trueStatement.addLineAtCurrentIndentation(
                        removeExternalStatement(p.getName(), inverse, removable));
                    if (p.getRelation().isMandatory()) {
                        list.addLinesAtCurrentIndentation(trueStatement);
                    } else {
                        IndentedList ifStatement = ifStatement("this." + p.getName() + " != null", trueStatement);
                        list.addLinesAtCurrentIndentation(ifStatement);
                    }

                    // registering new value at the inverse side
                    if (inverse.isSeqRelation() || inverse.isSetRelation()) {
                        list.addLineAtCurrentIndentation(callMethod(p.getName(), inverse.getOperationName(RegisterMethod.NAME), thisKeyword()) + endStatement());
                    } else if (inverse.isMapRelation()) {
                        list.addLineAtCurrentIndentation(p.getName() + memberOperator() + setAtStatement(((IndexedProperty) p).setterParams(), p.getRelation().fieldName())
                        );
                    } else if (inverse instanceof BooleanRelation) {
                        list.addLineAtCurrentIndentation(p.getName() + memberOperator() + "set" + Naming.withCapital(inverse.name() + "(true)")
                            + endStatement());
                    } else {
                        list.addLineAtCurrentIndentation(p.getName() + memberOperator() + "set" + Naming.withCapital(inverse.name() + "(" + thisKeyword() + ")")
                            + endStatement());
                    }
                }

                // setting new value at field
                if (p instanceof IndexedProperty) {
                    list.addLineAtCurrentIndentation(p.getRelation().fieldName()
                        + memberOperator() + setAtStatement(((IndexedProperty) p).setterParams(), p.getName()));
                } else {
                    list.addLineAtCurrentIndentation(assignment(thisKeyword() + memberOperator() + p.getRelation().fieldName(), p.getName()));
                }

                // optional field of primitive type needs a setting of the concerning defined-field:
                if (!p.getRelation().isMandatory() && p.getRelation().targetType().getUndefinedString() == null
                    && !p.getRelation().targetType().equals(BaseType.BOOLEAN)) {
                    list.addLineAtCurrentIndentation(assignment(p.getRelation().fieldName() + "Defined", "true"));
                }
            }
            list.addLinesAtCurrentIndentation(postProcessing(p));
            list.addLinesAtCurrentIndentation(bodyClosure());
        }
        return list;

    }

    String setAtStatement(List<Param> params, String value) {
        StringBuilder sb = new StringBuilder("set(");
        for (int i = 0; i < params.size() - 1; i++) {
            if (params.get(i).equals(BaseType.NATURAL)) {
                sb.append(params.get(i).expressIn(this));
                sb.append(" - 1");
            } else {
                sb.append(params.get(i).expressIn(this));
            }

            sb.append(", ");

        }
        sb.append(value);
        sb.append(");");
        return sb.toString();
    }

    @Override
    public String removeStatement(Relation r, String removable) {
        String result = thisKeyword() + memberOperator() + r.fieldName() + memberOperator();
        String methodName = "remove";
        if (removable.isEmpty()) {
            Relation inverse = r.inverse();
            methodName += Naming.withCapital(inverse.name());
        }
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("undefined");
            case COLL:
                result += methodName + "(" + removable + ")";
                break;
            case ITERATOR:
                throw new RuntimeException("undefined");
            case LIST:
                result += methodName + "(" + removable + ")";
                break;
            case MAP:
                result += methodName + "(" + removable + ")";
                break;
            case SET:
                result += methodName + "(" + removable + ")";
                break;
        }
        return result + ";";
    }

    public String removeExternalStatement(String caller, Relation r, String removable) {
        if (r instanceof BooleanRelation) {
            String result = thisKeyword() + memberOperator() + caller + memberOperator();
            String methodName = "set" + Naming.withCapital(r.name());
            return result += methodName + "(false);";
        } else {
            String result = thisKeyword() + memberOperator() + caller + memberOperator();
            String methodName = "remove" + Naming.withCapital(r.name());
            return result += methodName + "(" + removable + ");";
        }
    }

    @Override
    public String removeAtStatement(String name, String index
    ) {
        return name + ".remove(" + index + " - 1);";
    }

    @Override
    public String returnStatement(String statement
    ) {
        if (statement.isEmpty()) {
            return "return;";
        } else {
            return "return " + statement + ";";
        }
    }

    @Override
    public String size(CollectionKind ck
    ) {
        switch (ck) {
            case ARRAY:
                return "length";
            case COLL:
                return "size()";
            case ITERATOR:
                throw new RuntimeException("Iterator does not support size.");
            case LIST:
                return "size()";
            case MAP:
                return "size()";
            case SET:
                return "size()";
        }
        throw new RuntimeException("There is a CollectionKind missing here.");
    }

    @Override
    public String stringSymbol() {
        return "\"";
    }

    @Override
    public String subseq(Relation r, String i, String j
    ) {
        StringBuilder result = new StringBuilder();
        result.append(r.fieldName() + memberOperator());
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new RuntimeException("Undefined");
            case COLL:
                throw new RuntimeException("Undefined");
            case ITERATOR:
                throw new RuntimeException("Undefined");
            case LIST:
                result.append("subList(" + i + ", " + j + ")");
                break;
            case MAP:
                throw new RuntimeException("Undefined");
            case SET:
                throw new RuntimeException("Undefined");
        }
        return result.toString();
    }

    @Override
    public IndentedList systemClassHeader() {
        IndentedList list = new IndentedList();
        list.addLine(accessModifier(AccessModifier.PUBLIC) + " final class " + SYSTEM_CLASS + " " + bodyStart(), true);
        return list;
    }

    @Override
    public String thisKeyword() {
        return "this";
    }

    @Override
    public String type(STorCT type
    ) {
        return type(type, "", false);
    }

    private String type(STorCT type, String result, boolean isCT) {
        if (type == null) {
            result += "void";
        } else if (type instanceof ObjectType) {
            result += type.getName();
        } else if (type instanceof CT) {
            if (type instanceof MapType) {
                MapType hmt = (MapType) type;
                result += "Map<";
                result = type(hmt.getKeyType(), result, true);
                result += ", ";
                result = type(hmt.getValueType(), result, true);
                result += ">";
            } else {
                CT ct = (CT) type;
                switch (ct.getKind()) {
                    case LIST:
                        result += "List<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case SET:
                        result += "Set<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case ARRAY:
                        result = type(ct.getType(), result, false);
                        result += "[]";
                        break;
                    case ITERATOR:
                        result += "Iterator<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                    case MAP:
                        throw new RuntimeException("This should be MapType, not CollectionType");
                    case COLL:
                        result += "Collection<";
                        result = type(ct.getType(), result, true);
                        result += ">";
                        break;
                }
            }
        } else if (type instanceof BaseType) {
            BaseType bt = (BaseType) type;
            switch (bt.getName()) {
                case "String":
                    result += "String";
                    break;
                case "Integer":
                    result += isCT ? "Integer" : "int";
                    break;
                case "Natural":
                    result += isCT ? "Integer" : "int";
                    break;
                case "Real":
                    result += isCT ? "Double" : "double";
                    break;
                case "Character":
                    result += isCT ? "Character" : "char";
                    break;
                case "Boolean":
                    result += isCT ? "Boolean" : "boolean";
                    break;
                case "Object":
                    result += "Object";
                    break;
            }
        }
        return result;
    }

    @Override
    public String unmodifiable(CT ct, String statement) {
        String result = "";
        switch (ct.getKind()) {
            case ARRAY:
                throw new RuntimeException("Undefined");
            case COLL:
                result = "Collections" + memberOperator() + "unmodifiable";
                result += "Collection(" + statement + ")";
            case ITERATOR:
                throw new RuntimeException("Undefined");
            case LIST:
                result = "Collections" + memberOperator() + "unmodifiable";
                result += "List(" + statement + ")";
                break;
            case MAP:
                result = "Collections" + memberOperator() + "unmodifiable";
                result += "Map(" + statement + ")";
            case SET:
                result += "new ArrayList<" + type(ct.getType(), "", true) + ">(" + statement + ")";
        }
        return result;
    }

    @Override
    public String throwUnsupportedOperationException(String msg) {
        return "throw new UnsupportedOperationException(" + stringSymbol() + msg + stringSymbol() + ");";
    }

    @Override
    public String setProperty(String object, String property, String parameter) {
        return new StringBuilder().append(object).append(memberOperator()).append("set").append(Naming.withCapital(property)).append("(")
            .append(parameter).append(");").toString();
    }

    @Override
    public String contains(Relation r, String value) {
        StringBuilder result = new StringBuilder();
        result.append(r.fieldName()).append(memberOperator());
        switch (r.collectionType().getKind()) {
            case ARRAY:
                throw new IllegalStateException();
            case COLL:
                result.append("contains");
                break;
            case ITERATOR:
                throw new IllegalStateException();
            case LIST:
                result.append("contains");
                break;
            case MAP:
                result.append("containsKey");
                break;
            case SET:
                result.append("contains");
                break;
        }
        result.append("(").append(value).append(")");
        return result.toString();
    }

    @Override
    public String docEnd() {
        return " */";
    }

    @Override
    public String docLine(String line) {
        return " * " + line;
    }

    @Override
    public String docStart() {
        return "/**";
    }

    @Override
    public Map<OperationHeader, ImportedOperation> getOperations(String s, String className) {
        Map<OperationHeader, ImportedOperation> map = new HashMap<>();
        // We remove everything before the start of the class
        String remainingCodeToParse = s.substring(s.indexOf("{") + 1);
        // We remove the class closing
        remainingCodeToParse = remainingCodeToParse.substring(0, remainingCodeToParse.lastIndexOf("}"));

        while (remainingCodeToParse.contains("{")) {
            int openBrace = remainingCodeToParse.indexOf("{");
            int startBodyText = openBrace + 1;
            int closeBrace = remainingCodeToParse.indexOf("}");
            int nextOpenBrace = remainingCodeToParse.substring(startBodyText, closeBrace).indexOf("{");
            while (nextOpenBrace >= 0) {
                openBrace += 1 + nextOpenBrace;
                closeBrace += 1 + remainingCodeToParse.substring(closeBrace + 1).indexOf("}");
                nextOpenBrace = remainingCodeToParse.substring(openBrace + 1, closeBrace).indexOf("{");
            }

            IndentedList api = getDocumentation(remainingCodeToParse.substring(0, startBodyText - 1));
            OperationHeader oh = getOH(remainingCodeToParse.substring(0, startBodyText - 1), className);
            int startHeader = remainingCodeToParse.substring(0, openBrace).indexOf("*/");
            if (startHeader == -1) {
                startHeader = 0;
            } else {
                startHeader += 2;
            }
            String method = remainingCodeToParse.substring(startHeader, closeBrace + 1);

            method = skipStartingEmptyLinesAndOverride(method);

            map.put(oh, new ImportedOperation(oh, api, IndentedList.fromString(method, -1)));

            remainingCodeToParse = remainingCodeToParse.substring(closeBrace + 1);
        }

        return map;
    }

    private static String skipStartingEmptyLinesAndOverride(String text) {
        int start = 0;
        int endOfLine = text.indexOf(System.getProperty("line.separator"));
        int endOfLineLength = System.getProperty("line.separator").length();
        while (endOfLine != -1 && text.substring(start, start + endOfLine).trim().isEmpty()) {
            start += endOfLine + endOfLineLength;
            endOfLine = text.substring(start).indexOf(System.getProperty("line.separator"));
        }
        text = text.substring(start);
//        start = 0;
//        int tag = text.indexOf("@");
//        if (tag != -1) {
//            start += tag + 9; // length of '@Override' is 9
//        }
//        text = text.substring(start);
        start = 0;
        endOfLine = text.indexOf(System.getProperty("line.separator"));
        while (endOfLine != -1 && text.substring(start, start + endOfLine).trim().isEmpty()) {
            start += endOfLine + endOfLineLength;
            endOfLine = text.substring(start).indexOf(System.getProperty("line.separator"));
        }

        return text.substring(start).replace("@Override", "");
    }

    IndentedList getDocumentation(String sourcecode) {
        int start = sourcecode.indexOf(docStart());
        if (start == -1) {
            return new IndentedList();
        } else {
            int end = sourcecode.substring(start).indexOf(docEnd());
            if (end == -1) {
                return new IndentedList();
            } else {
                sourcecode = sourcecode.substring(start + 3).trim();
                start = sourcecode.indexOf("* ");
                IndentedList list = new IndentedList();
                list.addLineAtCurrentIndentation(docStart());
                while (start == 0) {
                    start = sourcecode.indexOf(System.getProperty("line.separator"));
                    list.addLineAtCurrentIndentation(docLine(sourcecode.substring(2, start)));
                    sourcecode = sourcecode.substring(start + 2).trim();
                    start = sourcecode.indexOf("* ");
                }
                list.addLineAtCurrentIndentation(docEnd());
                return list;
            }
        }

    }

    String skipComment(String method) {
        String methodCode = method.trim();
        int next = 0;
        do {
            methodCode = methodCode.substring(next);
            next = searchEndOfCommentLine(methodCode);
            if (next == -1) {
                next = searchEndOfComment(methodCode);
            }
        } while (next != -1);

        return methodCode;
    }

    int searchEndOfCommentLine(String text) {
        if (text.trim().startsWith("//")) {
            int next = text.indexOf(System.getProperty("line.separator"));
            if (next == -1) {
                return -1;
            } else {
                return next + System.getProperty("line.separator").length();
            }
        }
        return -1;

    }

    int searchEndOfComment(String text) {
        if (text.trim().startsWith("/*")) {
            int next = text.indexOf("*/");
            if (next == -1) {
                return -1;
            } else {
                return next + 2;
            }
        }
        return -1;
    }

    private OperationHeader getOH(String header, String className) {
        String signature = skipComment(header).replaceAll(System.getProperty("line.separator"), " ");
        String[] parts = signature.trim().split("\\s+");
        int part = 0;

        while (part < parts.length && parts[part].startsWith("@")) {
            part++;
        }
        String access;
        if (part >= parts.length) {
            throw new RuntimeException("incorrect header");
        }

        if (!ACCESSMODIFIERS.contains(parts[part])) {
            access = "";
        } else {
            access = parts[part];
            part++;
        }

        List<String> modifiers = new ArrayList<>();
        boolean keywordFound = MODIFIERS.contains(parts[part]);
        while (keywordFound) {
            modifiers.add(parts[part]);
            part++;
            keywordFound = MODIFIERS.contains(parts[part]);
        }

        String retrn;
        if (parts[part].trim().equals(className)) {
            retrn = "";
        } else {
            retrn = parts[part];
            part++;
        }

        int paramListStart = signature.indexOf("(");
        int paramListEnd = signature.indexOf(")");

        String name;
        parts = signature.substring(0, paramListStart).split("\\s");
        name = parts[parts.length - 1];

        String stringParams = signature.substring(paramListStart + 1, paramListEnd);
        List<String> params = Arrays.asList(stringParams.split(","));

        List<String> exceptions = new ArrayList<>();
        if (signature.substring(paramListEnd + 1).trim().startsWith("throws")) {
            int pointer = paramListEnd + 1;
            skipSpaces(signature, pointer);
            pointer += 6; // six characters of throws
            skipSpaces(signature, pointer);
            parts = signature.substring(pointer).split(",\\s+");
            for (int i = 0; i < parts.length; i++) {
                exceptions.add(parts[i].trim());
            }

        }
        return new LanguageOH(access, modifiers, retrn, name, params, exceptions, this);
    }

    private void skipSpaces(String text, int pointer) {
        while (pointer < text.length() && Character.isWhitespace(text.charAt(pointer))) {
            pointer++;
        }
    }

    @Override
    public String autoIncr(String fieldName) {
        return fieldName + "++";
    }

    @Override
    public String adjustMap(Relation r, String key, String amount) {
        StringBuilder sb = new StringBuilder();
        sb.append(r.fieldName());
        sb.append(memberOperator());
        sb.append("put(");
        sb.append(key);
        sb.append(", ");
        sb.append(r.fieldName());
        sb.append(memberOperator());
        sb.append("get(");
        sb.append(key);
        sb.append(") + ");
        sb.append(amount);
        sb.append(");");
        return sb.toString();
    }

    @Override
    public String get(List<Param> params) {
        StringBuilder sb = new StringBuilder("get(");
        String separator = "";
        for (Param param : params) {
            sb.append(separator);
            if (param.getType().equals(BaseType.NATURAL)) {
                sb.append(param.getName());
                sb.append(" - 1");
            } else {
                sb.append(param.expressIn(this));
            }
            separator = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String propertyName(String name, STorCT returnType) {
        if (returnType.equals(BaseType.BOOLEAN)) {
            if (name.toLowerCase().startsWith("is")) {
                return Naming.withoutCapital(name);
            } else {
                return "is" + Naming.withCapital(name);
            }
        } else {
            return "get" + Naming.withCapital(name);
        }
    }

    @Override
    public IndentedList operationTemplate(OperationHeader oh, boolean editable, boolean isAbstract) {
        IndentedList list = new IndentedList();
        String access = oh.getAccessModifier(this);
        if (editable && access.equalsIgnoreCase("private")) {
            access = "protected";
        }

        if (isAbstract) {
            list.addLine(access + " abstract " + oh.getReturn(this) + " " + oh.getName(this) + "(", true);

        } else {

            list.addLine(access + " " + oh.getReturn(this) + " " + oh.getName(this) + "(", true);
        }
        List<String> paramTypes = oh.getParamTypes(this);
        List<String> paramNames = oh.getParamNames(this);
        for (int i = 0; i < paramTypes.size(); i++) {
            list.addString(paramTypes.get(i) + " " + paramNames.get(i));
            if (i + 1 < paramTypes.size()) {
                list.addString(", ");
            }
        }

        list.addString(")");
        Iterator<String> exceptions = oh.getExceptions(this).iterator();
        if (exceptions.hasNext()) {
            list.addString(" throws ");
            do {
                list.addString(exceptions.next());
                if (exceptions.hasNext()) {
                    list.addString(", ");
                }
            } while (exceptions.hasNext());
        }
        if (isAbstract) {
            list.addString(";");
            list.addLine("", false);
        } else {
            list.addString(" " + bodyStart());
            if (editable) {
                list.addLineAtCurrentIndentation(throwUnsupportedOperationException("Symbiosis: Please write code here"));
            } else {
                list.addLineAtCurrentIndentation(throwUnsupportedOperationException("Symbiosis: Please do not enter code here;"
                    + " you are invited for editing in the jave file with the name extended with "
                    + TEMPLATE + "-postfix."));
            }
            list.addLinesAtCurrentIndentation(bodyClosure());
        }
        return list;
    }

    @Override
    public IndentedList constructorTemplate(Constructor c) {
        StringBuilder result = new StringBuilder();
        IndentedList list = new IndentedList();
        // access modifier
        result.append(accessModifier(c.getAccess()));
        // add the name
        result.append(" ").append(c.getName() + TEMPLATE);
        // add parameters
        Iterator<Param> params = c.getParams().iterator();
        result.append("(");
        while (params.hasNext()) {
            Param p = params.next();
            result.append(type(p.getType())).append(" ").append(p.getName());
            if (params.hasNext()) {
                result.append(", ");
            }
        }
        result.append(")");
        Iterator<equa.code.operations.Exception> exceptions = c.getExceptions();
        if (exceptions.hasNext()) {
            result.append(" throws");
        }
        while (exceptions.hasNext()) {
            equa.code.operations.Exception e = exceptions.next();
            result.append(" ").append(e.getName());
            if (exceptions.hasNext()) {
                result.append(",");
            }
        }
        list.addLine(result.append(" ").append(bodyStart()).toString(), true);
        result = new StringBuilder("super(");
        params = c.getParams().iterator();
        while (params.hasNext()) {
            Param p = params.next();
            result.append(p.getName());
            if (params.hasNext()) {
                result.append(", ");
            }
        }
        result.append(");");
        list.addLineAtCurrentIndentation(result.toString());
        list.addLinesAtCurrentIndentation(bodyClosure());
        return list;
    }

    @Override
    public List<String> getImports(String s) {
        int importIndex = s.indexOf("\nimport ");
        if (importIndex == -1) {
            return java.util.Collections.emptyList();
        } else {
            List<String> imports = new ArrayList<>();
            do {
                imports.add(s.substring(importIndex + 8, s.indexOf(";", importIndex)));
                importIndex = s.indexOf("\nimport ", importIndex + 1);
            } while (importIndex != -1);
            return imports;
        }
    }

    @Override
    public Set<String> getFields(String s) {
        int fieldIndex = s.indexOf("class ");
        if (fieldIndex == -1) {
            return new HashSet<>();
        }
        fieldIndex += s.substring(fieldIndex).indexOf("{");
        Set<String> fields = new HashSet<>();
        String remainingText = skipComment(s.substring(fieldIndex + 1));
        fieldIndex = remainingText.indexOf(";");
        boolean endOfFieldsSection = fieldIndex == -1 || remainingText.substring(0, fieldIndex).contains("(");
        while (!endOfFieldsSection) {
            if (isField(remainingText.substring(0, fieldIndex), false)) {
                fields.add(remainingText.substring(0, fieldIndex + 1).trim());
            }
            remainingText = skipComment(remainingText.substring(fieldIndex + 1));
            fieldIndex = remainingText.indexOf(";");
            endOfFieldsSection = fieldIndex == -1 || remainingText.substring(0, fieldIndex).contains("(");
        }
        return fields;
    }

    boolean isField(String s, boolean constant) {
        String trimmed = skipComment(s.trim());
        if (trimmed.isEmpty()) {
            return false;
        }
        if (constant) {
            return trimmed.contains("static ");
        } else {
            return !trimmed.contains("static ");
        }
    }

    @Override
    public Set<String> getConstants(String s) {
        int fieldIndex = s.indexOf("class ");
        if (fieldIndex == -1) {
            return new HashSet<>();
        }
        fieldIndex += s.substring(fieldIndex).indexOf("{");
        Set<String> constants = new HashSet<>();
        String remainingText = s.substring(fieldIndex + 1);
        fieldIndex = remainingText.indexOf(";");
        boolean endOfFieldsSection = fieldIndex != -1 || remainingText.substring(0, fieldIndex).contains("(");
        while (!endOfFieldsSection) {
            if (isField(remainingText.substring(0, fieldIndex), true)) {
                constants.add(remainingText.substring(0, fieldIndex).trim());
            }
            remainingText = remainingText.substring(fieldIndex + 1);
            fieldIndex = remainingText.indexOf(";");
        }
        return constants;
    }

    @Override
    public String putStatement(String name, String key, String value) {
        StringBuilder sb = new StringBuilder(name);
        sb.append(memberOperator());
        sb.append("put(");
        sb.append(key);
        sb.append(", ");
        sb.append(value);
        sb.append(");");
        return sb.toString();
    }

    @Override
    public IndentedList postProcessing(IRelationalOperation o) {
        IndentedList list = new IndentedList();
        Operation operation = (Operation) o;
        CodeClass cc = operation.getCodeClass();

        List<RoleEvent> events = o.getRelation().getEvents();
        for (RoleEvent e : events) {
            if (operation.canTrigger(e)) {
                FactType booleanFT = e.getCondition();
                IndentedList trueStatement = new IndentedList();
                trueStatement.addLineAtCurrentIndentation("this." + e.getNameOfHandler() + "();");
                if (booleanFT != null) {
                    Relation booleanRelation = new BooleanRelation((ObjectType) cc.getParent(), booleanFT.getResponsibleRole());

                    Property condition = cc.getProperty(booleanRelation);
                    //trueStatement.add(bodyStart());

                    //trueStatement.add(bodyClosure());
                    list.addLinesAtCurrentIndentation(ifStatement(booleanCall(condition.getName()), trueStatement));
                } else {
                    list.addLinesAtCurrentIndentation(trueStatement);
                }
            }
        }

        return list;
    }

    static String booleanCall(String propertyName) {
        String call = Naming.withCapital(propertyName);
        if (!call.toLowerCase().startsWith("is")) {
            call = "is" + call;
        }
        return call + "()";
    }

    @Override
    public String element(String list, String index) {
        String element;
        element = list + ".get(" + index + ")";
        return element;
    }

    @Override
    public String languageIndependentHeader(LanguageOH oh) {
        StringBuilder sb = new StringBuilder();
        String access = oh.getAccessModifier(JAVA);
        if (access.trim().isEmpty()) {
            access = " ";
        }
        sb.append(access).append(" ");
        sb.append(oh.getName(JAVA));
        sb.append("(");
        List<String> paramNames = oh.getParamNames(JAVA);
        List<String> paramTypes = oh.getParamTypes(JAVA);
        for (int i = 0; i < paramNames.size(); i++) {
            sb.append(paramNames.get(i));
            sb.append(" : ");
            sb.append(paramTypes.get(i));
        }
        sb.append(") ");
        // todo: compound returntypes are not expressed language independently
        String ret = oh.getReturn(JAVA);
        if (!ret.isEmpty()) {
            sb.append(": ").append(ret).append(" ");
        }
        List<String> modifiers = oh.getModifiers();
        if (modifiers.contains("static")) {
            sb.append("{class method}");
        }
        return sb.toString();

    }

    @Override
    public String isEmpty(String collection) {
        return collection + memberOperator() + "isEmpty()";
    }

    @Override
    public String superCall(Operation operation, List<Param> params) {
        return "super." + operation.callString(params);
    }

    @Override
    public String defaultValue(Relation r) {
        if (r.targetType().equals(BaseType.STRING)) {
            return stringSymbol() + r.getDefaultValue() + stringSymbol();
        } else if (r.targetType() instanceof ConstrainedBaseType) {
            return newInstance(r.targetType(), r.getDefaultValue() + "");
        } else {
            return r.getDefaultValue();
        }
    }

    @Override
    public String overrideModifier() {
        return "@Override";
    }

}

class JavaObjectFromString extends SimpleJavaFileObject {

    private String contents = null;

    public JavaObjectFromString(String className, String contents) {
        super(new File(className).toURI(), Kind.SOURCE);
        this.contents = contents;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return contents;
    }
}
