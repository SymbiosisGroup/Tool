/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package symbiosis.code;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import symbiosis.code.operations.AccessModifier;
import symbiosis.code.operations.ActualParam;
import symbiosis.code.operations.CT;
import symbiosis.code.operations.CollectionKind;
import symbiosis.code.operations.Constructor;
import symbiosis.code.operations.IRelationalOperation;
import symbiosis.code.operations.Operation;
import symbiosis.code.operations.Operator;
import symbiosis.code.operations.Param;
import symbiosis.code.operations.Property;
import symbiosis.code.operations.STorCT;
import symbiosis.meta.classrelations.Relation;
import symbiosis.meta.objectmodel.BaseType;
import symbiosis.meta.objectmodel.ConstrainedBaseType;
import symbiosis.meta.objectmodel.ObjectModel;
import symbiosis.meta.objectmodel.ObjectType;
import java.io.FileNotFoundException;

public interface Language extends Serializable {

    public static final Language CSHARP = new CSharp();
    public static final Language JAVA = new Java();

    /**
     *
     * @param oh operation header belonging to this language
     * @return the language independent expression of oh
     */
    String languageIndependentHeader(LanguageOH oh);

    /**
     * See {@link AccessModifier} for all possible modifiers.
     *
     * @param accessModifier
     * @return the string representation of the access modifier
     */
    String accessModifier(AccessModifier accessModifier);

    /**
     * String representation of adding to a collection
     *
     * @param name
     * @param kind
     * @param add
     * @return
     */
    String addCollection(String name, CollectionKind kind, String add);

    /**
     *
     * @return the AND operator with spaces
     */
    String and();

    /**
     * Initializes the field with the correct collection implementation.
     *
     * @param type
     * @return
     */
    String assignCollection(STorCT type);

    /**
     * Assigns the expression to the variable.
     *
     * @param variable
     * @param expression
     * @return
     */
    String assignment(String variable, String expression);

    /**
     * Increments this integer by one
     *
     * @param autoincr
     * @return
     */
    String autoIncr(String autoincr);

    /**
     *
     * @return the String that closes the body of an operation.
     */
    IndentedList bodyClosure();

    /**
     *
     * @return the String that starts the body of an operation.
     */
    String bodyStart();

    /**
     * Calls the constructor.
     *
     * @param otName
     * @param params
     * @return
     */
    String callConstructor(String otName, String... params);

    /**
     * Creates a string for object.name(param1, param2, ...) If object.isEmpty()
     * then it will be name(param1, param2, ...)
     *
     * @param object
     * @param name
     * @param params
     * @return
     */
    String callMethod(String object, String name, List<? extends ActualParam> params);

    /**
     * Creates a string for object.name(param1, param2, ...) If object.isEmpty()
     * then it will be name(param1, param2, ...)
     *
     * @param object
     * @param name
     * @param params
     * @return
     */
    String callMethod(String object, String name, String... params);

    /**
     * Casts oldName to newName of type type.
     *
     * @param type
     * @param newName
     * @param oldName
     * @return the string representation
     */
    String cast(STorCT type, String newName, String oldName);

    /**
     * Returns name instanceof type
     *
     * @param name
     * @param type
     * @return
     */
    String checkType(String name, STorCT type);

    /**
     * Returns the symbol to close a class.
     *
     * @return
     */
    String classClosure();

    /**
     * Creates the classheader for a given ObjectType.
     *
     * @param accessModifier access for this class
     * @param ot
     * @param template if there should be annotation for the persistence layer
     * @param withOrm
     * @param template
     * @param abstrct makes it so that the class is Abstract[OT.name]. To
     * support custom user written code.
     * @return
     */
    IndentedList classHeader(AccessModifier accessModifier, ObjectType ot, boolean template, boolean withOrm);

    /**
     * Clear a collection (make it empty) (removeEvery)
     *
     * @param r
     * @return String representation of this behavior
     */
    String clear(Relation r);

    /**
     * Concatenates two strings
     *
     * @param string1
     * @param string2
     * @return
     */
    String concatenate(String string1, String string2);

