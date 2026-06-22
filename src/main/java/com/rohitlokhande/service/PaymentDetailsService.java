package com.rohitlokhande.service;

import com.rohitlokhande.model.PaymentDetails;
import com.rohitlokhande.model.User;

public interface PaymentDetailsService {
    public PaymentDetails addPaymentDetails( String accountNumber,
                                             String accountHolderName,
                                             String ifsc,
                                             String bankName,
                                             User user
    );

    public PaymentDetails getUsersPaymentDetails(User user);


}
