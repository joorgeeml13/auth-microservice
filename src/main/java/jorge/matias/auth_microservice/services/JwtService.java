package jorge.matias.auth_microservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;



@Service
public class JwtService {
    
    @Value("${JWT_PRIVATE_KEY_PATH:classpath:certs/private_key.pem}")
    private Resource privateKeyResource;

    @Value("${JWT_PUBLIC_KEY_PATH:classpath:certs/public_key.pem}")
    private Resource publicKeyResource;

    @Value("${security.jwt.access-expiration-mins:15}")
    private long jwtExpirationMins;

    @Value("${security.jwt.refresh-expiration-days:7}")
    private long refreshExpirationDays;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private long jwtExpirationMillis;
    private long refreshExpirationMillis;

    @PostConstruct
    protected void init() throws Exception {
        this.privateKey = loadPrivateKey(privateKeyResource);
        this.publicKey = loadPublicKey(publicKeyResource);

        this.jwtExpirationMillis = jwtExpirationMins * 60 * 1000;
        this.refreshExpirationMillis = refreshExpirationDays * 24 * 60 * 60 * 1000;
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, jwtExpirationMins);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails, refreshExpirationDays);
    }
    
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subject = extractSubject(token);
        return (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private String buildToken(Map extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) 
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256) // USAMOS LA PRIVADA Y RS256
                .compact();
    }

    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // Quitamos saltos de línea
        
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new X509EncodedKeySpec(encoded));
    }
}