package com.example.xiamenbackground.util;

import com.example.xiamenbackground.entity.User;
import jakarta.servlet.http.HttpSession;

/**
 * 权限工具类
 */
public class PermissionUtil {

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null && "管理员".equals(user.getRole());
    }

    /**
     * 检查当前用户是否为调度主管
     */
    public static boolean isSupervisor(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null && "调度主管".equals(user.getRole());
    }

    /**
     * 检查当前用户是否为调度员
     */
    public static boolean isDispatcher(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null && "调度员".equals(user.getRole());
    }

    /**
     * 检查是否有设备编辑权限（管理员或调度主管）
     */
    public static boolean canEditEquipment(HttpSession session) {
        return isAdmin(session) || isSupervisor(session);
    }

    /**
     * 检查是否有用户管理权限（仅管理员）
     */
    public static boolean canManageUsers(HttpSession session) {
        return isAdmin(session);
    }

    /**
     * 检查是否有作业记录查看和导出权限（管理员或调度主管）
     */
    public static boolean canViewWorkRecords(HttpSession session) {
        return isAdmin(session) || isSupervisor(session);
    }

    /**
     * 检查是否有任务创建和下发权限（管理员、调度主管或调度员）
     */
    public static boolean canCreateTasks(HttpSession session) {
        return isAdmin(session) || isSupervisor(session) || isDispatcher(session);
    }
}
