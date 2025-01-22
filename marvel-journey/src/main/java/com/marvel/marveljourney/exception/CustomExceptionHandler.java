package com.marvel.marveljourney.exception;

import java.lang.reflect.InaccessibleObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    public static void handleException(Exception e) {
        if (e instanceof InaccessibleObjectException) {
            logger.error("Erro de acesso: Não foi possível acessar o campo especificado. Verifique as permissões de acesso. Mensagem: {}", e.getMessage());
        } else {
            logger.error("Erro inesperado: {}", e.getMessage());
        }
    }
}