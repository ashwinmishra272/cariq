import { useState } from 'react';
import type { QuizAnswers, RecommendResponse } from '../types';
import { getRecommendations } from '../api/cariq';

interface Props {
  onComplete: (answers: QuizAnswers, response: RecommendResponse) => void;
}

const BUDGET_OPTIONS = [
  { label: 'Under 5L',   sub: 'Up to ₹5 Lakh',       value: '0-5',    code: '01' },
  { label: '5 – 10L',   sub: '₹5 – ₹10 Lakh',        value: '5-10',   code: '02' },
  { label: '10 – 15L',  sub: '₹10 – ₹15 Lakh',       value: '10-15',  code: '03' },
  { label: '15 – 25L',  sub: '₹15 – ₹25 Lakh',       value: '15-25',  code: '04' },
  { label: 'Above 25L', sub: '₹25 Lakh and above',    value: '25-9999', code: '05' },
];

const USE_OPTIONS = [
  { label: 'City Commute', desc: 'Stop-start traffic, tight parking spots', value: 'city commute', sym: '◈' },
  { label: 'Family Car',   desc: 'Space, safety, and comfort for all',        value: 'family car',   sym: '◉' },
  { label: 'Highway',      desc: 'Long-haul drives, open-road performance',   value: 'highway',      sym: '◎' },
  { label: 'First Car',    desc: 'Easy to drive, forgiving, low stress',       value: 'first car',    sym: '◌' },
];

const PRIORITY_OPTIONS = [
  { label: 'Good Mileage',    tag: 'MPG'   },
  { label: 'Automatic',       tag: 'AUTO'  },
  { label: '7 Seats',         tag: '7-STR' },
  { label: 'Low Maintenance', tag: 'MNTN'  },
  { label: 'Safety 4★+',      tag: 'SAFE'  },
];

const STEPS = [
  { idx: '01', title: 'BUDGET',     sub: 'Set your price range' },
  { idx: '02', title: 'USAGE',      sub: 'How will you drive it?' },
  { idx: '03', title: 'PRIORITIES', sub: 'What matters most to you?' },
  { idx: '04', title: 'DETAILS',    sub: 'Anything else we should know?' },
];

const D: React.CSSProperties = { fontFamily: 'var(--font-display)', letterSpacing: '1.5px' };
const M: React.CSSProperties = { fontFamily: 'var(--font-mono)', letterSpacing: '2px' };
const B: React.CSSProperties = { fontFamily: 'var(--font-body)', letterSpacing: '0.3px' };

