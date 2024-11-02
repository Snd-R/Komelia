<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" align-self="center">
      <span>{{ label }}</span>
    </v-col>
    <v-col cols="4" align-self="center" class="text-right">
      <span>{{ status }}</span>
    </v-col>
    <v-col cols="3" align-self="center">
      <v-switch v-model="input" dense
                @update:modelValue="updateInput"
                @change="updateInput"
                class="float-right"
                :disabled="disabled"
      >
      </v-switch>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue'

const emit = defineEmits(['update:modelValue'])
const props = defineProps({
  label: {
    type: String,
  },
  value: {
    type: Boolean,
  },
  status: {
    type: String,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})

const input = ref(false)

function updateInput() {
  emit('update:modelValue', input.value)

}

watch(() => props.value, newVal => {
    input.value = newVal
  },
  {immediate: true})
</script>
