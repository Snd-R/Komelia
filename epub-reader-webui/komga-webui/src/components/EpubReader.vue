<template>
  <div id="root" :key="bookId">
    <v-slide-y-transition>
      <v-toolbar
          v-if="showToolbars"
          dense elevation="1"
          class="settings full-width"
          style="position: fixed; top: 0;z-index: 14"
      >
        <v-btn
            icon
            @click="closeBook"
        >
          <v-icon :icon="mdiArrowLeft"/>
        </v-btn>

        <v-btn
            :disabled="!hasToc && !hasLandmarks && !hasPageList"
            icon
            @click="showToc = !showToc">
          <v-icon :icon="mdiTableOfContents"/>
        </v-btn>

        <v-toolbar-title> {{ bookTitle }}</v-toolbar-title>
        <v-spacer></v-spacer>

        <v-btn
            icon
            :disabled="!fullscreenIsAvailable"
            @click="switchFullscreen()">
          <v-icon :icon="mdiFullscreen"/>
        </v-btn>

        <v-btn
            icon
            @click="showHelp = !showHelp">
          <v-icon :icon="mdiHelpCircle"/>
        </v-btn>

        <v-btn
            icon
            @click="toggleSettings"
        >
          <v-icon :icon="mdiCog"/>
        </v-btn>
      </v-toolbar>
    </v-slide-y-transition>

    <v-slide-y-reverse-transition>
      <!-- Bottom Toolbar-->
      <v-toolbar
          dense
          elevation="1"
          class="settings full-width"
          style="position: fixed; bottom: 0;z-index: 14"
          horizontal
          v-if="showToolbars"
      >
        <v-btn icon @click="previousBook">
          <v-icon :icon="mdiUndo"/>
        </v-btn>

        <v-spacer/>

        <v-btn
            icon
            :disabled="!historyCanGoBack"
            @click="historyBack"
        >
          <v-icon :icon="mdiChevronLeft"/>
        </v-btn>

        <span v-if="verticalScroll" class="mx-2" style="font-size: 0.85em">
          {{ progressionTotalPercentage }}
        </span>

        <v-btn
            icon
            :disabled="!historyCanGoForward"
            @click="historyForward"
        >
          <v-icon :icon="mdiChevronRight"/>
        </v-btn>

        <v-spacer/>

        <v-btn icon @click="nextBook">
          <v-icon :icon="mdiRedo"/>
        </v-btn>
      </v-toolbar>
    </v-slide-y-reverse-transition>

    <v-navigation-drawer
        v-model="showToc"
        temporary
        :width="smAndUp ? 500 : width - 50"
    >
      <v-tabs grow v-model="tab">
        <v-tab value="hasToc" v-if="hasToc">
          <v-icon :icon="mdiTableOfContents"/>
        </v-tab>
        <v-tab value="hasLandmarks" v-if="hasLandmarks">
          <v-icon :icon="mdiEiffelTower"/>
        </v-tab>
        <v-tab value="hasPageList" v-if="hasPageList">
          <v-icon :icon="mdiNumeric"/>
        </v-tab>
      </v-tabs>

      <v-tabs-window v-model="tab">
        <v-tabs-window-item value="hasToc" v-if="hasToc" class="scrolltab">
          <toc-list :toc="tableOfContents" @goto="goToEntry" class="scrolltab-content"/>
        </v-tabs-window-item>
        <v-tabs-window-item value="hasLandmarks" v-if="hasLandmarks" class="scrolltab">
          <toc-list :toc="landmarks" @goto="goToEntry" class="scrolltab-content"/>
        </v-tabs-window-item>
        <v-tabs-window-item value="hasPageList" v-if="hasPageList" class="scrolltab">
          <toc-list :toc="pageList" @goto="goToEntry" class="scrolltab-content"/>
        </v-tabs-window-item>
      </v-tabs-window>
    </v-navigation-drawer>

    <header id="headerMenu"/>

    <div id="D2Reader-Container" style="height: 100vh" :class="appearanceClass('bg')">
      <main tabindex=-1 id="iframe-wrapper" style="height: 100vh" @click="clickThrough">
        <div id="reader-loading"></div>
        <div id="reader-error"></div>
      </main>
      <a id="previous-chapter" rel="prev" role="button" aria-labelledby="previous-label"
         style="left: 50%;position: fixed;color: #000;height: 24px;background: #d3d3d33b; width: 150px;transform: translate(-50%, 0); display: block"
         :style="`top: ${showToolbars ? 48 : 0}px`"
         :class="settings.navigationButtons ? '' : 'hidden'"
      >
        <v-icon :icon="mdiChevronUp" style="left: calc(50% - 12px); position: relative;"/>
      </a>
      <a id="next-chapter" rel="next" role="button" aria-labelledby="next-label"
         :class="settings.navigationButtons ? '' : 'hidden'"
         style="bottom: 0;left: 50%;position: fixed;color: #000;height: 24px;background: #d3d3d33b; width: 150px;transform: translate(-50%, 0); display: block">
        <v-icon :icon="mdiChevronDown" style="left: calc(50% - 12px);position: relative;"/>
      </a>
    </div>

    <footer id="footerMenu">
      <a rel="prev" class="disabled" role="button" aria-labelledby="previous-label"
         style="top: 50%;left:0;position: fixed;height: 100px;background: #d3d3d33b;"
         :class="settings.navigationButtons ? '' : 'hidden'"
      >
        <v-icon :icon="mdiChevronLeft" style="top: calc(50% - 12px);
                        position: relative;"/>
      </a>
      <a rel="next" class="disabled" role="button" aria-labelledby="next-label"
         style="top: 50%;right:0;position: fixed;height: 100px;background: #d3d3d33b;"
         :class="settings.navigationButtons ? '' : 'hidden'"
      >
        <v-icon :icon="mdiChevronRight" style="top: calc(50% - 12px);position: relative;"/>
      </a>
    </footer>

    <v-container fluid class="full-width" style="position: fixed; bottom: 0; font-size: .85rem"
                 :class="appearanceClass()"
                 v-if="!verticalScroll"
    >
      <v-row>
        <v-col cols="10" class="text-truncate">
          {{ t('epubreader.page_of', {page: progressionPage, count: progressionPageCount}) }}
          ({{ progressionTitle || t('epubreader.current_chapter') }})
        </v-col>
        <v-spacer/>
        <v-col cols="auto">{{ progressionTotalPercentage }}</v-col>
      </v-row>
    </v-container>

    <v-bottom-sheet
        style="max-width:500px;"
        v-model="showSettings"
        :close-on-content-click="false"
        :opacity="0"
        @keydown.esc.stop=""
        scrollable
    >
      <v-card>
        <v-toolbar dark color="primary">
          <v-btn icon dark @click="showSettings = false">
            <v-icon :icon="mdiClose"/>
          </v-btn>
          <v-toolbar-title>{{ t('bookreader.reader_settings') }}</v-toolbar-title>
        </v-toolbar>

        <v-card-text class="pa-0">
          <v-list class="full-height full-width">
            <v-list-subheader class="font-weight-black text-h6">{{
                t('bookreader.settings.general')
              }}
            </v-list-subheader>
            <v-list-item v-if="fixedLayout">
              <settings-select
                  :items="readingDirs"
                  v-model="readingDirection"
                  :label="t('bookreader.settings.reading_mode')"
              />
            </v-list-item>

            <v-list-item>
              <settings-select
                  :items="navigationOptions"
                  v-model="navigationMode"
                  :label="t('epubreader.settings.navigation_mode')"
              />
            </v-list-item>

            <v-list-subheader class="font-weight-black text-h6">{{
                t('bookreader.settings.display')
              }}
            </v-list-subheader>

            <v-list-item>
              <v-row>
                <v-col align-self="center">
                  <span>{{ t('epubreader.settings.viewing_theme') }}</span>
                </v-col>
                <v-col align-self="center">
                  <v-btn
                      v-for="(a, i) in appearances"
                      :key="i"
                      :value="a.value"
                      :color="a.color"
                      :class="a.class"
                      class="mx-1"
                      @click="appearance = a.value"
                  >
                    <v-icon :icon="mdiCheck" v-if="appearance === a.value"/>
                  </v-btn>
                </v-col>
              </v-row>
            </v-list-item>

            <v-list-item v-if="!fixedLayout">
              <v-row>
                <v-col align-self="center">
                  <span>{{ t('epubreader.settings.layout') }}</span>
                </v-col>

                <v-col align-self="center">
                  <v-btn-toggle mandatory v-model="verticalScroll">
                    <v-btn :value="true">{{ t('epubreader.settings.layout_scroll') }}</v-btn>
                    <v-btn :value="false">{{ t('epubreader.settings.layout_paginated') }}</v-btn>
                  </v-btn-toggle>
                </v-col>
              </v-row>

            </v-list-item>

            <v-list-item v-if="!verticalScroll">
              <v-row>
                <v-col align-self="center">
                  <span>{{ t('epubreader.settings.column_count') }}</span>
                </v-col>

                <v-col align-self="center">
                  <v-btn-toggle mandatory v-model="columnCount">
                    <v-btn v-for="(c, i) in columnCounts" :key="i" :value="c.value">{{ c.text }}</v-btn>
                  </v-btn-toggle>
                </v-col>
              </v-row>
            </v-list-item>

            <v-list-item class="d-flex justify-center">
              <v-btn depressed @click="fontSize-=10">
                <v-icon :icon="mdiFormatTitle" small/>
              </v-btn>
              <span class="caption mx-8" style="width: 2rem">{{ fontSize }}%</span>
              <v-btn depressed @click="fontSize+=10">
                <v-icon :icon="mdiFormatTitle"/>
              </v-btn>
            </v-list-item>

            <v-list-item class="d-flex justify-center">
              <v-btn depressed @click="lineHeight-=.1">
                <v-icon>
                  <IconFormatLineSpacingDown/>
                </v-icon>
              </v-btn>
              <span class="caption mx-8" style="width: 2rem">{{ Math.round(lineHeight * 100) }}%</span>
              <v-btn depressed @click="lineHeight+=.1">
                <v-icon :icon="mdiFormatLetterSpacing"/>
              </v-btn>
            </v-list-item>

            <v-list-item>
              <v-slider
                  v-model="pageMargins"
                  :label="t('epubreader.settings.page_margins')"
                  min="0.5"
                  max="4"
                  step="0.25"
                  show-ticks="always"
                  tick-size="3"
              />
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </v-bottom-sheet>

    <v-snackbar
        v-model="notification.enabled"
        centered
        :timeout="notification.timeout"
    >
      <p class="text-h6 text-center ma-0">
        {{ notification.message }}
      </p>
    </v-snackbar>

    <shortcut-help-dialog
        v-model="showHelp"
        :shortcuts="shortcutsHelp"
    />
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import {computed, ComputedRef, onBeforeUnmount, onMounted, reactive, Ref, ref} from 'vue'
import D2Reader, {Locator, ReadingPosition} from '@d-i-t-a/reader'
import {Locations} from "@d-i-t-a/reader/dist/types/model/Locator";
import {BookDto} from '@/types/komga-books'
import {getBookTitleCompact} from '@/functions/book-title'
import {SeriesDto} from '@/types/komga-series'
import {TocEntry} from '@/types/epub'
import TocList from '@/components/TocList.vue'
import {flattenToc} from '@/functions/toc'
import ShortcutHelpDialog from '@/components/ShortcutHelpDialog.vue'
import SettingsSelect from '@/components/SettingsSelect.vue'
import {createR2Progression, r2ProgressionToReadingPosition} from '@/functions/readium'
import {useDisplay, useRtl} from "vuetify";
import {EpubReaderSettings} from "@/types/epub-reader-settings";
import {externalFunctions} from "@/main";
import IconFormatLineSpacingDown from "@/components/IconFormatLineSpacingDown.vue";
import {
  mdiArrowLeft,
  mdiCheck,
  mdiChevronDown,
  mdiChevronLeft,
  mdiChevronRight,
  mdiChevronUp,
  mdiClose,
  mdiCog,
  mdiEiffelTower,
  mdiFormatLetterSpacing,
  mdiFormatTitle, mdiFullscreen,
  mdiHelpCircle,
  mdiNumeric,
  mdiRedo,
  mdiTableOfContents,
  mdiUndo
} from '@mdi/js'

