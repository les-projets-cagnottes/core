package fr.lesprojetscagnottes.core.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.lesprojetscagnottes.core.common.strings.AuthenticationConfigConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.lesprojetscagnottes.core.common.strings.AuthenticationConfigConstants.*;

@Slf4j
@Component
public class TokenProvider implements Serializable {

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        // here you specify tokens, for that the expiration is ignored
        return false;
    }

    private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
        return (lastPasswordReset != null && created.before(lastPasswordReset));
    }

    public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
        final Date created = getIssuedAtDateFromToken(token);
        return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset)
                && (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public String generateToken(Authentication authentication, Date expiration) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create()
                .withSubject(authentication.getName())
                .withClaim(AUTHORITIES_KEY, authorities)
                .withExpiresAt(expiration)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .sign(Algorithm.HMAC512(AuthenticationConfigConstants.SECRET.getBytes()));
    }

    public String generateToken(Authentication authentication) {
        return generateToken(authentication, new Date(System.currentTimeMillis() + EXPIRATION_TIME * 1000));
    }

    public String refreshToken(String token) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + EXPIRATION_TIME * 1000);

        SimpleDateFormat dt1 = new SimpleDateFormat("yyyyy-MM-dd");
        log.debug("createdDate = " + dt1.format(createdDate));
        log.debug("expirationDate = " + dt1.format(expirationDate));

        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        DecodedJWT jwt = JWT.decode(token);
        return JWT.create()
                .withSubject(jwt.getSubject())
                .withClaim(AUTHORITIES_KEY, jwt.getClaim(AUTHORITIES_KEY).asString())
                .withExpiresAt(expirationDate)
                .withIssuedAt(createdDate)
                .sign(Algorithm.HMAC512(AuthenticationConfigConstants.SECRET.getBytes()));
    }

}