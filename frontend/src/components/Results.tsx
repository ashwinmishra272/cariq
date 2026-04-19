import { useState } from 'react';
import type { Car, QuizAnswers, RecommendResponse } from '../types';

interface Props {
  response: RecommendResponse;
  answers: QuizAnswers;
  onReset: () => void;
}

const RANK = [
  { label: 'TOP PICK',      code: '01', color: '#c9a84c',  dimColor: 'rgba(201,168,76,0.1)',  borderColor: 'rgba(201,168,76,0.25)' },
  { label: 'RUNNER UP',     code: '02', color: '#7a7a7a',  dimColor: 'rgba(122,122,122,0.08)', borderColor: 'rgba(122,122,122,0.2)' },
  { label: 'ALSO CONSIDER', code: '03', color: '#7c5a3e',  dimColor: 'rgba(124,90,62,0.08)',  borderColor: 'rgba(124,90,62,0.2)'  },
];

type CRow = { label: string; fn: (c: Car) => string };
const COMPARE_ROWS: CRow[] = [
  { label: 'PRICE',       fn: c => `₹${c.priceLakh.toFixed(2)}L` },
  { label: 'FUEL',        fn: c => c.fuelType },
  { label: 'GEARBOX',     fn: c => c.transmission },
  { label: 'EFFICIENCY',  fn: c => c.fuelType === 'Electric' ? `${c.mileage} km` : `${c.mileage} km/l` },
  { label: 'SEATING',     fn: c => `${c.seating} seats` },
  { label: 'SAFETY',      fn: c => `${c.safetyRating} / 5` },
  { label: 'USER RATING', fn: c => `${c.userRating.toFixed(1)} / 5` },
];

const D: React.CSSProperties = { fontFamily: 'var(--font-display)', letterSpacing: '1.5px' };
const M: React.CSSProperties = { fontFamily: 'var(--font-mono)',    letterSpacing: '2px'   };
const B: React.CSSProperties = { fontFamily: 'var(--font-body)',    letterSpacing: '0.3px' };

