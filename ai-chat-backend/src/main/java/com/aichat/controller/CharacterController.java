package com.aichat.controller;

import com.aichat.entity.ChatCharacter;
import com.aichat.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理控制器
 * 提供角色的搜索、管理和CRUD操作的REST API
 */
@RestController
@RequestMapping("/api/v1/characters")
@CrossOrigin(origins = "*")
public class CharacterController {

    @Autowired
    private CharacterService characterService;

    /**
     * 获取所有活跃角色
     */
    @GetMapping
    public ResponseEntity<List<ChatCharacter>> getAllCharacters() {
        List<ChatCharacter> characters = characterService.getAllActiveCharacters();
        return ResponseEntity.ok(characters);
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatCharacter> getCharacterById(@PathVariable Long id) {
        Optional<ChatCharacter> character = characterService.getCharacterById(id);
        return character.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索角色
     */
    @GetMapping("/search")
    public ResponseEntity<List<ChatCharacter>> searchCharacters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String keyword) {
        
        List<ChatCharacter> characters;
        
        if (name != null && !name.trim().isEmpty()) {
            characters = characterService.searchCharactersByName(name.trim());
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            characters = characterService.searchCharactersByKeyword(keyword.trim());
        } else {
            characters = characterService.getAllActiveCharacters();
        }
        
        return ResponseEntity.ok(characters);
    }

    /**
     * 获取推荐角色
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<ChatCharacter>> getRecommendedCharacters(
            @RequestParam(defaultValue = "6") int limit) {
        List<ChatCharacter> characters = characterService.getRecommendedCharacters(limit);
        return ResponseEntity.ok(characters);
    }

    /**
     * 创建新角色
     */
    @PostMapping
    public ResponseEntity<ChatCharacter> createCharacter(@Valid @RequestBody ChatCharacter character) {
        try {
            ChatCharacter createdCharacter = characterService.createCharacter(character);
            return ResponseEntity.ok(createdCharacter);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新角色信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ChatCharacter> updateCharacter(
            @PathVariable Long id, 
            @Valid @RequestBody ChatCharacter character) {
        try {
            ChatCharacter updatedCharacter = characterService.updateCharacter(id, character);
            return ResponseEntity.ok(updatedCharacter);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除角色（软删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        try {
            characterService.deleteCharacter(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 激活角色
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateCharacter(@PathVariable Long id) {
        try {
            characterService.activateCharacter(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 初始化默认角色数据
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeDefaultCharacters() {
        try {
            characterService.initializeDefaultCharacters();
            return ResponseEntity.ok("默认角色初始化成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("初始化失败: " + e.getMessage());
        }
    }
}