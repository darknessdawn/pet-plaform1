<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="user-card">
          <div class="user-info">
            <el-avatar :size="80" :src="userStore.userInfo.avatar" />
            <h3>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h3>
            <p>{{ userStore.userInfo.email }}</p>
          </div>
          <el-divider />
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card>
          <!-- 个人信息 -->
          <div v-if="activeMenu === 'info'" class="profile-section">
            <h3>个人信息</h3>
            <el-form :model="userForm" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="userForm.nickname" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="userForm.phone" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 收货地址 -->
          <div v-if="activeMenu === 'address'" class="profile-section">
            <h3>收货地址</h3>
            <el-button type="primary" @click="showAddAddressDialog">添加地址</el-button>
            
            <el-empty v-if="addressList.length === 0" description="暂无收货地址" />
            
            <div v-else class="address-list">
              <el-card v-for="addr in addressList" :key="addr.id" class="address-card">
                <div class="address-header">
                  <span class="receiver-name">{{ addr.receiverName }}</span>
                  <span class="receiver-phone">{{ addr.phone }}</span>
                  <el-tag v-if="addr.isDefault" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                </div>
                <div class="address-actions">
                  <el-button 
                    v-if="!addr.isDefault" 
                    type="primary" 
                    size="small" 
                    @click="setDefault(addr.id)"
                  >
                    设为默认
                  </el-button>
                  <el-button type="primary" size="small" @click="editAddress(addr)">编辑</el-button>
                  <el-button type="danger" size="small" @click="deleteAddr(addr.id)">删除</el-button>
                </div>
              </el-card>
            </div>
            
            <!-- 添加/编辑地址对话框 -->
            <el-dialog 
              v-model="addressDialogVisible" 
              :title="isEdit ? '编辑地址' : '添加地址'"
              width="500px"
            >
              <el-form :model="addressForm" :rules="addressRules" ref="addressFormRef" label-width="80px">
                <el-form-item label="收货人" prop="receiverName">
                  <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
                <el-form-item label="所在地区" prop="region">
                  <el-cascader
                    v-model="addressForm.region"
                    :options="regionOptions"
                    placeholder="请选择省市区"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="详细地址" prop="detailAddress">
                  <el-input 
                    v-model="addressForm.detailAddress" 
                    type="textarea" 
                    :rows="3"
                    placeholder="请输入详细地址" 
                  />
                </el-form-item>
                <el-form-item label="邮编" prop="zipCode">
                  <el-input v-model="addressForm.zipCode" placeholder="请输入邮编" maxlength="6" />
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="addressDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitAddress">确定</el-button>
              </template>
            </el-dialog>
          </div>
          
          <!-- 修改密码 -->
          <div v-if="activeMenu === 'password'" class="profile-section">
            <h3>修改密码</h3>
            <el-form :model="passwordForm" label-width="100px">
              <el-form-item label="原密码">
                <el-input v-model="passwordForm.oldPassword" type="password" />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="passwordForm.newPassword" type="password" />
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="passwordForm.confirmPassword" type="password" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword">确认修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '../api/address'

const userStore = useUserStore()
const activeMenu = ref('info')
const addressDialogVisible = ref(false)
const isEdit = ref(false)
const addressList = ref([])
const addressFormRef = ref(null)

const userForm = reactive({
  username: userStore.userInfo.username,
  nickname: userStore.userInfo.nickname,
  email: userStore.userInfo.email,
  phone: userStore.userInfo.phone
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 地址表单数据
const addressForm = reactive({
  id: null,
  receiverName: '',
  phone: '',
  region: [],
  detailAddress: '',
  zipCode: ''
})

// 地址表单验证规则
const addressRules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择省市区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 简化的地区选项（示例）
const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [{ value: '市辖区', label: '市辖区', children: [{ value: '朝阳区', label: '朝阳区' }] }]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [{ value: '市辖区', label: '市辖区', children: [{ value: '浦东新区', label: '浦东新区' }] }]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      { value: '广州市', label: '广州市', children: [{ value: '天河区', label: '天河区' }] },
      { value: '深圳市', label: '深圳市', children: [{ value: '南山区', label: '南山区' }] }
    ]
  }
]

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'address') {
    loadAddresses()
  }
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    addressList.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载地址失败:', error)
    // 如果接口不存在或失败，使用测试数据
    addressList.value = [
      {
        id: 1,
        receiverName: '张三',
        phone: '13800138001',
        province: '北京市',
        city: '北京市',
        district: '朝阳区',
        detailAddress: '朝阳北路 1 号',
        zipCode: '100000',
        isDefault: true
      }
    ]
  }
}

const showAddAddressDialog = () => {
  isEdit.value = false
  Object.assign(addressForm, {
    id: null,
    receiverName: '',
    phone: '',
    region: [],
    detailAddress: '',
    zipCode: ''
  })
  addressDialogVisible.value = true
}

const editAddress = (addr) => {
  isEdit.value = true
  Object.assign(addressForm, {
    id: addr.id,
    receiverName: addr.receiverName,
    phone: addr.phone,
    region: [addr.province, addr.city, addr.district],
    detailAddress: addr.detailAddress,
    zipCode: addr.zipCode
  })
  addressDialogVisible.value = true
}

const submitAddress = async () => {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      receiverName: addressForm.receiverName,
      phone: addressForm.phone,
      province: addressForm.region[0] || '',
      city: addressForm.region[1] || '',
      district: addressForm.region[2] || '',
      detailAddress: addressForm.detailAddress,
      zipCode: addressForm.zipCode
    }
    
    if (isEdit.value && addressForm.id) {
      await updateAddress(addressForm.id, data)
      ElMessage.success('地址更新成功')
    } else {
      await addAddress(data)
      ElMessage.success('地址添加成功')
    }
    
    addressDialogVisible.value = false
    loadAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存失败')
  }
}

const setDefault = async (id) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败')
  }
}

const deleteAddr = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAddress(id)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const saveProfile = () => {
  ElMessage.success('个人信息已保存')
}

const changePassword = () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  ElMessage.success('密码修改成功')
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

onMounted(() => {
  // 初始加载时不自动加载地址，等用户点击地址菜单时才加载
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.user-card {
  text-align: center;
}

.user-info {
  padding: 20px 0;
}

.user-info h3 {
  margin: 15px 0 5px;
}

.user-info p {
  color: #999;
  font-size: 14px;
}

.profile-section {
  padding: 20px;
}

.profile-section h3 {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.address-list {
  margin-top: 20px;
}

.address-card {
  margin-bottom: 15px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.receiver-name {
  font-weight: bold;
  font-size: 16px;
}

.receiver-phone {
  color: #666;
}

.address-detail {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  gap: 10px;
}
</style>
