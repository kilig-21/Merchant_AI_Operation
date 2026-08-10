"use client";

import type { CSSProperties, PointerEvent as ReactPointerEvent } from "react";
import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";

export interface DriftWallItem {
  image: string;
  title?: string;
  href?: string;
}

export interface DriftWallProps {
  items: DriftWallItem[];
  columns?: number;
  tileWidth?: number;
  tileHeight?: number;
  gap?: number;
  radius?: number;
  tilt?: number;
  turn?: number;
  roll?: number;
  perspective?: number;
  depth?: number;
  speed?: number;
  direction?: "up" | "down";
  variance?: number;
  parallax?: number;
  pauseOnHover?: boolean;
  lift?: number;
  fade?: number;
  dim?: number;
  grayscale?: boolean;
  overlayColor?: string;
  className?: string;
  style?: CSSProperties;
}

const prefersReducedMotion = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;

const columnFactor = (index: number, variance: number) => {
  const pseudo = ((index * 0.6180339887 + 0.35) % 1) * 2 - 1;
  return 1 + variance * pseudo;
};

export default function DriftWall({
  items,
  columns = 5,
  tileWidth = 200,
  tileHeight = 132,
  gap = 18,
  radius = 14,
  tilt = 16,
  turn = -14,
  roll = 0,
  perspective = 1200,
  depth = 120,
  speed = 42,
  direction = "up",
  variance = 0.45,
  parallax = 0.6,
  pauseOnHover = false,
  lift = 64,
  fade = 0.6,
  dim = 0.55,
  grayscale = false,
  overlayColor = "#060010",
  className = "",
  style = {},
}: DriftWallProps) {
  const containerRef = useRef<HTMLElement>(null);
  const planeRef = useRef<HTMLDivElement>(null);
  const trackRefs = useRef<Array<HTMLDivElement | null>>([]);
  const rafRef = useRef<number | null>(null);
  const offsetsRef = useRef<number[]>([]);
  const velocitiesRef = useRef<number[]>([]);
  const hoveredColRef = useRef(-1);
  const wallHoveredRef = useRef(false);
  const pointerRef = useRef({ x: 0, y: 0 });
  const pointerDampedRef = useRef({ x: 0, y: 0 });
  const lastTsRef = useRef<number | null>(null);
  const [containerHeight, setContainerHeight] = useState(600);
  const [activeId, setActiveId] = useState<string | null>(null);
  const activeIdRef = useRef<string | null>(null);
  const [reduced, setReduced] = useState(false);
  const safeColumns = Math.max(1, Math.min(8, Math.round(columns)));

  useEffect(() => {
    setReduced(prefersReducedMotion());
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const onChange = (event: MediaQueryListEvent) => setReduced(event.matches);
    query.addEventListener("change", onChange);
    return () => query.removeEventListener("change", onChange);
  }, []);

  const columnItems = useMemo(() => {
    const source = items.length ? items : [{ image: "/media/product-1.svg", title: "Morrow" }];
    const cols = Array.from({ length: safeColumns }, () => [] as DriftWallItem[]);
    source.forEach((item, index) => cols[index % safeColumns].push(item));
    return cols.map((col) => (col.length ? col : source.slice(0, 1)));
  }, [items, safeColumns]);

  const columnMeta = useMemo(() => {
    const unit = tileHeight + gap;
    return columnItems.map((col) => {
      const copyHeight = Math.max(unit, col.length * unit);
      const copies = Math.max(2, Math.ceil((containerHeight * 1.6) / copyHeight) + 1);
      return { copyHeight, copies };
    });
  }, [columnItems, tileHeight, gap, containerHeight]);

  useLayoutEffect(() => {
    if (!containerRef.current) return undefined;
    const observer = new ResizeObserver(([entry]) => setContainerHeight(entry.contentRect.height || 600));
    observer.observe(containerRef.current);
    return () => observer.disconnect();
  }, []);

  const baseVelocities = useMemo(() => {
    const directionSign = direction === "up" ? 1 : -1;
    return columnItems.map((_, index) => {
      const alternateSign = index % 2 === 0 ? 1 : -1;
      return speed * columnFactor(index, variance) * directionSign * alternateSign;
    });
  }, [columnItems, speed, direction, variance]);

  useEffect(() => {
    offsetsRef.current = columnMeta.map((meta, index) => meta.copyHeight * ((index * 0.37) % 1));
    velocitiesRef.current = columnItems.map(() => 0);
  }, [columnMeta, columnItems]);

  const applyPlaneTransform = useCallback(
    (pointerX: number, pointerY: number) => {
      const plane = planeRef.current;
      if (!plane) return;
      plane.style.transform =
        `translate(-50%, -50%) scale(1.18) rotateX(${tilt + pointerY}deg) ` +
        `rotateY(${turn + pointerX}deg) rotateZ(${roll}deg) translateZ(${-depth}px)`;
    },
    [tilt, turn, roll, depth],
  );

  useEffect(() => {
    const animate = (timestamp: number) => {
      if (lastTsRef.current === null) lastTsRef.current = timestamp;
      const delta = Math.min(0.05, Math.max(0, timestamp - lastTsRef.current) / 1000);
      lastTsRef.current = timestamp;

      const maxTilt = parallax * 8;
      const targetX = pointerRef.current.x * maxTilt;
      const targetY = -pointerRef.current.y * maxTilt;
      const damping = 1 - Math.exp(-delta / 0.12);
      pointerDampedRef.current.x += (targetX - pointerDampedRef.current.x) * damping;
      pointerDampedRef.current.y += (targetY - pointerDampedRef.current.y) * damping;
      applyPlaneTransform(pointerDampedRef.current.x, pointerDampedRef.current.y);

      for (let index = 0; index < trackRefs.current.length; index += 1) {
        const meta = columnMeta[index];
        const element = trackRefs.current[index];
        if (!meta || !element) continue;

        if (!reduced) {
          const paused = wallHoveredRef.current && pauseOnHover;
          const factor = paused || hoveredColRef.current === index ? 0 : 1;
          const target = baseVelocities[index] * factor;
          const ease = 1 - Math.exp(-delta / (target === 0 ? 0.16 : 0.28));
          velocitiesRef.current[index] += (target - velocitiesRef.current[index]) * ease;
          let next = (offsetsRef.current[index] ?? 0) + velocitiesRef.current[index] * delta;
          next = ((next % meta.copyHeight) + meta.copyHeight) % meta.copyHeight;
          offsetsRef.current[index] = next;
        }

        element.style.transform = `translate3d(0, ${-(offsetsRef.current[index] ?? 0)}px, 0)`;
      }

      rafRef.current = requestAnimationFrame(animate);
    };

    rafRef.current = requestAnimationFrame(animate);
    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      rafRef.current = null;
      lastTsRef.current = null;
    };
  }, [applyPlaneTransform, baseVelocities, columnMeta, parallax, pauseOnHover, reduced]);

  const activate = useCallback((id: string, index: number) => {
    activeIdRef.current = id;
    hoveredColRef.current = index;
    setActiveId(id);
  }, []);

  const release = useCallback(() => {
    activeIdRef.current = null;
    hoveredColRef.current = -1;
    setActiveId(null);
  }, []);

  const handlePointerMove = useCallback(
    (event: ReactPointerEvent<HTMLElement>) => {
      const rect = containerRef.current?.getBoundingClientRect();
      if (!rect) return;
      if (parallax > 0 && !reduced) {
        pointerRef.current = {
          x: (event.clientX - rect.left) / rect.width - 0.5,
          y: (event.clientY - rect.top) / rect.height - 0.5,
        };
      }

      const hit = document.elementFromPoint(event.clientX, event.clientY);
      const tile = hit?.closest<HTMLElement>("[data-tile-id]");
      if (!tile) return;
      const id = tile.dataset.tileId;
      if (!id || id === activeIdRef.current) return;
      activate(id, Number(tile.dataset.col));
    },
    [activate, parallax, reduced],
  );

  const handlePointerLeaveWall = useCallback(() => {
    wallHoveredRef.current = false;
    pointerRef.current = { x: 0, y: 0 };
    release();
  }, [release]);

  const cssVars = {
    "--dw-tile-w": `${tileWidth}px`,
    "--dw-tile-h": `${tileHeight}px`,
    "--dw-gap": `${gap}px`,
    "--dw-radius": `${radius}px`,
    "--dw-perspective": `${perspective}px`,
    "--dw-lift": `${lift}px`,
    "--dw-dim": dim,
    "--dw-gray": grayscale ? 1 : 0,
    "--dw-overlay": overlayColor,
    "--dw-edge": `${Math.max(0, (1 - fade) * 100)}%`,
    ...style,
  } as CSSProperties;

  const renderTile = (item: DriftWallItem, id: string, columnIndex: number) => {
    const content = (
      <span className="drift-wall__inner">
        {/* React Bits uses a native image because every tile moves continuously in a duplicated track. */}
        <img src={item.image} alt={item.title ?? ""} loading="lazy" decoding="async" draggable={false} />
        <span className="drift-wall__overlay" aria-hidden="true" />
        {item.title ? <span className="drift-wall__title">{item.title}</span> : null}
      </span>
    );
    const activeClass = activeId === id ? " is-active" : "";

    if (item.href) {
      return (
        <a
          className={`drift-wall__tile${activeClass}`}
          data-col={columnIndex}
          data-tile-id={id}
          href={item.href}
          key={id}
          onBlur={release}
          onFocus={() => activate(id, columnIndex)}
        >
          {content}
        </a>
      );
    }

    return (
      <span className={`drift-wall__tile${activeClass}`} data-col={columnIndex} data-tile-id={id} key={id}>
        {content}
      </span>
    );
  };

  const rootClass = ["drift-wall", reduced ? "drift-wall--reduced" : "", className].filter(Boolean).join(" ");

  return (
    <section
      ref={containerRef}
      aria-label="持续漂移的 Morrow 物件墙"
      className={rootClass}
      onPointerEnter={() => {
        wallHoveredRef.current = true;
      }}
      onPointerLeave={handlePointerLeaveWall}
      onPointerMove={handlePointerMove}
      style={cssVars}
    >
      <div ref={planeRef} className="drift-wall__plane">
        {columnItems.map((column, columnIndex) => {
          const meta = columnMeta[columnIndex];
          const copies = Array.from({ length: meta.copies });
          const columnKey = column.map((item) => item.href ?? item.image).join("|");
          return (
            <div className="drift-wall__col" key={columnKey}>
              <div
                className="drift-wall__track"
                ref={(element) => {
                  trackRefs.current[columnIndex] = element;
                }}
              >
                {copies.map((_, copyIndex) =>
                  column.map((item, itemIndex) =>
                    renderTile(item, `${columnIndex}-${copyIndex}-${itemIndex}`, columnIndex),
                  ),
                )}
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
