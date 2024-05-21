package es.mybopi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.Environment;

@Service
public class PaymentService {

    @Autowired
    private BraintreeConfig config;

    public BraintreeGateway getGateway() {
        return new BraintreeGateway(
            Environment.SANDBOX,
            config.getMerchantId(),
            config.getPublicKey(),
            config.getPrivateKey()
        );
    }

    public String getToken() {
        ClientTokenRequest clientTokenRequest = new ClientTokenRequest();
        return getGateway().clientToken().generate(clientTokenRequest);
    }

}
