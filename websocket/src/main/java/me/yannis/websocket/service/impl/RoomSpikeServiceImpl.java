//package me.yannis.websocket.service.impl;
//
//
//import com.google.common.collect.Sets;
//import lombok.extern.slf4j.Slf4j;
//import me.j360.framework.base.domain.rpc.query.DefaultApiPageQuery;
//import me.j360.framework.base.domain.rpc.result.DefaultPageResult;
//import me.j360.framework.base.domain.rpc.result.DefaultResult;
//import me.j360.framework.core.kit.mapper.orika.BeanMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Service;
//import world.oasis.base.message.request.PushToManyMessageRequest;
//import world.oasis.game.dao.*;
//import world.oasis.user.entity.DO.*;
//import world.oasis.user.entity.dto.RoomSpikeDto;
//import world.oasis.user.service.RoomSpikeService;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Slf4j
//@Service
//public class RoomSpikeServiceImpl implements RoomSpikeService {
//
//
//    @Autowired
//    private RoomInfoDao dao;
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//
//    @Override
//    public DefaultResult<String> create(Long uid, String name, Long mapId) {
//        Long now = System.currentTimeMillis();
//        RoomSpikeDO roomSpikeDO = new RoomSpikeDO();
//        roomSpikeDO.setUid(uid);
//        roomSpikeDO.setName(name);
//        roomSpikeDO.setMapId(mapId);
//        roomSpikeDO.setUids(Sets.newHashSet(uid));
//        roomSpikeDO.setStatus(0);
//        roomSpikeDO.setIsDel(0);
//        roomSpikeDO.setType(0);
//        roomSpikeDO.setCount(1);
//        roomSpikeDO.setCreateTime(now);
//        roomSpikeDO.setUpdateTime(now);
//        mongoTemplate.save(roomSpikeDO).getId();
//        return DefaultResult.success(roomSpikeDO.getId());
//    }
//
//    @Override
//    public DefaultPageResult<RoomSpikeDto> list(DefaultApiPageQuery defaultApiPageQuery) {
//        Query query = new Query(Criteria.where("status").is(1).and("isDel").is(0));
//        long count = mongoTemplate.count(query, RoomSpikeDO.class);
//        if (Objects.equals(0L, count)) {
//            return DefaultPageResult.success(0, Collections.EMPTY_LIST);
//        }
//        query.skip(defaultApiPageQuery.getOffset());
//        query.limit(defaultApiPageQuery.getSize());
//        query.with(Sort.by(Sort.Order.desc("_id")));
//        List<RoomSpikeDO> list = mongoTemplate.find(query, RoomSpikeDO.class);
//        List<RoomSpikeDto> roomSpikeDtos = BeanMapper.mapList(list, RoomSpikeDO.class, RoomSpikeDto.class);
//        return DefaultPageResult.success(roomSpikeDtos);
//    }
//
//    @Override
//    public DefaultResult<RoomSpikeDO> join(Long uid, String roomId) {
//        Query query = new Query(Criteria.where("_id").is(roomId).and("isDel").is(0));
//        RoomSpikeDO roomSpikeDO = mongoTemplate.findOne(query, RoomSpikeDO.class);
//        if (Objects.isNull(roomSpikeDO)) {
//            return DefaultResult.success(null);
//        }
//        HashSet<Long> uids = roomSpikeDO.getUids();
//        Integer status = roomSpikeDO.getStatus();
//        Update update = new Update();
//        if (Objects.equals(0, status) && Objects.equals(uid, roomSpikeDO.getUid())) {
//            //自己是房主->激活房间
//            update.set("status", 1);
//        } else {
//            //进入
//            uids.add(uid);
//            update.set("uids", uids);
//            update.set("count", uids.size());
//        }
//        update.set("updateTime", System.currentTimeMillis());
//
//        long r = mongoTemplate.updateFirst(query, update, RoomSpikeDO.class).getModifiedCount();
//        if (r > 0) {
//            return DefaultResult.success(roomSpikeDO);
//        }
//        return DefaultResult.success(null);
//    }
//
//    @Override
//    public DefaultResult<RoomSpikeDO> exit(Long uid, String roomId) {
//        Query query = new Query(Criteria.where("_id").is(roomId).and("isDel").is(0));
//        RoomSpikeDO roomSpikeDO = mongoTemplate.findOne(query, RoomSpikeDO.class);
//        if (Objects.isNull(roomSpikeDO)) {
//            return DefaultResult.success(null);
//        }
//        Update update = new Update();
//        //离开
//        HashSet<Long> uids = roomSpikeDO.getUids();
//        uids.remove(uid);
//        update.set("uids", uids);
//        update.set("count", uids.size());
//        if (Objects.equals(1, roomSpikeDO.getCount())) {
//            update.set("isDel", 1);
//        }
//        update.set("updateTime", System.currentTimeMillis());
//
//        long r = mongoTemplate.updateFirst(query, update, RoomSpikeDO.class).getModifiedCount();
//        if (r > 0) {
//            return DefaultResult.success(roomSpikeDO);
//        }
//        return DefaultResult.success(null);
//    }
//
//    @Override
//    public DefaultResult<RoomSpikeDO> exitOnClone(Long uid) {
//        Query query = new Query(Criteria.where("isDel").is(0).and("status").is(1).and("uids").in(uid));
//
//        RoomSpikeDO roomSpikeDO = mongoTemplate.findOne(query, RoomSpikeDO.class);
//        if (Objects.isNull(roomSpikeDO)) {
//            return DefaultResult.success(null);
//        }
//        Update update = new Update();
//        //离开
//        HashSet<Long> uids = roomSpikeDO.getUids();
//        uids.remove(uid);
//        update.set("uids", uids);
//        update.set("count", uids.size());
//        if (Objects.equals(1, roomSpikeDO.getCount())) {
//            update.set("isDel", 1);
//        }
//
//        update.set("updateTime", System.currentTimeMillis());
//        long r = mongoTemplate.updateFirst(query, update, RoomSpikeDO.class).getModifiedCount();
//        if (r > 0) {
//            return DefaultResult.success(roomSpikeDO);
//        }
//        return DefaultResult.success(null);
//    }
//
//    @Override
//    public DefaultResult<Boolean> pushByRoom(PushToManyMessageRequest pushToManyMessageRequest, Command command, CopyOnWriteArraySet<WsClient> webSocketSet) {
////        Query query = new Query(Criteria.where("_id").is(pushToManyMessageRequest.getRoomId()));
////
////        RoomSpikeDO roomSpikeDO = mongoTemplate.findOne(query, RoomSpikeDO.class);
////        if (Objects.isNull(roomSpikeDO)) {
////            return DefaultResult.success(Boolean.TRUE);
////        }
////        HashSet<Long> uids = roomSpikeDO.getUids();
////        HashSet<Long> needUids = pushToManyMessageRequest.getUids();
////        if (CollectionUtils.isNotEmpty(needUids)) {
////            uids.retainAll(needUids);//交集
////        }
//        return null;
//    }
//}
