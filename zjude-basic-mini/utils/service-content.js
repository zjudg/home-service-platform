/**
 * Convert the admin-authored service-detail HTML into text-only sections.
 * Rendering text nodes instead of raw HTML keeps mini-program behavior
 * consistent and prevents stored markup from being interpreted as code.
 */
export function parseServiceContent (value) {
  const html = String(value || '')
    .replace(/<script\b[^>]*>[\s\S]*?<\/script\s*>/gi, '')
    .replace(/<style\b[^>]*>[\s\S]*?<\/style\s*>/gi, '')
  if (!html.trim()) return []

  const headingPattern = /<h[1-6]\b[^>]*>([\s\S]*?)<\/h[1-6]\s*>/gi
  const headings = [...html.matchAll(headingPattern)]
  if (!headings.length) {
    const text = cleanText(html.replace(/<br\s*\/?>|<\/p\s*>/gi, '\n'))
    return text ? [{ title: '服务内容', paragraphs: [text], lists: [] }] : []
  }

  return headings.map((heading, index) => {
    const bodyStart = heading.index + heading[0].length
    const bodyEnd = headings[index + 1]?.index ?? html.length
    const body = html.slice(bodyStart, bodyEnd)
    const paragraphs = [...body.matchAll(/<p\b[^>]*>([\s\S]*?)<\/p\s*>/gi)]
      .map(match => cleanText(match[1]))
      .filter(Boolean)
    const lists = [...body.matchAll(/<(ol|ul)\b[^>]*>([\s\S]*?)<\/\1\s*>/gi)]
      .map(match => ({
        ordered: match[1].toLowerCase() === 'ol',
        items: [...match[2].matchAll(/<li\b[^>]*>([\s\S]*?)<\/li\s*>/gi)]
          .map(item => cleanText(item[1]))
          .filter(Boolean)
      }))
      .filter(list => list.items.length)

    // Preserve simple text entered without paragraph/list wrappers.
    const remainder = body
      .replace(/<(ol|ul)\b[^>]*>[\s\S]*?<\/\1\s*>/gi, ' ')
      .replace(/<p\b[^>]*>[\s\S]*?<\/p\s*>/gi, ' ')
    const looseText = cleanText(remainder)
    if (looseText) paragraphs.push(looseText)

    return { title: cleanText(heading[1]), paragraphs, lists }
  }).filter(section => section.title || section.paragraphs.length || section.lists.length)
}

function cleanText (value) {
  return decodeEntities(String(value || '')
    .replace(/<[^>]*>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim())
}

function decodeEntities (value) {
  const named = { '&nbsp;': ' ', '&amp;': '&', '&lt;': '<', '&gt;': '>', '&quot;': '"', '&#39;': "'" }
  return value
    .replace(/&nbsp;|&amp;|&lt;|&gt;|&quot;|&#39;/gi, entity => named[entity.toLowerCase()] || entity)
    .replace(/&#(\d+);/g, (_, code) => String.fromCodePoint(Number(code)))
    .replace(/&#x([\da-f]+);/gi, (_, code) => String.fromCodePoint(parseInt(code, 16)))
}
