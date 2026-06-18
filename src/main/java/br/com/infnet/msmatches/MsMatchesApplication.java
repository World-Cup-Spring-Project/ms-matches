package br.com.infnet.msmatches;

import br.com.infnet.msmatches.config.MatchesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MatchesProperties.class)
public class MsMatchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsMatchesApplication.class, args);
    }

}
