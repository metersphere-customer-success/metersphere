import Utils from "./utils";

const TU_KEY = "testutils";

function getData(storage) {
  return JSON.parse(storage.getItem(TU_KEY) ?? "{}");
}

function setData(storage, data) {
  storage.setItem(TU_KEY, JSON.stringify(data));
}

function getItem(storage, ...keys) {
  const data = getData(storage);
  return Utils.unchain(data, keys, null);
}

function setItem(storage, val, ...keys) {
  const data = getData(storage);
  Utils.chain(data, keys, val);
  setData(storage, data);
}

function getLocalItem(...keys) {
  return getItem(localStorage, ...keys);
}

function setLocalItem(val, ...keys) {
  setItem(localStorage, val, ...keys);
}

function getSessionItem(...keys) {
  return getItem(sessionStorage, ...keys);
}

function setSessionItem(val, ...keys) {
  setItem(sessionStorage, val, ...keys);
}

const Storage = { getLocalItem, setLocalItem, getSessionItem, setSessionItem };

export default Storage;
