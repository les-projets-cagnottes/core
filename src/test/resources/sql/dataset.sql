CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ORGANIZATION

insert into public.organizations
(id, created_at, created_by, updated_at, updated_by, logo_url, "name", social_name)
values
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'https://eu.ui-avatars.com/api/?name=Super+Boite', 'Super Boite', 'Notre Super Boite');

--- ORGANIZATION AUTHORITIES

insert into public.organizations_authorities
(id, created_at, created_by, updated_at, updated_by, "name", organization_id)
values
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'ROLE_SPONSOR', (select id from organizations where name = 'Super Boite')),
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'ROLE_MANAGER', (select id from organizations where name = 'Super Boite')),
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'ROLE_OWNER', (select id from organizations where name = 'Super Boite'));

--- USERS

insert into public.users
(id, created_at, created_by, updated_at, updated_by, avatar_url, email,
enabled, firstname, lastpasswordresetdate, lastname, "password", username)
values
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'https://eu.ui-avatars.com/api/?name=Charlotte+Fournier', 'CharlottePaquette@rhyta.com',
true, 'Charlotte', NOW(), 'Fournier', '$2a$10$mTExeAyURZPO4mE7GEbQA.KCB/t.rf8NgCg67vaj7wB.ZzzHG78Ai', 'CharlottePaquette@rhyta.com'),
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'https://eu.ui-avatars.com/api/?name=Alphonse+Lejeune', 'AlphonseLejeune@teleworm.us',
true, 'Alphonse', NOW(), 'Lejeune', '$2a$10$MSBSKYkqNF5AklPxR6VPOu05q4UlDWC9Oah71PFx5EAa3aMss/8V6', 'AlphonseLejeune@teleworm.us'),
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'https://eu.ui-avatars.com/api/?name=Denis+Giroux', 'DenisGiroux@dayrep.com',
true, 'Denis', NOW(), 'Giroux', '$2a$10$xwq1y1xADwLzwFg/HloAg.w0SuWFTaOveBqvE8XbryXR.rfkTyUKy', 'DenisGiroux@dayrep.com'),
(nextval('hibernate_sequence'), NOW(), 'System', NOW(), 'System', 'https://eu.ui-avatars.com/api/?name=Matilda+Caisse', 'MatildaCaisse@dayrep.com',
true, 'Matilda', NOW(), 'Caisse', '$2a$10$AFJJJrZWD.b9zhY5jXeI2uUnmoG9Zah5tnFeWe2Ewb3OQeFx6ORAS', 'MatildaCaisse@dayrep.com');

--- USER AUTHORITY

insert into public.user_authority
(user_id,authority_id)
values
((select id from users where username = 'CharlottePaquette@rhyta.com'), (select id from authorities where name = 'ROLE_USER')),
((select id from users where username = 'AlphonseLejeune@teleworm.us'), (select id from authorities where name = 'ROLE_USER')),
((select id from users where username = 'DenisGiroux@dayrep.com'), (select id from authorities where name = 'ROLE_USER')),
((select id from users where username = 'MatildaCaisse@dayrep.com'), (select id from authorities where name = 'ROLE_USER'));

--- ORGANIZATIONS USERS

INSERT INTO public.organizations_users
(organization_id, user_id)
values
((select id from organizations where name = 'Super Boite'), (select id from users where username = 'CharlottePaquette@rhyta.com')),
((select id from organizations where name = 'Super Boite'), (select id from users where username = 'AlphonseLejeune@teleworm.us')),
((select id from organizations where name = 'Super Boite'), (select id from users where username = 'DenisGiroux@dayrep.com')),
((select id from organizations where name = 'Super Boite'), (select id from users where username = 'MatildaCaisse@dayrep.com'));

--- USER AUTHORITY ORGANIZATIONS

insert into public.user_authority_organizations
(user_id,organization_authority_id)
VALUES
((select id from users where username = 'AlphonseLejeune@teleworm.us'), (select id from organizations_authorities where name = 'ROLE_SPONSOR' and organization_id = (select id from organizations where name = 'Super Boite'))),
((select id from users where username = 'CharlottePaquette@rhyta.com'), (select id from organizations_authorities where name = 'ROLE_MANAGER' and organization_id = (select id from organizations where name = 'Super Boite'))),
((select id from users where username = 'DenisGiroux@dayrep.com'), (select id from organizations_authorities where name = 'ROLE_OWNER' and organization_id = (select id from organizations where name = 'Super Boite')));

--- CONTENTS

