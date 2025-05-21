import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../utils/api'; // Relative path
import './PDFViewer.css';

function PDFViewer() {
    const { pdfPath } = useParams();
    const [pageMode, setPageMode] = useState('single');
    const [pdfUrl, setPdfUrl] = useState('');
    const [objectUrl, setObjectUrl] = useState('');
    const [title, setTitle] = useState('PDF Viewer');

    const fetchPdfBlob = async () => {
        try {
            const response = await api.get(`/materials/${pdfPath}`, {
                responseType: 'blob'
            });

            // Extract filename from content-disposition header if available
            const contentDisposition = response.headers['content-disposition'];
            let filename = 'Document';
            if (contentDisposition) {
                const filenameMatch = contentDisposition.match(/filename="?(.+)"?/);
                if (filenameMatch && filenameMatch[1]) {
                    filename = filenameMatch[1].replace(/\.pdf$/i, '');
                }
            }
            setTitle(filename);

            const blob = new Blob([response.data], { type: 'application/pdf' });
            const url = URL.createObjectURL(blob);
            setObjectUrl(url);
        } catch (err) {
            console.error('Failed to fetch PDF Blob:', err);
        }
    };

    const updatePdfUrl = () => {
        if (!objectUrl) return;
        const zoomLevel = Math.max(50, Math.min(200, (window.innerWidth / 1000) * 100));
        const viewMode = pageMode === 'continuous' ? 'FitH' : 'FitV';
        const url = `${objectUrl}#zoom=${zoomLevel}&view=${viewMode}&t=${Date.now()}`;
        setPdfUrl(url);
    };

    useEffect(() => {
        fetchPdfBlob();
        return () => {
            if (objectUrl) {
                URL.revokeObjectURL(objectUrl);
            }
        };
    }, [pdfPath]);

    useEffect(() => {
        updatePdfUrl();
        window.addEventListener('resize', updatePdfUrl);
        return () => window.removeEventListener('resize', updatePdfUrl);
    }, [pageMode, objectUrl]);

    return (
        <div className="App">
            <header className="App-header">
                <div className="App-container">
                    <div className="Pdf-header">
                        <div className="Pdf-section dark">
                            <h1 className="Pdf-title">{title}</h1>
                        </div>

                        <div className="Pdf-section light">
                            <div className="App-link-container">
                                {objectUrl && (
                                    <a
                                        className="App-link"
                                        href={objectUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        title="Open Full Screen"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                            <path d="M3 3H9V9H3V3ZM15 3H21V9H15V3ZM3 15H9V21H3V15ZM15 15H21V21H15V15Z" />
                                        </svg>
                                    </a>
                                )}
                            </div>

                            <div className="page-toggle-buttons">
                                <button
                                    className={`toggle-btn ${pageMode === 'single' ? 'active' : ''}`}
                                    onClick={() => setPageMode('single')}
                                    title="Single Page"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                        <path d="M4 4H20V20H4V4ZM6 6V18H18V6H6Z" />
                                    </svg>
                                </button>
                                <button
                                    className={`toggle-btn ${pageMode === 'continuous' ? 'active' : ''}`}
                                    onClick={() => setPageMode('continuous')}
                                    title="Continuous Pages"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
                                        <path d="M3 3H9V9H3V3ZM15 3H21V9H15V3ZM3 15H9V21H3V15ZM15 15H21V21H15V15Z" />
                                    </svg>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div
                        id="iframe-wrapper"
                        style={{
                            width: '100%',
                            height: '800px',
                            overflow: pageMode === 'single' ? 'hidden' : 'auto',
                        }}
                    >
                        {pdfUrl && (
                            <iframe
                                id="pdf-iframe"
                                title="PDF Preview"
                                src={pdfUrl}
                                width="100%"
                                height="100%"
                                style={{ border: 'none' }}
                            />
                        )}
                    </div>
                </div>
            </header>
        </div>
    );
}

export default PDFViewer;