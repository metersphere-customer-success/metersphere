<template>
  <div>
    <el-input-number
      size="small"
      type="number"
      v-model="coord.x"
      :min="0"
      @change="limitNumber($event, 'x')"
      :controls="false"
      :placeholder="$t('ui.input_content_x')"/>

    <el-input-number
      size="small"
      class="ml10"
      type="number"
      v-model="coord.y"
      :min="0"
      @change="limitNumber($event, 'y')"
      :controls="false"
      :placeholder="$t('ui.input_content_y')"/>

    <el-tooltip effect="dark" placement="top-start" content="默认当前元素左上角为坐标初始位置0,0；">
      <i class="el-icon-info pointer"/>
    </el-tooltip>

    <span
      class="coord-opt ml10"
      @click="coordOpt('add', coords, coord.index)">
      <i class="el-icon-plus"/>
    </span>

    <span
      class="coord-opt"
      @click="coordOpt('del', coords, coord.index)">
      <i class="el-icon-minus"/>
    </span>

  </div>
</template>

<script>
import { findIndex } from "lodash-es";
import {uuid} from "metersphere-frontend/src/model/ApiTestModel";

export default {
  name: "MouseCoord",
  props: {
    //当前坐标
    coord: {
      type: Object,
      default() {
        return {
          index: uuid(),
          x: 0,
          y: 0
        }
      }
    },
    //母坐标数组
    coords: {
      type: Array,
      default() {
        return [];
      }
    }
  },
  methods: {
    coordOpt(type, coords, index) {
      if (type === "add") {
        coords.splice(findIndex(coords, {"index": index}) + 1, 0, {index: uuid(), x: 0, y: 0});
      } else {
        if (coords.length != 1) {
          coords.splice(findIndex(coords, {"index": index}), 1);
        }
      }
      this.$emit("forceUpdate");
    },
    limitNumber(value, type) {
      this.$nextTick(() => {
        this.coord[type] = parseInt(value);
      });
    }
  }
}
</script>

<style scoped>
.ml10 {
  margin-left: 10px;
}

.coord-opt {
  cursor: pointer;
  color: #4b1980;
  margin-left: 15px;
}
</style>
