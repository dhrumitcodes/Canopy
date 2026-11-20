package com.societyhub.api.college.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateCollegeRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Enter a valid email domain without @")
    private String emailDomain;

    @NotBlank
    @Email
    private String adminEmail;
}