    /**
     * The header of a constructor + the call to the super constructor.
     *
     * @reason C#.
     * @param c
     * @param superParams
     * @return
     */
    IndentedList constructorHeaderAndSuper(Constructor c, List<String> superParams);

    /**
     * TODO Returns the contains call for a collection
     *
     * @param r
     * @param name what should be checked if it is in the collection
     * @return
     */
    String contains(Relation r, String name);

    /**
     * Create an instance of an Object with the given parameters
     *
     * @param type the type of the new instance
     * @param name the name for the new instance
     * @param otName the constructor of the new object
     * @param params the ObjectType that is creating the instance (needed for
     * use of this)
     * @return String representation.
     */
    String createInstance(STorCT type, String name, String otName, String... params);

    /**
     * Declares and assigns a value to a variable of a certain type.
     *
     * @param type
     * @param variable
     * @param expression
     * @return
     */
    String declarationAndAssignment(STorCT type, String variable, String expression);

    /**
     * The end of a comment for documentation
     *
     * @return
     */
    String docEnd();

    /**
     * A line of a comment for documentation
     *
     * @param line
     * @return
     */
    String docLine(String line);

    /**
     * The start of a comment for documentation
     *
     * @return
     */
    String docStart();

    /**
     * The end statement symbol (;)
     *
     * @return
     */
    String endStatement();

    /**
     * Calls the equals method on the first string with the second string.
     *
     * @param string1
     * @param string2
     * @return
     */
    String equalsStatement(String string1, String string2);

    /**
     * Declaration of a field member.
     *
     * @param f
     * @param withOrm if there needs to be annotation for the persistence layer
     * @return
     */
    IndentedList field(Field f, boolean withOrm);

    /**
     * Creates a for each loop
     *
     * @param type the type that is in the collection
     * @param name the name for the new variable
     * @param collection the name of the collection that is being iterated
     * @param body the body for the loop
     * @return
     */
    IndentedList forEachLoop(STorCT type, String name, String collection, IndentedList body);

    /**
     * Creates a for loop with an int-variable incremented by 1
     *
     * @param lower start value of the loop variable
     * @param exit the exit value of the loop variable
     * @param body the body for the loop
     * @return
     */
    IndentedList forLoop(int lower, int exit, IndentedList body);

    /**
     * Generates source code for the given parameters.
     *
     * @param om
     * @param edit if false, will try to generate a library, if that fails will
     * be normal source code
     * @param orm annotation for the persistence layer will be generated
     * @param mInh NOT supported at this time.
     * @param loc
     * @throws java.io.FileNotFoundException
     * @throws Exception probably an I/O
     * @return scan of om did not yield any error
     */
    boolean generate(ObjectModel om, boolean edit, boolean orm, boolean mInh, String loc) throws FileNotFoundException;

    /**
     * Returns a get call to a property. Different languages handle properties
     * differently
     *
     * @param name
     * @return
     */
    String getProperty(String name);

    /**
     * Returns the hashcode for a variable
     *
     * @param variable
     * @return
     */
    String hashCodeStatement(String variable);

    /**
     * Creates an if statement with no else
     *
     * @param condition
     * @param trueStatement
     * @return
     */
    IndentedList ifStatement(String condition, IndentedList trueStatement);

    /**
     * Creates an if statement with an else
     *
     * @param condition
     * @param trueStatement
     * @param falseStatement
     * @return
     */
    IndentedList ifStatement(String condition, IndentedList trueStatement, IndentedList falseStatement);

    /**
     * Returns the imports needed for this type
     *
     * @param r
     * @return
     */
    List<ImportType> imports(Relation r);

    /**
     * Calls the indexOf
     *
     * @param r
     * @param param
     * @return
     */
    String indexOf(Relation r, String param);

    /**
     * Calls the moethod which checks if collection is empty
     *
     * @param collection
     * @return
     */
    String isEmpty(String collection);

    /**
     * The operator to access class members.
     *
     * @reason C++
     * @return
     */
    String memberOperator();

