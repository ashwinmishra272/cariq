export interface Car {
  id: number;
  make: string;
  model: string;
  variant: string;
  priceLakh: number;
  fuelType: string;
  transmission: string;
  mileage: number;
  seating: number;
  safetyRating: number;
  userRating: number;
  pros: string;
  cons: string;
}

export interface QuizAnswers {
  budget: string;
  useCase: string;
  priorities: string[];
  extraInfo: string;
}

export interface RecommendResponse {
  sessionId: string;
  cars: Car[];
  reasoning: Record<string, string>;
  tradeoffs: string;
}