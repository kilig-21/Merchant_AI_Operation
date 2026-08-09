import { onBeforeUnmount, onMounted, type Ref } from 'vue'

interface PointerOptions {
  container?: Ref<HTMLElement | null>
  disabled?: Ref<boolean>
}

export function usePointerPosition(options: PointerOptions = {}) {
  let frame = 0
  let latestX = 0
  let latestY = 0

  function updateCssVariables() {
    frame = 0
    const element = options.container?.value ?? document.documentElement
    if (!element || options.disabled?.value) return

    const rect = element.getBoundingClientRect()
    const x = latestX - rect.left
    const y = latestY - rect.top
    element.style.setProperty('--pointer-x', `${x}px`)
    element.style.setProperty('--pointer-y', `${y}px`)
    element.style.setProperty('--pointer-nx', `${Math.max(-1, Math.min(1, (x / Math.max(1, rect.width) - .5) * 2))}`)
    element.style.setProperty('--pointer-ny', `${Math.max(-1, Math.min(1, (y / Math.max(1, rect.height) - .5) * 2))}`)
  }

  function move(event: PointerEvent) {
    latestX = event.clientX
    latestY = event.clientY
    if (!frame) frame = requestAnimationFrame(updateCssVariables)
  }

  onMounted(() => window.addEventListener('pointermove', move, { passive: true }))
  onBeforeUnmount(() => {
    window.removeEventListener('pointermove', move)
    cancelAnimationFrame(frame)
  })
}

