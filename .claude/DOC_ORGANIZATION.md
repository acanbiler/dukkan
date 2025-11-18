# Documentation Organization Guide

**Last Updated:** 2025-11-14

---

## 📚 Documentation Structure

### Root Directory (`/`)

**Essential Documentation (KEEP):**

| Document | Purpose | Audience | When to Read |
|----------|---------|----------|--------------|
| `README.md` | Project overview & quick start | Everyone | First time, new contributors |
| `NEXT_SESSION.md` | Current session starting point | Active developers | Every session start |
| `NEXT_STEPS.md` | Roadmap and priorities | Developers, PMs | Planning work |
| `CLAUDE.md` | Architecture patterns & commands | Developers | Before coding |
| `ARCHITECTURE.md` | Technical architecture details | Developers, architects | Understanding system design |
| `DEVELOPMENT.md` | Development workflow & setup | Developers | Initial setup |
| `REQUIREMENTS.md` | Business requirements | Everyone | Understanding features |
| `AUTH_IMPROVEMENTS.md` | Future auth enhancements | Developers | Planning auth work |
| `I18N_GUIDE.md` | Internationalization guide | Frontend developers | Adding translations |

---

### `.claude/` Directory

**Strategic Planning:**
- `.claude/dev/active/production-readiness/` - Comprehensive 75-task production readiness plan
  - `production-readiness-plan.md` - Main strategic document (Phase 1-4)
  - `production-readiness-context.md` - Key files, decisions, dependencies
  - `production-readiness-tasks.md` - Checklist format for tracking

**Archived Documentation:**
- `.claude/archive/` - Historical session completion docs
  - `INTEGRATION_COMPLETE.md` - Auth & i18n integration (archived 2025-11-14)
  - `ORDER_SERVICE_COMPLETE.md` - Order Service completion (archived 2025-11-14)
  - `GETTING_STARTED.md` - Old quick start (superseded by README.md & DEVELOPMENT.md)

---

## 🗂️ Documentation by Use Case

### "I'm new to the project"
1. Read `README.md` - Overview and quick start
2. Read `DEVELOPMENT.md` - Setup instructions
3. Read `CLAUDE.md` - Architecture and patterns
4. Read `ARCHITECTURE.md` - Technical decisions

### "I'm starting a new session"
1. Read `NEXT_SESSION.md` - Where we left off
2. Check `.claude/dev/active/production-readiness/` - Long-term plan
3. Skim `NEXT_STEPS.md` - Priorities

### "I need to implement a feature"
1. Check `REQUIREMENTS.md` - Business requirements
2. Check `CLAUDE.md` - Architecture patterns
3. Check `.claude/dev/active/production-readiness/` - If it's part of the plan
4. Read feature-specific guide if exists (e.g., `I18N_GUIDE.md`)

### "I want to improve authentication"
1. Read `AUTH_IMPROVEMENTS.md` - Roadmap of enhancements
2. Check `CLAUDE.md` - Current auth implementation
3. Check production-readiness plan - Testing requirements

### "I need to add translations"
1. Read `I18N_GUIDE.md` - Complete i18n usage guide
2. Update locale files in `frontend/src/i18n/locales/`

---

## 📋 Documentation Maintenance

### When to Update

**After Every Feature:**
- Update `NEXT_SESSION.md` with new status
- Update `README.md` if major feature added
- Update feature-specific guides if patterns change

**After Every Sprint:**
- Update `NEXT_STEPS.md` with completed items
- Update production-readiness tasks checklist
- Archive completion docs to `.claude/archive/`

**After Major Changes:**
- Update `ARCHITECTURE.md` if architecture changes
- Update `CLAUDE.md` if patterns change
- Update `REQUIREMENTS.md` if features change

### What NOT to Keep

**Delete/Archive these:**
- ❌ Session completion docs older than 2 weeks (move to archive)
- ❌ Outdated quick start guides (superseded by README.md)
- ❌ Temporary planning docs (after incorporated into main docs)
- ❌ Duplicate information (consolidate instead)

---

## 🔍 Finding Information Quickly

### "Where is X explained?"

| Topic | Document |
|-------|----------|
| Project setup | `README.md` (quick) or `DEVELOPMENT.md` (detailed) |
| Next tasks | `NEXT_SESSION.md` (immediate) or `NEXT_STEPS.md` (roadmap) |
| Architecture patterns | `CLAUDE.md` |
| Technical decisions | `ARCHITECTURE.md` |
| Business requirements | `REQUIREMENTS.md` |
| Auth roadmap | `AUTH_IMPROVEMENTS.md` |
| i18n usage | `I18N_GUIDE.md` |
| Detailed plan | `.claude/dev/active/production-readiness/` |
| Historical context | `.claude/archive/` |

