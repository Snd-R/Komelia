import {registerPlugins} from '@/plugins'
import App from './App.vue'
import {createApp} from 'vue'
import {createI18n} from "vue-i18n";
import en from './locales/en.json'
import './external'
import ExternalFunctions from "@/external";

export const externalFunctions = new ExternalFunctions()

const i18n = createI18n({
  locale: 'en',
  fallbackLocale: 'en',
  messages: {
    en: en
  },
})
const app = createApp(App)
registerPlugins(app)
app.use(i18n)
app.mount('#app')

