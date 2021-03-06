package wp;

import shared.LogParserPage;
import com.jcraft.jsch.SftpException;
import com.triggmine.help.Parser;
import com.triggmine.mail.inspector.EmailReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by kruzhitskaya on 01.04.15.
 */
public class TestRegistrationPurchaseWp {
    private WebDriver driver;
    private String baseUrl = "http://wordpress.triggmine.videal.net/";
    private EmailReader emailReader = new EmailReader();
    private String login = "triggminefortest@gmail.com";
    private String password = "0508101626";
    private String regLogin;
    private String regPassword;
    private String filePathWp = "/home/qa/web/wordpress.triggmine.videal.net/public_html/wp-content/plugins/triggmine/core/logs/log.txt";
    private String buyerId;
    private String cartId;
    private String token;
    JSONObject jsonObject;
    HashMap jsonObjectHashMap = null;


    @BeforeTest
    public void deleteMessages() throws GeneralSecurityException {
        emailReader.deleteAllMessages(login, password);
    }

    @BeforeTest(dependsOnMethods = "deleteMessages")
    public void setUp() {
        driver = new FirefoxDriver();
        driver.get(baseUrl);
        driver.manage().deleteAllCookies();
    }

    @BeforeTest(dependsOnMethods = "setUp")
    public void setConnection() throws IOException {
        LogParserPage.setConnection(filePathWp);
    }

    @Test(priority = 1)
    public void testAddItem() throws IOException, ParseException, InterruptedException, SftpException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        AddDeleteItemsWpPage.addItem(driver);//add items

//CreateReplaceCartItem
        ArrayList<String> json = LogParserPage.readFile(filePathWp);
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCartItem")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        token = (String) jsonObjectHashMap.get("Token");//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCartItem");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        buyerId = (String) data.get("BuyerId");//get BuyerId
        cartId = (String) data.get("CartId");//getCartId
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"
    }


    @Test(priority = 2)
    public void testRegistration() throws InterruptedException, SftpException, IOException, ParseException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        RegistrationWpPage.registration(driver);//register

        ArrayList<String> json = LogParserPage.readFile(filePathWp);
//GetBuyerId
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("GetBuyerId")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "GetBuyerId");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//CreateReplaceBuyerInfo
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceBuyerInfo")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        assertTrue(data.containsKey("BuyerRegStart"));
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceBuyerInfo");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//CreateReplaceCart
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCart")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        Assert.assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        Assert.assertEquals(cartId, data.get("CartId"));//check "CartId"
        Assert.assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        Assert.assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCart");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        Assert.assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

    }

    @Test(priority = 3, timeOut = 120000)
    public void checkMailContent() {

        ArrayList<String> messageHtml = emailReader.readGmail(login, password);//check mail content
        regLogin = Parser.getCredentials(messageHtml.get(0), "Username: ");//get username
        regPassword = Parser.getCredentials(messageHtml.get(0), "Password: ");//get password
    }

    @Test(priority = 4)
    public void checkLogin() throws InterruptedException, SftpException, IOException, ParseException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        LoginLogoutWpPage.loginActionAfterRegistration(regLogin, regPassword, driver);//log in with new credentials

        ArrayList<String> json = LogParserPage.readFile(filePathWp);
//GetBuyerId
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("GetBuyerId")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "GetBuyerId");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//CreateReplaceBuyerInfo
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceBuyerInfo")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        assertTrue(data.containsKey("BuyerRegEnd"));
        assertTrue(data.containsKey("FirstName"));
        assertTrue(data.containsKey("LastName"));
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceBuyerInfo");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//CreateReplaceCart
        for (int i = 0; i < json.size(); i++) {
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCart")) {
                break;
            }
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        Assert.assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId"
        Assert.assertEquals(cartId, data.get("CartId"));//check "CartId"
        Assert.assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        Assert.assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCart");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        Assert.assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

    }

    @AfterTest
    public void tearDown() {
        driver.quit();
    }
}