const bookId = ref('')
const {t} = useI18n()
const {width, height, smAndUp} = useDisplay()
const {isRtl} = useRtl()
isRtl.value = false

const d2Reader = ref({} as D2Reader)
const book = ref(undefined as unknown as BookDto)
const series = ref(undefined as unknown as SeriesDto)
const siblingPrevious: Ref<undefined | BookDto> = ref(undefined)
const siblingNext: Ref<undefined | BookDto> = ref(undefined)
const incognito = ref(false)
const showSettings = ref(false)
const showToolbars = ref(false)
const showToc = ref(false)
const showHelp = ref(false)
const tab: Ref<string> = ref("hasToc")
const readingDirs = ref(
    [
      {
        text: t('enums.epubreader.reading_direction.auto').toString(),
        value: 'auto',
      },
      {
        text: t('enums.epubreader.reading_direction.ltr').toString(),
        value: 'ltr',
      },
      {
        text: t('enums.epubreader.reading_direction.rtl').toString(),
        value: 'rtl',
      },
    ]
)
const appearances = ref(
    [
      {
        text: t('enums.epubreader.appearances.day').toString(),
        value: 'readium-default-on',
        color: 'white',
        class: 'black--text',
      },
      {
        text: t('enums.epubreader.appearances.sepia').toString(),
        value: 'readium-sepia-on',
        color: '#faf4e8',
        class: 'black--text',
      },
      {
        text: t('enums.epubreader.appearances.night').toString(),
        value: 'readium-night-on',
        color: 'black',
        class: 'white--text',
      },
    ]
)

