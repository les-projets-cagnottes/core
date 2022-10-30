package fr.lesprojetscagnottes.core.providers.microsoft.service;

import fr.lesprojetscagnottes.core.account.service.AccountService;
import fr.lesprojetscagnottes.core.authentication.model.AuthenticationResponseModel;
import fr.lesprojetscagnottes.core.authentication.service.AuthService;
import fr.lesprojetscagnottes.core.common.exception.UnauthaurizedException;
import fr.lesprojetscagnottes.core.common.security.TokenProvider;
import fr.lesprojetscagnottes.core.common.strings.StringGenerator;
import fr.lesprojetscagnottes.core.organization.entity.OrganizationEntity;
import fr.lesprojetscagnottes.core.organization.service.OrganizationService;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftTeamEntity;
import fr.lesprojetscagnottes.core.providers.microsoft.entity.MicrosoftUserEntity;
import fr.lesprojetscagnottes.core.user.UserGenerator;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MicrosoftAuthenticationService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MicrosoftGraphService microsoftGraphService;

    @Autowired
    private MicrosoftTeamService microsoftTeamService;

    @Autowired
    private MicrosoftUserService microsoftUserService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider jwtTokenUtil;

    public AuthenticationResponseModel login(String code, String redirectUri, String tenantId) {

        // Get token from Microsoft
        String token = microsoftGraphService.token(tenantId, "openid+profile+offline_access", code, redirectUri);
        if(token == null) {
            log.error("Unable to login with Microsoft : no token retrieved");
            throw new UnauthaurizedException();
        }

        // Get a matching MS Team in DB
        MicrosoftTeamEntity msTeam = microsoftTeamService.findByTenantId(tenantId);
        if(msTeam == null) {
            log.error("Unable to login with Microsoft : no matching team in DB");
            throw new UnauthaurizedException();
        }

        // Get Microsoft Organization
        MicrosoftTeamEntity msGraphTeam = microsoftGraphService.getOrganization(token, tenantId);
        if(msGraphTeam == null) {
            log.error("Unable to login with Microsoft : no Microsoft organization found");
            throw new UnauthaurizedException();
        }

        // Verify Microsoft Organization match MS Team in DB
        if(!msGraphTeam.getTenantId().equals(msTeam.getTenantId())) {
            log.error("Unable to login with Microsoft : Microsoft organization does not match team {} in DB", msTeam.getId());
            throw new UnauthaurizedException();
        }

        // Verify MS Team has an associated organization
        if(msTeam.getOrganization() == null || msTeam.getOrganization().getId() <= 0) {
            log.error("Unable to login with Microsoft : no organization associated");
            throw new UnauthaurizedException();
        }

        // Sync personal infos
        MicrosoftUserEntity msGraphUser = microsoftGraphService.whoami(token);
        if(msGraphUser == null) {
            log.error("Unable to login with Microsoft : cannot get Microsoft user");
            throw new UnauthaurizedException();
        }

        // Verify MS User is complying with company filter
        String companyFilter = msTeam.getCompanyFilter();
        if((companyFilter != null && !companyFilter.isEmpty() && !companyFilter.equals(msGraphUser.getCompanyName()))) {
            log.error("Unable to login with Microsoft : user is not in the valid company");
            throw new UnauthaurizedException();
        }

        // Save MS User
        MicrosoftUserEntity msUser = microsoftUserService.getByMsId(msGraphUser.getMsId());
        if(msUser == null) {
            msUser = new MicrosoftUserEntity();
        }
        msUser.setMail(msGraphUser.getMail());
        msUser.setMsId(msGraphUser.getMsId());
        msUser.setGivenName(msGraphUser.getGivenName());
        msUser.setSurname(msGraphUser.getSurname());
        msUser.setMsTeam(msTeam);
        msUser = microsoftUserService.save(msUser);

        // Update corresponding user
        UserEntity user = userService.findByEmail(msUser.getMail());
        if(user == null) {
            user = UserGenerator.newUser(msUser.getMail(), StringGenerator.randomString());
            user.setFirstname(msUser.getGivenName());
            user.setLastname(msUser.getSurname());
        } else if (user.getPassword().isEmpty()) {
            user.setPassword(StringGenerator.randomString());
        }
        user.setAvatarUrl(microsoftGraphService.getPhoto(token, msUser.getMsId()));
        user.setUsername(msUser.getMail());
        UserEntity savedUser = userService.save(user);

        // Associate User & MS User
        msUser.setUser(savedUser);
        microsoftUserService.save(msUser);

        // If the User is not member of organization => Add it
        // Else -> replace by the new one
        OrganizationEntity organization = organizationService.findById(msTeam.getOrganization().getId());
        organization.getMembers().stream().filter(member -> member.getId().equals(savedUser.getId()))
                .findAny()
                .ifPresentOrElse(
                        member -> member = savedUser,
                        () -> organization.getMembers().add(savedUser)
                );
        organizationService.save(organization);

        // Create accounts for usable budgets
        accountService.createUserAccountsForUsableBudgets(user, organization.getId());

        // Generate token
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authService.getAuthorities(user.getId()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new AuthenticationResponseModel(jwtTokenUtil.generateToken(authentication));
    }

}
