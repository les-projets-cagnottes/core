--liquibase formatted sql

--changeset lesprojetscagnottes:add-slackteam-image
ALTER TABLE slack_team
    ADD image_132 character varying(255);
--rollback alter table slack_team drop column image_132;
