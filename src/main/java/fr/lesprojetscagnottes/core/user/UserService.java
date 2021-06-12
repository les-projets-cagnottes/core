package fr.lesprojetscagnottes.core.user;

import fr.lesprojetscagnottes.core.authorization.entity.AuthorityEntity;
import fr.lesprojetscagnottes.core.authorization.name.AuthorityName;
import fr.lesprojetscagnottes.core.authorization.name.OrganizationAuthorityName;
import fr.lesprojetscagnottes.core.authorization.repository.AuthorityRepository;
import fr.lesprojetscagnottes.core.authorization.repository.OrganizationAuthorityRepository;
import fr.lesprojetscagnottes.core.organization.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.OrganizationRepository;
import fr.lesprojetscagnottes.core.common.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service(value = "userService")
public class UserService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private OrganizationAuthorityRepository organizationAuthorityRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bcryptEncoder;

    public UserEntity get(Principal principal) {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipal userPrincipal = (UserPrincipal) token.getPrincipal();
        UserEntity user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user == null) {
            user = userRepository.findByEmail(userPrincipal.getUsername());
        }
        return user;
    }

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.findByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
        }

        return new UserPrincipal(username, user.getPassword(), getAuthorities(user.getId()));
    }

    public List<GrantedAuthority> getAuthorities(long userId) {
        Set<AuthorityEntity> userAuthorities = authorityRepository.findAllByUsers_Id(userId);
        LOGGER.debug("Authorities for user {} :", userId);
        return userAuthorities.stream().map(r -> {
            LOGGER.debug("{}", r.getName().name());
            return new SimpleGrantedAuthority(r.getAuthority());
        }).collect(Collectors.toList());
    }

    public List<UserEntity> findAll() {
        List<UserEntity> list = new ArrayList<>();
        userRepository.findAll().iterator().forEachRemaining(list::add);
        return list;
    }

    public void delete(long id) {
        userRepository.deleteById(id);
    }

    public UserEntity findOne(String username) {
        return userRepository.findByUsername(username);
    }

    public UserEntity findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity save(UserEntity user) {
        UserEntity newUser = new UserEntity();
        newUser.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
            newUser.setLastPasswordResetDate(new Date());
        }
        newUser.setEmail(user.getEmail());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setAvatarUrl(user.getAvatarUrl());
        newUser.setEnabled(user.getEnabled());
        return userRepository.save(newUser);
    }

    public boolean isMemberOfOrganization(long userId, long organizationId) {
        return organizationRepository.findByIdAndMembers_Id(organizationId, userId) != null;
    }

    public boolean isSponsorOfOrganization(long userId, long organizationId) {
        return isMemberOfOrganization(userId, organizationId) && hasOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_SPONSOR);
    }

    public boolean isManagerOfOrganization(long userId, long organizationId) {
        return isMemberOfOrganization(userId, organizationId) && hasOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_MANAGER);
    }

    public boolean isOwnerOfOrganization(long userId, long organizationId) {
        return isMemberOfOrganization(userId, organizationId) && hasOrganizationAuthority(userId, organizationId, OrganizationAuthorityName.ROLE_OWNER);
    }

    private boolean hasOrganizationAuthority(long userId, long organizationId, OrganizationAuthorityName authorityName) {
        return organizationAuthorityRepository.findByOrganizationIdAndUsersIdAndName(organizationId, userId, authorityName) != null;
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