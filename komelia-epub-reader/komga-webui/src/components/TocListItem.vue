<template>
  <v-list-item
      :value="item.href"
      :active="item.current"
      link
      color="primary"
  >
    <v-list-item-title
        @click.stop="goto(item)"
    >
      {{ item.title }}
    </v-list-item-title>

    <template v-if="item.children" v-slot:append>
      <v-icon-btn
          :icon="expandIcon"
          @click.stop="expand"
          width="100"
          v-ripple
      />
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import {computed, PropType} from 'vue'
import {TocEntry} from '@/types/epub'
import {VListItem} from "vuetify/components";
import {VIconBtn} from 'vuetify/labs/VIconBtn'
import {mdiChevronDown, mdiChevronUp} from "@mdi/js";

const emit = defineEmits(['goto', 'expand'])

const props = defineProps({
  item: {
    type: Object as PropType<TocEntry>,
    required: true,
  },
  expanded: Boolean
})

function goto(element: TocEntry) {
  emit('goto', element)
}

const expandIcon = computed(() => {
  if (props.expanded) {
    return mdiChevronUp
  } else {
    return mdiChevronDown

  }
})

function expand() {
  emit('expand', props.item?.href)
}

</script>

<style>
</style>
