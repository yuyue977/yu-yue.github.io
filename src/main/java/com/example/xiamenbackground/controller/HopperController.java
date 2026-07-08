package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.Hopper;
import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.mqtt.MqttService;
import com.example.xiamenbackground.repository.HopperRepository;
import com.example.xiamenbackground.service.DataPushService;
import com.example.xiamenbackground.util.PermissionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hoppers")
public class HopperController {

    private final HopperRepository hopperRepository;
    private final DataPushService dataPushService;
    private final MqttService mqttService;

    public HopperController(HopperRepository hopperRepository, DataPushService dataPushService, MqttService mqttService) {
        this.hopperRepository = hopperRepository;
        this.dataPushService = dataPushService;
        this.mqttService = mqttService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("hoppers", hopperRepository.findAll());
        model.addAttribute("user", loginUser);
        return "hopper/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam Float douId, @RequestParam String materialInfo,
                      @RequestParam Float remain, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=hopper-tab";

        Hopper hopper = new Hopper();
        hopper.setDouId(douId);
        hopper.setMaterialInfo(materialInfo);
        hopper.setRemain(remain);
        hopperRepository.save(hopper);
        return "redirect:/equipment?activeTab=hopper-tab";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @RequestParam Float douId,
                         @RequestParam String materialInfo, @RequestParam Float remain,
                         HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=hopper-tab";

        Hopper hopper = hopperRepository.findById(id).orElse(null);
        if (hopper != null) {
            hopper.setDouId(douId);
            hopper.setMaterialInfo(materialInfo);
            hopper.setRemain(remain);
            hopperRepository.save(hopper);
        }
        return "redirect:/equipment?activeTab=hopper-tab";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=hopper-tab";
        hopperRepository.deleteById(id);
        return "redirect:/equipment?activeTab=hopper-tab";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=hopper-tab";

        Hopper hopper = hopperRepository.findById(id).orElse(null);
        if (hopper != null) {
            // 切换 douStatus，并同步更新 isLocked
            hopper.setDouStatus(!hopper.getDouStatus());
            hopper.setIsLocked(String.valueOf(!hopper.getDouStatus()));
            hopperRepository.save(hopper);
            // 发布 MQTT 通知，告知 Unity 客户端料斗锁定状态变更
            String douIdStr = String.valueOf(hopper.getDouId().intValue());
            mqttService.publish("/xmLoader/web/deviceLock", "{\"type\":\"dou\",\"id\":\"" + douIdStr + "\",\"isLocked\":" + hopper.getIsLocked() + "}");
            // 推送最新数据到 WebSocket，实时更新生产信息页面
            dataPushService.pushLatestData();
        }
        return "redirect:/equipment?activeTab=hopper-tab";
    }
}
