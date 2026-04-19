# CARIQ Frontend

A React/TypeScript single-page application that guides Indian car buyers through a 4-step quiz and presents AI-generated car recommendations with rankings, specs, and trade-off analysis.

---

## What Did You Build and Why?

The frontend is a focused, opinionated product experience — not a generic search UI. Rather than presenting a filter panel and a car list, it walks the user through a short quiz (budget → use case → priorities → free text) and then surfaces exactly 3 cars, ranked and explained. The goal was to reduce decision paralysis: most car-buying sites overwhelm with options; this one makes a confident recommendation.

Design-wise, the aesthetic is dark, high-contrast, and typographically bold — closer to automotive editorial than a typical SaaS dashboard.

### What Was Deliberately Cut

- **No user accounts or login** — anonymous, stateless experience
- **No pagination or full catalog browsing** — results are always exactly 3 cards
- **No saved comparisons or history** — session ends when the user refreshes
- **No unit/integration tests** — time constraint; component behavior was verified manually
- **No mobile-specific breakpoints** — responsive via `clamp()` and CSS Grid, but not pixel-perfect on small phones
- **No error state UI** — API failures surface as a generic browser alert, not a designed error screen
- **No analytics or tracking** — no GTM, no event logging

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Framework | React 19 + TypeScript | Component model fits the multi-step quiz pattern; TS catches API shape mismatches early |
| Build tool | Vite 8 | Fast HMR, ES module native, minimal config vs. CRA/Webpack |
| Styling | Tailwind CSS 4 + custom CSS variables | Utility classes for layout; custom vars for the design token system (colors, fonts) |
| HTTP | Axios | Cleaner interceptor and response transformation API than native fetch |
| Fonts | Bebas Neue, Barlow Condensed, DM Mono | Loaded via Google Fonts; creates the automotive editorial tone |

**Why not Next.js?**
This is a single-screen interactive experience with no SEO requirement. A full SSR framework would add complexity without benefit. Vite + React SPA is the minimal correct choice here.

**Why Tailwind?**
Rapid layout iteration. The component style objects (inline JSX) handle the dynamic/stateful styling; Tailwind handles structural layout. Mixing the two kept the code from becoming a mess of conditional class strings.

---

## Project Structure

```
frontend/
├── src/
│   ├── api/
│   │   └── cariq.ts          # Axios instance, POST /recommend, response mapper
│   ├── components/
│   │   ├── QuizFlow.tsx       # 4-step quiz UI (budget, use case, priorities, extra)
│   │   └── Results.tsx        # Car cards, compare modal, trade-off display
│   ├── types/
│   │   └── index.ts           # Car, QuizAnswers, RecommendResponse interfaces
│   ├── App.tsx                # Root — toggles between QuizFlow and Results
│   └── index.css              # CSS variables, keyframes, base resets
├── public/
├── index.html
├── vite.config.ts
└── package.json
```

---

## Key UI Flows

### Quiz (QuizFlow.tsx)
4 sequential steps, each gated by validation before advancing:

1. **Budget** — 5 preset radio-style buttons ("Under 5L" → "Above 25L")
2. **Use Case** — 4 options (City Commute, Family Car, Highway, First Car)
3. **Priorities** — multi-select chips (Mileage, Automatic, 7 Seats, Low Maintenance, Safety 4★+)
4. **Extra Info** — free-text textarea; maps to the backend `extra` hard-constraints field

Split-panel layout: sticky left column shows the question context and a progress indicator; right column holds the interactive controls. Transitions use CSS `opacity` + `translateY` animations.

### Results (Results.tsx)
- 3 car cards in a responsive grid, each with a rank badge (TOP PICK / RUNNER UP / ALSO CONSIDER)
- Per-card: make/model/variant, price, fuel type, gearbox, mileage, seating, safety and user ratings
- "Why This Car" reasoning box pulled from the LLM response
- Strengths (+) and Trade-offs (−) lists
- "Compare Top 2" button opens a `CompareModal` with a side-by-side diff of the top 2 cars
- Overall trade-off paragraph below the cards
- "Start Over" resets to the quiz

---

## Environment Configuration

```bash
# .env.local
VITE_API_BASE_URL=http://localhost:8080
```

Defaults to `http://localhost:8080` if the variable is not set.

---

## Running Locally

```bash
cd frontend
npm install
npm run dev
```

App available at `http://localhost:5173`.

---

## What AI Tools Helped With

- **Initial component scaffolding** — QuizFlow and Results were bootstrapped by AI from a rough description of the desired flow; the structure was close enough to build on without rewriting
- **CSS animation keyframes** — the `fadeUp`, `scaleIn`, `slideRight` sequences were AI-generated from a description of the desired feel
- **Type definitions** — generating the TypeScript interfaces from the backend JSON response shape was instant with AI; manually aligning backend vs. frontend types would have been tedious
- **Design direction** — the dark automotive aesthetic, font pairing (Bebas Neue for display, Barlow Condensed for body), and the rank badge color scheme were suggested by AI and refined manually

## Where AI Got in the Way

- **Overly complex state management** — the initial AI-generated QuizFlow used `useReducer` with separate action types for each step; overkill for 4 steps and was rewritten with plain `useState`
- **Generic UI aesthetics** — the first AI pass produced a clean but forgettable light-mode card UI; achieving the final dark, bold design required significant back-and-forth and manual CSS work
- **Hallucinated Tailwind 4 classes** — Tailwind 4 changed some utility names; AI sometimes generated class names from Tailwind 3 that didn't resolve, requiring manual lookup
- **Response type mismatches** — AI generated types that assumed the backend response shape without reading the actual API; the mapping layer in `cariq.ts` (INR → lakh conversion, field name normalization) was discovered and written manually

---

## If I Had 4 More Hours

1. **Designed error states** — replace the `alert()` on API failure with a proper error screen with a retry option
2. **Mobile polish** — dedicated breakpoints for sub-375px screens; the quiz layout collapses but isn't refined on small phones
3. **Streaming results** — show car cards appearing progressively as the LLM streams tokens rather than waiting for the full response
4. **Shareable results URL** — encode the session ID in the URL so users can link to their recommendation
5. **Skeleton loading** — replace the centered spinner with shimmer card placeholders that match the results layout, reducing perceived wait time