const columnCounts = ref(
    [
      {text: t('enums.epubreader.column_count.auto').toString(), value: 'auto'},
      {text: t('enums.epubreader.column_count.one').toString(), value: '1'},
      {text: t('enums.epubreader.column_count.two').toString(), value: '2'},
    ]
)

const settings = reactive(
    {
      // R2D2BC
      appearance: 'readium-default-on',
      pageMargins: 1,
      lineHeight: 1,
      fontSize: 100,
      verticalScroll: false,
      columnCount: 'auto',
      fixedLayoutMargin: 0,
      fixedLayoutShadow: false,
      direction: 'auto',
      // Epub Reader
      alwaysFullscreen: false,
      navigationClick: true,
      navigationButtons: true,
    } as EpubReaderSettings
)
const navigationOptions = ref(
    [
      {title: t('epubreader.settings.navigation_options.buttons'), value: 'button'},
      {title: t('epubreader.settings.navigation_options.click'), value: 'click'},
      {title: t('epubreader.settings.navigation_options.both'), value: 'buttonclick'},
    ]
)

// const fullscreenIsAvailable = await externalFunctions.isFullscreenAvailable()
const fullscreenIsAvailable = true

const tocs = reactive({
  toc: undefined as unknown as TocEntry[],
  landmarks: undefined as unknown as TocEntry[],
  pageList: undefined as unknown as TocEntry[],
})
const currentLocation: Ref<undefined | Locator> = ref(undefined)
const historyCanGoBack = ref(false)

