"use client";

import gsap from "gsap";
import { Mesh, Program, Renderer, Texture, Triangle } from "ogl";
import { useCallback, useEffect, useRef, useState } from "react";
import SplitText from "./SplitText";

export interface MorphSlide {
  image: string;
  alt: string;
  eyebrow: string;
  titleLines: readonly [string, string];
  summary: string;
  href: string;
  ctaLabel: string;
}

export interface MorphSliderProps {
  items: MorphSlide[];
  startIndex?: number;
  transition?: "melt" | "ripple" | "shear" | "swirl";
  duration?: number;
  intensity?: number;
  aberration?: number;
  drift?: number;
  autoplay?: boolean;
  autoplayDelay?: number;
  loop?: boolean;
  radius?: number;
  className?: string;
}

type MorphRenderState = "loading" | "poster" | "webgl-ready" | "webgl-failed";

const vertex = `
attribute vec2 position;
attribute vec2 uv;
varying vec2 vUv;
void main() { vUv = uv; gl_Position = vec4(position, 0.0, 1.0); }
`;

const fragment = `
precision highp float;
uniform sampler2D tCurrent;
uniform sampler2D tNext;
uniform vec2 uResolution;
uniform vec2 uCurrentSize;
uniform vec2 uNextSize;
uniform float uProgress;
uniform float uIntensity;
uniform float uAberration;
uniform float uDrift;
uniform float uTime;
varying vec2 vUv;
const float PI = 3.14159265359;

float hash21(vec2 p) {
  p = fract(p * vec2(123.34, 456.21));
  p += dot(p, p + 45.32);
  return fract(p.x * p.y);
}
float noise(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  f = f * f * (3.0 - 2.0 * f);
  float a = hash21(i), b = hash21(i + vec2(1.0, 0.0));
  float c = hash21(i + vec2(0.0, 1.0)), d = hash21(i + vec2(1.0, 1.0));
  return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}
vec2 cover(vec2 uv, vec2 resolution, vec2 imageSize) {
  float canvasRatio = resolution.x / max(resolution.y, 1.0);
  float imageRatio = imageSize.x / max(imageSize.y, 1.0);
  vec2 scale = vec2(1.0);
  if (canvasRatio > imageRatio) scale.y = imageRatio / canvasRatio;
  else scale.x = canvasRatio / imageRatio;
  return (uv - .5) * scale + .5;
}
void main() {
  float p = clamp(uProgress, 0.0, 1.0);
  float envelope = sin(p * PI);
  vec2 uv = vUv;
  uv += vec2(sin(uTime * .2 + uv.y * 5.0), cos(uTime * .23 + uv.x * 4.0)) * uDrift * .006;
  vec2 field = vec2(noise(uv * 4.4 + uTime * .02), noise(uv * 7.1 - uTime * .015)) - .5;
  vec2 currentUv = uv + field * uIntensity * .32 * p;
  vec2 nextUv = uv - field * uIntensity * .32 * (1.0 - p);
  float mask = smoothstep(noise(uv * 3.0 + uTime * .02) - .18, noise(uv * 3.0 + uTime * .02) + .18, p);
  float split = uAberration * envelope * .018;
  vec2 c = cover(currentUv, uResolution, uCurrentSize);
  vec2 n = cover(nextUv, uResolution, uNextSize);
  vec3 current = vec3(texture2D(tCurrent, c + vec2(split, 0.0)).r, texture2D(tCurrent, c).g, texture2D(tCurrent, c - vec2(split, 0.0)).b);
  vec3 next = vec3(texture2D(tNext, n + vec2(split, 0.0)).r, texture2D(tNext, n).g, texture2D(tNext, n - vec2(split, 0.0)).b);
  vec3 color = mix(current, next, mask);
  float vignette = smoothstep(1.0, .2, length(uv - .5));
  color *= .94 + vignette * .06;
  gl_FragColor = vec4(color, 1.0);
}
`;

