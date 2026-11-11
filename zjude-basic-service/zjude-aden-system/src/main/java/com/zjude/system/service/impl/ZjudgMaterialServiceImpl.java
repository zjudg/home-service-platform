package com.zjude.system.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zjude.common.exception.ServiceException;
import com.zjude.common.utils.StringUtils;
import com.zjude.common.utils.uuid.base.BaseKeyGenerate;
import com.zjude.system.domain.ZjudgMaterial;
import com.zjude.system.domain.ZjudgMaterialGroup;
import com.zjude.system.mapper.ZjudgMaterialGroupMapper;
import com.zjude.system.mapper.ZjudgMaterialMapper;
import com.zjude.system.service.IZjudgMaterialService;

/**
 * 素材库应用服务实现。
 */
@Service
public class ZjudgMaterialServiceImpl implements IZjudgMaterialService
{
    private static final String MATERIAL_KEY_PREFIX = "ZM_";

    private static final String GROUP_KEY_PREFIX = "MG_";

    private static final int MAX_GROUP_NAME = 64;

    @Resource
    private ZjudgMaterialMapper materialMapper;

    @Resource
    private ZjudgMaterialGroupMapper groupMapper;

    @Resource
    private BaseKeyGenerate keyGenerate;

    @Override
    public List<ZjudgMaterial> list(ZjudgMaterial query)
    {
        return materialMapper.selectList(query == null ? new ZjudgMaterial() : query);
    }

