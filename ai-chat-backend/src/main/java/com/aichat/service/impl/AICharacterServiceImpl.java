package com.aichat.service.impl;

import com.aichat.entity.AICharacter;
import com.aichat.repository.AICharacterRepository;
import com.aichat.service.AICharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * AI角色服务实现类
 * 实现角色管理的核心业务逻辑
 */
@Service
@Transactional
public class AICharacterServiceImpl implements AICharacterService {

    @Autowired
    private AICharacterRepository characterRepository;

    @Override
    public List<AICharacter> getLatestCharacters(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return characterRepository.findLatestCharacters(pageable);
    }

    @Override
    public List<AICharacter> getCharactersByKnowledgeDomain(String domain) {
        return characterRepository.findByKnowledgeDomain(domain);
    }

    @Override
    public List<AICharacter> getCharactersBySkill(String skill) {
        return characterRepository.findByAvailableSkill(skill);
    }

    @Override
    public Page<AICharacter> getCharactersByPopularity(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return characterRepository.findActiveCharactersByPopularity(pageable);
    }

    @Override
    public List<AICharacter> getAllActiveCharacters() {
        return characterRepository.findByIsActiveTrue();
    }

    @Override
    public Page<AICharacter> getCharactersByPage(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, sortBy != null ? sortBy : "popularity");
        Pageable pageable = PageRequest.of(page, size, sort);
        return characterRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public Page<AICharacter> searchCharacters(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return characterRepository.searchActiveCharacters(keyword, pageable);
    }

    @Override
    public List<AICharacter> getCharactersByType(String characterType) {
        return characterRepository.findByCharacterTypeAndIsActiveTrue(characterType);
    }

    @Override
    public List<AICharacter> getPopularCharacters(int limit) {
        return characterRepository.findByIsActiveTrueOrderByPopularityScoreDesc(PageRequest.of(0, limit));
    }

    @Override
    public Optional<AICharacter> getCharacterById(Long id) {
        return characterRepository.findById(id);
    }

    @Override
    public Optional<AICharacter> getCharacterByName(String name) {
        return characterRepository.findByName(name);
    }

    @Override
    public AICharacter createCharacter(AICharacter character) {
        // 设置创建时间
        character.setCreatedAt(LocalDateTime.now());
        character.setUpdatedAt(LocalDateTime.now());
        
        // 默认设置为激活状态
        if (character.getIsActive() == null) {
            character.setIsActive(true);
        }
        
        // 初始化人气值
        if (character.getPopularityScore() == null) {
            character.setPopularityScore(0);
        }
        
        return characterRepository.save(character);
    }

    @Override
    public AICharacter updateCharacter(AICharacter character) {
        // 更新修改时间
        character.setUpdatedAt(LocalDateTime.now());
        return characterRepository.save(character);
    }

    @Override
    public void deleteCharacter(Long id) {
        characterRepository.deleteById(id);
    }

    @Override
    public void activateCharacter(Long id) {
        Optional<AICharacter> characterOpt = characterRepository.findById(id);
        if (characterOpt.isPresent()) {
            AICharacter character = characterOpt.get();
            character.setIsActive(true);
            character.setUpdatedAt(LocalDateTime.now());
            characterRepository.save(character);
        }
    }

    @Override
    public void deactivateCharacter(Long id) {
        Optional<AICharacter> characterOpt = characterRepository.findById(id);
        if (characterOpt.isPresent()) {
            AICharacter character = characterOpt.get();
            character.setIsActive(false);
            character.setUpdatedAt(LocalDateTime.now());
            characterRepository.save(character);
        }
    }

    @Override
    public void incrementPopularity(Long characterId) {
        Optional<AICharacter> characterOpt = characterRepository.findById(characterId);
        if (characterOpt.isPresent()) {
            AICharacter character = characterOpt.get();
            character.setPopularityScore(character.getPopularityScore() + 1);
            character.setUpdatedAt(LocalDateTime.now());
            characterRepository.save(character);
        }
    }

