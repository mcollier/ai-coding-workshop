# Archive

This directory contains deprecated workshop materials that have been superseded by updated versions.

## Archived Files

### Monolithic Marp Presentations (Deprecated March 2026)

**Files:**
- `presentations/fundamentals-github-copilot.md` (834 lines, Part 1)
- `presentations/advanced-github-copilot.md` (862 lines, Part 2)

**Reason for archival:** Migrated to modular presentation structure for flexible workshop delivery (self-paced, role-specific, custom workshops).

**Replaced by:**
- [Modular Presentation Catalog](../docs/presentations/index.md) - Complete module listing and delivery patterns
- [Part 1 Modules](../docs/presentations/modules/part1/) - 7 standalone modules (~3 hours)
- [Part 2 Modules](../docs/presentations/modules/part2/) - 8 standalone modules (~3 hours)
- [Usage Guide](../docs/presentations/README.md) - How to use modular presentations

**Benefits of modular structure:**
- Self-paced learning (students pick individual modules)
- Custom workshops (mix modules for different audiences: developers, BAs, QA, infrastructure)
- Role-specific content (specialized modules coming soon)
- Easy maintenance (update individual topics independently)
- Flexible delivery (30-min lunch & learn to 6-hour full workshop)

**How to use archived monolithic presentations:**
```bash
# Export Part 1 to PDF
npx @marp-team/marp-cli archive/presentations/fundamentals-github-copilot.md --pdf

# Export Part 2 to PDF
npx @marp-team/marp-cli archive/presentations/advanced-github-copilot.md --pdf
```

**Deprecation timeline:** March 30, 2026 - Moved to archive (modular structure is now canonical)

---

### PowerPoint Presentation (Deprecated March 2026)

**File:** `using-ai-for-application-development-with-github-copilot-dotnet-edition.pptx`

**Reason for archival:** Migrated to Marp markdown-based presentations for better maintainability and version control.

**Replaced by:** Modular Marp presentations (see above)

**Benefits of Marp over PowerPoint:**
- Version-controlled markdown (better collaboration)
- Single source of truth with workshop content
- Easy updates via text editor
- Automated PDF generation via `marp-cli`
- Better git diffs in pull requests
- Consistent with documentation-as-code approach

**Deprecation timeline:**
- March 15, 2026 - Created monolithic Marp presentations
- March 30, 2026 - Migrated to modular structure (monolithic files archived)

---

## Presentation Evolution

```
Generation 1: PowerPoint (archived)
  ↓
Generation 2: Monolithic Marp (archived)
  ↓
Generation 3: Modular Marp (current) ✅
```

**Why three generations?**
1. **PowerPoint** → Difficult to maintain, poor version control
2. **Monolithic Marp** → Better than PowerPoint, but inflexible for custom delivery
3. **Modular Marp** → Self-paced, role-specific, mix-and-match capability

---

## Archive Policy

Files in this directory are:
- No longer actively maintained
- Kept for historical reference
- May be removed in future cleanup if deemed unnecessary

For current workshop materials, see the main [README.md](../README.md).
