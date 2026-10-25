package ufanet_pool.request.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Client {
    private Long id;
    private String name;
    private String phone;
    private String email;
}
