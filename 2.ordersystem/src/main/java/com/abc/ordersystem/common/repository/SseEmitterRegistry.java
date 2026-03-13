package com.abc.ordersystem.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
// 연결되면 emitter을 emitterMap에 저장하고, 연결 끊기면 삭제.
public class SseEmitterRegistry {

//    SseEmitter객체는 사용자의 연결정보(ip, macAddress 등)을 의미
//    ConcurrentHashMap은 Thread-Safe한 map(동시성 이슈 발생x)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    public void addSseEmitter(String email, SseEmitter sseEmitter){
        this.emitterMap.put(email, sseEmitter);
        System.out.println(this.emitterMap.size());
    }

    public SseEmitter getEmitter(String email){
        return this.emitterMap.get(email);
    }

    public void removeEmitter(String email){
        this.emitterMap.remove(email);
        System.out.println(this.emitterMap.size());

    }
}
