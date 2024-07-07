<template>
    <div @click="enterApp">
        <el-card class="util-card" shadow="hover" :body-style="{ padding: '0px' }">
            <div class="image">
                <img :src="imgSrc(info.img)" alt="info.img" />
            </div>
            <div class="title">
                {{ info.name }}
            </div>
        </el-card>
    </div>
</template>

<script setup>
const props = defineProps({
    info: {
        type: Object,
        required: true,
    }
})

const enterApp = function () {
    const inner = props.info.inner ?? false
    if (inner) {
        window.open(`/#${props.info.url}`, "_self")
    } else {
        window.open(props.info.url, "_blank")
    }
}

const imgSrc = function (name) {
    return require(`../../assets/images/testutils.${name}.png`)
}
</script>

<style scoped>
.util-card {
    cursor: pointer;
}

.image {
    text-align: center;
}

.image img {
    width: 50%;
}

.title {
    margin: 0 20px;
    text-align: center;
    font-size: 16px;
    padding: 10px;
}
</style>
