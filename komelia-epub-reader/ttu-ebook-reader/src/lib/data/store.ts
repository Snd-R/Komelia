/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import {
    ReaderImageGalleryAvailableKeybind,
    type ReaderImageGalleryKeybindMap
} from '$lib/components/book-reader/book-reader-image-gallery/book-reader-image-gallery';
import {BlurMode} from '$lib/data/blur-mode';
import {defaultSansFont, defaultSerifFont, type Font, type UserFont} from '$lib/data/fonts';
import {writableSubject} from '$lib/functions/svelte/store';
import {map, ReplaySubject, skip} from 'rxjs';
import {FuriganaStyle} from './furigana-style';
import type {ThemeOption} from './theme-option';
import {ViewMode} from './view-mode';
import type {WritingMode} from './writing-mode';
import {externalFunctions} from '$lib/external';
import {BookReaderAvailableKeybind, type BookReaderKeybindMap} from './book-reader-keybind';
import {subjectToSvelteWritable} from '$lib/functions/rxjs/subject-to-writable';

export const bookId$ = subjectToSvelteWritable(new ReplaySubject<string>(1));

export const skipKeyDownListener$ = writableSubject<boolean>(false);

export const theme$ = writableSubject('light-theme');
export const customThemes$ = writableSubject<Record<string, ThemeOption>>({});
export const multiplier$ = writableSubject(20);
export const serifFontFamily$ = writableSubject<Font>(defaultSerifFont);
export const sansFontFamily$ = writableSubject<Font>(defaultSansFont);
export const fontSize$ = writableSubject(20);
export const lineHeight$ = writableSubject(1.65);
export const hideSpoilerImage$ = writableSubject(true);
export const hideSpoilerImageMode$ = writableSubject<BlurMode>(BlurMode.AFTER_TOC);
export const hideFurigana$ = writableSubject(false);
export const furiganaStyle$ = writableSubject<FuriganaStyle>(FuriganaStyle.Partial);
export const writingMode$ = writableSubject<WritingMode>('vertical-rl');
export const verticalMode$ = writingMode$.pipe(map((writingMode) => writingMode === 'vertical-rl'));
export const enableReaderWakeLock$ = writableSubject(false);
export const showCharacterCounter$ = writableSubject(true);
export const viewMode$ = writableSubject<ViewMode>(ViewMode.Continuous);
export const secondDimensionMaxValue$ = writableSubject(0);
export const firstDimensionMargin$ = writableSubject(0);
export const swipeThreshold$ = writableSubject(10);
export const disableWheelNavigation$ = writableSubject(false);
export const autoPositionOnResize$ = writableSubject(true);
export const avoidPageBreak$ = writableSubject(false);
export const customReadingPointEnabled$ = writableSubject(false);
export const selectionToBookmarkEnabled$ = writableSubject(false);
export const confirmClose$ = writableSubject(false);
export const manualBookmark$ = writableSubject(false);
export const autoBookmark$ = writableSubject(true);
export const autoBookmarkTime$ = writableSubject(3);
export const pageColumns$ = writableSubject(0);
export const verticalCustomReadingPosition$ = writableSubject(100);
export const horizontalCustomReadingPosition$ = writableSubject(0);
export const userFonts$ = writableSubject<UserFont[]>([]);
export const availableSystemFonts$ = writableSubject<string[]>([]);

