import { onBeforeUnmount, onMounted } from 'vue'

export function useReveal(selector = '[data-reveal]') {
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    const nodes = [...document.querySelectorAll<HTMLElement>(selector)]
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      nodes.forEach((node) => node.classList.add('is-visible'))
      return
    }

    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return
        entry.target.classList.add('is-visible')
        observer?.unobserve(entry.target)
      })
    }, { rootMargin: '0px 0px -8% 0px', threshold: .08 })

    nodes.forEach((node) => observer?.observe(node))
  })

  onBeforeUnmount(() => observer?.disconnect())
}