INSERT INTO public.contents (id,created_at,created_by,updated_at,updated_by,organization_id,"name","value") values
(nextval('hibernate_sequence'),NOW(),'AlphonseLejeune@teleworm.us',NOW(),'AlphonseLejeune@teleworm.us',(select id from organizations where name = 'Super Boite'),'Règles d''utilisation du budget Super Boite','## Eligibilité
Attention : tous les projets ne sont pas éligibles aux campagnes cagnottes. Avant de soumettre votre campagne sur la plateforme, vérifiez que vous respectez bien les règles suivantes :

> Ceci est une démo, faites ce que vous voulez
 ');

--- BUDGETS

insert into public.budgets
(id, created_at, created_by, updated_at, updated_by,
amount_per_member,
end_date,
is_distributed,
"name",
start_date,
total_donations,
organization_id,
rules_id,
sponsor_id)
values
(nextval('hibernate_sequence'), NOW(), 'AlphonseLejeune@teleworm.us', NOW(), 'AlphonseLejeune@teleworm.us',
500.0,
(SELECT date_trunc('month', now() - interval '6 months') + interval '1 year' - interval '1 sec'),
true,
'Cagnotte',
(date_trunc('month', now() - interval '6 months')),
0.0,
(select id from organizations where name = 'Super Boite'),
(select id from contents where name = 'Règles d''utilisation du budget Super Boite'),
(select id from users where username = 'AlphonseLejeune@teleworm.us'));

--- ACCOUNTS

insert into public.accounts
(id, created_at, created_by, updated_at, updated_by, amount, initial_amount, budget_id, owner_id)
values
(nextval('hibernate_sequence'), now(), 'AlphonseLejeune@teleworm.us', now(), 'AlphonseLejeune@teleworm.us', 500.0, 500.0, (select id from budgets where name = 'Cagnotte'), (select id from users where username = 'CharlottePaquette@rhyta.com')),
(nextval('hibernate_sequence'), now(), 'AlphonseLejeune@teleworm.us', now(), 'AlphonseLejeune@teleworm.us', 500.0, 500.0, (select id from budgets where name = 'Cagnotte'), (select id from users where username = 'AlphonseLejeune@teleworm.us')),
(nextval('hibernate_sequence'), now(), 'AlphonseLejeune@teleworm.us', now(), 'AlphonseLejeune@teleworm.us', 500.0, 500.0, (select id from budgets where name = 'Cagnotte'), (select id from users where username = 'DenisGiroux@dayrep.com')),
(nextval('hibernate_sequence'), now(), 'AlphonseLejeune@teleworm.us', now(), 'AlphonseLejeune@teleworm.us', 500.0, 500.0, (select id from budgets where name = 'Cagnotte'), (select id from users where username = 'MatildaCaisse@dayrep.com'));

--- PROJECTS

insert into public.projects
(id,created_at,created_by,updated_at,updated_by,people_required,short_description,status,title,leader_id,workspace,organization_id,long_description)
values
(nextval('hibernate_sequence'), now() - interval '2 months', 'DenisGiroux@dayrep.com', now() - interval '2 months', 'DenisGiroux@dayrep.com',
2, 'Pour la salle de pause', 'FINISHED', 'Du nouveau mobilier', (select id from users where username = 'DenisGiroux@dayrep.com'),
uuid_generate_v4(),(select id from organizations where name = 'Super Boite'),
'<h1 id="du-nouveau-mobilier">Du nouveau mobilier</h1>
<h2 id="de-quoi-s-agit-il-">De quoi s&#39;agit-il ?</h2>
<h2 id="qui-est-concern-">Qui est concerné ?</h2>
<h2 id="a-quoi-va-servir-le-budget-">A quoi va servir le budget ?</h2>
<h2 id="pourquoi-a-me-tient-coeur">Pourquoi ça me tient à coeur</h2>'),
(nextval('hibernate_sequence'), now() - interval '1 month', 'CharlottePaquette@rhyta.com', now() - interval '1 month', 'CharlottePaquette@rhyta.com',
4, 'Notre boite est la meilleure', 'FINISHED', 'Un feu d''artifice pour le séminaire annuel', (select id from users where username = 'CharlottePaquette@rhyta.com'),
uuid_generate_v4(),(select id from organizations where name = 'Super Boite'),
'<h1 id="allumer-le-feu">Allumer le feu</h1>
<h2 id="de-quoi-s-agit-il-">De quoi s&#39;&#39;agit-il ?</h2>
<h2 id="qui-est-concern-">Qui est concerné ?</h2>
<h2 id="a-quoi-va-servir-le-budget-">A quoi va servir le budget ?</h2>
<h2 id="pourquoi-a-me-tient-coeur">Pourquoi ça me tient à coeur</h2>'),
(nextval('hibernate_sequence'), now() - interval '6 days', 'MatildaCaisse@dayrep.com', now() - interval '6 days', 'MatildaCaisse@dayrep.com',
2, 'Avec ça, nos concurrents n''ont qu''à bien se tenir','IN_PROGRESS','Le projet ABCD', (select id from users where username = 'MatildaCaisse@dayrep.com'),
uuid_generate_v4(),(select id from organizations where name = 'Super Boite'),
'<h1 id="le-projet-abcd">Le projet ABCD</h1>
<h2 id="de-quoi-s-agit-il-">De quoi s&#39;&#39;agit-il ?</h2>
<h2 id="qui-est-concern-">Qui est concerné ?</h2>
<h2 id="a-quoi-va-servir-le-budget-">A quoi va servir le budget ?</h2>
<h2 id="pourquoi-c-est-important">Pourquoi c&#39;&#39;est important</h2>');

--- PROJECTS MEMBERS

insert into public.projects_members
(project_id,user_id) values
((select id from projects where title = 'Un feu d''artifice pour le séminaire annuel'), (select id from users where username = 'CharlottePaquette@rhyta.com')),
((select id from projects where title = 'Du nouveau mobilier'), (select id from users where username = 'DenisGiroux@dayrep.com')),
((select id from projects where title = 'Du nouveau mobilier'), (select id from users where username = 'MatildaCaisse@dayrep.com')),
((select id from projects where title = 'Du nouveau mobilier'), (select id from users where username = 'CharlottePaquette@rhyta.com')),
((select id from projects where title = 'Le projet ABCD'), (select id from users where username = 'MatildaCaisse@dayrep.com')),
((select id from projects where title = 'Le projet ABCD'), (select id from users where username = 'DenisGiroux@dayrep.com'));

--- CAMPAIGNS

insert into public.campaigns
(id,created_at,created_by,updated_at,updated_by,project_id,budget_id,donations_required,funding_deadline,status,total_donations)
values
(nextval('hibernate_sequence'), now() - interval '2 months', 'DenisGiroux@dayrep.com', now() - interval '2 months', 'DenisGiroux@dayrep.com',
(select id from projects where title = 'Du nouveau mobilier'),(select id from budgets where name = 'Cagnotte'),800.0, now() - interval '1 month', 'SUCCESSFUL', 800.0),
(nextval('hibernate_sequence'), now() - interval '1 month', 'CharlottePaquette@rhyta.com', now() - interval '1 month', 'CharlottePaquette@rhyta.com',
(select id from projects where title = 'Un feu d''artifice pour le séminaire annuel'),(select id from budgets where name = 'Cagnotte'),0.0, now() - interval '14 days', 'FAILED', 0.0),
(nextval('hibernate_sequence'), now() - interval '6 days', 'MatildaCaisse@dayrep.com', now() - interval '6 days', 'MatildaCaisse@dayrep.com',
(select id from projects where title = 'Le projet ABCD'),(select id from budgets where name = 'Cagnotte'),500.0, now() + interval '15 days', 'IN_PROGRESS', 300.0);

--- DONATIONS

SELECT public.create_donation(
(select id from accounts where budget_id =((select id from budgets where name = 'Cagnotte')) and owner_id = (select id from users where username = 'DenisGiroux@dayrep.com')),
(select id from campaigns where title = 'Du nouveau mobilier'), 400.0);
SELECT public.create_donation(
(select id from accounts where budget_id =((select id from budgets where name = 'Cagnotte')) and owner_id = (select id from users where username = 'MatildaCaisse@dayrep.com')),
(select id from campaigns where title = 'Du nouveau mobilier'), 200.0);
SELECT public.create_donation(
(select id from accounts where budget_id =((select id from budgets where name = 'Cagnotte')) and owner_id = (select id from users where username = 'AlphonseLejeune@teleworm.us')),
(select id from campaigns where title = 'Du nouveau mobilier'), 200.0);
SELECT public.create_donation(
(select id from accounts where budget_id =((select id from budgets where name = 'Cagnotte')) and owner_id = (select id from users where username = 'MatildaCaisse@dayrep.com')),
(select id from campaigns where title = 'Le projet ABCD'), 300.0);

UPDATE public.donations
	SET created_at=(now() - interval '45 days'), updated_at=(now() - interval '45 days')
	WHERE campaign_id=(select id from campaigns where title = 'Du nouveau mobilier');
UPDATE public.donations
	SET created_at=(now() - interval '6 days'), updated_at=(now() - interval '6 days')
	WHERE campaign_id=(select id from campaigns where title = 'Le projet ABCD');

--- IDEAS

insert into public.ideas
(id, created_at, created_by, has_anonymous_creator, has_leader_creator, icon, long_description, short_description, updated_at, updated_by,
organization_id, submitter_id)
values
(nextval('hibernate_sequence'), now(), 'CharlottePaquette@rhyta.com', false, false, 'fas fa-recycle', 'Ca va être génial !<br>', 'Clean walk du quartier', now(), 'CharlottePaquette@rhyta.com',
(select id from organizations where name = 'Super Boite'), (select id from users where username = 'CharlottePaquette@rhyta.com')),
(nextval('hibernate_sequence'), now(), 'anonymous', true, false, 'fas fa-code-branch', 'C''est galère de poser des congés.<br>', 'Optimiser le logiciel de pose des congés', now(), 'anonymous',
(select id from organizations where name = 'Super Boite'), NULL),
(nextval('hibernate_sequence'), now(), 'DenisGiroux@dayrep.com', false, true, 'fas fa-apple-alt', 'Qui n''aime pas les fruits ?<br>','Des fruits frais en salle de pause' ,now(), 'DenisGiroux@dayrep.com',
(select id from organizations where name = 'Super Boite'), (select id from users where username = 'DenisGiroux@dayrep.com'));
