--liquibase formatted sql

--changeset lesprojetscagnottes:add-project-last_status_update
ALTER TABLE projects
    ADD last_status_update timestamp without time zone DEFAULT now();
UPDATE projects SET last_status_update = updated_at;
--rollback alter table projects drop column last_status_update;
