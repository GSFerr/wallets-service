package br.com.walletservice.wallets_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDTO<T> {
    private String message;
    private T data;
}

