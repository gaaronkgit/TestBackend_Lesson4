package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest 
{
    static UserDTO user = new UserDTO();
    static BookingDTO bookingDTO = new BookingDTO();

    static Map<String, String> headers= new HashMap<>();
    static String token;
    static String bookingID;

    @BeforeAll
    public static void PrepareTest() throws IOException
    {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        ObjectMapper mapper = new ObjectMapper();
        user = mapper.readValue(GetJsonByFileName("user"), UserDTO.class);
        mapper = new ObjectMapper();
        bookingDTO = mapper.readValue(GetJsonByFileName("booking"), BookingDTO.class);

    }

    static String GetJsonByFileName(String name) throws IOException
    {
        String json = "";
        BufferedReader br = new BufferedReader(new FileReader("src/test/resources/" + name + ".json"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null)
            {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            json = sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            br.close();
        }
        return json;
    }

    @Order(1)
    @Test
    public void CheckServerUp()
    {
        given()
                .when()
                .get()
                .then()
                .statusCode(200);
    }

    @Order(2)
    @Test
    public void GetToken() throws IOException {
        String json = GetJsonByFileName("user");
        Response response =given()
                .header("Content-type", "application/json")
                .and()
                .body(json)
                .when()
                .post("/auth")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        token = response.jsonPath().getString("token");
        headers.put("Cookie","token=" + token);
    }

    @Order(3)
    @Test
    public void CreateBooking() throws IOException {
        String body = GetJsonByFileName("booking");
        Response response =given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .when()
                .post("/booking")
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        bookingID = response.jsonPath().getString("bookingid");
        headers.put("bookingid",bookingID);

    }

    @Order(4)
    @Test
    public void GetBooking()
    {
        given()
                .when()
                .get("/booking/" + bookingID)
                .then()
                .statusCode(200);
    }

    @Order(5)
    @Test
    public void UpdateBooking() throws IOException {
        String body = GetJsonByFileName("booking");;
        Response response = given()
                .headers(headers)
                .and()
                .body(body)
                .when()
                .patch("/booking/"+bookingID)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    }

    @Order(5)
    @Test
    public void DeleteBooking()
    {
        Response response = given()
                .headers(headers)
                .when()
                .delete("/booking/"+bookingID)
                .then()
                .extract().response();

        Assertions.assertEquals(201, response.statusCode());
    }


}
