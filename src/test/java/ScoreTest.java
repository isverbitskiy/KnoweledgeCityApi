import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

public class ScoreTest extends BaseApiTest {

    @Test
    @Description("Checks that the system correctly displays the current score for the registered user.")
    public void testDisplayCurrentScoreForRegisteredUser() {
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score:"));
    }

    @Test
    @Description("Checks that a 400 error is returned when the email parameter is missing.")
    public void testMissingEmailParameter() {
        baseRequest()
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(400)
                .body(containsString("Error: Email parameter is missing"));
    }

    @Test
    @Description("Checks that a 400 error is returned when the action parameter is missing.")
    public void testMissingActionParameter() {
        baseRequest()
                .queryParam("email", staticEmail)
                .when()
                .post("/")
                .then().log().all()
                .statusCode(400)
                .body(containsString("Error: Action parameter is missing"));
    }

    @Test
    @Description("Checks the score is displayed correctly for a new user after a successful login.")
    public void testDisplayScoreForNewUser() {
        String email = generateRandomEmail();
        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "login")
                .post("/")
                .then()
                .statusCode(200)
                .body(containsString("You have successfully logged in"));

        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score: 0"));
    }

    @Test
    @Description("Checks that the score is reset after calling reset.") // Bug #5
    public void testResetScore() {
        resetUserState(staticEmail); // Reset state before the test

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score: 0"));
    }

    @Test
    @Description("Checks that the score is displayed correctly after successfully answering several questions.")
    public void testDisplayScoreAfterAnsweringQuestions() {
        String response = baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .extract().asString();

        int currentScore = extractScore(response);

        // Submit the correct answer for the first question
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[0])
                .queryParam("answer", ANSWERS[0])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Submit the correct answer for the second question
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[1])
                .queryParam("answer", ANSWERS[1])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Verify that the score is displayed correctly
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString(String.format("Current score: %d", (currentScore + 2))));
    }
}