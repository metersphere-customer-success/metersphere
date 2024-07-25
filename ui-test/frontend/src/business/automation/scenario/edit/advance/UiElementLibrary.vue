<template>
  <ui-element-library-list/>
</template>

<script>

    import UiElementLibraryList from "./element/UiElementLibraryList";
    import UiElementSimpleEdit from "./element/UiElementSimpleEdit";

    export default {
      name: "UiElementLibrary",
      props: {
      },
      components: {UiElementSimpleEdit, UiElementLibraryList},
      data() {
        return {
          currentElement: {}
        }
      },
      methods: {

      }
    }
</script>

<style scoped>

</style>
