import user from 'metersphere-frontend/src/store/modules/user';
import commandStore from './command/command-state'
import {defineStore} from 'pinia';

let useUserStore = defineStore(user);
let useCommandStore = defineStore(commandStore);
const useStore = () => ({
  user: useUserStore()
});

export {
  useUserStore,
  useCommandStore,
  useStore as default
};