const historyCanGoForward = ref(false)
const notification = reactive({
  enabled: false,
  message: '',
  timeout: 4000,
})
const clickTimer: Ref<number | undefined> = ref(undefined)
// const forceUpdate = ref(false)
const progressionTitle: Ref<undefined | string> = ref(undefined)
const progressionPage: Ref<undefined | number> = ref(undefined)
const progressionPageCount: Ref<undefined | number> = ref(undefined)
const effectiveDirection = ref('ltr')
const fixedLayout = ref(false)


const effectiveRtl = computed(() => {
  return effectiveDirection.value === 'rtl'
})

const shortcutsD2Reader = {
  'CTRL+Space': {
    description: 'epubreader.shortcuts.previous',
    display: 'CTRL + SPACE',
  },
  'Space': {
    description: 'epubreader.shortcuts.next',
    display: 'SPACE',
  },
}

const shortcutsD2ReaderLTR = {
  'ArrowLeft': {
    description: 'epubreader.shortcuts.previous',
    display: '←',
  },
  'ArrowRight': {
    description: 'epubreader.shortcuts.next',
    display: '→',
  },
}

const shortcutsD2ReaderRTL = {
  'ArrowRight': {
    description: 'epubreader.shortcuts.previous',
    display: '→',
  },
  'ArrowLeft': {
    description: 'epubreader.shortcuts.next',
    display: '←',
  },
}

const epubShortcutsSettingsScroll = {
  'v': {
    description: 'epubreader.shortcuts.scroll',
    display: 'v',
    action: () => changeLayout(true)
  },
}

