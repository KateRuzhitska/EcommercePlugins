package magento;

import com.jcraft.jsch.SftpException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import shared.LogParserPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.*;


/**
 * Created by kruzhitskaya on 30.03.15.
 */
public class TestAddItemsLoginMgn {
    private WebDriver driver;
    private String baseUrl = "http://magento.triggmine.videal.net";
    private String filePathMg = "/home/qa/web/magento.triggmine.videal.net/public_html/app/code/community/Videal/Triggmine/Model/core/logs/log.txt";
    private String buyerId;
    private String cartId;
    private String token;
    JSONObject jsonObject;
    HashMap jsonObjectHashMap = null;

    @BeforeTest
    public void setUp() {
        driver = new FirefoxDriver();
        driver.manage().deleteAllCookies();
        driver.get(baseUrl + "/index.php/bags.html");
    }

    @BeforeTest(dependsOnMethods = "setUp")
    public void setConnection () throws IOException {
        LogParserPage.setConnection(filePathMg);
    }

    @Test(priority = 1)
    public void testAddItemsLogin() throws InterruptedException, SftpException, IOException, ParseException {
        LogParserPage.removeFile(filePathMg);//remove log.txt
        AddDeleteItemsMgnPage.addItem(driver);//add item to the cart

        //CreateReplaceCart
        ArrayList<String> json = LogParserPage.readFile(filePathMg);
        for (int i=0; i < json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCartItem"))
            {break;}
        }

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        buyerId = (String) data.get("BuyerId");//get BuyerId in response
        cartId = (String) data.get("CartId");//get CartId in response

        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCartItem");//check proper method is sent
        token = (String) (jsonObjectHashMap.get("Token"));//check Token is the same for each action
    }

    @Test(priority = 2)
    public void testLogIn() throws InterruptedException, SftpException, IOException, ParseException {
        LogParserPage.removeFile(filePathMg);//remove log.txt
        LoginLogoutMgnPage.logInAction("triggmine01@gmail.com", "0508101626", driver);//log in

        ArrayList<String> json = LogParserPage.readFile(filePathMg);

//CreateReplaceCart
        for(int i=0; i<json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if (jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCart"))
                {break;}
        }

        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId" is the same
        assertEquals(cartId, data.get("CartId"));//check "CartId" is the same
        assertEquals(token, jsonObjectHashMap.get("Token"));//check "Token" is the same
        assertEquals(jsonObjectHashMap.get("Method").toString(), "CreateReplaceCart");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//CreateReplaceBuyerInfo
        for(int i=0; i<json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceBuyerInfo"))
            {break;}
        }

        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId" is the same
        assertEquals(token, jsonObjectHashMap.get("Token"));//check "Token" is the same
        assertEquals(data.get("BuyerEmail").toString(), "triggmine01@gmail.com");//check buyer email
        assertEquals(data.get("BuyerRegEnd").toString(), "2015-02-06 13:14:13");//check the end of registration
        assertEquals(jsonObjectHashMap.get("Method").toString(), "CreateReplaceBuyerInfo");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

//GetBuyerId
        for(int i=0; i<json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("GetBuyerId"))
            {break;}
        }
        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId" is the same
        assertEquals(token, jsonObjectHashMap.get("Token"));//check "Token" is the same
        assertEquals(data.get("BuyerEmail").toString(), "triggmine01@gmail.com");//check buyer email
        assertEquals(jsonObjectHashMap.get("Method").toString(), "GetBuyerId");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"

    }

    @AfterTest(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }


}