type EngineOptions = {
  duration: number;
  intensity: number;
  aberration: number;
  drift: number;
  loop: boolean;
  onIndexChange: (index: number) => void;
  onReady: () => void;
  onFallback: () => void;
  onTransitionChange: (transitioning: boolean) => void;
};

class MorphEngine {
  private readonly items: MorphSlide[];
  private readonly options: EngineOptions;
  private readonly renderer: Renderer;
  private readonly gl: Renderer["gl"];
  private readonly canvas: HTMLCanvasElement;
  private readonly program: Program;
  private readonly mesh: Mesh;
  private readonly textures: Texture[];
  private readonly sizes: Array<[number, number]>;
  private readonly available: boolean[];
  private readonly resizeObserver: ResizeObserver;
  private current: number;
  private tween: gsap.core.Tween | null = null;
  private raf: number | null = null;
  private paused = false;
  private animating = false;
  private ready = false;
  private destroyed = false;

  constructor(container: HTMLDivElement, items: MorphSlide[], startIndex: number, options: EngineOptions) {
    this.items = items;
    this.options = options;
    this.current = startIndex;
    this.renderer = new Renderer({
      alpha: true,
      // The campaign artwork is intentionally soft and motion-blurred, so a
      // 1:1 backing buffer preserves the look while avoiding millions of
      // unnecessary fragment-shader calculations on high-DPI screens.
      antialias: false,
      dpr: 1,
    });
    this.gl = this.renderer.gl;
    this.gl.clearColor(0, 0, 0, 0);
    this.canvas = this.gl.canvas as HTMLCanvasElement;
    this.canvas.className = "morph-slider__canvas";
    this.canvas.setAttribute("aria-hidden", "true");
    this.canvas.addEventListener("webglcontextlost", this.onContextLost);
    container.appendChild(this.canvas);

    this.textures = items.map(() => this.fallbackTexture());
    this.sizes = items.map(() => [1, 1]);
    this.available = items.map(() => false);
    this.program = new Program(this.gl, {
      vertex,
      fragment,
      uniforms: {
        tCurrent: { value: this.textures[startIndex] },
        tNext: { value: this.textures[startIndex] },
        uResolution: { value: [1, 1] },
        uCurrentSize: { value: this.sizes[startIndex] },
        uNextSize: { value: this.sizes[startIndex] },
        uProgress: { value: 0 },
        uIntensity: { value: options.intensity },
        uAberration: { value: options.aberration },
        uDrift: { value: options.drift },
        uTime: { value: 0 },
      },
    });
    this.mesh = new Mesh(this.gl, { geometry: new Triangle(this.gl), program: this.program });
    this.resizeObserver = new ResizeObserver(this.resize);
    this.resizeObserver.observe(container);
    this.resize();
    this.loadTextures();
    this.requestFrame();
  }

  private readonly onContextLost = (event: Event) => {
    event.preventDefault();
    this.options.onFallback();
  };

  private readonly resize = () => {
    const rect = this.canvas.parentElement?.getBoundingClientRect();
    if (!rect) return;
    this.renderer.setSize(Math.max(rect.width, 1), Math.max(rect.height, 1));
    this.program.uniforms.uResolution.value = [this.canvas.width, this.canvas.height];
    this.requestFrame();
  };

  private requestFrame() {
    if (this.destroyed || this.paused || this.raf !== null) return;
    this.raf = requestAnimationFrame(this.frame);
  }

  private readonly frame = (time: number) => {
    this.raf = null;
    if (this.destroyed || this.paused) return;
    this.program.uniforms.uTime.value = time / 1000;
    this.renderer.render({ scene: this.mesh });
    if (!this.ready && this.available[this.current]) {
      this.ready = true;
      requestAnimationFrame(() => this.options.onReady());
    }
    // Keep rendering at full frame rate only while the melt transition is
    // changing. Once the slide settles, retain the rendered frame and let the
    // browser scroll without a full-canvas WebGL pass on every animation tick.
    if (this.animating) this.requestFrame();
  };

