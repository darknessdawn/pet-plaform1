<template>
  <div class="membership-page">
    <h2>会员中心</h2>
    
    <!-- 会员信息卡片 -->
    <el-card class="member-info-card">
      <div class="member-header">
        <div class="member-avatar">
          <el-avatar :size="80" icon="User" />
        </div>
        <div class="member-details">
          <h3>{{ membership.levelName || '普通会员' }}</h3>
          <p v-if="membership.expireDate">
            有效期至：{{ formatDate(membership.expireDate) }}
          </p>
        </div>
      </div>
      
      <el-row :gutter="20" class="member-stats">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">当前积分</div>
            <div class="stat-value">{{ membership.points || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">累计积分</div>
            <div class="stat-value">{{ membership.totalPoints || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">已用积分</div>
            <div class="stat-value">{{ membership.usedPoints || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-label">成长值</div>
            <div class="stat-value">{{ membership.growthValue || 0 }}</div>
          </div>
        </el-col>
      </el-row>
      
      <div class="member-benefits">
        <div class="benefit-item">
          <el-tag type="success">购物折扣 {{ (membership.discountRate * 10).toFixed(1) }}折</el-tag>
        </div>
        <div class="benefit-item">
          <el-tag type="warning">积分倍率 {{ membership.pointsRate }}倍</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 会员等级列表 -->
    <div class="level-section">
      <h3>会员等级</h3>
      <el-table :data="membershipLevels" style="width: 100%">
        <el-table-column prop="levelName" label="等级名称" width="150" />
        <el-table-column prop="minPoints" label="所需成长值" width="150" />
        <el-table-column label="折扣率" width="150">
          <template #default="{ row }">
            {{ (row.discountRate * 10).toFixed(1) }}折
          </template>
        </el-table-column>
        <el-table-column label="积分倍率" width="150">
          <template #default="{ row }">
            {{ row.pointsRate }}倍
          </template>
        </el-table-column>
        <el-table-column prop="freeShipping" label="包邮特权" width="150">
          <template #default="{ row }">
            {{ row.freeShipping ? '✓' : '×' }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button 
              v-if="row.id > membership.levelId" 
              type="primary" 
              size="small"
              @click="upgradeMembership(row.id)"
              :disabled="membership.growthValue < row.minPoints"
            >
              {{ membership.growthValue >= row.minPoints ? '升级' : '成长值不足' }}
            </el-button>
            <el-tag v-else type="success" size="small">当前等级</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const membership = ref({});
const membershipLevels = ref([]);

// 加载会员信息
const loadMembershipInfo = async () => {
  try {
    const res = await request.get('/membership/my');
    membership.value = res.data || {};
  } catch (error) {
    console.error('加载会员信息失败:', error);
  }
};

// 加载会员等级
const loadMembershipLevels = async () => {
  try {
    const res = await request.get('/membership/levels');
    membershipLevels.value = res.data || [];
  } catch (error) {
    console.error('加载会员等级失败:', error);
  }
};

// 升级会员
const upgradeMembership = async (levelId) => {
  try {
    await request.post(`/membership/upgrade?levelId=${levelId}`);
    ElMessage.success('升级成功');
    loadMembershipInfo();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '升级失败');
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

onMounted(() => {
  loadMembershipInfo();
  loadMembershipLevels();
});
</script>

<style scoped>
.membership-page {
  padding: 20px;
}

.member-info-card {
  margin-bottom: 30px;
}

.member-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}

.member-avatar {
  margin-right: 20px;
}

.member-details h3 {
  font-size: 24px;
  margin: 0 0 10px 0;
}

.member-stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  color: #409eff;
  font-weight: bold;
}

.member-benefits {
  display: flex;
  gap: 20px;
}

.benefit-item {
  flex: 1;
}

.level-section {
  margin-top: 30px;
}
</style>