    /**
     * 批量创建默认角色
     */
    public void createDefaultCharacters() {
        // 检查是否已有角色数据
        if (characterRepository.count() > 0) {
            return;
        }

        // 创建哈利波特角色
        AICharacter harryPotter = new AICharacter();
        harryPotter.setName("哈利·波特");
        harryPotter.setDescription("勇敢的魔法师，霍格沃茨的学生，拥有神奇的魔法能力");
        harryPotter.setCharacterType("文学角色");
        harryPotter.setPersonalityTraits("勇敢、善良、忠诚、有正义感");
        harryPotter.setKnowledgeDomains("魔法,冒险,友谊,成长");
        harryPotter.setAvailableSkills("魔法知识问答,冒险故事创作,友谊指导");
        harryPotter.setVoiceConfig("{\"voice_id\":\"harry\",\"speed\":1.0,\"pitch\":1.0}");
        harryPotter.setBackgroundStory("你是哈利·波特，一个勇敢的年轻魔法师。你经历过许多冒险，拥有丰富的魔法知识和人生经验。请用温暖、友善的语气与用户交流，分享你的魔法世界经历。");
        createCharacter(harryPotter);

        // 创建苏格拉底角色
        AICharacter socrates = new AICharacter();
        socrates.setName("苏格拉底");
        socrates.setDescription("古希腊哲学家，以苏格拉底式问答法闻名，追求智慧和真理");
        socrates.setCharacterType("历史人物");
        socrates.setPersonalityTraits("智慧、谦逊、好奇、善于思辨");
        socrates.setKnowledgeDomains("哲学,伦理学,逻辑学,人生智慧");
        socrates.setAvailableSkills("哲学思辨,人生指导,逻辑分析");
        socrates.setVoiceConfig("{\"voice_id\":\"socrates\",\"speed\":0.9,\"pitch\":0.9}");
        socrates.setBackgroundStory("你是苏格拉底，古希腊的智者。你善于通过提问来引导他人思考，追求真理和智慧。请用苏格拉底式的问答方法与用户交流，帮助他们深入思考问题。");
        createCharacter(socrates);

        // 创建爱因斯坦角色
        AICharacter einstein = new AICharacter();
        einstein.setName("阿尔伯特·爱因斯坦");
        einstein.setDescription("伟大的物理学家，相对论的提出者，诺贝尔物理学奖获得者");
        einstein.setCharacterType("科学家");
        einstein.setPersonalityTraits("好奇、创新、幽默、深思");
        einstein.setKnowledgeDomains("物理学,数学,科学哲学,创新思维");
        einstein.setAvailableSkills("科学解答,创新思维指导,数学辅导");
        einstein.setVoiceConfig("{\"voice_id\":\"einstein\",\"speed\":1.0,\"pitch\":1.1}");
        einstein.setBackgroundStory("你是爱因斯坦，一位充满好奇心的科学家。你善于用简单的语言解释复杂的科学概念，并鼓励创新思维。请用幽默而深刻的方式与用户交流科学知识。");
        createCharacter(einstein);

        // 创建莎士比亚角色
        AICharacter shakespeare = new AICharacter();
        shakespeare.setName("威廉·莎士比亚");
        shakespeare.setDescription("英国文学巨匠，戏剧家和诗人，创作了众多不朽的文学作品");
        shakespeare.setCharacterType("文学家");
        shakespeare.setPersonalityTraits("富有诗意、深刻、浪漫、智慧");
        shakespeare.setKnowledgeDomains("文学,戏剧,诗歌,人性洞察");
        shakespeare.setAvailableSkills("文学创作,诗歌写作,戏剧指导");
        shakespeare.setVoiceConfig("{\"voice_id\":\"shakespeare\",\"speed\":0.95,\"pitch\":1.0}");
        shakespeare.setBackgroundStory("你是莎士比亚，伟大的文学家。你对人性有深刻的洞察，善于用优美的语言表达复杂的情感。请用富有诗意和智慧的语言与用户交流。");
        createCharacter(shakespeare);

        // 创建孔子角色
        AICharacter confucius = new AICharacter();
        confucius.setName("孔子");
        confucius.setDescription("中国古代伟大的思想家、教育家，儒家学派创始人");
        confucius.setCharacterType("思想家");
        confucius.setPersonalityTraits("仁慈、智慧、谦逊、重视教育");
        confucius.setKnowledgeDomains("儒家思想,教育,伦理道德,人生哲学");
        confucius.setAvailableSkills("人生指导,教育咨询,道德伦理讨论");
        confucius.setVoiceConfig("{\"voice_id\":\"confucius\",\"speed\":0.9,\"pitch\":0.95}");
        confucius.setBackgroundStory("你是孔子，中华文化的伟大导师。你重视教育和道德修养，善于用温和的方式传授人生智慧。请用儒雅、温和的语气与用户交流。");
        createCharacter(confucius);
    }

    /**
     * 获取角色统计信息
     */
    public long getTotalCharacterCount() {
        return characterRepository.count();
    }

    public long getActiveCharacterCount() {
        return characterRepository.countByIsActiveTrue();
    }

    /**
     * 根据人气排序获取角色
     */
    public List<AICharacter> getCharactersByPopularity(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "popularity"));
        return characterRepository.findAll(pageable).getContent();
    }
}