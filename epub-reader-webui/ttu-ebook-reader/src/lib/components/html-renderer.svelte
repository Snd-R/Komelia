<script lang="ts">

  import {onMount, tick} from "svelte";

  interface Props {
    html: string
    onLoad: () => void;
  }

  let {html, onLoad}: Props = $props()

  $effect.pre(() => {
    // no way to watch single variable except for this???
    html

    tick().then(() => onLoad())
  });
</script>

{@html html}
