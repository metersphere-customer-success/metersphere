<template>
  <el-col>
    <ui-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>
<script>

import UiHeaderMenus from "./head/UiHeaderMenus";

export default {
  name: "MsUITest",
  components: {
    UiHeaderMenus
  },
  data() {
    return {
    }
  }
}

</script>

<style scoped>

</style>
