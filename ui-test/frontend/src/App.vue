<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
import {registerRequestHeaders} from "@/api/ajax";

export default {
  beforeCreate() {
    registerRequestHeaders();
  }
};
</script>
<style>
.el-message {
  z-index: 9999 !important; 
}
</style>