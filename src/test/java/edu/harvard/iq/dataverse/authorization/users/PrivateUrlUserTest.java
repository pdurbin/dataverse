/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.authorization.users;

import edu.harvard.iq.dataverse.authorization.RoleAssignee;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class PrivateUrlUserTest {

    @Test
    public void testIdentifier2roleAssignee() {
        RoleAssignee returnValueFromEmptyString = null;
        try {
            returnValueFromEmptyString = PrivateUrlUser.identifier2roleAssignee("");
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
            assertEquals(ex.getMessage(), "Could not find dataset id in ''");
        }
        assertNull(returnValueFromEmptyString);

        RoleAssignee returnValueFromNonColon = null;
        String peteIdentifier = "@pete";
        try {
            returnValueFromNonColon = PrivateUrlUser.identifier2roleAssignee(peteIdentifier);
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
            assertEquals(ex.getMessage(), "Could not find dataset id in '" + peteIdentifier + "'");
        }
        assertNull(returnValueFromNonColon);

        RoleAssignee returnValueFromNonNumber = null;
        String nonNumberIdentifier = PrivateUrlUser.PREFIX + "nonNumber";
        try {
            returnValueFromNonNumber = PrivateUrlUser.identifier2roleAssignee(nonNumberIdentifier);
        } catch (Exception ex) {
            assertEquals(ex.getClass(), IllegalArgumentException.class);
            assertEquals(ex.getMessage(), "Could not find dataset id in '" + nonNumberIdentifier + "'");
        }
        assertNull(returnValueFromNonNumber);

        RoleAssignee returnFromValidIdentifier = null;
        String validIdentifier = PrivateUrlUser.PREFIX + 42;
        returnFromValidIdentifier = PrivateUrlUser.identifier2roleAssignee(validIdentifier);
        assertNotNull(returnFromValidIdentifier);
        assertEquals(":privateUrlForDvObjectId42", returnFromValidIdentifier.getIdentifier());
        assertEquals("Private URL Enabled", returnFromValidIdentifier.getDisplayInfo().getTitle());
        Assert.assertTrue(returnFromValidIdentifier instanceof PrivateUrlUser);
        PrivateUrlUser privateUrlUser42 = (PrivateUrlUser) returnFromValidIdentifier;
        assertEquals(42, privateUrlUser42.getDatasetId());

    }

}
