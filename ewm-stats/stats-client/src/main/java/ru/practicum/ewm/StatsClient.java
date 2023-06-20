package ru.practicum.ewm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Map;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class StatsClient {
    static final String BASE_URL = "https://localhost:9090";

    public ResponseEntity<Object> create(EndpointHitDto endpointHitDto) {
        RestTemplate rest = createRestTemplate("/hit");
        return makeAndSendRequest(rest, HttpMethod.POST, "", null, endpointHitDto);
    }

    public ResponseEntity<Object> getViewStats(String start, String end, List<String> uris, Boolean unique) {
        RestTemplate rest = createRestTemplate("/stats");

        String paramsUri = "";
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                paramsUri = String.format("%s%s", paramsUri, String.format("&uris=%s", uri));
            }
        }

        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", paramsUri,
                "unique", unique
        );

        String path = String.format("?start={start}&end={end}&uris={%s}&unique={unique}", paramsUri);
        return makeAndSendRequest(rest, HttpMethod.GET, path, parameters, null);
    }

    private RestTemplate createRestTemplate(String prefix) {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(BASE_URL + prefix))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(RestTemplate rest, HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<Object> ewmServerResponse;
        try {
            if (parameters != null) {
                ewmServerResponse = rest.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                ewmServerResponse = rest.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(ewmServerResponse);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}