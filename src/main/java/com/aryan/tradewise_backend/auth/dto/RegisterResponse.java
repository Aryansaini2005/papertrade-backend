package com.aryan.tradewise_backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {

    private String firstName;
    private String email;
    private String message;
}
