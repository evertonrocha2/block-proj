package com.crud.system.exception;

/**
 * Exceção customizada para violações de regras de negócio.
 * Lançada quando operações violam restrições lógicas do domínio.
 */
public class BusinessRuleException extends RuntimeException {
    
    public BusinessRuleException(String message) {
        super(message);
    }
    
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
