# Archive

This directory contains deprecated workshop materials that have been superseded by updated versions.

## Archived Files

### PowerPoint Presentation (Deprecated March 2026)

**File:** `using-ai-for-application-development-with-github-copilot-dotnet-edition.pptx`

**Reason for archival:** Migrated to Marp markdown-based presentations for better maintainability and version control.

**Replaced by:**
- [Part 1: Fundamentals Presentation](../docs/presentations/fundamentals-github-copilot.md) - Marp format
- [Part 2: Advanced GitHub Copilot Presentation](../docs/presentations/advanced-github-copilot.md) - Marp format

**Benefits of Marp format:**
- Version-controlled markdown (better collaboration)
- Single source of truth with workshop content
- Easy updates via text editor
- Automated PDF generation via `marp-cli`
- Better git diffs in pull requests
- Consistent with documentation-as-code approach

**How to generate PDFs from Marp:**
```bash
# Part 1
npx @marp-team/marp-cli docs/presentations/fundamentals-github-copilot.md --pdf

# Part 2
npx @marp-team/marp-cli docs/presentations/advanced-github-copilot.md --pdf
```

**Note:** The archived PowerPoint file is preserved for historical reference but is no longer maintained.

## Archive Policy

Files in this directory are:
- No longer actively maintained
- Kept for historical reference
- May be removed in future cleanup if deemed unnecessary

For current workshop materials, see the main [README.md](../README.md).
