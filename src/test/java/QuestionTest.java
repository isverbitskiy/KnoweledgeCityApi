import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class QuestionTest extends BaseApiTest {

    @Test
    @Description("Verifies retrieving the next question for a registered user.")
    public void testNextQuestion() {
        resetUserState(staticEmail); // Reset state before the test

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "question")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString(QUESTIONS[0]))
                .body(containsString(QUESTION_IDS[0]))
                .body(containsString(ANSWERS[0]));
    }

    @Test
    @Description("Ensure that a 400 error is returned when the email parameter is missing.")
    public void testMissingEmailParameter() {
        baseRequest()
                .queryParam("action", "question")
                .when()
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
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Action parameter is missing"));
    }

    @Test
    @Description("Ensure that a 400 error is returned when the email format is invalid.")
    public void testInvalidEmailFormat() {
        baseRequest()
                .queryParam("email", "invalidemail")
                .queryParam("action", "question")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(400)
                .body(containsString("Error: Invalid email address"));
    }

    @Test
    @Description("Verify the correct message for no more questions when all are exhausted.")
    public void testNoMoreQuestionsAvailable() {
        resetUserState(staticEmail); // Reset state before the test

        // Retrieve questions until exhaustion
        for (int i = 0; i < 5; i++) { // Assume there are only 5 questions
            baseRequest()
                    .queryParam("email", staticEmail)
                    .queryParam("action", "question")
                    .post("/");
        }

        // Verify the message indicating that no more questions are available
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "question")
                .when()
                .post("/")
                .then()
                .log().all()
                .statusCode(200)
                .body(is("No more questions available"));
    }

    @Test
    @Description("Verifies the correct retrieval of the next question for a new user after successful login.")
    public void testNextQuestionWithNewUser() {
        String email = generateRandomEmail();
        // Register a new user
        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "login")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("You have successfully logged in"));

        // Retrieve the next question
        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "question")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString(QUESTIONS[0]))
                .body(containsString(QUESTION_IDS[0]))
                .body(containsString(ANSWERS[0]));
    }

    @Test
    @Description("Ensure that the Id field indicates a sequential order of questions for a registered user.")
    public void testQuestionOrder() {
        resetUserState(staticEmail); // Reset state before the test

        int previousId = -1; // Initial value before the first question
        boolean noMoreQuestions = false;

        while (!noMoreQuestions) {
            String response = baseRequest()
                    .queryParam("email", staticEmail)
                    .queryParam("action", "question")
                    .when()
                    .post("/")
                    .then()
                    .log().all()
                    .statusCode(200)
                    .extract()
                    .asString();

            if (response.contains("No more questions available")) {
                noMoreQuestions = true;
            } else {
                // Extract the Id from the response
                int currentId = extractQuestionId(response);

                // Verify that the current Id is greater than the previous one
                assert currentId > previousId : "Questions are out of order!";
                previousId = currentId;
            }
        }
    }

    @Test
    @Description("Verifies that the next question is returned correctly after answering the previous one successfully.")
    //Bug #6
    public void testGetNextQuestionAfterAnswer() {
        resetUserState(staticEmail);
        // Answer the first question
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[0])
                .queryParam("answer", ANSWERS[0])
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200);

        // Retrieve the next question
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "question")
                .when()
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString(QUESTIONS[1]))
                .body(containsString(QUESTION_IDS[1]))
                .body(containsString(ANSWERS[1]));
    }
}