const epubShortcutsSettings = {
  'p': {
    description: 'epubreader.shortcuts.cycle_pagination',
    display: 'p',
    action: cyclePagination
  },
  'a': {
    description: 'epubreader.shortcuts.cycle_viewing_theme',
    display: 'a',
    action: cycleViewingTheme
  },
  '+': {
    description: 'epubreader.shortcuts.font_size_increase',
    display: '+',
    action: () => changeFontSize(true)
  },
  '-': {
    description: 'epubreader.shortcuts.font_size_decrease',
    display: '-',
    action: () => changeFontSize(false)
  },
  'f': {
    description: 'bookreader.shortcuts.fullscreen',
    display: 'f',
    action: switchFullscreen
  },
}

const epubShortcutsMenus = {
  'm': {
    description: 'bookreader.shortcuts.show_hide_toolbars',
    display: 'm',
    action: toggleToolbars
  },
  's': {
    description: 'bookreader.shortcuts.show_hide_settings',
    display: 's',
    action: toggleSettings
  },
  't': {
    description: 'epubreader.shortcuts.show_hide_toc',
    display: 't',
    action: toggleTableOfContents
  },
  'h': {
    description: 'bookreader.shortcuts.show_hide_help',
    display: 'h',
    action: toggleHelp
  },
  'Escape': {
    description: 'bookreader.shortcuts.close',
    display: 'Escape',
    action: closeDialog
  },
}


const shortcuts: ComputedRef<any> = computed(() => {
  if (fixedLayout.value) {
    return shortcuts
  }
  return Object.assign({}, shortcuts, epubShortcutsSettingsScroll, epubShortcutsSettings, epubShortcutsMenus)
})

const shortcutsHelp = computed(() => {
  let nav = []
  if (effectiveDirection.value === 'rtl') nav.push(
      Object.entries(shortcutsD2ReaderRTL)
          .map(([, value],) => value)
  )
  else nav.push(
      Object.entries(shortcutsD2ReaderLTR)
          .map(([, value],) => value)
  )
  nav.push(
      Object.entries(shortcutsD2Reader).map(([, value],) => value)
  )

  return {
    [t('bookreader.shortcuts.reader_navigation')]: nav.flat(),
    [t('bookreader.shortcuts.settings')]: Object.entries(epubShortcutsSettings).map(([, value],) => value),
    [t('bookreader.shortcuts.menus')]: Object.entries(epubShortcutsMenus).map(([, value],) => value),
  }
})

const progressionTotalPercentage = computed(() => {
  const p = currentLocation.value?.locations?.totalProgression
  if (p) return `${Math.round(p * 100)}%`
  return ''
})

const tableOfContents = computed(() => {
  if (tocs.toc) return flattenToc(tocs.toc, 1, 0, currentLocation.value?.href)
  return []
})

const landmarks = computed(() => {
  if (tocs.landmarks) return flattenToc(tocs.landmarks, 1, 0, currentLocation.value?.href)
  return []
})
const pageList = computed(() => {
  if (tocs.pageList) return flattenToc(tocs.pageList, 1, 0, currentLocation.value?.href)
  return []
})
const hasToc = computed(() => {
  return tocs.toc?.length > 0
})
const hasLandmarks = computed(() => {
  return tocs.landmarks?.length > 0
})
const hasPageList = computed(() => {
  return tocs.pageList?.length > 0
})
const bookTitle = computed(() => {
  if (!!book.value && !!series.value)
    return getBookTitleCompact(book.value.metadata.title, series.value.metadata.title)
  return book.value?.metadata?.title
})
const appearance = computed({
  get(): string {
    return settings.appearance
  },
  set(color: string): void {
    if (appearances.value.map(x => x.value).includes(color)) {
      settings.appearance = color
      d2Reader.value.applyUserSettings({appearance: color})
      externalFunctions.saveReaderSettings(settings)
    }
  },
})
const verticalScroll = computed({
  get(): boolean {
    return settings.verticalScroll
  },
  set(value: string | boolean): void {
    settings.verticalScroll = value as any
    d2Reader.value.applyUserSettings({verticalScroll: value as any})
    externalFunctions.saveReaderSettings(settings)
  },
})
const columnCount = computed({
  get(): boolean {
    return settings.columnCount as any
  },
  set(value: string): void {
    if (columnCounts.value.map(x => x.value).includes(value)) {
      settings.columnCount = value
      d2Reader.value.applyUserSettings({columnCount: value as any})
      externalFunctions.saveReaderSettings(settings)
    }
  },
})
const readingDirection = computed({
  get(): boolean {
    let direction = settings.direction
    return direction as any
  },
  set(value: string): void {
    if (readingDirs.value.map(x => x.value).includes(value)) {
      settings.direction = value
      d2Reader.value.applyUserSettings({direction: value as any})
      externalFunctions.saveReaderSettings(settings)
    }
  },
})
const pageMargins = computed({
  get(): number {
    return settings.pageMargins
  },
  set(value: number): void {
    settings.pageMargins = value
    d2Reader.value.applyUserSettings({pageMargins: value})
    externalFunctions.saveReaderSettings(settings)
  },
})
const lineHeight = computed({
  get(): number {
    return settings.lineHeight
  },
  set(value: number): void {
    settings.lineHeight = value
    d2Reader.value.applyUserSettings({lineHeight: value})
    externalFunctions.saveReaderSettings(settings)
  },
})
const fontSize = computed({
  get(): number {
    return settings.fontSize
  },
  set(value: number): void {
    settings.fontSize = value
    d2Reader.value.applyUserSettings({fontSize: value})
    externalFunctions.saveReaderSettings(settings)
  },
})
const navigationMode = computed({
  get(): string {
    let r = settings.navigationButtons ? 'button' : ''
    if (settings.navigationClick) r += 'click'
    return r
  },
  set(value: string): void {
    settings.navigationButtons = value.includes('button')
    settings.navigationClick = value.includes('click')
    externalFunctions.saveReaderSettings(settings)
  },
})