  private fallbackTexture() {
    const data = new Uint8Array([255, 255, 255, 0, 255, 255, 255, 0, 255, 255, 255, 0, 255, 255, 255, 0]);
    return new Texture(this.gl, { image: data, width: 2, height: 2, generateMipmaps: false });
  }

  private loadTextures() {
    let completed = 0;
    const finish = () => {
      completed += 1;
      if (completed !== this.items.length || this.destroyed) return;
      if (!this.available.some(Boolean)) this.options.onFallback();
      if (!this.available[this.current]) {
        const firstAvailable = this.available.findIndex(Boolean);
        if (firstAvailable >= 0) this.setCurrentTexture(firstAvailable);
      }
    };

    this.items.forEach((item, index) => {
      const image = new Image();
      image.decoding = "async";
      image.onload = () => {
        if (this.destroyed) return;
        const texture = new Texture(this.gl, { generateMipmaps: false });
        texture.image = image;
        this.textures[index] = texture;
        this.sizes[index] = [image.naturalWidth || 1, image.naturalHeight || 1];
        this.available[index] = true;
        if (index === this.current) this.setCurrentTexture(index);
        finish();
      };
      image.onerror = finish;
      image.src = item.image;
    });
  }

  private setCurrentTexture(index: number) {
    this.current = index;
    this.program.uniforms.tCurrent.value = this.textures[index];
    this.program.uniforms.tNext.value = this.textures[index];
    this.program.uniforms.uCurrentSize.value = this.sizes[index];
    this.program.uniforms.uNextSize.value = this.sizes[index];
    this.options.onIndexChange(index);
    this.requestFrame();
  }

  private findAvailable(from: number, direction: number) {
    for (let step = 1; step <= this.items.length; step += 1) {
      const candidate = from + direction * step;
      if (!this.options.loop && (candidate < 0 || candidate >= this.items.length)) return -1;
      const bounded = (candidate + this.items.length) % this.items.length;
      if (this.available[bounded]) return bounded;
    }
    return -1;
  }

  setPaused(value: boolean) {
    this.paused = value;
    if (value && this.raf !== null) {
      cancelAnimationFrame(this.raf);
      this.raf = null;
    }
    if (!value) this.requestFrame();
  }

  next() {
    const target = this.findAvailable(this.current, 1);
    if (target >= 0) this.goTo(target);
  }

  prev() {
    const target = this.findAvailable(this.current, -1);
    if (target >= 0) this.goTo(target);
  }

  goTo(target: number) {
    if (this.animating || target === this.current || !this.available[target]) return;
    this.animating = true;
    this.options.onTransitionChange(true);
    this.program.uniforms.tNext.value = this.textures[target];
    this.program.uniforms.uNextSize.value = this.sizes[target];
    this.tween?.kill();
    this.requestFrame();
    this.tween = gsap.fromTo(
      this.program.uniforms.uProgress,
      { value: 0 },
      {
        value: 1,
        duration: this.options.duration,
        ease: "power2.inOut",
        onComplete: () => {
          this.current = target;
          this.program.uniforms.tCurrent.value = this.textures[target];
          this.program.uniforms.uCurrentSize.value = this.sizes[target];
          this.program.uniforms.uProgress.value = 0;
          this.animating = false;
          this.options.onIndexChange(target);
          this.options.onTransitionChange(false);
          this.requestFrame();
        },
      },
    );
  }

  destroy() {
    this.destroyed = true;
    this.tween?.kill();
    if (this.raf !== null) cancelAnimationFrame(this.raf);
    this.resizeObserver.disconnect();
    this.canvas.removeEventListener("webglcontextlost", this.onContextLost);
    this.canvas.remove();
    this.gl.getExtension("WEBGL_lose_context")?.loseContext();
  }
}

