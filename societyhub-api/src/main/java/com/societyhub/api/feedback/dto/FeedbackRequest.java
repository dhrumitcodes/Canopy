package com.societyhub.api.feedback.dto;
import jakarta.validation.constraints.*;
public record FeedbackRequest(@NotNull @Min(1) @Max(5) Integer rating,@Size(max=2000) String comments) {}