/* ── Compare Modal ─────────────────────────────────────── */
function CompareModal({ a, b, onClose }: { a: Car; b: Car; onClose: () => void }) {
  return (
    <div
      style={{
        position: 'fixed', inset: 0, zIndex: 50,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: 'rgba(0,0,0,0.82)', padding: '20px',
        animation: 'fadeIn 0.15s ease',
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div style={{
        background: 'var(--bg-2)', border: '1px solid var(--border)',
        borderRadius: '4px', width: '100%', maxWidth: '660px',
        overflow: 'hidden', animation: 'scaleIn 0.18s ease',
      }}>
        {/* Header */}
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '18px 26px', borderBottom: '1px solid var(--border)',
        }}>
          <span style={{ ...D, fontSize: '20px', color: 'var(--text)' }}>COMPARISON</span>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', fontSize: '18px', lineHeight: 1, padding: '4px' }}
            aria-label="Close"
          >
            ✕
          </button>
        </div>

        {/* Car name headers */}
        <div style={{ display: 'grid', gridTemplateColumns: '130px 1fr 1fr', borderBottom: '1px solid var(--border)' }}>
          <div />
          {[a, b].map((car, i) => (
            <div key={car.id} style={{
              padding: '18px 22px', borderLeft: '1px solid var(--border)',
              background: i === 0 ? 'rgba(201,168,76,0.05)' : 'transparent',
            }}>
              <div style={{ ...M, fontSize: '11px', color: i === 0 ? '#d4af52' : 'var(--text-muted)', marginBottom: '6px' }}>
                {i === 0 ? 'TOP PICK' : 'RUNNER UP'}
              </div>
              <div style={{ ...D, fontSize: '18px', color: 'var(--text)', lineHeight: 1.1 }}>
                {car.make} {car.model}
              </div>
              <div style={{ ...B, fontSize: '20px', fontWeight: 600, color: i === 0 ? '#d4af52' : '#9a9a9a', marginTop: '6px' }}>
                ₹{car.priceLakh.toFixed(2)}L
              </div>
            </div>
          ))}
        </div>

        {/* Rows */}
        {COMPARE_ROWS.map((row, i) => (
          <div key={row.label} style={{ display: 'grid', gridTemplateColumns: '130px 1fr 1fr', borderBottom: '1px solid #141414' }}>
            <div style={{
              padding: '12px 22px', ...M, fontSize: '11px', color: 'var(--text-muted)',
              display: 'flex', alignItems: 'center',
              background: i % 2 === 0 ? 'transparent' : 'rgba(255,255,255,0.015)',
            }}>
              {row.label}
            </div>
            {[a, b].map((car, ci) => (
              <div key={car.id} style={{
                padding: '12px 22px', borderLeft: '1px solid #141414',
                ...B, fontSize: '15px', fontWeight: 500, color: 'var(--text-dim)',
                display: 'flex', alignItems: 'center',
                background: ci === 0
                  ? (i % 2 === 0 ? 'rgba(201,168,76,0.02)' : 'rgba(201,168,76,0.04)')
                  : (i % 2 === 0 ? 'transparent' : 'rgba(255,255,255,0.01)'),
              }}>
                {row.fn(car)}
              </div>
            ))}
          </div>
        ))}

        <div style={{ padding: '18px 26px' }}>
          <button
            onClick={onClose}
            style={{
              ...D, fontSize: '14px', letterSpacing: '2px',
              padding: '10px 28px',
              background: 'var(--surface)', color: 'var(--text-muted)',
              border: '1px solid var(--border)', borderRadius: '2px', cursor: 'pointer',
              transition: 'all 0.13s ease',
            }}
            onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--text)'; }}
            onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--text-muted)'; }}
          >
            CLOSE
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Car Card ──────────────────────────────────────────── */
function CarCard({ car, rank, reasoning }: { car: Car; rank: number; reasoning: string }) {
  const rd = RANK[rank] ?? RANK[2];
  const pros = car.pros.split(',').map(p => p.trim()).filter(Boolean);
  const cons = car.cons.split(',').map(c => c.trim()).filter(Boolean);
  const mileageLabel = car.fuelType === 'Electric' ? `${car.mileage} km range` : `${car.mileage} km/l`;

  return (
    <article style={{
      background: 'var(--bg-2)', border: '1px solid var(--border)',
      borderRadius: '4px', overflow: 'hidden',
      display: 'flex', flexDirection: 'column',
      animation: `fadeUp 0.45s ${rank * 0.1}s ease both`,
      transition: 'border-color 0.2s ease',
    }}
      onMouseEnter={e => { (e.currentTarget as HTMLElement).style.borderColor = rd.borderColor; }}
      onMouseLeave={e => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--border)'; }}
    >
      {/* Rank strip */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '10px 22px', borderBottom: '1px solid var(--border)',
        background: rd.dimColor,
      }}>
        <span style={{ ...M, fontSize: '11px', color: rd.color }}>{rd.label}</span>
        <span style={{ ...D, fontSize: '28px', color: rd.color, opacity: 0.25 }}>{rd.code}</span>
      </div>

      <div style={{ padding: '24px 22px', flex: 1, display: 'flex', flexDirection: 'column', gap: '20px' }}>

        {/* Make / Model / Price */}
        <div>
          <div style={{ ...M, fontSize: '11px', color: 'var(--text-muted)', marginBottom: '6px' }}>
            {car.make.toUpperCase()}
          </div>
          <h3 style={{ ...D, fontSize: '42px', lineHeight: 1, color: 'var(--text)', margin: '0 0 2px' }}>
            {car.model}
          </h3>
          <div style={{ ...B, fontSize: '14px', color: 'var(--text-muted)', fontWeight: 400, marginBottom: '10px' }}>
            {car.variant}
          </div>
          <div style={{ ...D, fontSize: '32px', color: rd.color }}>
            ₹{car.priceLakh.toFixed(2)}<span style={{ fontSize: '16px', opacity: 0.65 }}> L</span>
          </div>
        </div>

        {/* Spec grid */}
        <div style={{
          display: 'grid', gridTemplateColumns: '1fr 1fr',
          gap: '1px', background: 'var(--border)',
          border: '1px solid var(--border)', borderRadius: '2px', overflow: 'hidden',
        }}>
          {[
            { k: 'FUEL',       v: car.fuelType },
            { k: 'GEARBOX',    v: car.transmission },
            { k: 'EFFICIENCY', v: mileageLabel },
            { k: 'SEATING',    v: `${car.seating} seats` },
          ].map(spec => (
            <div key={spec.k} style={{ padding: '11px 14px', background: 'var(--bg-2)' }}>
              <div style={{ ...M, fontSize: '10px', color: 'var(--text-muted)', marginBottom: '4px' }}>
                {spec.k}
              </div>
              <div style={{ ...B, fontSize: '15px', fontWeight: 500, color: 'var(--text-dim)' }}>
                {spec.v}
              </div>
            </div>
          ))}
        </div>

        {/* Safety + User ratings */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ ...M, fontSize: '11px', color: 'var(--text-muted)', minWidth: '72px' }}>SAFETY</span>
            <div style={{ display: 'flex', gap: '3px' }}>
              {Array.from({ length: 5 }, (_, i) => (
                <div key={i} style={{
                  width: '18px', height: '3px', borderRadius: '1px',
                  background: i < car.safetyRating ? rd.color : 'var(--border-light)',
                }} />
              ))}
            </div>
            <span style={{ ...M, fontSize: '11px', color: rd.color }}>{car.safetyRating}/5</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span style={{ ...M, fontSize: '11px', color: 'var(--text-muted)', minWidth: '72px' }}>USER RATING</span>
            <div style={{ display: 'flex', gap: '3px' }}>
              {Array.from({ length: 5 }, (_, i) => (
                <div key={i} style={{
                  width: '18px', height: '3px', borderRadius: '1px',
                  background: i < Math.round(car.userRating) ? 'var(--green)' : 'var(--border-light)',
                }} />
              ))}
            </div>
            <span style={{ ...M, fontSize: '11px', color: 'var(--green)' }}>{car.userRating.toFixed(1)}/5</span>
          </div>
        </div>

        {/* Why this car */}
        {reasoning && (
          <div style={{
            padding: '14px 16px',
            background: 'var(--surface)', border: '1px solid var(--border)',
            borderLeft: `2px solid ${rd.color}`, borderRadius: '2px',
          }}>
            <div style={{ ...M, fontSize: '10px', color: rd.color, marginBottom: '8px' }}>
              WHY THIS CAR
            </div>
            <p style={{ ...B, fontSize: '15px', color: 'var(--text-dim)', fontWeight: 400, lineHeight: 1.55, margin: 0 }}>
              {reasoning}
            </p>
          </div>
        )}

        {/* Strengths + Trade-offs */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '14px' }}>
          {pros.length > 0 && (
            <div>
              <div style={{ ...M, fontSize: '10px', color: 'var(--green)', marginBottom: '8px' }}>STRENGTHS</div>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '5px' }}>
                {pros.map(p => (
                  <li key={p} style={{ ...B, fontSize: '14px', color: 'var(--text-dim)', fontWeight: 400, display: 'flex', gap: '7px' }}>
                    <span style={{ color: 'var(--green)', flexShrink: 0 }}>+</span>{p}
                  </li>
                ))}
              </ul>
            </div>
          )}
          {cons.length > 0 && (
            <div>
              <div style={{ ...M, fontSize: '10px', color: 'var(--red)', marginBottom: '8px' }}>TRADE-OFFS</div>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '5px' }}>
                {cons.map(c => (
                  <li key={c} style={{ ...B, fontSize: '14px', color: 'var(--text-dim)', fontWeight: 400, display: 'flex', gap: '7px' }}>
                    <span style={{ color: 'var(--red)', flexShrink: 0 }}>−</span>{c}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

      </div>
    </article>
  );
}

/* ── Results Page ──────────────────────────────────────── */
export default function Results({ response, answers, onReset }: Props) {
  const [showCompare, setShowCompare] = useState(false);
  const { cars, reasoning, tradeoffs } = response;

  return (
    <div style={{ minHeight: '100svh', background: 'var(--bg)' }}>

      {/* Sticky header */}
      <header style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '18px 32px', borderBottom: '1px solid var(--border)',
        background: 'var(--bg)', position: 'sticky', top: 0, zIndex: 10,
      }}>
        <div style={{ ...D, fontSize: '20px', color: 'var(--text)' }}>
          CAR<span style={{ color: 'var(--accent)' }}>IQ</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {cars.length >= 2 && (
            <button
              onClick={() => setShowCompare(true)}
              style={{
                ...M, fontSize: '11px',
                padding: '9px 18px',
                background: 'transparent', color: 'var(--accent)',
                border: '1px solid var(--accent-border)',
                borderRadius: '2px', cursor: 'pointer', transition: 'all 0.13s ease',
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--accent-glow)'; }}
              onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
            >
              COMPARE ↕
            </button>
          )}
          <button
            onClick={onReset}
            style={{
              ...M, fontSize: '11px',
              padding: '9px 18px',
              background: 'transparent', color: 'var(--text-muted)',
              border: '1px solid var(--border)',
              borderRadius: '2px', cursor: 'pointer', transition: 'all 0.13s ease',
            }}
            onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--text)'; (e.currentTarget as HTMLElement).style.borderColor = 'var(--border-light)'; }}
            onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--text-muted)'; (e.currentTarget as HTMLElement).style.borderColor = 'var(--border)'; }}
          >
            ← RESTART
          </button>
        </div>
      </header>

      {/* Hero strip */}
      <div style={{
        padding: 'clamp(48px,7vw,88px) 32px clamp(36px,5vw,60px)',
        borderBottom: '1px solid var(--border)',
        position: 'relative', overflow: 'hidden',
      }}>
        {/* Background word */}
        <div aria-hidden style={{
          position: 'absolute', right: '-20px', top: '-30px',
          ...D, fontSize: 'clamp(120px, 18vw, 220px)', lineHeight: 1,
          color: 'var(--border)', pointerEvents: 'none', userSelect: 'none',
        }}>
          PICKS
        </div>

        <div style={{ position: 'relative', zIndex: 1 }}>
          <div style={{ ...M, fontSize: '11px', color: 'var(--accent)', marginBottom: '16px' }}>
            YOUR SHORTLIST — {cars.length} CAR{cars.length !== 1 ? 'S' : ''}
          </div>
          <h1 style={{
            ...D, fontSize: 'clamp(52px, 9vw, 120px)',
            lineHeight: 1, color: 'var(--text)', margin: '0 0 22px',
          }}>
            HAND-PICKED<br />
            <span style={{ color: 'var(--accent)' }}>FOR YOU.</span>
          </h1>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px 20px', alignItems: 'center' }}>
            {[
              `Budget: ₹${answers.budget}L`,
              `Use: ${answers.useCase}`,
              ...(answers.priorities.length > 0 ? [`Priorities: ${answers.priorities.join(', ')}`] : []),
            ].map((tag, i) => (
              <span key={i} style={{ ...B, fontSize: '16px', color: 'var(--text-dim)', fontWeight: 400 }}>
                {i > 0 && <span style={{ marginRight: '20px', color: '#484848' }}>·</span>}
                {tag}
              </span>
            ))}
          </div>
        </div>
      </div>

      {/* Card grid */}
      <main style={{ padding: 'clamp(28px,4vw,48px) 32px' }}>
        {cars.length > 0 ? (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
            gap: '14px',
            alignItems: 'start',
          }}>
            {cars.map((car, i) => (
              <CarCard key={car.id} car={car} rank={i} reasoning={reasoning[String(car.id)] ?? ''} />
            ))}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '80px 0' }}>
            <div style={{ ...D, fontSize: '64px', color: 'var(--border)', marginBottom: '16px' }}>
              NO RESULTS
            </div>
            <p style={{ ...B, fontSize: '18px', color: 'var(--text-muted)', fontWeight: 300 }}>
              Try adjusting your budget or priorities
            </p>
          </div>
        )}

        {/* Trade-off analysis */}
        {tradeoffs && (
          <div style={{
            marginTop: '32px', padding: '24px 28px',
            background: 'var(--bg-2)', border: '1px solid var(--border)',
            borderLeft: '3px solid var(--accent)', borderRadius: '3px',
            animation: 'fadeUp 0.5s 0.35s ease both',
          }}>
            <div style={{ ...M, fontSize: '11px', color: 'var(--accent)', marginBottom: '10px' }}>
              TRADE-OFF ANALYSIS
            </div>
            <p style={{
              ...B, fontSize: '17px', fontWeight: 400,
              color: 'var(--text-dim)', lineHeight: 1.55, margin: 0,
            }}>
              {tradeoffs}
            </p>
          </div>
        )}
      </main>

      {showCompare && cars.length >= 2 && (
        <CompareModal a={cars[0]} b={cars[1]} onClose={() => setShowCompare(false)} />
      )}
    </div>
  );
}
