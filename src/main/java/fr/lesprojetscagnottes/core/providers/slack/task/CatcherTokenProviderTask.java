package fr.lesprojetscagnottes.core.providers.slack.task;

import fr.lesprojetscagnottes.core.authentication.service.AuthService;
import fr.lesprojetscagnottes.core.common.security.TokenProvider;
import fr.lesprojetscagnottes.core.providers.slack.service.CatcherService;
import fr.lesprojetscagnottes.core.user.entity.UserEntity;
import fr.lesprojetscagnottes.core.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;

@Slf4j
@Component
public class CatcherTokenProviderTask extends TimerTask {

    @Autowired
    private AuthService authService;

    @Autowired
    private CatcherService catcherService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenProvider jwtTokenUtil;

    @Override
    public void run() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 30);
        Date nextYear = cal.getTime();
        UserEntity admin = userService.findByEmail("admin");
        Authentication authentication = new UsernamePasswordAuthenticationToken(admin.getEmail(), null, authService.getAuthorities(admin.getId()));
        log.debug("Send token to slack-events-catcher");
        catcherService.sendToken(jwtTokenUtil.generateToken(authentication, nextYear));
    }

}
