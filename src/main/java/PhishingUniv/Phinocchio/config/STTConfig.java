package PhishingUniv.Phinocchio.config;

import com.google.auth.oauth2.GoogleCredentials;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.cloud.ServiceOptions;

@Configuration
public class STTConfig {
    @Value("${spring.cloud.gcp.credentials.location}")
    private String credentialsLocation;

    @Bean
    public GoogleCredentials googleCredentials() throws Exception {
        return GoogleCredentials.fromStream(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(credentialsLocation)))
                .createScoped(ServiceOptions.getDefaultProjectId());
    }
}
