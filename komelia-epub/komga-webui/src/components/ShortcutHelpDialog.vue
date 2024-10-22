<template>
  <v-dialog
      offset-y :max-height="height"
      v-model="dialog"
      scrollable
  >
    <v-card>
      <v-btn icon absolute top right @click="dialog = false">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-card-text>
        <v-row>
          <v-col v-for="(category, i) in Object.keys(shortcuts)" :key="i" cols="12" md="4">
            <div class="text-center text-h6">
              {{ category }}
            </div>
            <v-table>
              <template v-slot:default>
                <thead>
                <tr>
                  <th class="text-left">{{ t('dialog.shortcut_help.label_key') }}</th>
                  <th class="text-left">{{ t('dialog.shortcut_help.label_description') }}</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(s, j) in shortcuts[category]"
                    :key="j"
                >
                  <td>
                    <kbd style="font-size: 1.2em" class="text-truncate">
                      {{ s.display }}
                    </kbd>
                  </td>
                  <td>{{ t(s.description) }}</td>
                </tr>
                </tbody>
              </template>
            </v-table>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {ref, watch} from 'vue'
import {useDisplay} from 'vuetify'
import {useI18n} from "vue-i18n";

const emit = defineEmits(['update:modelValue'])
const {t} = useI18n()
const {height} = useDisplay()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  shortcuts: {
    type: Object,
    required: true,
  },
})
const dialog = ref(props.modelValue)

watch(() => props.modelValue, newVal => {
      dialog.value = newVal
    },
    {immediate: true}
)

watch(() => dialog.value, newVal => {
  emit('update:modelValue', newVal)
})
</script>
