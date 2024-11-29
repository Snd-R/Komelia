<script lang="ts">
  import {faBookmark as farBookmark} from '@fortawesome/free-regular-svg-icons';
  import {
    faBookmark as fasBookmark, faCog,
    faCrosshairs,
    faExpand,
    faFlag,
    faList,
    faImages,
    faRotateLeft, faSignOutAlt
  } from '@fortawesome/free-solid-svg-icons';
  import Popover from '$lib/components/popover/popover.svelte';
  import {
    baseHeaderClasses,
    baseIconClasses,
    nTranslateXHeaderFa,
    translateXHeaderFa
  } from '$lib/css-classes';
  import {customReadingPointEnabled$, viewMode$} from '$lib/data/store';
  import {ViewMode} from '$lib/data/view-mode';
  import {dummyFn, isMobile$} from '$lib/functions/utils';
  import Fa from 'svelte-fa';

  interface Props {
    hasChapterData: boolean;
    autoScrollMultiplier: number;
    hasCustomReadingPoint: boolean;
    showFullscreenButton: boolean;
    isBookmarkScreen: boolean;
    hasBookmarkData: boolean;

    tocClick: () => void;
    bookmarkClick: () => void;
    scrollToBookmarkClick: () => void;
    completeBook: () => void;
    fullscreenClick: () => void;
    showCustomReadingPoint: () => void;
    setCustomReadingPoint: () => void;
    resetCustomReadingPoint: () => void;
    readerImageGalleryClick: () => void;
    settingsClick: () => void;
    closeBook: () => void;
  }

  let {
    hasChapterData,
    autoScrollMultiplier,
    hasCustomReadingPoint,
    showFullscreenButton,
    isBookmarkScreen = $bindable(),
    hasBookmarkData,
    tocClick,
    bookmarkClick,
    scrollToBookmarkClick,
    completeBook,
    fullscreenClick,
    showCustomReadingPoint,
    setCustomReadingPoint,
    resetCustomReadingPoint,
    readerImageGalleryClick,
    settingsClick,
    closeBook,
  }: Props = $props()

  const customReadingPointMenuItems: {
    label: string;
    action: any;
  }[] = [
    ...(hasCustomReadingPoint ? [{label: 'Show Point', action: showCustomReadingPoint}] : []),
    {label: 'Set Point', action: setCustomReadingPoint},
    ...(hasCustomReadingPoint ? [{label: 'Reset Point', action: resetCustomReadingPoint}] : [])
  ];

  let customReadingPointMenuElm: Popover | undefined = $state();


  function dispatchCustomReadingPointAction(action: () => void) {
    action()
    if (customReadingPointMenuElm) {
      customReadingPointMenuElm.toggleOpen();
    }
  }
</script>

<div class="flex justify-between bg-gray-700 px-4 md:px-8 {baseHeaderClasses}">
  <div class="flex transform-gpu {nTranslateXHeaderFa}">
    {#if hasChapterData}
      <div
          tabindex="0"
          role="button"
          title="Open Table of Contents"
          class={baseIconClasses}
          onclick={tocClick}
          onkeyup={dummyFn}
      >
        <Fa icon={faList}/>
      </div>
    {/if}
    <div
        tabindex="0"
        role="button"
        title="Create Bookmark"
        class={baseIconClasses}
        onclick={bookmarkClick}
        onkeyup={dummyFn}
    >
      <Fa icon={isBookmarkScreen ? fasBookmark : farBookmark}/>
    </div>
    {#if hasBookmarkData}
      <div
          tabindex="0"
          role="button"
          title="Return to Bookmark"
          class={baseIconClasses}
          onclick={scrollToBookmarkClick}
          onkeyup={dummyFn}
      >
        <Fa icon={faRotateLeft}/>
      </div>
    {/if}
    {#if $viewMode$ === ViewMode.Continuous && !$isMobile$}
      <div
          class="flex items-center px-4 text-xl xl:px-3 xl:text-lg"
          title="Current Autoscroll Speed"
      >
        {autoScrollMultiplier}x
      </div>
    {/if}
  </div>

  <div class="flex transform-gpu {translateXHeaderFa}">
    <div
        tabindex="0"
        role="button"
        title="Complete Book"
        class={baseIconClasses}
        onclick={completeBook}
        onkeyup={dummyFn}
    >
      <Fa icon={faFlag}/>
    </div>
    {#if $customReadingPointEnabled$ || $viewMode$ === ViewMode.Paginated}
      <div class="flex">
        <Popover
            placement="bottom"
            fallbackPlacements={['bottom-end', 'bottom-start']}
            yOffset={0}
            bind:this={customReadingPointMenuElm}
        >
          <div slot="icon" title="Open Custom Point Actions" class={baseIconClasses}>
            <Fa icon={faCrosshairs}/>
          </div>
          <div class="w-40 bg-gray-700 md:w-32" slot="content">
            {#each customReadingPointMenuItems as actionItem (actionItem.label)}
              <div
                  tabindex="0"
                  role="button"
                  class="px-4 py-2 text-sm hover:bg-white hover:text-gray-700"
                  onclick={() => dispatchCustomReadingPointAction(actionItem.action)}
                  onkeyup={dummyFn}
              >
                {actionItem.label}
              </div>
            {/each}
          </div>
        </Popover>
      </div>
    {/if}
    {#if showFullscreenButton}
      <div
          tabindex="0"
          role="button"
          title="Toggle Fullscreen"
          class={baseIconClasses}
          onclick={fullscreenClick}
          onkeyup={dummyFn}
      >
        <Fa icon={faExpand}/>
      </div>
    {/if}
    <div
        tabindex="0"
        role="button"
        title="Images"
        class={baseIconClasses}
        onclick={readerImageGalleryClick}
        onkeyup={dummyFn}
    >
      <Fa icon={faImages}/>
    </div>
    <div
        tabindex="0"
        role="button"
        title="Settings"
        class={baseIconClasses}
        onclick={settingsClick}
        onkeyup={dummyFn}
    >
      <Fa icon={faCog}/>
    </div>

    <div
        tabindex="0"
        role="button"
        title="Close Book"
        class={baseIconClasses}
        onclick={closeBook}
        onkeyup={dummyFn}
    >
      <Fa icon={faSignOutAlt}/>
    </div>
  </div>
</div>
