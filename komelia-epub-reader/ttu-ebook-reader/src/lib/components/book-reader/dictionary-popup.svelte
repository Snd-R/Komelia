<script lang="ts">
  import {faXmark} from '@fortawesome/free-solid-svg-icons';
  import {onDestroy, onMount} from 'svelte';
  import Fa from 'svelte-fa';

  interface DictionaryDefinition {
    partOfSpeech: string;
    definition: string;
    example?: string;
  }

  type DictionaryFetchWindow = Window &
    typeof globalThis & {
      originalFetch?: typeof fetch;
    };

  export let word: string;
  export let x: number;
  export let y: number;
  export let fontColor = '#fff';
  export let backgroundColor = '#333';
  export let onClose: () => void = () => {};

  let popupEl: HTMLElement;
  let loading = true;
  let error = '';
  let definitions: DictionaryDefinition[] = [];
  let abortController: AbortController | undefined;
  let loadedWord = '';

  $: left = Math.min(Math.max(8, x - 160), Math.max(8, window.innerWidth - 328));
  $: top = Math.min(Math.max(8, y + 12), Math.max(8, window.innerHeight - 260));
  $: if (word && word !== loadedWord) {
    loadedWord = word;
    loadDefinitions(word);
  }

  onMount(() => {
    document.addEventListener('pointerdown', handlePointerDown, true);
  });

  onDestroy(() => {
    abortController?.abort();
    document.removeEventListener('pointerdown', handlePointerDown, true);
  });

  async function loadDefinitions(value: string) {
    abortController?.abort();
    const controller = new AbortController();
    abortController = controller;
    loading = true;
    error = '';
    definitions = [];

    try {
      const response = await fetchDictionary(value, controller.signal);

      if (!response.ok) {
        throw new Error('No definition found');
      }

      const entries = await response.json();
      definitions = extractDefinitions(entries);
      if (!definitions.length) {
        throw new Error('No definition found');
      }
    } catch (err: any) {
      if (err.name !== 'AbortError') {
        error = err.message || 'Could not load definition';
      }
    } finally {
      if (!controller.signal.aborted) {
        loading = false;
      }
    }
  }

  function fetchDictionary(value: string, signal: AbortSignal): Promise<Response> {
    const dictionaryWindow = window as DictionaryFetchWindow;
    const fetchFunction = dictionaryWindow.originalFetch ?? window.fetch;
    const url = `https://api.dictionaryapi.dev/api/v2/entries/en/${
      encodeURIComponent(value.toLowerCase())
    }`;

    return fetchFunction.call(window, url, {signal, cache: 'no-store'});
  }

  function extractDefinitions(entries: any): DictionaryDefinition[] {
    if (!Array.isArray(entries)) {
      return [];
    }

    return entries
      .flatMap((entry) =>
        (entry.meanings || []).flatMap((meaning: any) =>
          (meaning.definitions || []).map((definition: any) => ({
            partOfSpeech: meaning.partOfSpeech || '',
            definition: definition.definition || '',
            example: definition.example
          }))
        )
      )
      .filter((definition) => definition.definition)
      .slice(0, 3);
  }

  function handlePointerDown(event: PointerEvent) {
    if (popupEl && event.target instanceof Node && !popupEl.contains(event.target)) {
      onClose();
    }
  }
</script>

<div
    bind:this={popupEl}
    role="dialog"
    aria-label="Dictionary"
    class="dictionary-popup writing-horizontal-tb fixed z-[70] max-h-[240px] w-[320px] max-w-[calc(100vw-16px)] overflow-auto rounded border p-3 text-sm shadow-lg"
    style:left={`${left}px`}
    style:top={`${top}px`}
    style:color={fontColor}
    style:background-color={backgroundColor}
    style:border-color={fontColor}
>
  <div class="mb-2 flex items-center justify-between gap-3">
    <div class="min-w-0 truncate text-base font-bold">{word}</div>
    <button
        type="button"
        aria-label="Close dictionary"
        class="flex h-8 w-8 shrink-0 items-center justify-center rounded"
        style:color={fontColor}
        onclick={onClose}
    >
      <Fa icon={faXmark}/>
    </button>
  </div>

  {#if loading}
    <div class="opacity-80">Loading definition...</div>
  {:else if error}
    <div class="opacity-80">{error}</div>
  {:else}
    <div class="space-y-2">
      {#each definitions as definition}
        <div>
          {#if definition.partOfSpeech}
            <div class="text-xs uppercase opacity-70">{definition.partOfSpeech}</div>
          {/if}
          <div>{definition.definition}</div>
          {#if definition.example}
            <div class="mt-1 italic opacity-80">{definition.example}</div>
          {/if}
        </div>
      {/each}
    </div>
  {/if}
</div>
