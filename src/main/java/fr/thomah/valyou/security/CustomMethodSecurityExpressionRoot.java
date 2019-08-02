package fr.thomah.valyou.security;

import fr.thomah.valyou.model.Organization;
import fr.thomah.valyou.model.User;
import fr.thomah.valyou.repository.OrganizationRepository;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.List;

class CustomMethodSecurityExpressionRoot
        extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private OrganizationRepository organizationRepository;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public boolean isMember(Long OrganizationId) {
        User user = (User) this.getPrincipal();
        List<Organization> orgs = organizationRepository.findByMembers_Id(user.getId());
        Organization orgUser = orgs.stream().filter(organization -> organization.getId().equals(OrganizationId)).findFirst().orElse(null);
        return orgUser != null;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}