onBeforeUnmount(() => {
  d2Reader.value.stop()
})

onMounted(async () => {
  let bookId = await externalFunctions.getInitialBookId()
  let externalSettings = await externalFunctions.getReaderSettings()
  Object.assign(settings, externalSettings)
  await setupState(bookId)
})

function previousBook() {
  if (siblingPrevious.value == undefined) {
    closeBook()
  } else {
    d2Reader.value.stop()
    setupState(siblingPrevious.value.id)
  }
}

function nextBook() {
  if (siblingNext.value == undefined) {
    closeBook()
  } else {
    d2Reader.value.stop()
    setupState(siblingNext.value.id)
  }
}

async function switchFullscreen() {
  await externalFunctions.toggleFullscreen()
}

function toggleToolbars() {
  showToolbars.value = !showToolbars.value
}

function toggleSettings() {
  showSettings.value = !showSettings.value
}

function toggleTableOfContents() {
  showToc.value = !showToc.value
}

function toggleHelp() {
  showHelp.value = !showHelp.value
}

function keyPressed(e: KeyboardEvent) {
  shortcuts.value[e.key]?.action()
}

function clickThrough(e: MouseEvent) {
  let x = e.x
  let y = e.y
  if ((e.target as Node)?.ownerDocument != document) {
    const iframe = e.view?.frameElement
    if (iframe == null) return
    const iframeWrapper = iframe.parentElement?.parentElement
    if (iframeWrapper == null) return
    const scaleComputed = iframeWrapper.getBoundingClientRect().width / iframeWrapper.offsetWidth
    const rect = iframe.getBoundingClientRect()
    x = rect.left + (e.x * scaleComputed)
    y = rect.top + (e.y * scaleComputed)
  }

  if (e.detail === 1) {
    clickTimer.value = setTimeout(() => {
      singleClick(x, y)
    }, 200)
  }
  if (e.detail === 2) {
    clearTimeout(clickTimer.value)
  }
}

function singleClick(x: number, y: number) {
  console.log("single click")
  if (verticalScroll.value) {
    if (settings.navigationClick) {
      if (y < height.value / 4) return d2Reader.value.previousPage()
      if (y > height.value * .75) return d2Reader.value.nextPage()
    }
  } else {
    if (settings.navigationClick) {
      if (x < width.value / 4) return effectiveRtl.value ? d2Reader.value.nextPage() : d2Reader.value.previousPage()
      if (x > width.value * .75) return effectiveRtl.value ? d2Reader.value.previousPage() : d2Reader.value.nextPage()
    }
  }
  toggleToolbars()
}

