--liquibase formatted sql

--changeset lesprojetscagnottes:add-project-last_status_update
ALTER TABLE projects
    ADD last_status_update timestamp without time zone DEFAULT now();
UPDATE projects SET last_status_update = updated_at;
--rollback alter table projects drop column last_status_update;

--changeset lesprojetscagnottes:change-notifications-variables-type
ALTER TABLE notifications ALTER COLUMN variables TYPE text;
--rollback alter table notifications alter column variables type varchar(255);
