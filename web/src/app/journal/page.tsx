import { SiteFooter } from "@/components/SiteFooter";
import { SiteNav } from "@/components/SiteNav";
import { getJournalEntries } from "@/lib/journal";
import Image from "next/image";
import Link from "next/link";

export default function JournalPage() {
  const entries = getJournalEntries();
  return (
    <>
      <SiteNav />
      <main className="page-shell">
        <header className="page-intro">
          <div>
            <span className="eyebrow">MORROW / JOURNAL</span>
            <h1>
              关于物件，
              <br />
              也关于生活。
            </h1>
          </div>
          <p>选物、空间和使用经验。推广或品牌合作内容会始终明确标识。</p>
        </header>
        <section className="journal-grid">
          {entries.map((entry) => (
            <Link className="journal-card surface" href={`/journal/${entry.slug}`} key={entry.slug}>
              <div className="journal-card-media">
                <Image src={entry.cover} alt={entry.title} fill sizes="50vw" />
              </div>
              <div className="journal-card-copy">
                {entry.sponsored && <span className="sponsored">推广 / {entry.sponsorName}</span>}
                <span className="eyebrow">{entry.publishedAt}</span>
                <h2>{entry.title}</h2>
                <p>{entry.summary}</p>
              </div>
            </Link>
          ))}
        </section>
      </main>
      <SiteFooter />
    </>
  );
}
