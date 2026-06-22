package com.rohitlokhande.service;


import com.rohitlokhande.exception.WalletException;
import com.rohitlokhande.model.Order;
import com.rohitlokhande.model.User;
import com.rohitlokhande.model.Wallet;

public interface WalletService {


    Wallet getUserWallet(User user) throws WalletException;

    public Wallet addBalanceToWallet(Wallet wallet, Long money) throws WalletException;

    public Wallet findWalletById(Long id) throws WalletException;

    public Wallet walletToWalletTransfer(User sender,Wallet receiverWallet, Long amount) throws WalletException;

    public Wallet payOrderPayment(Order order, User user) throws WalletException;



}
