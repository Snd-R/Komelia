<script lang="ts">
  import {
    debounceTime,
    EMPTY,
    filter,
    fromEvent,
    map,
    merge,
    of,
    ReplaySubject,
    share,
    shareReplay,
    skip,
    startWith,
    switchMap,
    take,
    takeWhile,
    tap,
    timer
  } from 'rxjs';
  import {quintInOut} from 'svelte/easing';
  import {fly} from 'svelte/transition';
  import {faSpinner} from '@fortawesome/free-solid-svg-icons';
  import BookReader from '$lib/components/book-reader/book-reader.svelte';
  import type {AutoScroller, BookmarkManager, PageManager} from '$lib/components/book-reader/types';
  import MessageDialog from '$lib/components/message-dialog.svelte';
  import StyleSheetRenderer from '$lib/components/style-sheet-renderer.svelte';
  import Fa from 'svelte-fa';
  import {
    autoBookmark$,
    autoBookmarkTime$,
    autoPositionOnResize$,
    avoidPageBreak$,
    bookId$,
    bookReaderKeybindMap$,
    confirmClose$,
    customReadingPointEnabled$,
    customThemes$,
    firstDimensionMargin$,
    serifFontFamily$,
    sansFontFamily$,
    fontSize$,
    furiganaStyle$,
    hideFurigana$,
    hideSpoilerImage$,
    hideSpoilerImageMode$,
    horizontalCustomReadingPosition$,
    lineHeight$,
    loadExternalSettings,
    manualBookmark$,
    multiplier$,
    pageColumns$,
    secondDimensionMaxValue$,
    selectionToBookmarkEnabled$,
    showCharacterCounter$,
    skipKeyDownListener$,
    theme$,
    verticalCustomReadingPosition$,
    verticalMode$,
    viewMode$,
    writingMode$
  } from '$lib/data/store';
  import BookReaderHeader from '$lib/components/book-reader/book-reader-header.svelte';
  import {
    readerImageGalleryPictures$,
    toggleImageGalleryPictureSpoiler$,
    updateImageGalleryPictureSpoilers$
  } from '$lib/components/book-reader/book-reader-image-gallery/book-reader-image-gallery';
  import BookReaderImageGallery
    from '$lib/components/book-reader/book-reader-image-gallery/book-reader-image-gallery.svelte';
  import {
    getChapterData,
    nextChapter$,
    sectionList$,
    sectionProgress$,
    tocIsOpen$
  } from '$lib/components/book-reader/book-toc/book-toc';
  import BookToc from '$lib/components/book-reader/book-toc/book-toc.svelte';
  import ConfirmDialog from '$lib/components/confirm-dialog.svelte';
  import {type BookData, type BookmarkData,} from '$lib/data/books-db';
  import {type Dialog, dialogManager} from '$lib/data/dialog-manager';
  import {PAGE_CHANGE, SKIPKEYLISTENER, SYNCED} from '$lib/data/events';
  import {fullscreenManager} from '$lib/data/fullscreen-manager';
  import {logger} from '$lib/data/logger';
  import {availableThemes} from '$lib/data/theme-option';
  import {ViewMode} from '$lib/data/view-mode';
  import loadBookData from '$lib/functions/book-data-loader/load-book-data';
  import {reduceToEmptyString} from '$lib/functions/rxjs/reduce-to-empty-string';
  import {tapDom} from '$lib/functions/rxjs/tap-dom';
  import {clickOutside} from '$lib/functions/use-click-outside';
  import {dummyFn, isMobile, isMobile$} from '$lib/functions/utils';
  import {onKeydownReader} from '../../on-keydown-reader';
  import {onDestroy, onMount, tick} from 'svelte';
  import {
    clearRange,
    getParagraphToPoint,
    getRangeForUserSelection,
    getReferencePoints,
    pulseElement
  } from '$lib/functions/range-util';
  import {externalFunctions} from "$lib/external";

  interface Props {
    onSettingsClick: () => void;
  }

  let {onSettingsClick}: Props = $props();

  let showHeader = $state(false);
  let isBookmarkScreen = $state(false);
  let showFooter = $state(true);
  let exploredCharCount = $state(0);
  let bookCharCount = $state(0);
  let autoScroller: AutoScroller | undefined = $state();
  let bookmarkManager: BookmarkManager | undefined = $state();
  let pageManager: PageManager | undefined = $state();
  let bookmarkData: Promise<BookmarkData | undefined> = $state(Promise.resolve(undefined));
  let customReadingPointTop = $state(-2);
  let customReadingPointLeft = $state(-2);
  let customReadingPoint = $state($verticalMode$
    ? $verticalCustomReadingPosition$
    : $horizontalCustomReadingPosition$);
  let customReadingPointScrollOffset = $state(0);
  let customReadingPointRange: Range | undefined = $state();
  let lastSelectedRange: Range | undefined = $state();
  let lastSelectedRangeWasEmpty = $state(true);
  let isSelectingCustomReadingPoint = $state(false);
  let showCustomReadingPoint = $state(false);
  let storedExploredCharacter = $state(0);
  let hasBookmarkData = $state(false);
  let frozenPosition = $state(-1);
  let skipFirstFreezeChange = $state(false);
  let showReaderImageGallery = $state(false);
  let fullscreenAvailable = $state(false)

  let initPromise = initialize();

  const queuedReaderImageGalleryPictures = new Map<string, boolean>();

  const rawBookData$ = new ReplaySubject<BookData | undefined>(1)

  async function initialize() {

    let bookData: BookData | undefined;
    try {
      bookData = await externalFunctions.getBookData();
      fullscreenAvailable = await externalFunctions.isFullscreenAvailable()

    } catch (error: any) {
      const message = `Error loading book: ${error.message}`;

      logger.warn(message);

      dialogManager.dialogs$.next([
        {
          component: MessageDialog,
          props: {
            title: 'Load Error',
            message
          }
        }
      ]);
    } finally {
      rawBookData$.next(bookData);
    }
  }

  const initBookmarkData$ = rawBookData$.pipe(
    tap((rawBookData) => {
      if (!rawBookData) return;
      bookmarkData = externalFunctions.getBookmark();
    }),
    reduceToEmptyString()
  );

  const bookData$ = rawBookData$.pipe(
    switchMap((rawBookData) => {
      if (!rawBookData) return EMPTY;

      sectionList$.next(rawBookData.sections || []);

      return loadBookData(
        rawBookData,
        '.book-content',
        document,
        $viewMode$ === ViewMode.Paginated,
        hideSpoilerImageMode$.getValue()
      );
    }),
    shareReplay({refCount: true, bufferSize: 1})
  );

  const resize$ = (visualViewport ? fromEvent(visualViewport, 'resize') : of()).pipe(share());

  const containerViewportWidth$ = resize$.pipe(
    startWith(0),
    map(() => visualViewport?.width || 0),
  );

  const containerViewportHeight$ = resize$.pipe(
    startWith(0),
    map(() => visualViewport?.height || 0),
  );

  const themeOption$ = theme$.pipe(
    map(
      (theme) =>
        availableThemes.get(theme) || $customThemes$[theme] || availableThemes.get('light-theme')
    ),
    filter((o): o is NonNullable<typeof o> => !!o),
  );

  const backgroundColor$ = themeOption$.pipe(map((o) => o.backgroundColor));

  const collectReaderImageGallerySpoilerToggles$ = toggleImageGalleryPictureSpoiler$.pipe(
    tap((readerImageGalleryPicture) => {
      queuedReaderImageGalleryPictures.set(
        readerImageGalleryPicture.url,
        readerImageGalleryPicture.unspoilered
      );

      updateImageGalleryPictureSpoilers$.next();
    }),
    reduceToEmptyString()
  );

  const handleUpdateImageGalleryPictureSpoilers$ = updateImageGalleryPictureSpoilers$.pipe(
    debounceTime(250),
    tap(() => {
      $readerImageGalleryPictures$ = $readerImageGalleryPictures$.map((galleryPicture) => {
        const picture = galleryPicture;

        if (queuedReaderImageGalleryPictures.has(picture.url)) {
          picture.unspoilered = queuedReaderImageGalleryPictures.get(picture.url)!;
        }

        return picture;
      });

      queuedReaderImageGalleryPictures.clear();
    }),
    reduceToEmptyString()
  );

  const backgroundStyleName = 'background-color';
  const setBackgroundColor$ = backgroundColor$.pipe(
    tapDom(
      () => document.body,
      (backgroundColor, body) => body.style.setProperty(backgroundStyleName, backgroundColor),
      (body) => body.style.removeProperty(backgroundStyleName)
    ),
    reduceToEmptyString(),
  );

  const writingModeStyleName = 'writing-mode';
  const setWritingMode$ = writingMode$.pipe(
    tapDom(
      () => document.documentElement,
      (writingMode, documentElement) =>
        documentElement.style.setProperty(writingModeStyleName, writingMode),
      (documentElement) => documentElement.style.removeProperty(writingModeStyleName)
    ),
    reduceToEmptyString(),
  );

  const sectionData$ = sectionProgress$.pipe(
    map((sectionProgress) => [...sectionProgress.values()]),
  );

  const textSelector$ = fromEvent(document, 'selectionchange').pipe(
    debounceTime(200),
    tap(() => {
      const currentSelected = window.getSelection()?.toString() || '';

      if (!currentSelected && lastSelectedRangeWasEmpty) {
        lastSelectedRange = undefined;
      } else if (currentSelected) {
        lastSelectedRange = window.getSelection()?.getRangeAt(0);
        lastSelectedRangeWasEmpty = false;
      } else {
        lastSelectedRangeWasEmpty = true;
      }
    }),
    reduceToEmptyString()
  );


  $effect(() => {
    if ($tocIsOpen$) {
      autoScroller?.off();
    }
  })

  $effect(() => {
    if (bookCharCount) {
      document.dispatchEvent(new CustomEvent(PAGE_CHANGE, {detail: {exploredCharCount}}));
    }
  });

  $effect(() => {
    document.dispatchEvent(new CustomEvent(PAGE_CHANGE, {detail: {bookCharCount}}))
  });

  $effect(() => {
    if (showCustomReadingPoint) {

      pulseElement(customReadingPointRange?.endContainer?.parentElement, 'add', 1);

      fromEvent(document, 'click')
        .pipe(skip(1), take(1))
        .subscribe(() => {
          showCustomReadingPoint = false;
          pulseElement(customReadingPointRange?.endContainer?.parentElement, 'remove', 1);
        });
    }
  })
  $effect(() => {
    if (frozenPosition !== -1 && exploredCharCount >= frozenPosition) {
      if (skipFirstFreezeChange) {
        skipFirstFreezeChange = false;
      } else {
        frozenPosition = -1;
      }
    }
  })


  let isPaginated = $derived($viewMode$ === ViewMode.Paginated);

  $effect(() => {
    bookmarkData.then((data) => {
      hasBookmarkData = !!data;
      storedExploredCharacter = data?.exploredCharCount || 0;
    })
  })

  /** Experimental Code - May be removed any time without warning */

  $effect(() => {
    document.dispatchEvent(new CustomEvent(SKIPKEYLISTENER, {detail: $skipKeyDownListener$}))
  });

  onMount(() => {
    // settings = await SettingsStore.getSettingsStore();
    document.addEventListener('ttu-action', handleAction, false)
  });

  function handleAction({detail}: any) {
    if (!detail.type) {
      return;
    }
    if (detail.type === 'skipKeyDownListener') {
      skipKeyDownListener$.next(detail.params.value);
    }
  }

  /** Experimental Code - May be removed any time without warning */

  onDestroy(() => {
    document.removeEventListener('ttu-action', handleAction, false);

    readerImageGalleryPictures$.next([]);
  });

  function handleUnload(event: BeforeUnloadEvent) {
    return event;
  }


  async function completeBook() {
    if (!$rawBookData$) {
      return;
    }

    const wasAutoscrollerEnabled = autoScroller?.wasAutoScrollerEnabled$.getValue();

    showHeader = false;
    autoScroller?.off();

    const diffToComplete = Math.max(0, bookCharCount - exploredCharCount)
    const wasCanceled = await new Promise((resolver) => {
      dialogManager.dialogs$.next([
        {
          component: ConfirmDialog,
          props: {
            dialogHeader: 'Complete Book',
            dialogMessage: `Would you like to complete this Book${
              diffToComplete ? ` and capture ${diffToComplete} characters read` : ''
            }?`,
            resolver
          }
        }
      ]);
    });

    if (wasCanceled) {
      if (wasAutoscrollerEnabled) {
        autoScroller?.toggle();
      }

      return;
    }

    dialogManager.dialogs$.next([
      {
        component: '<div/>',
        disableCloseOnClick: true
      }
    ]);

    try {
      dialogManager.dialogs$.next([]);

      merge(fromEvent(document, 'pointerup'), timer(10000))
        .pipe(take(1))
        .subscribe(() => {
        });
    } catch (message: any) {
      dialogManager.dialogs$.next([
        {
          component: MessageDialog,
          props: {
            title: 'Error',
            message: `Error completing Book: ${message}`
          }
        }
      ]);
    }
  }

  function copyCurrentProgress(currentProgress: string) {
    try {
      navigator.clipboard.writeText(currentProgress);
    } catch (error: any) {
      logger.error(`Error writing Progress to Clipboard: ${error.message}`);
    }
  }

  function onKeydown(ev: KeyboardEvent) {
    if (
      $skipKeyDownListener$ ||
      ev.altKey ||
      ev.ctrlKey ||
      ev.shiftKey ||
      ev.metaKey ||
      ev.repeat
    ) {
      return;
    }

    const result = onKeydownReader(
      ev,
      bookReaderKeybindMap$.getValue(),
      bookmarkPage,
      scrollToBookmark,
      (x) => multiplier$.next(multiplier$.getValue() + x),
      autoScroller,
      pageManager,
      $verticalMode$,
      changeChapter,
      handleSetCustomReadingPoint,
      () => {
      },
      () => {
      }
    );

    if (!result) return;

    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
    ev.preventDefault();
  }

  function getBookIdSync() {
    let bookId: string | undefined;
    bookId$.subscribe((x) => (bookId = x)).unsubscribe();
    return bookId;
  }

  async function bookmarkPage() {
    const bookId = getBookIdSync();
    if (!bookId || !bookmarkManager || !$sectionData$?.length) return;

    const [, currentChapterIndex, referenceId] = getChapterData($sectionData$);

    let data: BookmarkData;

    showHeader = false;

    if (isPaginated) {
      const userSelectedRange = $selectionToBookmarkEnabled$
        ? getRangeForUserSelection(window, lastSelectedRange)
        : undefined;
      const bookmarkRange = userSelectedRange || customReadingPointRange;

      pulseElement(bookmarkRange?.endContainer?.parentElement, 'add', 0.5, 500);

      data = bookmarkManager.formatBookmarkDataByRange(bookId, currentChapterIndex, referenceId, bookmarkRange);

      if (userSelectedRange) {
        clearRange(window);
      }
    } else {
      data = bookmarkManager.formatBookmarkData(bookId, currentChapterIndex, referenceId, customReadingPointScrollOffset);
    }

    await externalFunctions.putBookmark(data);

    bookmarkData = Promise.resolve(data);
  }

  async function scrollToBookmark() {
    const data = await bookmarkData;
    if (!data || !bookmarkManager) return;

    bookmarkManager.scrollToBookmark(data, customReadingPointScrollOffset);
  }

  async function onFullscreenClick() {
    showHeader = false;

    if (await externalFunctions.isFullscreen()) {
      await externalFunctions.exitFullscreen()
    } else {
      await externalFunctions.enterFullscreen()
    }
  }

  function changeChapter(offset: number) {
    if (!$sectionData$?.length) {
      return;
    }

    const [mainChapters, currentChapterIndex] = getChapterData($sectionData$);

    if (
      (!currentChapterIndex && offset === -1) ||
      (offset === 1 && currentChapterIndex === mainChapters.length - 1)
    ) {
      return;
    }

    const nextChapter = mainChapters[currentChapterIndex + offset];

    if (!nextChapter) {
      return;
    }

    nextChapter$.next(nextChapter.reference);
  }

  function openActionBackdrop() {
    dialogManager.dialogs$.next([
      {
        component: '<div/>',
        disableCloseOnClick: true
      }
    ]);
  }

  async function leaveReader() {
    let message;

    try {
      await tick();

      autoScroller?.off();

      if ($confirmClose$ && storedExploredCharacter !== exploredCharCount) {
        const wasCanceled = await new Promise((resolver) => {
          dialogManager.dialogs$.next([
            {
              component: ConfirmDialog,
              props: {
                dialogHeader: 'Confirm Exit',
                dialogMessage: 'Your current location was not bookmarked. Continue leaving?',
                resolver
              },

              disableCloseOnClick: true
            }
          ]);
        });

        if (wasCanceled) {
          return;
        }

        await tick();
      }

      openActionBackdrop();

      if (!$manualBookmark$) {
        await bookmarkPage();
      }

      dialogManager.dialogs$.next([]);

    } catch (error: any) {
      message = error.message;
    }

    if (message) {
      logger.error(message);

      dialogManager.dialogs$.next([
        {
          component: MessageDialog,
          props: {
            title: 'Error',
            message
          },
          disableCloseOnClick: true
        }
      ]);
    }

    await externalFunctions.closeBook()
  }

  function handleSetCustomReadingPoint() {
    if (!$customReadingPointEnabled$ && !isPaginated) {
      return;
    }

    const contentEl = document.querySelector('.book-content');

    if (!contentEl) {
      return;
    }

    autoScroller?.off();

    if (isPaginated) {
      customReadingPointTop = window.innerHeight / 2 - 2;
      customReadingPointLeft = window.innerWidth / 2 - 2;
    }

    showHeader = false;
    isSelectingCustomReadingPoint = true;
    document.body.classList.add('cursor-crosshair');

    const {
      elLeftReferencePoint,
      elTopReferencePoint,
      elRightReferencePoint,
      elBottomReferencePoint,
      pointGap
    } = getReferencePoints(window, contentEl, $verticalMode$, $firstDimensionMargin$);

    merge(fromEvent(document, 'pointerup'), fromEvent(document, 'pointermove'))
      // eslint-disable-next-line rxjs/no-ignored-takewhile-value
      .pipe(takeWhile(() => isSelectingCustomReadingPoint))
      .subscribe((event: Event) => {
        if (!(event instanceof PointerEvent)) {
          return;
        }

        if (event.type === 'pointerup') {
          document.body.classList.remove('cursor-crosshair');
          isSelectingCustomReadingPoint = false;

          tick().then(() => {
            customReadingPointLeft = $verticalMode$ ? event.x : customReadingPointLeft;
            customReadingPointTop = $verticalMode$ ? customReadingPointTop : event.y;

            const result = getParagraphToPoint(customReadingPointLeft, customReadingPointTop);

            if (result) {
              pulseElement(result.parent, 'add', 0.5, 500);
            }

            if (isPaginated) {
              customReadingPointRange = result?.range;
            } else {
              let newPercentage = 0;

              if ($verticalMode$) {
                newPercentage = Math.ceil(
                  (Math.max(0, customReadingPointLeft - elLeftReferencePoint) /
                    (elRightReferencePoint - elLeftReferencePoint)) *
                  100
                );

                verticalCustomReadingPosition$.next(newPercentage);
              } else {
                newPercentage = Math.ceil(
                  (Math.max(0, customReadingPointTop - elTopReferencePoint) /
                    (elBottomReferencePoint - elTopReferencePoint)) *
                  100
                );

                horizontalCustomReadingPosition$.next(newPercentage);
              }

              customReadingPoint = newPercentage;
            }

          });
        } else {
          const insideXBound =
            event.x >= elLeftReferencePoint + pointGap && event.x <= elRightReferencePoint;
          const insideYBound =
            event.y >= elTopReferencePoint && event.y <= elBottomReferencePoint - pointGap;

          if (isPaginated) {
            customReadingPointTop = insideYBound ? event.y : customReadingPointTop;
            customReadingPointLeft = insideXBound ? event.x : customReadingPointLeft;
          } else if ($verticalMode$ && insideXBound) {
            customReadingPointLeft = event.x;
          } else if (!$verticalMode$ && insideYBound) {
            customReadingPointTop = event.y;
          }
        }
      });
  }


