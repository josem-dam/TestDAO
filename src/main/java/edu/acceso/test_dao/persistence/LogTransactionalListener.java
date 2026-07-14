package edu.acceso.test_dao.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LogTransactionalListener {
    private static final Logger logger = LoggerFactory.getLogger(LogTransactionalListener.class);

    public record AuditEvent(Level level, String mensaje) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void logAuditEvent(AuditEvent event) {
        logger.atLevel(event.level()).log(event.mensaje());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void logAuditEventRollback(AuditEvent event) {
        logger.atLevel(event.level()).log("Transacción fallida. Acción revertida: {}", event.mensaje());
    }
}
