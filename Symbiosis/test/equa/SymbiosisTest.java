package equa;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import equa.code.Language;
import equa.meta.ChangeNotAllowedException;
import equa.meta.Message;
import equa.meta.objectmodel.BaseValueRole;
import equa.meta.objectmodel.CBTRole;
import equa.meta.objectmodel.FactType;
import equa.meta.objectmodel.ObjectModel;
import equa.meta.objectmodel.ObjectRole;
import equa.meta.requirements.Requirement;
import equa.meta.requirements.RequirementModel;
import equa.meta.requirements.RuleRequirement;
import equa.project.Project;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.management.modelmbean.RequiredModelMBean;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author frankpeeters
 */
public class SymbiosisTest {

    private Project p;
    private ObjectModel om;
    private static File rootPath;
    private File projectPath;
    private String projectFileName;

    @BeforeClass
    public static void setUp() {
        rootPath = new File("/Users/frankpeeters/");
    }

    @Test
    public void testAirhockey() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "__SymbiosisTest/Airhockey"), "AirhockeyTest.sym");
        generateBehaviour();
        generateSource();
    }
    
   @Test
    public void testAirhockeyTutorial() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "_AirhockeyTutorial"), "AirhockeyTutorial4.sym");
        generateBehaviour();
        generateSource();
    }

    @Test
    public void testAirflights() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "__SymbiosisTest/AirFlights/"), "AirFlightsTest.sym");
        generateBehaviour();
        generateSource();
    }

    @Test
    public void testCountries() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "__SymbiosisTest/Countries/"), "CountriesTest.sym");
        generateBehaviour();
        generateSource();
    }

    @Test
    public void testCompany() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "__SymbiosisTest/Company/"), "CompanyTest.sym");
        generateBehaviour();
        generateSource();
    }

    @Test
    public void testStaffAssociation() throws IOException, ClassNotFoundException {
        initFields(new File(rootPath, "__SA/"), "StaffAssociation4.sym");
        generateBehaviour();
        generateSource();
    }

    private void initFields(File path, String fileName) throws ClassNotFoundException, IOException {
        projectPath = path;
        this.projectFileName = fileName;
        File f = new File(projectPath, fileName);
        p = Project.getProject(f);
        om = p.getObjectModel();
    }

    private void generateBehaviour() throws IOException, ClassNotFoundException {
        List<Message> messages = om.generateClasses(true, true);
        int errors = 0;
        int warnings = 0;
        for (Message message : messages) {
            if (message.isError()) {
                errors++;
            } else {
                warnings++;
            }
            System.out.println(message.getText());
        }
        assertEquals("errors while creating behaviour of " + projectFileName, 0, errors);
    }

    private void generateSource() throws FileNotFoundException {
        boolean result = Language.JAVA.generate(om, true, false, false, projectPath.getAbsolutePath() + "/src");
        assertTrue("model errors in " + projectFileName + "/src", result);
    }

     @Test
    public void testRemoveDefaultValue() throws ClassNotFoundException, IOException {
        initFields(new File(rootPath, "__SymbiosisTest/Airhockey"), "AirhockeyTest.sym");
        int size = p.getRequirementModel().countRules();
        FactType positionBat = om.getFactType("BatPosition");
        CBTRole position = (CBTRole) positionBat.getRole(1);
        position.removeDefaultValue();
        assertEquals("Removing defaultValue Rule", size - 1, p.getRequirementModel().countRules());
        checkRequirements();
    }

    @Test
    public void testRemoveCBT() throws ClassNotFoundException, IOException, ChangeNotAllowedException {
        initFields(new File(rootPath, "__SymbiosisTest/Airhockey"), "AirhockeyTest.sym");
        int size = p.getRequirementModel().countRules();
        FactType positionBat = om.getFactType("BatPosition");
        positionBat.deobjectifyRole((ObjectRole) positionBat.getRole(1));
        assertEquals("Removing ValueConstraint with DefaultValue", size - 1, p.getRequirementModel().countRules());
        om.removeFactType(om.getFactType("Position"));
        // removing of value constraint rule and uniqueness constraint 
        assertEquals("Removing ValueConstraint with DefaultValue", size - 3, p.getRequirementModel().countRules());
        checkRequirements();
    }

    private void checkRequirements() throws FileNotFoundException, ClassNotFoundException, IOException {
        p.save(new File(projectPath, "Test.sym"));
        initFields(projectPath, "Test.sym");
        RequirementModel rm = p.getRequirementModel();
        Iterator<Requirement> it = rm.requirements();
        while (it.hasNext()) {
            it.next().getText();
        }
    }

    private void testRemoveMandatoryConstraint() {
        // objecttype

        // settype
        //sequencetype
    }

    private void testRemoveFrequencyConstraint() {
        // objecttype

        // settype
        //sequencetype
    }

    private void testRemoveValueConstraint() {

    }

    private void testRemoveUniquenessConstraint() {

    }

    private void testRemovePermissions() {
        //addable

        //insertable
        //settable
        //removable
        //adjustable
    }

    private void testObjectify() {

    }

    private void testDeobjectify() {

    }

    private void testMergeRoles() {

    }

    private void testReplaceBySubtype() {

    }

    private void testReplaceBySupertype() {

    }

    private void testRemoveAutoIncr() {

    }

    private void testRemoveAbstract() {

    }

    private void testRemoveFactType() {

    }

    private void testRemoveObjectType() {

    }

    private void testRemoveCollectionType() {

    }

    private void testRemoveAbstractType() {

    }

    private void testRemoveSingleton() {

    }

    private void testRemoveAction() {

    }

    private void testRemoveFact() {

    }

    private void testRemoveRule() {

    }

}
