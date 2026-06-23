package dujiacun.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    // 用于签名的密钥（实际应保存在安全位置，例如环境变量或配置文件）
    // 这里使用一个足够长的 Base64 编码的字符串作为示例
//    @Value("${jwt.SECRET_KEY_STRING}")
    private static String SECRET_KEY_STRING = "mySuperSecretKeyForHS256AlgorithmShouldBeAtLeast256BitsLong!";
    private static SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    // 简单示例：生成只有主题和基本声明的 JWT
//    public static String generateSimpleToken(String userId, long ttlMillis) {
//        return Jwts.builder()
//                .subject(userId)
//                .claim("role", "user")
//                .claim("loginTime", System.currentTimeMillis())
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + ttlMillis))
//                .signWith(SECRET_KEY)
//                .compact();
//    }

    /**
     * 生成 JWT
     * @param subject  主题（通常是用户ID或用户名）
     * @param claims   自定义声明（键值对）
     * @param ttlMillis 有效期（毫秒）
     * @return JWT 字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims, long ttlMillis) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiration = Date.from(now.plusMillis(ttlMillis));

        return Jwts.builder()
                .subject(subject)                     // 设置主题（sub）
                .claims(claims)                       // 设置自定义声明
                .issuedAt(issuedAt)                   // 签发时间（iat）
                .expiration(expiration)               // 过期时间（exp）
                .signWith(SECRET_KEY, Jwts.SIG.HS256) // 使用 HS256 签名算法
                .compact();
    }

    /**
     * 解析 JWT 并返回所有声明（Claims）
     * @param token JWT 字符串
     * @return Claims 对象（包含 subject、过期时间、自定义声明等）
     * @throws JwtException 如果 token 无效、过期或签名错误
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)          // 使用相同的密钥验证签名
                .build()
                .parseSignedClaims(token)        // 解析并验证
                .getPayload();                   // 获取 Claims 部分
    }

    /**
     * 从 token 中提取主题（sub）
     */
    public static String getSubject(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 token 中提取自定义声明（根据 key）
     */
    public static Object getClaim(String token, String claimKey) {
        return parseToken(token).get(claimKey);
    }

    /**
     * 检查 token 是否已过期
     */
    public static boolean isTokenExpired(String token) {
        Date expiration = parseToken(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 验证 token 是否有效（签名正确 + 未过期）
     */
    public static boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
