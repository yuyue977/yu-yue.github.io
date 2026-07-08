package com.example.xiamenbackground.service;

import com.example.xiamenbackground.entity.Hopper;
import com.example.xiamenbackground.entity.Silo;
import com.example.xiamenbackground.repository.HopperRepository;
import com.example.xiamenbackground.repository.SiloRepository;
import com.example.xiamenbackground.websocket.DataWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataPushService {

    @Autowired
    private SiloRepository siloRepository;

    @Autowired
    private HopperRepository hopperRepository;

    @Autowired
    private DataWebSocketHandler webSocketHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void pushLatestData() {
        List<Silo> silos = siloRepository.findAll();
        List<Hopper> hoppers = hopperRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("silos", silos.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("cangId", s.getCangId());
            m.put("materialInfo", s.getMaterialInfo());
            m.put("remain", s.getRemain());
            m.put("cangCapacity", s.getCangCapacity());
            m.put("cangStatus", s.getCangStatus());
            m.put("isLocked", s.getIsLocked());
            return m;
        }).collect(Collectors.toList()));

        data.put("hoppers", hoppers.stream().map(h -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", h.getId());
            m.put("douId", h.getDouId());
            m.put("materialInfo", h.getMaterialInfo());
            m.put("remain", h.getRemain());
            m.put("douCapacity", h.getDouCapacity());
            m.put("douStatus", h.getDouStatus());
            m.put("isLocked", h.getIsLocked());
            return m;
        }).collect(Collectors.toList()));

        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            System.err.println("=== JSON 序列化失败: " + e.getMessage() + " ===");
            return;
        }
        webSocketHandler.broadcast(json);
        System.out.println("=== 已推送数据到前端, silos: " + silos.size() + ", hoppers: " + hoppers.size() + " ===");
    }
}
