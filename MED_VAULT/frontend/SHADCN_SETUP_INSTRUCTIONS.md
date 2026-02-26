# Shadcn Setup Notes for This Repo

Current frontend is Vite + React 18 + JavaScript, so it **does not fully match** your requested stack (React 19 + Tailwind v4 + TypeScript + shadcn project structure).

## What was added now
- `lucide-react` installed.
- Requested block files created under `app/`, `assets/svg/`, `components/shadcn-studio/`.
- Minimal `components/ui` and `lib/utils` added so block imports resolve.

## To fully match requested stack (recommended)
Run these in `frontend`:

```bash
npm install react@^19 react-dom@^19
npm install -D typescript @types/node
npm install -D tailwindcss @tailwindcss/vite
```

Add shadcn config and components:

```bash
npx shadcn@latest init
npx shadcn@latest add button card checkbox input label separator dropdown-menu utils
```

If you want strict Tailwind v4 + Next App Router style with `app/` pages, easiest is creating a Next.js app and moving these block files there.

## Optional Next.js migration path
```bash
npx create-next-app@latest medvault-next --ts --tailwind --app
cd medvault-next
npx shadcn@latest init
npx shadcn@latest add button card checkbox input label separator dropdown-menu utils
```

Then copy files from this repo's `app/`, `assets/svg/`, and `components/shadcn-studio/`.