async function setupState(currentBookId: string) {
  bookId.value = currentBookId
  book.value = await externalFunctions.bookGet(currentBookId)
  series.value = await externalFunctions.getOneSeries(book.value.seriesId)

  const progression = await externalFunctions.bookGetProgression(currentBookId)
  const serverUrl = await externalFunctions.getServerUrl()
  let initialLocation: ReadingPosition | undefined = undefined
  if (progression != undefined) {
    initialLocation = r2ProgressionToReadingPosition(currentBookId, progression)
  }

// parse query params to get context and contextId
// if (this.$route.query.contextId && this.$route.query.context
//   && Object.values(ContextOrigin).includes(this.$route.query.context as ContextOrigin)) {
//   this.context = {
//     origin: this.$route.query.context as ContextOrigin,
//     id: this.$route.query.contextId as string,
//   }
//   this.book.context = this.context
//   //   if (this?.context.origin === ContextOrigin.READLIST) {
//   //     this.contextName = (await (this.$komgaReadLists.getOneReadList(this.context.id))).name
//   //     document.title = `Komga - ${this.contextName} - ${this.book.metadata.title}`
//   //   }
// } else {
  document.title = `Komga - ${getBookTitleCompact(book.value.metadata.title, series.value.metadata.title)}`
// }

  incognito.value = await externalFunctions.isIncognito()
  d2Reader.value = await D2Reader.load({
    url: new URL(`${serverUrl}/api/v1/books/${currentBookId}/manifest`),

    userSettings: settings,
    storageType: 'memory',
    lastReadingPosition: initialLocation,
    injectables: [
      {
        type: 'style',
        url: new URL('../styles/readium/ReadiumCSS-before.css', import.meta.url).toString(),
        r2before: true,
      },
      {
        type: 'style',
        url: new URL('../styles/readium/ReadiumCSS-default.css', import.meta.url).toString(),
        r2default: true,
      },
      {
        type: 'style',
        url: new URL('../styles/readium/ReadiumCSS-after.css', import.meta.url).toString(),
        r2after: true,
      },
      {type: 'style', url: new URL('../styles/r2d2bc/popup.css', import.meta.url).toString()},
      {type: 'style', url: new URL('../styles/r2d2bc/popover.css', import.meta.url).toString()},
      {type: 'style', url: new URL('../styles/r2d2bc/style.css', import.meta.url).toString()},
    ],
    requestConfig: {
      credentials: 'include',
    },
    attributes: {
      margin: 0, // subtract this from the iframe height, when setting the iframe minimum height
      navHeight: 10, // used for positioning the toolbox
      iframePaddingTop: 20, // top padding inside iframe
      bottomInfoHeight: 35, // #reader-info-bottom height
    },
    rights: {
      enableBookmarks: false,
      enableAnnotations: false,
      enableTTS: false,
      enableSearch: false,
      enableTimeline: false,
      enableDefinitions: false,
      enableContentProtection: false,
      enableMediaOverlays: false,
      enablePageBreaks: false,
      autoGeneratePositions: false,
      enableLineFocus: false,
      customKeyboardEvents: false,
      enableHistory: true,
      enableCitations: false,
      enableConsumption: false,
    },
    services: {
      positions: new URL(`${serverUrl}/api/v1/books/${currentBookId}/positions`),
    },
    api: {
      getContent: externalFunctions.d2ReaderGetContent,
      getContentBytesLength: externalFunctions.d2ReaderGetContentBytesLength,
      updateCurrentLocation: updateCurrentLocation,
      keydownFallthrough: keyPressed,
      clickThrough: clickThrough,
      positionInfo: updatePositionInfo,
      chapterInfo: updateChapterInfo,
      direction: updateDirection,
    },
  })

  fixedLayout.value = d2Reader.value.publicationLayout === 'fixed'

  tocs.toc = d2Reader.value.tableOfContents
  tocs.landmarks = d2Reader.value.landmarks
  tocs.pageList = d2Reader.value.pageList

  try {
    // if (this?.context.origin === ContextOrigin.READLIST) {
    //   this.siblingNext = await this.$komgaReadLists.getBookSiblingNext(this.context.id, bookId)
    // } else {
    siblingNext.value = await externalFunctions.bookGetBookSiblingNext(currentBookId)
    // }
  } catch (e) {
  }
  try {
    // if (this?.context.origin === ContextOrigin.READLIST) {
    //   this.siblingPrevious = await this.$komgaReadLists.getBookSiblingPrevious(this.context.id, bookId)
    // } else {
    siblingPrevious.value = await externalFunctions.bookGetBookSiblingPrevious(currentBookId)
    // }
  } catch (e) {
  }
}

