package com.aichat.repository;

import com.aichat.entity.AICharacter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI角色数据访问层
 */
@Repository
public interface AICharacterRepository extends JpaRepository<AICharacter, Long> {
    
    /**
     * 根据名称查找角色
     */
    Optional<AICharacter> findByName(String name);
    
    /**
     * 查找所有激活的角色
     */
    List<AICharacter> findByIsActiveTrue();
    
    /**
     * 根据角色类型查找
     */
    List<AICharacter> findByCharacterTypeAndIsActiveTrue(String characterType);
    
    /**
     * 根据名称模糊搜索激活的角色
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AICharacter> searchActiveCharacters(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 按受欢迎程度排序查找激活的角色
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true ORDER BY c.popularityScore DESC")
    Page<AICharacter> findActiveCharactersByPopularity(Pageable pageable);
    
    /**
     * 根据知识领域查找角色
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true AND " +
           "c.knowledgeDomains LIKE CONCAT('%', :domain, '%')")
    List<AICharacter> findByKnowledgeDomain(@Param("domain") String domain);
    
    /**
     * 根据可用技能查找角色
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true AND " +
           "c.availableSkills LIKE CONCAT('%', :skill, '%')")
    List<AICharacter> findByAvailableSkill(@Param("skill") String skill);
    
    /**
     * 获取热门角色（按受欢迎程度排序）
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true AND c.popularityScore > 0 " +
           "ORDER BY c.popularityScore DESC")
    List<AICharacter> findPopularCharacters(Pageable pageable);
    
    /**
     * 获取最新添加的角色
     */
    @Query("SELECT c FROM AICharacter c WHERE c.isActive = true ORDER BY c.createdAt DESC")
    List<AICharacter> findLatestCharacters(Pageable pageable);
    
    /**
     * 分页查找激活的角色
     */
    Page<AICharacter> findByIsActiveTrue(Pageable pageable);
    
    /**
     * 根据名称或描述模糊搜索
     */
    List<AICharacter> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    /**
     * 按人气排序查找激活角色
     */
    List<AICharacter> findByIsActiveTrueOrderByPopularityScoreDesc(Pageable pageable);
    
    /**
     * 统计激活的角色数量
     */
    long countByIsActiveTrue();
    
    /**
     * 根据角色类型统计数量
     */
    long countByCharacterTypeAndIsActiveTrue(String characterType);
}