import {download, fileUpload} from "@/api/ajax"

export default {
  install(Vue) {
    Vue.prototype.$download = download;
    Vue.prototype.$fileUpload = fileUpload;
  }
}
