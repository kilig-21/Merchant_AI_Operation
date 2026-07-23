package org.example.merchant_ai_operation.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Service
public class JwtService {
    private final Algorithm algorithm;//保存 JWT 签名算法，比如 HMAC256。
    private final JWTVerifier verifier;//保存 JWT 校验器，后面解析 token 时用。
    private final long expireHours;//保存 token 有效期，生成 token 时用。



    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expire-hours}") long expireHours
    ) {

        //用你的密钥创建一个签名算法。可以理解成：以后生成 token 时，要用这把“暗号钥匙”盖章。
        this.algorithm = Algorithm.HMAC256(secret);

        //创建一个校验器。以后别人带 token 来访问接口，我们就用同一把“暗号钥匙”检查这个 token 是不是我们发的、有没有被改过。
        this.verifier = JWT.require(algorithm).build();

        //把配置里的有效期保存下来。后面生成 token 时会用它计算过期时间
        this.expireHours = expireHours;
    }



    //生成token:
    public String createToken(Long userId,Long tenantId,String userType){
        return JWT.create()//开始创建一个 JWT。

                //把用户 ID 放进 token 的 sub 字段。sub 通常表示“这个 token 属于谁”。
                .withSubject(String.valueOf(userId))

                //额外放入租户 ID 和用户类型。后面做商家权限、租户隔离会用到。
                .withClaim("tenantId", tenantId)
                .withClaim("userType", userType)

                //记录签发时间。
                .withIssuedAt(Instant.now())

                //记录过期时间，比如现在开始 2 小时后过期。
                .withExpiresAt(Instant.now().plus(expireHours, ChronoUnit.HOURS))

                //用前面创建的算法和密钥签名，最终生成字符串 token。
                .sign(algorithm);
    }


    //这个方法负责“读 token 并校验”。
    public LoginPrincipal parse(String token){
        //用校验器验证 token。如果 token 被改过、过期、签名不对，这里会直接抛异常。验证通过后，得到一个 DecodedJWT，也就是“解开的 token 内容”。
        //DecodedJWT:经过校验、解码成功之后，承载 JWT 全部信息的对象；把字符串 token 解析成可读取的 Java 对象。经过校验、解码成功之后，承载 JWT 全部信息的对象；把字符串 token 解析成可读取的 Java 对象。
        DecodedJWT jwt = verifier.verify(token);

        Long tenantId = jwt.getClaim("tenantId").isNull()
                ? null : jwt.getClaim("tenantId").asLong();//将其转化称long


        //把 token 里的用户 ID、租户 ID、用户类型，重新组装成我们后端好用的 LoginPrincipal
        return new LoginPrincipal(
                Long.valueOf(jwt.getSubject()),
                tenantId,
                jwt.getClaim("userType").asString()
        );

    }

}
