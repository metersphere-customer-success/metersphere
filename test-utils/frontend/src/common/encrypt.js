import CryptoJS from 'crypto-js'

const UTF8 = CryptoJS.enc.Utf8
const BASE64 = CryptoJS.enc.Base64

const OPTION = {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
}

function _getSecretKey (key) {
    return UTF8.parse(key)
}

function encrypt (key, words) {
    const skey = _getSecretKey(key)
    const encrypted = CryptoJS.DES.encrypt(words, skey, OPTION)

    // encrypted.toString()
    return encrypted.ciphertext.toString(BASE64)
}

function decrypt (key, words) {
    const skey = _getSecretKey(key)
    const decrypted = CryptoJS.DES.decrypt({
        ciphertext: BASE64.parse(words)
    }, skey, OPTION)

    return decrypted.toString(UTF8)
}

const Encrypt = { encrypt, decrypt }

export default Encrypt