package com.rohitlokhande.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.rohitlokhande.model.CoinDTO;
import com.rohitlokhande.response.ApiResponse;
import com.rohitlokhande.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ChatBotServiceImpl implements ChatBotService{

    @Value("${gemini.api.key}")
    private String API_KEY;

    private double convertToDouble(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
        }
    }

    public CoinDTO makeApiRequest(String currencyName) {
        System.out.println("coin name "+currencyName);
        String url = "https://api.coingecko.com/api/v3/coins/"+currencyName.toLowerCase();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();


        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> responseBody = responseEntity.getBody();
        if (responseBody != null) {
            Map<String, Object> image = (Map<String, Object>) responseBody.get("image");

            Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");

            CoinDTO coinInfo = new CoinDTO();
            coinInfo.setId((String) responseBody.get("id"));
            coinInfo.setSymbol((String) responseBody.get("symbol"));
            coinInfo.setName((String) responseBody.get("name"));
            coinInfo.setImage((String) image.get("large"));

            coinInfo.setCurrentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")));
            coinInfo.setMarketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")));
            coinInfo.setMarketCapRank((int) responseBody.get("market_cap_rank"));
            coinInfo.setTotalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")));
            coinInfo.setHigh24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")));
            coinInfo.setLow24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")));
            coinInfo.setPriceChange24h(convertToDouble(marketData.get("price_change_24h")) );
            coinInfo.setPriceChangePercentage24h(convertToDouble(marketData.get("price_change_percentage_24h")));
            coinInfo.setMarketCapChange24h(convertToDouble(marketData.get("market_cap_change_24h")));
            coinInfo.setMarketCapChangePercentage24h(convertToDouble( marketData.get("market_cap_change_percentage_24h")));
            coinInfo.setCirculatingSupply(convertToDouble(marketData.get("circulating_supply")));
            coinInfo.setTotalSupply(convertToDouble(marketData.get("total_supply")));

            return coinInfo;

        }
        return null;
    }



    public FunctionResponse getFunctionResponse(String prompt){
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get the coin details from given currency object\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency name, id, symbol.\"\n" +
                "              },\n" +
                "              \"currencyData\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"Currency Data id, symbol, name, image, current_price, market_cap, market_cap_rank, fully_diluted_valuation, total_volume, high_24h, low_24h, price_change_24h, price_change_percentage_24h, market_cap_change_24h, market_cap_change_percentage_24h, circulating_supply, total_supply, max_supply, ath, ath_change_percentage, ath_date, atl, atl_change_percentage, atl_date, last_updated.\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\", \"currencyData\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

        String responseBody = response.getBody();
        ReadContext ctx = JsonPath.parse(responseBody);

        try {
            // Try to extract function call
            String currencyName = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyName");
            String currencyData = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyData");
            String name = ctx.read("$.candidates[0].content.parts[0].functionCall.name");

            FunctionResponse res = new FunctionResponse();
            res.setCurrencyName(currencyName);
            res.setCurrencyData(currencyData);
            res.setFunctionName(name);

            System.out.println(name + " ------- " + currencyName + "-----" + currencyData);
            return res;

        } catch (PathNotFoundException e) {
            // Function call not found, check if there's a text response instead
            System.out.println("No function call found in response. Checking for text response...");
            try {
                String textResponse = ctx.read("$.candidates[0].content.parts[0].text");
                System.out.println("Gemini returned text instead of function call: " + textResponse);
                // Return null or throw custom exception to handle in calling method
                return null;
            } catch (PathNotFoundException ex) {
                System.err.println("Unexpected response format from Gemini API");
                System.err.println("Full response: " + responseBody);
                throw new RuntimeException("Unable to parse Gemini API response", ex);
            }
        }
    }




    @Override
    public ApiResponse getCoinDetails(String prompt) {

        FunctionResponse res = getFunctionResponse(prompt);

        // Check if function response is null (text response instead of function call)
        if (res == null) {
            // Handle case where Gemini didn't invoke the function
            ApiResponse ans = new ApiResponse();
            ans.setMessage("I couldn't find specific cryptocurrency information for that query. Please ask about a specific coin like 'What is the price of Bitcoin?' or 'Tell me about Ethereum'.");
            return ans;
        }

        String apiResponse = makeApiRequest(res.getCurrencyName()).toString();

        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"text\": \"" + prompt + "\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"model\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"functionCall\": {\n" +
                "            \"name\": \"getCoinDetails\",\n" +
                "            \"args\": {\n" +
                "              \"currencyName\": \"" + res.getCurrencyName() + "\",\n" +
                "              \"currencyData\": \"" + res.getCurrencyData() + "\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"function\",\n" +
                "      \"parts\": [\n" +
                "        {\n" +
                "          \"functionResponse\": {\n" +
                "            \"name\": \"getCoinDetails\",\n" +
                "            \"response\": {\n" +
                "              \"name\": \"getCoinDetails\",\n" +
                "              \"content\": " + apiResponse + "\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"tools\": [\n" +
                "    {\n" +
                "      \"functionDeclarations\": [\n" +
                "        {\n" +
                "          \"name\": \"getCoinDetails\",\n" +
                "          \"description\": \"Get crypto currency data from given currency object.\",\n" +
                "          \"parameters\": {\n" +
                "            \"type\": \"OBJECT\",\n" +
                "            \"properties\": {\n" +
                "              \"currencyName\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency Name, id, symbol .\"\n" +
                "              },\n" +
                "              \"currencyData\": {\n" +
                "                \"type\": \"STRING\",\n" +
                "                \"description\": \"The currency data id, symbol, current price, image, market cap extra... \"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\"currencyName\",\"currencyData\"]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, request, String.class);

        System.out.println("Response: " + response.getBody());
        ReadContext ctx = JsonPath.parse(response.getBody());

        String text = ctx.read("$.candidates[0].content.parts[0].text");
        ApiResponse ans = new ApiResponse();
        ans.setMessage(text);

        return ans;
    }

    @Override
    public CoinDTO getCoinByName(String coinName) {
        return this.makeApiRequest(coinName);
    }

    @Override
    public String simpleChat(String prompt) {

        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObject = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject textObject = new JSONObject();
        textObject.put("text", prompt);
        partsArray.put(textObject);
        contentsObject.put("parts", partsArray);
        contentsArray.put(contentsObject);
        requestBody.put("contents", contentsArray);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

        String responseBody = response.getBody();

        System.out.println("Response Body: " + responseBody);

        return responseBody;
    }
}