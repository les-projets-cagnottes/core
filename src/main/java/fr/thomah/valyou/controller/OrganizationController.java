package fr.thomah.valyou.controller;

import fr.thomah.valyou.exception.NotFoundException;
import fr.thomah.valyou.generator.OrganizationGenerator;
import fr.thomah.valyou.model.*;
import fr.thomah.valyou.repository.BudgetRepository;
import fr.thomah.valyou.repository.OrganizationAuthorityRepository;
import fr.thomah.valyou.repository.OrganizationRepository;
import fr.thomah.valyou.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class OrganizationController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<Organization> list(@RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return repository.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Organization getById(@PathVariable("id") Long id) {
        Organization org = repository.findById(id).orElse(null);
        if(org == null) {
            throw new NotFoundException();
        } else {
            return org;
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"member_id"})
    public Set<Organization> getUserOrganizations(Principal authUserToken, @RequestParam("member_id") Long memberId) {
        return repository.findByMembers_Id(memberId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Organization create(@RequestBody Organization org, Principal owner) {
        org = repository.save(OrganizationGenerator.newOrganization(org));
        for(OrganizationAuthorityName authorityName : OrganizationAuthorityName.values()) {
            organizationAuthorityRepository.save(new OrganizationAuthority(org, authorityName));
        }
        OrganizationAuthority memberOrganizationAuthority = organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_MEMBER);
        for(User member : org.getMembers()) {
            member = userRepository.findById(member.getId()).orElse(null);
            if(member != null) {
                member.addOrganizationAuthority(memberOrganizationAuthority);
                userRepository.save(member);
            }
        }
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) owner;
        User userOwner = (User) token.getPrincipal();
        userOwner = userRepository.findByEmail(userOwner.getEmail());
        userOwner.addOrganizationAuthority(organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_OWNER));
        userRepository.save(userOwner);

        return org;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public void save(@PathVariable("id") String id, @RequestBody Organization org) {
        Organization orgInDb = repository.findById(Long.valueOf(id)).orElse(null);
        if (orgInDb == null) {
            throw new NotFoundException();
        } else {
            orgInDb.setName(org.getName());
            orgInDb.setMembers(org.getMembers());
            repository.save(org);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable("id") String id) {
        repository.deleteById(Long.valueOf(id));
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = {"offset", "limit"})
    public Page<User> getMembers(@PathVariable("id") long id, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return userRepository.findByOrganizations_idOrderByIdAsc(id, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addMember(@PathVariable long id, @RequestBody long userId) {
        Organization org = repository.findById(id).orElse(null);
        User newMember = userRepository.findById(userId).orElse(null);
        if(org == null || newMember == null) {
            throw new NotFoundException();
        } else {
            OrganizationAuthority memberOrganizationAuthority = organizationAuthorityRepository.findByOrganizationAndName(org, OrganizationAuthorityName.ROLE_MEMBER);
            newMember.addOrganizationAuthority(memberOrganizationAuthority);
            org.getMembers().add(newMember);

            repository.save(org);
            userRepository.save(newMember);
        }
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value = "/api/organization/{id}/members", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void removeMember(@PathVariable long id, @RequestParam long userId) {
        Organization org = repository.findById(id).orElse(null);
        User member = userRepository.findById(userId).orElse(null);
        if(org == null || member == null) {
            throw new NotFoundException();
        } else {
            Set<OrganizationAuthority> organizationAuthorities = new LinkedHashSet<>();
            member.getUserOrganizationAuthorities().stream().filter(organizationAuthority -> organizationAuthority.getOrganization().getId() == id).forEach(organizationAuthorities::add);
            member.getUserOrganizationAuthorities().removeAll(organizationAuthorities);
            userRepository.save(member);

            org.getMembers().remove(member);
            repository.save(org);
        }
    }

}
