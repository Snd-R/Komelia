/**
 * plugins/vuetify.ts
 *
 * Framework documentation: https://vuetifyjs.com`
 */

// Styles
import 'vuetify/styles'
import {aliases, mdi} from 'vuetify/iconsets/mdi-svg'

// Composables
import {createVuetify} from 'vuetify'


export default createVuetify({
    theme: {
        defaultTheme: 'dark',
    },
    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: {
            mdi,
        },
    },
})