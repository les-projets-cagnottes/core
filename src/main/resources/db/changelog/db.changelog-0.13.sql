--liquibase formatted sql

--changeset lesprojetscagnottes:create-sequence-hibernate_sequence
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
--rollback drop sequence hibernate_sequence;

--changeset lesprojetscagnottes:create-table-accounts
CREATE TABLE IF NOT EXISTS accounts (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    amount real,
    initial_amount real,
    budget_id bigint,
    owner_id bigint
);
--rollback drop table accounts;

--changeset lesprojetscagnottes:create-table-api_tokens
CREATE TABLE IF NOT EXISTS api_tokens (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    expiration timestamp without time zone,
    token character varying(255),
    user_id bigint,
    description character varying(255)
);
--rollback drop table api_tokens;

--changeset lesprojetscagnottes:create-table-authorities
CREATE TABLE IF NOT EXISTS authorities (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    name character varying(50) NOT NULL
);
--rollback drop table authorities;

--changeset lesprojetscagnottes:create-table-budgets
CREATE TABLE IF NOT EXISTS budgets (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    amount_per_member real NOT NULL,
    end_date timestamp without time zone NOT NULL,
    is_distributed boolean NOT NULL,
    name character varying(255),
    start_date timestamp without time zone NOT NULL,
    organization_id bigint,
    sponsor_id bigint,
    rules_id bigint,
    total_donations real
);
--rollback drop table budgets;

--changeset lesprojetscagnottes:create-table-campaigns
CREATE TABLE IF NOT EXISTS campaigns (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    donations_required real,
    funding_deadline timestamp without time zone,
    status character varying(50),
    title character varying(255),
    total_donations real,
    project_id bigint,
    budget_id bigint
);
--rollback drop table campaigns;

--changeset lesprojetscagnottes:create-table-contents
CREATE TABLE IF NOT EXISTS contents (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    name character varying(255),
    value text,
    organization_id bigint
);
--rollback drop table contents;

--changeset lesprojetscagnottes:create-table-donations
CREATE TABLE IF NOT EXISTS donations (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    amount real NOT NULL,
    contributor_id bigint,
    account_id bigint,
    campaign_id bigint
);
--rollback drop table donations;

--changeset lesprojetscagnottes:create-table-files
CREATE TABLE IF NOT EXISTS files (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    directory character varying(255),
    format character varying(255),
    name character varying(255),
    url character varying(255)
);
--rollback drop table files;

--changeset lesprojetscagnottes:create-table-ideas
CREATE TABLE IF NOT EXISTS ideas (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    has_anonymous_creator boolean,
    has_leader_creator boolean,
    icon character varying(255),
    long_description text,
    short_description character varying(255),
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    organization_id bigint,
    submitter_id bigint,
    workspace character varying(255)
);
--rollback drop table ideas;

--changeset lesprojetscagnottes:create-table-ms_notifications
CREATE TABLE IF NOT EXISTS ms_notifications (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    sent boolean,
    notification_id bigint,
    team_id bigint
);
--rollback drop table ms_notifications;

--changeset lesprojetscagnottes:create-table-ms_team
CREATE TABLE IF NOT EXISTS ms_team (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    channel_id character varying(255),
    company_filter character varying(255),
    display_name character varying(255),
    group_id character varying(255),
    tenant_id character varying(255),
    organization_id bigint
);
--rollback drop table ms_team;

--changeset lesprojetscagnottes:create-table-ms_user
CREATE TABLE IF NOT EXISTS ms_user (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    company_name character varying(255),
    given_name character varying(255),
    mail character varying(255),
    ms_id character varying(255),
    surname character varying(255),
    ms_team_id bigint,
    user_id bigint
);
--rollback drop table ms_user;

--changeset lesprojetscagnottes:create-table-news
CREATE TABLE IF NOT EXISTS news (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    content text,
    title character varying(255),
    type character varying(50),
    author_id bigint,
    organization_id bigint,
    project_id bigint,
    workspace character varying(255)
);
--rollback drop table news;

--changeset lesprojetscagnottes:create-table-notifications
CREATE TABLE IF NOT EXISTS notifications (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    name character varying(255),
    variables character varying(255),
    organization_id bigint
);
--rollback drop table notifications;

--changeset lesprojetscagnottes:create-table-organizations
CREATE TABLE IF NOT EXISTS organizations (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    name character varying(255) NOT NULL,
    logo_url character varying(255),
    social_name character varying(255)
);
--rollback drop table organizations;

--changeset lesprojetscagnottes:create-table-organizations_authorities
CREATE TABLE IF NOT EXISTS organizations_authorities (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    name character varying(50) NOT NULL,
    organization_id bigint
);
--rollback drop table organizations_authorities;

--changeset lesprojetscagnottes:create-table-organizations_users
CREATE TABLE IF NOT EXISTS organizations_users (
    organization_id bigint NOT NULL,
    user_id bigint NOT NULL,
    PRIMARY KEY (organization_id, user_id)
);
--rollback drop table organizations_users;

