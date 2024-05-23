package es.mybopi.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Token;
import es.mybopi.dto.StripeTokenDto;
import es.mybopi.dto.StripeChargeDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init(){
        Stripe.apiKey = stripeApiKey;
    }

    public StripeTokenDto createCardToken(StripeTokenDto model){

        try{
            Map<String, Object> card = new HashMap<>();
            card.put("number", model.getCardNumber());
            card.put("exp_month", Integer.parseInt(model.getExpMonth()));
            card.put("exp_year", Integer.parseInt(model.getExpYear()));
            card.put("cvc", model.getCvc());
            Map<String, Object> params = new HashMap<>();
            params.put("card", card);
            
            Token token = Token.create(params);
            if(token != null && token.getId() != null){
                model.setSuccess(true);
                model.setToken(token.getId());
            }
            return model;
        }catch(StripeException e){
            log.error("StripeService (createCardToken): ", e);
            throw new RuntimeException(e.getMessage());
        }

    }


    public StripeChargeDto charge(StripeChargeDto chargeRequest) {
        try {
            chargeRequest.setSuccess(false);
            Map<String, Object> chargeParams = new HashMap<>();
    
            // Convertir la cantidad a double antes de multiplicarla por 100
            double amount = Double.parseDouble(chargeRequest.getAmount());
            chargeParams.put("amount", (int) (amount * 100)); // Convertir el resultado a int
            chargeParams.put("currency", "EUR");
            chargeParams.put("description", "Payment for id " + chargeRequest.getAdditionalInfo().getOrDefault("ID_TAG", ""));
            chargeParams.put("source", chargeRequest.getStripeToken());
            Map<String, Object> metaData = new HashMap<>();
            metaData.put("id", chargeRequest.getChargeId());
            metaData.putAll(chargeRequest.getAdditionalInfo());
            chargeParams.put("metadata", metaData);
            Charge charge = Charge.create(chargeParams);
            chargeRequest.setMessage(charge.getOutcome().getSellerMessage());
    
            if (charge.getPaid()) {
                chargeRequest.setChargeId(charge.getId());
                chargeRequest.setSuccess(true);
            }
            return chargeRequest;
            
        } catch (StripeException e) {
            log.error("StripeService (charge)", e);
            throw new RuntimeException(e.getMessage());
        } catch (NumberFormatException e) {
            // Manejar la excepción si la cadena de la cantidad no tiene un formato numérico válido
            log.error("Formato de cantidad no válido", e);
            // Puedes devolver una respuesta indicando que el formato de la cantidad no es válido
            StripeChargeDto errorResponse = new StripeChargeDto();
            return errorResponse;
        }
    }
    
    

}
