# Branching and Release Strategy

This document describes the branching strategy, versioning scheme, and release process for the Bnnch: Sort mod.

## Branch Structure

```
main                    <- Active development for LATEST MC version
|
+-- mc/1.21.1           <- Stable/LTS branch for MC 1.21.1
|
+-- feature/<name>      <- Feature branches (off main or mc/*)
+-- fix/<name>          <- Bug fix branches (off main or mc/*)
+-- release/<version>   <- Optional: release prep branches
```

### Branch Naming Conventions

| Branch Type      | Pattern                 | Example              | Base             |
|------------------|-------------------------|----------------------|------------------|
| Main development | `main`                  | `main`               | -                |
| MC version       | `mc/<mc-version>`       | `mc/1.21.1`          | -                |
| Feature          | `feature/<short-name>`  | `feature/auto-sort`  | `main` or `mc/*` |
| Bug fix          | `fix/<short-name>`      | `fix/crash-on-empty` | `main` or `mc/*` |
| Release prep     | `release/<mod-version>` | `release/1.2.0`      | `main` or `mc/*` |

### Support Policy

We support:

- **Latest MC version** - Active development on `main`
- **One stable/LTS version** - Maintained on `mc/<version>` branch

## Versioning Scheme

**Format:** `<major>.<minor>.<patch>+mc<mc-version>`

| Component | Meaning                            | Example     |
|-----------|------------------------------------|-------------|
| Major     | Breaking changes, major features   | `2.0.0`     |
| Minor     | New features, backwards compatible | `1.1.0`     |
| Patch     | Bug fixes only                     | `1.0.1`     |
| MC suffix | Minecraft version target           | `+mc1.21.1` |

### Version Examples

- `1.0.0+mc1.21.1` - Initial release for MC 1.21.1
- `1.0.0+mc1.21.4` - Same mod version, different MC target
- `1.1.0+mc1.21.4` - New feature release
- `1.1.1+mc1.21.1` - Bug fix backported to stable

### Git Tag Format

Tags follow the pattern: `v<mod-version>+mc<mc-version>`

Examples:

- `v1.0.0+mc1.21.1`
- `v1.2.3+mc1.21.4`

## Workflows

### Feature Development

1. Create `feature/<name>` branch from `main` (or `mc/*` for version-specific features)
2. Develop and test locally
3. Open PR to target branch (`main` or `mc/*`)
4. Merge after review and CI passes
5. If feature applies to both versions, cherry-pick to the other branch

```bash
# Example: Adding a feature to main, then backporting to mc/1.21.1
git checkout main
git checkout -b feature/auto-sort
# ... develop feature ...
git push -u origin feature/auto-sort
# Open PR to main, merge after approval

# Backport to stable branch
git checkout mc/1.21.1
git checkout -b feature/auto-sort-backport
git cherry-pick <commit-hash>
# Resolve any conflicts
git push -u origin feature/auto-sort-backport
# Open PR to mc/1.21.1
```

### Bug Fixes (Backporting)

1. Create `fix/<name>` branch from `main`
2. Fix the bug and test
3. Open PR to `main`, merge after CI passes
4. Cherry-pick commit(s) to `mc/<stable>` branch
5. Open PR with cherry-picked fix to `mc/<stable>`

```bash
# Example: Fix on main, backport to mc/1.21.1
git checkout main
git checkout -b fix/crash-on-empty
# ... fix bug ...
git push -u origin fix/crash-on-empty
# Open PR to main, merge after approval

# Backport to stable
git checkout mc/1.21.1
git checkout -b fix/crash-on-empty-backport
git cherry-pick <commit-hash>
git push -u origin fix/crash-on-empty-backport
# Open PR to mc/1.21.1
```

### Creating a Release

1. Ensure all changes are merged to target branch (`main` or `mc/*`)
2. Update `mod_version` in `gradle.properties` if needed
3. Create and push an annotated tag

