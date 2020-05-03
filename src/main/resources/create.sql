
CREATE OR REPLACE PROCEDURE public.update_campaigns_total_donations()
	LANGUAGE plpgsql
AS $procedure$
	declare
		campaign_cursor	CURSOR FOR select d.campaign_id , sum(d.amount) as total_donations from donations d group by d.campaign_id;
		campaignId	campaigns.id%TYPE;
		campaignTotalDonations	campaigns.total_donations%TYPE;
	BEGIN
		OPEN campaign_cursor;
		FETCH campaign_cursor INTO campaignId, campaignTotalDonations;
		RAISE NOTICE 'update campaigns set total_donations = % where id = %', campaignTotalDonations, campaignId;
		update campaigns set total_donations = campaignTotalDonations where id = campaignId;
		close campaign_cursor;
	END;
$procedure$
;
;
