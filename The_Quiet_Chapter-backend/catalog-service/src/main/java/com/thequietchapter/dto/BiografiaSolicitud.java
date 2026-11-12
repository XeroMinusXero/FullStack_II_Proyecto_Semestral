package com.thequietchapter.dto;

import jakarta.validation.constraints.Size;

public record BiografiaSolicitud(@Size(max = 5000) String biografia) {
}
