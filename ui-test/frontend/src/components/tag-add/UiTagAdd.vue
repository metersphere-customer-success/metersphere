<template>
  <div class="el-input-tag input-tag-wrapper">
    <el-tag
        class="tag"
        v-for="(tag, index) in innerTags"
        :key="tag.id"
        effect="plain"
        :closable="true"
        @close="deleteTag(index)"
        size="small"
    >
      {{ tag.name }}
    </el-tag>
    <el-button
        plain
        size="mini"
        class="add-button"
        icon="el-icon-plus"
        @click="clickAdd"
    />
  </div>
</template>

<script>
/**
 * 形如ms-tag-input 的通过点击添加按钮添加tag
 * 输入：对象数组 []
 */
export default {
  name: "UiTagAdd",
  data() {
    return {
      innerTags: []
    }
  },
  props: {
    tags: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    tags(val) {
      this.innerTags = val;
    }
  },
  mounted() {
    this.innerTags = this.tags;
  },
  methods: {
    gettags() {
      return this.innerTags;
    },
    deleteTag(index) {
      this.innerTags.splice(index, 1);
    },
    clickAdd() {
      this.$emit("add");
    }
  }
}
</script>

<style scoped>
.tag {
  margin-right: 10px;
  background-color: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
}

.input-tag-wrapper {
  position: relative;
  font-size: 14px;
  background-color: #fff !important;
  background-image: none;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
  color: #606266;
  display: inline-block;
  outline: none;
  padding: 0 10px 0 5px;
  transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
  min-width: 560px;
  max-width: 618px;
  width:618px
}

.add-button {
  padding: 5px;
  border: 1px dashed #dcdfe6;
}
</style>