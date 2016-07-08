package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.confirmemail.ConfirmEmailData;
import edu.harvard.iq.dataverse.util.json.JsonPrinter;
import java.util.UUID;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static edu.harvard.iq.dataverse.util.json.JsonPrinter.jsonForAuthUser;
import static junit.framework.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author bsilverstein
 * @todo: Develop test to reflect access restrictions without confirmation
 */
public class ConfirmEmailIT {
    
    private static final Logger logger = Logger.getLogger(ConfirmEmailIT.class.getCanonicalName());
    
    private static final String builtinUserKey = "burrito";
    private static final String idKey = "id";
    private static final String usernameKey = "userName";
    private static final String emailKey = "email";
    private static final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
    private static final ConfirmEmailData emailData = new ConfirmEmailData(authenticatedUser);
    private static final String confirmToken = getConfirmEmailToken(emailData);
    
    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();             
    }
    
    

        @Test
        public void testConfirm() {
            //  Can't seem to get timestamp to appear in authenticated user Json output
            String email = null;
            Response createUserToConfirm = createUser(getRandomUsername(), "firstName", "lastName", email);
            createUserToConfirm.prettyPrint();
            createUserToConfirm.then().assertThat()
                 .statusCode(200);
 
            long userIdToConfirm = JsonPath.from(createUserToConfirm.body().asString()).getLong("data.authenticatedUser.id");
            String userToConfirmApiToken = JsonPath.from(createUserToConfirm.body().asString()).getString("data.apiToken");
            String usernameToConfirm = JsonPath.from(createUserToConfirm.body().asString()).getString("data.user.userName");
            Response getApiToken = getApiTokenUsingUsername(usernameToConfirm, usernameToConfirm);          
            getApiToken.then().assertThat()
                 .statusCode(200);

        }
  
        
    private Response createUser(String username, String firstName, String lastName, String email) {
        String userAsJson = getUserAsJsonString(username, firstName, lastName, email);
        String password = getPassword(userAsJson);
        Response response = given()
                .body(userAsJson) 
                .contentType(ContentType.JSON)
                .post("/api/builtin-users?key=" + builtinUserKey + "&password=" + password);
        return response;
    }
    
    private static String getRandomUsername() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private static String getUserAsJsonString(String username, String firstName, String lastName, String email) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(usernameKey, username);
        builder.add("firstName", firstName);
        builder.add("lastName", lastName);
        if (email == null) {
            builder.add(emailKey, getEmailFromUserName(username));
        } else {
            builder.add(emailKey, email);
        }
        
        String userAsJson = builder.build().toString();
        logger.fine("User to create: " + userAsJson);
        return userAsJson;
    }
    //May be redundant / unusable?
    private static String getAuthUserAsJsonString(AuthenticatedUser authenticatedUser){
        JsonObjectBuilder authenticatedUserBuilder = JsonPrinter.jsonForAuthUser(authenticatedUser);
        authenticatedUserBuilder.add("id", authenticatedUser.getId());
        authenticatedUserBuilder.add("identifier", authenticatedUser.getIdentifier());
        authenticatedUserBuilder.add("displayName", authenticatedUser.getDisplayInfo().getTitle());
        authenticatedUserBuilder.add("firstName", authenticatedUser.getFirstName());
        authenticatedUserBuilder.add("lastName", authenticatedUser.getLastName());
        authenticatedUserBuilder.add("email", authenticatedUser.getEmail());
        authenticatedUserBuilder.add("superuser", authenticatedUser.isSuperuser());
        authenticatedUserBuilder.add("affiliation", authenticatedUser.getAffiliation());
        authenticatedUserBuilder.add("position", authenticatedUser.getPosition());
        authenticatedUserBuilder.add("persistentUserId", authenticatedUser.getAuthenticatedUserLookup().getPersistentUserId());
        authenticatedUserBuilder.add("confirmToken", authenticatedUser.getConfirmToken());
        authenticatedUserBuilder.add("authenticationProviderId", authenticatedUser.getAuthenticatedUserLookup().getAuthenticationProviderId());

        String authenticatedUserAsJson = authenticatedUserBuilder.build().toString();
        logger.fine("Authenticated User to create: " + authenticatedUserAsJson);
        return authenticatedUserAsJson;
        
    }
    
    private static String getPassword(String jsonStr) {
        String password = JsonPath.from(jsonStr).get(usernameKey);
        return password;
    }
    
    private static String getEmailFromUserName(String username) {
        return username + "@mailinator.com";
    }
    
    private static String getConfirmEmailToken(ConfirmEmailData emailData){
        String confirmToken = emailData.getToken();
        return confirmToken;
    }
    
    private Response getApiTokenUsingUsername(String username, String password) {
        Response response = given()
                .contentType(ContentType.JSON)
                .get("/api/builtin-users/" + username + "/api-token?username=" + username + "&password=" + password);
        return response;
    }
    
}
