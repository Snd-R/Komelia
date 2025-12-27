/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import type {BlurMode} from '$lib/data/blur-mode';
import type {BookData} from '$lib/data/books-db';
import formatBookDataHtml from './format-book-data-html';
import formatStyleSheet from './format-style-sheet';
import {map} from 'rxjs/operators';

export default function loadBookData(
  bookData: BookData,
  parentSelector: string,
  document: Document,
  isPaginated: boolean,
  blurMode: BlurMode
) {
  return formatBookDataHtml(bookData, document, isPaginated, blurMode).pipe(
    map((htmlContent) => ({
      htmlContent,
      styleSheet: formatStyleSheet(bookData, parentSelector)
    }))
  );
}
