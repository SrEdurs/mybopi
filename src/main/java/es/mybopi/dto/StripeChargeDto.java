package es.mybopi.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class StripeChargeDto {

    private String stripeToken;
    private String username;
    private String amount;
    private boolean success;
    private String message;
    private String chargeId;
    private Map<String, Object> additionalInfo = new HashMap<>();
}
