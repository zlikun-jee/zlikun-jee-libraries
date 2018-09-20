package com.zlikun.jee;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author zlikun
 * @date 2018-09-20 16:08
 */
@Slf4j
public class JWTTest {

    @Test
    public void hmac() {

        // HMAC
        Algorithm algorithm = Algorithm.HMAC256("admin#2018@zlikun.com");

        // Create and Sign a Token
        Map<String, Object> headers = new HashMap<>(4);
        headers.put("clientId", "C0000001");
        String token = JWT.create()
                .withIssuer("zlikun")
                .withHeader(headers)
                .withClaim("card", "X0000001")
                .withClaim("amount", 17.5)
                .sign(algorithm);

        // token => eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImNsaWVudElkIjoiQzAwMDAwMDEifQ.eyJhbW91bnQiOjE3LjUsImlzcyI6InpsaWt1biIsImNhcmQiOiJYMDAwMDAwMSJ9.CszVrXeBP0MmCF5PEjlKGkfg7EdL_4t6Q8O5rqGPPbs
        log.info("token => {}", token);

        // Verify a Token，验签与解码的区别在于，比解码多了一步验签（把token中的最后一部分修改后，单独解码是OK的，但验签会失败）
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("zlikun")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        // header = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImNsaWVudElkIjoiQzAwMDAwMDEifQ,
        // payload = eyJhbW91bnQiOjE3LjUsImlzcyI6InpsaWt1biIsImNhcmQiOiJYMDAwMDAwMSJ9,
        // signature = CszVrXeBP0MmCF5PEjlKGkfg7EdL_4t6Q8O5rqGPPbs,
        // token = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImNsaWVudElkIjoiQzAwMDAwMDEifQ.eyJhbW91bnQiOjE3LjUsImlzcyI6InpsaWt1biIsImNhcmQiOiJYMDAwMDAwMSJ9.CszVrXeBP0MmCF5PEjlKGkfg7EdL_4t6Q8O5rqGPPbs,
        // type = JWT
        log.info("header = {}, payload = {}, signature = {}, token = {}, type = {}",
                jwt.getHeader(), jwt.getPayload(), jwt.getSignature(), jwt.getToken(), jwt.getType());
        assertEquals("X0000001", jwt.getClaim("card").asString());
        assertEquals(17.5, jwt.getClaim("amount").asDouble(), 0);

        // Decode a Token，如果不考虑安全问题，直接使用解码（不验签）是可以的
        jwt = JWT.decode(token);
        // header = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImNsaWVudElkIjoiQzAwMDAwMDEifQ,
        // payload = eyJhbW91bnQiOjE3LjUsImlzcyI6InpsaWt1biIsImNhcmQiOiJYMDAwMDAwMSJ9,
        // signature = CszVrXeBP0MmCF5PEjlKGkfg7EdL_4t6Q8O5rqGPPbs,
        // token = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImNsaWVudElkIjoiQzAwMDAwMDEifQ.eyJhbW91bnQiOjE3LjUsImlzcyI6InpsaWt1biIsImNhcmQiOiJYMDAwMDAwMSJ9.CszVrXeBP0MmCF5PEjlKGkfg7EdL_4t6Q8O5rqGPPbs,
        // type = JWT
        log.info("header = {}, payload = {}, signature = {}, token = {}, type = {}",
                jwt.getHeader(), jwt.getPayload(), jwt.getSignature(), jwt.getToken(), jwt.getType());

        assertEquals("X0000001", jwt.getClaim("card").asString());
        assertEquals(17.5, jwt.getClaim("amount").asDouble(), 0);

    }

    @Test
    public void rsa() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // RSA
        RSAKeyGenerator generator = new RSAKeyGenerator();
        RSAPublicKey publicKey = generator.getPublicKey();
        RSAPrivateKey privateKey = generator.getPrivateKey();

        Algorithm algorithm = Algorithm.RSA256(publicKey, privateKey);

        // encode
        String token = JWT.create()
                .withClaim("author", "zlikun")
                .sign(algorithm);
        log.info("token = {}", token);

        // decode
        DecodedJWT jwt = JWT.decode(token);
        assertEquals("zlikun", jwt.getClaim("author").asString());

        // verify
        jwt = JWT.require(algorithm).build().verify(token);
        assertEquals("zlikun", jwt.getClaim("author").asString());

    }

    /**
     * 生成公私钥
     */
    private static class RSAKeyGenerator {

        private static final String KEY_RSA = "RSA";
        private static final BouncyCastleProvider provider = new BouncyCastleProvider();
        private KeyFactory keyFactory;
        private KeyPair keyPair;

        public RSAKeyGenerator() throws NoSuchAlgorithmException {
            keyFactory = KeyFactory.getInstance(KEY_RSA, provider);
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_RSA, provider);
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
        }

        public RSAPublicKey getPublicKey() throws InvalidKeySpecException {
            byte[] encode = keyPair.getPublic().getEncoded();
            log.info("publicKey = {}", Base64.getEncoder().encodeToString(encode));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encode);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }

        public RSAPrivateKey getPrivateKey() throws InvalidKeySpecException {
            byte[] encode = keyPair.getPrivate().getEncoded();
            log.info("privateKey = {}", Base64.getEncoder().encodeToString(encode));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encode);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }

    }

}
