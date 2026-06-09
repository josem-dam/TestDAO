package edu.acceso.test_dao.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Clase que gestiona el registro de eventos de transacciones en la aplicación.
 */
@Component
public class LoggingManager {
    private static final Logger logger = LoggerFactory.getLogger(LoggingManager.class);

    public record EventMessage(String message) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEvent(EventMessage event) {
        logger.debug("Se completa la operación de {}", event.message());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRollback(EventMessage event) {
        logger.debug("Transacción fallida: se revierte la operación de {}", event.message());
    }
}