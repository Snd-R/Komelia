/**
 * @license BSD-3-Clause
 * Copyright (c) 2024, ッツ Reader Authors
 * All rights reserved.
 */

import type {ReaderSettings} from '$lib/data/store';
import type {BookData, BookmarkData} from '$lib/data/books-db';
import type {UserFont} from "$lib/data/fonts";

export interface ExternalFunctionsWindow extends Window {
  getCurrentBookId(): Promise<CallbackResponse<string>>;

  getBookData(): Promise<CallbackResponse<BookData>>;

  getSettings(): Promise<CallbackResponse<ReaderSettings>>;

  putSettings(settings: ReaderSettings): Promise<CallbackResponse<undefined>>;

  getBookmark(): Promise<CallbackResponse<BookmarkData | undefined>>;

  putBookmark(bookmarkData: BookmarkData): Promise<CallbackResponse<undefined>>;

  closeBook(): Promise<CallbackResponse<undefined>>

  getAvailableFonts(): Promise<CallbackResponse<string[]>>

  saveFont(path: string): Promise<CallbackResponse<undefined>>

  openFilePickerForFonts(): Promise<CallbackResponse<string | null>>

  saveSelectedFont(name: string): Promise<CallbackResponse<UserFont>>

  getStoredFonts(): Promise<CallbackResponse<UserFont[]>>

  removeStoredFont(font: UserFont): Promise<CallbackResponse<undefined>>

  isFullscreenAvailable(): Promise<CallbackResponse<boolean>>

  isFullscreen(): Promise<CallbackResponse<boolean>>

  setFullscreen(enabled: boolean): Promise<CallbackResponse<undefined>>

  completeBook(): Promise<CallbackResponse<undefined>>
}

interface CallbackResponse<T> {
  result: T
}

export class ExternalFunctions {
  windowFunctions = window as unknown as ExternalFunctionsWindow;

  async getCurrentBookId(): Promise<string> {
    return this.windowFunctions.getCurrentBookId().then((result) => result.result)
  }

  async getBookData(): Promise<BookData> {
    return this.windowFunctions.getBookData().then((result) => result.result)
  }

  async getSettings(): Promise<ReaderSettings> {
    return this.windowFunctions.getSettings().then((result) => result.result)
  }

  async putSettings(settings: ReaderSettings): Promise<undefined> {
    return this.windowFunctions.putSettings(settings).then((result) => result.result)
  }

  async getBookmark(): Promise<BookmarkData | undefined> {
    return this.windowFunctions.getBookmark().then((result) => result.result)
  }

  async putBookmark(bookmarkData: BookmarkData): Promise<undefined> {
    return this.windowFunctions.putBookmark(bookmarkData).then((result) => result.result)
  }

  async closeBook(): Promise<undefined> {
    return this.windowFunctions.closeBook().then((result) => result.result)
  }

  async getAvailableSystemFonts(): Promise<string[]> {
    return this.windowFunctions.getAvailableFonts().then((result) => result.result)
  }

  async saveFont(path: string): Promise<undefined> {
    return this.windowFunctions.saveFont(path).then((result) => result.result)
  }

  async openFilePickerForFonts(): Promise<string | null> {
    return this.windowFunctions.openFilePickerForFonts().then((result) => result.result)
  }

  async saveSelectedFont(name: string): Promise<UserFont> {
    return this.windowFunctions.saveSelectedFont(name).then((result) => result.result)
  }

  async getStoredFonts(): Promise<UserFont[]> {
    return this.windowFunctions.getStoredFonts().then((result) => result.result)
  }

  async removeStoredFont(font: UserFont): Promise<undefined> {
    return this.windowFunctions.removeStoredFont(font).then((result) => result.result)
  }

  async isFullscreen(): Promise<boolean> {
    return this.windowFunctions.isFullscreen().then((result) => result.result)
  }

  async enterFullscreen(): Promise<undefined> {
    return this.windowFunctions.setFullscreen(true).then((result) => result.result)

  }

  async exitFullscreen(): Promise<undefined> {
    return this.windowFunctions.setFullscreen(false).then((result) => result.result)
  }

  async isFullscreenAvailable(): Promise<boolean> {
    return this.windowFunctions.isFullscreenAvailable().then((result) => result.result)
  }

  async completeBook(): Promise<undefined> {
    return this.windowFunctions.completeBook().then((result) => result.result)
  }
}

export const externalFunctions = new ExternalFunctions()

