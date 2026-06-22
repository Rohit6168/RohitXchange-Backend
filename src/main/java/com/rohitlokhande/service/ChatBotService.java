package com.rohitlokhande.service;

import com.rohitlokhande.model.CoinDTO;
import com.rohitlokhande.response.ApiResponse;

public interface ChatBotService {
    ApiResponse getCoinDetails(String coinName);

    CoinDTO getCoinByName(String coinName);

    String simpleChat(String prompt);
}
