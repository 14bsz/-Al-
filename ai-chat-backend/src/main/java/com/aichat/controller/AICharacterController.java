package com.aichat.controller;

import com.aichat.entity.AICharacter;
import com.aichat.service.AICharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI角色控制器
 * 提供角色管理的REST API接口
 */
@RestController
@RequestMapping("/api/characters")
@CrossOrigin(origins = "*")
public class AICharacterController {

    @Autowired
    private AICharacterService characterService;

    /**
     * 获取所有激活的角色
     */
    @GetMapping
    public ResponseEntity<List<AICharacter>> getAllActiveCharacters() {
        try {
            List<AICharacter> characters = characterService.getAllActiveCharacters();
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 分页获取角色列表
     */
    @GetMapping("/page")
    public ResponseEntity<Page<AICharacter>> getCharactersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "popularity") String sortBy) {
        try {
            Page<AICharacter> characters = characterService.getCharactersByPage(page, size, sortBy);
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 搜索角色
     */
    @GetMapping("/search")
    public ResponseEntity<List<AICharacter>> searchCharacters(@RequestParam String keyword) {
        try {
            // 使用分页搜索，但只返回第一页的结果
            Page<AICharacter> charactersPage = characterService.searchCharacters(keyword, 0, 20);
            List<AICharacter> characters = charactersPage.getContent();
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据类型获取角色
     */
    @GetMapping("/type/{characterType}")
    public ResponseEntity<List<AICharacter>> getCharactersByType(@PathVariable String characterType) {
        try {
            List<AICharacter> characters = characterService.getCharactersByType(characterType);
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取热门角色
     */
    @GetMapping("/popular")
    public ResponseEntity<List<AICharacter>> getPopularCharacters(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AICharacter> characters = characterService.getPopularCharacters(limit);
            return ResponseEntity.ok(characters);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AICharacter> getCharacterById(@PathVariable Long id) {
        try {
            Optional<AICharacter> character = characterService.getCharacterById(id);
            if (character.isPresent()) {
                return ResponseEntity.ok(character.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据名称获取角色
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<AICharacter> getCharacterByName(@PathVariable String name) {
        try {
            Optional<AICharacter> character = characterService.getCharacterByName(name);
            if (character.isPresent()) {
                return ResponseEntity.ok(character.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建新角色
     */
    @PostMapping
    public ResponseEntity<AICharacter> createCharacter(@RequestBody AICharacter character) {
        try {
            AICharacter createdCharacter = characterService.createCharacter(character);
            return ResponseEntity.ok(createdCharacter);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新角色信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<AICharacter> updateCharacter(
            @PathVariable Long id, 
            @RequestBody AICharacter character) {
        try {
            character.setId(id);
            AICharacter updatedCharacter = characterService.updateCharacter(character);
            return ResponseEntity.ok(updatedCharacter);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        try {
            characterService.deleteCharacter(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 激活角色
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateCharacter(@PathVariable Long id) {
        try {
            characterService.activateCharacter(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 停用角色
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCharacter(@PathVariable Long id) {
        try {
            characterService.deactivateCharacter(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 增加角色人气
     */
    @PostMapping("/{id}/popularity")
    public ResponseEntity<Void> incrementPopularity(@PathVariable Long id) {
        try {
            characterService.incrementPopularity(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取角色统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCharacterStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCharacters", characterService.getTotalCharacterCount());
            statistics.put("activeCharacters", characterService.getActiveCharacterCount());
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取角色可用技能
     */
    @GetMapping("/{id}/skills")
    public ResponseEntity<List<String>> getCharacterSkills(@PathVariable Long id) {
        try {
            Optional<AICharacter> characterOpt = characterService.getCharacterById(id);
            if (characterOpt.isPresent()) {
                AICharacter character = characterOpt.get();
                String skillsStr = character.getAvailableSkills();
                if (skillsStr != null && !skillsStr.isEmpty()) {
                    List<String> skills = Arrays.asList(skillsStr.split(","));
                    return ResponseEntity.ok(skills);
                } else {
                    // 返回默认技能
                    List<String> defaultSkills = Arrays.asList(
                        "情感分析与共情",
                        "专业知识问答", 
                        "创意写作与协作",
                        "个性化学习指导",
                        "角色扮演与情境模拟"
                    );
                    return ResponseEntity.ok(defaultSkills);
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取角色类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getCharacterTypes() {
        try {
            List<String> types = Arrays.asList(
                "文学角色",
                "历史人物", 
                "科学家",
                "文学家",
                "思想家",
                "艺术家",
                "虚拟助手",
                "其他"
            );
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}