package com.thequietchapter.dto;

public record TokenRespuesta(String accessToken, String tokenType, long expiresIn, PerfilRespuesta usuario) {
}
