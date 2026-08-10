"use client";

import type { ReactNode, PointerEvent as ReactPointerEvent } from "react";
import { useCallback, useEffect, useRef, useState } from "react";

export interface HorizontalProductRailProps {
  label: string;
  children: ReactNode;
  scrollAmount?: number;
  showControls?: boolean;
  showProgress?: boolean;
}

export function HorizontalProductRail({
  label,
  children,
  scrollAmount = 0.82,
  showControls = true,
  showProgress = true,
}: HorizontalProductRailProps) {
  const viewportRef = useRef<HTMLDivElement>(null);
  const dragRef = useRef({
    startX: 0,
    startScroll: 0,
    pointerId: -1,
    dragging: false,
    lastScroll: 0,
    lastTime: 0,
    velocity: 0,
  });
  const suppressClickRef = useRef(false);
  const wheelEndRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [canPrevious, setCanPrevious] = useState(false);
  const [canNext, setCanNext] = useState(true);
  const [progress, setProgress] = useState(0);
  const [dragging, setDragging] = useState(false);

  const update = useCallback(() => {
    const viewport = viewportRef.current;
    if (!viewport) return;
    const maximum = Math.max(viewport.scrollWidth - viewport.clientWidth, 0);
    setCanPrevious(viewport.scrollLeft > 2);
    setCanNext(viewport.scrollLeft < maximum - 2);
    setProgress(maximum ? viewport.scrollLeft / maximum : 0);
  }, []);

  useEffect(() => {
    const viewport = viewportRef.current;
    if (!viewport) return;
    const observer = new ResizeObserver(update);
    observer.observe(viewport);
    for (const child of Array.from(viewport.children)) observer.observe(child);
    const onWheel = (event: WheelEvent) => {
      const maximum = viewport.scrollWidth - viewport.clientWidth;
      if (maximum <= 0) return;

      const rawDelta = event.shiftKey
        ? event.deltaY || event.deltaX
        : Math.abs(event.deltaX) > Math.abs(event.deltaY)
          ? event.deltaX
          : event.deltaY;
      const deltaScale =
        event.deltaMode === WheelEvent.DOM_DELTA_LINE
          ? 22
          : event.deltaMode === WheelEvent.DOM_DELTA_PAGE
            ? viewport.clientWidth
            : 1;
      const delta = rawDelta * deltaScale;
      if (!delta) return;
      const movingForward = delta > 0;
      const canMove = movingForward ? viewport.scrollLeft < maximum - 2 : viewport.scrollLeft > 2;
      if (!canMove) return;

      event.preventDefault();
      event.stopPropagation();
      viewport.dataset.wheelActive = "true";
      viewport.scrollBy({ left: delta, behavior: "auto" });

      if (wheelEndRef.current) clearTimeout(wheelEndRef.current);
      wheelEndRef.current = setTimeout(() => {
        delete viewport.dataset.wheelActive;
        wheelEndRef.current = null;
      }, 160);
    };
    viewport.addEventListener("wheel", onWheel, { passive: false, capture: true });
    update();
    return () => {
      observer.disconnect();
      viewport.removeEventListener("wheel", onWheel, { capture: true });
      if (wheelEndRef.current) clearTimeout(wheelEndRef.current);
    };
  }, [update]);

  const moveByPage = (direction: -1 | 1) => {
    const viewport = viewportRef.current;
    if (!viewport) return;
    viewport.scrollBy({ left: viewport.clientWidth * scrollAmount * direction, behavior: "smooth" });
  };

  const onPointerDown = (event: ReactPointerEvent<HTMLDivElement>) => {
    if (event.pointerType === "touch" || event.button !== 0) return;
    const viewport = viewportRef.current;
    if (!viewport) return;
    dragRef.current = {
      startX: event.clientX,
      startScroll: viewport.scrollLeft,
      pointerId: event.pointerId,
      dragging: false,
      lastScroll: viewport.scrollLeft,
      lastTime: performance.now(),
      velocity: 0,
    };
    viewport.setPointerCapture?.(event.pointerId);
  };

  const onPointerMove = (event: ReactPointerEvent<HTMLDivElement>) => {
    const viewport = viewportRef.current;
    if (!viewport || dragRef.current.pointerId !== event.pointerId) return;
    const distance = event.clientX - dragRef.current.startX;
    if (!dragRef.current.dragging && Math.abs(distance) > 6) {
      dragRef.current.dragging = true;
      setDragging(true);
    }
    if (dragRef.current.dragging) {
      viewport.scrollLeft = dragRef.current.startScroll - distance;
      const now = performance.now();
      const elapsed = Math.max(now - dragRef.current.lastTime, 1);
      dragRef.current.velocity = (viewport.scrollLeft - dragRef.current.lastScroll) / elapsed;
      dragRef.current.lastScroll = viewport.scrollLeft;
      dragRef.current.lastTime = now;
    }
  };

  const finishDrag = (event: ReactPointerEvent<HTMLDivElement>) => {
    const viewport = viewportRef.current;
    if (!viewport || dragRef.current.pointerId !== event.pointerId) return;
    suppressClickRef.current = dragRef.current.dragging;
    const velocity = dragRef.current.velocity;
    const wasDragging = dragRef.current.dragging;
    dragRef.current = {
      startX: 0,
      startScroll: 0,
      pointerId: -1,
      dragging: false,
      lastScroll: 0,
      lastTime: 0,
      velocity: 0,
    };
    setDragging(false);
    viewport.releasePointerCapture?.(event.pointerId);
    if (wasDragging) {
      const firstCard = viewport.querySelector<HTMLElement>(".product-card");
      const track = viewport.querySelector<HTMLElement>(".arrival-rail-track");
      const gap = track ? Number.parseFloat(getComputedStyle(track).columnGap || "0") : 0;
      const stride = Math.max(
        (firstCard?.getBoundingClientRect().width ?? viewport.clientWidth * 0.82) + gap,
        1,
      );
      const projected = viewport.scrollLeft + velocity * 170;
      const maximum = Math.max(viewport.scrollWidth - viewport.clientWidth, 0);
      const target = Math.max(0, Math.min(Math.round(projected / stride) * stride, maximum));
      viewport.scrollTo({ left: target, behavior: "smooth" });
    }
  };

  return (
    <div className={`arrival-rail-shell ${dragging ? "is-dragging" : ""}`}>
      {showControls && (
        <div className="arrival-rail-controls" aria-label={`${label}列表控制`}>
          <button
            type="button"
            aria-label={`向左浏览${label}`}
            disabled={!canPrevious}
            onClick={() => moveByPage(-1)}
          >
            ←
          </button>
          <button
            type="button"
            aria-label={`向右浏览${label}`}
            disabled={!canNext}
            onClick={() => moveByPage(1)}
          >
            →
          </button>
        </div>
      )}
      <section
        ref={viewportRef}
        className="arrival-rail"
        aria-label={`${label}横向商品列表`}
        onScroll={update}
        onPointerDown={onPointerDown}
        onPointerMove={onPointerMove}
        onPointerUp={finishDrag}
        onPointerCancel={finishDrag}
        onClickCapture={(event) => {
          if (!suppressClickRef.current) return;
          event.preventDefault();
          event.stopPropagation();
          suppressClickRef.current = false;
        }}
        onKeyDown={(event) => {
          if (event.key === "ArrowLeft") {
            event.preventDefault();
            moveByPage(-1);
          }
          if (event.key === "ArrowRight") {
            event.preventDefault();
            moveByPage(1);
          }
        }}
      >
        <div className="arrival-rail-track">{children}</div>
      </section>
      {showProgress && (
        <div className="arrival-rail-progress" aria-hidden="true">
          <span style={{ transform: `scaleX(${Math.max(progress, 0.035)})` }} />
        </div>
      )}
    </div>
  );
}