    @Override
    public ZjudgMaterial detail(String materialId)
    {
        if (StringUtils.isEmpty(materialId))
        {
            return null;
        }
        return materialMapper.selectById(materialId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(ZjudgMaterial material, String username)
    {
        validate(material);
        requireGroup(material.getGroupId());

        ZjudgMaterial item = copy(material);
        item.setMaterialId(keyGenerate.snowflakeStringKey(MATERIAL_KEY_PREFIX));
        item.setCreateBy(username);
        materialMapper.insert(item);
        return item.getMaterialId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ZjudgMaterial material, String username)
    {
        if (material == null || StringUtils.isEmpty(material.getMaterialId()))
        {
            throw new ServiceException("素材主键不能为空");
        }
        if (materialMapper.selectById(material.getMaterialId()) == null)
        {
            throw new ServiceException("素材不存在");
        }
        validate(material);
        requireGroup(material.getGroupId());

        ZjudgMaterial item = copy(material);
        item.setUpdateBy(username);
        return materialMapper.update(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int remove(List<String> materialIds)
    {
        List<String> ids = cleanIds(materialIds);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择要删除的素材");
        }
        return materialMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int move(List<String> materialIds, String groupId)
    {
        List<String> ids = cleanIds(materialIds);
        if (ids.isEmpty())
        {
            throw new ServiceException("请选择要移动的素材");
        }
        requireGroup(groupId);
        return materialMapper.moveToGroup(ids, groupId);
    }

    @Override
    public List<Map<String, Object>> groups()
    {
        List<ZjudgMaterialGroup> groups = groupMapper.selectAll();
        // 「全部」不落库，由服务层合成在首位
        List<Map<String, Object>> result = new ArrayList<>();
        int total = 0;
        for (ZjudgMaterialGroup group : groups)
        {
            int count = materialMapper.countByGroup(group.getGroupId());
            total += count;
            result.add(groupItem(group, count));
        }

        Map<String, Object> all = new LinkedHashMap<>();
        all.put("groupId", "all");
        all.put("groupName", "全部");
        all.put("sortNum", -1);
        all.put("count", total);
        result.add(0, all);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addGroup(String groupName)
    {
        String name = requireGroupName(groupName);
        if (groupMapper.selectByName(name) != null)
        {
            throw new ServiceException("分组名称已存在");
        }

        ZjudgMaterialGroup group = new ZjudgMaterialGroup();
        group.setGroupId(keyGenerate.snowflakeStringKey(GROUP_KEY_PREFIX));
        group.setGroupName(name);
        group.setSortNum(0);
        groupMapper.insert(group);
        return group.getGroupId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int renameGroup(String groupId, String groupName)
    {
        if (StringUtils.isEmpty(groupId))
        {
            throw new ServiceException("分组主键不能为空");
        }
        if (ZjudgMaterialGroup.UNGROUPED_ID.equals(groupId))
        {
            throw new ServiceException("默认分组不支持重命名");
        }
        ZjudgMaterialGroup existing = groupMapper.selectById(groupId);
        if (existing == null)
        {
            throw new ServiceException("分组不存在");
        }

        String name = requireGroupName(groupName);
        ZjudgMaterialGroup sameName = groupMapper.selectByName(name);
        if (sameName != null && !sameName.getGroupId().equals(groupId))
        {
            throw new ServiceException("分组名称已存在");
        }

        ZjudgMaterialGroup group = new ZjudgMaterialGroup();
        group.setGroupId(groupId);
        group.setGroupName(name);
        group.setSortNum(existing.getSortNum());
        return groupMapper.update(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeGroup(String groupId)
    {
        if (StringUtils.isEmpty(groupId))
        {
            throw new ServiceException("分组主键不能为空");
        }
        if (ZjudgMaterialGroup.UNGROUPED_ID.equals(groupId))
        {
            throw new ServiceException("默认分组不支持删除");
        }
        if (groupMapper.selectById(groupId) == null)
        {
            throw new ServiceException("分组不存在");
        }

        // 先把素材迁到「未分组」，否则删除分组后素材会失去归属
        List<ZjudgMaterial> materials = materialMapper.selectList(groupQuery(groupId));
        int moved = 0;
        if (!materials.isEmpty())
        {
            List<String> ids = materials.stream().map(ZjudgMaterial::getMaterialId).toList();
            moved = materialMapper.moveToGroup(ids, ZjudgMaterialGroup.UNGROUPED_ID);
        }
        groupMapper.deleteById(groupId);
        return moved;
    }

    private ZjudgMaterial groupQuery(String groupId)
    {
        ZjudgMaterial query = new ZjudgMaterial();
        query.setGroupId(groupId);
        return query;
    }

    private Map<String, Object> groupItem(ZjudgMaterialGroup group, int count)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("groupId", group.getGroupId());
        item.put("groupName", group.getGroupName());
        item.put("sortNum", group.getSortNum());
        item.put("count", count);
        return item;
    }

    private void requireGroup(String groupId)
    {
        if (StringUtils.isEmpty(groupId) || groupMapper.selectById(groupId) == null)
        {
            throw new ServiceException("素材分组不存在");
        }
    }

    private String requireGroupName(String groupName)
    {
        if (StringUtils.isEmpty(groupName) || groupName.trim().isEmpty())
        {
            throw new ServiceException("分组名称不能为空");
        }
        String name = groupName.trim();
        if (name.length() > MAX_GROUP_NAME)
        {
            throw new ServiceException("分组名称长度不能超过 " + MAX_GROUP_NAME + " 个字符");
        }
        return name;
    }

    private List<String> cleanIds(List<String> ids)
    {
        if (ids == null)
        {
            return List.of();
        }
        return ids.stream().filter(StringUtils::isNotEmpty).distinct().toList();
    }

    private void validate(ZjudgMaterial material)
    {
        if (material == null)
        {
            throw new ServiceException("素材信息不能为空");
        }
        if (StringUtils.isEmpty(material.getMaterialName()))
        {
            throw new ServiceException("素材名称不能为空");
        }
        if (StringUtils.isEmpty(material.getMaterialUrl()))
        {
            throw new ServiceException("请上传素材文件");
        }
        String type = material.getMaterialType();
        if (!ZjudgMaterial.TYPE_IMAGE.equals(type) && !ZjudgMaterial.TYPE_VIDEO.equals(type))
        {
            throw new ServiceException("素材类型只能是图片或视频");
        }
    }

    private ZjudgMaterial copy(ZjudgMaterial source)
    {
        ZjudgMaterial target = new ZjudgMaterial();
        target.setMaterialId(source.getMaterialId());
        target.setGroupId(source.getGroupId());
        target.setMaterialName(source.getMaterialName().trim());
        target.setMaterialType(source.getMaterialType());
        target.setMaterialUrl(source.getMaterialUrl());
        target.setFileSize(source.getFileSize());
        return target;
    }
}
