package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.Silo;
import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.mqtt.MqttService;
import com.example.xiamenbackground.repository.SiloRepository;
import com.example.xiamenbackground.service.DataPushService;
import com.example.xiamenbackground.util.PermissionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/silos")
public class SiloController {

    private final SiloRepository siloRepository;
    private final MqttService mqttService;
    private final DataPushService dataPushService;

    public SiloController(SiloRepository siloRepository, MqttService mqttService, DataPushService dataPushService) {
        this.siloRepository = siloRepository;
        this.mqttService = mqttService;
        this.dataPushService = dataPushService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("silos", siloRepository.findAll());
        model.addAttribute("user", loginUser);
        return "silo/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam String cangId, @RequestParam String materialInfo,
                      @RequestParam Float remain, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=silo-tab";

        Silo silo = new Silo();
        silo.setCangId(cangId);
        silo.setMaterialInfo(materialInfo);
        silo.setRemain(remain);
        silo.setIsLocked(false);
        siloRepository.save(silo);
        mqttService.publish("/xmLoader/web/refresh", "refresh");
        return "redirect:/equipment?activeTab=silo-tab";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @RequestParam String cangId,
                         @RequestParam String materialInfo, @RequestParam Float remain,
                         HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=silo-tab";

        Silo silo = siloRepository.findById(id).orElse(null);
        if (silo != null) {
            silo.setCangId(cangId);
            silo.setMaterialInfo(materialInfo);
            silo.setRemain(remain);
            siloRepository.save(silo);
            mqttService.publish("/xmLoader/web/refresh", "refresh");
        }
        return "redirect:/equipment?activeTab=silo-tab";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=silo-tab";
        Silo silo = siloRepository.findById(id).orElse(null);
        if (silo != null) {
            // 切换 isLocked，并同步更新 cangStatus
            silo.setIsLocked(!silo.getIsLocked());
            silo.setCangStatus(!silo.getIsLocked());
            siloRepository.save(silo);
            mqttService.publish("/xmLoader/web/deviceLock", "{\"type\":\"cang\",\"id\":\"" + silo.getCangId() + "\",\"isLocked\":" + silo.getIsLocked() + "}");
            // 推送最新数据到 WebSocket，实时更新生产信息页面
            dataPushService.pushLatestData();
        }
        return "redirect:/equipment?activeTab=silo-tab";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=silo-tab";
        siloRepository.deleteById(id);
        mqttService.publish("/xmLoader/web/refresh", "refresh");
        return "redirect:/equipment?activeTab=silo-tab";
    }
}
