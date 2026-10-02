'use client';

import React from 'react';
import DefaultWrapper from '@/layout/DefaultWrapper';
import TakeQuiz from '@/components/quiz/TakeQuiz';
import ClientWrapper from '@/components/wrappers/ClientWrapper';

interface QuizAttemptPageProps {
  params: Promise<{
    quizId: string;
  }>;
}

const QuizAttemptPage: React.FC<QuizAttemptPageProps> = ({ params }) => {
  const [quizId, setQuizId] = React.useState<number | null>(null);

  React.useEffect(() => {
    params.then((p) => {
      setQuizId(parseInt(p.quizId, 10));
    });
  }, [params]);

  if (!quizId) {
    return (
      <ClientWrapper>
        <div style={{ padding: '60px 20px', textAlign: 'center' }}>
          <p>Loading...</p>
        </div>
      </ClientWrapper>
    );
  }

  return (
    <ClientWrapper>
      <TakeQuiz quizId={quizId} />
    </ClientWrapper>
  );
};

export default QuizAttemptPage;
