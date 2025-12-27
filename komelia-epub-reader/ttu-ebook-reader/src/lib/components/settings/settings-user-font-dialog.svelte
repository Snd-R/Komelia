<script lang="ts">
  import DialogTemplate from '$lib/components/dialog-template.svelte';
  import SvelteUserFontAdd from '$lib/components/settings/settings-user-font-add.svelte';
  import {dialogManager} from '$lib/data/dialog-manager';
  import {logger} from '$lib/data/logger';
  import {userFonts$} from '$lib/data/store';
  import {dummyFn} from '$lib/functions/utils';
  import {faSpinner, faTrashCan} from '@fortawesome/free-solid-svg-icons';
  import type {BehaviorSubject} from 'rxjs';
  import {type Font, loadFont, type UserFont} from "$lib/data/fonts";
  import Fa from "svelte-fa";
  import {externalFunctions} from "$lib/external";

  interface Props {
    currentFont: BehaviorSubject<Font>;
    defaultFont: Font;
  }

  let {currentFont, defaultFont}: Props = $props();

  const tabs = ['Stored', 'Add'];

  let isLoading = $state(false);
  let currentTab = $state('Stored');

  let userFonts = $derived($userFonts$.toSorted((a, b) => a.fileName.localeCompare(b.fileName)))

  function selectFont(font: UserFont) {
    loadFont(font)
    currentFont.next(font);
    dialogManager.dialogs$.next([]);
  }

  async function removeFont(font: UserFont) {

    isLoading = true;

    try {
      $userFonts$ = $userFonts$.filter((userFont) => userFont !== font);
      const currentFontName = currentFont.getValue();

      if (!$userFonts$.find((userFont) => userFont === currentFontName)) {
        currentFont.next(defaultFont);
      }

      await externalFunctions.removeStoredFont(font)
    } catch (error: any) {
      logger.error(`Error deleting Font: ${error.message}`);
    }

    isLoading = false;
  }
</script>

<DialogTemplate>
  <div slot="content">
    <div class="border-b border-b-gray-200">
      <ul class="-mb-px flex items-center gap-4 text-sm font-medium">
        {#each tabs as tab (tab)}
          <li class="flex-1">
            <button
                class="relative flex items-center justify-center gap-2 px-1 py-3 hover:text-blue-700"
                class:text-blue-700={currentTab === tab}
                class:after:absolute={currentTab === tab}
                class:after:left-0={currentTab === tab}
                class:after:bottom-0={currentTab === tab}
                class:after:h-0.5={currentTab === tab}
                class:after:w-full={currentTab === tab}
                class:after:bg-blue-700={currentTab === tab}
                class:text-gray-500={currentTab !== tab}
                onclick={() => (currentTab = tab)}
            >
              {tab}
            </button>
          </li>
        {/each}
      </ul>
    </div>
    <div class="mt-5">
      {#if currentTab === 'Stored'}
        {#if $userFonts$.length}
          <div
              class="grid grid-cols-[repeat(2,auto)] items-center gap-y-4 gap-x-4 max-h-[50vh] overflow-auto break-all md:gap-x-14"
          >
            {#each userFonts as userFont}
              <div
                  tabindex="0"
                  role="button"
                  title="Click to select Font"
                  class="hover:text-blue-700"
                  onclick={() => selectFont(userFont)}
                  onkeyup={dummyFn}
              >
                {userFont.displayName} / {userFont.fileName}
              </div>
              <div
                  tabindex="0"
                  role="button"
                  title="Remove Font"
                  class="hover:text-blue-700"
                  onclick={() => removeFont(userFont)}
                  onkeyup={dummyFn}
              >
                <Fa icon={faTrashCan}/>
              </div>
            {/each}
          </div>
        {:else}
          <div>You have currently no stored Fonts</div>
        {/if}
      {:else}
        <SvelteUserFontAdd bind:isLoading/>
      {/if}
    </div>
    {#if isLoading}
      <div class="fixed inset-0 flex h-full w-full items-center justify-center text-7xl">
        <Fa icon={faSpinner} spin/>
      </div>
    {/if}
  </div>
</DialogTemplate>
