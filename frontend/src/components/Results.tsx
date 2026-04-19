import { useState } from 'react';
import type { Car, QuizAnswers, RecommendResponse } from '../types';

interface Props {
  response: RecommendResponse;
  answers: QuizAnswers;
  onReset: () => void;
}

// ── data for comparison table ──────────────────────────────────────────────────

type CompareRow = { label: string; render: (car: Car) => string };

const COMPARE_ROWS: CompareRow[] = [
  { label: 'Price',          render: c => `₹${c.priceLakh.toFixed(2)} L` },
  { label: 'Fuel Type',      render: c => c.fuelType },
  { label: 'Transmission',   render: c => c.transmission },
  { label: 'Mileage',        render: c => c.fuelType === 'Electric' ? `${c.mileage} km range` : `${c.mileage} km/l` },
  { label: 'Seating',        render: c => `${c.seating} seats` },
  { label: 'Safety Rating',  render: c => `${c.safetyRating} / 5` },
];

const RANK = [
  { badge: '🥇 Top Pick',       pill: 'bg-amber-100 text-amber-700 border-amber-300'  },
  { badge: '🥈 Runner Up',      pill: 'bg-slate-100  text-slate-600  border-slate-300'  },
  { badge: '🥉 Also Consider',  pill: 'bg-orange-100 text-orange-700 border-orange-300' },
];

// ── small helpers ──────────────────────────────────────────────────────────────

function StarRating({ rating }: { rating: number }) {
  return (
    <span className="flex gap-0.5" aria-label={`${rating} out of 5 stars`}>
      {Array.from({ length: 5 }, (_, i) => (
        <span key={i} className={i < rating ? 'text-amber-400' : 'text-gray-200'}>★</span>
      ))}
    </span>
  );
}

function Pill({ label }: { label: string }) {
  return (
    <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600 whitespace-nowrap">
      {label}
    </span>
  );
}

// ── compare modal ──────────────────────────────────────────────────────────────

function CompareModal({ a, b, onClose }: { a: Car; b: Car; onClose: () => void }) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-2xl overflow-hidden animate-[fadeIn_.15s_ease-out]">

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h3 className="text-base font-bold text-gray-900">⚖️ Side-by-Side Comparison</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-700 text-lg leading-none transition-colors"
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        {/* Car name headers */}
        <div className="grid grid-cols-3 bg-gray-50 px-6 py-3 gap-3 border-b border-gray-100">
          <div />
          {[a, b].map(car => (
            <div key={car.id} className="text-center">
              <p className="text-sm font-bold text-gray-900 leading-tight">{car.make} {car.model}</p>
              <p className="text-xs text-blue-600 font-semibold mt-0.5">₹{car.priceLakh.toFixed(2)} L</p>
            </div>
          ))}
        </div>

        {/* Comparison rows */}
        <div className="divide-y divide-gray-50">
          {COMPARE_ROWS.map((row, i) => (
            <div
              key={row.label}
              className={`grid grid-cols-3 px-6 py-3 gap-3 items-center ${i % 2 === 0 ? 'bg-white' : 'bg-gray-50/60'}`}
            >
              <p className="text-xs font-medium text-gray-500">{row.label}</p>
              <p className="text-center text-sm text-gray-800 font-medium">{row.render(a)}</p>
              <p className="text-center text-sm text-gray-800 font-medium">{row.render(b)}</p>
            </div>
          ))}
        </div>

        <div className="px-6 py-4 border-t border-gray-100">
          <button
            onClick={onClose}
            className="w-full py-2 rounded-xl bg-gray-100 text-sm font-semibold text-gray-700 hover:bg-gray-200 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

// ── car card ───────────────────────────────────────────────────────────────────