</script>

{$setBackgroundColor$ ?? ''}
{#await initPromise}
  <div class="fixed inset-0 flex h-full w-full items-center justify-center text-7xl">
    <Fa icon={faSpinner} spin/>
  </div>
{:then _}
  {$collectReaderImageGallerySpoilerToggles$ ?? ''}
  {$handleUpdateImageGalleryPictureSpoilers$ ?? ''}
  <button class="fixed inset-x-0 top-0 z-10 h-8 w-full" onclick={() => (showHeader = true)}></button>
  {#if showHeader}
    <div
        class="elevation-4 writing-horizontal-tb fixed inset-x-0 top-0 z-10 w-full"
        transition:fly|local={{ y: -300, easing: quintInOut }}
        use:clickOutside={() => (showHeader = false)}
    >
      <BookReaderHeader
          hasChapterData={!!$sectionData$?.length}
          hasCustomReadingPoint={!!(
            ($customReadingPointEnabled$ || isPaginated) &&
            ((isPaginated && customReadingPointRange) ||
            (!isPaginated && customReadingPointLeft > -1 && customReadingPointTop > -1))
            )}
          showFullscreenButton={fullscreenAvailable}
          autoScrollMultiplier={$multiplier$}
          {hasBookmarkData}
          tocClick={() => {
            showHeader = false;
            tocIsOpen$.next(true);
        }}
          completeBook={completeBook}
          setCustomReadingPoint={handleSetCustomReadingPoint}
          showCustomReadingPoint={() => {
            showHeader = false;
            showCustomReadingPoint = true;
        }}
          resetCustomReadingPoint={() => {
            showHeader = false;
            if (isPaginated) {
              customReadingPointRange = undefined;
            } else if ($verticalMode$) {
              verticalCustomReadingPosition$.next(100);
              customReadingPoint = 100;
            } else {
              horizontalCustomReadingPosition$.next(0);
              customReadingPoint = 0;
            }
        }}
          fullscreenClick={onFullscreenClick}
          bookmarkClick={bookmarkPage}
          scrollToBookmarkClick={() => {
            showHeader = false;
            scrollToBookmark();
          }}
          readerImageGalleryClick={() => {
            showHeader = false;
            showReaderImageGallery = true;
            }}
          settingsClick={onSettingsClick}
          closeBook={leaveReader}
          bind:isBookmarkScreen
      />
    </div>
  {/if}
  {#if $bookData$ && $rawBookData$}
    <StyleSheetRenderer styleSheet={$bookData$.styleSheet}/>
    <BookReader
        htmlContent={$bookData$.htmlContent}
        width={$containerViewportWidth$ ?? 0}
        height={$containerViewportHeight$ ?? 0}
        verticalMode={$verticalMode$}
        fontColor={$themeOption$?.fontColor}
        backgroundColor={$backgroundColor$}
        hintFuriganaFontColor={$themeOption$?.hintFuriganaFontColor}
        hintFuriganaShadowColor={$themeOption$?.hintFuriganaShadowColor}
        fontFamilyGroupOne={$serifFontFamily$}
        fontFamilyGroupTwo={$sansFontFamily$}
        fontSize={$fontSize$}
        lineHeight={$lineHeight$}
        hideSpoilerImage={$hideSpoilerImage$}
        hideFurigana={$hideFurigana$}
        furiganaStyle={$furiganaStyle$}
        viewMode={$viewMode$}
        secondDimensionMaxValue={$secondDimensionMaxValue$}
        firstDimensionMargin={$firstDimensionMargin$}
        autoPositionOnResize={$autoPositionOnResize$}
        avoidPageBreak={$avoidPageBreak$}
        pageColumns={$pageColumns$}
        autoBookmark={$autoBookmark$}
        autoBookmarkTime={$autoBookmarkTime$}
        multiplier={$multiplier$}
        bind:exploredCharCount
        bind:bookCharCount
        bind:isBookmarkScreen
        bind:bookmarkData
        bind:autoScroller
        bind:bookmarkManager
        bind:pageManager
        bind:customReadingPoint
        bind:customReadingPointTop
        bind:customReadingPointLeft
        bind:customReadingPointScrollOffset
        bind:customReadingPointRange
        bind:showCustomReadingPoint
        on:bookmark={bookmarkPage}
    />
    {$initBookmarkData$ ?? ''}
    {$setWritingMode$ ?? ''}
    {$textSelector$ ?? ''}
  {/if}

  {#if $sectionData$ && $tocIsOpen$ }
    <div
        class="writing-horizontal-tb fixed top-0 left-0 z-[60] flex h-full w-full max-w-xl flex-col justify-between"
        style:color={$themeOption$?.fontColor}
        style:background-color={$backgroundColor$}
        in:fly|local={{ x: -100, duration: 100, easing: quintInOut }}
        use:clickOutside={() => { tocIsOpen$.next(false); }}
    >
      <BookToc
          sectionData={$sectionData$}
          verticalMode={$verticalMode$}
          exploredCharCount={exploredCharCount}
      />
    </div>
  {/if}

  {#if showReaderImageGallery}
    <BookReaderImageGallery
        fontColor={$themeOption$.fontColor}
        backgroundColor={$backgroundColor$}
        on:close={() => (showReaderImageGallery = false)}
    />
  {/if}

  {#if (isSelectingCustomReadingPoint && !$isMobile$) || (!isPaginated && showCustomReadingPoint)}
    <div
        class="fixed left-0 z-20 h-[1px] w-full border border-red-500"
        style:top={`${customReadingPointTop}px`}
    ></div>
    <div
        class="fixed top-0 z-20 h-full w-[1px] border border-red-500"
        style:left={`${customReadingPointLeft}px`}
    ></div>
  {/if}

  <div
      id="ttu-page-footer"
      tabindex="0"
      role="button"
      class="writing-horizontal-tb fixed bottom-0 left-0 z-10 flex h-8 w-full items-center justify-between text-xs leading-none"
      style:color={$themeOption$?.tooltipTextFontColor}
      onclick={() => (showFooter = !showFooter)}
      onkeyup={dummyFn}
  >
    {#if showFooter && bookCharCount}
      {@const currentProgress = `${exploredCharCount} / ${bookCharCount} ${(
          (exploredCharCount / bookCharCount) *
          100
      ).toFixed(2)}%`}
      <div
          tabindex="0"
          role="button"
          title="Click to copy Progress"
          class="writing-horizontal-tb fixed bottom-2 right-2 z-10 text-xs leading-none select-none"
          class:invisible={!$showCharacterCounter$}
          style:color={$themeOption$?.tooltipTextFontColor}
          onclick={(e) => {
              e.stopPropagation()
              if (!$showCharacterCounter$) return;
              copyCurrentProgress(currentProgress);
              if (e.target instanceof HTMLElement) {
                pulseElement(e.target, 'add', 0.5, 500);
              }
            }}
          onkeyup={dummyFn}
      >
        {currentProgress}
      </div>
    {/if}
  </div>

{/await}

<svelte:window
    on:keydown={onKeydown}
    on:beforeunload={handleUnload}
    on:resize={() => {
  }}
/>
