import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../utils/api';
import './PDFViewer.css';

function PDFViewer() {
    // Extract the full path from the URL
    const location = useLocation();
    const navigate = useNavigate();
    const fullPath = location.pathname.replace('/Material/path/', '');
    const stateTitle = location.state?.title;
    const { courseId, courseTitle } = location.state || {};

    const [pageMode, setPageMode] = useState('single');
    const [pdfUrl, setPdfUrl] = useState('');
    const [objectUrl, setObjectUrl] = useState('');
    const [title, setTitle] = useState(stateTitle || 'PDF Viewer');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [displayMode, setDisplayMode] = useState('object'); // 'iframe' or 'object'
    const [directUrl, setDirectUrl] = useState('');

    const fetchPdf = async () => {
        try {
            console.log('Full PDF Path:', fullPath);
            // Make sure path is properly decoded
            const path = decodeURIComponent(fullPath);

            // The PDF path should start with "cursuri" and end with ".pdf"
            const pathParts = path.split('/');

            if (pathParts.length >= 3 && pathParts[0] === 'cursuri') {
                const courseTitle = pathParts[1];
                const fileName = pathParts[pathParts.length - 1]; // Get the last part (PDF filename)

                // Set title from filename if not provided in state
                if (!stateTitle) {
                    setTitle(fileName.replace('.pdf', '') || 'Document');
                }

                // Create direct URL for fallback - use the full path as received
                const baseUrl = window.location.origin;
                const apiEndpoint = `/Material/path/${path}`;
                const directPdfUrl = `${baseUrl}${apiEndpoint}`;
                setDirectUrl(directPdfUrl);

                console.log('Fetching PDF from API endpoint:', apiEndpoint);

                // Use the api.getBinaryFile method to fetch the PDF
                const response = await api.getBinaryFile(apiEndpoint, {
                    'Accept': 'application/pdf'
                });

                console.log('PDF response received:', response.status);

                if (response.data && response.data.size > 0) {
                    // Create a blob URL
                    const blob = new Blob([response.data], { type: 'application/pdf' });
                    const blobUrl = URL.createObjectURL(blob);

                    console.log('Blob URL created, size:', blob.size);
                    setObjectUrl(blobUrl);
                    setPdfUrl(blobUrl);
                } else {
                    console.warn('Empty PDF response received');
                    // Fall back to direct URL
                    setPdfUrl(directPdfUrl);
                }
            } else {
                setError('Invalid PDF path format. Expected: cursuri/{courseTitle}/.../{fileName}.pdf');
                console.error('Invalid PDF path format:', path);
            }
        } catch (err) {
            console.error('Error fetching PDF:', err);
            
            // Try direct URL as fallback
            if (directUrl) {
                console.log('Using direct URL as fallback');
                setPdfUrl(directUrl);
            } else {
                setError(`Failed to load PDF: ${err.message || 'Unknown error'}`);
            }
        } finally {
            setLoading(false);
        }
    };
    
    useEffect(() => {
        if (fullPath) {
            fetchPdf();
        }
        
        return () => {
            // Clean up blob URL when component unmounts
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [fullPath]);

    // Function to toggle between iframe and object display modes
    const toggleDisplayMode = () => {
        setDisplayMode(prevMode => prevMode === 'iframe' ? 'object' : 'iframe');
    };

    const navigateBack = () => {
        if (courseId) {
            navigate(`/course/${courseId}`);
        } else {
            // Fallback to library if no course ID is provided
            navigate('/library');
        }
    };

    return (
        <div className="pdf-viewer-container">
            {/* Header */}
            <div className="pdf-header">
                <div className="header-left">
                    <button 
                        onClick={navigateBack}
                        className="back-button"
                        title={courseTitle ? `Back to ${courseTitle}` : 'Back to course'}
                    >
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                            <path d="M8.5 2.5l-6 6 6 6V11h7V5H8.5V2.5z"/>
                        </svg>
                        Back
                    </button>
                    <h1 className="pdf-title">{title}</h1>
                </div>
                
                <div className="header-right">
                    <div className="toolbar">
                        <button
                            className={`toolbar-btn ${pageMode === 'single' ? 'active' : ''}`}
                            onClick={() => setPageMode('single')}
                            title="Single Page View"
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                                <path d="M3 2h10a1 1 0 011 1v10a1 1 0 01-1 1H3a1 1 0 01-1-1V3a1 1 0 011-1zm1 2v8h8V4H4z"/>
                            </svg>
                        </button>
                        
                        <button
                            className={`toolbar-btn ${pageMode === 'continuous' ? 'active' : ''}`}
                            onClick={() => setPageMode('continuous')}
                            title="Continuous View"
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                                <path d="M2 2h4v4H2V2zm8 0h4v4h-4V2zM2 10h4v4H2v-4zm8 0h4v4h-4v-4z"/>
                            </svg>
                        </button>
                        
                        <div className="toolbar-divider"></div>
                        
                        <button 
                            className="toolbar-btn"
                            onClick={toggleDisplayMode} 
                            title={`Switch to ${displayMode === 'iframe' ? 'Object' : 'Iframe'} renderer`}
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                                <path d="M8 4a4 4 0 100 8 4 4 0 000-8zM0 8a8 8 0 1116 0A8 8 0 010 8z"/>
                            </svg>
                            {displayMode === 'iframe' ? 'Object' : 'Iframe'}
                        </button>

                        {directUrl && (
                            <a 
                                href={directUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="toolbar-btn external-link"
                                title="Open in new tab"
                            >
                                <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                                    <path d="M6.22 8.72a.75.75 0 001.06 1.06l5.22-5.22v1.69a.75.75 0 001.5 0V3.75a.75.75 0 00-.75-.75h-2.5a.75.75 0 000 1.5h1.69L6.22 8.72z"/>
                                    <path d="M3.5 6.75c0-.69.56-1.25 1.25-1.25H7A.75.75 0 007 4H4.75A2.75 2.75 0 002 6.75v4.5A2.75 2.75 0 004.75 14h4.5A2.75 2.75 0 0012 11.25V9a.75.75 0 00-1.5 0v2.25c0 .69-.56 1.25-1.25 1.25h-4.5c-.69 0-1.25-.56-1.25-1.25v-4.5z"/>
                                </svg>
                            </a>
                        )}
                    </div>
                </div>
            </div>

            {/* Error Display */}
            {error && (
                <div className="error-banner">
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                        <path d="M8 15A7 7 0 118 1a7 7 0 010 14zm0 1A8 8 0 108 0a8 8 0 000 16z"/>
                        <path d="M7.002 11a1 1 0 112 0 1 1 0 01-2 0zM7.1 4.995a.905.905 0 111.8 0l-.35 3.507a.552.552 0 01-1.1 0L7.1 4.995z"/>
                    </svg>
                    {error}
                </div>
            )}

            {/* PDF Content */}
            <div className="pdf-content">
                {loading ? (
                    <div className="loading-container">
                        <div className="loading-spinner"></div>
                        <p>Loading PDF...</p>
                    </div>
                ) : pdfUrl ? (
                    <div 
                        className="pdf-viewer-wrapper"
                        style={{
                            overflow: pageMode === 'single' ? 'hidden' : 'auto'
                        }}
                    >
                        {displayMode === 'iframe' ? (
                            <iframe
                                title="PDF Viewer"
                                src={pdfUrl}
                                className="pdf-iframe"
                                allowFullScreen={true}
                            />
                        ) : (
                            <object
                                data={pdfUrl}
                                type="application/pdf"
                                className="pdf-object"
                            >
                                <div className="pdf-fallback">
                                    <p>Your browser doesn't support PDF viewing.</p>
                                    <a href={pdfUrl} download={`${title}.pdf`} className="download-link">
                                        Download PDF
                                    </a>
                                </div>
                            </object>
                        )}
                    </div>
                ) : (
                    <div className="no-content">
                        <svg width="48" height="48" viewBox="0 0 16 16" fill="currentColor">
                            <path d="M5.854 4.854a.5.5 0 10-.708-.708l-3.5 3.5a.5.5 0 000 .708l3.5 3.5a.5.5 0 00.708-.708L2.707 8l3.147-3.146zm4.292 0a.5.5 0 01.708-.708l3.5 3.5a.5.5 0 010 .708l-3.5 3.5a.5.5 0 01-.708-.708L13.293 8l-3.147-3.146z"/>
                        </svg>
                        <p>PDF could not be loaded</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default PDFViewer;