    /**
     * Returns the imports and namespace
     *
     * @param nameSpace
     * @param imports
     * @reason C# (namespace is after imports)
     * @return
     */
    IndentedList nameSpaceAndImports(NameSpace nameSpace, Set<ImportType> imports,
        Set<String> importsManuallyAdded);

    String importUtilities(NameSpace namespace);

    /**
     * The symbol to end the namespace (with indentation)
     *
     * @reason C#
     * @return
     */
    IndentedList nameSpaceEnd();

    /**
     * Returns the namespace start
     *
     * @param nameSpace
     * @return
     */
    IndentedList nameSpaceStart(NameSpace nameSpace);

    /**
     * Negates the value of statement.
     *
     * @param statement
     * @return
     */
    String negate(String statement);

    /**
     * Creates a new instance of type type with params.
     *
     * @param type
     * @param params
     * @return
     */
    String newInstance(STorCT type, String... params);

    /**
     * @reason C#
     * @return the parameter for an object for equals method
     */
    String nonObjectKeyword();

    /**
     * Returns the entire operation header for an operation Also does the
     * prespec and escape conditions.
     *
     * @param o
     * @return
     */
    IndentedList operationHeader(Operation o);

    /**
     * The operator with spaces e.g. [ < ]
     *
     * @param operator
     * @return
     */
    String operator(Operator operator);

    /**
     * The OR boolean operator with spaces
     *
     * @return
     */
    String or();

    /**
     * Creates the code for this property.
     *
     * @param p
     * @return
     */
    IndentedList propertyCode(Property p);

    /**
     * Removes an item from a collection (the string representation)
     *
     * @param r
     * @param removable
     * @return
     */
    String removeStatement(Relation r, String removable);

    /**
     * Calls the removeAt for sequenceRelation
     *
     * @param name
     * @param index
     * @return
     */
    String removeAtStatement(String name, String index);

    /**
     *
     * @param statement The statement
     * @return String representing returning the statement;
     */
    String returnStatement(String statement);

    String setProperty(String object, String property, String parameter);

    /**
     * Returns the call to the size of the collection
     *
     * @param ck
     * @return
     */
    String size(CollectionKind ck);

    /**
     * @return the symbol to start and end a String.
     */
    String stringSymbol();

    /**
     * Calls the subsequence method on the collection associated with the
     * relation
     *
     * @param r
     * @param i start
     * @param j end
     * @return String representation
     */
    String subseq(Relation r, String i, String j);

    String superCall(Operation operation, List<Param> params);

    /**
     * The class header for the system class SystemOperations
     *
     * @return
     */
    IndentedList systemClassHeader();

    /**
     * A reference to the instance itself
     *
     * @return
     */
    String thisKeyword();

    /**
     * Throws an unsupported operation exception with given msg. This is used
     * for custom code.
     *
     * @param msg
     * @return
     */
    String throwUnsupportedOperationException(String msg);

    /**
     *
     * @param type
     * @return the String representation for the given type.
     */
    String type(STorCT type);

    /**
     * Returns the collection as an unmodifiable collection OR as a copy
     * (depending on if the language supports unmodifiable)
     *
     * @param ct
     * @param statement
     * @return
     */
    String unmodifiable(CT ct, String statement);

    //TODO JH
    Map<OperationHeader, ImportedOperation> getOperations(String s, String className);

    Set<String> getFields(String s);
    
    Set<String> getConstants(String s);
    
     //TODO JH

    List<String> getImports(String s);
    
    //TODO JH
    String adjustMap(Relation r, String key, String amount);

    //TODO JH

    String get(List<Param> params);

    //TODO JH

    String propertyName(String name, STorCT returnType);

    //TODO JH

    IndentedList operationTemplate(OperationHeader oh, boolean editable, boolean isAbstract);

    //TODO JH

    IndentedList constructorTemplate(Constructor c);

   

    //TODO JH

    String putStatement(String name, String key, String value);

    //TODO JH

    IndentedList postProcessing(IRelationalOperation o);

    /**
     *
     * @param list a sequence of elements
     * @param index 0 <= index < list.size()
         * @
     * return the element in list with on index
     */
    String element(String list, String index);

    public String defaultValue(Relation r);
    
    public String overrideModifier();
    
}
