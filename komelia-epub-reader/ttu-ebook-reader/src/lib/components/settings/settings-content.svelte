<script lang="ts">
  import { faPlus, faSpinner} from '@fortawesome/free-solid-svg-icons';
  import ButtonToggleGroup from '$lib/components/button-toggle-group/button-toggle-group.svelte';
  import {optionsForToggle, type ToggleOption} from '$lib/components/button-toggle-group/toggle-option';
  import Ripple from '$lib/components/ripple.svelte';
  import SettingsCustomTheme from '$lib/components/settings/settings-custom-theme.svelte';
  import SettingsDimensionPopover from '$lib/components/settings/settings-dimension-popover.svelte';
  import SettingsFontSelector from '$lib/components/settings/settings-font-selector.svelte';
  import SettingsItemGroup from '$lib/components/settings/settings-item-group.svelte';
  import SettingsUserFontDialog from '$lib/components/settings/settings-user-font-dialog.svelte';
  import {inputClasses} from '$lib/css-classes';
  import {BlurMode} from '$lib/data/blur-mode';
  import {dialogManager} from '$lib/data/dialog-manager';
  import {FuriganaStyle} from '$lib/data/furigana-style';
  import {
    autoBookmark$,
    autoBookmarkTime$,
    autoPositionOnResize$,
    avoidPageBreak$,
    confirmClose$,
    customReadingPointEnabled$,
    customThemes$,
    disableWheelNavigation$,
    enableReaderWakeLock$,
    firstDimensionMargin$,
    fontSize$,
    furiganaStyle$,
    hideFurigana$,
    hideSpoilerImage$,
    hideSpoilerImageMode$,
    horizontalCustomReadingPosition$,
    lineHeight$,
    manualBookmark$,
    pageColumns$,
    sansFontFamily$,
    secondDimensionMaxValue$,
    selectionToBookmarkEnabled$,
    serifFontFamily$,
    showCharacterCounter$,
    swipeThreshold$,
    theme$,
    verticalCustomReadingPosition$,
    viewMode$,
    writingMode$
  } from '$lib/data/store';
  import {availableThemes as availableThemesMap} from '$lib/data/theme-option';
  import {ViewMode} from '$lib/data/view-mode';
  import type {WritingMode} from '$lib/data/writing-mode';
  import {dummyFn} from '$lib/functions/utils';
  import {defaultSansFont, defaultSerifFont} from "$lib/data/fonts";
  import Fa from "svelte-fa";
  import {faFolderOpen} from "@fortawesome/free-regular-svg-icons";

  let optionsForTheme = $derived.by(() => {
    let availableThemes = [...Array.from(availableThemesMap.entries()), ...Object.entries($customThemes$)]
      .map(([theme, option]) => ({theme, option}))

    return availableThemes.map(({theme, option}) => ({
      id: theme,
      text: 'ぁあ',
      style: {
        color: option.fontColor,
        'background-color': option.backgroundColor
      },
      thickBorders: true,
      showIcons: true
    }))
  })

  const optionsForFuriganaStyle: ToggleOption<FuriganaStyle>[] = [
    {
      id: FuriganaStyle.Hide,
      text: 'Hide'
    },
    {
      id: FuriganaStyle.Partial,
      text: 'Partial'
    },
    {
      id: FuriganaStyle.Toggle,
      text: 'Toggle'
    },
    {
      id: FuriganaStyle.Full,
      text: 'Full'
    }
  ];

  const optionsForWritingMode: ToggleOption<WritingMode>[] = [
    {
      id: 'horizontal-tb',
      text: 'Horizontal'
    },
    {
      id: 'vertical-rl',
      text: 'Vertical'
    }
  ];

  const optionsForViewMode: ToggleOption<ViewMode>[] = [
    {
      id: ViewMode.Continuous,
      text: 'Continuous'
    },
    {
      id: ViewMode.Paginated,
      text: 'Paginated'
    }
  ];

  const optionsForBlurMode: ToggleOption<BlurMode>[] = [
    {
      id: BlurMode.ALL,
      text: 'All'
    },
    {
      id: BlurMode.AFTER_TOC,
      text: 'After ToC'
    }
  ];

  let showSpinner = false;
  let furiganaStyleTooltip = $state('');

  let autoBookmarkTooltip = $derived(`If enabled sets a bookmark after ${$autoBookmarkTime$} seconds without scrolling/page change`);
  let wakeLockSupported = $derived('wakeLock' in navigator);
  let verticalMode = $derived($writingMode$ === 'vertical-rl');
  let avoidPageBreakTooltip = $derived(
    $avoidPageBreak$
      ? 'Avoids breaking words/sentences into different pages'
      : 'Allow words/sentences to break into different pages'
  );
  $effect(() => {
    switch ($furiganaStyle$) {
      case FuriganaStyle.Hide:
        furiganaStyleTooltip = 'Always hidden';
        break;
      case FuriganaStyle.Toggle:
        furiganaStyleTooltip = 'Hidden by default, can be toggled on click';
        break;
      case FuriganaStyle.Full:
        furiganaStyleTooltip = 'Hidden by default, show on hover or click';
        break;
      default:
        furiganaStyleTooltip = 'Display furigana as grayed out text';
        break;
    }
  })
