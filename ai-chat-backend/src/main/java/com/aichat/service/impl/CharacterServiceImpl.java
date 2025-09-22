package com.aichat.service.impl;

import com.aichat.entity.ChatCharacter;
import com.aichat.service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理服务实现类
 */
@Service
@Transactional
public class CharacterServiceImpl implements CharacterService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ChatCharacter> getAllActiveCharacters() {
        TypedQuery<ChatCharacter> query = entityManager.createQuery(
            "SELECT c FROM ChatCharacter c WHERE c.isActive = true ORDER BY c.name", 
            ChatCharacter.class
        );
        return query.getResultList();
    }

    @Override
    public Optional<ChatCharacter> getCharacterById(Long id) {
        ChatCharacter character = entityManager.find(ChatCharacter.class, id);
        return Optional.ofNullable(character);
    }

    @Override
    public List<ChatCharacter> searchCharactersByName(String name) {
        TypedQuery<ChatCharacter> query = entityManager.createQuery(
            "SELECT c FROM ChatCharacter c WHERE c.isActive = true AND LOWER(c.name) LIKE LOWER(:name) ORDER BY c.name", 
            ChatCharacter.class
        );
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    @Override
    public List<ChatCharacter> searchCharactersByKeyword(String keyword) {
        TypedQuery<ChatCharacter> query = entityManager.createQuery(
            "SELECT c FROM ChatCharacter c WHERE c.isActive = true AND " +
            "(LOWER(c.name) LIKE LOWER(:keyword) OR LOWER(c.description) LIKE LOWER(:keyword)) " +
            "ORDER BY c.name", 
            ChatCharacter.class
        );
        query.setParameter("keyword", "%" + keyword + "%");
        return query.getResultList();
    }

    @Override
    public ChatCharacter createCharacter(ChatCharacter character) {
        character.setCreatedAt(LocalDateTime.now());
        character.setUpdatedAt(LocalDateTime.now());
        if (character.getIsActive() == null) {
            character.setIsActive(true);
        }
        entityManager.persist(character);
        return character;
    }

    @Override
    public ChatCharacter updateCharacter(Long id, ChatCharacter character) {
        ChatCharacter existingCharacter = entityManager.find(ChatCharacter.class, id);
        if (existingCharacter == null) {
            throw new RuntimeException("角色不存在，ID: " + id);
        }
        
        // 更新字段
        existingCharacter.setName(character.getName());
        existingCharacter.setDescription(character.getDescription());
        existingCharacter.setSystemPrompt(character.getSystemPrompt());
        existingCharacter.setBackgroundPrompt(character.getBackgroundPrompt());
        existingCharacter.setAvatarUrl(character.getAvatarUrl());
        existingCharacter.setPersonality(character.getPersonality());
        existingCharacter.setSkills(character.getSkills());
        existingCharacter.setPersonalityConfig(character.getPersonalityConfig());
        existingCharacter.setUpdatedAt(LocalDateTime.now());
        
        return entityManager.merge(existingCharacter);
    }

    @Override
    public void deleteCharacter(Long id) {
        ChatCharacter character = entityManager.find(ChatCharacter.class, id);
        if (character != null) {
            character.setIsActive(false);
            character.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(character);
        }
    }

    @Override
    public void activateCharacter(Long id) {
        ChatCharacter character = entityManager.find(ChatCharacter.class, id);
        if (character != null) {
            character.setIsActive(true);
            character.setUpdatedAt(LocalDateTime.now());
            entityManager.merge(character);
        }
    }

    @Override
    public List<ChatCharacter> getRecommendedCharacters(int limit) {
        TypedQuery<ChatCharacter> query = entityManager.createQuery(
            "SELECT c FROM ChatCharacter c WHERE c.isActive = true ORDER BY c.createdAt DESC", 
            ChatCharacter.class
        );
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public void initializeDefaultCharacters() {
        // 检查是否已有角色数据
        TypedQuery<Long> countQuery = entityManager.createQuery(
            "SELECT COUNT(c) FROM ChatCharacter c", Long.class
        );
        Long count = countQuery.getSingleResult();
        
        if (count > 0) {
            return; // 已有数据，不需要初始化
        }

        // 创建默认角色
        createDefaultCharacters();
    }

    private void createDefaultCharacters() {
        // 1. 哈利波特
        ChatCharacter harry = new ChatCharacter(
            "哈利波特",
            "霍格沃茨魔法学校的学生，拥有闪电疤痕的男孩，勇敢善良，是魔法世界的救世主",
            "你是哈利波特，一个勇敢、善良、有时冲动的巫师。你在霍格沃茨学习魔法，有着与伏地魔对抗的经历。你重视友谊，特别是与赫敏和罗恩的友谊。你会用第一人称回答问题，语气友好而真诚。",
            "你出生在魔法世界，但在麻瓜家庭长大。11岁时收到霍格沃茨的入学通知书，开始了你的魔法学习之旅。你擅长魁地奇，是格兰芬多学院的找球手。",
            "https://picsum.photos/200/200?random=1",
            "勇敢、善良、忠诚、有时冲动",
            "魔法知识,魁地奇运动,黑魔法防御术"
        );
        createCharacter(harry);

        // 2. 苏格拉底
        ChatCharacter socrates = new ChatCharacter(
            "苏格拉底",
            "古希腊哲学家，被誉为西方哲学的奠基人之一，以苏格拉底式问答法闻名",
            "你是苏格拉底，古希腊的哲学家。你相信'我知道我什么都不知道'，喜欢通过提问来引导他人思考。你会用苏格拉底式的问答方法来回应，经常反问对方，帮助他们自己发现真理。你的语言风格睿智而谦逊。",
            "你生活在公元前5世纪的雅典，是一位伟大的哲学家和教育家。你不收取学费，在街头与人们讨论哲学问题，追求智慧和真理。",
            "https://picsum.photos/200/200?random=2",
            "睿智、谦逊、好奇、善于思辨",
            "哲学思辨,逻辑推理,道德伦理"
        );
        createCharacter(socrates);

        // 3. 爱因斯坦
        ChatCharacter einstein = new ChatCharacter(
            "爱因斯坦",
            "20世纪最伟大的物理学家之一，相对论的提出者，诺贝尔物理学奖获得者",
            "你是阿尔伯特·爱因斯坦，一位充满好奇心和想象力的物理学家。你提出了相对论，改变了人类对时空的认识。你喜欢用简单的语言解释复杂的物理概念，经常用比喻和思想实验来说明问题。你相信'想象力比知识更重要'。",
            "你出生于德国，后来移居美国。你的科学成就包括狭义相对论、广义相对论、光电效应等。你也关心社会问题，是一位和平主义者。",
            "https://picsum.photos/200/200?random=3",
            "好奇、富有想象力、幽默、深思熟虑",
            "物理学知识,数学推理,科学思维"
        );
        createCharacter(einstein);

        // 4. 莎士比亚
        ChatCharacter shakespeare = new ChatCharacter(
            "莎士比亚",
            "英国文艺复兴时期最伟大的戏剧家和诗人，被誉为英国文学史上最杰出的作家",
            "你是威廉·莎士比亚，伟大的戏剧家和诗人。你创作了《哈姆雷特》、《罗密欧与朱丽叶》等不朽作品。你的语言富有诗意和戏剧性，善于洞察人性，经常引用你的作品中的名句。你会用优雅而富有文学色彩的语言与人交流。",
            "你生活在16-17世纪的英国，是伊丽莎白时代的代表人物。你既是演员又是剧作家，创作了37部戏剧和154首十四行诗。",
            "https://picsum.photos/200/200?random=4",
            "富有诗意、深刻、机智、充满激情",
            "文学创作,诗歌写作,戏剧表演"
        );
        createCharacter(shakespeare);

        // 5. 孔子
        ChatCharacter confucius = new ChatCharacter(
            "孔子",
            "中国古代伟大的思想家、教育家，儒家学派的创始人，被尊为'万世师表'",
            "你是孔子，中国古代的圣人和教育家。你提倡'仁爱'、'礼制'、'中庸'等思想。你重视教育，认为'有教无类'。你会用温和而睿智的语言回答问题，经常引用《论语》中的话语，注重道德修养和人际关系。",
            "你生活在春秋时期，周游列国传播你的思想。你创办私学，培养了众多弟子。你的思想对中国文化产生了深远影响。",
            "https://picsum.photos/200/200?random=5",
            "仁爱、睿智、谦逊、重视教育",
            "儒家思想,道德教育,人生哲理"
        );
        createCharacter(confucius);

        // 6. 居里夫人
        ChatCharacter curie = new ChatCharacter(
            "居里夫人",
            "波兰裔法国物理学家和化学家，首位获得诺贝尔奖的女性，也是首位获得两次诺贝尔奖的人",
            "你是玛丽·居里，一位坚韧不拔的科学家。你发现了镭和钋两种元素，在放射性研究方面做出了杰出贡献。你会用严谨而热情的语言谈论科学，鼓励女性追求科学事业。你相信科学无国界，知识属于全人类。",
            "你出生在波兰，后来在法国进行科学研究。你与丈夫皮埃尔·居里共同研究放射性，获得了诺贝尔物理学奖和化学奖。",
            "https://picsum.photos/200/200?random=6",
            "坚韧、专注、无私、开拓进取",
            "科学研究,化学知识,女性励志"
        );
        createCharacter(curie);
    }
}