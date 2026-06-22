package com.rohitlokhande.service;

import com.rohitlokhande.model.Coin;
import com.rohitlokhande.model.User;
import com.rohitlokhande.model.Watchlist;

public interface WatchlistService {

    Watchlist findUserWatchlist(Long userId) throws Exception;

    Watchlist createWatchList(User user);

    Watchlist findById(Long id) throws Exception;

    Coin addItemToWatchlist(Coin coin,User user) throws Exception;
}
