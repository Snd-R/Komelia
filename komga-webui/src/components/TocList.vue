<template>
  <v-list v-if="toc"
          v-model:selected="currentToc"
          expand
  >
    <template v-for="(t, i) in toc">
      <v-list-group v-if="t.children"
                    :key="i"
                    no-action
      >
        <template v-slot:activator>
          <toc-list-item :item="t" @goto="goto" class="ps-0"/>
        </template>

        <toc-list-item v-for="(child, i) in t.children"
                       :key="i"
                       :item="child"
                       @goto="goto"
                       :class="`ms-${((child.level??1) - 1) * 4}`"
        />
      </v-list-group>

      <!--  Single item    -->
      <template v-else>
        <toc-list-item :key="i" :item="t" @goto="goto"/>
      </template>
    </template>
  </v-list>
</template>

<script setup lang="ts">
import {PropType, ref, Ref} from 'vue'
import {TocEntry} from '@/types/epub'
import TocListItem from '@/components/TocListItem.vue'

const emit = defineEmits(['goto'])
const props = defineProps({
  toc: {
    type: Array as PropType<Array<TocEntry>>,
    required: false,
  },

})

const currentToc: Ref<any> = ref(props.toc?.find((el) => el.current == true)?.href)

function goto(element: TocEntry) {
  emit('goto', element)
}
</script>

<style>
</style>
