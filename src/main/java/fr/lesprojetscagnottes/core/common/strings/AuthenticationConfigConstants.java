package fr.lesprojetscagnottes.core.common.strings;

public class AuthenticationConfigConstants {
    public static final String SECRET = "LesProjetsCagnottes2019";
    public static final long EXPIRATION_TIME = 5*60*60;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String AUTHORITIES_KEY = "scopes";
}
