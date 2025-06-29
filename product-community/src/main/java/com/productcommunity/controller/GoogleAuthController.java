package com.productcommunity.controller;

import com.productcommunity.enums.ERole;
import com.productcommunity.model.Role;
import com.productcommunity.model.User;
import com.productcommunity.repository.RoleRepository;
import com.productcommunity.repository.UserRepository;
import com.productcommunity.security.jwt.JwtUtil;
import com.productcommunity.security.user.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/auth/google")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Google Oauth2.0 APIs", description = "google auth related APIs")
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;


    private final RestTemplate restTemplate;


    private final UserDetailsServiceImpl userDetailsService;


    private final PasswordEncoder passwordEncoder;


    private final UserRepository userRepository;


    private final RoleRepository roleRepository;


    private final JwtUtil jwtUtil;

    @GetMapping("/auth-url")
    public ResponseEntity<String> getGoogleAuthUrl() {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=email%20profile" +
                "&access_type=offline" +
                "&prompt=consent"; // Ensures consent screen shows every time

        return ResponseEntity.ok(authUrl);
    }

    @GetMapping("/callback")
    @Operation(summary = "google OAuth2.0")
    public ResponseEntity<?> handleGoogleCallbackUsingPlayground(@RequestParam String code) {
        try {
            // get auth code from this uri : https://developers.google.com/oauthplayground/
            // put this uri in Authorize APIs input bar : https://www.googleapis.com/auth/userinfo.email
            // then in top right corner click setting and check Use your own OAuth credentials
            // then provide there client id and client secret in input bar and click on close
            // and then click authorize api and then you will get Authorization code and
            // then use that code as a request parameter.

            String tokenEndpoint = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            //params.add("redirect_uri", "https://developers.google.com/oauthplayground");
            //params.add("redirect_uri", "http://localhost:9191/auth/google/callback");
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            // Exchange code for tokens
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);
            String idToken = (String) tokenResponse.getBody().get("id_token");


            //String userInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            //ResponseEntity<Map> userInfoResponse = restTemplate.getForEntity(userInfoUrl, Map.class);

            // Get user info using access token (more reliable than id_token)
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.set("Authorization", "Bearer " + accessToken);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(userInfoHeaders),
                    Map.class
            );


            if (userInfoResponse.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> userInfo = userInfoResponse.getBody();
                String email = (String) userInfo.get("email");
                UserDetails userDetails = null;
                User savedUser = null;
                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                    savedUser = userRepository.findByUserName(email);
                } catch (Exception e) {
                    User user = new User();
                    user.setFirstName((String) userInfo.get("given_name"));
                    //user.setLastName((String) userInfo.get("family_name"));
                    user.setLastName("kumar");
                    user.setUserName(email);
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                    // Handle roles
                    Collection<Role> roles = new HashSet<>();
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setName(ERole.ROLE_USER);
                                return roleRepository.save(newRole);
                            });
                    roles.add(userRole);

                    user.setRoles(roles);


                    savedUser = userRepository.save(user);
                }
                String jwtToken = jwtUtil.generateToken(email);

                // Return token + user details
                Map<String, Object> response = new HashMap<>();
                response.put("userId", savedUser.getId());
                response.put("token", jwtToken);
                response.put("email", email);
                response.put("firstName", userInfo.get("given_name"));
                response.put("lastName", userInfo.get("family_name"));
                response.put("picture", userInfo.get("picture"));

                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Exception occurred while handleGoogleCallback ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}