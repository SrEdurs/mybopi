package es.mybopi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = "braintree")
public class BraintreeConfig {

    @Value("${braintree.merchant-id}")
    private String merchantId;

    @Value("${braintree.public-key}")
    private String publicKey;

    @Value("${braintree.private-key}")
    private String privateKey;

}
