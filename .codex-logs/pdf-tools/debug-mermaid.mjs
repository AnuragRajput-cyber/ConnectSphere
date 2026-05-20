import fs from 'node:fs';
import path from 'node:path';
import puppeteer from 'puppeteer-core';

const mermaid = fs.readFileSync('.codex-logs/pdf-tools/node_modules/mermaid/dist/mermaid.min.js', 'utf8');
const md = fs.readFileSync('connectsphere-web/CONNECTSPHERE_WEB_EXPLAINED.md', 'utf8');
const diagram = md.match(/```mermaid\r?\n([\s\S]*?)```/)[1];
const escaped = diagram.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
const html = `<html><body><div class="mermaid">${escaped}</div><script>${mermaid}</script><script>
mermaid.initialize({startOnLoad:false,securityLevel:'loose'});
mermaid.run({querySelector:'.mermaid'}).then(()=>console.log('done')).catch(e=>console.error('ERR', e.message, e.str, e.hash && JSON.stringify(e.hash)));
</script></body></html>`;
const file = path.resolve('.codex-logs/pdf-tools/test-mermaid.html');
fs.writeFileSync(file, html);
const browser = await puppeteer.launch({ executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe', headless: 'new' });
const page = await browser.newPage();
page.on('console', (msg) => console.log(msg.type(), msg.text()));
page.on('pageerror', (e) => console.log('pageerror', e.message));
await page.goto(`file:///${file.replaceAll('\\', '/')}`, { waitUntil: 'networkidle0' });
await new Promise((resolve) => setTimeout(resolve, 3000));
console.log(await page.$eval('.mermaid', (e) => e.innerText).catch((e) => `no inner ${e.message}`));
await browser.close();
