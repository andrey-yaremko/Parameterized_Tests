package com.greencity.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreenCitySigninTest {

    @FindBy(css = "a.ubs-header-sign-in")
    private WebElement signInButton;
    @FindBy(css = "#email")
    private WebElement emailInput;
    @FindBy(css = "#password")
    private WebElement passwordInput;
    @FindBy(css = "button.ubsStyle")
    private WebElement signInSubmitButton;
    @FindBy(css = ".alert-general-error")
    private WebElement generalErrorMessage;
    @FindBy(id = "pass-err-msg")
    private WebElement errorPasswordMessage;
    @FindBy(id = "email-err-msg")
    private WebElement errorEmailMessage;
    @FindBy(css = "li.ubs-user-name")
    private WebElement profileName;
    @FindBy(css = "li[aria-label='sign-out']")
    private WebElement logoutButton;
    @FindBy(css = "h1[_ngcontent-ng-c4235776424]")
    private WebElement welcomeText;
    @FindBy(css = "h2[_ngcontent-ng-c4235776424]")
    private WebElement signInDetailsText;
    @FindBy(css = "label[for='email']")
    private WebElement emailLabel;

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @BeforeEach
    public void setupTest() throws InterruptedException {
        driver.get("https://www.greencity.cx.ua/#/ubs");
        PageFactory.initElements(driver, this);
        try {
            if (isElementPresent(signInButton)) {
                wait.until(ExpectedConditions.elementToBeClickable(signInButton)).click();
                wait.until(ExpectedConditions.elementToBeClickable(emailInput));
            }
        } catch (Exception e) {
            driver.navigate().refresh();
        }
    }

    private boolean isElementPresent(WebElement element) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(1)).until(ExpectedConditions.visibilityOf(element));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @AfterEach
    public void tearDownTest() throws InterruptedException {
        try {
            if (isElementPresent(profileName)) {
                wait.until(ExpectedConditions.elementToBeClickable(profileName)).click();
                wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();
                wait.until(ExpectedConditions.visibilityOf(signInButton));
            }
        } catch (Exception e) {
            driver.navigate().refresh();
            try { wait.until(ExpectedConditions.visibilityOf(signInButton)); } catch (Exception ignored) {}
        }
    }

    // --- ТЕСТИ ---

    @Test
    @DisplayName("1. Перевірка заголовку сторінки")
    public void verifyTitle() {
        Assertions.assertEquals("Зручний сервіс pick-up від УБС — Швидка допомога з переробкою", driver.getTitle());
    }

    @Test
    @DisplayName("2. Перевірка елементів UI у формі входу (UA)")
    public void verifySigninPopupElements() {
        wait.until(ExpectedConditions.visibilityOf(welcomeText));
        wait.until(ExpectedConditions.visibilityOf(signInDetailsText));
        wait.until(ExpectedConditions.visibilityOf(emailLabel));

        assertThat(welcomeText.getText(), is("З поверненням!"));
        assertThat(signInDetailsText.getText(), is("Будь ласка, внесiть свої дані для входу."));
        assertThat(emailLabel.getText(), is("Електронна пошта"));
        assertTrue(emailInput.isDisplayed(), "Поле Email має бути видимим");
        assertTrue(passwordInput.isDisplayed(), "Поле Пароль має бути видимим");
        assertTrue(signInSubmitButton.isDisplayed(), "Кнопка 'Увійти' має бути видимою");
    }

    @ParameterizedTest(name = "Вхід з незареєстрованими даними: {0}")
    @CsvSource(value = {
            "samplestest@greencity.com, weyt3$Guew^",
            "nonexistent@example.com, WrongPassword123!"
    })
    @DisplayName("3. Вхід з незареєстрованим користувачем (негативний)")
    public void signIn_UnregisteredUser(String email, String password) throws InterruptedException {
        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        signInSubmitButton.click();
        wait.until(ExpectedConditions.visibilityOf(generalErrorMessage));
        assertThat(generalErrorMessage.getText(), is("Введено невірний email або пароль"));
    }

    @ParameterizedTest(name = "Вхід з невалідним email: {0}")
    @CsvSource(value = {
            "samplestesgreencity.com, 'Перевірте коректність введеної електронної адреси'",
            "test@.com,              'Перевірте коректність введеної електронної адреси'",
            "@domain.com,            'Перевірте коректність введеної електронної адреси'"
    })
    @DisplayName("4. Вхід з невалідним форматом Email (негативний)")
    public void signIn_InvalidEmailFormat(String email, String message) throws InterruptedException {
        emailInput.sendKeys(email);
        passwordInput.sendKeys("dummyPassword");
        signInSubmitButton.click();
        wait.until(ExpectedConditions.visibilityOf(errorEmailMessage));
        assertThat(errorEmailMessage.getText(), is(message));
    }

    @ParameterizedTest(name = "Вхід з невалідним паролем (довжина): {0}")
    @CsvSource(value = {

            "123, 'Пароль повинен мати від 8 до 20 символів без пробілів, містити хоча б один символ латинського алфавіту верхнього (A-Z) та нижнього регістру (a-z), число (0-9) та спеціальний символ (~`!@#$%^&*()+=_-{}[]|:;”’?/<>,.)'",
            "short, 'Пароль повинен мати від 8 до 20 символів без пробілів, містити хоча б один символ латинського алфавіту верхнього (A-Z) та нижнього регістру (a-z), число (0-9) та спеціальний символ (~`!@#$%^&*()+=_-{}[]|:;”’?/<>,.)'"

    })
    @DisplayName("5a. Вхід з невалідним форматом пароля (довжина)")
    public void signIn_InvalidPasswordLength(String password, String expectedDetailedErrorMessage) throws InterruptedException {
        emailInput.sendKeys("test@greencity.com");
        passwordInput.sendKeys(password);
        wait.until(ExpectedConditions.visibilityOf(welcomeText));
        welcomeText.click();
        wait.until(ExpectedConditions.visibilityOf(errorPasswordMessage));
        assertThat(errorPasswordMessage.getText(), is(expectedDetailedErrorMessage));
    }

    @ParameterizedTest(name = "Вхід з невалідним паролем (символи): {0}")
    @CsvSource(value = {
            "ValidPass1, 'Введено невірний email або пароль'"
    })
    @DisplayName("5b. Вхід з невалідним форматом пароля (символи, submit)")
    public void signIn_InvalidPasswordSubmit(String password, String expectedGeneralErrorMessage) throws InterruptedException {
        emailInput.sendKeys("test@greencity.com");
        passwordInput.sendKeys(password);
        signInSubmitButton.click();
        wait.until(ExpectedConditions.visibilityOf(generalErrorMessage));
        assertThat(generalErrorMessage.getText(), is(expectedGeneralErrorMessage));
    }

    @Test
    @DisplayName("6. Вхід з порожніми полями (негативний)")
    public void signIn_EmptyFields() throws InterruptedException {
        emailInput.click();
        passwordInput.click();
        signInSubmitButton.click();
        wait.until(ExpectedConditions.visibilityOf(errorEmailMessage));
        wait.until(ExpectedConditions.visibilityOf(errorPasswordMessage));
        assertThat(errorEmailMessage.getText(), is("Введіть пошту."));
        assertThat(errorPasswordMessage.getText(), is("Це поле є обов'язковим"));
    }

    @ParameterizedTest(name = "Вхід з валідними даними: {0}")
    @CsvSource(value = {
            "Typicm@gmail.com, wM1&rsX9!u",
    })
    @DisplayName("7. Вхід з валідними даними (позитивний)")
    public void signIn_Positive(String email, String password) {
        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        signInSubmitButton.click();

        boolean signInButtonVanished = wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("a.ubs-header-sign-in")));
        assertTrue(signInButtonVanished, "Кнопка 'Sign In' повинна зникнути після успішного входу для користувача: " + email);
    }

    @AfterAll
    public static void tearDown() throws InterruptedException {
        Thread.sleep(3000);
        if (driver != null) {
            driver.quit();
        }
    }
}