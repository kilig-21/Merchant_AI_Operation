<script setup lang="ts">
import { computed, ref } from 'vue'
import extractColors from '@wenhaoqi/wasm_design_utils/extract-colors'
import { usePointerPosition } from '../composables/usePointerPosition'

const props = withDefaults(defineProps<{
  src: string
  alt: string
  tone?: string
  eager?: boolean
}>(), { tone: '#eceae5', eager: false })

const accent = ref(props.tone)
const container = ref<HTMLElement | null>(null)
const mediaStyle = computed(() => ({ '--media-tone': accent.value }))
usePointerPosition({ container })

async function pickAccent(event: Event) {
  const image = event.currentTarget as HTMLImageElement
  try {
    const colors = await extractColors(image, { pixels: 12000 })
    const selected = colors.find((color) => color.saturation > 0.09 && color.lightness > 0.2 && color.lightness < 0.84) ?? colors[0]
    if (selected) accent.value = selected.hex
  } catch {
    // Cross-origin images can opt out of canvas reads; the authored fallback tone remains.
  }
}
</script>

<template>
  <div ref="container" class="product-media" :style="mediaStyle">
    <img :src="src" :alt="alt" :loading="eager ? 'eager' : 'lazy'" crossorigin="anonymous" @load="pickAccent" />
    <slot />
  </div>
</template>

<style scoped>
.product-media {
  position: relative;
  overflow: hidden;
  background: color-mix(in srgb, var(--media-tone) 21%, #f5f5f7);
  isolation: isolate;
}
.product-media::before {
  position: absolute;
  z-index: 2;
  inset: 0;
  pointer-events: none;
  content: '';
  opacity: 0;
  background: radial-gradient(360px circle at var(--pointer-x, 50%) var(--pointer-y, 50%), rgba(255,255,255,.2), transparent 68%);
  transition: opacity .35s;
}
.product-media:hover::before { opacity: 1; }
.product-media::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 34%;
  pointer-events: none;
  background: linear-gradient(transparent, color-mix(in srgb, var(--media-tone) 16%, transparent));
}
.product-media img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 900ms cubic-bezier(.2,.7,.2,1);
}
.product-media:hover img { transform: scale(1.025) translate(calc(var(--pointer-nx, 0) * 3px), calc(var(--pointer-ny, 0) * 3px)); }
@media (pointer: coarse), (prefers-reduced-motion: reduce) {
  .product-media::before { display: none; }
  .product-media:hover img { transform: none; }
}
</style>
