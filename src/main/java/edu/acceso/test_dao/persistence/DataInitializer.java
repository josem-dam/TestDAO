package edu.acceso.test_dao.persistence;

import java.util.List;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import edu.acceso.test_dao.modelo.Centro;
import edu.acceso.test_dao.modelo.Centro.Titularidad;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final AppService appService;

    public DataInitializer(AppService appService) {
        this.appService = appService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(!appService.listarCentros().isEmpty()) return;

        List<Centro> centros = List.of(
            new Centro(11004866L, "IES Castillo de Luna", Titularidad.PUBLICA),
            new Centro(11700603L, "IES Pintor Juan Lara", Titularidad.PUBLICA),
            new Centro(11007533L, "IES Arroyo Hondo", Titularidad.PUBLICA)
        );
        centros.forEach(appService::agregarCentro);
    }

}