--changeset lesprojetscagnottes:create-table-projects
CREATE TABLE IF NOT EXISTS projects (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    long_description text,
    people_required integer,
    short_description character varying(255),
    status character varying(50),
    title character varying(255),
    leader_id bigint,
    workspace character varying(255),
    organization_id bigint
);
--rollback drop table projects;

--changeset lesprojetscagnottes:create-table-projects_members
CREATE TABLE IF NOT EXISTS projects_members (
    project_id bigint NOT NULL,
    user_id bigint NOT NULL,
    PRIMARY KEY (project_id, user_id)
);
--rollback drop table projects_members;

--changeset lesprojetscagnottes:create-table-slack_team
CREATE TABLE IF NOT EXISTS slack_team (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    access_token character varying(255),
    bot_access_token character varying(255),
    bot_user_id character varying(255),
    team_id character varying(255),
    organization_id bigint,
    publication_channel character varying(255),
    team_name character varying(255),
    publication_channel_id character varying(255),
    bot_id character varying(255)
);
--rollback drop table slack_team;

--changeset lesprojetscagnottes:create-table-slack_user
CREATE TABLE IF NOT EXISTS slack_user (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    email character varying(255),
    im_id character varying(255),
    image_192 character varying(255),
    name character varying(255),
    user_id bigint,
    slack_team_id bigint,
    deleted boolean,
    slack_id character varying(255),
    is_restricted boolean
);
--rollback drop table slack_user;

--changeset lesprojetscagnottes:create-table-user_authority
CREATE TABLE IF NOT EXISTS user_authority (
    user_id bigint NOT NULL,
    authority_id bigint NOT NULL,
    PRIMARY KEY (user_id, authority_id)
);
--rollback drop table user_authority;

--changeset lesprojetscagnottes:create-table-user_authority_organizations
CREATE TABLE IF NOT EXISTS user_authority_organizations (
    user_id bigint NOT NULL,
    organization_authority_id bigint NOT NULL,
    PRIMARY KEY (user_id, organization_authority_id)
);
--rollback drop table user_authority_organizations;

--changeset lesprojetscagnottes:create-table-users
CREATE TABLE IF NOT EXISTS users (
    id bigint primary key,
    created_at timestamp without time zone DEFAULT now(),
    created_by character varying(255) DEFAULT 'System'::character varying,
    updated_at timestamp without time zone DEFAULT now(),
    updated_by character varying(255) DEFAULT 'System'::character varying,
    avatar_url character varying(255),
    email character varying(255) NOT NULL UNIQUE,
    enabled boolean NOT NULL,
    firstname character varying(255),
    lastpasswordresetdate timestamp without time zone NOT NULL,
    lastname character varying(255),
    password character varying(255) NOT NULL,
    username character varying(255)
);
--rollback drop table users;

--changeset lesprojetscagnottes:add-fk-accounts
ALTER TABLE ONLY accounts
    ADD CONSTRAINT fk_budget FOREIGN KEY (budget_id) REFERENCES budgets(id),
    ADD CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users(id);
--rollback alter table accounts drop constraint fk_owner;
--rollback alter table accounts drop constraint fk_budget;

--changeset lesprojetscagnottes:add-fk-api_tokens
ALTER TABLE api_tokens
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table api_tokens drop constraint fk_user;

--changeset lesprojetscagnottes:add-fk-budgets
ALTER TABLE ONLY budgets
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_rules FOREIGN KEY (rules_id) REFERENCES contents(id),
    ADD CONSTRAINT fk_sponsor FOREIGN KEY (sponsor_id) REFERENCES users(id);
--rollback alter table budgets drop constraint fk_sponsor;
--rollback alter table budgets drop constraint fk_rules;
--rollback alter table budgets drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-campaigns
ALTER TABLE ONLY campaigns
    ADD CONSTRAINT fk_budget FOREIGN KEY (budget_id) REFERENCES budgets(id),
    ADD CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id);
--rollback alter table campaigns drop constraint fk_project;
--rollback alter table campaigns drop constraint fk_budget;

--changeset lesprojetscagnottes:add-fk-contents
ALTER TABLE ONLY contents
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);
--rollback alter table contents drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-donations
ALTER TABLE ONLY donations
    ADD CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    ADD CONSTRAINT fk_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns(id);
--rollback alter table donations drop constraint fk_campaign;
--rollback alter table donations drop constraint fk_account;

--changeset lesprojetscagnottes:add-fk-ideas
ALTER TABLE ONLY ideas
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_submitter FOREIGN KEY (submitter_id) REFERENCES users(id);
--rollback alter table ideas drop constraint fk_submitter;
--rollback alter table ideas drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-ms_notification
ALTER TABLE ONLY ms_notifications
    ADD CONSTRAINT fk_notification FOREIGN KEY (notification_id) REFERENCES notifications(id),
    ADD CONSTRAINT fk_ms_team FOREIGN KEY (team_id) REFERENCES ms_team(id);
