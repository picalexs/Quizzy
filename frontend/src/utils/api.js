import axios from 'axios';

const envUrl = import.meta.env.VITE_BACKEND_URL;
const BASE_URL =
    typeof envUrl === 'string' && envUrl.trim() !== ''
        ? envUrl
        : 'http://localhost:3000'; 

const axiosInstance = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' },
});

axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        //DEBUG REMOVE LATER
        console.log(`Making ${config.method.toUpperCase()} request to: ${config.baseURL}${config.url}`);
        console.log('Headers:', config.headers);
        if (config.data) {
            console.log('Body:', config.data);
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response) => {
        console.log(`Response status: ${response.status}`);
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            console.log('Interceptor: Unauthorized response (401) received.');
            if (window.location.pathname !== '/login') {
                console.log('Interceptor: Not on /login page. Clearing auth data and redirecting to /login.');
                localStorage.removeItem('authToken');
                localStorage.removeItem('user');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userId'); 
                window.location.href = '/login'; 
            } else {
                console.log('Interceptor: Already on /login page. Skipping redirect. Auth data might still be cleared.');
                localStorage.removeItem('authToken');
                localStorage.removeItem('user');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userId');
            }
        }
        return Promise.reject(error);
    }
);

const binaryAxiosInstance = axios.create({
    baseURL: BASE_URL,
    responseType: 'blob',
});

binaryAxiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        console.log(`Making binary ${config.method.toUpperCase()} request to: ${config.baseURL}${config.url}`);
        return config;
    },
    (error) => Promise.reject(error)
);


binaryAxiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.log('Interceptor (Binary): Unauthorized response (401) received.');
            if (window.location.pathname !== '/login') {
                console.log('Interceptor (Binary): Not on /login page. Clearing auth data and redirecting to /login.');
                localStorage.removeItem('authToken');
                localStorage.removeItem('user');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userId');
                window.location.href = '/login';
            } else {
                 console.log('Interceptor (Binary): Already on /login page. Skipping redirect.');
                localStorage.removeItem('authToken');
                localStorage.removeItem('user');
                localStorage.removeItem('userRole');
                localStorage.removeItem('userId');
            }
        }
        return Promise.reject(error);
    }
);


// Export helper functions for API calls
export const api = {
    get: (url) => axiosInstance.get(url),
    post: (url, data) => axiosInstance.post(url, data),
    put: (url, data) => axiosInstance.put(url, data),
    delete: (url) => axiosInstance.delete(url),
    getBinaryFile: (url, headers = {}) => binaryAxiosInstance.get(url, { headers })
};
