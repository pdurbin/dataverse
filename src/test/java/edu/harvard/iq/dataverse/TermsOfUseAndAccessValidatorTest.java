/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import javax.validation.ConstraintValidatorContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pdurbin
 */
public class TermsOfUseAndAccessValidatorTest {
    
    public TermsOfUseAndAccessValidatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class TermsOfUseAndAccessValidator.
     */
//    @Test
//    public void testInitialize() {
//        System.out.println("initialize");
//        ValidateTermsOfUseAndAccess constraintAnnotation = null;
//        TermsOfUseAndAccessValidator instance = new TermsOfUseAndAccessValidator();
//        instance.initialize(constraintAnnotation);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of isValid method, of class TermsOfUseAndAccessValidator.
     */
//    @Test
//    public void testIsValid() {
//        System.out.println("isValid");
//        TermsOfUseAndAccess value = null;
//        ConstraintValidatorContext context = null;
//        TermsOfUseAndAccessValidator instance = new TermsOfUseAndAccessValidator();
//        boolean expResult = false;
//        boolean result = instance.isValid(value, context);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of update method, of class TermsOfUseAndAccessValidator.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        TermsOfUseAndAccess previous = new TermsOfUseAndAccess();
        previous.setDisclaimer("previousDisclaimer");
        TermsOfUseAndAccess incoming = new TermsOfUseAndAccess();
        incoming.setDisclaimer("incomingDisclaimer");
        incoming.setDisclaimer(null);
        TermsOfUseAndAccess expResult = new TermsOfUseAndAccess();
        expResult.setDisclaimer("incomingDisclaimer");
        expResult.setDisclaimer(null);
        TermsOfUseAndAccess result = TermsOfUseAndAccessValidator.update(previous, incoming);
        System.out.println("disclaimer: " + result.getDisclaimer());
        assertEquals(expResult.getDisclaimer(), result.getDisclaimer());
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
