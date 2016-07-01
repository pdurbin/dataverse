package edu.harvard.iq.dataverse.api;

import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinUserServiceBean;
import java.util.UUID;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.containsString;
import org.junit.BeforeClass;
import org.junit.Test;

public class BuiltinUsersIT {

    private static final Logger logger = Logger.getLogger(BuiltinUsersIT.class.getCanonicalName());

    private static final String builtinUserKey = "burrito";
    private static final String idKey = "id";
    private static final String usernameKey = "userName";
    private static final String emailKey = "email";

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = UtilIT.getRestAssuredBaseUri();
    }

    /**
     * @todo We're simulating a password guessing attack here by using the API
     * token lookup endpoint but are there other ways to easily test this? Is it
     * time to start trying to use https://www.cypress.io for automated API
     * testing? Or just use Selenium?
     */
    @Test
    public void testPasswordGuessingAttack() {
        String email = null;
        Response createUserToBeAttacked = createUser(getRandomUsername(), "firstName", "lastName", email);
        createUserToBeAttacked.prettyPrint();
        createUserToBeAttacked.then().assertThat()
                .statusCode(200);

        long userIdUnderAttack = JsonPath.from(createUserToBeAttacked.body().asString()).getLong("data.authenticatedUser.id");
        String usernameUnderAttack = JsonPath.from(createUserToBeAttacked.body().asString()).getString("data.user.userName");
        String apiToken = JsonPath.from(createUserToBeAttacked.body().asString()).getString("data.apiToken");
        int numAttemptsNeededToLockAccountMinusOne = BuiltinUserServiceBean.numBadLoginsRequiredToLockAccount - 1;

        for (int i = 0; i < numAttemptsNeededToLockAccountMinusOne; i++) {
            Response getApiTokenShouldFail = getApiTokenUsingUsername(usernameUnderAttack, "guess" + i);
            getApiTokenShouldFail.prettyPrint();
            getApiTokenShouldFail.then().assertThat()
                    .statusCode(400)
                    .body("message", equalTo("Bad username or password"));
        }
        Response attemptShouldLockAccount = getApiTokenUsingUsername(usernameUnderAttack, "guess" + numAttemptsNeededToLockAccountMinusOne + 1);
        attemptShouldLockAccount.prettyPrint();
        attemptShouldLockAccount.then().assertThat()
                .statusCode(400)
                /**
                 * @todo Add the timestamp, I guess.
                 */
                .body("message", equalTo("Bad username or password. Account has been locked until FIXME."));

        Response shouldShowAlreadyLockedResponse = getApiTokenUsingUsername(usernameUnderAttack, "wrongPassword");
        shouldShowAlreadyLockedResponse.prettyPrint();
        shouldShowAlreadyLockedResponse.then().assertThat()
                .statusCode(400)
                /**
                 * @todo Add the timestamp, I guess. Should this message be
                 * different than above?
                 */
                .body("message", equalTo("Bad username or password. Account has been locked until FIXME."));

    }

    @Test
    public void testDisableAccount() throws InterruptedException {

        String email = null;
        Response createUserWhoWillBeDisabled = createUser(getRandomUsername(), "firstName", "lastName", email);
        createUserWhoWillBeDisabled.prettyPrint();
        createUserWhoWillBeDisabled.then().assertThat()
                .statusCode(200);

        long userIdToBeDisabled = JsonPath.from(createUserWhoWillBeDisabled.body().asString()).getLong("data.authenticatedUser.id");
        String userToBeDisabledApiToken = JsonPath.from(createUserWhoWillBeDisabled.body().asString()).getString("data.apiToken");
        String usernameToBeDisabled = JsonPath.from(createUserWhoWillBeDisabled.body().asString()).getString("data.user.userName");

        Response getApiToken = getApiTokenUsingUsername(usernameToBeDisabled, usernameToBeDisabled);
        getApiToken.then().assertThat()
                .statusCode(200);

        Response createDataverse = UtilIT.createRandomDataverse(userToBeDisabledApiToken);
        createDataverse.prettyPrint();
        createDataverse.then().assertThat()
                .statusCode(201);
        String dataverseAlias = JsonPath.from(createDataverse.body().asString()).getString("data.alias");

        UtilIT.deleteDataverse(dataverseAlias, userToBeDisabledApiToken).then().assertThat()
                .statusCode(200);

        Response createSuperuser = createUser(getRandomUsername(), "firstName", "lastName", email);
        createSuperuser.prettyPrint();
        createSuperuser.then().assertThat()
                .statusCode(200);

        String superuserUsername = JsonPath.from(createSuperuser.body().asString()).getString("data.user.userName");
        String superuserApiToken = JsonPath.from(createSuperuser.body().asString()).getString("data.apiToken");

        Response makeSuperUser = UtilIT.makeSuperUser(superuserUsername);
        makeSuperUser.then().assertThat()
                .statusCode(200);

        Response attemptToDisableSelf = UtilIT.lockUser(userIdToBeDisabled, userToBeDisabledApiToken);
        attemptToDisableSelf.prettyPrint();
        attemptToDisableSelf.then().assertThat()
                .statusCode(403)
                .body("message", equalTo("Superusers only."));

        /**
         * @todo how do we push out the expiration date after 10 invalid logins?
         */
        long numSecondsToLock = 3;
        Response lockUserTemporarily = UtilIT.lockAccount(userIdToBeDisabled, numSecondsToLock, superuserApiToken);
        lockUserTemporarily.prettyPrint();
        lockUserTemporarily.then().assertThat()
                .statusCode(200)
                .body("data.message", equalTo("User id " + userIdToBeDisabled + " has been locked for " + numSecondsToLock + " seconds."));

        Response createDataverseShouldFail = UtilIT.createRandomDataverse(userToBeDisabledApiToken);
        createDataverseShouldFail.prettyPrint();
        createDataverseShouldFail.then().assertThat()
                .statusCode(401)
                .body("message", containsString("account for user id " + userIdToBeDisabled + " is locked until"));

        Response getApiTokenShouldFail = getApiTokenUsingUsername(usernameToBeDisabled, usernameToBeDisabled);
        getApiTokenShouldFail.prettyPrint();
        getApiTokenShouldFail.then().assertThat()
                .statusCode(403)
                .body("message", containsString("account for user id " + userIdToBeDisabled + " is locked until"));;

        Thread.sleep(numSecondsToLock * 1000);

        Response createDataverseShouldWorkAgain = UtilIT.createRandomDataverse(userToBeDisabledApiToken);
//        createDataverseShouldWorkAgain.prettyPrint();
        createDataverseShouldWorkAgain.then().assertThat()
                .statusCode(201);

        Response lockUserIndefinitely = UtilIT.lockUser(userIdToBeDisabled, superuserApiToken);
        lockUserIndefinitely.prettyPrint();
        lockUserIndefinitely.then().assertThat()
                .statusCode(200)
                .body("data.message", equalTo("User id " + userIdToBeDisabled + " has been locked indefinitely."));

        Response createDataverseShouldFailLockedIndefinitely = UtilIT.createRandomDataverse(userToBeDisabledApiToken);
//        createDataverseShouldFailLockedIndefinitely.prettyPrint();
        createDataverseShouldFailLockedIndefinitely.then().assertThat()
                .statusCode(401)
                .body("message", containsString("account for user id " + userIdToBeDisabled + " is locked until"));

        Response userLockedIndefinitely = getUserFromDatabase(usernameToBeDisabled);
        userLockedIndefinitely.prettyPrint();
        userLockedIndefinitely.then().assertThat()
                .statusCode(200)
                .body("data.lockedUntil", startsWith("9999"));

        Response unlockUser = UtilIT.unlockUser(userIdToBeDisabled, superuserApiToken);
        unlockUser.prettyPrint();
        unlockUser.then().assertThat()
                .statusCode(200)
                .body("data.message", equalTo("User id " + userIdToBeDisabled + " has been unlocked."));

        Response createDataverseShouldWorkOnceAgain = UtilIT.createRandomDataverse(userToBeDisabledApiToken);
//        createDataverseShouldWorkOnceAgain.prettyPrint();
        createDataverseShouldWorkOnceAgain.then().assertThat()
                .statusCode(201);

        Response userNoLongerLocked = getUserFromDatabase(usernameToBeDisabled);
        userNoLongerLocked.prettyPrint();
        userNoLongerLocked.then().assertThat()
                .statusCode(200)
                .body("data.lockedUntil", nullValue());

    }

    @Test
    public void testUserId() {

        String email = null;
        Response createUserResponse = createUser(getRandomUsername(), "firstName", "lastName", email);
        createUserResponse.prettyPrint();
        assertEquals(200, createUserResponse.getStatusCode());

        JsonPath createdUser = JsonPath.from(createUserResponse.body().asString());
        int builtInUserIdFromJsonCreateResponse = createdUser.getInt("data.user." + idKey);
        int authenticatedUserIdFromJsonCreateResponse = createdUser.getInt("data.authenticatedUser." + idKey);
        String username = createdUser.getString("data.user." + usernameKey);

        Response getUserResponse = getUserFromDatabase(username);
        getUserResponse.prettyPrint();
        assertEquals(200, getUserResponse.getStatusCode());

        JsonPath getUserJson = JsonPath.from(getUserResponse.body().asString());
        int userIdFromDatabase = getUserJson.getInt("data.id");

        Response deleteUserResponse = deleteUser(username);
        assertEquals(200, deleteUserResponse.getStatusCode());
        deleteUserResponse.prettyPrint();

        System.out.println(userIdFromDatabase + " was the id from the database");
        System.out.println(builtInUserIdFromJsonCreateResponse + " was the id of the BuiltinUser from JSON response on create");
        System.out.println(authenticatedUserIdFromJsonCreateResponse + " was the id of the AuthenticatedUser from JSON response on create");
        assertEquals(userIdFromDatabase, authenticatedUserIdFromJsonCreateResponse);
    }

    @Test
    public void testLeadingWhitespaceInEmailAddress() {
        String randomUsername = getRandomUsername();
        String email = " " + randomUsername + "@mailinator.com";
        Response createUserResponse = createUser(randomUsername, "firstName", "lastName", email);
        createUserResponse.prettyPrint();
        assertEquals(200, createUserResponse.statusCode());
        String emailActual = JsonPath.from(createUserResponse.body().asString()).getString("data.user." + emailKey);
        // the backend will trim the email address
        String emailExpected = email.trim();
        assertEquals(emailExpected, emailActual);
    }

    @Test
    public void testLeadingWhitespaceInUsername() {
        String randomUsername = " " + getRandomUsername();
        String email = randomUsername + "@mailinator.com";
        Response createUserResponse = createUser(randomUsername, "firstName", "lastName", email);
        createUserResponse.prettyPrint();
        assertEquals(400, createUserResponse.statusCode());
    }

    @Test
    public void testLogin() {

        String usernameToCreate = getRandomUsername();
        String email = null;
        Response createUserResponse = createUser(usernameToCreate, "firstName", "lastName", email);
        createUserResponse.prettyPrint();
        assertEquals(200, createUserResponse.getStatusCode());

        JsonPath createdUser = JsonPath.from(createUserResponse.body().asString());
        String createdUsername = createdUser.getString("data.user." + usernameKey);
        assertEquals(usernameToCreate, createdUsername);
        String createdToken = createdUser.getString("data.apiToken");
        logger.info(createdToken);

        Response getApiTokenUsingUsername = getApiTokenUsingUsername(usernameToCreate, usernameToCreate);
        getApiTokenUsingUsername.prettyPrint();
        assertEquals(200, getApiTokenUsingUsername.getStatusCode());
        String retrievedTokenUsingUsername = JsonPath.from(getApiTokenUsingUsername.asString()).getString("data.message");
        assertEquals(createdToken, retrievedTokenUsingUsername);

        Response failExpected = getApiTokenUsingUsername("junk", "junk");
        failExpected.prettyPrint();
        assertEquals(400, failExpected.getStatusCode());

        if (BuiltinUsers.retrievingApiTokenViaEmailEnabled) {
            Response getApiTokenUsingEmail = getApiTokenUsingEmail(usernameToCreate + "@mailinator.com", usernameToCreate);
            getApiTokenUsingEmail.prettyPrint();
            assertEquals(200, getApiTokenUsingEmail.getStatusCode());
            String retrievedTokenUsingEmail = JsonPath.from(getApiTokenUsingEmail.asString()).getString("data.message");
            assertEquals(createdToken, retrievedTokenUsingEmail);
        }

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

    private Response getApiTokenUsingUsername(String username, String password) {
        Response response = given()
                .contentType(ContentType.JSON)
                .get("/api/builtin-users/" + username + "/api-token?username=" + username + "&password=" + password);
        return response;
    }

    private Response getApiTokenUsingEmail(String email, String password) {
        Response response = given()
                .contentType(ContentType.JSON)
                .get("/api/builtin-users/" + email + "/api-token?username=" + email + "&password=" + password);
        return response;
    }

    private Response getUserFromDatabase(String username) {
        Response getUserResponse = given()
                .get("/api/admin/authenticatedUsers/" + username + "/");
        return getUserResponse;
    }

    private static Response deleteUser(String username) {
        Response deleteUserResponse = given()
                .delete("/api/admin/authenticatedUsers/" + username + "/");
        return deleteUserResponse;
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

    private static String getPassword(String jsonStr) {
        String password = JsonPath.from(jsonStr).get(usernameKey);
        return password;
    }

    private static String getEmailFromUserName(String username) {
        return username + "@mailinator.com";
    }

}
