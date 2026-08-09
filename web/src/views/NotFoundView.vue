<template>
  <main class="not-found">
    <p class="meta-label">404 / LOST OBJECT</p>
    <h1>这一页暂时<br>不在陈列中。</h1>
    <p>也许它换了位置，或者还没有准备好与你见面。</p>
    <RouterLink class="button button--dark" to="/">回到首页</RouterLink>
  </main>
</template>

<style scoped>
.not-found{min-height:100vh;display:grid;align-content:center;justify-items:start;padding:clamp(28px,7vw,110px);background:var(--paper);background-image:radial-gradient(circle at 80% 18%,rgba(198,240,77,.7),transparent 24%)}
h1{margin:18px 0;font:500 clamp(58px,10vw,148px)/.87 var(--font-display);letter-spacing:-.075em}p:not(.meta-label){max-width:440px;color:var(--muted);line-height:1.7}.button{margin-top:24px}
</style>
