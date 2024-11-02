<template>
  <v-list-item
      :value="item.href"
      link
      color="primary"
      @click.stop="goto(item)"
  >
    <v-list-item-title
        :title="item.title"
        :class="item.current ? 'primary--text' : ''"
        :style="item.href ? 'cursor: pointer' : undefined"
    >
      {{ item.title }}
    </v-list-item-title>
  </v-list-item>
</template>

<script setup lang="ts">
import {PropType} from 'vue'
import {TocEntry} from '@/types/epub'

const emit = defineEmits(['goto'])

defineProps({
  item: {
    type: Object as PropType<TocEntry>,
    required: true,
  },

})

function goto(element: TocEntry) {
  emit('goto', element)
}

</script>

<style>
</style>
