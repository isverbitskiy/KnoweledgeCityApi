import io.qameta.allure.Description;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;

public class AnswerTest extends BaseApiTest {

    @Test
    @Description("Verifies submitting the correct answer for an existing question.")
    public void testSubmitValidAnswer() {

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("answer", ANSWERS[0])
                .queryParam("question_id", QUESTION_IDS[0])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));
    }

    @Test
    @Description("Ensure that a 400 error is returned when the questionId parameter is missing.")
    public void testMissingQuestionId() {
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("answer", ANSWERS[2])
                .post("/")
                .then().log().all()
                .statusCode(400)
                .body(containsString("Error: Question ID parameter is missing"));
    }

    @Test
    @Description("Verifies handling of incorrect answer format (e.g., empty answer).")
    public void testInvalidAnswerFormat() {
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[1])
                .queryParam("answer", "")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Incorrect answer"));
    }

    @Test
    @Description("Verifies handling of missing answer.") // Bug #1
    public void testMissingAnswerFormat() {
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[1])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Incorrect answer"));
    }

    @Test
    @Description("Ensure that a 400 error is returned for a non-existent question.") // Bug #2
    public void testNonExistentQuestion() {
        int nonExistentQuestionId = 9999;

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", nonExistentQuestionId)
                .queryParam("answer", ANSWERS[0])
                .post("/")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    @Description("Verifies submitting an answer to a question as a new user.")
    public void testSubmitAnswerWithNewUser() {
        String newUserEmail = generateRandomEmail();

        // Create a new user
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "login")
                .post("/")
                .then().statusCode(200)
                .body(containsString("You have successfully logged in"));

        // Submit an answer to an existing question
        baseRequest()
                .queryParam("email", newUserEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[1])
                .queryParam("answer", ANSWERS[1])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));
    }

    @Test
    @Description("Verifies that the system correctly interprets true/false, yes/no answers.") // Bug #3
    public void testSubmitBooleanAnswer() {
        int questionId = 2; // Assume a question with this ID exists
        String answerYes = "yes";
        String answerFalse = "false";
        String answerNo = "no";

        // Correct answer "true"
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[2])
                .queryParam("answer", ANSWERS[2])
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Correct answer "yes"
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[2])
                .queryParam("answer", answerYes)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Incorrect answer "false"
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[2])
                .queryParam("answer", answerFalse)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Incorrect answer"));

        // Incorrect answer "no"
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[2])
                .queryParam("answer", answerNo)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Incorrect answer"));
    }

    @Test
    @Description("Verifies handling of numeric answers.")
    public void testSubmitNumericAnswer() {
        int numericAnswer = 42;

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", QUESTION_IDS[1])
                .queryParam("answer", numericAnswer)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));
    }

    @Test()
    @Description("Verifies that the system does not increase the score twice for the same question.")
    public void testSubmitDuplicateAnswer() {
        int questionId = 0; // Assume a question with this ID exists
        String validAnswer = "To test the bartender's skills";

        // Retrieve initial score
        String initialScoreResponse = baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .extract()
                .asString();

        int initialScore = extractScore(initialScoreResponse);

        // Submit the correct answer for the first time
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", questionId)
                .queryParam("answer", validAnswer)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Correct answer"));

        // Retrieve the score after the first correct answer
        String firstAnswerScoreResponse = baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .extract()
                .asString();

        int firstAnswerScore = extractScore(firstAnswerScoreResponse);

        // Verify that the score increased by 1
        assert firstAnswerScore == initialScore + 1 : "Score did not increase correctly after the first answer";

        // Submit the same answer again
        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", questionId)
                .queryParam("answer", validAnswer)
                .post("/")
                .then().log().all()
                .statusCode(200);

        // Retrieve the score after the second (duplicate) answer
        String secondAnswerScoreResponse = baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "score")
                .post("/")
                .then().log().all()
                .statusCode(200)
                .extract()
                .asString();

        int secondAnswerScore = extractScore(secondAnswerScoreResponse);

        // Verify that the score remains the same after the second (duplicate) answer
        assert secondAnswerScore == firstAnswerScore : "Score increased after duplicate answer";
    }

    @Test
    @Description("Verifies that the system does not count an incorrect numeric answer.")
    public void testIncorrectNumericAnswer() {
        int questionId = 3; // Assume a question with this ID exists
        String incorrectNumericAnswer = "100";

        baseRequest()
                .queryParam("email", staticEmail)
                .queryParam("action", "answer")
                .queryParam("question_id", questionId)
                .queryParam("answer", incorrectNumericAnswer)
                .post("/")
                .then().log().all()
                .statusCode(200)
                .body(containsString("Incorrect answer"));
    }
}