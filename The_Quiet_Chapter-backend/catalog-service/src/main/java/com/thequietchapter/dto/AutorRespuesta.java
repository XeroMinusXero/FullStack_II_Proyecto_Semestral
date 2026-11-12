package com.thequietchapter.dto;

import com.thequietchapter.model.Autor;

public record AutorRespuesta(Long id, String nombre, String biografia) {
    public static AutorRespuesta desde(Autor a) {
        return new AutorRespuesta(a.getId(), a.getNombre(), a.getBiografia());
    }
}
