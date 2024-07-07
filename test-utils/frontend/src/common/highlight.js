import hljs from "highlight.js";
import LineifyPlugin from "../lib/highlight.lineify.min.js";
import "highlight.js/styles/atom-one-light.css";
import { Utils } from "@/common";

const plugins = { DYE: [] };

function lineify($e, $lines, config) {
  for (let i = 0; i < $lines.length; i++) {
    const $line = $lines[i];
    $line.insertBefore(this.createLineNumSpan(i + 1), $line.firstChild);
  }
}

function linedye($e, $lines, config) {
  const name = $e.getAttribute("name");
  const plugin = plugins.DYE[name];
  if (!Utils.nil(plugin)) {
    plugin($lines, config, $e);
  }
}

function linecovs($e, $lines, config) {
  const plugin = plugins["COVS"];
  if (!Utils.nil(plugin)) {
    plugin($lines, config, $e);
  }
}

hljs.addPlugin(new LineifyPlugin(lineify));
hljs.addPlugin(new LineifyPlugin(linedye));
hljs.addPlugin(new LineifyPlugin(linecovs));

function addDyePlugin(name, callback) {
  plugins.DYE[name] = callback;
}

function delDyePlugin(name) {
  delete plugins.DYE[name];
}

function setCovsPlugin(callback) {
  plugins["COVS"] = callback;
}

const HighLightJS = { hljs, addDyePlugin, delDyePlugin, setCovsPlugin };

export default HighLightJS;
