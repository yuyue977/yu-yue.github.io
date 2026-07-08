package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.Loader;
import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.LoaderRepository;
import com.example.xiamenbackground.util.PermissionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/loaders")
public class LoaderController {

    private final LoaderRepository loaderRepository;

    public LoaderController(LoaderRepository loaderRepository) {
        this.loaderRepository = loaderRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        model.addAttribute("loaders", loaderRepository.findAll());
        model.addAttribute("user", loginUser);
        return "loader/list";
    }

    @PostMapping("/add")
    public String add(@RequestParam String loaderNumber, @RequestParam String loaderModel,
                      @RequestParam Float loaderBucketCapacity, @RequestParam String deviceId,
                      @RequestParam Integer battery, @RequestParam String status,
                      HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=loader-tab";

        Loader loader = new Loader();
        loader.setLoaderNumber(loaderNumber);
        loader.setLoaderModel(loaderModel);
        loader.setLoaderBucketCapacity(loaderBucketCapacity);
        loader.setDeviceId(deviceId);
        loader.setBattery(battery);
        loader.setStatus(status);
        loaderRepository.save(loader);
        return "redirect:/equipment";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @RequestParam String loaderNumber,
                         @RequestParam String loaderModel, @RequestParam Float loaderBucketCapacity,
                         @RequestParam Integer battery, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=loader-tab";

        Loader loader = loaderRepository.findById(id).orElse(null);
        if (loader != null) {
            loader.setLoaderNumber(loaderNumber);
            loader.setLoaderModel(loaderModel);
            loader.setLoaderBucketCapacity(loaderBucketCapacity);
            loader.setBattery(battery);
            loaderRepository.save(loader);
        }
        return "redirect:/equipment";
    }

    @PostMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=loader-tab";
        Loader loader = loaderRepository.findById(id).orElse(null);
        if (loader != null) {
            String current = loader.getStatus();
            String next;
            if ("作业中".equals(current)) {
                next = "停止";
            } else if ("停止".equals(current)) {
                next = "待机";
            } else {
                next = "作业中";
            }
            loader.setStatus(next);
            loaderRepository.save(loader);
        }
        return "redirect:/equipment";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";
        if (!PermissionUtil.canEditEquipment(session)) return "redirect:/equipment?activeTab=loader-tab";
        loaderRepository.deleteById(id);
        return "redirect:/equipment?activeTab=loader-tab";
    }
}
