//package me.yannis.websocket.config;
//
//import com.vip.vjtools.vjkit.mapper.JsonMapper;
//import me.j360.framework.boot.shiro.dao.SessionStorageDAO;
//import me.j360.framework.common.web.context.DefaultJwtSessionUser;
//import me.j360.framework.common.web.context.DefaultSessionUser;
//import me.j360.framework.common.web.context.SessionUser;
//import org.redisson.api.RBucket;
//import org.redisson.api.RedissonClient;
//import org.redisson.client.codec.StringCodec;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import world.oasis.base.constant.AppConfig;
//
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//
//@Component
//public class RedissonSessionStorageDAO implements SessionStorageDAO {
//
//    @Autowired
//    private RedissonClient redissonClient;
//
//
//    public RedissonSessionStorageDAO() {
//
//    }
//
//    @Override
//    public void save(SessionUser sessionUser) {
//        RBucket<String> sessionBucket = redissonClient.getBucket(String.format(AppConfig.USER_SESSION, ((DefaultSessionUser) sessionUser).getSessionId()), StringCodec.INSTANCE);
//        sessionBucket.set(JsonMapper.INSTANCE.toJson(sessionUser), 30, TimeUnit.DAYS);
//    }
//
//    @Override
//    public SessionUser get(String sessionId) {
//        RBucket<String> sessionBucket = redissonClient.getBucket(String.format(AppConfig.USER_SESSION, sessionId), StringCodec.INSTANCE);
//        DefaultJwtSessionUser defaultSessionUser = JsonMapper.INSTANCE.fromJson(sessionBucket.get(), DefaultJwtSessionUser.class);
//        return defaultSessionUser;
//    }
//
//    @Override
//    public Set<String> roles(Long principal) {
//        return null;
//    }
//
//    public void setRedissonClient(RedissonClient redissonClient) {
//        this.redissonClient = redissonClient;
//    }
//
//}
