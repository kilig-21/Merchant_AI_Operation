<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'

const videoUrl = 'https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260715_090628_7052d8a6-a094-4341-a4a2-ad58493a67a9.mp4'
defineProps<{ poster: string }>()
const video = ref<HTMLVideoElement | null>(null)
const displayCanvas = ref<HTMLCanvasElement | null>(null)
const frames: ImageData[] = []
const captureCanvas = document.createElement('canvas')
const captureContext = captureCanvas.getContext('2d', { willReadFrequently: true })
const showCanvas = ref(false)
let capturing = false
let lastTime = -1
let animationFrame = 0
let playbackTimer = 0
let frameIndex = 0
let direction = 1

function captureFrame() {
  const element = video.value
  if (!element || !captureContext || element.currentTime === lastTime || frames.length >= 240) return
  const width = Math.min(960, element.videoWidth)
  const height = Math.max(1, Math.round(width * (element.videoHeight / element.videoWidth)))
  if (!width || !height) return

  captureCanvas.width = width
  captureCanvas.height = height
  captureContext.drawImage(element, 0, 0, width, height)
  try {
    frames.push(captureContext.getImageData(0, 0, width, height))
    lastTime = element.currentTime
  } catch {
    capturing = false
  }
}

function requestNextFrame() {
  if (!capturing || !video.value) return
  const frameVideo = video.value as HTMLVideoElement & { requestVideoFrameCallback?: (callback: () => void) => number }
  if (frameVideo.requestVideoFrameCallback) {
    frameVideo.requestVideoFrameCallback(() => {
      captureFrame()
      requestNextFrame()
    })
  } else {
    animationFrame = requestAnimationFrame(() => {
      captureFrame()
      requestNextFrame()
    })
  }
}

async function startCapture() {
  if (!video.value) return
  frames.length = 0
  lastTime = -1
  capturing = true
  try {
    await video.value.play()
    requestNextFrame()
  } catch {
    capturing = false
  }
}

function drawDisplayFrame() {
  const canvas = displayCanvas.value
  const image = frames[frameIndex]
  if (!canvas || !image) return
  if (canvas.width !== image.width || canvas.height !== image.height) {
    canvas.width = image.width
    canvas.height = image.height
  }
  canvas.getContext('2d')?.putImageData(image, 0, 0)
}

function beginBoomerang() {
  capturing = false
  cancelAnimationFrame(animationFrame)
  if (frames.length < 2) return
  frameIndex = 0
  direction = 1
  showCanvas.value = true
  drawDisplayFrame()
  playbackTimer = window.setInterval(() => {
    frameIndex += direction
    if (frameIndex >= frames.length - 1) {
      frameIndex = frames.length - 1
      direction = -1
    } else if (frameIndex <= 0) {
      frameIndex = 0
      direction = 1
    }
    drawDisplayFrame()
  }, 1000 / 30)
}

onBeforeUnmount(() => {
  capturing = false
  cancelAnimationFrame(animationFrame)
  window.clearInterval(playbackTimer)
})
</script>

<template>
  <div class="boomerang-video" :style="{ backgroundImage: `url(${poster})` }" aria-hidden="true">
    <video ref="video" :src="videoUrl" :poster="poster" muted playsinline preload="auto" crossorigin="anonymous" :class="{ 'is-hidden': showCanvas }" @loadedmetadata="startCapture" @ended="beginBoomerang" />
    <canvas v-show="showCanvas" ref="displayCanvas" />
  </div>
</template>

<style scoped>
.boomerang-video { position: absolute; inset: 0; z-index: 0; overflow: hidden; background-position: center 58%; background-size: cover; transform: scale(1.15); transform-origin: top; }
.boomerang-video video, .boomerang-video canvas { display: block; width: 100%; height: 100%; object-fit: cover; object-position: top; }
.boomerang-video .is-hidden { display: none; }
</style>
