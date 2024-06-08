import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

public class ResetTest extends BaseApiTest {

    @Test
    @Description("Ensures that the system resets the score and current question state for a registered user.")
    public void testResetScoreAndStateForRegisteredUser() {
        // Submit a valid answer to increase the score
        int questionId = 0; // Assume a question with this ID exists
        String questionText = "Why did the QA engineer go to the bar?";
        String validAnswer = "To test the bartender's skills";

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", questionId)
                .queryParam("answer", validAnswer)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Check that the score increased
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .extract().asString();

        // Perform reset
        resetUserState(staticEmail);

        // Verify that the score is reset
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score: 0"));

        // Verify that the first question is returned after reset
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "question")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString(questionText))
                .body(containsString("Id: 0"))
                .body(containsString(validAnswer));
    }

    @Test
    @Description("Ensure that a 400 error is returned when the email parameter is missing.")
    public void testMissingEmailParameter() {
        baseRequest()
                .queryParam("action", "reset")
                .post("/")
                .then().log().all()
                .statusCode(400)
                .body(containsString("Error: Email parameter is missing"));
    }

    @Test
    @Description("Ensure that a 400 error is returned when the action parameter is missing.")
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
    @Description("Ensures that the state is properly reset for a new user after login.")
    public void testResetStateForNewUser() {
        String newUserEmail = generateRandomEmail();

        // Register a new user
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "login")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("You have successfully logged in"));

        // Verify the initial score is 0
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score: 0"));

        // Perform reset
        resetUserState(newUserEmail);

        // Verify that the score remains 0
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Current score: 0"));

        // Verify that the first question is returned after reset
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "question")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Question: Why did the QA engineer go to the bar?"))
                .body(containsString("Id:0"))
                .body(containsString("Answer: To test the bartender's skills"));
    }
}