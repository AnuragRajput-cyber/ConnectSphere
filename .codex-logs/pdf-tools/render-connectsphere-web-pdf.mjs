import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { marked } from 'marked';
import puppeteer from 'puppeteer-core';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const root = path.resolve(__dirname, '..', '..');
const source = path.join(root, 'connectsphere-web', 'CONNECTSPHERE_WEB_EXPLAINED.md');
const htmlOut = path.join(root, 'connectsphere-web', 'CONNECTSPHERE_WEB_EXPLAINED.html');
const pdfOut = path.join(root, 'connectsphere-web', 'CONNECTSPHERE_WEB_EXPLAINED.pdf');
const diagramOut = path.join(root, 'connectsphere-web', 'connectsphere-web-architecture.png');
const mermaidScriptPath = path.join(__dirname, 'node_modules', 'mermaid', 'dist', 'mermaid.min.js');
const chromePath = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';

function escapeHtml(value) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;');
}

function buildHtml(markdown, mermaidScript) {
  marked.setOptions({ gfm: true, breaks: false });
  const diagrams = [];
  const withPlaceholders = markdown.replace(/```mermaid\r?\n([\s\S]*?)```/g, (_match, diagram) => {
    const index = diagrams.push(diagram.trim()) - 1;
    return `MERMAID_DIAGRAM_PLACEHOLDER_${index}`;
  });
  let body = marked.parse(withPlaceholders);
  diagrams.forEach((diagram, index) => {
    const rendered = `<div class="diagram-shell"><div class="mermaid">${escapeHtml(diagram)}</div></div>`;
    body = body
      .replace(`<p>MERMAID_DIAGRAM_PLACEHOLDER_${index}</p>`, rendered)
      .replace(`MERMAID_DIAGRAM_PLACEHOLDER_${index}`, rendered);
  });

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>ConnectSphere Web Explained</title>
  <style>
    @page { size: A4; margin: 14mm 12mm; }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: Inter, Arial, sans-serif;
      color: #172033;
      background: #f7fafc;
      font-size: 12.5px;
      line-height: 1.58;
    }
    main {
      max-width: 980px;
      margin: 0 auto;
      padding: 34px 38px 46px;
      background: #ffffff;
      border: 1px solid #e5eaf0;
      box-shadow: 0 18px 50px rgba(15, 23, 42, 0.08);
    }
    h1 {
      margin: 0 0 18px;
      color: #0f172a;
      font-size: 31px;
      line-height: 1.1;
      letter-spacing: 0;
      border-bottom: 3px solid #1d9bf0;
      padding-bottom: 14px;
    }
    h2 {
      margin: 30px 0 10px;
      color: #0f172a;
      font-size: 22px;
      line-height: 1.2;
      break-after: avoid;
      page-break-after: avoid;
    }
    h3 {
      margin: 20px 0 8px;
      color: #111827;
      font-size: 16px;
      break-after: avoid;
      page-break-after: avoid;
    }
    p { margin: 8px 0; }
    blockquote {
      margin: 14px 0;
      padding: 12px 16px;
      border-left: 4px solid #1d9bf0;
      background: #eff6ff;
      color: #0f172a;
      border-radius: 8px;
    }
    ul, ol { margin: 8px 0 12px 24px; padding: 0; }
    li { margin: 4px 0; }
    code {
      font-family: "Cascadia Mono", Consolas, monospace;
      font-size: 11px;
      background: #f1f5f9;
      color: #0f172a;
      padding: 1px 4px;
      border-radius: 4px;
    }
    pre {
      margin: 12px 0 16px;
      padding: 14px;
      overflow-wrap: anywhere;
      white-space: pre-wrap;
      background: #0f172a;
      color: #e5edf6;
      border-radius: 10px;
      border: 1px solid #243449;
      page-break-inside: avoid;
    }
    pre code {
      display: block;
      padding: 0;
      color: inherit;
      background: transparent;
      font-size: 10.2px;
      line-height: 1.48;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0 18px;
      font-size: 11.2px;
      page-break-inside: avoid;
    }
    th, td {
      border: 1px solid #dbe4ef;
      padding: 7px 8px;
      vertical-align: top;
    }
    th {
      background: #eef6ff;
      color: #0f172a;
      text-align: left;
    }
    .diagram-shell {
      margin: 18px 0 22px;
      padding: 14px;
      border: 1px solid #dbeafe;
      border-radius: 12px;
      background: #fbfdff;
      page-break-inside: avoid;
      overflow: hidden;
    }
    .mermaid {
      display: flex;
      justify-content: center;
      width: 100%;
      overflow: visible;
    }
    .mermaid svg {
      max-width: 100% !important;
      height: auto !important;
      font-family: Inter, Arial, sans-serif !important;
    }
    a { color: #0f6cbd; text-decoration: none; }
    hr { border: 0; border-top: 1px solid #e5eaf0; margin: 24px 0; }
    @media print {
      body { background: #ffffff; }
      main { box-shadow: none; border: 0; padding: 0; }
      h1, h2, h3, table, pre, blockquote, .diagram-shell { break-inside: avoid; }
    }
  </style>
</head>
<body>
  <main>${body}</main>
  <script>${mermaidScript}</script>
  <script>
    window.__MERMAID_RENDERED = false;
    window.addEventListener('load', async () => {
      try {
        mermaid.initialize({
          startOnLoad: false,
          securityLevel: 'loose',
          theme: 'base',
          themeVariables: {
            primaryColor: '#eff6ff',
            primaryTextColor: '#0f172a',
            primaryBorderColor: '#1d9bf0',
            lineColor: '#475569',
            secondaryColor: '#f8fafc',
            tertiaryColor: '#ffffff',
            fontFamily: 'Inter, Arial, sans-serif'
          },
          flowchart: { htmlLabels: true, curve: 'basis' },
          sequence: { mirrorActors: false }
        });
        await mermaid.run({ querySelector: '.mermaid' });
        window.__MERMAID_RENDERED = true;
      } catch (error) {
        console.error(error);
        window.__MERMAID_RENDERED = true;
      }
    });
  </script>
</body>
</html>`;
}

const markdown = await fs.readFile(source, 'utf8');
const mermaidScript = await fs.readFile(mermaidScriptPath, 'utf8');
const html = buildHtml(markdown, mermaidScript);
await fs.writeFile(htmlOut, html, 'utf8');

const browser = await puppeteer.launch({
  executablePath: chromePath,
  headless: 'new',
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
});

try {
  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 1800, deviceScaleFactor: 2 });
  await page.goto(`file://${htmlOut.replaceAll('\\', '/')}`, { waitUntil: 'networkidle0' });
  await page.waitForFunction('window.__MERMAID_RENDERED === true', { timeout: 20000 });
  await page.evaluate(() => document.fonts && document.fonts.ready);

  const firstDiagram = await page.$('.diagram-shell');
  if (firstDiagram) {
    await firstDiagram.screenshot({ path: diagramOut, omitBackground: false });
  }

  await page.pdf({
    path: pdfOut,
    format: 'A4',
    printBackground: true,
    margin: { top: '13mm', right: '11mm', bottom: '13mm', left: '11mm' },
    preferCSSPageSize: true,
  });
} finally {
  await browser.close();
}

console.log(JSON.stringify({ htmlOut, pdfOut, diagramOut }, null, 2));