</script>

<div class="grid grid-cols-1 items-center sm:grid-cols-2 sm:gap-6 lg:md:gap-8 lg:grid-cols-3">
  <div class="lg:col-span-2">
    <SettingsItemGroup title="Theme">
      <ButtonToggleGroup
          options={optionsForTheme}
          bind:selectedOptionId={$theme$}
          on:edit={({ detail }) =>
                dialogManager.dialogs$.next([
                  {
                    component: SettingsCustomTheme,
                    props: { selectedTheme: detail, existingThemes: optionsForTheme }
                  }
                ])}
          on:delete={({ detail }) => {
            $theme$ = optionsForTheme[optionsForTheme.length - 2]?.id || 'light-theme';
            delete $customThemes$[detail];
            $customThemes$ = { ...$customThemes$ };
          }}
      >
        <button
            class="m-1 rounded-md border-2 border-gray-400 p-2 text-lg"
            onclick={() =>
                  dialogManager.dialogs$.next([
                    {
                      component: SettingsCustomTheme,
                      props: { existingThemes: optionsForTheme }
                    }
                  ])}
        >
          <Fa icon={faPlus} class="mx-2"/>
          <Ripple/>
        </button>
      </ButtonToggleGroup>
    </SettingsItemGroup>
  </div>
  <div class="h-full">
    <SettingsItemGroup title="View mode">
      <ButtonToggleGroup options={optionsForViewMode} bind:selectedOptionId={$viewMode$}/>
    </SettingsItemGroup>
  </div>
  <SettingsItemGroup title="Serif Font family">
    <div slot="header" class="flex items-center mx-2">
      <div
          tabindex="0"
          role="button"
          title="Open Custom Font Dialog"
          onclick={() =>
              dialogManager.dialogs$.next([
                {
                  component: SettingsUserFontDialog,
                  props: {
                      currentFont: serifFontFamily$,
                      defaultFont: defaultSerifFont
                  }
                }
              ])}
          onkeyup={dummyFn}
      >
        <Fa icon={faFolderOpen}/>
      </div>
    </div>
    <SettingsFontSelector bind:fontValue={$serifFontFamily$} defaultFont={defaultSerifFont} family="Serif"/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Sans Font family">
    <div slot="header" class="flex items-center mx-2">
      <div
          tabindex="0"
          role="button"
          title="Open Custom Font Dialog"
          onclick={() =>
              dialogManager.dialogs$.next([
                {
                  component: SettingsUserFontDialog,
                  props: {
                      currentFont: sansFontFamily$,
                      defaultFont: defaultSansFont
                  }
                }
              ])}
          onkeyup={dummyFn}
      >
        <Fa icon={faFolderOpen}/>
      </div>
    </div>
    <SettingsFontSelector bind:fontValue={$sansFontFamily$} defaultFont={defaultSansFont} family="Sans-Serif"/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Font size">
    <input type="number"
           class={inputClasses}
           step="1"
           min="1"
           bind:value={$fontSize$}/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Line Height">
    <input
        type="number"
        class={inputClasses}
        step="0.05"
        min="1"
        bind:value={$lineHeight$}
        onchange={() => {
              if (!$lineHeight$ || $lineHeight$ < 1) {
                $lineHeight$ = 1.65;
              }}
            }
    />
  </SettingsItemGroup>
  <SettingsItemGroup
      title={verticalMode ? 'Reader Left/right margin' : 'Reader Top/bottom margin'}
  >
    <SettingsDimensionPopover
        slot="header"
        isFirstDimension
        isVertical={verticalMode}
        bind:dimensionValue={$firstDimensionMargin$}
    />
    <input
        type="number"
        class={inputClasses}
        step="1"
        min="0"
        bind:value={$firstDimensionMargin$}
    />
  </SettingsItemGroup>
  <SettingsItemGroup title={verticalMode ? 'Reader Max height' : 'Reader Max width'}>
    <SettingsDimensionPopover
        slot="header"
        isVertical={verticalMode}
        bind:dimensionValue={$secondDimensionMaxValue$}
    />
    <input
        type="number"
        class={inputClasses}
        step="1"
        min="0"
        bind:value={$secondDimensionMaxValue$}
    />
  </SettingsItemGroup>
  <SettingsItemGroup
      title="Swipe Threshold"
      tooltip={'Distance which you need to swipe in order trigger a navigation'}
  >
    <input
        type="number"
        step="1"
        min="10"
        class={inputClasses}
        bind:value={$swipeThreshold$}
        onblur={() => {
              if ($swipeThreshold$ < 10 || typeof $swipeThreshold$ !== 'number') {
                $swipeThreshold$ = 10;
              }
            }}
    />
  </SettingsItemGroup>
  {#if $autoBookmark$}
    <SettingsItemGroup title="Auto Bookmark Time" tooltip={'Time in s for Auto Bookmark'}>
      <input
          type="number"
          step="1"
          min="1"
          class={inputClasses}
          bind:value={$autoBookmarkTime$}
          onblur={() => {
                if ($autoBookmarkTime$ < 1 || typeof $autoBookmarkTime$ !== 'number') {
                  $autoBookmarkTime$ = 3;
                }
              }}
      />
    </SettingsItemGroup>
  {/if}
  <SettingsItemGroup title="Writing mode">
    <ButtonToggleGroup options={optionsForWritingMode} bind:selectedOptionId={$writingMode$}/>
  </SettingsItemGroup>
  {#if wakeLockSupported}
    <SettingsItemGroup
        title="Enable Screen Lock"
        tooltip={'When enabled the reader site attempts to request a WakeLock that prevents device screens from dimming or locking'}
    >
      <ButtonToggleGroup
          options={optionsForToggle}
          bind:selectedOptionId={$enableReaderWakeLock$}
      />
    </SettingsItemGroup>
  {/if}
  <SettingsItemGroup title="Show Character Counter">
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$showCharacterCounter$}/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Disable Wheel Navigation">
    <ButtonToggleGroup
        options={optionsForToggle}
        bind:selectedOptionId={$disableWheelNavigation$}
    />
  </SettingsItemGroup>
  <SettingsItemGroup
      title="Close Confirmation"
      tooltip={`When enabled asks for confirmation on closing/reloading a reader tab and unsaved changes were detected`}
  >
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$confirmClose$}/>
  </SettingsItemGroup>
  <SettingsItemGroup
      title="Manual Bookmark"
      tooltip={'If enabled current position will not be bookmarked when leaving the reader via menu elements'}
  >
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$manualBookmark$}/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Auto Bookmark" tooltip={autoBookmarkTooltip}>
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$autoBookmark$}/>
  </SettingsItemGroup>
  <SettingsItemGroup title="Blur image">
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$hideSpoilerImage$}/>
  </SettingsItemGroup>
  {#if $hideSpoilerImage$}
    <SettingsItemGroup
        title="Blur Mode"
        tooltip="Determines if all or only images after the table of contents will be blurred"
    >
      <ButtonToggleGroup options={optionsForBlurMode} bind:selectedOptionId={$hideSpoilerImageMode$}/>
    </SettingsItemGroup>
  {/if}
  <SettingsItemGroup title="Hide furigana">
    <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$hideFurigana$}/>
  </SettingsItemGroup>
  {#if $hideFurigana$}
    <SettingsItemGroup title="Hide furigana style" tooltip={furiganaStyleTooltip}>
      <ButtonToggleGroup
          options={optionsForFuriganaStyle}
          bind:selectedOptionId={$furiganaStyle$}
      />
    </SettingsItemGroup>
  {/if}
  {#if $viewMode$ === ViewMode.Continuous}
    <SettingsItemGroup
        title="Custom Reading Point"
        tooltip={'Allows to set a persistent custom point in the reader from which the current progress and bookmark is calculated when enabled'}
    >
      <div class="flex items-center">
        <ButtonToggleGroup
            options={optionsForToggle}
            bind:selectedOptionId={$customReadingPointEnabled$}
        />
        {#if $customReadingPointEnabled$}
          <div
              tabindex="0"
              role="button"
              class="ml-4 hover:underline"
              onclick={() => {
                    verticalCustomReadingPosition$.next(100);
                    horizontalCustomReadingPosition$.next(0);
                  }}
              onkeyup={dummyFn}
          >
            Reset Points
          </div>
        {/if}
      </div>
    </SettingsItemGroup>
    <SettingsItemGroup title="Auto position on resize">
      <ButtonToggleGroup
          options={optionsForToggle}
          bind:selectedOptionId={$autoPositionOnResize$}
      />
    </SettingsItemGroup>
  {:else}
    <SettingsItemGroup title="Avoid Page Break" tooltip={avoidPageBreakTooltip}>
      <ButtonToggleGroup options={optionsForToggle} bind:selectedOptionId={$avoidPageBreak$}/>
    </SettingsItemGroup>
    <SettingsItemGroup
        title="Selection to Bookmark"
        tooltip={'When enabled bookmarks will be placed to a near paragraph of current/previous selected text instead of page start'}
    >
      <ButtonToggleGroup
          options={optionsForToggle}
          bind:selectedOptionId={$selectionToBookmarkEnabled$}
      />
    </SettingsItemGroup>
    {#if !verticalMode}
      <SettingsItemGroup title="Page Columns">
        <input type="number"
               class={inputClasses}
               step="1" min="0"
               bind:value={$pageColumns$}/>
      </SettingsItemGroup>
    {/if}
  {/if}
  {#if showSpinner}
    <div class="tap-highlight-transparent fixed inset-0 bg-black/[.2]"></div>
    <div class="fixed inset-0 flex h-full w-full items-center justify-center text-7xl">
      <Fa icon={faSpinner} spin/>
    </div>
  {/if}
</div>
