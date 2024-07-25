<template>
  <div class="edit-container">
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
    </el-form>

    <el-button
      class="save-btn"
      size="small"
      type="primary"
      @click="save">
      {{ $t('commons.save') }}
    </el-button>

  </div>
</template>

<script>
  import MsEditDialog from "@/business/element/components/common/dialog/MsEditDialog";
  import MsSingleHandleDrag from "@/business/element/components/MsSingleHandleDrag";
  import {
    UI_ELEMENT_LOCATION_TYPE_OPTION
  } from "metersphere-frontend/src/utils/table-constants";
  import SelectTree from "metersphere-frontend/src/components/select-tree/SelectTree";
  import {editUiElement} from "@/business/network/ui-element";
  import {useCommandStore} from "@/store";

  const commandStore = new useCommandStore();
  export default {
    name: "UiElementSimpleEdit",
    components: {SelectTree, MsSingleHandleDrag, MsEditDialog},
    props: {
      labelWidth: {
        Object: String,
        default() {
          return '100px';
        }
      }
    },
    data() {
      return {
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        form: {
          name: '',
          locationType: '',
          location: ''
        },
        rules: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
          ],
          locationType: [
            {required: true, message: this.$t('ui.pls_sel_loctype'), trigger: 'blur'}
          ],
          location: [
            {required: true, message: this.$t('ui.pls_input_locator'), trigger: 'blur'},
            {max: 300, message: this.$t('test_track.length_less_than') + '300', trigger: 'blur'}
          ],
        },
        commandStore: commandStore
      };
    },
    watch: {
      'commandStore.librarySelectElement'() {
        Object.assign(this.form, commandStore.librarySelectElement);
      }
    },
    computed: {
      locationTypeOptions() {
        return UI_ELEMENT_LOCATION_TYPE_OPTION;
      },
    },
    methods: {
      save() {
        this.$refs.form.validate((valid) => {
          if (valid) {
            let param = {};
            Object.assign(param, this.form);
            this.result = editUiElement(param).then(data => {
              this.visible = false;
              param.id = data.data;
              Object.assign(commandStore.librarySelectElement, this.form);
              this.$success(this.$t('commons.save_success'));
            });
          }
        });
      }
    }
  };
</script>

<style scoped>

  .save-btn {
    float: right;
  }

  .edit-container {
    padding: 80px 20px;
  }

</style>
