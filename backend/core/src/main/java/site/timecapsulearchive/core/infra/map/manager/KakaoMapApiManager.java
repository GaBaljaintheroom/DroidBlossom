package site.timecapsulearchive.core.infra.map.manager;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import site.timecapsulearchive.core.global.error.ErrorCode;
import site.timecapsulearchive.core.infra.map.config.KakaoMapProperties;
import site.timecapsulearchive.core.infra.map.data.dto.AddressData;
import site.timecapsulearchive.core.infra.map.data.dto.Document;
import site.timecapsulearchive.core.infra.map.data.mapper.AddressMapper;
import site.timecapsulearchive.core.infra.map.data.response.KakaoMapApiResponse;
import site.timecapsulearchive.core.infra.sms.exception.ExternalApiException;

@Component
@RequiredArgsConstructor
public class KakaoMapApiManager implements MapApiManager {

    private static final String URL = "https://dapi.kakao.com";
    private static final String PATH = "/v2/local/geo/coord2address.json";
    private static final String LONGITUDE = "x";
    private static final String LATITUDE = "y";

    private final KakaoMapProperties kakaoMapProperties;
    private final RestTemplate restTemplate;
    private final AddressMapper addressMapper;

    public AddressData reverseGeoCoding(final Double latitude, final Double longitude) {
        final URI uri = getKakaoMapApiUrl(latitude, longitude);

        final HttpEntity<Void> entity = getHttpEntity();

        final KakaoMapApiResponse response = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            KakaoMapApiResponse.class
        ).getBody();

        if (isError(response)) {
            throw new ExternalApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        final Document document = response.documents().get(0);
        if (document.roadAddressDto() == null) {
            return addressMapper.addressDtoToAddress(document.addressDto());
        }

        return addressMapper.roadAddressToData(document.roadAddressDto());
    }

    private URI getKakaoMapApiUrl(final Double latitude, final Double longitude) {
        return UriComponentsBuilder.fromHttpUrl(URL)
            .path(PATH)
            .queryParam(LATITUDE, latitude)
            .queryParam(LONGITUDE, longitude)
            .build()
            .toUri();
    }

    private boolean isError(final KakaoMapApiResponse response) {
        return response == null || response.documents() == null || response.documents().isEmpty();
    }

    private HttpEntity<Void> getHttpEntity() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, kakaoMapProperties.apiKey());

        return new HttpEntity<>(null, headers);
    }
}
