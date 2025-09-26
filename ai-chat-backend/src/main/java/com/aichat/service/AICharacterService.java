package com.aichat.service;

import com.aichat.entity.AICharacter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * AI角色服务接口
 * 提供角色管理的业务逻辑
 */
public interface AICharacterService {
    
    /**
     * 获取所有激活的角色
     */
    List<AICharacter> getAllActiveCharacters();
    
    /**
     * 分页获取角色
     */
    Page<AICharacter> getCharactersByPage(int page, int size, String sortBy);
    
    /**
     * 根据ID获取角色
     */
    Optional<AICharacter> getCharacterById(Long id);
    
    /**
     * 根据名称获取角色
     */
    Optional<AICharacter> getCharacterByName(String name);
    
    /**
     * 搜索角色
     */
    Page<AICharacter> searchCharacters(String keyword, int page, int size);
    
    /**
     * 按受欢迎程度获取角色
     */
    Page<AICharacter> getCharactersByPopularity(int page, int size);
    
    /**
     * 获取热门角色
     */
    List<AICharacter> getPopularCharacters(int limit);
    
    /**
     * 获取最新角色
     */
    List<AICharacter> getLatestCharacters(int limit);
    
    /**
     * 根据角色类型获取角色
     */
    List<AICharacter> getCharactersByType(String characterType);
    
    /**
     * 根据知识领域获取角色
     */
    List<AICharacter> getCharactersByKnowledgeDomain(String domain);
    
    /**
     * 根据技能获取角色
     */
    List<AICharacter> getCharactersBySkill(String skill);
    
    /**
     * 创建角色
     */
    AICharacter createCharacter(AICharacter character);
    
    /**
     * 更新角色
     */
    AICharacter updateCharacter(AICharacter character);
    
    /**
     * 删除角色
     */
    void deleteCharacter(Long id);
    
    /**
     * 激活角色
     */
    void activateCharacter(Long id);
    
    /**
     * 停用角色
     */
    void deactivateCharacter(Long id);
    
    /**
     * 增加人气值
     */
    void incrementPopularity(Long id);
    
    /**
     * 创建默认角色
     */
    void createDefaultCharacters();
    
    /**
     * 获取角色总数
     */
    long getTotalCharacterCount();
    
    /**
     * 获取激活角色数量
     */
    long getActiveCharacterCount();
    
    /**
     * 按人气获取角色
     */
    List<AICharacter> getCharactersByPopularity(int limit);
}