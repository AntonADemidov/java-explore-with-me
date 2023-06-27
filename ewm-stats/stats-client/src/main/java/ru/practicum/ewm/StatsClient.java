package ru.practicum.ewm;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
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
    static String BASE_URL;

    public StatsClient(@Value("${stats-server.url}") String baseUrl) {
        this.BASE_URL = baseUrl;
    }

    public void createEndpointHit(EndpointHitFromUserDto endpointHitFromUserDto) {
        RestTemplate rest = createRestTemplate("/hit");
        makeAndSendRequest(rest, HttpMethod.POST, "", null, endpointHitFromUserDto);
    }

    public ResponseEntity<Object> getViewStats(String start, String end, List<String> uris, Boolean unique) {
        RestTemplate rest = createRestTemplate("/stats");

        StringBuilder builder = new StringBuilder();
        if (uris != null && !uris.isEmpty()) {
            int size = uris.size();
            for (int i = 0; i < size; i++) {
                builder.append(uris.get(i));

                if ((i + 1) != size) {
                    builder.append("&uris=");
                }
            }
        }
        String paramsUri = builder.toString();

        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", paramsUri,
                "unique", unique
        );

        String path = String.format("?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
        return makeAndSendRequest(rest, HttpMethod.GET, path, parameters, null);
    }

    private RestTemplate createRestTemplate(String prefix) {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(String.format("%s%s", BASE_URL, prefix)))
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