export const bookReaderKeybindMap$ = writableSubject<BookReaderKeybindMap>({
  KeyB: BookReaderAvailableKeybind.BOOKMARK,
  b: BookReaderAvailableKeybind.BOOKMARK,
  KeyR: BookReaderAvailableKeybind.JUMP_TO_BOOKMARK,
  r: BookReaderAvailableKeybind.JUMP_TO_BOOKMARK,
  PageDown: BookReaderAvailableKeybind.NEXT_PAGE,
  pagedown: BookReaderAvailableKeybind.NEXT_PAGE,
  PageUp: BookReaderAvailableKeybind.PREV_PAGE,
  pageup: BookReaderAvailableKeybind.PREV_PAGE,
  Space: BookReaderAvailableKeybind.AUTO_SCROLL_TOGGLE,
  ' ': BookReaderAvailableKeybind.AUTO_SCROLL_TOGGLE,
  KeyA: BookReaderAvailableKeybind.AUTO_SCROLL_INCREASE,
  a: BookReaderAvailableKeybind.AUTO_SCROLL_INCREASE,
  KeyD: BookReaderAvailableKeybind.AUTO_SCROLL_DECREASE,
  d: BookReaderAvailableKeybind.AUTO_SCROLL_DECREASE,
  KeyN: BookReaderAvailableKeybind.PREV_CHAPTER,
  n: BookReaderAvailableKeybind.PREV_CHAPTER,
  KeyM: BookReaderAvailableKeybind.NEXT_CHAPTER,
  m: BookReaderAvailableKeybind.NEXT_CHAPTER,
  KeyT: BookReaderAvailableKeybind.SET_READING_POINT,
  t: BookReaderAvailableKeybind.SET_READING_POINT,
  KeyP: BookReaderAvailableKeybind.TOGGLE_TRACKING,
  p: BookReaderAvailableKeybind.TOGGLE_TRACKING,
  KeyF: BookReaderAvailableKeybind.TOGGLE_TRACKING_FREEZE,
  f: BookReaderAvailableKeybind.TOGGLE_TRACKING_FREEZE
});

export const readerImageGalleryKeybindMap$ = writableSubject<ReaderImageGalleryKeybindMap>({
  PageDown: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  pagedown: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  ArrowDown: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  arrowdown: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  ArrowRight: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  arrowright: ReaderImageGalleryAvailableKeybind.NEXT_IMAGE,
  ArrowUp: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  arrowup: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  ArrowLeft: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  arrowleft: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  PageUp: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  pageup: ReaderImageGalleryAvailableKeybind.PREVIOUS_IMAGE,
  Escape: ReaderImageGalleryAvailableKeybind.CLOSE,
  escape: ReaderImageGalleryAvailableKeybind.CLOSE
});

export interface ReaderSettings {
  theme: string;
  customThemes: Record<string, ThemeOption>;
  multiplier: number;
  serifFontFamily: Font;
  sansFontFamily: Font;
  fontSize: number;
  lineHeight: number;
  hideSpoilerImage: boolean;
  hideSpoilerImageMode: BlurMode;
  hideFurigana: boolean;
  furiganaStyle: FuriganaStyle;
  writingMode: WritingMode;
  enableReaderWakeLock: boolean;
  showCharacterCounter: boolean;
  viewMode: ViewMode;
  secondDimensionMaxValue: number;
  firstDimensionMargin: number;
  swipeThreshold: number;
  disableWheelNavigation: boolean;
  autoPositionOnResize: boolean;
  avoidPageBreak: boolean;
  customReadingPointEnabled: boolean;
  selectionToBookmarkEnabled: boolean;
  confirmClose: boolean;
  manualBookmark: boolean;
  autoBookmark: boolean;
  autoBookmarkTime: number;
  pageColumns: number;
  verticalCustomReadingPosition: number;
  horizontalCustomReadingPosition: number;
  userFonts: UserFont[];
}

let externalSettingsState: ReaderSettings;

