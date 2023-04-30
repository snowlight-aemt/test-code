package me.snowlight.springkakaopayapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Log4j2
@Controller
@RequiredArgsConstructor
public class PaymentApiController {
    public static final String CID = "TC0ONETIME";
    public static final String PAYMENT_APPROVE_URL = "https://kapi.kakao.com/v1/payment/approve";
    public static final String PAYMENT_READY_URL = "https://kapi.kakao.com/v1/payment/ready";
    public static final String SUCCESS_URL = "http://localhost:8080/success";
    public static final String FAIL_URL = "http://localhost:8080/fail";
    public static final String CANCEL_URL = "http://localhost:8080/cancel";
    public static final String PARTNER_ORDER_ID = UUID.randomUUID().toString();
    public static final String PARTNER_USER_ID = UUID.randomUUID().toString();

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final ConcurrentMap<String, KakaoPaymentDto.KakaoApproveRequest> map = new ConcurrentHashMap<>();

    @Value("${kakao.api.key}")
    private String apiKey;

    @GetMapping("/success")
    public ResponseEntity<String> success(@RequestParam("pg_token") String pgToken,
                                          @RequestParam("partner_order_id") String partnerOrderId) {
        KakaoPaymentDto.KakaoApproveRequest kakaoApproveRequest = this.map.get(partnerOrderId);
//        this.map.remove(partnerOrderId);
        kakaoApproveRequest.approve(pgToken);
        MultiValueMap<String, String> params = convertToMultiValueMap(kakaoApproveRequest);
        HttpHeaders headers = getKakaoHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        return restTemplate.exchange(PAYMENT_APPROVE_URL, HttpMethod.POST, body, String.class);
    }

    @GetMapping("/fail")
    public ResponseEntity<String> fail(@RequestParam Map<String, String> params) {
        log.info("fail");
        log.info(params.toString());

        return ResponseEntity.ok("OK");
    }

    @GetMapping("/payment")
    public ResponseEntity<KakaoPaymentDto.KakaoResponse> payment() {
        String itemName = "사과상자";
        Integer quantity = 1;
        Integer totalAmount = 2200;
        Integer taxFreeAmount = 0;

        KakaoPaymentDto.KakaoReadyRequest readyRequest = KakaoPaymentDto.KakaoReadyRequest.builder()
                                                .cid(CID)
                                                .partner_order_id(PARTNER_ORDER_ID)
                                                .partner_user_id(PARTNER_USER_ID)
                                                .item_name(itemName)
                                                .quantity(quantity)
                                                .total_amount(totalAmount)
                                                .tax_free_amount(taxFreeAmount)
                                                .approval_url(SUCCESS_URL + "?partner_order_id=" + PARTNER_ORDER_ID)
                                                .fail_url(FAIL_URL)
                                                .cancel_url(CANCEL_URL)
                                                .build();
        MultiValueMap<String, String> params = convertToMultiValueMap(readyRequest);
        HttpHeaders headers = getKakaoHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoPaymentDto.KakaoResponse> response = restTemplate.exchange(PAYMENT_READY_URL,
                                                                                HttpMethod.POST,
                                                                                body,
                                                                                KakaoPaymentDto.KakaoResponse.class);
        KakaoPaymentDto.KakaoResponse kakaoResponse = response.getBody();
        KakaoPaymentDto.KakaoApproveRequest kakaoApproveRequest = KakaoPaymentDto.KakaoApproveRequest.builder()
                                                                                    .cid(CID)
                                                                                    .tid(kakaoResponse.getTid())
                                                                                    .partner_order_id(PARTNER_ORDER_ID)
                                                                                    .partner_user_id(PARTNER_USER_ID)
                                                                                    .total_amount(totalAmount)
                                                                                    .build();

        this.map.put(PARTNER_ORDER_ID, kakaoApproveRequest);
        return ResponseEntity.ok(kakaoResponse);
    }

    @PostMapping("/order")
    public ResponseEntity<String> cancel() {
        KakaoPaymentDto.KakaoApproveRequest kakaoApproveRequest = this.map.get(PARTNER_ORDER_ID);
        KakaoPaymentDto.KakaoCancel kakaoCancel = KakaoPaymentDto.KakaoCancel.builder()
                .cid(CID)
                .tid(kakaoApproveRequest.getTid())
                .cancel_amount(kakaoApproveRequest.getTotal_amount())
                .cancel_tax_free_amount(0)
                .build();

        MultiValueMap<String, String> params = convertToMultiValueMap(kakaoCancel);
        HttpHeaders headers = getKakaoHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> body = new HttpEntity<>(params, headers);

        ResponseEntity<String> exchange = restTemplate.exchange("https://kapi.kakao.com/v1/payment/cancel", HttpMethod.POST, body, String.class);
        return exchange;
    }

    @GetMapping("/order")
    public ResponseEntity<String> order() {
        KakaoPaymentDto.KakaoApproveRequest kakaoApproveRequest = this.map.get(PARTNER_ORDER_ID);
        HttpEntity<Void> body = new HttpEntity<>(null, getKakaoHttpHeaders());
        ResponseEntity<String> exchange = restTemplate.exchange("https://kapi.kakao.com/v1/payment/order?cid="+ CID +"&tid=" + kakaoApproveRequest.getTid(), HttpMethod.GET, body, String.class);

        return exchange;
    }

    private HttpHeaders getKakaoHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "KakaoAK " + apiKey);
        return headers;
    }

    private MultiValueMap<String, String> convertToMultiValueMap(Object readyRequest) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.setAll(objectMapper.convertValue(readyRequest, new TypeReference<>() {}));
        return params;
    }
}
