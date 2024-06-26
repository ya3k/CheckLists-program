package com.ya3k.checklist.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KeyCloakService {


    @Value("${keycloak.client}")
    private  String CLIENT_ID;
    @Value("${keycloak.client-secret}")
    private  String CLIENT_SECRET;
    @Value("${keycloak.introspect}")
    private  String INTROSPECT_URL;


    private final RestTemplate restTemplate;
    @Autowired
    public KeyCloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean introspectToken(String token) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("token", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(INTROSPECT_URL, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Error while introspecting token", e);
        }

        // Parse JSON string to JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Error while parsing response", e);
        }

        // Get the value of "active"
        return jsonNode.get("active").asBoolean();
    }

//    public Boolean introspectToken(String token) throws Exception {
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//
//        //ENV!!!!!!!!!!!!
//        body.add("client_id", CLIENT_ID);
//        body.add("client_secret", CLIENT_SECRET);
//
//        // access token
//        body.add("token", token);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        //ENV!!!!!!!!!!!!
//        //url introspect verify token
//        String url = INTROSPECT_URL;
//
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//
//
//
//        // Parse JSON string to JsonNode
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode jsonNode = mapper.readTree(response.getBody());
//
//        // Get the value of "active"
//        boolean isActive = jsonNode.get("active").asBoolean();
//
//        return isActive;
//
//    }


}