export async function loadExternalSettings() {
  if (externalSettingsState != undefined) return;

  bookId$.next(await externalFunctions.getCurrentBookId());
  availableSystemFonts$.next(await externalFunctions.getAvailableSystemFonts());
  let userfonts = await externalFunctions.getStoredFonts()
  userFonts$.next(userfonts);
  externalSettingsState = await externalFunctions.getSettings();

  theme$.next(externalSettingsState.theme);
  customThemes$.next(externalSettingsState.customThemes);
  multiplier$.next(externalSettingsState.multiplier);
  serifFontFamily$.next(externalSettingsState.serifFontFamily);
  sansFontFamily$.next(externalSettingsState.sansFontFamily);
  fontSize$.next(externalSettingsState.fontSize);
  lineHeight$.next(externalSettingsState.lineHeight);
  hideSpoilerImage$.next(externalSettingsState.hideSpoilerImage);
  hideSpoilerImageMode$.next(externalSettingsState.hideSpoilerImageMode);
  hideFurigana$.next(externalSettingsState.hideFurigana);
  furiganaStyle$.next(externalSettingsState.furiganaStyle);
  writingMode$.next(externalSettingsState.writingMode);
  enableReaderWakeLock$.next(externalSettingsState.enableReaderWakeLock);
  showCharacterCounter$.next(externalSettingsState.showCharacterCounter);
  viewMode$.next(externalSettingsState.viewMode);
  secondDimensionMaxValue$.next(externalSettingsState.secondDimensionMaxValue);
  firstDimensionMargin$.next(externalSettingsState.firstDimensionMargin);
  swipeThreshold$.next(externalSettingsState.swipeThreshold);
  disableWheelNavigation$.next(externalSettingsState.disableWheelNavigation);
  autoPositionOnResize$.next(externalSettingsState.autoPositionOnResize);
  avoidPageBreak$.next(externalSettingsState.avoidPageBreak);
  customReadingPointEnabled$.next(externalSettingsState.customReadingPointEnabled);
  selectionToBookmarkEnabled$.next(externalSettingsState.selectionToBookmarkEnabled);
  confirmClose$.next(externalSettingsState.confirmClose);
  manualBookmark$.next(externalSettingsState.manualBookmark);
  autoBookmark$.next(externalSettingsState.autoBookmark);
  autoBookmarkTime$.next(externalSettingsState.autoBookmarkTime);
  pageColumns$.next(externalSettingsState.pageColumns);
  verticalCustomReadingPosition$.next(externalSettingsState.verticalCustomReadingPosition);
  horizontalCustomReadingPosition$.next(externalSettingsState.horizontalCustomReadingPosition);

  theme$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.theme = value;
    externalFunctions.putSettings(externalSettingsState);
  });

  customThemes$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.customThemes = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  multiplier$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.multiplier = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  serifFontFamily$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.serifFontFamily = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  sansFontFamily$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.sansFontFamily = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  fontSize$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.fontSize = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  lineHeight$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.lineHeight = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  hideSpoilerImage$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.hideSpoilerImage = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  hideSpoilerImageMode$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.hideSpoilerImageMode = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  hideFurigana$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.hideFurigana = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  furiganaStyle$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.furiganaStyle = value;
    externalFunctions.putSettings(externalSettingsState);
  });

  writingMode$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.writingMode = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  enableReaderWakeLock$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.enableReaderWakeLock = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  showCharacterCounter$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.showCharacterCounter = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  viewMode$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.viewMode = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  secondDimensionMaxValue$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.secondDimensionMaxValue = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  firstDimensionMargin$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.firstDimensionMargin = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  swipeThreshold$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.swipeThreshold = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  disableWheelNavigation$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.disableWheelNavigation = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  autoPositionOnResize$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.autoPositionOnResize = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  avoidPageBreak$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.avoidPageBreak = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  customReadingPointEnabled$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.customReadingPointEnabled = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  selectionToBookmarkEnabled$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.selectionToBookmarkEnabled = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  confirmClose$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.confirmClose = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  manualBookmark$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.manualBookmark = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  autoBookmark$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.autoBookmark = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  autoBookmarkTime$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.autoBookmarkTime = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  pageColumns$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.pageColumns = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  verticalCustomReadingPosition$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.verticalCustomReadingPosition = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  horizontalCustomReadingPosition$.pipe(skip(1)).subscribe((value) => {
    externalSettingsState.horizontalCustomReadingPosition = value;
    externalFunctions.putSettings(externalSettingsState);
  });
  // userFonts$.pipe(skip(1)).subscribe((value) => {
  //   externalSettingsState.userFonts = value;
  //   externalFunctions.putSettings(externalSettingsState);
  // });
}
