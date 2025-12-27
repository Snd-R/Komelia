/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import {writableSubject} from '$lib/functions/svelte/store';
import type {Component} from "svelte";

export interface Dialog {
  component: Component<any,any,any> | (new (...args: any[]) => any) | string;
  props?: Record<string, any>;
  disableCloseOnClick?: boolean;
  zIndex?: string;
}

const dialogs$ = writableSubject<Dialog[]>([]);

export const dialogManager = {
  dialogs$
};