```bash
# Example: Release v1.2.0 for MC 1.21.4 from main
git checkout main
git pull origin main

# Update version in gradle.properties if needed
# mod_version=1.2.0

# Create annotated tag
git tag -a "v1.2.0+mc1.21.4" -m "Release v1.2.0 for Minecraft 1.21.4"
git push origin "v1.2.0+mc1.21.4"

# CI automatically builds and creates GitHub Release
```

## Release Checklist

### Pre-Release

- [ ] All features/fixes merged to target branch
- [ ] CI passing on target branch
- [ ] Version number updated in `gradle.properties` (if needed)
- [ ] Changelog prepared (for GitHub Release notes)
- [ ] Local build and test successful

### Release

- [ ] Create annotated git tag: `v<version>+mc<mc-version>`
- [ ] Push tag to trigger release workflow
- [ ] Verify GitHub Release created with JAR artifact
- [ ] Verify mod metadata shows correct version

### Post-Release (Phase 3 - Future)

- [ ] Verify CurseForge upload successful
- [ ] Verify Modrinth upload successful
- [ ] Announce release (Discord, social media, etc.)

## MC Version Transitions

When a new Minecraft version releases (e.g., MC 1.22):

### Step 1: Create Stable Branch

```bash
# Create stable branch from current main
git checkout main
git checkout -b mc/1.21.4
git push -u origin mc/1.21.4
```

### Step 2: Update Main for New MC Version

```bash
git checkout main

# Update gradle.properties:
# - minecraft_version=1.22
# - minecraft_version_range=[1.22,1.23)
# - neo_version=<new-neo-version>
# - Update parchment versions

# Update any version-specific code

git add -A
git commit -m "chore: update to Minecraft 1.22"
git push origin main
```

### Step 3: Archive Old Stable (Optional)

If dropping support for the oldest version:

```bash
# Option A: Delete the branch
git push origin --delete mc/1.21.1

# Option B: Archive by renaming
git branch -m mc/1.21.1 archive/mc/1.21.1
git push origin archive/mc/1.21.1
git push origin --delete mc/1.21.1
```

### Branch State Example

**Before MC 1.22:**

```
main (MC 1.21.4) <- latest
mc/1.21.1        <- stable
```

**After MC 1.22:**

```
main (MC 1.22)   <- latest
mc/1.21.4        <- stable (new)
mc/1.21.1        <- dropped or archived
```

## CI/CD Pipeline

### Continuous Integration

The CI workflow runs on:

- Pushes to `main`
- Pushes to `mc/*` branches
- Pull requests targeting `main` or `mc/*` branches

It performs:

1. Build the mod
2. Run game tests

### Release Automation

The release workflow triggers on tags starting with `v` and validates the format
`v<major>.<minor>.<patch>+mc<mc-version>`. It then:

1. Validates the tag format (fails fast if invalid)
2. Builds the mod JAR
3. Extracts version information from the tag
4. Creates a GitHub Release with the JAR attached

### Future: Platform Publishing (Phase 3)

After setting up CurseForge and Modrinth projects, the release workflow will also:

- Upload to CurseForge
- Upload to Modrinth

## Configuration Files

| File                            | Purpose                                          |
|---------------------------------|--------------------------------------------------|
| `gradle.properties`             | Mod version, MC version, dependencies            |
| `.github/workflows/ci.yml`      | CI pipeline configuration                        |
| `.github/workflows/release.yml` | Release automation                               |
| `build.gradle`                  | Build configuration (Phase 3: publishing plugin) |

## Quick Reference

### Common Commands

```bash
# Build
./gradlew.bat build

# Test locally
./gradlew.bat runClient

# Create release tag
git tag -a "v1.0.0+mc1.21.1" -m "Release v1.0.0 for Minecraft 1.21.1"
git push origin "v1.0.0+mc1.21.1"
```

### Tag Pattern

```
v<major>.<minor>.<patch>+mc<mc-version>
```

Examples: `v1.0.0+mc1.21.1`, `v2.1.3+mc1.22`
