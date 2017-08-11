/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.persistentid;

import edu.harvard.iq.dataverse.util.xml.XmlValidator;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author pdurbin
 */
public class DataCiteUtilTest {
    
    public DataCiteUtilTest() {
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
     * Test of generateXML method, of class DataCiteUtil.
     */
    @Test
    public void testGenerateXML() throws IOException, SAXException {
        System.out.println("generateXML");
        DataCiteUtil instance = new DataCiteUtil("foo");
        String expResult = "";
        String result = instance.generateXML();
        System.out.println("result: " + result);
        PrintWriter out = new PrintWriter("/tmp/out.xml");
        out.println(result);
        FileUtils.writeStringToFile(new File("/tmp/out.xml"), result);
//        XmlValidator.validateXml("/tmp/foo", "/tmp/bar");
//        assertEquals(expResult, result);
    }
    
}
