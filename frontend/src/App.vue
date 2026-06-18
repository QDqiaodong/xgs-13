<template>
  <div id="app">
    <el-container style="min-height: 100vh">
      <el-aside width="220px" style="background: #304156">
        <div style="height: 60px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #3a4a5b">
          <span style="color: #fff; font-size: 16px; font-weight: bold">设备维保系统</span>
        </div>
        <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
          <el-menu-item index="/dashboard">
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/equipments">
            <span>设备档案</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <span>维保工单</span>
          </el-menu-item>
          <el-menu-item index="/plans">
            <span>维保计划</span>
          </el-menu-item>
          <el-menu-item index="/spare-parts">
            <span>备件库存</span>
          </el-menu-item>
          <el-menu-item index="/customers">
            <span>客户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px">
          <span style="font-size: 18px; font-weight: bold; color: #333">第三方工业设备维保工单资产台账系统</span>
          <span style="color: #999; font-size: 13px">自动化设备 · 空压机 · 制冷设备</span>
        </el-header>
        <el-main style="background: #f0f2f5; padding: 20px">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; }
.el-menu { border-right: none; }
.el-menu-item { height: 50px; line-height: 50px; }
.el-aside { overflow-y: auto; }
</style>
