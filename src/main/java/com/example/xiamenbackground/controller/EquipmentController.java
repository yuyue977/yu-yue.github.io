package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.Hopper;
import com.example.xiamenbackground.entity.Loader;
import com.example.xiamenbackground.entity.Silo;
import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.HopperRepository;
import com.example.xiamenbackground.repository.LoaderRepository;
import com.example.xiamenbackground.repository.SiloRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class EquipmentController {

    @Autowired
    private SiloRepository siloRepository;

    @Autowired
    private HopperRepository hopperRepository;

    @Autowired
    private LoaderRepository loaderRepository;

    /**
     * 设备基础信息页面 - 包含料仓、料斗、装载机三个Tab，支持搜索
     */
    @GetMapping("/equipment")
    public String equipmentPage(
            // 装载机搜索参数
            @RequestParam(required = false) String loaderNumber,
            @RequestParam(required = false) String loaderModel,
            @RequestParam(required = false) String loaderStatus,
            // 料仓搜索参数
            @RequestParam(required = false) String cangId,
            @RequestParam(required = false) String siloStatus,
            // 料斗搜索参数
            @RequestParam(required = false) String douId,
            @RequestParam(required = false) String hopperStatus,
            // 当前激活的Tab
            @RequestParam(required = false, defaultValue = "loader-tab") String activeTab,
            HttpSession session, Model model) {

        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        List<Loader> loaders = new ArrayList<>();
        List<Silo> silos = new ArrayList<>();
        List<Hopper> hoppers = new ArrayList<>();

        try {
            // 装载机搜索
            loaders = loaderRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (loaderNumber != null && !loaderNumber.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("loaderNumber"), "%" + loaderNumber.trim() + "%"));
                }
                if (loaderModel != null && !loaderModel.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("loaderModel"), "%" + loaderModel.trim() + "%"));
                }
                if (loaderStatus != null && !loaderStatus.trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), loaderStatus.trim()));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            loaders = new ArrayList<>();
        }

        try {
            // 料仓搜索
            silos = siloRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (cangId != null && !cangId.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("cangId"), "%" + cangId.trim() + "%"));
                }
                if (siloStatus != null && !siloStatus.trim().isEmpty()) {
                    boolean locked = "停用".equals(siloStatus.trim());
                    predicates.add(cb.equal(root.get("isLocked"), locked));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            silos = new ArrayList<>();
        }

        try {
            // 料斗搜索
            hoppers = hopperRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (douId != null && !douId.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("douId").as(String.class), "%" + douId.trim() + "%"));
                }
                if (hopperStatus != null && !hopperStatus.trim().isEmpty()) {
                    // hopperStatus: "启用" -> douStatus=true, "停用" -> douStatus=false
                    boolean enabled = "启用".equals(hopperStatus.trim());
                    predicates.add(cb.equal(root.get("douStatus"), enabled));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            hoppers = new ArrayList<>();
        }

        model.addAttribute("silos", silos);
        model.addAttribute("hoppers", hoppers);
        model.addAttribute("loaders", loaders);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", activeTab);

        // 回显搜索参数
        model.addAttribute("loaderNumber", loaderNumber);
        model.addAttribute("loaderModel", loaderModel);
        model.addAttribute("loaderStatus", loaderStatus);
        model.addAttribute("cangId", cangId);
        model.addAttribute("siloStatus", siloStatus);
        model.addAttribute("douId", douId);
        model.addAttribute("hopperStatus", hopperStatus);

        return "equipment";
    }
}
