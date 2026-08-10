"use client";

import { useEffect, useRef } from "react";

export interface LineSidebarItem {
  id: string;
  label: string;
  shortLabel?: string;
}

interface LineSidebarProps {
  items: LineSidebarItem[];
  activeIndex: number;
  onItemClick: (index: number, item: LineSidebarItem) => void;
  className?: string;
}

export default function LineSidebar({ items, activeIndex, onItemClick, className = "" }: LineSidebarProps) {
  const listRef = useRef<HTMLUListElement>(null);
  const targets = useRef<number[]>(items.map(() => 0));
  const values = useRef<number[]>(items.map(() => 0));
  const frame = useRef<number | null>(null);

  useEffect(() => {
    targets.current = items.map(() => 0);
    values.current = items.map(() => 0);
  }, [items]);

  useEffect(() => {
    const run = () => {
      const list = listRef.current;
      if (!list) return;
      values.current = values.current.map((value, index) => {
        const next = value + (targets.current[index] - value) * 0.14;
        const item = list.children[index] as HTMLElement | undefined;
        item?.style.setProperty("--effect", String(next));
        return next;
      });
      frame.current = requestAnimationFrame(run);
    };
    frame.current = requestAnimationFrame(run);
    return () => {
      if (frame.current !== null) cancelAnimationFrame(frame.current);
      frame.current = null;
    };
  }, []);

  const handleMove = (event: React.PointerEvent<HTMLUListElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    const pointerY = event.clientY - rect.top;
    targets.current = items.map((_, index) => {
      const element = event.currentTarget.children[index] as HTMLElement | undefined;
      if (!element) return 0;
      const center = element.offsetTop + element.offsetHeight / 2;
      const distance = Math.abs(pointerY - center);
      return Math.max(0, 1 - distance / 100) ** 1.5;
    });
  };

  const reset = () => {
    targets.current = items.map(() => 0);
  };

  return (
    <nav className={`line-sidebar ${className}`} aria-label="首页章节导航">
      <ul ref={listRef} className="line-sidebar__list" onPointerMove={handleMove} onPointerLeave={reset}>
        {items.map((item, index) => (
          <li
            key={item.id}
            className="line-sidebar__item"
            aria-current={activeIndex === index ? "step" : undefined}
          >
            <button type="button" onClick={() => onItemClick(index, item)}>
              <span className="line-sidebar__marker" aria-hidden="true" />
              <span className="line-sidebar__label">
                <span className="line-sidebar__index">{String(index + 1).padStart(2, "0")}</span>
                <span className="line-sidebar__text">
                  <span className="line-sidebar__full">{item.label}</span>
                  <span className="line-sidebar__short">{item.shortLabel ?? item.label}</span>
                </span>
              </span>
            </button>
          </li>
        ))}
      </ul>
    </nav>
  );
}
