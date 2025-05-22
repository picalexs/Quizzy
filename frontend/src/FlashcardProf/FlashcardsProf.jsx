import React, { useState } from 'react';
import './FlashcardsProf.css';

const FlashcardsProf = () => {
    const [showAddBox, setShowAddBox] = useState(false);
    const [showQuestionInput, setShowQuestionInput] = useState(false);
    const [showAnswerInput, setShowAnswerInput] = useState(false);
    const [question, setQuestion] = useState('');
    const [answers, setAnswers] = useState([]);
    const [savedFlashcards, setSavedFlashcards] = useState([]);

    const handleNewFlashcardClick = () => {
        setShowAddBox(true);
        setShowQuestionInput(false);
        setShowAnswerInput(false);
        setQuestion('');
        setAnswers([]);
    };

    const handleAddQuestionClick = () => {
        setShowQuestionInput(true);
    };

    const handleAddAnswerClick = () => {
        setShowAnswerInput(true);
        setAnswers([...answers, { text: '', correct: false }]);
    };

    const handleAnswerChange = (index, newText) => {
        const newAnswers = [...answers];
        newAnswers[index].text = newText;
        setAnswers(newAnswers);
    };

    const toggleCorrect = (index) => {
        const newAnswers = [...answers];
        newAnswers[index].correct = !newAnswers[index].correct;
        setAnswers(newAnswers);
    };

    const deleteAnswer = (index) => {
        const newAnswers = [...answers];
        newAnswers.splice(index, 1);
        setAnswers(newAnswers);
    };

    const handleSaveFlashcard = () => {
        if (!question.trim()) {
            alert("Please enter a question.");
            return;
        }

        const validAnswers = answers.filter(a => a.text.trim() !== '');
        if (validAnswers.length === 0) {
            alert("Please add at least one answer.");
            return;
        }

        const newFlashcard = {
            question: question.trim(),
            answers: validAnswers
        };

        setSavedFlashcards(prev => [...prev, newFlashcard]);

        setQuestion('');
        setAnswers([]);
        setShowAddBox(false);
    };

    return (
        <div className="flashcardsProf-container">
            <div className="flashcardsProf-blue-box">
                <div className="flashcardsProf-white-header">
                    <div className="flashcardsProf-white-header-text">
                        Create flashcards for “Software Engineering”
                    </div>
                </div>

                <div
                    className="flashcardsProf-new-flashcard"
                    onClick={handleNewFlashcardClick}
                    style={{ cursor: 'pointer' }}
                >
                    <div className="flashcardsProf-new-flashcard-text">+ New flashcard</div>
                </div>

                <div className="flashcardsProf-saved-cards">
                    {savedFlashcards.map((card, index) => (
                        <div key={index} className="flashcardsProf-card">
                            <div className="flashcardsProf-card-question">
                                <strong>Q:</strong> {card.question}
                            </div>
                            <div className="flashcardsProf-card-answers">
                                {card.answers.map((ans, i) => (
                                    <div key={i} className={`flashcardsProf-card-answer ${ans.correct ? 'correct' : 'wrong'}`}>
                                        {ans.text} {ans.correct}
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

                <div className="flashcardsProf-delimitation"></div>

                {showAddBox && (
                    <div className="flashcardsProf-add-box expanded">
                        <div
                            className={`flashcardsProf-add-box-question ${showQuestionInput ? 'flashcardsProf-add-box-question-expanded' : ''}`}
                            onClick={handleAddQuestionClick}
                            style={{ cursor: showQuestionInput ? 'default' : 'pointer' }}
                        >
                            {showQuestionInput ? (
                                <input
                                    type="text"
                                    placeholder="Enter your question"
                                    className="flashcardsProf-input"
                                    value={question}
                                    onChange={(e) => setQuestion(e.target.value)}
                                />
                            ) : (
                                '+ Add question'
                            )}
                        </div>

                        <div className="flashcardsProf-add-delimitation"></div>

                        <div className="flashcardsProf-answer-section">
                            <div className="flashcardsProf-answer-header">
                                Answers
                                <button className="flashcardsProf-add-answer-btn" onClick={handleAddAnswerClick}>
                                    + Add answer
                                </button>
                            </div>

                            {answers.map((answer, index) => (
                                <div
                                    key={index}
                                    className={`flashcardsProf-answer-row ${answer.correct ? 'correct' : 'wrong'}`}
                                >
                                    <input
                                        type="text"
                                        className="flashcardsProf-input"
                                        value={answer.text}
                                        onChange={(e) => handleAnswerChange(index, e.target.value)}
                                        placeholder="Enter an answer"
                                    />
                                    <button
                                        className={`flashcardsProf-correct-toggle ${answer.correct ? 'correct' : 'wrong'}`}
                                        onClick={() => toggleCorrect(index)}
                                    >
                                        {answer.correct ? 'Correct' : 'Wrong'}
                                    </button>
                                    <button
                                        className="flashcardsProf-delete-answer-btn"
                                        onClick={() => deleteAnswer(index)}
                                    >
                                        Delete
                                    </button>
                                </div>
                            ))}
                        </div>

                        <div className="flashcardsProf-add-box-buttons">
                            <button className="flashcardsProf-save-btn" onClick={handleSaveFlashcard}>
                                Save flashcard
                            </button>
                            <button className="flashcardsProf-delete-btn" onClick={handleSaveFlashcard}>
                                Delete flashcard
                            </button>
                            <button className="flashcardsProf-cancel-btn" onClick={() => setShowAddBox(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default FlashcardsProf;
