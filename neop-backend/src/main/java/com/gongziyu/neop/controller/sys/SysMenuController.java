package com.gongziyu.neop.controller.sys;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.SysMenu;
import com.gongziyu.neop.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<SysMenu> list = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        return Result.success(list.stream().map(this::toFrontend).collect(Collectors.toList()));
    }

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        List<SysMenu> all = sysMenuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSort));
        List<Map<String, Object>> treeList = all.stream().map(this::toFrontend).collect(Collectors.toList());
        return Result.success(buildTree(treeList));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        SysMenu menu = toEntity(body);
        menu.setId(null);
        sysMenuMapper.insert(menu);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        SysMenu menu = toEntity(body);
        sysMenuMapper.updateById(menu);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuMapper.deleteById(id);
        return Result.success();
    }

    private Map<String, Object> toFrontend(SysMenu entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("parentId", entity.getParentId());
        map.put("title", entity.getMenuName());
        map.put("type", entity.getMenuType());
        map.put("icon", entity.getMenuIcon());
        map.put("path", entity.getMenuPath());
        map.put("component", entity.getComponent());
        map.put("permission", entity.getPermission());
        map.put("sort", entity.getSort());
        map.put("visible", entity.getStatus() != null && entity.getStatus() == 1);
        return map;
    }

    private SysMenu toEntity(Map<String, Object> map) {
        SysMenu entity = new SysMenu();
        if (map.get("id") != null) {
            entity.setId(((Number) map.get("id")).longValue());
        }
        entity.setParentId(map.get("parentId") != null ? ((Number) map.get("parentId")).longValue() : 0);
        entity.setMenuName((String) map.getOrDefault("title", ""));
        Object type = map.get("type");
        entity.setMenuType(type != null ? String.valueOf(type) : "1");
        entity.setMenuIcon((String) map.getOrDefault("icon", ""));
        entity.setMenuPath((String) map.getOrDefault("path", ""));
        entity.setComponent((String) map.getOrDefault("component", ""));
        entity.setPermission((String) map.getOrDefault("permission", ""));
        entity.setSort(map.get("sort") != null ? ((Number) map.get("sort")).intValue() : 0);
        Object visible = map.get("visible");
        entity.setStatus(visible != null && (Boolean) visible ? 1 : 0);
        return entity;
    }

    private List<Map<String, Object>> buildTree(List<Map<String, Object>> flatList) {
        List<Map<String, Object>> roots = new ArrayList<>();
        Map<Object, List<Map<String, Object>>> childrenMap = flatList.stream()
                .filter(m -> m.get("parentId") != null)
                .collect(Collectors.groupingBy(m -> m.get("parentId")));
        for (Map<String, Object> node : flatList) {
            Object id = node.get("id");
            List<Map<String, Object>> children = childrenMap.getOrDefault(id, Collections.emptyList());
            if (!children.isEmpty()) {
                node.put("children", children);
            }
            if (node.get("parentId") == null || ((Number) node.get("parentId")).longValue() == 0) {
                roots.add(node);
            }
        }
        return roots;
    }
}
