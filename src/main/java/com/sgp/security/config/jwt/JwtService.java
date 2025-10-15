package com.sgp.security.config.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // Se cargará desde application.properties (ej. jwt.secret-key=ASUPERLONGANDSECUREBASE64KEY)
    @Value("${jwt.secret-key}")
    private String secretKey;

    // El token expira en 24 horas (en milisegundos)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // --- 1. Extracción de Claims (Información) ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- 2. Generación de Token ---

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        // Incluimos el rol en los claims extra
        // 👉 Convertir authorities a una lista de strings
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList());

        extraClaims.put("roles", roles); // Nota: cambiamos "role" a "roles" y es una lista

        // 🔎 También incluimos el ID del usuario si es de tipo User (nuestra entidad)
        if (userDetails instanceof com.sgp.user.model.User user) {
            extraClaims.put("userId", user.getId()); // Usa el getter de tu entidad
            extraClaims.put("forced_change", user.isForcePasswordChange());
        }

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())  //-- esto ees el Email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Expira en 24h
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- 3. Validación de Token ---

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // --- 4. Clave de Firma ---

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // -*-- MEtodos de ayuda ---

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

}