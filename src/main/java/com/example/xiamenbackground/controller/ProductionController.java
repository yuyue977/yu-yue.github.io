package com.example.xiamenbackground.controller;

import com.example.xiamenbackground.entity.Loader;
import com.example.xiamenbackground.entity.Silo;
import com.example.xiamenbackground.entity.Hopper;
import com.example.xiamenbackground.entity.User;
import com.example.xiamenbackground.repository.LoaderRepository;
import com.example.xiamenbackground.repository.SiloRepository;
import com.example.xiamenbackground.repository.HopperRepository;
import com.example.xiamenbackground.repository.TableTaskRepository;
import com.example.xiamenbackground.util.PermissionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ProductionController {

    @Autowired
    private LoaderRepository loaderRepository;

    @Autowired
    private SiloRepository siloRepository;

    @Autowired
    private HopperRepository hopperRepository;

    @Autowired
    private TableTaskRepository tableTaskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * 设备生产信息主页面
     */
    @GetMapping("/production")
    public String productionPage(
            @RequestParam(required = false) String loaderNumber,
            @RequestParam(required = false) String loaderModel,
            @RequestParam(required = false) String loaderProdStatus,
            @RequestParam(required = false) String cangId,
            @RequestParam(required = false) String siloProdStatus,
            @RequestParam(required = false) String douId,
            @RequestParam(required = false) String hopperProdStatus,
            @RequestParam(required = false, defaultValue = "loader-tab") String activeTab,
            HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        // 装载机搜索
        List<Loader> loaders;
        try {
            loaders = loaderRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (loaderNumber != null && !loaderNumber.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("loaderNumber"), "%" + loaderNumber.trim() + "%"));
                }
                if (loaderModel != null && !loaderModel.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("loaderModel"), "%" + loaderModel.trim() + "%"));
                }
                if (loaderProdStatus != null && !loaderProdStatus.trim().isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), loaderProdStatus.trim()));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            loaders = loaderRepository.findAll();
        }

        // 料仓搜索
        List<Silo> silos;
        try {
            silos = siloRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (cangId != null && !cangId.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("cangId"), "%" + cangId.trim() + "%"));
                }
                if (siloProdStatus != null && !siloProdStatus.trim().isEmpty()) {
                    boolean enabled = "启用".equals(siloProdStatus.trim());
                    predicates.add(cb.equal(root.get("cangStatus"), enabled));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            silos = siloRepository.findAll();
        }

        // 料斗搜索
        List<Hopper> hoppers;
        try {
            hoppers = hopperRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (douId != null && !douId.trim().isEmpty()) {
                    predicates.add(cb.like(root.get("douId").as(String.class), "%" + douId.trim() + "%"));
                }
                if (hopperProdStatus != null && !hopperProdStatus.trim().isEmpty()) {
                    boolean enabled = "启用".equals(hopperProdStatus.trim());
                    predicates.add(cb.equal(root.get("douStatus"), enabled));
                }
                return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
            });
        } catch (Exception e) {
            hoppers = hopperRepository.findAll();
        }

        model.addAttribute("loaders", loaders);
        model.addAttribute("silos", silos);
        model.addAttribute("hoppers", hoppers);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", activeTab);

        // 回显搜索参数
        model.addAttribute("loaderNumber", loaderNumber);
        model.addAttribute("loaderModel", loaderModel);
        model.addAttribute("loaderProdStatus", loaderProdStatus);
        model.addAttribute("cangId", cangId);
        model.addAttribute("siloProdStatus", siloProdStatus);
        model.addAttribute("douId", douId);
        model.addAttribute("hopperProdStatus", hopperProdStatus);

        return "production";
    }

    /**
     * 装载机导出选择页面
     */
    @GetMapping("/export-loader-select")
    public String exportLoaderSelectPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!PermissionUtil.canEditEquipment(session)) {
            return "redirect:/production?activeTab=loader-tab";
        }

        List<Loader> loaders = loaderRepository.findAll();
        model.addAttribute("loaders", loaders);
        model.addAttribute("user", user);

        return "export-loader-select";
    }

    /**
     * 料仓导出选择页面
     */
    @GetMapping("/export-silo-select")
    public String exportSiloSelectPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!PermissionUtil.canEditEquipment(session)) {
            return "redirect:/production?activeTab=silo-tab";
        }

        List<Silo> silos = siloRepository.findAll();
        
        // 计算每个料仓的24h和7天工作量
        Map<String, Float> work24hMap = new HashMap<>();
        Map<String, Float> work1wMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime24h = now.minusHours(24);
        LocalDateTime startTime1w = now.minusDays(7);

        for (Silo silo : silos) {
            Integer cangIdInt = safeParseInt(silo.getCangId());
            if (cangIdInt != null) {
                Float work24h = tableTaskRepository.sumVolumeByCangAndTimeRange(cangIdInt, startTime24h, now);
                Float work1w = tableTaskRepository.sumVolumeByCangAndTimeRange(cangIdInt, startTime1w, now);
                work24hMap.put(silo.getCangId(), work24h != null ? work24h : 0.0f);
                work1wMap.put(silo.getCangId(), work1w != null ? work1w : 0.0f);
            } else {
                work24hMap.put(silo.getCangId(), 0.0f);
                work1wMap.put(silo.getCangId(), 0.0f);
            }
        }

        model.addAttribute("silos", silos);
        model.addAttribute("work24hMap", work24hMap);
        model.addAttribute("work1wMap", work1wMap);
        model.addAttribute("user", user);

        return "export-silo-select";
    }

    /**
     * 料斗导出选择页面
     */
    @GetMapping("/export-hopper-select")
    public String exportHopperSelectPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }
        if (!PermissionUtil.canEditEquipment(session)) {
            return "redirect:/production?activeTab=hopper-tab";
        }

        List<Hopper> hoppers = hopperRepository.findAll();
        
        // 计算每个料斗的24h和7天工作量
        Map<Float, Float> work24hMap = new HashMap<>();
        Map<Float, Float> work1wMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime24h = now.minusHours(24);
        LocalDateTime startTime1w = now.minusDays(7);

        for (Hopper hopper : hoppers) {
            Float work24h = tableTaskRepository.sumVolumeByDouAndTimeRange(hopper.getDouId().intValue(), startTime24h, now);
            Float work1w = tableTaskRepository.sumVolumeByDouAndTimeRange(hopper.getDouId().intValue(), startTime1w, now);
            work24hMap.put(hopper.getDouId(), work24h != null ? work24h : 0.0f);
            work1wMap.put(hopper.getDouId(), work1w != null ? work1w : 0.0f);
        }

        model.addAttribute("hoppers", hoppers);
        model.addAttribute("work24hMap", work24hMap);
        model.addAttribute("work1wMap", work1wMap);
        model.addAttribute("user", user);

        return "export-hopper-select";
    }

    /**
     * 导出装载机数据为JSON文件
     */
    @PostMapping("/production/export-loader")
    public void exportLoaderData(@RequestParam("ids") List<Integer> ids,
                                 HttpSession session, HttpServletResponse response) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) { response.setStatus(401); return; }
        if (!PermissionUtil.canEditEquipment(session)) { response.setStatus(403); return; }

        List<Map<String, Object>> dataList = new ArrayList<>();
        int seq = 1;
        for (Integer id : ids) {
            Loader loader = loaderRepository.findById(id).orElse(null);
            if (loader != null) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("序号", seq++);
                item.put("设备编号", loader.getLoaderNumber());
                item.put("设备型号", loader.getLoaderModel());
                item.put("电池容量(KW/H)", loader.getBattery());
                if (loader.getRemain() != null) {
                    item.put("电池余量(%)", loader.getRemain());
                }
                item.put("设备状态", loader.getStatus());
                dataList.add(item);
            }
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));
        writeJsonResponse(response, dataList, ts + "装载机导出数据文件.json");
    }

    /**
     * 导出料仓数据为JSON文件
     */
    @PostMapping("/production/export-silo")
    public void exportSiloData(@RequestParam("ids") List<Integer> ids,
                               HttpSession session, HttpServletResponse response) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) { response.setStatus(401); return; }
        if (!PermissionUtil.canEditEquipment(session)) { response.setStatus(403); return; }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime24h = now.minusHours(24);
        LocalDateTime startTime1w = now.minusDays(7);

        List<Map<String, Object>> dataList = new ArrayList<>();
        int seq = 1;
        for (Integer id : ids) {
            Silo silo = siloRepository.findById(id).orElse(null);
            if (silo != null) {
                Float work24h = 0.0f;
                Float work1w = 0.0f;
                Integer cangIdInt = safeParseInt(silo.getCangId());
                if (cangIdInt != null) {
                    Float w24 = tableTaskRepository.sumVolumeByCangAndTimeRange(cangIdInt, startTime24h, now);
                    Float w1w = tableTaskRepository.sumVolumeByCangAndTimeRange(cangIdInt, startTime1w, now);
                    work24h = w24 != null ? w24 : 0.0f;
                    work1w = w1w != null ? w1w : 0.0f;
                }

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("序号", seq++);
                item.put("料仓编号", silo.getCangId());
                item.put("物料信息", silo.getMaterialInfo());
                item.put("料仓容量(m³)", silo.getCangCapacity() != null ? silo.getCangCapacity() : 0);
                item.put("料仓余量(%)", silo.getRemain() != null ? silo.getRemain() : 0);
                item.put("料仓状态", silo.getCangStatus() ? "启用" : "未启用");
                item.put("近24h工作量(m³)", work24h);
                item.put("近7天工作量(m³)", work1w);
                dataList.add(item);
            }
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));
        writeJsonResponse(response, dataList, ts + "料仓导出数据文件.json");
    }

    /**
     * 导出料斗数据为JSON文件
     */
    @PostMapping("/production/export-hopper")
    public void exportHopperData(@RequestParam("ids") List<Integer> ids,
                                 HttpSession session, HttpServletResponse response) {
        User user = (User) session.getAttribute("loginUser");
        if (user == null) { response.setStatus(401); return; }
        if (!PermissionUtil.canEditEquipment(session)) { response.setStatus(403); return; }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime24h = now.minusHours(24);
        LocalDateTime startTime1w = now.minusDays(7);

        List<Map<String, Object>> dataList = new ArrayList<>();
        int seq = 1;
        for (Integer id : ids) {
            Hopper hopper = hopperRepository.findById(id).orElse(null);
            if (hopper != null) {
                Float work24h = 0.0f;
                Float work1w = 0.0f;
                Float w24 = tableTaskRepository.sumVolumeByDouAndTimeRange(hopper.getDouId().intValue(), startTime24h, now);
                Float w1w = tableTaskRepository.sumVolumeByDouAndTimeRange(hopper.getDouId().intValue(), startTime1w, now);
                work24h = w24 != null ? w24 : 0.0f;
                work1w = w1w != null ? w1w : 0.0f;

                Map<String, Object> item = new LinkedHashMap<>();
                item.put("序号", seq++);
                item.put("料斗编号", hopper.getDouId());
                item.put("物料信息", hopper.getMaterialInfo());
                item.put("料斗容量(m³)", hopper.getDouCapacity() != null ? hopper.getDouCapacity() : 0);
                item.put("料斗余量(%)", hopper.getRemain() != null ? hopper.getRemain() : 0);
                item.put("料斗状态", hopper.getDouStatus() ? "启用" : "未启用");
                item.put("近24h工作量(m³)", work24h);
                item.put("近7天工作量(m³)", work1w);
                dataList.add(item);
            }
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss"));
        writeJsonResponse(response, dataList, ts + "料斗导出数据文件.json");
    }

    /**
     * 将数据列表写入JSON响应
     */
    private void writeJsonResponse(HttpServletResponse response, List<Map<String, Object>> dataList, String filename) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            String encodedFilename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
            response.setCharacterEncoding("UTF-8");
            String json = objectMapper.writeValueAsString(dataList);
            response.getWriter().write(json);
            response.getWriter().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安全地将字符串解析为 Integer，失败时返回 null
     */
    private Integer safeParseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
