import axios from 'axios';
import type { Car, QuizAnswers, RecommendResponse } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
});

// ── shape returned by the Spring Boot backend ─────────────────────────────────
interface BackendCar {
  id: number;
  make: string;
  model: string;
  variant: string;
  price: number;          // raw INR
  fuelType: string;
  transmission: string;
  mileage: number;
  seating: number;
  safetyRating: number;
  userRating: number;
  pros: string;
  cons: string;
}

interface BackendRecommendResponse {
  sessionId: string;
  shortlist: BackendCar[];
  reasoning: Record<string, string>;
  tradeoff: string;
}

function mapCar(raw: BackendCar): Car {
  return { ...raw, priceLakh: raw.price / 100_000 };
}

export async function getRecommendations(answers: QuizAnswers): Promise<RecommendResponse> {
  const { data } = await api.post<BackendRecommendResponse>('/api/recommend', {
    budget: answers.budget,
    use: answers.useCase,
    priorities: answers.priorities,
    extra: answers.extraInfo,
  });

  return {
    sessionId: data.sessionId,
    cars: data.shortlist.map(mapCar),
    reasoning: data.reasoning,
    tradeoffs: data.tradeoff,
  };
}