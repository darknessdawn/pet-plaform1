# 路径别名配置修复说明

## 问题描述
前端页面访问会员中心、优惠券中心、积分商城时出现错误：
```
Failed to resolve import "@/utils/request" from "src/views/MembershipCenter.vue"
```

## 问题原因
Vite 配置文件中没有设置 `@` 路径别名，导致无法正确解析 `@/utils/request` 这样的导入路径。

## 解决方案

### 1. 修改 vite.config.js
添加了路径别名配置：

```javascript
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  // ... 其他配置
})
```

### 2. 安装 @types/node
为了支持 Node.js 的 path 模块，安装了类型定义包：
```bash
npm install --save-dev @types/node
```

## 修复的文件

### vite.config.js
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      }
    }
  }
})
```

## 验证结果

✅ 前端服务正常运行：http://localhost:3001  
✅ 后端服务正常运行：http://localhost:8082/api  
✅ 路径别名 `@` 已正确配置  
✅ 所有新页面可以正常访问：
   - /coupon - 优惠券中心
   - /membership - 会员中心
   - /points - 积分商城

## 使用说明

现在可以在所有 Vue 文件中使用 `@` 别名导入 src 目录下的文件：

```javascript
// 示例
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import ProductCard from '@/components/ProductCard.vue'
```

## 注意事项

1. Vite 服务器会自动检测配置文件变化并重启
2. 如果修改 vite.config.js 后没有自动重启，可以手动停止并重新启动：
   ```bash
   # 停止当前服务（Ctrl+C）
   # 重新启动
   npm run dev
   ```

3. `@` 代表的是 `./src` 目录的绝对路径
