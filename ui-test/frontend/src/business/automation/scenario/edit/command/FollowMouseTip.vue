<template>
  <div
    class="container"
    v-if="active && content"
    :style="{ top: top, left: left }"
  >
    {{ content }}
  </div>
</template>
<script>
export default {
  name: "FollowMouseTip",
  props: {
    duration: {
      type: Number,
      //ms
      default: 1000,
    },
  },
  data() {
    return {
      active: false,
      TIMER: -1,
      content: "",
      x: 0,
      y: 0,
    };
  },
  computed: {
    top() {
      return this.y - 80 + "px";
    },
    left() {
      return this.x - 20 + "px";
    },
  },
  methods: {
    tip(content, x, y) {
      this.content = content;
      this.x = x;
      this.y = y;
      this.active = true;
      clearTimeout(this.TIMER);
      this.TIMER = setTimeout(() => {
        this.active = false;
      }, this.duration);
    },
  },
};
</script>
<style scoped>
.container {
  position: absolute;
  z-index: 9999;
  width: auto;
  height: 30px;
  background-color: #f0f9eb;
  color: #7dc23a;
  line-height: 0;
}
</style>
