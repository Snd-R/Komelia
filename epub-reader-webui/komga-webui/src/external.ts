import {BookDto} from "@/types/komga-books";
import {R2Progression} from "@/types/readium";
import {SeriesDto} from "@/types/komga-series";
import {ReadListDto} from "@/types/komga-readlists";
import {RequestConfig} from "@d-i-t-a/reader/dist/types/navigator/IFrameNavigator";
import {EpubReaderSettings} from "@/types/epub-reader-settings";

declare global {
    interface Window {
        resourceBaseUrl: string,

        bookId: () => Promise<CallbackResponse<string>>
        incognito: () => Promise<CallbackResponse<boolean>>
        bookGet: (bookId: string) => Promise<CallbackResponse<BookDto>>
        bookGetProgression: (bookId: string) => Promise<CallbackResponse<R2Progression | undefined>>
        bookUpdateProgression: (type: {
            bookId: string,
            progression: R2Progression
        }) => Promise<CallbackResponse<undefined>>
        bookGetBookSiblingNext: (bookId: string) => Promise<CallbackResponse<BookDto>>
        bookGetBookSiblingPrevious: (bookId: string) => Promise<CallbackResponse<BookDto>>
        getOneSeries: (seriesId: string) => Promise<CallbackResponse<SeriesDto>>

        readListGetOne: (readListId: string) => Promise<CallbackResponse<ReadListDto>>
        // readListGetBookSiblingNext: (readListId: string, bookId: string) => Promise<BookDto>
        // readListGetBookSiblingPrevious: (readListId: string, bookId: string) => Promise<BookDto>

        d2ReaderGetContent: (href: string) => Promise<CallbackResponse<string>>;
        d2ReaderGetContentBytesLength: (href: string, requestConfig?: RequestConfig) => Promise<CallbackResponse<number>>;

        getReaderSettings: () => Promise<CallbackResponse<EpubReaderSettings>>;
        setReaderSettings: (settings: EpubReaderSettings) => Promise<CallbackResponse<undefined>>;

        closeBook: () => Promise<CallbackResponse<undefined>>;

        getPublication: (bookId: string) => Promise<CallbackResponse<any>>
        externalFetch: (href: string) => Promise<CallbackResponse<string>>;
        originalFetch: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

        getServerUrl: () => Promise<CallbackResponse<string>>
        getSettings: () => Promise<CallbackResponse<EpubReaderSettings>>
        saveSettings: (settings: EpubReaderSettings) => Promise<CallbackResponse<undefined>>

        isFullscreenAvailable: () => Promise<CallbackResponse<boolean>>
        toggleFullscreen: () => Promise<CallbackResponse<undefined>>
    }
}

export interface CallbackResponse<T> {
    result: T
}

export default class ExternalFunctions {
    async getInitialBookId(): Promise<string> {
        return this.callbackResult(window.bookId())
    }

    async isIncognito(): Promise<boolean> {
        return this.callbackResult(window.incognito())
    }

    async bookGet(bookId: string): Promise<BookDto> {
        return this.callbackResult(window.bookGet(bookId))
    }

    async bookGetProgression(bookId: string): Promise<R2Progression | undefined> {
        return this.callbackResult(window.bookGetProgression(bookId))
    }

    async bookUpdateProgression(bookId: string, progression: R2Progression): Promise<undefined> {
        return this.callbackResult(window.bookUpdateProgression({bookId: bookId, progression: progression}))
    }

    async bookGetBookSiblingNext(bookId: string): Promise<BookDto> {
        return this.callbackResult(window.bookGetBookSiblingNext(bookId))
    }

    async bookGetBookSiblingPrevious(bookId: string): Promise<BookDto> {
        return this.callbackResult(window.bookGetBookSiblingPrevious(bookId))
    }

    async getOneSeries(seriesId: string): Promise<SeriesDto> {
        return this.callbackResult(window.getOneSeries(seriesId))
    }

    async readListGetOne(readListId: string): Promise<ReadListDto> {
        return this.callbackResult(window.readListGetOne(readListId))
    }

    async d2ReaderGetContent(href: string): Promise<string> {
        return window.d2ReaderGetContent(href).then((value) => value.result)
    }

    async d2ReaderGetContentBytesLength(href: string, requestConfig?: RequestConfig): Promise<number> {
        return window.d2ReaderGetContentBytesLength(href, requestConfig).then((value) => value.result)
    }

    async closeBook(): Promise<undefined> {
        return window.closeBook().then((value) => value.result)
    }

    async getReaderSettings(): Promise<EpubReaderSettings> {
        return window.getSettings().then((value) => value.result)
    }

    async saveReaderSettings(settings: EpubReaderSettings): Promise<undefined> {
        return window.saveSettings(settings).then((value) => value.result)
    }

    async externalFetch(href: string, requestConfig?: RequestConfig): Promise<number> {
        return this.callbackResult(window.d2ReaderGetContentBytesLength(href, requestConfig))
    }

    async getPublication(bookId: string): Promise<any> {
        return window.getPublication(bookId).then((value) => value.result)
    }

    async getServerUrl(): Promise<string> {
        return window.getServerUrl().then((value) => value.result)
    }

    async isFullscreenAvailable(): Promise<boolean> {
        return window.isFullscreenAvailable().then((value) => value.result)
    }

    async toggleFullscreen(): Promise<undefined> {
        return window.toggleFullscreen().then((value) => value.result)
    }

    async callbackResult<T>(promise: Promise<CallbackResponse<T>>): Promise<T> {
        return promise
            .then((value) => value.result)
    }
}

window.originalFetch = window.fetch
window.fetch = function (resource, init) {
    init = Object.assign({}, init)
    if (typeof resource == 'string') {
        return window.externalFetch(resource).then((value) => new Response(value.result))
    } else {
        return window.originalFetch(resource, init)
    }
}
