<template>
    <div id="utils-home">
        <el-row :gutter="20">
            <el-col :span="4" v-for="(app, idx) in utilsApps" :key="idx">
                <util-card :info="app" />
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import UtilCard from "./component/UtilCard"

const APPS = [
    {
        name: "UI自动化测试平台",
        img: "uitest",
        url: "http://test.uiauto.cicc.com.cn/",
    },
    {
        name: "精准测试平台",
        img: "precision",
        url: "/testutils/precision",
        inner: true,
    },
]

const utilsApps = APPS.filter((app) => !(app.hidden ?? false))
</script>

<style scoped>
#utils-home {
    padding: 10px;
}
</style>
