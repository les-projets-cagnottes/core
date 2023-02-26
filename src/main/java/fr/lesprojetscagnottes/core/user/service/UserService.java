package fr.lesprojetscagnottes.core.user.service;

import fr.lesprojetscagnottes.core.authentication.service.AuthService;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.common.GenericModel;
import fr.lesprojetscagnottes.core.common.security.UserPrincipal;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.repository.OrganizationRepository;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.model.UserModel;
import fr.lesprojetscagnottes.core.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;

@Slf4j
@Service
public class UserService {

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    public UserEntity findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity findByEmail(String mail) {
        return userRepository.findByEmail(mail);
    }

    public UserEntity get(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserEntity user;
        if(token.getPrincipal().getClass().equals(GenericModel.class)) {
            user = userRepository.findById(Long.getLong(token.getPrincipal().toString())).orElse(null);
        } else {
            UserDetails userDetails = this.authService.loadUserByUsername(token.getPrincipal().toString());
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            user = userRepository.findByUsername(userPrincipal.getUsername());
            if (user == null) {
                user = userRepository.findByEmail(userPrincipal.getUsername());
            }
        }
        return user;
    }

    public List<UserEntity> findAll() {
        List<UserEntity> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    public Set<UserEntity> findAllByProjects_Id(Long projectId) {
        return userRepository.findAllByProjects_Id(projectId);
    }

    public String generateAvatarUrl(UserModel userModel) {
        String avatarUrl;
        if(userModel.getAvatarUrl() == null || userModel.getAvatarUrl().isEmpty()) {
            avatarUrl = "https://eu.ui-avatars.com/api/?name=";
            boolean hasFirstname = userModel.getFirstname() != null && !userModel.getFirstname().isEmpty();
            if(hasFirstname) {
                avatarUrl+= userModel.getFirstname();
            }
            if(userModel.getLastname() != null && !userModel.getLastname().isEmpty()) {
                if(hasFirstname) {
                    avatarUrl+= "+";
                }
                avatarUrl+= userModel.getLastname();
            }
        } else {
            avatarUrl = userModel.getAvatarUrl();
        }
        return avatarUrl;
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public UserEntity save(UserEntity user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setLastPasswordResetDate(new Date());
        }
        user.setUsername(user.getEmail());
        log.debug("Saving {}", user);
        return userRepository.save(user);
    }

    public boolean isMemberOfOrganization(long userId, long organizationId) {
        return organizationRepository.findByIdAndMembers_Id(organizationId, userId) != null;
    }

    public boolean isNotSponsorOfOrganization(long userId, long organizationId) {
        return !isMemberOfOrganization(userId, organizationId) || hasNotOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_SPONSOR);
    }

    public boolean isNotManagerOfOrganization(long userId, long organizationId) {
        return !isMemberOfOrganization(userId, organizationId) || hasNotOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_MANAGER);
    }

    public boolean isNotOwnerOfOrganization(long userId, long organizationId) {
        return !isMemberOfOrganization(userId, organizationId) || hasNotOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_OWNER);
    }

    private boolean hasNotOrganizationAuthority(long userId, long organizationId, OrganizationAuthorityName authorityName) {
        return organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organizationId, userId, authorityName) == null;
    }

    public boolean isNotAdmin(long userId) {
        return authorityRepository.findByNameAndUsers_Id(AuthorityName.ROLE_ADMIN, userId) == null;
    }

    public boolean hasNoACommonOrganization(Set<OrganizationEntity> organizations1, Set<OrganizationEntity> organizations2) {
        final boolean[] notFound = {true};
        organizations1.forEach(organization1 -> {
            int k = 0;
            while(notFound[0] && k < organizations2.size()) {
                notFound[0] = organizations2.stream().noneMatch(organization2 -> organization2.getId().equals(organization1.getId()));
                k++;
            }
        });
        return notFound[0];
    }

}