/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import {getCharacterCount} from './get-character-count';
import {writableSubject} from '$lib/functions/svelte/store';

function externalTargetFilterFunction(element: HTMLElement) {
  return getCharacterCount(element) > 0;
}

export function isMobile(window: Window) {
  const UA = window.navigator.userAgent;
  const userAgentRegex = /\b(BlackBerry|webOS|iPhone|IEMobile|Android|Windows Phone|iPad|iPod)\b/i;

  if (('maxTouchPoints' in window.navigator) as any) {
    return window.navigator.maxTouchPoints > 0;
  }

  if (('msMaxTouchPoints' in window.navigator) as any) {
    return window.navigator.msMaxTouchPoints > 0;
  }

  const mQ = window.matchMedia?.('(pointer:coarse)');
  if (mQ?.media === '(pointer: coarse)') {
    return !!mQ.matches;
  }

  if ('orientation' in window) {
    return true;
  }
  return userAgentRegex.test(UA);
}

export function dummyFn() {}

export const isMobile$ = writableSubject<boolean>(false);

export function getWeightedAverage(values: number[], weights: number[]) {
  let sum = 0;
  let weightedSum = 0;

  for (let index = 0, { length } = values; index < length; index += 1) {
    sum += values[index] * weights[index];
    weightedSum += weights[index];
  }

  return sum / weightedSum;
}

export function getExternalTargetElement(
  source: Document | Element,
  selector: string,
  uselast = true
) {
  const elements = [...source.querySelectorAll<HTMLSpanElement>(selector)].filter(
    externalTargetFilterFunction
  );

  return uselast ? elements[elements.length - 1] : elements[0];
}
