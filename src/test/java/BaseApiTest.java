import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.given;

public class BaseApiTest {
    protected static final String[] QUESTIONS = {
            "Why did the QA engineer go to the bar?",
            "How many QA engineers does it take to change a light bulb?",
            "Did the QA engineer enjoy their last bug hunt?",
            "Why did the QA engineer drown in the pool?",
            "Is it possible for a QA engineer to have too much coffee?"
    };

    protected static final String[] ANSWERS = {
            "To test the bartender's skills",
            "42",
            "true",
            "Because they didn't receive the 'float' property!",
            "false"
    };
    protected static final String[] QUESTION_IDS = {"0", "1", "2", "3", "4"};
    protected static Faker faker = new Faker();
    protected final String BASE_URL = "https://qa-test.kcdev.pro";
    protected final String staticEmail = "niwatarou@gmail.com";

    protected static io.restassured.specification.RequestSpecification baseRequest() {
        return given().contentType("application/x-www-form-urlencoded");
    }

    @BeforeEach
    public void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    protected String generateRandomEmail() {
        return faker.internet().emailAddress();
    }

    protected void resetUserState(String email) {
        baseRequest()
                .queryParam("email", email)
                .queryParam("action", "reset")
                .post("/")
                .then()
                .log().all()
                .statusCode(200);
    }

    protected int extractScore(String response) {
        String scorePrefix = "Current score: ";
        int index = response.indexOf(scorePrefix);
        if (index != -1) {
            return Integer.parseInt(response.substring(index + scorePrefix.length()).trim());
        }
        throw new IllegalStateException("Score not found in response");
    }

    protected int extractQuestionId(String response) {
        String[] lines = response.split("\n");
        for (String line : lines) {
            if (line.startsWith("Id:")) {
                return Integer.parseInt(line.replace("Id:", "").trim());
            }
        }
        throw new IllegalStateException("Id not found in response");
    }
}


