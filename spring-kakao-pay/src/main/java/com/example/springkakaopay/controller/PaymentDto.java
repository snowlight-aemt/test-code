package com.example.springkakaopay.controller;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDto {
    @Getter
    @Setter
    @Builder
    public static class Request {

    }

    @Getter
    @Setter
    @Builder
    public static class KakaoReadyRequest {
        @Size(min = 1, max = 10)
        private String cid;
        @Pattern(regexp = "^[a-z0-9]{24}$")
        private String cid_secret;
        @Size(min = 1, max = 100)
        private String partner_order_id;
        @Size(min = 1, max = 100)
        private String partner_user_id;
        @Size(min = 1, max = 100)
        private String item_name;
        @Size(max = 100)
        private String item_code;
        @NotBlank
        private Integer quantity;
        @NotBlank
        private Integer total_amount;
        @NotBlank
        private Integer tax_free_amount;
        private Integer vat_amount;
        private Integer green_deposit;
        @Size(min = 1, max = 255)
        private String approval_url;
        @Size(min = 1, max = 255)
        private String cancel_url;
        @Size(min = 1, max = 255)
        private String fail_url;
        private List<String> available_cards = new ArrayList<>();
        private String payment_method_type;
        @Max(12)
        private String install_month;
        private String custom_json;
    }

    @Getter
    @Setter
    @Builder
    public static class KakaoReadyResponse {
        @Size(max = 20)
        private String tid;
        private String next_redirect_app_url;
        private String next_redirect_mobile_url;
        private String next_redirect_pc_url;
        private String android_app_scheme;
        private String ios_app_scheme;
        private LocalDateTime created_at;
    }
}
