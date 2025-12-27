<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" class="text-left" align-self="center">
      <span class="">{{ label }}</span>
    </v-col>
    <v-col cols="7">
      <v-select
          filled
          dense
          solo
          :items="items"
          v-model="input"
          @update:modelValue="updateInput"
          @change="updateInput"
          :hide-details="true"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import {Ref, ref, watch} from 'vue'

const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  items: null as any,
  label: {
    type: String,
  },
  modelValue: {
    type: [String, Number, Boolean],
  },
  display: {
    type: String,
  },
})

const input: Ref<string | number | boolean | undefined> = ref(props.modelValue)

watch(() => props.modelValue, newVal => {
      input.value = newVal
    },
    {immediate: true}
)

function updateInput() {
  emit('update:modelValue', input.value)
}
</script>

<style>
.v-text-field__details, div.v-input__control {
  min-height: 0 !important;
}

.v-text-field__details {
  display: none !important;
}

</style>
