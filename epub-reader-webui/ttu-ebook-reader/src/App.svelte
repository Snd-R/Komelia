<script lang="ts">
  import {serifFontFamily$, loadExternalSettings, userFonts$} from '$lib/data/store';
  import {type Dialog, dialogManager} from '$lib/data/dialog-manager';
  import {dummyFn, isMobile, isMobile$} from '$lib/functions/utils';
  import SettingsContent from "$lib/components/settings/settings-content.svelte";
  import SettingsHeader from "$lib/components/settings/settings-header.svelte";
  import Reader from "$lib/components/Reader.svelte";
  import {pxScreen} from "$lib/css-classes";
  import {faSpinner} from "@fortawesome/free-solid-svg-icons";
  import {loadFont} from "$lib/data/fonts";
  import Fa from "svelte-fa";

  let showSettings = $state(false)

  let dialogs: Dialog[] = $state([]);
  let clickOnCloseDisabled = $state(false);
  let zIndex = $state('');

  let initPromise = init()

  async function init() {
    await loadExternalSettings()
    isMobile$.next(isMobile(window));

    await Promise.all($userFonts$.map((font) => loadFont(font)))
  }

  function closeAllDialogs() {
    dialogManager.dialogs$.next([]);
    clickOnCloseDisabled = false;
    zIndex = '';
  }

  dialogManager.dialogs$.subscribe((d) => {
    clickOnCloseDisabled = d[0]?.disableCloseOnClick ?? false;
    zIndex = d[0]?.zIndex ?? '';
    dialogs = d;
  });

</script>

{#if dialogs.length > 0}
  <div class="writing-horizontal-tb fixed inset-0 z-50 h-full w-full" style:z-index={zIndex}>
    <div
        tabindex="0"
        role="button"
        class="tap-highlight-transparent absolute inset-0 bg-black/[.32]"
        onclick={() => {
          if (!clickOnCloseDisabled) {
            closeAllDialogs();
          }
        }}
        onkeyup={dummyFn}
    ></div>

    <div
        class="relative top-1/2 left-1/2 inline-block max-w-[80vw] -translate-x-1/2 -translate-y-1/2"
    >
      {#each dialogs as dialog}
        {#if typeof dialog.component === 'string'}
          {@html dialog.component}
        {:else}
          <dialog.component {...dialog.component} {...dialog.props} onclose={closeAllDialogs}/>
        {/if}
      {/each}
    </div>
  </div>
{/if}
{#await initPromise}
  <div class="fixed inset-0 flex h-full w-full items-center justify-center text-7xl">
    <Fa icon={faSpinner} spin/>
  </div>
{:then _}
  {#if showSettings}
    <div class="elevation-4 fixed inset-x-0 top-0 z-10">
      <SettingsHeader onExit={() => showSettings = false}/>
    </div>

    <div class="{pxScreen} h-full pt-16 xl:pt-14">
      <div class="max-w-5xl">
        <SettingsContent/>
      </div>
    </div>
  {:else}
    <Reader
        onSettingsClick={() => showSettings=true}
    />
  {/if}

  <span style={`font-family: "${$serifFontFamily$.familyName}"`}></span>
{:catch error}
  <p style="color: red">{error.message}</p>
{/await}
