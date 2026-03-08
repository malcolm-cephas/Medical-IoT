package com.malcolm.medicaliot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for JSON Web Token (JWT) handling.
 * Responsible for generating, signing, and validating tokens.
 */
@Service
public class JwtService {

    // Secret key used for signing tokens.
    // In a production environment, this should be stored securely (e.g.,
    // environment variables).
    // The current key corresponds to a 256-bit key suitable for the HS256
    // algorithm.
    private static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    /**
     * Decodes the secret key into a cryptographic Key object.
     * 
     * @return SecretKey for HMAC-SHA algorithms.
     */
    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET));
    }

    /**
     * Extracts the username (Subject) from a valid JWT token.
     * 
     * @param token The JWT token string.
     * @return The username contained in the token subject.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract a specific claim from the token.
     * 
     * @param token          The JWT token.
     * @param claimsResolver A function to extract the desired type from Claims.
     * @return The extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a token for a user without extra claims.
     * 
     * @param username The username of the user.
     * @param role     The role of the user.
     * @return A signed JWT string.
     */
    public String generateToken(String username, String role) {
        return generateToken(new HashMap<>(), username, role);
    }

    /**
     * Generates a signed JWT for a given user.
     * 
     * @param extraClaims Additional data to embed in the token payload.
     * @param username    The subject of the token (user's unique identifier).
     * @param role        The user's role, embedded as a claim for frontend use.
     * @return A signed JWT string.
     */
    public String generateToken(Map<String, Object> extraClaims, String username, String role) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .claim("role", role) // Embeds role into token for easy client-side access
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token valid for 10 hours
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a token against a username.
     * 
     * @param token    The JWT token.
     * @param username The username from the database/user details.
     * @return True if the token's subject matches the username and is not expired;
     *         otherwise False.
     */
    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    /**
     * Checks if the token has passed its expiration time.
     * 
     * @param token The JWT token.
     * @return True if expired, False otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses the token to retrieve all claims (payload data).
     * Verifies the signature using the secret key.
     * 
     * @throws JwtException if the token is invalid or signature doesn't match.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
