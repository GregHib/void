// Collapsible sidebar sections (see Docs.kt's `.docs-nav-group`). Sections render collapsed by
// default — except the one holding the active page, expanded server-side so navigation keeps its
// context. Opening a section closes whichever sibling section (same parent list) was open, so at
// most one section per level is ever expanded at a time.
window.voidToggleDocsSection = function (btn) {
  var group = btn.closest('.docs-nav-group');
  if (!group) {
    return;
  }
  var parent = group.parentElement;
  var wasOpen = group.classList.contains('open');
  parent.querySelectorAll(':scope > .docs-nav-group.open').forEach(function (sibling) {
    sibling.classList.remove('open');
    var children = sibling.querySelector(':scope > .docs-nav-children');
    if (children) {
      children.hidden = true;
    }
    // A plain section header *is* the `.docs-nav-expand` (a full-width row button); a linked one
    // (e.g. "Home") nests a separate chevron button inside `.docs-nav-header` instead — this
    // matches either shape.
    var toggle = sibling.querySelector(':scope > .docs-nav-expand, :scope > .docs-nav-header > .docs-nav-expand');
    if (toggle) {
      toggle.setAttribute('aria-expanded', 'false');
    }
  });
  if (!wasOpen) {
    group.classList.add('open');
    var ownChildren = group.querySelector(':scope > .docs-nav-children');
    if (ownChildren) {
      ownChildren.hidden = false;
    }
    btn.setAttribute('aria-expanded', 'true');
  }
};

// Sidebar search box, backed by `docs/search-index.json` (written by Docs.kt alongside the
// pages). Matches are scored by where the query hits — title first, then description, then body
// text — so a page whose title matches always outranks one that only mentions the term in passing.
(function () {
  var DOCS_SEARCH_INDEX_URL = 'search-index.json';
  var cachedIndex = null;

  function loadIndex() {
    if (cachedIndex) {
      return Promise.resolve(cachedIndex);
    }
    return fetch(DOCS_SEARCH_INDEX_URL)
      .then(function (res) { return res.ok ? res.json() : []; })
      .then(function (data) { cachedIndex = data; return data; })
      .catch(function () { return []; });
  }

  function escapeHtml(text) {
    return text.replace(/[&<>"']/g, function (c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  function score(entry, query) {
    var title = entry.title.toLowerCase();
    var description = (entry.description || '').toLowerCase();
    var text = (entry.text || '').toLowerCase();
    if (title.indexOf(query) === 0) {
      return 4;
    }
    if (title.indexOf(query) >= 0) {
      return 3;
    }
    if (description.indexOf(query) >= 0) {
      return 2;
    }
    if (text.indexOf(query) >= 0) {
      return 1;
    }
    return 0;
  }

  function renderResults(container, matches) {
    if (matches.length === 0) {
      container.innerHTML = '<div class="docs-search-empty">No matching pages.</div>';
      container.hidden = false;
      return;
    }
    container.innerHTML = matches.map(function (entry) {
      var desc = entry.description
        ? '<span class="docs-search-result-desc">' + escapeHtml(entry.description) + '</span>'
        : '';
      return '<a class="docs-search-result" href="' + entry.id + '.html">' +
        '<span class="docs-search-result-title">' + escapeHtml(entry.title) + '</span>' +
        desc +
        '</a>';
    }).join('');
    container.hidden = false;
  }

  document.addEventListener('DOMContentLoaded', function () {
    var input = document.getElementById('docs-search-input');
    var results = document.getElementById('docs-search-results');
    if (!input || !results) {
      return;
    }

    input.addEventListener('focus', function () { loadIndex(); });

    input.addEventListener('input', function () {
      var query = input.value.trim().toLowerCase();
      if (!query) {
        results.hidden = true;
        results.innerHTML = '';
        return;
      }
      loadIndex().then(function (index) {
        var matches = index
          .map(function (entry) { return { entry: entry, score: score(entry, query) }; })
          .filter(function (m) { return m.score > 0; })
          .sort(function (a, b) { return b.score - a.score; })
          .slice(0, 8)
          .map(function (m) { return m.entry; });
        renderResults(results, matches);
      });
    });

    input.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') {
        results.hidden = true;
        input.blur();
      } else if (event.key === 'Enter') {
        var first = results.querySelector('.docs-search-result');
        if (first) {
          window.location = first.getAttribute('href');
        }
      }
    });

    document.addEventListener('click', function (event) {
      if (!input.contains(event.target) && !results.contains(event.target)) {
        results.hidden = true;
      }
    });
  });
})();
