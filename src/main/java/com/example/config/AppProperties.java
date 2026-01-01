package com.example.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "")
public class AppProperties {

    private final Server server = new Server();
    private final Datasource datasource = new Datasource();
    private final Redis redis = new Redis();
    private final Jwt jwt = new Jwt();
    private final Google google = new Google();

    @Getter @Setter
    public static class Server {
        @NotNull
        private Integer port;
    }

    @Getter @Setter
    public static class Datasource {
        @NotBlank
        private String host;

        @NotNull
        private Integer port;

        @NotBlank
        private String name;

        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }

    @Getter @Setter
    public static class Redis {
        @NotBlank
        private String host;

        @NotNull
        private Integer port;
    }

    @Getter @Setter
    public static class Jwt {
        @NotBlank
        private String secret;

        @NotBlank
        private String refreshSecret;

        @NotNull
        private Long expiration;

        @NotNull
        private Long refreshExpiration;
    }

    @Getter @Setter
    public static class Google {
        @NotBlank
        private String clientId;

        @NotBlank
        private String clientSecret;

        @NotBlank
        private String redirectUri;
    }

    @Getter @Setter
    public static class MagicLink{
        @NotNull
        private Long expiration;
    }
}
