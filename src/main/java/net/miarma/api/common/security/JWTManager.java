package net.miarma.api.common.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import net.miarma.api.common.ConfigManager;
import net.miarma.api.common.Constants;
import net.miarma.api.core.entities.UserEntity;

import java.util.Date;

public class JWTManager {

	private final ConfigManager config = ConfigManager.getInstance();
    private final Algorithm algorithm; 
    private final JWTVerifier verifier;
    private static JWTManager instance;
    
    private JWTManager() {
		this.algorithm = Algorithm.HMAC256(config.getStringProperty("jwt.secret"));
		this.verifier = JWT.require(algorithm).build();
	}
    
    public static synchronized JWTManager getInstance() {
		if (instance == null) {
			instance = new JWTManager();
		}
		return instance;
	}
    
    public String generateToken(UserEntity user, boolean keepLoggedIn) {
    	final long EXPIRATION_TIME_MS = 1000L * (keepLoggedIn ? config.getIntProperty("jwt.expiration") : config.getIntProperty("jwt.expiration.short"));
    	return JWT.create()
    	        .withSubject(user.getUser_name())
    	        .withClaim("userId", user.getUser_id())
    	        .withClaim("isAdmin", user.getRole() == Constants.CoreUserRole.ADMIN)
    	        .withIssuedAt(new Date())
    	        .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
    	        .sign(algorithm);
    }
    
    public DecodedJWT decodeWithoutVerification(String token) {
        return JWT.decode(token);
    }

    public boolean isValid(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isAdmin(String token) {
		try {
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getClaim("isAdmin").asBoolean();
		} catch (Exception e) {
			return false;
		}
	}

    public int getUserId(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("userId").asInt();
        } catch (Exception e) {
            return -1;
        }
    }

    public String getSubject(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

	public int extractUserId(String token) {
		try {
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getClaim("userId").asInt();
		} catch (Exception e) {
			return -1;
		}
	}
}