function historyBack() {
  d2Reader.value.historyBack()
}

function historyForward() {
  d2Reader.value.historyForward()
}

function updateCurrentLocation(location: Locator): Promise<Locator> {
  // handle history
  historyCanGoBack.value = (d2Reader.value.historyCurrentIndex ?? 0) > 0
  historyCanGoForward.value = (d2Reader.value.historyCurrentIndex ?? 0) < (d2Reader.value.history?.length ?? 0) - 1

  markProgress(location)
  currentLocation.value = location
  return new Promise(function (resolve, _) {
    resolve(location)
  })
}

function updatePositionInfo(location: Locator) {
  progressionPage.value = location.displayInfo?.resourceScreenIndex
  progressionPageCount.value = location.displayInfo?.resourceScreenCount
}

function updateChapterInfo(title?: string) {
  progressionTitle.value = title
}

function updateDirection(dir: string) {
  effectiveDirection.value = dir
}

function appearanceClass(suffix?: string): string {
  let c = appearance.value.replace('readium-', '').replace('-on', '').replace('default', 'day')
  if (suffix) c += `-${suffix}`
  return c
}

function goToEntry(tocEntry: TocEntry) {
  if (tocEntry.href !== undefined) {
    const url = new URL(tocEntry.href)
    let locations = {
      progression: 0,
    } as Locations
    let href = tocEntry.href
    if (url.hash) {
      locations = {
        fragment: url.hash.slice(1),
      }
      href = tocEntry.href.substring(0, tocEntry.href.indexOf('#'))
    }
    let locator = {
      href: href,
      locations: locations,
    }
    d2Reader.value.goTo(locator)
    showToc.value = false
  }
}

function closeDialog() {
  if (showToc.value) {
    showToc.value = false
    return
  }
  if (showSettings.value) {
    showSettings.value = false
    return
  }
  if (showToolbars.value) {
    showToolbars.value = false
    return
  }
  closeBook()
}

async function closeBook() {
  await externalFunctions.closeBook()
}

function cycleViewingTheme() {
  const i = (appearances.value.map(x => x.value).indexOf(settings.appearance) + 1) % appearances.value.length
  const newValue = appearances.value[i]
  appearance.value = newValue.value
  const text = t(newValue.text)
  sendNotification(`${t('epubreader.settings.viewing_theme')}: ${text}`)
}

function changeLayout(scroll: boolean) {
  verticalScroll.value = scroll
  const text = scroll ? t('epubreader.settings.layout_scroll') : t('epubreader.settings.layout_paginated')
  sendNotification(`${t('epubreader.settings.layout')}: ${text}`)
}

function cyclePagination() {
  if (verticalScroll.value) {
    columnCount.value = 'auto'
    changeLayout(false)
  } else {
    const i = (columnCounts.value.map(x => x.value).indexOf(settings.columnCount) + 1) % columnCounts.value.length
    const newValue = columnCounts.value[i]
    columnCount.value = newValue.value
    const text = t(newValue.text)
    sendNotification(`${t('epubreader.settings.column_count')}: ${text}`)
  }
}

function changeFontSize(increase: boolean) {
  fontSize.value += increase ? 10 : -10
}

function sendNotification(message: string, timeout: number = 4000) {
  notification.timeout = timeout
  notification.message = message
  notification.enabled = true
}

function markProgress(location: Locator) {
  externalFunctions.bookUpdateProgression(bookId.value, createR2Progression(location))
  // debounce(() => {
  //   if (!incognito.value) {
  //     externalFunctions.bookUpdateProgression(bookId.value, createR2Progression(location))
  //   }
  // }, 500)
}

</script>
<style src="@d-i-t-a/reader/dist/reader.css"/>
<style scoped>
.settings {
  z-index: 2;
}

.full-height {
  height: 100%;
}

.full-width {
  width: 100%;
}

.sepia-bg {
  background-color: #faf4e8;
}

.sepia {
  color: #5B5852;
}

.day-bg {
  background-color: #fff;
}

.day {
  color: #5B5852;
}

.night-bg {
  background-color: #000000;
}

.night {
  color: #DADADA;
}

.scrolltab {
  overflow-y: scroll;
}

.scrolltab-content {
  max-height: calc(100vh - 48px);
}

.hidden {
  display: none !important;
}
</style>