--rollback alter table ms_notifications drop constraint fk_ms_team;
--rollback alter table ms_notifications drop constraint fk_notification;

--changeset lesprojetscagnottes:add-fk-ms_team
ALTER TABLE ONLY ms_team
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);
--rollback alter table ms_team drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-ms_user
ALTER TABLE ONLY ms_user
    ADD CONSTRAINT fk_ms_team FOREIGN KEY (ms_team_id) REFERENCES ms_team(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table ms_user drop constraint fk_user;
--rollback alter table ms_user drop constraint fk_ms_team;

--changeset lesprojetscagnottes:add-fk-news
ALTER TABLE ONLY news
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id),
    ADD CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES users(id);
--rollback alter table news drop constraint fk_author;
--rollback alter table news drop constraint fk_project;
--rollback alter table news drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-notifications
ALTER TABLE ONLY notifications
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);
--rollback alter table notifications drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-organizations_authorities
ALTER TABLE ONLY organizations_authorities
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);
--rollback alter table organizations_authorities drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-organizations_users
ALTER TABLE ONLY organizations_users
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table organizations_users drop constraint fk_user;
--rollback alter table organizations_users drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-projects
ALTER TABLE ONLY projects
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    ADD CONSTRAINT fk_leader FOREIGN KEY (leader_id) REFERENCES public.users(id);
--rollback alter table projects drop constraint fk_leader;
--rollback alter table projects drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-projects_members
ALTER TABLE ONLY projects_members
    ADD CONSTRAINT fk_project FOREIGN KEY (project_id) REFERENCES projects(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table projects_members drop constraint fk_user;
--rollback alter table projects_members drop constraint fk_project;

--changeset lesprojetscagnottes:add-fk-slack_team
ALTER TABLE ONLY slack_team
    ADD CONSTRAINT fk_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);
--rollback alter table slack_team drop constraint fk_organization;

--changeset lesprojetscagnottes:add-fk-slack_user
ALTER TABLE ONLY slack_user
    ADD CONSTRAINT fk_slack_team FOREIGN KEY (slack_team_id) REFERENCES slack_team(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table slack_user drop constraint fk_user;
--rollback alter table slack_user drop constraint fk_slack_team;

--changeset lesprojetscagnottes:add-fk-user_authority
ALTER TABLE ONLY user_authority
    ADD CONSTRAINT fk_authority FOREIGN KEY (authority_id) REFERENCES authorities(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table user_authority drop constraint fk_user;
--rollback alter table user_authority drop constraint fk_authority;

--changeset lesprojetscagnottes:add-fk-user_authority_organizations
ALTER TABLE ONLY user_authority_organizations
    ADD CONSTRAINT fk_organization_authority FOREIGN KEY (organization_authority_id) REFERENCES organizations_authorities(id),
    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
--rollback alter table user_authority_organizations drop constraint fk_user;
--rollback alter table user_authority_organizations drop constraint fk_organization_authority;

--changeset lesprojetscagnottes:add-function-create_donation
CREATE OR REPLACE FUNCTION create_donation(_account_id bigint, _campaign_id bigint, _amount real)
    RETURNS boolean
    LANGUAGE plpgsql
    AS '
        DECLARE
            _account_amount FLOAT4;
            _donation_id INT8;
            _budget_id INT8;
        BEGIN
            select amount into _account_amount from accounts where id = _account_id;
            IF _account_amount < _amount THEN
                RAISE EXCEPTION ''Not enough amount on account %'', _account_id
                    USING HINT = ''Please check your budget'';
                return false;
            END IF;
            select nextval(''hibernate_sequence'') into _donation_id;
            select budget_id
                into _budget_id
                from accounts where id = _account_id;
            insert into donations (id, amount, campaign_id, account_id)
                values(_donation_id, _amount, _campaign_id, _account_id);
            update accounts set amount = (amount - _amount) where id = _account_id;
            update campaigns set total_donations = total_donations  + _amount where id = _campaign_id;
            update budgets set total_donations = total_donations + _amount where id = _budget_id;

            return true;
        END;
    ';
--rollback drop function if exists create_donation;

--changeset lesprojetscagnottes:add-function-delete_donation
CREATE OR REPLACE FUNCTION delete_donation(_donation_id bigint)
    RETURNS boolean
    LANGUAGE plpgsql
    AS
    '
        DECLARE
            _account_id INT8;
            _campaign_id INT8;
            _budget_id INT8;
            _amount FLOAT4;
        BEGIN
            select account_id, campaign_id, amount
                into _account_id, _campaign_id, _amount
                from donations where id = _donation_id;
            select budget_id
                into _budget_id
                from accounts where id = _account_id;
            update budgets set total_donations = total_donations - _amount where id = _budget_id;
            update campaigns set total_donations = total_donations  - _amount where id = _campaign_id;
            update accounts set amount = (amount + _amount) where id = _account_id;
            delete from donations where id = _donation_id;
            return true;
        END;
    ';
--rollback drop function if exists delete_donation;
