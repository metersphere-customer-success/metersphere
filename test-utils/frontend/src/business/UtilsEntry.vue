<template>
  <el-col>
    <utils-header />
    <div>
      <transition>
        <keep-alive>
          <router-view />
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>
import UtilsHeader from "./header/UtilsHeader";

export default {
  name: "UtilsEntry",
  components: { UtilsHeader },
}
</script>
