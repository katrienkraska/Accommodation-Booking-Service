package org.example.testutil;

import org.example.model.payment.Payment;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentTestUtil {

    public static Payment getExpectedPayment() {

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setSessionId("testSessionId1");
        payment.setSessionUrl("session/url");
        payment.setAmountToPay(BigDecimal.valueOf(3000).setScale(2, RoundingMode.HALF_UP));

        return payment;
    }
}
