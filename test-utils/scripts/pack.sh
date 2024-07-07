#!/bin/bash

ROOT_PATH=$(cd "$(dirname "$0")"/..; pwd)
echo "${ROOT_PATH}"

DST_DIR="/mnt/d/share/dimgs/"

BE_DIR="${ROOT_PATH}/backend"
FE_DIR="${ROOT_PATH}/frontend"

TARGET_DIR="${BE_DIR}/target"

PRD_NAME="test-utils"
DEP_DIR="dependency"

check_jar() {
    local version="$1"
    local jarfile="${TARGET_DIR}/${PRD_NAME}-${version}.jar"
    [[ -f "${jarfile}" ]] || { echo "[ERROR] file not found: ${jarfile}"; exit 1; }
    echo "[   OK] file found: ${jarfile}"
}

make_deps() {
    cd "${TARGET_DIR}" || { echo "[ERROR] cd error: ${TARGET_DIR}"; exit 1; } 

    rm -rf "${DEP_DIR}" 2>/dev/null
    mkdir "${DEP_DIR}" 2>/dev/null
    cd "${DEP_DIR}" || { echo "[ERROR] cd error: ${DEP_DIR}"; exit 1; }

    echo "[ INFO] jar -xf ../*.jar"
    jar -xf ../*.jar
    pwd
    ls -l
}

docker_build() {
    local tag="$1"
    local imgname="ms-${PRD_NAME}:${tag}"

    cd "${ROOT_PATH}" || { echo "[ERROR] cd error: ${ROOT_PATH}"; exit 1; }
    echo "[ INFO] docker remove image: ${imgname}"
    docker rmi -f "${imgname}"
    echo "[ INFO] docker build image: ${imgname}"
    docker build -t "${imgname}" .
    echo "[ INFO] docker images show"
    docker images | grep "${PRD_NAME}"
}

save_image() {
    local tag="$1"
    local imgname="ms-${PRD_NAME}:${tag}"
    local tarname="ms-${PRD_NAME}-${tag}.tar"

    cd "${ROOT_PATH}" || { echo "[ERROR] cd error: ${ROOT_PATH}"; exit 1; }
    rm -rf tmp 2>/dev/null
    mkdir tmp 2>/dev/null
    cd tmp || { echo "[ERROR] cd error: tmp"; exit 1; }
    echo "[ INFO] docker save image ..."
    docker save -o "${tarname}" "${imgname}"
    ls -l

    [[ -f "${tarname}" ]] || { echo "[ERROR] file not found: ${tarname}"; exit 1; }
    echo "[ INFO] copy image tar"
    cp -f "${tarname}" "${DST_DIR}"
    ls -l "${DST_DIR}"
}

main() {
    local version="${1}"
    [[ -n "${version}" ]] || { echo "[Error] need version"; exit 1; }

    check_jar "${version}"
    make_deps
    docker_build "${version}"
    save_image "${version}"
}

main "$@"
