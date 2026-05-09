export interface DictionaryLookup {
  word: string;
  range: Range;
  x: number;
  y: number;
}

interface CaretPositionDocument {
  caretPositionFromPoint?: (x: number, y: number) => {
    offsetNode: Node;
    offset: number;
  } | null;
}

export function getDictionaryLookupFromPoint(
  document: Document,
  contentEl: HTMLElement,
  x: number,
  y: number
): DictionaryLookup | undefined {
  const target = document.elementFromPoint(x, y);
  if (!target || !contentEl.contains(target)) {
    return undefined;
  }

  const caretRange = getCaretRangeFromPoint(document, x, y);
  if (!caretRange) {
    return undefined;
  }

  const textNode = getTextNode(caretRange.startContainer, caretRange.startOffset);
  if (!textNode || !contentEl.contains(textNode.parentElement)) {
    return undefined;
  }

  const text = textNode.textContent || '';
  const wordBounds = getWordBounds(text, caretRange.startOffset);
  if (!wordBounds) {
    return undefined;
  }

  const range = document.createRange();
  range.setStart(textNode, wordBounds.start);
  range.setEnd(textNode, wordBounds.end);

  const rect = range.getBoundingClientRect();

  return {
    word: text.slice(wordBounds.start, wordBounds.end).trim(),
    range,
    x: rect.left + rect.width / 2 || x,
    y: rect.bottom || y
  };
}

export function getDictionaryLookupFromSelection(
  document: Document,
  contentEl: HTMLElement,
  selection: Selection | null | undefined
): DictionaryLookup | undefined {
  if (!selection || selection.rangeCount === 0 || selection.isCollapsed) {
    return undefined;
  }

  const selectedText = selection.toString().trim();
  if (!isSingleWord(selectedText)) {
    return undefined;
  }

  const range = selection.getRangeAt(0);
  const container = range.commonAncestorContainer;
  const containerElement = container instanceof Element ? container : container.parentElement;
  if (!containerElement || !contentEl.contains(containerElement)) {
    return undefined;
  }

  const rect = getRangeRect(range);
  if (!rect) {
    return undefined;
  }

  return {
    word: selectedText,
    range: range.cloneRange(),
    x: rect.left + rect.width / 2,
    y: rect.bottom
  };
}

function getCaretRangeFromPoint(document: Document, x: number, y: number) {
  const caretDocument = document as CaretPositionDocument;
  const range = document.caretRangeFromPoint?.(x, y);
  if (range) {
    return range;
  }

  const position = caretDocument.caretPositionFromPoint?.(x, y);
  if (!position) {
    return undefined;
  }

  const positionRange = document.createRange();
  positionRange.setStart(position.offsetNode, position.offset);
  positionRange.collapse(true);

  return positionRange;
}

function getTextNode(node: Node, offset: number): Text | undefined {
  if (node instanceof Text) {
    return node;
  }

  const childNode = node.childNodes.item(Math.max(0, offset - 1)) || node.childNodes.item(offset);
  if (childNode instanceof Text) {
    return childNode;
  }

  return undefined;
}

function getRangeRect(range: Range) {
  const rects = range.getClientRects();
  return rects.length ? rects[0] : range.getBoundingClientRect();
}

function isSingleWord(value: string) {
  const matches = [...value.matchAll(/[\p{L}\p{M}\p{N}'-]+/gu)];
  return matches.length === 1 && matches[0][0] === value;
}

function getWordBounds(text: string, offset: number) {
  const segmentBounds = getSegmentedWordBounds(text, offset);
  if (segmentBounds) {
    return segmentBounds;
  }

  const wordRegex = /[\p{L}\p{M}\p{N}'-]+/gu;
  const clampedOffset = Math.max(0, Math.min(offset, text.length));

  for (const match of text.matchAll(wordRegex)) {
    const start = match.index || 0;
    const end = start + match[0].length;
    if (
      (clampedOffset >= start && clampedOffset <= end) ||
      (clampedOffset - 1 >= start && clampedOffset - 1 < end)
    ) {
      return {start, end};
    }
  }

  return undefined;
}

function getSegmentedWordBounds(text: string, offset: number) {
  if (!('Segmenter' in Intl)) {
    return undefined;
  }

  const segmenter = new Intl.Segmenter(document.documentElement.lang || navigator.language, {
    granularity: 'word'
  });
  const clampedOffset = Math.max(0, Math.min(offset, text.length));

  for (const segmentData of segmenter.segment(text)) {
    if (!segmentData.isWordLike) {
      continue;
    }

    const start = segmentData.index;
    const end = start + segmentData.segment.length;
    if (
      (clampedOffset >= start && clampedOffset <= end) ||
      (clampedOffset - 1 >= start && clampedOffset - 1 < end)
    ) {
      return {start, end};
    }
  }

  return undefined;
}
