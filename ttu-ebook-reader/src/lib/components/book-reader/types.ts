/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import type {BehaviorSubject} from 'rxjs';
import type {BookmarkData} from '$lib/data/books-db';

export interface AutoScroller {
  wasAutoScrollerEnabled$: BehaviorSubject<boolean>;
  toggle: () => void;
  off: () => void;
}

export interface BookmarkManager {
  formatBookmarkData: (
    bookId: string,
    chapterIndex: number,
    chapterReferenceId: string,
    customReadingPointScrollOffset: number
  ) => BookmarkData;

  formatBookmarkDataByRange: (
    bookId: string,
    chapterIndex: number,
    chapterReferenceId: string,
    customReadingPointRange: Range | undefined
  ) => BookmarkData;

  scrollToBookmark: (bookmarkData: BookmarkData, customReadingPointScrollOffset?: number) => void;
}

export interface PageManager {
  nextPage: () => void;

  prevPage: () => void;

  updateSectionDataByOffset: (offset: number) => void;
}
