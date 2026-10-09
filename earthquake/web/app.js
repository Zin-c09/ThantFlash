// ThantQuake frontend.
// Uses the local backend (/api/quakes — Python, PHP or Java) when one is running,
// otherwise calls the USGS feed directly (USGS allows CORS), so it also works on GitHub Pages.

const USGS_FEED = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary";
const REFRESH_MS = 60_000;

const LINKS = [
  { name: "USGS Latest Earthquakes", url: "https://earthquake.usgs.gov/earthquakes/map/", note: "ကမ္ဘာ့ ငလျင် live map" },
  { name: "USGS GeoJSON Feed API", url: "https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php", note: "ဒီ app သုံးတဲ့ free API (key မလို)" },
  { name: "USGS FDSN Event API", url: "https://earthquake.usgs.gov/fdsnws/event/1/", note: "ရက်/နေရာ/magnitude နဲ့ query" },
  { name: "EMSC Seismic Portal", url: "https://www.seismicportal.eu/", note: "ဥရောပ ငလျင်စင်တာ + realtime WebSocket API" },
  { name: "GDACS", url: "https://www.gdacs.org/", note: "UN ဘေးအန္တရာယ် သတိပေးချက် (ငလျင်/ဆူနာမီ)" },
  { name: "Myanmar DMH", url: "https://www.moezala.gov.mm/", note: "မိုးလေဝသနှင့် ဇလဗေဒ ဦးစီးဌာန" },
  { name: "Japan JMA", url: "https://www.jma.go.jp/bosai/map.html#contents=earthquake_map", note: "ဂျပန် ငလျင် / 震度 သတင်း" },
  { name: "Thai Meteorological Dept", url: "https://earthquake.tmd.go.th/", note: "ထိုင်း ငလျင် သတင်း" },
  { name: "Tsunami.gov", url: "https://www.tsunami.gov/", note: "ဆူနာမီ သတိပေးချက်" },
  { name: "ReliefWeb", url: "https://reliefweb.int/disasters", note: "ဘေးအန္တရာယ် သတင်း / အစီရင်ခံစာ" },
];

const $ = (sel) => document.querySelector(sel);
const form = $("#filters");
let map, layer;

function initMap() {
  if (!window.L) return; // Leaflet failed to load (offline) — list still works.
  map = L.map("map", { worldCopyJump: true }).setView([20, 96], 3);
  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 12,
    attribution: "&copy; OpenStreetMap",
  }).addTo(map);
  layer = L.layerGroup().addTo(map);
}

function magClass(m) {
  if (m >= 7) return "severe";
  if (m >= 5.5) return "high";
  if (m >= 4) return "mid";
  return "low";
}

const magColor = { low: "#2f9e44", mid: "#f08c00", high: "#e03131", severe: "#862e9c" };

// Same shape the Python / PHP / Java backends return.
function normalize(feature) {
  const p = feature.properties;
  const [lon, lat, depth] = feature.geometry.coordinates;
  return {
    id: feature.id, mag: p.mag, place: p.place || "Unknown", time: p.time,
    lat, lon, depth, url: p.url, tsunami: p.tsunami === 1, alert: p.alert, felt: p.felt,
  };
}

function feedName(period, minmag) {
  const level = { 0: "all", 2.5: "2.5", 4.5: "4.5", 6: "4.5" }[minmag] || "all";
  return `${level}_${period}`;
}

async function fetchQuakes({ period, minmag, q }) {
  const params = new URLSearchParams({ period, minmag, q });
  try {
    const res = await fetch(`api/quakes?${params}`);
    if (res.ok && res.headers.get("content-type")?.includes("json")) {
      return await res.json();
    }
  } catch { /* no backend — fall through */ }

  const res = await fetch(`${USGS_FEED}/${feedName(period, minmag)}.geojson`);
  if (!res.ok) throw new Error(`USGS HTTP ${res.status}`);
  const data = await res.json();
  const needle = q.trim().toLowerCase();
  const quakes = data.features
    .map(normalize)
    .filter((e) => (e.mag ?? 0) >= minmag)
    .filter((e) => !needle || e.place.toLowerCase().includes(needle))
    .sort((a, b) => b.time - a.time);
  return { source: "usgs-direct", generated: Date.now(), count: quakes.length, quakes };
}

