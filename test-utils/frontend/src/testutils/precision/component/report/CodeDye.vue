<template>
  <div>
    <div v-if="pulling" class="loading">{{ CONSTANS.LOADING }}</div>
    <div v-else>
      <el-row v-if="compare" :gutter="4">
        <el-col :span="12">
          <highlight-code :name="name" :code="code" class="compact" />
        </el-col>
        <el-col :span="12">
          <highlight-code :name="`${name}-base`" :code="code" class="compact" />
        </el-col>
      </el-row>
      <highlight-code v-else :name="name" :code="code" class="compact" />
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  tid: {
    type: String,
    required: true,
  },
  cid: {
    type: String,
    default: "",
  },
  mod: {
    type: String,
    required: true,
  },
  pkg: {
    type: String,
    required: true,
  },
  src: {
    type: String,
    required: true,
  },
});

const { pulling, name, code, compare } = setup(props);
</script>

<script>
import { computed, ref, watch } from "vue";

import { HighLightJS, Utils } from "@/common";
import { CONSTANS, getCode, getTask } from "../../stores/report";

import { HighlightCode } from "../common";

function setup(props) {
  const { pulling, name, code, compare, state } = useCode(props);
  useHighlight(name, state);

  return { pulling, name, code, compare };
}

function useCode(props) {
  const pulling = ref(false);

  const state = {};
  const code = ref("");

  const name = computed(function () {
    return `${props.mod}.${props.pkg}.${props.src}`;
  });

  const compare = computed(function () {
    return !Utils.empty(props.cid);
  });

  watch(
    props,
    async function ({ tid, cid, mod, pkg, src }) {
      if (Utils.nil(tid) || Utils.nil(mod) || Utils.nil(pkg) || Utils.nil(src)) {
        return;
      }

      pulling.value = true;

      async function makestate(id, key) {
        if (Utils.empty(id)) {
          return;
        }

        const data = await getTask(id, mod);
        const { path, sources } = Utils.unchain(data, [mod, 'packages', pkg]);
        state[key] = Utils.unchain(sources, [src, 'lines'], []);

        if (Utils.nil(state.path)) {
          state.path = path;
        }
      }

      await makestate(tid, 'lines');
      await makestate(cid, 'base');

      if (!Utils.nil(state.path)) {
        code.value = await getCode(tid, `${state.path}/${src}`, mod);
      }

      pulling.value = false;
    },
    { immediate: true, deep: true }
  );

  return { pulling, name, code, compare, state };
}

function useHighlight(name, state) {
  function linedye($lines, data) {
    data.forEach(function ([nr, mi, ci, mb, cb]) {
      const $line = $lines[nr - 1];
      if (!Utils.nil($line)) {
        if (ci > 0) {
          $line.classList.add(mb === 0 ? "fc" : "pc");
        }

        if (mb > 0 && cb > 0) {
          $line.classList.add("bpc");
          $line.title = `${mb} of ${mb + cb} branches missed.`;
        } else if (mb === 0 && cb > 0) {
          $line.classList.add("bfc");
          $line.title = `All ${cb} branches covered.`;
        }
      }
    });
  }

  function linedyeMain($lines) {
    linedye($lines, state.lines ?? []);
  }

  function linedyeBase($lines) {
    linedye($lines, state.base ?? []);
  }

  watch(
    name,
    function (newName, oldName) {
      if (!Utils.nil(oldName)) {
        HighLightJS.delDyePlugin(oldName);
        HighLightJS.delDyePlugin(`${oldName}-base`);
      }

      HighLightJS.addDyePlugin(newName, linedyeMain);
      HighLightJS.addDyePlugin(`${newName}-base`, linedyeBase);
    },
    { immediate: true }
  );
}
</script>

<style scoped>
div.loading {
  text-align: center;
  font-weight: bold;
}
</style>

<style>
@import "@/assets/precision/default.css";
</style>
