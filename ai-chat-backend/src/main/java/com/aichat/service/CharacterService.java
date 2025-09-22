package com.aichat.service;

import com.aichat.entity.ChatCharacter;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理服务接口
 * 提供角色的搜索、管理和CRUD操作
 */
public interface CharacterService {
    
    /**
     * 获取所有活跃的角色
     * @return 活跃角色列表
     */
    List<ChatCharacter> getAllActiveCharacters();
    
    /**
     * 根据ID获取角色
     * @param id 角色ID
     * @return 角色信息
     */
    Optional<ChatCharacter> getCharacterById(Long id);
    
    /**
     * 根据名称搜索角色
     * @param name 角色名称（支持模糊搜索）
     * @return 匹配的角色列表
     */
    List<ChatCharacter> searchCharactersByName(String name);
    
    /**
     * 根据关键词搜索角色（名称和描述）
     * @param keyword 搜索关键词
     * @return 匹配的角色列表
     */
    List<ChatCharacter> searchCharactersByKeyword(String keyword);
    
    /**
     * 创建新角色
     * @param character 角色信息
     * @return 创建的角色
     */
    ChatCharacter createCharacter(ChatCharacter character);
    
    /**
     * 更新角色信息
     * @param id 角色ID
     * @param character 更新的角色信息
     * @return 更新后的角色
     */
    ChatCharacter updateCharacter(Long id, ChatCharacter character);
    
    /**
     * 删除角色（软删除）
     * @param id 角色ID
     */
    void deleteCharacter(Long id);
    
    /**
     * 激活角色
     * @param id 角色ID
     */
    void activateCharacter(Long id);
    
    /**
     * 获取推荐角色列表
     * @param limit 返回数量限制
     * @return 推荐角色列表
     */
    List<ChatCharacter> getRecommendedCharacters(int limit);
    
    /**
     * 初始化默认角色数据
     */
    void initializeDefaultCharacters();
}