function timeAgo(ms) {
  const s = Math.round((Date.now() - ms) / 1000);
  if (s < 60) return `${s} စက္ကန့်အရင်`;
  if (s < 3600) return `${Math.round(s / 60)} မိနစ်အရင်`;
  if (s < 86400) return `${Math.round(s / 3600)} နာရီအရင်`;
  return `${Math.round(s / 86400)} ရက်အရင်`;
}

function el(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  Object.assign(node, attrs);
  node.append(...children);
  return node;
}

function render(data) {
  $("#source").textContent = `source: ${data.source}`;
  const qs = data.quakes;
  const strongest = qs.reduce((m, e) => ((e.mag ?? 0) > (m?.mag ?? -1) ? e : m), null);

  $("#stats").replaceChildren(
    el("div", { className: "stat" }, el("b", { textContent: qs.length }), el("span", { textContent: "ငလျင် အရေအတွက်" })),
    el("div", { className: "stat" }, el("b", { textContent: strongest ? strongest.mag.toFixed(1) : "–" }), el("span", { textContent: "အပြင်းဆုံး" })),
    el("div", { className: "stat" }, el("b", { textContent: qs.filter((e) => e.tsunami).length }), el("span", { textContent: "Tsunami flag" })),
  );

  $("#status").textContent = qs.length
    ? `Updated ${new Date(data.generated).toLocaleTimeString()}`
    : "ဒီ filter နဲ့ ငလျင် မတွေ့ပါ။";

  $("#list").replaceChildren(...qs.slice(0, 200).map((e) => {
    const cls = magClass(e.mag ?? 0);
    const meta = `${new Date(e.time).toLocaleString()} · ${timeAgo(e.time)} · အနက် ${e.depth?.toFixed(0)} km`
      + (e.felt ? ` · ${e.felt} ယောက် ခံစားရ` : "");
    const place = el("div", { className: "place" }, e.place);
    if (e.tsunami) place.append(el("span", { className: "tag", textContent: "TSUNAMI" }));
    if (e.alert) place.append(el("span", { className: "tag", textContent: `ALERT ${e.alert}` }));
    const item = el("li", { className: "quake", title: "Map မှာကြည့်ရန် နှိပ်ပါ" },
      el("div", { className: `mag ${cls}`, textContent: (e.mag ?? 0).toFixed(1) }),
      el("div", { className: "info" }, place,
        el("div", { className: "meta" }, meta, " · ",
          el("a", { href: e.url, target: "_blank", rel: "noopener", textContent: "USGS အသေးစိတ်" }))),
    );
    item.addEventListener("click", (ev) => {
      if (ev.target.tagName !== "A" && map) map.flyTo([e.lat, e.lon], 6);
    });
    return item;
  }));

  if (layer) {
    layer.clearLayers();
    for (const e of qs) {
      const m = e.mag ?? 0;
      L.circleMarker([e.lat, e.lon], {
        radius: Math.max(3, m * 2.2), color: magColor[magClass(m)], weight: 1, fillOpacity: 0.55,
      }).bindPopup(`<b>M${m.toFixed(1)}</b><br>${escapeHtml(e.place)}<br>${new Date(e.time).toLocaleString()}`)
        .addTo(layer);
    }
  }
}

function escapeHtml(s) {
  return s.replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

async function load() {
  const f = new FormData(form);
  const opts = { period: f.get("period"), minmag: Number(f.get("minmag")), q: f.get("q") || "" };
  $("#status").textContent = "Loading…";
  try {
    render(await fetchQuakes(opts));
  } catch (err) {
    $("#status").textContent = `Data ယူလို့ မရပါ: ${err.message}`;
  }
}

$("#links").replaceChildren(...LINKS.map((l) =>
  el("li", {}, el("a", { href: l.url, target: "_blank", rel: "noopener", textContent: l.name }),
    el("small", { textContent: l.note }))));

form.addEventListener("submit", (ev) => { ev.preventDefault(); load(); });
form.addEventListener("change", load);
initMap();
load();
setInterval(load, REFRESH_MS);
