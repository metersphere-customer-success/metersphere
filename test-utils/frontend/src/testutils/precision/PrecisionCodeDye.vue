<template>
  <div style="margin: 5px">
    <permission-query v-if="querying" />
    <div v-else-if="permit">
      <div v-if="packages.length <= 0" class="info">
        {{ CONSTANS.UNCOVERED }}
      </div>
      <el-collapse>
        <el-collapse-item v-for="(mod, midx) in packages" :title="mod.name" :name="mod.name" :key="midx">
          <template slot="title">
            <div class="bg-img mod-title">{{ mod.name }}</div>
          </template>
          <el-collapse>
            <el-collapse-item v-for="(pkg, pidx) in mod.packages" :title="pkg.name" :name="pkg.name" :key="pidx">
              <template slot="title">
                <div class="bg-img pkg-title">{{ pkg.name }}</div>
              </template>
              <el-collapse style="margin: 10px" @change="(srcs) => handleSources(mod.name, pkg.name, srcs)">
                <el-collapse-item v-for="(src, sidx) in pkg.sources" :title="src.name" :name="src.name"
                  :key="`${pidx}-${sidx}`">
                  <template slot="title">
                    <div class="bg-img src-title">{{ src.name }}</div>
                  </template>
                  <code-dye v-if="src.show" :tid="tid" :cid="cid" :mod="mod.name" :pkg="pkg.name" :src="src.name" />
                </el-collapse-item>
              </el-collapse>
            </el-collapse-item>
          </el-collapse>
        </el-collapse-item>
      </el-collapse>
    </div>
    <no-permission-hint v-else :name="appname" />
  </div>
</template>

<script setup>
const route = useRoute();
const { permit, appname, querying, tid, cid, packages, handleSources } = setup(route);
</script>

<script>
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router/composables";

import { Utils } from "@/common";
import { CONSTANS, getCodedye, getTask } from "./stores/report";
import { TYPES, useProject, useTaskPermission } from "@/store/project";

import { NoPermissionHint, PermissionQuery } from "./component/common";
import { CodeDye } from "./component/report";

function setup(route) {
  const tid = computed(function () {
    return route.params.tid ?? "";
  });

  const cid = computed(function () {
    return route.params.cid ?? "";
  });

  useProject(TYPES.COVS, true, tid);
  const { permit, appname, querying } = useTaskPermission(tid);

  const { pkgsmap, pkgslist } = usePackages(route, appname, permit);
  const { handleSources } = useSourcesShow(pkgsmap);

  return { permit, appname, querying, tid, cid, packages: pkgslist, handleSources };
}

function usePackages(route, appname, permit) {
  const pkgsmap = ref(null);

  watch(
    function () {
      return [route.params.tid, route.params.cid, permit.value];
    },
    async function ([tid, cid, permit]) {
      if (!Utils.nil(tid) && permit) {
        const tdata = await getTask(tid, appname.value);
        const bdata = await getTask(cid, appname.value);
        pkgsmap.value = getCodedye(tid, tdata, cid, bdata);
      }
    },
    { immediate: true }
  );

  const pkgslist = computed(function () {
    return Object.keys(pkgsmap.value ?? {}).map(function (mod) {
      const pkgsInfo = (pkgsmap.value ?? {})[mod] ?? {};
      const packages = Object.keys(pkgsInfo).map(function (pkg) {
        const srcs = pkgsInfo[pkg] ?? {};
        const sources = Object.keys(srcs).map(function (src) {
          return { name: src, show: srcs[src] };
        });
        return { name: pkg, sources };
      });

      return { name: mod, packages }
    });
  });

  return { pkgsmap, pkgslist };
}

function useSourcesShow(pkgsmap) {
  function handleSources(mod, pkg, srcs = []) {
    const data = Utils.unchain(pkgsmap.value, [mod, pkg], {})
    Object.keys(data).forEach(function (src) {
      pkgsmap.value[mod][pkg][src] = srcs.includes(src);
    });
  }

  return { handleSources };
}
</script>

<style scoped>
div.info {
  margin: 1em 0;
  text-align: center;
  font-weight: bold;
}

div.mod-title {
  font-size: 15px;
  font-weight: bold;
  background-image: url("@/assets/precision/report.gif");
}

div.pkg-title {
  font-size: 15px;
  font-weight: bold;
  background-image: url("@/assets/precision/package.gif");
}

div.src-title {
  font-size: 14px;
  background-image: url("@/assets/precision/source.gif");
}

div.bg-img {
  margin-left: 10px;
  padding-left: 18px;
  background-position: left center;
  background-repeat: no-repeat;
}
</style>
