select 
	c.id,
	c.created_at,
	c.created_by,
	c.updated_at,
	c.updated_by,
	c.title, 
	c.status,
	c.short_description as shortDescription,
	c.long_description as longDescription,
	c.donations_required as donationRequired,
	c.people_required as peopleRequired,
	c.funding_deadline as fundingDeadline,
	c.total_donations as totalDonations
	from campaigns c 
    inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id
    inner join organizations o on campaigns_organizations.organization_id = o.id
    inner join organizations_users on organizations_users.organization_id = o.id
    inner join users u on u.id = organizations_users.user_id
    where u.id = :user_id and c.status IN (:status);

select count(*) from campaigns c 
    inner join campaigns_organizations on c.id = campaigns_organizations.campaign_id 
    inner join organizations o on campaigns_organizations.organization_id = o.id 
    inner join organizations_users on organizations_users.organization_id = o.id 
    inner join users u on u.id = organizations_users.user_id 
    where u.id = :user_id and c.status IN (:status);