export default function QuizFlow({ onComplete }: Props) {
  const [step, setStep]             = useState(1);
  const [visible, setVisible]       = useState(true);
  const [budget, setBudget]         = useState('');
  const [useCase, setUseCase]       = useState('');
  const [priorities, setPriorities] = useState<string[]>([]);
  const [extraInfo, setExtraInfo]   = useState('');
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState('');

  function transition(fn: () => void) {
    setVisible(false);
    setTimeout(() => { fn(); setVisible(true); }, 200);
  }

  const goNext = () => transition(() => setStep(s => s + 1));
  const goBack = () => transition(() => setStep(s => s - 1));

  function togglePriority(p: string) {
    setPriorities(prev => prev.includes(p) ? prev.filter(x => x !== p) : [...prev, p]);
  }

  function canProceed() {
    if (step === 1) return budget !== '';
    if (step === 2) return useCase !== '';
    if (step === 3) return priorities.length > 0;
    return true;
  }

  async function handleSubmit() {
    setError('');
    setLoading(true);
    try {
      const answers: QuizAnswers = { budget, useCase, priorities, extraInfo };
      const response = await getRecommendations(answers);
      onComplete(answers, response);
    } catch {
      setError('Connection failed. Check your network and try again.');
    } finally {
      setLoading(false);
    }
  }

  const cur = STEPS[step - 1];

  return (
    <div style={{ minHeight: '100svh', display: 'flex', flexDirection: 'column', background: 'var(--bg)' }}>

      {/* ── Top Bar ──────────────────────────────────────── */}
      <header style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '18px 32px', borderBottom: '1px solid var(--border)',
        background: 'var(--bg)', position: 'sticky', top: 0, zIndex: 20,
      }}>
        <div style={{ ...D, fontSize: '20px', color: 'var(--text)' }}>
          CAR<span style={{ color: 'var(--accent)' }}>IQ</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <span style={{ ...M, fontSize: '11px', color: 'var(--text-muted)' }}>
            {cur.idx} / 04
          </span>
          <div style={{ width: '100px', height: '2px', background: 'var(--border)', borderRadius: '1px', overflow: 'hidden' }}>
            <div style={{
              height: '100%', background: 'var(--accent)',
              width: `${(step / 4) * 100}%`,
              transition: 'width 0.45s cubic-bezier(0.4,0,0.2,1)',
            }} />
          </div>
        </div>
      </header>

      {/* ── Split Layout ──────────────────────────────────── */}
      <div style={{ flex: 1, display: 'grid', gridTemplateColumns: 'clamp(280px, 30%, 400px) 1fr' }}>

        {/* Left: Question Panel */}
        <div style={{
          background: 'var(--bg-2)', borderRight: '1px solid var(--border)',
          padding: '52px 36px 44px',
          display: 'flex', flexDirection: 'column', justifyContent: 'space-between',
          position: 'sticky', top: '57px', height: 'calc(100svh - 57px)',
          overflow: 'hidden',
        }}>
          {/* Ghost number */}
          <div aria-hidden style={{
            position: 'absolute', bottom: '-24px', right: '-8px',
            ...D, fontSize: '180px', lineHeight: 1,
            color: 'var(--border)', pointerEvents: 'none', userSelect: 'none',
          }}>
            {cur.idx}
          </div>

          <div style={{ position: 'relative', zIndex: 1 }}>
            <div style={{ ...M, fontSize: '11px', color: 'var(--accent)', marginBottom: '24px' }}>
              STEP {cur.idx} OF 04
            </div>
            <h2 style={{
              ...D, fontSize: 'clamp(48px, 5.5vw, 76px)',
              lineHeight: 1, color: 'var(--text)', marginBottom: '12px',
            }}>
              {cur.title}
            </h2>
            <p style={{ ...B, fontSize: '17px', color: 'var(--text-dim)', fontWeight: 400 }}>
              {cur.sub}
            </p>
          </div>

          {/* Step lines */}
          <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {STEPS.map((s, i) => {
              const done    = i + 1 < step;
              const active  = i + 1 === step;
              return (
                <div key={s.idx} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{
                    flex: 1, height: '1px',
                    background: done || active ? 'var(--accent)' : 'var(--border)',
                    opacity: done ? 0.45 : 1,
                    transition: 'background 0.3s ease',
                  }} />
                  <span style={{
                    ...M, fontSize: '10px',
                    color: done || active ? 'var(--accent)' : '#3e3e3e',
                    transition: 'color 0.3s ease', minWidth: '16px', textAlign: 'right',
                  }}>
                    {s.idx}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right: Options Panel */}
        <div style={{ padding: 'clamp(32px,5vw,68px) clamp(24px,6vw,80px)', overflowY: 'auto' }}>
          <div style={{
            opacity: visible ? 1 : 0,
            transform: visible ? 'translateX(0)' : 'translateX(14px)',
            transition: 'opacity 0.2s ease, transform 0.2s ease',
          }}>

            {/* Step 1 — Budget */}
            {step === 1 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                {BUDGET_OPTIONS.map((opt, i) => (
                  <button
                    key={opt.value}
                    onClick={() => setBudget(opt.value)}
                    style={{
                      display: 'flex', alignItems: 'center', gap: '18px',
                      padding: '17px 20px',
                      background: budget === opt.value ? 'var(--accent-glow)' : 'var(--surface)',
                      border: budget === opt.value ? '1px solid var(--accent-border)' : '1px solid var(--border)',
                      borderRadius: '3px', cursor: 'pointer', textAlign: 'left',
                      transition: 'all 0.13s ease',
                      animation: `fadeUp 0.35s ${i * 0.05}s ease both`,
                    }}
                    onMouseEnter={e => { if (budget !== opt.value) (e.currentTarget as HTMLElement).style.borderColor = 'var(--border-light)'; }}
                    onMouseLeave={e => { if (budget !== opt.value) (e.currentTarget as HTMLElement).style.borderColor = 'var(--border)'; }}
                  >
                    <span style={{ ...M, fontSize: '11px', color: budget === opt.value ? 'var(--accent)' : 'var(--text-muted)', minWidth: '18px' }}>
                      {opt.code}
                    </span>
                    <span style={{ ...D, fontSize: '26px', color: budget === opt.value ? 'var(--text)' : 'var(--text-dim)', transition: 'color 0.13s', flex: 1 }}>
                      {opt.label}
                    </span>
                    <span style={{ ...B, fontSize: '14px', color: 'var(--text-muted)', fontWeight: 400 }}>
                      {opt.sub}
                    </span>
                    {budget === opt.value && <span style={{ color: 'var(--accent)', fontSize: '13px' }}>→</span>}
                  </button>
                ))}
              </div>
            )}

            {/* Step 2 — Use Case */}
            {step === 2 && (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '8px' }}>
                {USE_OPTIONS.map((opt, i) => (
                  <button
                    key={opt.value}
                    onClick={() => setUseCase(opt.value)}
                    style={{
                      padding: '26px 20px',
                      background: useCase === opt.value ? 'var(--accent-glow)' : 'var(--surface)',
                      border: useCase === opt.value ? '1px solid var(--accent-border)' : '1px solid var(--border)',
                      borderRadius: '3px', cursor: 'pointer', textAlign: 'left',
                      transition: 'all 0.13s ease',
                      animation: `fadeUp 0.35s ${i * 0.065}s ease both`,
                    }}
                    onMouseEnter={e => { if (useCase !== opt.value) (e.currentTarget as HTMLElement).style.borderColor = 'var(--border-light)'; }}
                    onMouseLeave={e => { if (useCase !== opt.value) (e.currentTarget as HTMLElement).style.borderColor = 'var(--border)'; }}
                  >
                    <div style={{ fontSize: '20px', color: useCase === opt.value ? 'var(--accent)' : '#484848', marginBottom: '16px', lineHeight: 1, transition: 'color 0.13s' }}>
                      {opt.sym}
                    </div>
                    <div style={{ ...D, fontSize: '22px', color: useCase === opt.value ? 'var(--text)' : 'var(--text-dim)', marginBottom: '6px', transition: 'color 0.13s' }}>
                      {opt.label}
                    </div>
                    <div style={{ ...B, fontSize: '14px', color: 'var(--text-muted)', fontWeight: 400, lineHeight: 1.45 }}>
                      {opt.desc}
                    </div>
                  </button>
                ))}
              </div>
            )}

            {/* Step 3 — Priorities */}
            {step === 3 && (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <p style={{ ...M, fontSize: '11px', color: 'var(--text-muted)', marginBottom: '14px' }}>
                  SELECT ALL THAT APPLY
                </p>
                {PRIORITY_OPTIONS.map((opt, i) => {
                  const active = priorities.includes(opt.label);
                  return (
                    <button
                      key={opt.label}
                      onClick={() => togglePriority(opt.label)}
                      style={{
                        display: 'flex', alignItems: 'center', gap: '14px',
                        padding: '15px 20px',
                        background: active ? 'var(--accent-glow)' : 'var(--surface)',
                        border: active ? '1px solid var(--accent-border)' : '1px solid var(--border)',
                        borderRadius: '3px', cursor: 'pointer', textAlign: 'left',
                        transition: 'all 0.13s ease',
                        animation: `fadeUp 0.35s ${i * 0.06}s ease both`,
                      }}
                    >
                      <div style={{
                        width: '16px', height: '16px', flexShrink: 0,
                        border: active ? '2px solid var(--accent)' : '2px solid #404040',
                        borderRadius: '2px',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        background: active ? 'var(--accent)' : 'transparent',
                        transition: 'all 0.13s ease',
                      }}>
                        {active && (
                          <svg width="8" height="6" viewBox="0 0 8 6" fill="none">
                            <path d="M1 3L3 5L7 1" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        )}
                      </div>
                      <span style={{ ...B, fontSize: '19px', fontWeight: 500, color: active ? 'var(--text)' : 'var(--text-dim)', flex: 1, transition: 'color 0.13s' }}>
                        {opt.label}
                      </span>
                      <span style={{ ...M, fontSize: '11px', color: active ? 'var(--accent)' : '#484848', transition: 'color 0.13s' }}>
                        {opt.tag}
                      </span>
                    </button>
                  );
                })}
              </div>
            )}

            {/* Step 4 — Extra info */}
            {step === 4 && (
              <div style={{ animation: 'fadeUp 0.35s ease both' }}>
                <textarea
                  rows={7}
                  value={extraInfo}
                  onChange={e => setExtraInfo(e.target.value)}
                  placeholder="e.g. I want good boot space, prefer dark colours, drive mostly at night, need a sunroof..."
                  style={{
                    width: '100%',
                    background: 'var(--surface)', border: '1px solid var(--border)',
                    borderRadius: '3px', padding: '20px',
                    ...B, fontSize: '17px', fontWeight: 300,
                    color: 'var(--text)', lineHeight: 1.6, resize: 'none', outline: 'none',
                    transition: 'border-color 0.14s ease',
                  }}
                  onFocus={e => { (e.target as HTMLElement).style.borderColor = 'var(--accent-border)'; }}
                  onBlur={e => { (e.target as HTMLElement).style.borderColor = 'var(--border)'; }}
                />
                <p style={{ ...M, fontSize: '11px', color: 'var(--text-muted)', marginTop: '10px' }}>
                  OPTIONAL — skip if nothing specific comes to mind
                </p>
              </div>
            )}

            {/* Error */}
            {error && (
              <div style={{
                marginTop: '20px', padding: '13px 16px',
                background: 'rgba(255,80,80,0.05)', border: '1px solid rgba(255,80,80,0.2)',
                borderRadius: '3px', ...B, fontSize: '15px', color: '#ff7070',
              }}>
                {error}
              </div>
            )}

            {/* Navigation */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '44px' }}>
              <button
                onClick={goBack}
                disabled={step === 1}
                style={{
                  ...M, fontSize: '10px', letterSpacing: '2px',
                  color: step === 1 ? '#3a3a3a' : 'var(--text-muted)',
                  background: 'none', border: 'none', padding: 0,
                  cursor: step === 1 ? 'default' : 'pointer',
                  transition: 'color 0.14s ease',
                }}
                onMouseEnter={e => { if (step > 1) (e.currentTarget as HTMLElement).style.color = 'var(--text)'; }}
                onMouseLeave={e => { if (step > 1) (e.currentTarget as HTMLElement).style.color = 'var(--text-muted)'; }}
              >
                ← BACK
              </button>

              {step < 4 ? (
                <button
                  onClick={goNext}
                  disabled={!canProceed()}
                  style={{
                    ...D, fontSize: '16px', letterSpacing: '3px',
                    padding: '12px 36px',
                    background: canProceed() ? 'var(--accent)' : 'var(--surface)',
                    color: canProceed() ? '#fff' : '#484848',
                    border: canProceed() ? '1px solid var(--accent)' : '1px solid var(--border)',
                    borderRadius: '2px',
                    cursor: canProceed() ? 'pointer' : 'not-allowed',
                    transition: 'all 0.16s ease',
                  }}
                  onMouseEnter={e => { if (canProceed()) (e.currentTarget as HTMLElement).style.background = '#ff6b0a'; }}
                  onMouseLeave={e => { if (canProceed()) (e.currentTarget as HTMLElement).style.background = 'var(--accent)'; }}
                >
                  CONTINUE →
                </button>
              ) : (
                <button
                  onClick={handleSubmit}
                  disabled={loading}
                  style={{
                    ...D, fontSize: '16px', letterSpacing: '3px',
                    padding: '12px 36px',
                    background: loading ? 'var(--surface)' : 'var(--accent)',
                    color: loading ? '#5a5a5a' : '#fff',
                    border: loading ? '1px solid var(--border)' : '1px solid var(--accent)',
                    borderRadius: '2px',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    display: 'flex', alignItems: 'center', gap: '10px',
                    transition: 'all 0.16s ease',
                  }}
                  onMouseEnter={e => { if (!loading) (e.currentTarget as HTMLElement).style.background = '#ff6b0a'; }}
                  onMouseLeave={e => { if (!loading) (e.currentTarget as HTMLElement).style.background = 'var(--accent)'; }}
                >
                  {loading ? (
                    <>
                      <svg style={{ animation: 'spin 0.8s linear infinite', width: '13px', height: '13px', flexShrink: 0 }} viewBox="0 0 24 24" fill="none">
                        <circle style={{ opacity: 0.2 }} cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
                        <path style={{ opacity: 0.9 }} fill="currentColor" d="M4 12a8 8 0 018-8v8H4z" />
                      </svg>
                      ANALYSING...
                    </>
                  ) : 'GET MY SHORTLIST →'}
                </button>
              )}
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}
