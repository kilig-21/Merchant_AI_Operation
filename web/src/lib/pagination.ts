export function appendUniquePage<T>(current: T[], next: T[], identity: (item: T) => string | number): T[] {
  const seen = new Set(current.map(identity));
  const appended = next.filter((item) => {
    const key = identity(item);
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
  return [...current, ...appended];
}

export function hasNextPage(received: number, pageSize: number): boolean {
  return received === pageSize;
}
