import {mount} from 'svelte'
import './app.scss'
import App from './App.svelte'

const app = mount(App, {
  target: document.getElementById('app')!,
})

declare global {
  interface HTMLElement {
    scrollIntoViewIfNeeded(arg?: boolean): void;
  }
  interface Navigator {
    msMaxTouchPoints: number;
    standalone: boolean | undefined;
  }
}

export default app
