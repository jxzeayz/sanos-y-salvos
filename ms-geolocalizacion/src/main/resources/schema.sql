-- Indice espacial GiST para acelerar ST_DWithin sobre zonas_reporte.ubicacion.
-- El proyecto usa ddl-auto=update (sin Flyway/Liquibase), por lo que este script
-- corre después de que Hibernate crea/actualiza la tabla (ver spring.jpa.defer-datasource-initialization).
CREATE INDEX IF NOT EXISTS idx_zonas_reporte_ubicacion ON zonas_reporte USING GIST (ubicacion);