### "I want to understand..."

| What | Read |
|------|------|
| Microservices architecture | `ARCHITECTURE.md` |
| Layered architecture pattern | `CLAUDE.md` (Backend Architecture section) |
| Domain-Driven Design approach | `CLAUDE.md` (Architectural Patterns) |
| API Gateway routing | `ARCHITECTURE.md` + `CLAUDE.md` |
| Database schema | `production-readiness-context.md` (Database Schema Reference) |
| Security implementation | `ARCHITECTURE.md` (Security) + `AUTH_IMPROVEMENTS.md` |
| Frontend structure | `CLAUDE.md` (Frontend Architecture) |

---

## 📐 Documentation Standards

### File Naming
- **UPPERCASE.md** - Project-level docs (e.g., `README.md`, `ARCHITECTURE.md`)
- **lowercase.md** - Component-specific docs (e.g., `frontend/README.md`)
- **kebab-case.md** - Multi-word docs (e.g., `production-readiness-plan.md`)

### Document Structure
All major docs should have:
1. **Title** - Clear, descriptive
2. **Last Updated** - Date of last modification
3. **Table of Contents** - For long docs (>100 lines)
4. **Sections** - Logical grouping with headers
5. **Examples** - Code snippets where applicable
6. **Related Links** - Cross-references to other docs

### Markdown Style
- Use **bold** for emphasis
- Use `code blocks` for commands, file names, code
- Use > blockquotes for important notes
- Use tables for structured data
- Use emoji sparingly (mainly in headings for visual navigation)

---

## 🔄 Documentation Workflow

### Adding New Documentation

1. **Determine scope:**
   - Project-level? → Root directory
   - Component-specific? → Component directory
   - Strategic plan? → `.claude/dev/active/`
   - Historical? → `.claude/archive/`

2. **Name appropriately:**
   - Follow naming conventions above
   - Be descriptive and specific

3. **Cross-reference:**
   - Add to README.md if major
   - Add to this guide (`DOC_ORGANIZATION.md`)
   - Link from related docs

4. **Keep organized:**
   - One topic per document
   - Avoid duplication
   - Update existing docs instead of creating new ones

### Archiving Old Documentation

**When to archive:**
- Session completion docs after 2+ weeks
- Superseded quick start guides
- Outdated feature plans (after implementation)

**How to archive:**
```bash
mv DOC_NAME.md .claude/archive/
```

**Update references:**
- Remove from README.md
- Remove from this guide's main section
- Add to archive section if noteworthy

---

## 🎯 Current Documentation Status

### ✅ Well Organized
- Root-level essential docs clear and up-to-date
- Strategic planning in `.claude/dev/active/`
- Historical docs in `.claude/archive/`

### ⚠️ Needs Attention
- `DEVELOPMENT.md` could be more comprehensive (expand setup guides)
- Consider adding `DEPLOYMENT.md` when Dockerization complete
- Consider adding `TESTING.md` when tests implemented

### 📅 Future Additions
- `DEPLOYMENT.md` - Production deployment guide (after Phase 1 of plan)
- `TESTING.md` - Testing strategy and guides (after tests written)
- `CONTRIBUTING.md` - Contribution guidelines (if open-sourced)
- `CHANGELOG.md` - Version history (when v1.0 released)

---

## 💡 Best Practices

### Do's ✅
- Keep README.md concise and high-level
- Use NEXT_SESSION.md as the "session memory"
- Update docs as you code, not after
- Cross-reference related documents
- Archive completed session docs
- Use consistent formatting across all docs

### Don'ts ❌
- Don't duplicate information across multiple docs
- Don't create docs for temporary planning (use tasks instead)
- Don't leave outdated docs in root directory
- Don't mix session notes with permanent documentation
- Don't skip updating docs after major changes

---

## 📞 Documentation Maintenance Responsibility

**Active Developers:** Update `NEXT_SESSION.md`, `NEXT_STEPS.md`, production-readiness tasks
**Tech Lead:** Update `ARCHITECTURE.md`, `CLAUDE.md`, this guide
**Product Manager:** Update `REQUIREMENTS.md`
**Everyone:** Keep `README.md` accurate

---

**Maintained by:** Development Team
**Review Cadence:** Monthly or after major features
**Last Review:** 2025-11-14
