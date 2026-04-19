import { useState } from 'react';
import type { QuizAnswers, RecommendResponse } from './types';
import QuizFlow from './components/QuizFlow';
import Results from './components/Results';

type View = 'quiz' | 'results';

function App() {
  const [view, setView] = useState<View>('quiz');
  const [quizAnswers, setQuizAnswers] = useState<QuizAnswers | null>(null);
  const [recommendResponse, setRecommendResponse] = useState<RecommendResponse | null>(null);

  function handleComplete(answers: QuizAnswers, response: RecommendResponse) {
    setQuizAnswers(answers);
    setRecommendResponse(response);
    setView('results');
  }

  function handleReset() {
    setQuizAnswers(null);
    setRecommendResponse(null);
    setView('quiz');
  }

  return (
    <>
      {view === 'quiz' && (
        <QuizFlow onComplete={handleComplete} />
      )}
      {view === 'results' && recommendResponse && quizAnswers && (
        <Results response={recommendResponse} answers={quizAnswers} onReset={handleReset} />
      )}
    </>
  );
}

export default App;