export default function MorphSlider({
  items,
  startIndex = 0,
  duration = 1.15,
  intensity = 0.42,
  aberration = 0.18,
  drift = 0.25,
  autoplay = true,
  autoplayDelay = 4.8,
  loop = true,
  radius = 26,
  className = "",
}: MorphSliderProps) {
  const stageRef = useRef<HTMLDivElement>(null);
  const engineRef = useRef<MorphEngine | null>(null);
  const dragRef = useRef({ startX: 0, active: false, moved: false });
  const interactionRef = useRef({ hovered: false, focused: false });
  const fallbackTimerRef = useRef<number | null>(null);
  const initialIndex = Math.min(Math.max(startIndex, 0), Math.max(items.length - 1, 0));
  const [index, setIndex] = useState(initialIndex);
  const [renderState, setRenderState] = useState<MorphRenderState>("loading");
  const [paused, setPaused] = useState(false);
  const [transitioning, setTransitioning] = useState(false);
  const [reducedMotion, setReducedMotion] = useState(false);

  const fallback = renderState === "webgl-failed" || reducedMotion;

  useEffect(() => {
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const update = () => setReducedMotion(query.matches);
    update();
    query.addEventListener("change", update);
    return () => query.removeEventListener("change", update);
  }, []);

  useEffect(() => {
    if (!items.length || reducedMotion) return;
    const stage = stageRef.current;
    if (!stage) return;
    try {
      const engine = new MorphEngine(stage, items, initialIndex, {
        duration,
        intensity,
        aberration,
        drift,
        loop,
        onIndexChange: setIndex,
        onReady: () => setRenderState("webgl-ready"),
        onFallback: () => setRenderState("webgl-failed"),
        onTransitionChange: setTransitioning,
      });
      engineRef.current = engine;
      const observer = new IntersectionObserver(([entry]) => engine.setPaused(!entry.isIntersecting), {
        threshold: 0.05,
      });
      observer.observe(stage);
      const onVisibility = () => engine.setPaused(document.hidden);
      document.addEventListener("visibilitychange", onVisibility);
      return () => {
        observer.disconnect();
        document.removeEventListener("visibilitychange", onVisibility);
        engine.destroy();
        engineRef.current = null;
      };
    } catch {
      setRenderState("webgl-failed");
      return undefined;
    }
  }, [aberration, drift, duration, initialIndex, intensity, items, loop, reducedMotion]);

  const fallbackGoTo = useCallback(
    (target: number) => {
      if (transitioning || target === index) return;
      setTransitioning(true);
      setIndex(target);
      if (fallbackTimerRef.current !== null) window.clearTimeout(fallbackTimerRef.current);
      fallbackTimerRef.current = window.setTimeout(() => setTransitioning(false), reducedMotion ? 1 : 320);
    },
    [index, reducedMotion, transitioning],
  );

  const goTo = useCallback(
    (target: number) => {
      if (!items.length || transitioning) return;
      const bounded = loop
        ? (target + items.length) % items.length
        : Math.max(0, Math.min(target, items.length - 1));
      if (fallback) fallbackGoTo(bounded);
      else engineRef.current?.goTo(bounded);
    },
    [fallback, fallbackGoTo, items.length, loop, transitioning],
  );

  const previous = useCallback(() => goTo(index - 1), [goTo, index]);
  const next = useCallback(() => goTo(index + 1), [goTo, index]);

  useEffect(() => {
    if (!autoplay || paused || transitioning || items.length < 2) return;
    const interval = window.setInterval(next, Math.max(autoplayDelay, 1) * 1000);
    return () => window.clearInterval(interval);
  }, [autoplay, autoplayDelay, items.length, next, paused, transitioning]);

  useEffect(
    () => () => {
      if (fallbackTimerRef.current !== null) window.clearTimeout(fallbackTimerRef.current);
    },
    [],
  );

  const active = items[index] ?? items[0];
  if (!active) return null;

  return (
    <section
      className={`morph-slider morph-slider--${renderState} ${className}`}
      style={{ borderRadius: `${radius}px` }}
      aria-label="Morrow 本期广告"
      onPointerEnter={() => {
        interactionRef.current.hovered = true;
        setPaused(true);
      }}
      onPointerLeave={() => {
        interactionRef.current.hovered = false;
        setPaused(interactionRef.current.focused);
        dragRef.current = { startX: 0, active: false, moved: false };
      }}
      onFocusCapture={() => {
        interactionRef.current.focused = true;
        setPaused(true);
      }}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) {
          interactionRef.current.focused = false;
          setPaused(interactionRef.current.hovered);
        }
      }}
      onKeyDown={(event) => {
        if (event.key === "ArrowLeft") {
          event.preventDefault();
          previous();
        }
        if (event.key === "ArrowRight") {
          event.preventDefault();
          next();
        }
      }}
    >
      <img
        key={active.image}
        className="morph-slider__poster"
        src={active.image}
        alt={active.alt}
        draggable={false}
        onLoad={() => setRenderState((state) => (state === "loading" ? "poster" : state))}
        onError={() => setRenderState("webgl-failed")}
      />
      <div
        ref={stageRef}
        className="morph-slider__stage"
        role="group"
        aria-roledescription="carousel"
        aria-label="广告图片轮播"
        onPointerDown={(event) => {
          if (transitioning) return;
          dragRef.current = { startX: event.clientX, active: true, moved: false };
          event.currentTarget.setPointerCapture?.(event.pointerId);
          setPaused(true);
        }}
        onPointerMove={(event) => {
          if (!dragRef.current.active) return;
          if (Math.abs(event.clientX - dragRef.current.startX) > 6) dragRef.current.moved = true;
        }}
        onPointerUp={(event) => {
          if (dragRef.current.active && dragRef.current.moved) {
            const distance = event.clientX - dragRef.current.startX;
            if (Math.abs(distance) > 45) distance > 0 ? previous() : next();
          }
          dragRef.current = { startX: 0, active: false, moved: false };
          event.currentTarget.releasePointerCapture?.(event.pointerId);
          setPaused(interactionRef.current.hovered || interactionRef.current.focused);
        }}
        onPointerCancel={() => {
          dragRef.current = { startX: 0, active: false, moved: false };
          setPaused(interactionRef.current.hovered || interactionRef.current.focused);
        }}
      />
      <div className="morph-slider__veil" aria-hidden="true" />
      <div key={`caption-${index}-${active.image}`} className="morph-slider__caption" aria-live="polite">
        <SplitText
          as="span"
          className="morph-slider__eyebrow"
          delay={45}
          duration={0.5}
          from={{ opacity: 0, y: 14 }}
          splitType="words"
          text={active.eyebrow}
        />
        <h3 aria-label={active.titleLines.join(" ")}>
          <SplitText
            as="span"
            className="morph-slider__title-line"
            delay={38}
            duration={0.72}
            startDelay={0.08}
            text={active.titleLines[0]}
          />
          <SplitText
            as="span"
            className="morph-slider__title-line"
            delay={38}
            duration={0.72}
            startDelay={0.2}
            text={active.titleLines[1]}
          />
        </h3>
        <div className="morph-slider__support">
          <p>{active.summary}</p>
          <a href={active.href}>{active.ctaLabel} ↗</a>
        </div>
      </div>
      <button
        type="button"
        className="morph-slider__arrow morph-slider__arrow--previous"
        aria-label="上一张广告"
        onClick={previous}
        disabled={transitioning || (!loop && index === 0)}
      >
        ←
      </button>
      <button
        type="button"
        className="morph-slider__arrow morph-slider__arrow--next"
        aria-label="下一张广告"
        onClick={next}
        disabled={transitioning || (!loop && index === items.length - 1)}
      >
        →
      </button>
      <div className="morph-slider__indicators" role="tablist" aria-label="广告页码">
        {items.map((item, itemIndex) => (
          <button
            key={item.image}
            type="button"
            role="tab"
            className={itemIndex === index ? "active" : ""}
            aria-label={`查看第 ${itemIndex + 1} 张广告`}
            aria-selected={itemIndex === index}
            disabled={transitioning}
            onClick={() => goTo(itemIndex)}
          >
            <span>{itemIndex + 1}</span>
          </button>
        ))}
      </div>
      <span className="sr-only" aria-live="polite">
        第 {index + 1} 张，共 {items.length} 张
      </span>
    </section>
  );
}
