import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static com.github.javafaker.Faker.instance;
import static org.hamcrest.Matchers.containsString;

public class LoginTest extends BaseApiTest {

    @Test
    @Description("Login attempt with an already registered email.")
    public void testExistingUserLogin() {
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: User is already logged in"));
    }

    @Test
    @Description("Test for invalid email format (missing top-level domain).")
    public void testInvalidEmailDomainFormat() {
        baseRequest()
                .queryParam("email", "testtest.test")
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Test for invalid email format (missing dot in domain).")
    public void testInvalidEmailDotFormat() {
        baseRequest()
                .queryParam("email", "test@testtest")
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Missing email parameter.")
    public void testMissingEmailParameter() {
        baseRequest()
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Email parameter is missing"));
    }

    @Test
    @Description("Missing action parameter.")
    public void testMissingActionParameter() {
        String email = generateRandomEmail();

        baseRequest()
                .queryParam("email", email)
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Action parameter is missing"));
    }

    @Test
    @Description("Successful registration of a new user.")
    public void testNewUserRegistration() {
        String email = generateRandomEmail();

        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(200)
                .body(containsString("You have successfully logged in"));
    }

    @Test
    @Description("Excessive email length.")
    public void testExcessiveEmailLength() {
        String username = "a".repeat(256);
        String domain = instance().internet().domainName();
        String email = username + "@" + domain;

        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Test for invalid email format (incorrect characters).")
    public void testInvalidCharEmailFormat() {
        baseRequest()
                .queryParam("email", "test\"123\"@test.test")
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Empty email.")
    public void testEmptyEmail() {
        baseRequest()
                .queryParam("email", "")
                .queryParam("action", "login")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Invalid action parameter.")
    public void testInvalidAction() {
        String email = generateRandomEmail();
        baseRequest()
                .queryParam("email", email)
                .queryParam("action", email)
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid action"));
    }
}