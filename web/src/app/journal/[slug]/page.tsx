import { ProductCard } from "@/components/ProductCard";
import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { demoProducts } from "@/lib/demo-data";
import { getJournalEntries, getJournalEntry } from "@/lib/journal";
import { MDXRemote } from "next-mdx-remote/rsc";
import Image from "next/image";
import Link from "next/link";
import { notFound } from "next/navigation";

export function generateStaticParams() {
  return getJournalEntries().map((entry) => ({ slug: entry.slug }));
}
export default async function JournalEntryPage({ params }: { params: Promise<{ slug: string }> }) {
  const entry = getJournalEntry((await params).slug);
  if (!entry) notFound();
  const related = demoProducts.filter((item) => entry.metadata.relatedProductIds.includes(item.id));
  return (
    <>
      <SiteNav />
      <main className="article-shell">
        {entry.metadata.sponsored && (
          <p className="sponsored">推广 / 品牌合作 · {entry.metadata.sponsorName}</p>
        )}
        <span className="eyebrow">JOURNAL / {entry.metadata.publishedAt}</span>
        <h1>{entry.metadata.title}</h1>
        <p className="article-lead">{entry.metadata.summary}</p>
        <div className="article-cover">
          <Image src={entry.metadata.cover} alt={entry.metadata.title} fill priority sizes="100vw" />
        </div>
        <article className="article-body">
          <MDXRemote source={entry.content} />
        </article>
        {entry.metadata.ctaHref && (
          <Link className="button primary" href={entry.metadata.ctaHref}>
            {entry.metadata.ctaLabel || "继续浏览"} →
          </Link>
        )}
        {related.length > 0 && (
          <section className="related-products">
            <span className="eyebrow">RELATED OBJECTS</span>
            <h2 className="editorial">文中提到的物件</h2>
            <div className="product-grid">
              {related.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          </section>
        )}
      </main>
      <SiteFooter />
    </>
  );
}