function CarCard({ car, rank, reasoning }: { car: Car; rank: number; reasoning: string }) {
  const { badge, pill } = RANK[rank] ?? RANK[2];
  const pros = car.pros.split(',').map(p => p.trim()).filter(Boolean);
  const cons = car.cons.split(',').map(c => c.trim()).filter(Boolean);
  const mileageLabel = car.fuelType === 'Electric'
    ? `${car.mileage} km range`
    : `${car.mileage} km/l`;

  return (
    <div className="flex flex-col rounded-2xl border border-gray-200 bg-white shadow-md hover:shadow-xl hover:-translate-y-1 transition-all duration-200 overflow-hidden">

      {/* Rank strip */}
      <div className={`border-b px-4 py-2 text-xs font-semibold ${pill}`}>
        {badge}
      </div>

      <div className="flex flex-col gap-4 p-5 flex-1">

        {/* Title + price */}
        <div>
          <h3 className="text-lg font-bold text-gray-900 leading-tight">
            {car.make} {car.model}
          </h3>
          <p className="text-sm text-gray-400 mt-0.5">{car.variant}</p>
          <p className="mt-2 text-2xl font-extrabold text-blue-600">
            ₹{car.priceLakh.toFixed(2)}<span className="text-base font-semibold ml-0.5"> L</span>
          </p>
        </div>

        {/* Spec pills */}
        <div className="flex flex-wrap gap-2">
          <Pill label={car.fuelType} />
          <Pill label={car.transmission} />
          <Pill label={mileageLabel} />
          <Pill label={`${car.seating} Seats`} />
        </div>

        {/* Safety rating */}
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500">Safety</span>
          <StarRating rating={car.safetyRating} />
          <span className="text-xs text-gray-400">{car.safetyRating}/5</span>
        </div>

        {/* AI reasoning */}
        {reasoning && (
          <div className="rounded-xl bg-blue-50 border border-blue-100 px-3 py-3">
            <p className="text-xs font-semibold text-blue-500 mb-1 uppercase tracking-wide">Why this car?</p>
            <p className="text-xs text-blue-900 leading-relaxed">{reasoning}</p>
          </div>
        )}

        {/* Pros */}
        {pros.length > 0 && (
          <div>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1.5">Pros</p>
            <ul className="space-y-1.5">
              {pros.map(p => (
                <li key={p} className="flex items-start gap-2 text-xs text-gray-700">
                  <span className="text-green-500 shrink-0 mt-px">✓</span>
                  {p}
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Cons */}
        {cons.length > 0 && (
          <div>
            <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1.5">Cons</p>
            <ul className="space-y-1.5">
              {cons.map(c => (
                <li key={c} className="flex items-start gap-2 text-xs text-gray-700">
                  <span className="text-red-400 shrink-0 mt-px">✕</span>
                  {c}
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

// ── main ───────────────────────────────────────────────────────────────────────

export default function Results({ response, answers, onReset }: Props) {
  const [showCompare, setShowCompare] = useState(false);
  const { cars, reasoning, tradeoffs } = response;

  return (
    <div className="min-h-screen bg-gray-50">

      {/* Top bar */}
      <header className="sticky top-0 z-10 bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between gap-4">
          <div>
            <h1 className="text-xl font-bold text-gray-900">🚗 Your CarIQ Shortlist</h1>
            <p className="text-sm text-gray-500 mt-0.5">
              {cars.length} recommendation{cars.length !== 1 ? 's' : ''} · Budget {answers.budget}L · {answers.useCase}
            </p>
          </div>
          <button
            onClick={onReset}
            className="shrink-0 px-4 py-2 rounded-xl border border-gray-200 text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors"
          >
            ↩ Start Over
          </button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-8 space-y-8">

        {/* Car grid */}
        {cars.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            {cars.map((car, i) => (
              <CarCard
                key={car.id}
                car={car}
                rank={i}
                reasoning={reasoning[String(car.id)] ?? ''}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-20 text-gray-400">
            <p className="text-5xl mb-4">😕</p>
            <p className="text-lg font-semibold text-gray-600">No matching cars found</p>
            <p className="text-sm mt-1">Try adjusting your budget or priorities</p>
          </div>
        )}

        {/* Tradeoff section */}
        {tradeoffs && (
          <div className="rounded-2xl bg-amber-50 border border-amber-200 px-6 py-5">
            <div className="flex items-start gap-3">
              <span className="text-xl shrink-0 mt-0.5">⚠️</span>
              <div>
                <p className="text-sm font-bold text-amber-800 mb-1">Trade-off Analysis</p>
                <p className="text-sm text-amber-700 leading-relaxed">{tradeoffs}</p>
              </div>
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex flex-wrap items-center gap-3 pb-8">
          {cars.length >= 2 && (
            <button
              onClick={() => setShowCompare(true)}
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 text-white text-sm font-semibold shadow-sm hover:bg-blue-700 active:scale-95 transition-all"
            >
              ⚖️ Compare Top 2
            </button>
          )}
          <button
            onClick={onReset}
            className="px-5 py-2.5 rounded-xl border border-gray-300 text-sm font-medium text-gray-600 hover:bg-gray-100 active:scale-95 transition-all"
          >
            ↩ Start Over
          </button>
        </div>

      </main>

      {/* Compare modal */}
      {showCompare && cars.length >= 2 && (
        <CompareModal a={cars[0]} b={cars[1]} onClose={() => setShowCompare(false)} />
      )}
    </div>
  );
}