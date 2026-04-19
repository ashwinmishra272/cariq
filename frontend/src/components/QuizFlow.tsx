import { useState } from 'react';
import type { QuizAnswers, RecommendResponse } from '../types';
import { getRecommendations } from '../api/cariq';

interface Props {
  onComplete: (answers: QuizAnswers, response: RecommendResponse) => void;
}

// ── data ──────────────────────────────────────────────────────────────────────

const BUDGET_OPTIONS = [
  { label: 'Under 5L',  sublabel: '< ₹5 Lakh',        value: '0-5'    },
  { label: '5 – 10L',  sublabel: '₹5 – ₹10 Lakh',    value: '5-10'   },
  { label: '10 – 15L', sublabel: '₹10 – ₹15 Lakh',   value: '10-15'  },
  { label: '15 – 25L', sublabel: '₹15 – ₹25 Lakh',   value: '15-25'  },
  { label: 'Above 25L',sublabel: '₹25 Lakh+',         value: '25-100' },
];

const USE_OPTIONS = [
  { label: 'City Commute', icon: '🏙️', value: 'city commute' },
  { label: 'Family Car',   icon: '👨‍👩‍👧', value: 'family car'   },
  { label: 'Highway',      icon: '🛣️',  value: 'highway'      },
  { label: 'First Car',    icon: '🎉', value: 'first car'    },
];

const PRIORITY_OPTIONS = [
  'Good Mileage',
  'Automatic',
  '7 Seats',
  'Low Maintenance',
  'Safety 4★+',
];

const STEP_LABELS = ['Budget', 'Primary Use', 'Priorities', 'Anything Else?'];

// ── shared card styles ─────────────────────────────────────────────────────────

function cardClass(selected: boolean) {
  return [
    'cursor-pointer rounded-2xl border-2 px-5 py-4 text-left transition-all duration-150 select-none',
    selected
      ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-200 shadow-sm'
      : 'border-gray-200 bg-white hover:border-blue-300 hover:bg-blue-50/40',
  ].join(' ');
}

// ── step sub-components ────────────────────────────────────────────────────────

function StepBudget({ selected, onSelect }: { selected: string; onSelect: (v: string) => void }) {
  return (
    <div>
      <h2 className="text-xl font-semibold text-gray-800 mb-1">What's your budget?</h2>
      <p className="text-sm text-gray-500 mb-5">Select one option</p>
      <div className="flex flex-col gap-3">
        {BUDGET_OPTIONS.map(opt => (
          <button
            key={opt.value}
            className={cardClass(selected === opt.value)}
            onClick={() => onSelect(opt.value)}
          >
            <span className="font-semibold text-gray-900">{opt.label}</span>
            <span className="ml-3 text-sm text-gray-400">{opt.sublabel}</span>
          </button>
        ))}
      </div>
    </div>
  );
}

function StepUse({ selected, onSelect }: { selected: string; onSelect: (v: string) => void }) {
  return (
    <div>
      <h2 className="text-xl font-semibold text-gray-800 mb-1">Primary use?</h2>
      <p className="text-sm text-gray-500 mb-5">How will you mostly use the car?</p>
      <div className="grid grid-cols-2 gap-3">
        {USE_OPTIONS.map(opt => (
          <button
            key={opt.value}
            className={cardClass(selected === opt.value)}
            onClick={() => onSelect(opt.value)}
          >
            <div className="text-2xl mb-1">{opt.icon}</div>
            <div className="font-semibold text-gray-900 text-sm">{opt.label}</div>
          </button>
        ))}
      </div>
    </div>
  );
}

function StepPriorities({
  selected,
  onToggle,
}: {
  selected: string[];
  onToggle: (v: string) => void;
}) {
  return (
    <div>
      <h2 className="text-xl font-semibold text-gray-800 mb-1">What matters most?</h2>
      <p className="text-sm text-gray-500 mb-5">Pick one or more</p>
      <div className="flex flex-wrap gap-3">
        {PRIORITY_OPTIONS.map(opt => {
          const active = selected.includes(opt);
          return (
            <button
              key={opt}
              onClick={() => onToggle(opt)}
              className={[
                'rounded-full border-2 px-5 py-2 text-sm font-medium transition-all duration-150 select-none',
                active
                  ? 'border-blue-500 bg-blue-500 text-white shadow-sm'
                  : 'border-gray-200 bg-white text-gray-700 hover:border-blue-300 hover:bg-blue-50',
              ].join(' ')}
            >
              {opt}
            </button>
          );
        })}
      </div>
    </div>
  );
}

