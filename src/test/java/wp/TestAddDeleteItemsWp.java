package wp;

import org.testng.Assert;
import shared.LogParserPage;
import com.jcraft.jsch.SftpException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.Assert.*;

/**
 * Created by kruzhitskaya on 31.03.15.
 */
public class TestAddDeleteItemsWp {
    private WebDriver driver;
    private String baseUrl = "http://wordpress.triggmine.videal.net/";
    private String filePathWp = "/home/qa/web/wordpress.triggmine.videal.net/public_html/wp-content/plugins/triggmine/core/logs/log.txt";
    private String buyerId;
    private String cartId;
    private String token;
    JSONObject jsonObject;
    HashMap jsonObjectHashMap = null;

    @BeforeTest
    public void setUp() {
        driver = new FirefoxDriver();
        driver.get(baseUrl);
        driver.manage().deleteAllCookies();
    }

    @BeforeTest(dependsOnMethods = "setUp")
    public void setConnection () throws IOException {
        LogParserPage.setConnection(filePathWp);
    }

    @Test(priority = 1)
    public void testAddItem() throws InterruptedException, SftpException, IOException, ParseException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        AddDeleteItemsWpPage.addItem(driver);//add item

//CreateReplaceCart
        ArrayList<String> json = LogParserPage.readFile(filePathWp);
        for (int i=0; i < json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCart"))
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
    public void testUpdateItem() throws IOException, InterruptedException, ParseException, SftpException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        AddDeleteItemsWpPage.addItem(driver);//update cart
        Thread.sleep(5000);

        ArrayList<String> json = LogParserPage.readFile(filePathWp);
//CreateReplaceCartItem
        for (int i=0; i < json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCartItem"))
            {break;}
        }

        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(data.get("ReplaceOnly").toString(), "1");//check item is updated
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId" in the same
        assertEquals(cartId, data.get("CartId"));//check "CartId" in the same
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCartItem");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"
    }

    @Test(priority = 3)
    public void testDeleteItem() throws IOException, ParseException, InterruptedException, SftpException {
        LogParserPage.removeFile(filePathWp);//remove log.txt
        AddDeleteItemsWpPage.deleteItem(driver);//clear cart

        ArrayList<String> json = LogParserPage.readFile(filePathWp);
//CreateReplaceCart
        for (int i=0; i < json.size(); i++){
            jsonObject = LogParserPage.getJson(json.get(i));
            jsonObjectHashMap = (HashMap) jsonObject.get("Request");
            if(jsonObjectHashMap.get("Method").toString().contentEquals("CreateReplaceCart"))
            {break;}
        }

        jsonObjectHashMap = (HashMap) jsonObject.get("Request");
        JSONObject data = (JSONObject) jsonObjectHashMap.get("Data");
        assertEquals(buyerId, data.get("BuyerId"));//check "BuyerId" in the same
        assertEquals(cartId, data.get("CartId"));//check "CartId" in the same
        assertEquals(token, jsonObjectHashMap.get("Token"));//check Token is the same for each action
        assertEquals(jsonObjectHashMap.get("Method"), "CreateReplaceCart");//check proper method is sent

        jsonObjectHashMap = (HashMap) jsonObject.get("Response");
        assertEquals(jsonObjectHashMap.get("ErrorCode").toString(), "0");//check "ErrorCode" is "0"
    }

    @AfterTest
    public void tearDown() throws Exception {
        driver.quit();
    }
}
