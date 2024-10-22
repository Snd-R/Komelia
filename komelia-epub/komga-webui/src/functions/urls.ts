const fullUrl = 'http://komelia.lan'
const baseUrl = '/home/den/tmp/komga-webui/index.html'

const urls = {
  origin: !fullUrl.endsWith('/') ? `${fullUrl}/` : fullUrl,
  originNoSlash: fullUrl.endsWith('/') ? fullUrl.slice(0, -1) : fullUrl,
  base: !baseUrl.endsWith('/') ? `${baseUrl}/` : baseUrl,
  baseNoSlash: baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl,
} as Urls

export default urls

export function bookManifestUrl(bookId: string): string {
  return `${urls.originNoSlash}/api/v1/books/${bookId}/manifest`
}

export function bookPositionsUrl(bookId: string): string {
  return `${urls.originNoSlash}/api/v1/books/${bookId}/positions`
}
