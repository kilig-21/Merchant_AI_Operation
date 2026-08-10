import fs from "node:fs";
import path from "node:path";
import matter from "gray-matter";
import type { JournalMetadata } from "./types";

const contentDir = path.join(process.cwd(), "src", "content", "journal");

function metadata(slug: string, data: Record<string, unknown>): JournalMetadata {
  return {
    slug,
    title: String(data.title || slug),
    summary: String(data.summary || ""),
    publishedAt: String(data.publishedAt || ""),
    cover: String(data.cover || ""),
    relatedProductIds: Array.isArray(data.relatedProductIds) ? data.relatedProductIds.map(Number) : [],
    sponsored: Boolean(data.sponsored),
    sponsorName: data.sponsorName ? String(data.sponsorName) : undefined,
    ctaLabel: data.ctaLabel ? String(data.ctaLabel) : undefined,
    ctaHref: data.ctaHref ? String(data.ctaHref) : undefined,
  };
}

export function getJournalEntries() {
  return fs
    .readdirSync(contentDir)
    .filter((file) => file.endsWith(".mdx"))
    .map((file) => {
      const slug = file.replace(/\.mdx$/, "");
      const parsed = matter(fs.readFileSync(path.join(contentDir, file), "utf8"));
      return metadata(slug, parsed.data);
    })
    .sort((a, b) => b.publishedAt.localeCompare(a.publishedAt));
}

export function getJournalEntry(slug: string) {
  const file = path.join(contentDir, `${slug}.mdx`);
  if (!fs.existsSync(file)) return null;
  const parsed = matter(fs.readFileSync(file, "utf8"));
  return { metadata: metadata(slug, parsed.data), content: parsed.content };
}
