-- DROP PROCEDURE create_donation(bigint,bigint,bigint,real);
-- DROP PROCEDURE delete_donation(bigint);

-- User 10 Account 56 Campaign 102 Budget 47

SELECT public.create_donation(:account_id, :campaign_id, :budget_id, :amount);
SELECT public.delete_donation(:donation_id);
