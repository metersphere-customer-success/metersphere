<template>
    <div id="menu-bar">
        <el-row type="flex">
            <project-select v-if="mtype === MTYPES.PRECISION" />
            <project-change v-else :project-name="currentMsProject" />
            <el-col :span="14">
                <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                    :default-active="activeIndex">
                    <template v-for="(item, idx) in menus">
                        <el-menu-item v-if="item.path" :index="item.path" :key="`${idx}-a`">
                            {{ item.label }}
                        </el-menu-item>
                        <el-submenu v-else-if="item.children" :index="`${idx}`" :key="`${idx}-b`">
                            <template slot="title">{{ item.label }}</template>
                            <el-menu-item v-for="(sub, sidx) in item.children" :index="sub.path"
                                :key="`${idx}-s-${sidx}`">
                                {{ sub.label }}
                            </el-menu-item>
                        </el-submenu>
                    </template>
                </el-menu>
            </el-col>
            <el-col :span="10">
                <ms-header-right-menus />
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
const route = useRoute()
const { menus, activeIndex, mtype, currentMsProject } = setup(route)
</script>

<script>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router/composables'

import { Utils } from '@/common'

import ProjectChange from 'metersphere-frontend/src/components/head/ProjectSwitch'
import ProjectSelect from '../component/ProjectSelect'
import MsHeaderRightMenus from 'metersphere-frontend/src/components/layout/HeaderRightMenus'

const TESTUTILS = 'testutils'

const MTYPES = {
    PRECISION: 'precision',
    DEFAULT: 'default',
}

const MENU_ITEMS = [
    {
        label: '首页',
        path: '/testutils/home',
    },
    {
      label: '数据规则库',
      path: '/testutils/dataRules',
    },
    // {
    //     label: '数据规则库',
    //     prefix: '/testutils/dataRules',
    //     children: [
    //         {
    //             label: '报告列表(全部)',
    //             path: '/testutils/precision/reports/all',
    //         },
    //         {
    //             label: '报告列表(流水线)',
    //             path: '/testutils/precision/reports',
    //         },
    //         {
    //             label: '配置中心',
    //             path: '/testutils/precision/config',
    //         },
    //     ],
    // }
]

function setup(route) {
    const mtype = ref(MTYPES.DEFAULT)

    const name = sessionStorage.getItem('project_name') ?? ''
    const currentMsProject = ref(name)

    const menus = MENU_ITEMS.filter(function ({ hidden = false }) {
        return !hidden
    })

    const activeIndex = ref((menus[0] ?? {}).path ?? '')

    watch(function () {
        return route.path
    }, function (cpath) {
        switchSelect(cpath, mtype)
        if (Utils.nil(cpath)) {
            return
        }

        const prefix = `/${TESTUTILS}`
        if (cpath.startsWith(prefix)) {
            const dpath = determinPath(cpath, menus)
            if (!Utils.nil(dpath)) {
                activeIndex.value = dpath
            }
        }
    }, { immediate: true })

    return { menus, activeIndex, mtype, currentMsProject }
}

function determinPath(cpath, menus) {
    function match(path) {
        return !Utils.nil(path) && cpath.startsWith(path)
    }

    for (let i = 0; i < menus.length; i++) {
        const { path: mpath, prefix, children = [] } = menus[i]
        if (match(mpath)) {
            return mpath
        } else if (match(prefix)) {
            for (let j = 0; j < children.length; j++) {
                const { path: smpath } = children[j]
                if (match(smpath)) {
                    return smpath
                }
            }
            return (children[0] ?? {}).path
        }
    }

    return null
}

function switchSelect(cpath, mtype) {
    if (Utils.nil(cpath)) {
        mtype.value = MTYPES.DEFAULT
    } else if (cpath.startsWith(`/${TESTUTILS}/${MTYPES.PRECISION}`)) {
        mtype.value = MTYPES.PRECISION
    } else {
        mtype.value = MTYPES.DEFAULT
    }
}
</script>

<style scoped>
#menu-bar {
    border-bottom: 1px solid #E6E6E6;
    background-color: #FFF;
}
</style>
