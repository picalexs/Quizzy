import React, { useState, useEffect } from 'react';
import './FlashcardsProf.css';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../utils/api';

const FlashcardsProf = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { courseId, courseTitle, questionId, question: initialQuestion } = location.state || {};
    
    const [showAddBox, setShowAddBox] = useState(false);
    const [question, setQuestion] = useState('');
    const [answers, setAnswers] = useState([]);
    const [savedQuestions, setSavedQuestions] = useState([]);
    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [error, setError] = useState(null);
    const [errorDetails, setErrorDetails] = useState(null);
    const [currentQuestionId, setCurrentQuestionId] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [originalAnswers, setOriginalAnswers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentTestId, setCurrentTestId] = useState(null);

    const createTestForCourse = async () => {
        try {
            const userId = localStorage.getItem("userId");
            if (!userId || !courseId) {
                setError("Missing user ID or course ID");
                return;
            }

            const testData = {
                title: `${courseTitle} Test`,
                description: "Course test questions",
                date: new Date(),
                professorId: parseInt(userId),
                courseId: parseInt(courseId)
            };

            const response = await api.post('/tests', testData);
            if (response.data && response.data.id) {
                setCurrentTestId(response.data.id);
                return response.data.id;
            }
        } catch (err) {
            console.error("Error creating test:", err);
            setError("Failed to create test for course");
        }
        return null;
    };

    const handleBack = () => {
        navigate(`/course/${courseId}`, {
            state: { course: { id: courseId, title: courseTitle } }
        });
    };

    useEffect(() => {
        const fetchQuestions = async () => {
            if (!courseId) {
                setError("Course ID is missing");
                setLoading(false);
                return;
            }
            
            try {
                // First try to get the test for this course
                const testResponse = await api.get(`/tests/course/${courseId}`);
                if (testResponse.data && testResponse.data.length > 0) {
                    setCurrentTestId(testResponse.data[0].id);
                } else {
                    // If no test exists, create one
                    await createTestForCourse();
                }

                // Get all questions for the course
                const response = await api.get(`/questions/course/${courseId}`);
                if (response.data && Array.isArray(response.data)) {
                    setSavedQuestions(response.data);
                }
                
                if (initialQuestion && questionId) {
                    setCurrentQuestionId(questionId);
                    setQuestion(initialQuestion.questionText);
                    setIsEditing(true);
                    setShowAddBox(true);
                    
                    const answersResponse = await api.get(`/answers/question/${questionId}`);
                    if (answersResponse.data && Array.isArray(answersResponse.data)) {
                        const formattedAnswers = answersResponse.data.map(ans => ({
                            id: ans.id,
                            text: ans.optionText,
                            isCorrect: ans.correct
                        }));
                        setAnswers(formattedAnswers);
                        setOriginalAnswers(formattedAnswers);
                    }
                }
            } catch (err) {
                console.error("Error loading data:", err);
                setError(err.response?.data?.message || "Failed to load questions");
            } finally {
                setLoading(false);
            }
        };
        
        fetchQuestions();
    }, [courseId, questionId, initialQuestion]);

    const handleNewQuestion = () => {
        setCurrentQuestionId(null);
        setIsEditing(false);
        setShowAddBox(true);
        setQuestion('');
        setAnswers([]);
        setOriginalAnswers([]);
        setError(null);
    };

    const handleAddAnswer = () => {
        setAnswers([...answers, { text: '', isCorrect: false }]);
    };

    const handleAnswerChange = (index, newText) => {
        const newAnswers = [...answers];
        newAnswers[index].text = newText;
        setAnswers(newAnswers);
    };

    const toggleCorrect = (index) => {
        const newAnswers = [...answers];
        newAnswers[index].isCorrect = !newAnswers[index].isCorrect;
        setAnswers(newAnswers);
    };

    const deleteAnswer = (index) => {
        const newAnswers = [...answers];
        newAnswers.splice(index, 1);
        setAnswers(newAnswers);
    };

    const handleQuestionClick = async (questionToEdit) => {
        setIsEditing(true);
        setCurrentQuestionId(questionToEdit.id);
        setQuestion(questionToEdit.questionText);
        setShowAddBox(true);
        setError(null);
        
        try {
            const answersResponse = await api.get(`/answers/question/${questionToEdit.id}`);
            if (answersResponse.data && Array.isArray(answersResponse.data)) {
                const formattedAnswers = answersResponse.data.map(ans => ({
                    id: ans.id,
                    text: ans.optionText,
                    isCorrect: ans.correct
                }));
                setAnswers(formattedAnswers);
                setOriginalAnswers(formattedAnswers);
            }
        } catch (err) {
            console.error("Error loading answers:", err);
            setError("Failed to load answers");
            setAnswers([]);
            setOriginalAnswers([]);
        }
    };

    const handleDeleteQuestion = async (questionId) => {
        if (!questionId) return;
        
        setDeleting(true);
        setError(null);
        
        try {
            await api.delete(`/questions/${questionId}`);
            setSavedQuestions(prev => prev.filter(q => q.id !== questionId));
            setShowAddBox(false);
            setCurrentQuestionId(null);
            setIsEditing(false);
        } catch (err) {
            console.error("Error deleting question:", err);
            setError("Failed to delete question");
        } finally {
            setDeleting(false);
        }
    };

    const handleSave = async () => {
        if (!question.trim()) {
            setError("Please provide a question");
            return;
        }

        const validAnswers = answers.filter(a => a.text.trim());
        if (validAnswers.length === 0) {
            setError("Please provide at least one answer");
            return;
        }

        if (!currentTestId) {
            setError("No test available for this course");
            return;
        }

        setSaving(true);
        setError(null);
        
        try {
            // Step 1: Save the question with test ID
            const questionData = {
                questionText: question,
                testId: currentTestId // Using testId instead of courseId
            };
            
            let savedQuestionId;
            if (!isEditing) {
                const questionResponse = await api.post('/questions', questionData);
                savedQuestionId = questionResponse.data.id;
            } else {
                await api.put(`/questions/${currentQuestionId}`, questionData);
                savedQuestionId = currentQuestionId;
            }

            // Step 2: Handle existing answers if editing
            if (isEditing) {
                const answersToDelete = originalAnswers.filter(
                    orig => !answers.some(curr => curr.id === orig.id)
                );
                
                for (const answer of answersToDelete) {
                    await api.delete(`/answers/${answer.id}`);
                }
            }
                
            // Step 3: Save all answers
            for (const answer of validAnswers) {
                const answerData = {
                    optionText: answer.text.trim(),
                    correct: answer.isCorrect,
                    questionId: savedQuestionId
                };

                if (answer.id) {
                    await api.put(`/answers/${answer.id}`, answerData);
                } else {
                    await api.post('/answers', answerData);
                }
            }
            
            // Step 4: Refresh questions list
            const response = await api.get(`/questions/test/${currentTestId}`);
            if (response.data && Array.isArray(response.data)) {
                setSavedQuestions(response.data);
            }
                
            // Step 5: Reset form
            setShowAddBox(false);
            setQuestion('');
            setAnswers([]);
            setCurrentQuestionId(null);
            setIsEditing(false);
            
        } catch (err) {
            console.error("Error saving question:", err);
            setError(err.response?.data?.message || "Failed to save question");
            setErrorDetails(err.message);
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return <div className="flashcardsProf-loading">Loading...</div>;
    }

    return (
        <div className="flashcardsProf-container">
            <div className="flashcardsProf-blue-box">
                <div className="flashcardsProf-white-header">
                    <button onClick={handleBack} className="flashcardsProf-back-btn">
                        ← Back
                    </button>
                    <div className="flashcardsProf-white-header-text">
                        Create test questions for "{courseTitle || 'Course'}"
                    </div>
                </div>

                <button className="flashcardsProf-new-flashcard" onClick={handleNewQuestion}>
                    <span className="flashcardsProf-new-flashcard-text">+ New test question</span>
                </button>

                <div className="flashcardsProf-saved-cards">
                    {savedQuestions.map((q) => (
                        <div 
                            key={q.id} 
                            className="flashcardsProf-card"
                            onClick={() => handleQuestionClick(q)}
                        >
                            {q.questionText}
                        </div>
                    ))}
                </div>

                <div className="flashcardsProf-delimitation"></div>

                {showAddBox && (
                    <div className="flashcardsProf-add-box">
                        <div className="flashcardsProf-add-box-question">
                            <input
                                type="text"
                                value={question}
                                onChange={(e) => setQuestion(e.target.value)}
                                placeholder="Enter your question"
                                className="flashcardsProf-input"
                            />
                        </div>

                        <div className="flashcardsProf-add-delimitation"></div>

                        <div className="flashcardsProf-answer-section">
                            <div className="flashcardsProf-answer-header">
                                <span>Answers</span>
                                <button 
                                    className="flashcardsProf-add-answer-btn"
                                    onClick={handleAddAnswer}
                                >
                                    + Add answer
                                </button>
                            </div>

                            {answers.map((answer, index) => (
                                <div key={index} className="flashcardsProf-answer-row">
                                    <input
                                        type="text"
                                        value={answer.text}
                                        onChange={(e) => handleAnswerChange(index, e.target.value)}
                                        placeholder="Enter answer"
                                        className="flashcardsProf-input"
                                    />
                                    <button
                                        className={`flashcardsProf-correct-toggle ${answer.isCorrect ? 'correct' : 'wrong'}`}
                                        onClick={() => toggleCorrect(index)}
                                    >
                                        {answer.isCorrect ? 'Correct' : 'Wrong'}
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
                            <button 
                                className="flashcardsProf-cancel-btn" 
                                onClick={() => {
                                    setShowAddBox(false);
                                    setCurrentQuestionId(null);
                                    setIsEditing(false);
                                }}
                            >
                                Cancel
                            </button>
                            {isEditing && (
                                <button 
                                    className="flashcardsProf-delete-btn" 
                                    onClick={() => handleDeleteQuestion(currentQuestionId)}
                                    disabled={deleting}
                                >
                                    {deleting ? 'Deleting...' : 'Delete question'}
                                </button>
                            )}
                            <button
                                className="flashcardsProf-save-btn" 
                                onClick={handleSave}
                                disabled={saving}
                            >
                                {saving ? 'Saving...' : (isEditing ? 'Update question' : 'Save question')}
                            </button>
                        </div>
                    </div>
                )}

                {error && (
                    <div className="flashcardsProf-error-notification">
                        <p>{error}</p>
                        {errorDetails && <p className="flashcardsProf-error-details">{errorDetails}</p>}
                    </div>
                )}
            </div>
        </div>
    );
};

export default FlashcardsProf;