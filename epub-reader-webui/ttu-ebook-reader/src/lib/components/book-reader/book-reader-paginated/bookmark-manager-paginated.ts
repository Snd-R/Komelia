/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import type {BehaviorSubject, Observable} from 'rxjs';

import type {BookmarkManager} from '$lib/components/book-reader/types';
import type {BookmarkData} from '$lib/data/books-db';
import type {PageManagerPaginated} from './page-manager-paginated';
import type {SectionCharacterStatsCalculator} from './section-character-stats-calculator';

export class BookmarkManagerPaginated implements BookmarkManager {
  constructor(
    private calculator: SectionCharacterStatsCalculator,
    private pageManager: PageManagerPaginated,
    private sectionReady$: Observable<SectionCharacterStatsCalculator>,
    private sectionIndex$: BehaviorSubject<number>,
    private setIntendedCharCount: (count: number) => void
  ) {
  }

  scrollToBookmark(bookmarkData: BookmarkData) {
    const charCount = bookmarkData.exploredCharCount;
    if (!charCount) return;

    const index = this.calculator.getSectionIndexByCharCount(charCount);

    const scroll = (calc: SectionCharacterStatsCalculator) => {
      const scrollPos = calc.getScrollPosByCharCount(charCount);
      this.pageManager.scrollTo(scrollPos, false);
      this.setIntendedCharCount(charCount);
    };

    const currentSectionIndex = this.sectionIndex$.getValue();

    if (currentSectionIndex === index) {
      scroll(this.calculator);
      return;
    }

    const subscription = this.sectionReady$.subscribe((updatedCalc) => {
      scroll(updatedCalc);
      subscription.unsubscribe();
    });
    this.sectionIndex$.next(index);
  }

  formatBookmarkData(
    bookId: string,
    chapterIndex: number,
    chapterReferenceId: string,
  ): BookmarkData {
    return this.formatBookmarkDataByRange(bookId, chapterIndex, chapterReferenceId, undefined);
  }

  formatBookmarkDataByRange(
    bookId: string,
    chapterIndex: number,
    chapterReferenceId: string,
    customReadingPointRange: Range | undefined
  ): BookmarkData {
    const exploredCharCount = this.calculator.calcExploredCharCount(customReadingPointRange);
    const bookCharCount = this.calculator.charCount;

    return {
      bookId: bookId,
      exploredCharCount,
      progress: exploredCharCount / bookCharCount,
      lastBookmarkModified: new Date().getTime(),
      chapterIndex: chapterIndex,
      chapterReference: chapterReferenceId
    };
  }
}
