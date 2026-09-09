package com.tenderpilot.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {

        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();

    }


    @Bean
    SecretKey accessTokenSecretKey(
            SecurityProperties properties
    ) {

        byte[] secretBytes =
                properties
                        .accessTokenSecret()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        if (
                secretBytes.length < 32
        ) {

            throw new IllegalStateException(
                    "ACCESS_TOKEN_SECRET must contain at least 32 UTF-8 bytes"
            );

        }


        return new SecretKeySpec(
                secretBytes,
                "HmacSHA256"
        );

    }


    @Bean
    JwtEncoder jwtEncoder(
            SecretKey accessTokenSecretKey
    ) {

        return NimbusJwtEncoder
                .withSecretKey(
                        accessTokenSecretKey
                )
                .algorithm(
                        MacAlgorithm.HS256
                )
                .build();

    }


    @Bean
    JwtDecoder jwtDecoder(

            SecretKey accessTokenSecretKey,

            SecurityProperties properties

    ) {

        NimbusJwtDecoder decoder =

                NimbusJwtDecoder
                        .withSecretKey(
                                accessTokenSecretKey
                        )
                        .macAlgorithm(
                                MacAlgorithm.HS256
                        )
                        .build();


        decoder.setJwtValidator(

                JwtValidators
                        .createDefaultWithIssuer(
                                properties.issuer()
                        )

        );


        return decoder;

    }


    @Bean
    SecurityFilterChain securityFilterChain(

            HttpSecurity http,

            JwtDecoder jwtDecoder

    ) throws Exception {

        http

                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .cors(
                        Customizer.withDefaults()
                )

                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )

                .authorizeHttpRequests(
                        authorization ->
                                authorization

                                        .requestMatchers(
                                                HttpMethod.OPTIONS,
                                                "/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/actuator/health"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/api/v1/system/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                HttpMethod.POST,
                                                "/api/v1/auth/register",
                                                "/api/v1/auth/login",
                                                "/api/v1/auth/refresh",
                                                "/api/v1/auth/logout"
                                        )
                                        .permitAll()

                                        .anyRequest()
                                        .authenticated()
                )

                .oauth2ResourceServer(
                        resourceServer ->
                                resourceServer.jwt(
                                        jwt ->
                                                jwt
                                                        .decoder(
                                                                jwtDecoder
                                                        )
                                                        .jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter()
                                                        )
                                )
                );


        return http.build();

    }


    private Converter<
            Jwt,
            ? extends AbstractAuthenticationToken
            > jwtAuthenticationConverter() {

        return jwt -> {

            String role =
                    jwt.getClaimAsString(
                            "role"
                    );


            List<GrantedAuthority> authorities =

                    role == null
                            || role.isBlank()

                            ? List.of()

                            : List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            );


            String principalName =
                    jwt.getClaimAsString(
                            "email"
                    );


            if (
                    principalName == null
                    || principalName.isBlank()
            ) {

                principalName =
                        jwt.getSubject();

            }


            return new JwtAuthenticationToken(

                    jwt,

                    authorities,

                    principalName

            );

        };

    }

}