function StepExtra({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  return (
    <div>
      <h2 className="text-xl font-semibold text-gray-800 mb-1">Anything else?</h2>
      <p className="text-sm text-gray-500 mb-5">Optional — tell us more about your needs</p>
      <textarea
        rows={5}
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder="e.g. I need good boot space or I like black cars"
        className="w-full rounded-2xl border-2 border-gray-200 px-4 py-3 text-sm text-gray-800 placeholder-gray-400 outline-none focus:border-blue-400 focus:ring-2 focus:ring-blue-100 transition-all resize-none"
      />
    </div>
  );
}

// ── main component ─────────────────────────────────────────────────────────────

export default function QuizFlow({ onComplete }: Props) {
  const [step, setStep] = useState(1);
  const [visible, setVisible] = useState(true);

  const [budget, setBudget]       = useState('');
  const [useCase, setUseCase]     = useState('');
  const [priorities, setPriorities] = useState<string[]>([]);
  const [extraInfo, setExtraInfo]  = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState('');

  function transition(fn: () => void) {
    setVisible(false);
    setTimeout(() => {
      fn();
      setVisible(true);
    }, 180);
  }

  function goNext() { transition(() => setStep(s => s + 1)); }
  function goBack() { transition(() => setStep(s => s - 1)); }

  function togglePriority(p: string) {
    setPriorities(prev => prev.includes(p) ? prev.filter(x => x !== p) : [...prev, p]);
  }

  function canProceed() {
    if (step === 1) return budget !== '';
    if (step === 2) return useCase !== '';
    if (step === 3) return priorities.length > 0;
    return true; // step 4 is optional
  }

  async function handleSubmit() {
    setError('');
    setLoading(true);
    try {
      const answers: QuizAnswers = { budget, useCase, priorities, extraInfo };
      const response = await getRecommendations(answers);
      onComplete(answers, response);
    } catch {
      setError('Something went wrong. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">

        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-gray-900">Find Your Perfect Car</h1>
          <p className="mt-1 text-gray-500 text-sm">Answer 4 quick questions — we'll do the rest</p>
        </div>

        {/* Progress bar */}
        <div className="mb-8">
          <div className="flex justify-between mb-2">
            {STEP_LABELS.map((label, i) => (
              <span
                key={label}
                className={[
                  'text-xs font-medium transition-colors',
                  i + 1 < step  ? 'text-blue-400' :
                  i + 1 === step ? 'text-blue-600' :
                  'text-gray-300',
                ].join(' ')}
              >
                {label}
              </span>
            ))}
          </div>
          <div className="h-1.5 w-full bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-blue-500 rounded-full transition-all duration-500 ease-out"
              style={{ width: `${(step / STEP_LABELS.length) * 100}%` }}
            />
          </div>
          <p className="mt-2 text-right text-xs text-gray-400">Step {step} of {STEP_LABELS.length}</p>
        </div>

        {/* Step content with fade + slide transition */}
        <div
          className={[
            'transition-all duration-200 ease-out',
            visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-3',
          ].join(' ')}
        >
          {step === 1 && <StepBudget    selected={budget}    onSelect={setBudget}    />}
          {step === 2 && <StepUse       selected={useCase}   onSelect={setUseCase}   />}
          {step === 3 && <StepPriorities selected={priorities} onToggle={togglePriority} />}
          {step === 4 && <StepExtra     value={extraInfo}    onChange={setExtraInfo}  />}
        </div>

        {/* Error flash */}
        {error && (
          <div className="mt-5 flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            <span className="mt-0.5">⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {/* Navigation */}
        <div className="mt-8 flex items-center justify-between">
          <button
            onClick={goBack}
            disabled={step === 1}
            className="px-4 py-2 text-sm text-gray-500 hover:text-gray-800 disabled:invisible transition-colors"
          >
            ← Back
          </button>

          {step < 4 ? (
            <button
              onClick={goNext}
              disabled={!canProceed()}
              className="px-6 py-2.5 rounded-xl bg-blue-600 text-white text-sm font-semibold shadow-sm hover:bg-blue-700 active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed transition-all"
            >
              Next →
            </button>
          ) : (
            <button
              onClick={handleSubmit}
              disabled={loading}
              className="flex items-center gap-2 px-7 py-2.5 rounded-xl bg-blue-600 text-white text-sm font-semibold shadow-sm hover:bg-blue-700 active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed transition-all"
            >
              {loading ? (
                <>
                  <svg className="animate-spin h-4 w-4 shrink-0" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                  </svg>
                  Finding cars…
                </>
              ) : (
                '🚗 Find My Car'
              )}
            </button>
          )}
        </div>

      </div>
    </div>
  );
}