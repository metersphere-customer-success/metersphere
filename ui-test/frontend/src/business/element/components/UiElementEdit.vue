<template>
  <ms-edit-dialog
    width="30%"
    :visible.sync="visible"
    @confirm="save"
    :title="form && form.id ? $t('permission.project_ui_element.edit') : $t('permission.project_ui_element.create')"
    append-to-body
    ref="msEditDialog">

    <el-form
      label-position="right"
      size="small"
      :model="form"
      :rules="rules"
      ref="form">

      <el-form-item
        prop="name"
        :label="$t('ui.element_name')"
        :label-width="labelWidth">
        <el-input
          v-model="form.name"
          autocomplete="off"/>
      </el-form-item>

      <el-form-item
        prop="moduleId"
        :label="$t('test_track.case.module')"
        :label-width="labelWidth">
        <select-tree
          clearable
          checkStrictly
          size="small"
          :data="moduleTree"
          :defaultKey="form.moduleId"
          :obj="{id: 'id', label: 'name'}"
          @getValue="setModule"/>
      </el-form-item>

      <el-form-item
        prop="locationType"
        :label="$t('ui.element_locator_type')"
        :label-width="labelWidth">
        <el-select
          filterable
          v-model="form.locationType"
          :placeholder="$t('ui.element_locator_type')">
          <el-option
            v-for="item in locationTypeOptions"
            :key="item.value"
            :label="$t(item.text)"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item
        prop="location"
        :label="$t('ui.element_locator')"
        :label-width="labelWidth">
        <el-input
          v-model="form.location"
          autocomplete="off"/>
      </el-form-item>

      <el-form-item
        rop="description"
        :label="$t('ui.description')"
        :label-width="labelWidth">
        <el-input
          :autosize="{ minRows: 2, maxRows: 4 }"
          type="textarea"
          :maxlength="512"
          v-model="form.description"/>
      </el-form-item>

    </el-form>

  </ms-edit-dialog>
</template>

<script>

import MsEditDialog from "@/business/element/components/common/dialog/MsEditDialog";
import MsSingleHandleDrag from "@/business/element/components/MsSingleHandleDrag";
import {
  UI_ELEMENT_LOCATION_TYPE_OPTION
} from "metersphere-frontend/src/utils/table-constants";
import SelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
import {addUiElement, editUiElement} from "@/business/network/ui-element";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";


export default {
  name: "UiElementEdit",
  components: {SelectTree, MsSingleHandleDrag, MsEditDialog},
  props: {
    moduleTree: Array,
    labelWidth: {
      Object: String,
      default() {
        return '130px';
      }
    }
  },
  data() {
    var moduleValidator = (rule, value, callback) => {
      if (value !== ''){
        if(!value ){
          callback(new Error(rule.message));
        }
        if(this.moduleIdInit && Array.isArray(value) && value.length <= 0){
          callback(new Error(rule.message));
        }
      }

      if(value &&  !Array.isArray(value)){
        this.moduleIdInit = true;
      }
      if (value === '' && this.moduleIdInit) {
          callback(new Error(rule.message));
      }
      
      callback();
    };
    return {
      form: {
        name: '',
        moduleId: '',
        locationType: '',
        location: '',
        description: ''
      },
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      moduleIdInit: false,
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
        ],
        moduleId: [
          {required: true, validator: moduleValidator, message: this.$t('test_track.case.input_module'), trigger: 'blur'}
        ],
        locationType: [
          {required: true, message: this.$t('ui.pls_sel_loctype'), trigger: 'blur'}
        ],
        location: [
          {required: true, message: this.$t('ui.pls_input_locator'), trigger: 'blur'},
          {max: 300, message: this.$t('test_track.length_less_than') + '300', trigger: 'blur'}
        ],
      },
      visible: false,
      originModuleId: null
    };
  },
  computed: {
    locationTypeOptions() {
      return UI_ELEMENT_LOCATION_TYPE_OPTION;
    }
  },
  methods: {
    open(data, moduleId) {
      this.visible = true;
      if (data) {
        Object.assign(this.form, data);
      } else {
        this.resetFrom();
      }
      if (moduleId) {
        this.form.moduleId = moduleId;
      }
      this.originModuleId = this.form.moduleId;
    },
    setModule(id) {
      if(!id && !this.moduleId){
        id = this.moduleId;
      }
      this.form.moduleId = id;
    },
    resetFrom() {
      this.form = {
        name: '',
        moduleId: '',
        locationType: '',
        location: '',
        description: ''
      };
    },
    save() {
      this.moduleIdInit = true;
      this.$refs.form.validate((valid) => {
        if (valid) {
          let param = {};
          Object.assign(param, this.form);
          param.projectId = getCurrentProjectID();
          let saveFunc = addUiElement;
          if (param.id) {
            saveFunc = editUiElement;
          }
          this.result = saveFunc(param).then(data => {
            this.visible = false;
            if (!param.id) {
              param.id = data.data;
              this.$emit('refreshTree');
            } else if (this.originModuleId !== param.moduleId){
              this.$emit('refreshTree');
            }
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
            this.$emit('save', param);

            if(this.form.moduleId){
              localStorage.setItem("RECENTLY_ELEMENT_MODULE", this.form.moduleId);
            }
          });
        }
      });
    }
  }
};
</script>

<style scoped>

</style>
