/**
 * 角色管理服务
 * 提供角色搜索、获取和管理的API调用功能
 */

const API_BASE_URL = 'http://localhost:8080/api';

class CharacterService {
  /**
   * 获取所有活跃角色
   */
  async getAllCharacters() {
    try {
      const response = await fetch(`${API_BASE_URL}/characters`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('获取角色列表失败:', error);
      throw error;
    }
  }

  /**
   * 根据ID获取角色详情
   */
  async getCharacterById(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/${id}`);
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('角色不存在');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('获取角色详情失败:', error);
      throw error;
    }
  }

  /**
   * 搜索角色
   */
  async searchCharacters(params = {}) {
    try {
      const queryParams = new URLSearchParams();
      
      if (params.name) {
        queryParams.append('name', params.name);
      }
      if (params.keyword) {
        queryParams.append('keyword', params.keyword);
      }

      const url = `${API_BASE_URL}/characters/search${queryParams.toString() ? '?' + queryParams.toString() : ''}`;
      const response = await fetch(url);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('搜索角色失败:', error);
      throw error;
    }
  }

  /**
   * 获取推荐角色
   */
  async getRecommendedCharacters(limit = 6) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/recommended?limit=${limit}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('获取推荐角色失败:', error);
      throw error;
    }
  }

  /**
   * 创建新角色
   */
  async createCharacter(character) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(character)
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('创建角色失败:', error);
      throw error;
    }
  }

  /**
   * 更新角色信息
   */
  async updateCharacter(id, character) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(character)
      });
      
      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('角色不存在');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('更新角色失败:', error);
      throw error;
    }
  }

  /**
   * 删除角色
   */
  async deleteCharacter(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/${id}`, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return true;
    } catch (error) {
      console.error('删除角色失败:', error);
      throw error;
    }
  }

  /**
   * 激活角色
   */
  async activateCharacter(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/${id}/activate`, {
        method: 'PUT'
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return true;
    } catch (error) {
      console.error('激活角色失败:', error);
      throw error;
    }
  }

  /**
   * 初始化默认角色数据
   */
  async initializeDefaultCharacters() {
    try {
      const response = await fetch(`${API_BASE_URL}/characters/initialize`, {
        method: 'POST'
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.text();
    } catch (error) {
      console.error('初始化默认角色失败:', error);
      throw error;
    }
  }
}

// 创建单例实例
const characterService = new CharacterService();

export default characterService;